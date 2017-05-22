package app.drewromanyk.com.minesweeper.interfaces

import app.drewromanyk.com.minesweeper.models.Cell

/**
 * Created by drewromanyk on 5/20/17.
 */
interface MinesweeperHandler : GameTimerHandler {
    fun isSwiftOpenEnabled(): Boolean

    // TODO: Maybe change this to be inside Minesweeper and not MinesweeperUi?
    fun onSwiftChange()

    fun onCellChange(cell: Cell)

    fun onGameStatusChange(cell: Cell)
}