package app.drewromanyk.com.minesweeper.interfaces

import app.drewromanyk.com.minesweeper.models.Cell

/**
 * Created by drewromanyk on 5/20/17.
 */
interface MinesweeperHandler : GameTimerHandler {
    fun isSwiftOpenEnabled(): Boolean

    fun onSwiftChange()

    fun onCellChange(cell: Cell, flagChange: Boolean)

    fun onGameStatusChange(cell: Cell)
}