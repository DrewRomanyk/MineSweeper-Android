package app.drewromanyk.com.minesweeper.interfaces

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.games.Player

/**
 * Created by Drew on 12/11/15.
 * PlayNavigator
 * Interface to enable activity needed functions in the recycler view
 */
interface ProfileUiHandler {
    fun onSignIn(player: Player?)
}
