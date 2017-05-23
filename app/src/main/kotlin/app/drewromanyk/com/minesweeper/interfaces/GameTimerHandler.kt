package app.drewromanyk.com.minesweeper.interfaces

/**
 * Created by Drew Romanyk on 5/20/17.
 * Interface to allow for event handlers for the timer
 */
interface GameTimerHandler {
    fun onTimerTick(gameTime: Long)
}