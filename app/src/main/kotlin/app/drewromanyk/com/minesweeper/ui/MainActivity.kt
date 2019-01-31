package app.drewromanyk.com.minesweeper.ui

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import app.drewromanyk.com.minesweeper.BuildConfig
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.enums.GameStatus
import app.drewromanyk.com.minesweeper.interfaces.GameUiHandler
import app.drewromanyk.com.minesweeper.interfaces.ProfileUiHandler
import app.drewromanyk.com.minesweeper.util.PremiumUtils
import app.drewromanyk.com.minesweeper.util.UserPrefStorage
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.games.*
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_main.*

// request codes we use when invoking an external activity
private const val RC_UNUSED = 5001
private const val RC_SIGN_IN = 9001

/**
 * Activity that handles everything to do with the main navigation or contact with google services
 */
class MainActivity : AppCompatActivity() {
    private var googleSignInClient: GoogleSignInClient? = null
    private var achievementsClient: AchievementsClient? = null
    private var leaderboardsClient: LeaderboardsClient? = null
    private var playersClient: PlayersClient? = null
    var currentPlayer: Player? = null

    var profileUiHandler: ProfileUiHandler? = null
    var gameUiHandler: GameUiHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(UserPrefStorage.getUiThemeMode(this).themeResourceId)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this, getString(R.string.ad_app_id))

        setupGoogleGames()
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(this, R.id.mainNavHost).navigateUp()
    }

    override fun onResume() {
        super.onResume()

        googleSignInClient!!.silentSignIn().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                onConnected(task.result!!)
            } else {
                onDisconnected()
            }
        }
    }
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode
        if (!isFinishing) {
            if (gameUiHandler != null && UserPrefStorage.getVolumeButton(this)) {
                when (keyCode) {
                    KeyEvent.KEYCODE_VOLUME_UP -> {
                        if (action == KeyEvent.ACTION_DOWN) {
                            gameUiHandler!!.toggleClickMode()
                        }
                        return true
                    }
                    KeyEvent.KEYCODE_VOLUME_DOWN -> {
                        if (action == KeyEvent.ACTION_DOWN) {
                            gameUiHandler!!.toggleClickMode()
                        }
                        return true
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(this) != null
    }

    fun startSignInIntent() {
        startActivityForResult(googleSignInClient!!.signInIntent, RC_SIGN_IN)
    }

    fun startAchievementIntent() {
        if (isSignedIn()) {
            achievementsClient!!.achievementsIntent
                    .addOnSuccessListener { intent -> startActivityForResult(intent, RC_UNUSED) }
        } else {
            startSignInIntent()
        }
    }

    fun startLeaderboardIntent() {
        if (isSignedIn()) {
            leaderboardsClient!!.allLeaderboardsIntent
                    .addOnSuccessListener { intent -> startActivityForResult(intent, RC_UNUSED) }
        } else {
            startSignInIntent()
        }
    }

    fun startSignOutProcess() {
        googleSignInClient!!.signOut().addOnCompleteListener(this) {
            onDisconnected()
        }
    }

    fun navigateToGame(difficulty: GameDifficulty) {
        val bundle = Bundle()
        bundle.putString("gameDifficulty", difficulty.name)
        findNavController(this, R.id.mainNavHost).navigate(R.id.action_main_to_game, bundle)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!PremiumUtils.instance.handleBillingActivityResults(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == RC_SIGN_IN) {
                handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data))
            } else if (requestCode == RC_UNUSED && resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
                googleSignInClient?.signOut()
                achievementsClient = null
                leaderboardsClient = null
                playersClient = null
            }
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            onConnected(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            onConnected(null)
        }
    }

    private fun setupGoogleGames() {
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build())
    }

    private fun onConnected(account: GoogleSignInAccount?) {
        if (account == null) {
            onDisconnected()
            profileUiHandler?.onSignIn(null)
        } else {
            Games.getGamesClient(this, account).setViewForPopups(container as View)
            achievementsClient = Games.getAchievementsClient(this, account)
            leaderboardsClient = Games.getLeaderboardsClient(this, account)
            playersClient = Games.getPlayersClient(this, account)
            playersClient!!.currentPlayer.addOnCompleteListener {
                currentPlayer = it.result
                this.profileUiHandler?.onSignIn(currentPlayer)
            }.addOnCanceledListener {
                this.profileUiHandler?.onSignIn(null)
            }.addOnFailureListener {
                this.profileUiHandler?.onSignIn(null)
            }
        }
    }

    private fun onDisconnected() {
        achievementsClient = null
        leaderboardsClient = null
        playersClient = null
        currentPlayer = null
    }

    fun updateLeaderboards(gameStatus: GameStatus, gameDifficulty: GameDifficulty, score: Long, millis: Long) {
        val achievementSeconds = longArrayOf(20_000, 4000_000, 150_000)
        val achievementWin = arrayOf(BuildConfig.ACHIEVEMENT_EASY, BuildConfig.ACHIEVEMENT_MEDIUM, BuildConfig.ACHIEVEMENT_EXPERT)
        val achievementSpeed = arrayOf(BuildConfig.ACHIEVEMENT_FAST, BuildConfig.ACHIEVEMENT_QUICK, BuildConfig.ACHIEVEMENT_SWIFT)
        val leaderboardScores = arrayOf(BuildConfig.LEADERBOARD_EASY_BEST_SCORES, BuildConfig.LEADERBOARD_MEDIUM_BEST_SCORES, BuildConfig.LEADERBOARD_EXPERT_BEST_SCORES)
        val leaderboardTimes = arrayOf(BuildConfig.LEADERBOARD_EASY_BEST_TIMES, BuildConfig.LEADERBOARD_MEDIUM_BEST_TIMES, BuildConfig.LEADERBOARD_EXPERT_BEST_TIMES)
        val leaderboardStreaks = arrayOf(BuildConfig.LEADERBOARD_EASY_BEST_STREAK, BuildConfig.LEADERBOARD_MEDIUM_BEST_STREAKs, BuildConfig.LEADERBOARD_EXPERT_BEST_STREAKs)

        if (gameStatus == GameStatus.VICTORY) {
            val fba = FirebaseAnalytics.getInstance(this)
            fba.logEvent("game_over_google_games_victory", null)
            if (!isSignedIn()) {
                return
            }
            // Skip non ranked difficulty
            if ((gameDifficulty === GameDifficulty.CUSTOM) || (gameDifficulty === GameDifficulty.RESUME)) {
                return
            }

            // Offset is 2 for RESUME and CUSTOM
            val gameDiffIndex = gameDifficulty.ordinal - 2
            fba.logEvent("game_over_games_achievements", null)
            achievementsClient!!.unlock(achievementWin[gameDiffIndex])
            if (millis < achievementSeconds[gameDiffIndex]) {
                achievementsClient!!.unlock(achievementSpeed[gameDiffIndex])
            }

            fba.logEvent("game_over_games_leaderboards", null)
            leaderboardsClient!!.submitScore(leaderboardScores[gameDiffIndex], score)
            val seconds = Math.ceil(millis / 1000.0).toInt()
            leaderboardsClient!!.submitScore(leaderboardTimes[gameDiffIndex], seconds.toLong())
            leaderboardsClient!!.submitScore(leaderboardStreaks[gameDiffIndex], UserPrefStorage.getCurWinStreakForDifficulty(this, gameDifficulty).toLong())
            fba.logEvent("game_over_games_finished_update", null)
        }
    }
}
