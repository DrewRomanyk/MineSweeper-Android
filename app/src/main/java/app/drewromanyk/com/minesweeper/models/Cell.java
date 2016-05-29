package app.drewromanyk.com.minesweeper.models;

import android.content.Context;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import app.drewromanyk.com.minesweeper.BuildConfig;
import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.activities.GameActivity;
import app.drewromanyk.com.minesweeper.util.UserPrefStorage;

/**
 * Created by Drew on 12/7/2014.
 */

public class Cell {
    public final static int MINE = -1;
    private static final String PACKAGE_NAME = "app.drewromanyk.com.minesweeper";

    private GameActivity gameActivity;

    private boolean reveal;
    private int value; // -1 is bomb, then the rest is the amount of neighbors
    private int flaggedNeighbors;
    private int mineNeighbors;
    private boolean flagged;
    private int row;
    private int column;
    private ImageView cellButton;
    private double gameCellScale = 1;

    // empty constructor for null cells
    public Cell() {}

    //context is for the creation of the image button
    public Cell(int row, int column, GameActivity gameActivity) {
        this(row, column, 0, false, false, gameActivity);

    }

    //create exisiting cell for resume
    public Cell(int row, int column, int value, boolean reveal, boolean flagged, GameActivity gameActivity) {
        this.reveal = reveal;
        this.flagged = flagged;
        this.value = value;
        this.row = row;
        this.column = column;
        this.gameActivity = gameActivity;

        createButton();
    }

    //create exisiting cell for resume
    public Cell(int row, int column, int value, boolean reveal, boolean flagged) {
        this.reveal = reveal;
        this.flagged = flagged;
        this.value = value;
        this.row = row;
        this.column = column;
    }

    // Create image button with gridlayout values, and correct image
    private void createButton() {
        cellButton = new ImageButton(gameActivity);
        cellButton.setLayoutParams(new GridLayout.LayoutParams());
        updateImageValue();
    }

    // Update size of cell based on density of screen & pinch scale & Preferences
    private void updateButtonSize() {
        final double densityScale = gameActivity.getResources().getDisplayMetrics().density / 3;
        int cellSize =(int) ((((1.0 * UserPrefStorage.getCellSize(gameActivity)) / 100) * 100) * gameCellScale * densityScale);
        cellButton.getLayoutParams().width = cellSize;
        cellButton.getLayoutParams().height = cellSize;
        cellButton.setMaxWidth(cellSize);
        cellButton.setMinimumWidth(cellSize);
        cellButton.setMaxHeight(cellSize);
        cellButton.setMinimumHeight(cellSize);
    }

    //returns the image button for the cell
    public ImageView getButton() { return cellButton; }

    // Updates the cells image and size of the button
    protected void updateImageValue() {
        int id = (UserPrefStorage.getLightMode(gameActivity)) ? R.drawable.ic_cell_unknown_light : R.drawable.ic_cell_unknown;

        if(isUnknownFlagCell()) {
            id = (UserPrefStorage.getLightMode(gameActivity)) ? R.drawable.ic_cell_unknownflag_light : R.drawable.ic_cell_unknownflag;
        } else if(isFlaggedCell()) {
            id = (UserPrefStorage.getLightMode(gameActivity)) ? R.drawable.ic_cell_flag_light : R.drawable.ic_cell_flag;
        } else if(isRevealedNumCell()) {
            if(UserPrefStorage.getLightMode(gameActivity)) {
                id = gameActivity.getResources().getIdentifier(
                        "ic_cell_" + getValue() + "_light", "drawable", PACKAGE_NAME);
            } else {
                id = gameActivity.getResources().getIdentifier(
                        "ic_cell_" + getValue(), "drawable", PACKAGE_NAME);
            }
            if(getValue() == 0) id = android.R.color.transparent;
        } else if(isRevealedFlaggedBombCell()) {
            id = (UserPrefStorage.getLightMode(gameActivity)) ? R.drawable.ic_cell_bombflagged_light : R.drawable.ic_cell_bombflagged;
        } else if(isRevealedUnflaggedBombCell()) {
            id = (UserPrefStorage.getLightMode(gameActivity)) ? R.drawable.ic_cell_bomb_light : R.drawable.ic_cell_bomb;
        }

        getButton().setBackgroundDrawable(gameActivity.getResources().getDrawable(id));
        updateButtonSize();
    }

    private boolean isUnknownFlagCell() {
        return (!isRevealed() && !isFlagged() && gameActivity.getFlagMode());
    }

    private boolean isFlaggedCell() {
        return (!isRevealed() && isFlagged());
    }

    private boolean isRevealedNumCell() {
        return (isRevealed() && (getValue() >= 0));
    }

    private boolean isRevealedFlaggedBombCell() {
        return (isRevealed() && isMine() && isFlagged());
    }

    private boolean isRevealedUnflaggedBombCell() {
        return (isRevealed() && isMine());
    }

    //returns the clicked mine cell image
    protected void updateClickedMine() {
        int id = (UserPrefStorage.getLightMode(gameActivity)) ? R.drawable.ic_cell_bombpressed_light : R.drawable.ic_cell_bombpressed;
        getButton().setBackgroundDrawable(gameActivity.getResources().getDrawable(id));
        updateButtonSize();
    }

    //revealed is when it is viewable by the user
    public boolean isRevealed() { return reveal; }

    public void setRevealed(boolean reveal) { this.reveal = reveal; }

    //flagged is when the user has it flagged as a bomb
    public boolean isFlagged() { return flagged; }

    public void setFlagged(boolean flagged) { this.flagged = flagged; }

    //value is what the cell is, either a bomb or how many neighboring bombs near that cell
    public int getValue() { return value; }

    public void setValue(int value) { this.value = value; }

    //row and column of the cell for the board
    public int getRow() { return row; }

    public int getColumn() { return column; }

    //is the cell a bomb?
    public boolean isMine() { return (value == MINE); }

    public int getFlaggedNeighbors() { return flaggedNeighbors; }
    public void setFlaggedNeighbors(int flaggedNeighbors) { this.flaggedNeighbors = flaggedNeighbors; }

    public int getMineNeighbors() { return mineNeighbors; }
    public void setMineNeighbors(int mineNeighbors) { this.mineNeighbors = mineNeighbors; }

    public void setGameCellScale(double gameCellScale) { this.gameCellScale = gameCellScale; }
}