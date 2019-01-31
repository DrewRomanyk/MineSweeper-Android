package app.drewromanyk.com.minesweeper.models

import java.util.LinkedList

/**
 * Created by Drew on 05/18/2017.
 * ThreeBV
 * Class to help calculate the score (3BV) of the minesweeper board
 */
class Score {
    private var value: Double = 0.0

    fun getScore(time: Long): Double {
        return value / time * 1000
    }

    fun reset() {
        value = 0.0
    }

    fun calculateScore(cells: Array<Array<Cell>>): Double {
        val marked = Array(cells.size) { BooleanArray(cells[0].size) }

        // For each empty 0 cell
        for (r in 0 until cells.size) {
            for (c in 0 until cells[0].size) {
                if (!marked[r][c] && cells[r][c].isEmpty()) {
                    marked[r][c] = true

                    value++
                    floodFillMark(cells, marked, r, c)
                }
            }
        }

        // For each unmarked cell that isnt a bomb
        for (r in 0 until cells.size) {
            for (c in 0 until cells[0].size) {
                if (!marked[r][c] && !cells[r][c].isMine()) {
                    marked[r][c] = true
                    value++
                }
            }
        }
        return value
    }

    fun isValidScore(): Boolean = value > 1

    private data class Coordinates(val row: Int, val col: Int)

    // Finds all the empty cells to not count for the difficulty rating
    private fun floodFillMark(cells: Array<Array<Cell>>, marked: Array<BooleanArray>, markRow: Int, markColumn: Int) {
        val coordQueue = LinkedList<Coordinates>()
        coordQueue.add(Coordinates(markRow, markColumn))

        while (!coordQueue.isEmpty()) {
            val currCoords = coordQueue.poll()
            for (r in currCoords.row - 1..currCoords.row + 1) {
                for (c in currCoords.col - 1..currCoords.col + 1) {
                    if (inbounds(r, c, cells.size, cells[0].size) && !marked[r][c] && !((currCoords.col == c) && (currCoords.row == r))) {
                        marked[r][c] = true
                        if (cells[r][c].isEmpty()) {
                            coordQueue.add(Coordinates(r, c))
                        }
                    }
                }
            }
        }
    }

    private fun inbounds(row: Int, column: Int, rows: Int, columns: Int): Boolean {
        return (row in 0 until rows) && (column in 0 until columns)
    }
}
