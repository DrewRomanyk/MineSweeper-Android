package app.drewromanyk.com.minesweeper.models;

import java.util.LinkedList;

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

    protected class Coordinates {
        int row;
        int col;

        public Coordinates(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    // Finds all the empty cells to not count for the difficulty rating
    private void floodFillMark(boolean[][] marked, int markRow, int markColumn) {
        LinkedList<Coordinates> coordQueue = new LinkedList<>();
        coordQueue.add(new Coordinates(markRow, markColumn));

        while(!coordQueue.isEmpty()) {
            Coordinates currCords = coordQueue.poll();
            for (int r = currCords.row - 1; r <= currCords.row + 1; r++) {
                for (int c = currCords.col - 1; c <= currCords.col + 1; c++) {
                    if (inbounds(r, c) && !marked[r][c] && !(currCords.col == c && currCords.row == r)) {
                        marked[r][c] = true;
                        if (cell[r][c].getValue() == 0) {
                            coordQueue.add(new Coordinates(r, c));
                        }
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
