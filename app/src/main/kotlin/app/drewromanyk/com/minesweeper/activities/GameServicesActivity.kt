package app.drewromanyk.com.minesweeper.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import app.drewromanyk.com.minesweeper.BuildConfig
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.enums.GameStatus
import app.drewromanyk.com.minesweeper.util.UserPrefStorage
import com.crashlytics.android.Crashlytics
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.AchievementsClient
import com.google.android.gms.games.Games
import com.google.android.gms.games.LeaderboardsClient
import com.google.firebase.analytics.FirebaseAnalytics


/**
 * Created by Drew on 11/6/15.
 * This is a Base Activity for all activities in order to unify Google Games Services
 */
abstract class GameServicesActivity: AppCompatActivity() {
    protected var googleSignInClient: GoogleSignInClient? = null
    protected var achievementsClient: AchievementsClient? = null
    protected var leaderboardsClient: LeaderboardsClient? = null

    // request codes we use when invoking an external activity
    protected val RC_UNUSED = 5001
    protected val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupGoogleGames()
    }

    private fun setupGoogleGames() {
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build())
    }

    fun startSignInIntent() {
        startActivityForResult(googleSignInClient!!.signInIntent, RC_SIGN_IN)
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

    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(this) != null
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

    open fun onConnected(googleSignInAccount: GoogleSignInAccount) {
        achievementsClient = Games.getAchievementsClient(this, googleSignInAccount)
        leaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount)
    }

    fun onDisconnected() {
        achievementsClient = null
        leaderboardsClient = null
    }

}
