package app.drewromanyk.com.minesweeper.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.interfaces.ProfileUiHandler
import app.drewromanyk.com.minesweeper.util.PremiumUtils
import app.drewromanyk.com.minesweeper.util.UserPrefStorage
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.games.*


class MainActivity : AppCompatActivity() {
    private var googleSignInClient: GoogleSignInClient? = null
    private var achievementsClient: AchievementsClient? = null
    private var leaderboardsClient: LeaderboardsClient? = null
    private var playersClient: PlayersClient? = null
    var currentPlayer: Player? = null

    // request codes we use when invoking an external activity
    protected val RC_UNUSED = 5001
    protected val RC_SIGN_IN = 9001

    var profileUiHandler: ProfileUiHandler? = null

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

    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(this) != null
    }

    fun startSignInIntent() {
        startActivityForResult(googleSignInClient!!.signInIntent, RC_SIGN_IN)
    }

    fun startAchivementIntent() {
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
}
