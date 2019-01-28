package app.drewromanyk.com.minesweeper.interfaces

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.games.Player

/**
 * Interface to enable activity to pass action to game fragment
 */
interface GameUiHandler {
    fun toggleClickMode()
}
