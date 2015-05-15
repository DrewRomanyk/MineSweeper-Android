package app.drewromanyk.com.minesweeper.models;

/**
 * Created by drewi_000 on 12/14/2014.
 */
public class CellNeighbors {
    private static final Cell NULL_CELL = new Cell();

    private Cell[][] neighboringCells;
    private int rows;
    private int columns;
    private int numMines;
    private int numFlags;



    public CellNeighbors(Cell[][] cell, Cell tgtCell, int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        neighboringCells = new Cell[3][3];

        setNeighboringCellsNull();
        numMines = 0;
        numFlags = 0;

        findNeighboringCells(cell, tgtCell);
    }

    public Cell[][] getNeighboringCells() { return neighboringCells; }

    public int getNumMines() { return numMines; }

    public void setNumFlags(int numFlags) { this.numFlags = numFlags; }

    public int getNumFlags() { return numFlags; }

    //sets the default value for the neighbors as null
    private void setNeighboringCellsNull() {
        for (int r = 0; r < neighboringCells.length; r++) {
            for (int c = 0; c < neighboringCells[r].length ; c++) {
                neighboringCells[r][c] = NULL_CELL;
            }
        }
    }

    //sets the valid neighboring cells to the class
    private void findNeighboringCells(Cell[][] cell, Cell tgtCell) {
        for (int r = tgtCell.getRow() - 1; r <= tgtCell.getRow() + 1; r++) {
            for (int c = tgtCell.getColumn() - 1; c <= tgtCell.getColumn() + 1; c++) {
                if (inbounds(r, c)) {
                    neighboringCells[r - (tgtCell.getRow() - 1)][c - (tgtCell.getColumn() - 1)] = cell[r][c];
                    if(cell[r][c].isMine()) {
                        numMines++;
                    } else if(cell[r][c].isFlagged()) {
                        numFlags++;
                    }
                }
            }
        }
    }

    //checks if the cell being called is inbounds
    private boolean inbounds(int row, int column) {
        return (0 <= row && row < rows && 0 <= column && column < columns);
    }
}
