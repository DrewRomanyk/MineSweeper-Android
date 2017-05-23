package app.drewromanyk.com.minesweeper.models

import app.drewromanyk.com.minesweeper.enums.CellStatus

/**
 * Created by Drew Romanyk on 5/18/17.
 * Model for handling Cell data and operations
 */

class Cell {
    companion object {
        val MINE = -1
    }

    private var cellStatus: CellStatus = CellStatus.UNKNOWN
    var value: Int = 0
    var row: Int = 0
        private set
    var column: Int = 0
        private set

    fun reset() {
        value = 0
        cellStatus = CellStatus.UNKNOWN
    }

    fun setCoordinates(row: Int, column: Int) {
        this.row = row
        this.column = column
    }

    fun setResumeValues(row: Int, column: Int, value: Int, revealed: Boolean, flagged: Boolean) {
        this.row = row
        this.column = column
        this.value = value

        if (revealed) {
            this.cellStatus = CellStatus.REVEALED
        } else if (flagged) {
            this.cellStatus = CellStatus.FLAGGED
        }
    }

    fun switchFlagStatus() {
        assert(cellStatus != CellStatus.REVEALED)

        cellStatus = if (cellStatus == CellStatus.FLAGGED)
            CellStatus.UNKNOWN else CellStatus.FLAGGED
    }

    fun setToRevealed() {
        cellStatus = CellStatus.REVEALED
    }


    fun isEmpty(): Boolean {
        return value == 0
    }

    fun isMine(): Boolean {
        return value == MINE
    }

    fun isRevealed(): Boolean {
        return cellStatus == CellStatus.REVEALED
    }

    fun isFlagged(): Boolean {
        return cellStatus == CellStatus.FLAGGED
    }

    fun isUnknown(): Boolean {
        return cellStatus == CellStatus.UNKNOWN
    }
}
