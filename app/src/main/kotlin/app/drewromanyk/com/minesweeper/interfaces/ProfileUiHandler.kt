package app.drewromanyk.com.minesweeper.interfaces

import com.google.android.gms.games.Player

/**
 * Interface to enable activity to send the player info to the play fragment
 */
interface ProfileUiHandler {
    fun onSignIn(player: Player?)
}
