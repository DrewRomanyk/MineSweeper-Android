package app.drewromanyk.com.minesweeper.models;

/**
 * Created by Drew on 12/14/2014.
 */
public class ThreeBV {

    private int rows;
    private int columns;
    private double score3BV;
    private Cell[][] cell;

    public ThreeBV (Cell[][] cell, int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.cell = cell;
        score3BV = 0;
    }

    public double getThreeBV() { return score3BV; }

    // Calculates the difficulty rating for the game
    public void calculate3BV() {
        boolean[][] marked = new boolean[rows][columns];

        // For each empty 0 cell
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if (!marked[r][c] && cell[r][c].getValue() == 0) {
                    marked[r][c] = true;

                    score3BV++;
                    floodFillMark(marked, r, c);
                }
            }
        }

        // For each unmarked cell that isnt a bomb
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if (!marked[r][c] && !cell[r][c].isMine()) {
                    marked[r][c] = true;
                    score3BV++;
                }
            }
        }
    }

    // Finds all the empty cells to not count for the difficulty rating
    private void floodFillMark(boolean[][] marked, int markRow, int markColumn) {
        for (int r = markRow - 1; r <= markRow + 1; r++) {
            for (int c = markColumn - 1; c <= markColumn + 1; c++) {
                if (inbounds(r, c) && !marked[r][c] && !(markColumn == c && markRow == r)) {
                    marked[r][c] = true;
                    if (cell[r][c].getValue() == 0) {
                        floodFillMark(marked, r, c);
                    }
                }
            }
        }
    }

    // Checks if the cell being called is inbounds
    private boolean inbounds(int row, int column) {
        return (0 <= row && row < rows && 0 <= column && column < columns);
    }
}
