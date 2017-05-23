package app.drewromanyk.com.minesweeper.interfaces

import app.drewromanyk.com.minesweeper.models.Cell

/**
 * Created by Drew Romanyk on 5/20/17.
 * Interface for handling all UI related operations for Minesweeper
 */
interface MinesweeperHandler : GameTimerHandler {
    fun isSwiftOpenEnabled(): Boolean

    fun onSwiftChange()

    fun onCellChange(cell: Cell, flagChange: Boolean)

    fun onGameStatusChange(cell: Cell)
}