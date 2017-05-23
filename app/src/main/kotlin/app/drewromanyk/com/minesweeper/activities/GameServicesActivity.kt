package app.drewromanyk.com.minesweeper.activities

import android.app.ActivityManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.games.Games
import com.google.android.gms.games.GamesActivityResultCodes
import com.google.android.gms.plus.Plus
import com.google.firebase.analytics.FirebaseAnalytics

import app.drewromanyk.com.minesweeper.BuildConfig
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.enums.GameStatus
import app.drewromanyk.com.minesweeper.enums.ResultCodes
import app.drewromanyk.com.minesweeper.util.BaseGameUtils
import app.drewromanyk.com.minesweeper.util.UserPrefStorage

/**
 * Created by Drew on 11/6/15.
 * This is a Base Activity for all activities in order to unify Google Games Services
 */
abstract class GameServicesActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // GOOGLE GAMES
    var googleApiClient: GoogleApiClient? = null
        private set
    private var isResolvingConnectionFailure = false
    private var signInClicked = false
    private var autoStartSignInFlow = true

    protected fun onSignInClick() {
        signInClicked = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupTaskActivityInfo()
        setupGoogleGames()
    }

    private fun setupTaskActivityInfo() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Only for LOLLIPOP and newer versions
            if (this !is MainActivity)
                window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)

            val tDesc = ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_icon),
                    ContextCompat.getColor(this, R.color.primary_task))
            setTaskDescription(tDesc)
        }
    }

    private fun setupGoogleGames() {
        autoStartSignInFlow = (this is MainActivity) && UserPrefStorage.isFirstTime(this)
        UserPrefStorage.setFirstTime(this, false)

        // Create the Google API Client with access to Plus and Games
        googleApiClient = GoogleApiClient.Builder(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
    }

    fun updateLeaderboards(gameStatus: GameStatus, gameDifficulty: GameDifficulty, score: Long, millis: Long) {
        val achievementSeconds = longArrayOf(20_000, 60_000, 150_000)
        val achievementWin = arrayOf(BuildConfig.ACHIEVEMENT_EASY, BuildConfig.ACHIEVEMENT_MEDIUM, BuildConfig.ACHIEVEMENT_EXPERT)
        val achievementSpeed = arrayOf(BuildConfig.ACHIEVEMENT_FAST, BuildConfig.ACHIEVEMENT_QUICK, BuildConfig.ACHIEVEMENT_SWIFT)
        val leaderboardScores = arrayOf(BuildConfig.LEADERBOARD_EASY_BEST_SCORES, BuildConfig.LEADERBOARD_MEDIUM_BEST_SCORES, BuildConfig.LEADERBOARD_EXPERT_BEST_SCORES)
        val leaderboardTimes = arrayOf(BuildConfig.LEADERBOARD_EASY_BEST_TIMES, BuildConfig.LEADERBOARD_MEDIUM_BEST_TIMES, BuildConfig.LEADERBOARD_EXPERT_BEST_TIMES)
        val leaderboardStreaks = arrayOf(BuildConfig.LEADERBOARD_EASY_BEST_STREAK, BuildConfig.LEADERBOARD_MEDIUM_BEST_STREAKs, BuildConfig.LEADERBOARD_EXPERT_BEST_STREAKs)

        if (gameStatus == GameStatus.VICTORY) {
            val fba = FirebaseAnalytics.getInstance(this)
            fba.logEvent("game_over_google_games_victory", null)
            if (googleApiClient?.isConnected as Boolean) {
                // Skip non ranked difficulty
                if ((gameDifficulty === GameDifficulty.CUSTOM) || (gameDifficulty === GameDifficulty.RESUME))
                    return

                // Offset is 2 for RESUME and CUSTOM
                val gameDiffIndex = gameDifficulty.ordinal - 2
                fba.logEvent("game_over_games_achievements", null)
                Games.Achievements.unlock(googleApiClient, "" + achievementWin[gameDiffIndex])
                if (millis < achievementSeconds[gameDiffIndex]) {
                    Games.Achievements.unlock(googleApiClient, "" + achievementSpeed[gameDiffIndex])
                }

                fba.logEvent("game_over_games_leaderboards", null)
                Games.Leaderboards.submitScore(googleApiClient,
                        "" + leaderboardScores[gameDiffIndex],
                        score)
                val seconds = Math.ceil(millis / 1000.0).toInt()
                Games.Leaderboards.submitScore(googleApiClient,
                        "" + leaderboardTimes[gameDiffIndex],
                        seconds.toLong())
                Games.Leaderboards.submitScore(googleApiClient,
                        "" + leaderboardStreaks[gameDiffIndex],
                        UserPrefStorage.getCurWinStreakForDifficulty(this, gameDifficulty).toLong())
                fba.logEvent("game_over_games_finished_update", null)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        googleApiClient!!.connect()
    }

    override fun onStop() {
        super.onStop()

        if (googleApiClient!!.isConnected) {
            googleApiClient!!.disconnect()
        }
    }

    /*
     * GOOGLE GAME CONNECTION
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ResultCodes.SIGN_IN.ordinal) {
            signInClicked = false
            isResolvingConnectionFailure = false
            if ((googleApiClient != null) && (resultCode == AppCompatActivity.RESULT_OK)) {
                googleApiClient!!.connect()
            }
        }

        if ((googleApiClient != null) && (resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED)) {
            googleApiClient!!.disconnect()
        }
    }

    override fun onConnected(bundle: Bundle?) {}

    override fun onConnectionSuspended(i: Int) {
        googleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        if (isResolvingConnectionFailure) {
            // Already resolving
            return
        }

        // If the sign in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (signInClicked || autoStartSignInFlow) {
            autoStartSignInFlow = false
            signInClicked = false
            isResolvingConnectionFailure = true

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    googleApiClient!!, connectionResult,
                    ResultCodes.SIGN_IN.ordinal, getString(R.string.signin_other_error))) {
                isResolvingConnectionFailure = false
            }
        }
    }
}
