package app.drewromanyk.com.minesweeper.interfaces

import app.drewromanyk.com.minesweeper.enums.ClickMode

/**
 * Created by drewromanyk on 5/23/17.
 */
interface MinesweeperUiHandler {
    fun onGameTimerTick(gameTime: Long, score: Double)
    fun onFlagChange(clickMode: ClickMode)
    fun onDefeat()
    fun onVictory(score: Long, time: Long)
}