package app.drewromanyk.com.minesweeper.models;

import android.content.Context;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.activities.GameActivity;
import app.drewromanyk.com.minesweeper.activities.MainActivity;
import app.drewromanyk.com.minesweeper.util.UserPreferenceStorage;

/**
 * Created by Drew on 12/7/2014.
 */

public class Cell {
    private static final String PACKAGE_NAME = "app.drewromanyk.com.minesweeper";
    private Context context;
    //cell value
    private final static int MINE = -1;

    private boolean reveal;
    private int value; // -1 is bomb, then the rest is the amount of neighbors
    private boolean flagged;
    private int row;
    private int column;
    private ImageView cellButton;

    // empty constructor for null cells
    public Cell() {}

    //context is for the creation of the image button
    public Cell(int row, int column, Context context) {
        this(row, column, 0, false, false, context);

    }

    //create exisiting cell for resume
    public Cell(int row, int column, int value, boolean reveal, boolean flagged, Context context) {
        this.reveal = reveal;
        this.flagged = flagged;
        this.value = value;
        this.row = row;
        this.column = column;
        this.context = context;

        createButton();
    }

    // Create image button with gridlayout values, and correct image
    private void createButton() {
        cellButton = new ImageButton(context);
        cellButton.setLayoutParams(new GridLayout.LayoutParams());
        updateImageValue();
    }

    // Update size of cell based on density of screen & pinch scale & Preferences
    private void updateButtonSize() {
        final double densityScale = context.getResources().getDisplayMetrics().density / 3;
        int cellSize =(int) ((((1.0 * UserPreferenceStorage.getCellSize(context)) / 100) * 100) * GameActivity.pinchScale * densityScale);
        cellButton.getLayoutParams().width = cellSize;
        cellButton.getLayoutParams().height = cellSize;
        cellButton.setMinimumHeight(1);
        cellButton.setMinimumWidth(1);
    }

    //returns the image button for the cell
    public ImageView getButton() { return cellButton; }

    // Updates the cells image and size of the button
    protected void updateImageValue() {
        int id = (UserPreferenceStorage.getLightMode(context)) ? R.drawable.ic_cell_unknown_light : R.drawable.ic_cell_unknown;

        if(isUnknownFlagCell()) {
            id = (UserPreferenceStorage.getLightMode(context)) ? R.drawable.ic_cell_unknownflag_light : R.drawable.ic_cell_unknownflag;
        } else if(isFlaggedCell()) {
            id = (UserPreferenceStorage.getLightMode(context)) ? R.drawable.ic_cell_flag_light : R.drawable.ic_cell_flag;
        } else if(isRevealedNumCell()) {
            if(UserPreferenceStorage.getLightMode(context)) {
                id = GameActivity.activity.getResources().getIdentifier(
                        "ic_cell_" + getValue() + "_light", "drawable", PACKAGE_NAME);
            } else {
                id = GameActivity.activity.getResources().getIdentifier(
                        "ic_cell_" + getValue(), "drawable", PACKAGE_NAME);
            }
        } else if(isRevealedFlaggedBombCell()) {
            id = (UserPreferenceStorage.getLightMode(context)) ? R.drawable.ic_cell_bombflagged_light : R.drawable.ic_cell_bombflagged;
        } else if(isRevealedUnflaggedBombCell()) {
            id = (UserPreferenceStorage.getLightMode(context)) ? R.drawable.ic_cell_bomb_light : R.drawable.ic_cell_bomb;
        }

        getButton().setBackgroundDrawable(context.getResources().getDrawable(id));
        updateButtonSize();
    }

    private boolean isUnknownFlagCell() {
        return (!isRevealed() && !isFlagged() && GameActivity.getFlagMode());
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
        int id = (UserPreferenceStorage.getLightMode(context)) ? R.drawable.ic_cell_bombpressed_light : R.drawable.ic_cell_bombpressed;
        getButton().setBackgroundDrawable(context.getResources().getDrawable(id));
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
}