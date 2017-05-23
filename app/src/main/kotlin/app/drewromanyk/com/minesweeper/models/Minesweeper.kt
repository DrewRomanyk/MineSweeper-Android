package app.drewromanyk.com.minesweeper.models

import app.drewromanyk.com.minesweeper.enums.GameStatus
import app.drewromanyk.com.minesweeper.interfaces.MinesweeperHandler
import org.json.JSONArray
import org.json.JSONException
import java.util.*

/**
 * Created by Drew Romanyk on 5/18/17.
 * Pure non-android class to represent all Minesweeper operations
 */

class Minesweeper(rows: Int, columns: Int, private val mineCount: Int, private val gameHandler: MinesweeperHandler, startTime: Long = GameTimer.DEFAULT_START_TIME) {
    private val gameTimer: GameTimer = GameTimer(startTime, gameHandler)
    val cells: Array<Array<Cell>> = Array(rows) { Array(columns) { Cell() } }

    var gameStatus: GameStatus = GameStatus.NOT_STARTED
        private set(value) {
            field = value
            if (value.isGameOver()) {
                pauseTimer()
            }
        }
    private val score: Score = Score()
    private var flaggedMines: Int = 0
    private var flaggedCells: Int = 0
    private var revealedCells: Int = 0

    init {
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                cells[r][c].setCoordinates(r, c)
            }
        }
    }

    @Throws(JSONException::class, IllegalArgumentException::class)
    constructor(gameHandler: MinesweeperHandler, rows: Int, columns: Int, mineCount: Int, gameTime: Long, gameStatus: GameStatus,
                cellValues: JSONArray, cellRevealed: JSONArray, cellFlagged: JSONArray) :
            this(rows, columns, mineCount, gameHandler, gameTime) {
        if ((rows == 0) || (columns == 0) || (gameStatus == GameStatus.NOT_STARTED)) {
            throw IllegalArgumentException()
        }

        this.gameStatus = gameStatus

        var currentCell = 0

        for (r in 0 until rows) {
            for (c in 0 until columns) {
                cells[r][c].setResumeValues(r, c,
                        cellValues.getInt(currentCell),
                        cellRevealed.getBoolean(currentCell),
                        cellFlagged.getBoolean(currentCell))
                currentCell++

                if (cells[r][c].isRevealed()) {
                    revealedCells++
                } else if (cells[r][c].isFlagged()) {
                    flaggedCells++
                    if (cells[r][c].isMine()) {
                        flaggedMines++
                    }
                }
            }
        }

        score.calculateScore(cells)
        gameTimer.startGameTime()
    }

    private fun firstClickInit(clickedCell: Cell) {
        fun placeMinesOnEmptyCells(clickedCell: Cell) {
            // Clear values for cells
            for (row_cells in cells) {
                for (cell in row_cells) {
                    cell.value = 0
                }
            }
            var placedMines = 0

            while (placedMines != mineCount) {
                val randomR = (Math.random() * cells.size).toInt()
                val randomC = (Math.random() * cells[0].size).toInt()

                val validSpot = getCellNeighbors(clickedCell).all { (it.row != randomR) && (it.column != randomC) }

                if (validSpot && !cells[randomR][randomC].isMine()) {
                    placedMines++
                    cells[randomR][randomC].value = Cell.MINE
                    for (cell in getCellNeighbors(cells[randomR][randomC]).filter { !it.isMine() }) {
                        cell.value += 1
                    }
                }
            }
        }

        gameStatus = GameStatus.PLAYING

        do {
            placeMinesOnEmptyCells(clickedCell)
            score.calculateScore(cells)
        } while (!score.isValidScore())

        gameTimer.startGameTime()
    }

    fun reset() {
        flaggedMines = 0
        flaggedCells = 0
        revealedCells = 0
        gameStatus = GameStatus.NOT_STARTED
        score.reset()
        gameTimer.reset()

        for (rowCells in cells) {
            for (cell in rowCells) {
                cell.reset()
            }
        }
    }

    /***
     * Reveal & Flag Cells
     */

    fun revealCell(row: Int, col: Int) {
        if (gameStatus.isGameOver()) return
        val clickedCell = cells[row][col]
        if (clickedCell.isRevealed() && clickedCell.isEmpty()) {
            gameHandler.onSwiftChange()
            return
        }

        val queue = LinkedList<Cell>()
        if (gameHandler.isSwiftOpenEnabled() && clickedCell.isRevealed() && !clickedCell.isEmpty() &&
                isNumFlagNeighborsEqualToValue(clickedCell)) {
            getCellNeighbors(clickedCell).filterTo(queue) { !it.isRevealed() && !it.isFlagged() }
        } else {
            queue.add(clickedCell)
        }

        while (!queue.isEmpty()) {
            val currentCell = queue.poll()

            if (currentCell.isRevealed() && (clickedCell != currentCell)) continue
            if (currentCell.isFlagged()) continue

            if (gameStatus == GameStatus.NOT_STARTED) {
                firstClickInit(currentCell)
            }

            if (currentCell.isMine()) {
                gameStatus = GameStatus.DEFEAT
                gameHandler.onGameStatusChange(currentCell)
            } else if (!currentCell.isRevealed()) {
                revealedCells++
                currentCell.setToRevealed()

                if (currentCell.isEmpty()) {
                    getCellNeighbors(currentCell).filterTo(queue) { !it.isRevealed() && !it.isFlagged() }
                }
                gameHandler.onCellChange(currentCell, flagChange = false)
            }
        }

        if (hasWonGame()) {
            gameStatus = GameStatus.VICTORY
            gameHandler.onGameStatusChange(clickedCell)
        }
    }

    private fun hasWonGame(): Boolean = (mineCount + revealedCells == cells.size * cells[0].size)

    private fun getCellNeighbors(cell: Cell): Collection<Cell> {
        val neighbors: MutableList<Cell> = mutableListOf()

        for (r in cell.row - 1..cell.row + 1) {
            (cell.column - 1..cell.column + 1)
                    .filter { inbounds(r, it) && !((cell.row == r) && (cell.column == it)) }
                    .mapTo(neighbors) { cells[r][it] }
        }

        return neighbors
    }

    private fun isNumFlagNeighborsEqualToValue(cell: Cell): Boolean {
        val numFlaggedNeighbors = getCellNeighbors(cell).count { it.isFlagged() }
        return numFlaggedNeighbors == cell.value
    }

    fun flagCell(row: Int, col: Int) {
        if (gameStatus.isGameOver()) return
        val clickedCell = cells[row][col]

        if (clickedCell.isRevealed() && clickedCell.isEmpty()) {
            gameHandler.onSwiftChange()
            return
        } else if (clickedCell.isRevealed()) {
            // SwiftOpen
            revealCell(row, col)
            return
        }

        clickedCell.switchFlagStatus()
        flaggedCells = if (clickedCell.isFlagged()) flaggedCells + 1 else flaggedCells - 1
        if (clickedCell.isMine()) {
            flaggedMines = if (clickedCell.isFlagged()) flaggedMines + 1 else flaggedMines - 1
        }
        gameHandler.onCellChange(clickedCell, flagChange = true)

    }

    /***
     * Time
     */

    fun resumeTimer() {
        if (gameStatus == GameStatus.PLAYING) {
            gameTimer.startGameTime()
        }
    }

    fun pauseTimer() {
        gameTimer.stopGameTime()
    }

    fun getTime(): Long = gameTimer.time


    /***
     * Helper
     */

    fun getScore(): Double = score.getScore(gameTimer.time)

    fun getMinesLeftNumber(): Int = mineCount - flaggedCells

    fun getExplorePercent(): Float = revealedCells.toFloat() / ((cells.size * cells[0].size) - mineCount) * 100

    private fun inbounds(row: Int, col: Int): Boolean =
            (row in 0 until cells.size) && (col in 0 until cells[0].size)

}
