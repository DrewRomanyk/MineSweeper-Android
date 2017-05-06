package app.drewromanyk.com.minesweeper.models;

import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.activities.GameActivity;
import app.drewromanyk.com.minesweeper.enums.UiThemeModeEnum;
import app.drewromanyk.com.minesweeper.util.UserPrefStorage;

/**
 * Created by Drew on 12/7/2014.
 * Cell
 * Class to define how cells in a board operate and look
 */

class Cell {
    final static int MINE = -1;
    private static final String PACKAGE_NAME = "app.drewromanyk.com.minesweeper";

    private GameActivity gameActivity;

    private boolean reveal;
    private int value; // -1 is bomb, then the rest is the amount of neighbors
    private boolean flagged;
    private int row;
    private int column;
    private ImageView cellButton;
    private double gameCellScale = 1;

    //context is for the creation of the image button
    Cell(int row, int column, double gameCellScale, GameActivity gameActivity) {
        this(row, column, 0, false, false, gameCellScale, gameActivity);
    }

    //create existing cell for resume
    Cell(int row, int column, int value, boolean reveal, boolean flagged, double gameCellScale, GameActivity gameActivity) {
        this.reveal = reveal;
        this.flagged = flagged;
        this.value = value;
        this.row = row;
        this.column = column;
        this.gameActivity = gameActivity;
        this.gameCellScale = gameCellScale;

        createButton();
    }

    //create existing cell for resume
    Cell(int row, int column, int value, boolean reveal, boolean flagged, double gameCellScale) {
        this.reveal = reveal;
        this.flagged = flagged;
        this.value = value;
        this.row = row;
        this.column = column;
        this.gameCellScale = gameCellScale;
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
        int cellSize = (int) ((((1.0 * UserPrefStorage.getCellSize(gameActivity)) / 100) * 100) * gameCellScale * densityScale);
        cellButton.getLayoutParams().width = cellSize;
        cellButton.getLayoutParams().height = cellSize;
        cellButton.setMaxWidth(cellSize);
        cellButton.setMinimumWidth(cellSize);
        cellButton.setMaxHeight(cellSize);
        cellButton.setMinimumHeight(cellSize);
    }

    //returns the image button for the cell
    ImageView getButton() {
        return cellButton;
    }

    // Updates the cells image and size of the button
    void updateImageValue() {
        UiThemeModeEnum uiThemeMode = UserPrefStorage.getUiThemeMode(gameActivity);
        boolean light_mode = false;
        boolean material_mode = true;
        switch (uiThemeMode) {
            case LIGHT:
                light_mode = true;
                break;
            case DARK:
                light_mode = false;
                break;
            case AMOLED:
                light_mode = false;
                break;
            case CLASSICAL:
                light_mode = true;
                material_mode = false;
                break;
        }
        int id = (material_mode) ?
                (light_mode) ? R.drawable.ic_cell_unknown_light : R.drawable.ic_cell_unknown
                : R.drawable.ic_cell_unknown_classical;

        if (isUnknownFlagCell()) {
            id = (material_mode) ?
                    (light_mode) ? R.drawable.ic_cell_unknownflag_light : R.drawable.ic_cell_unknownflag
                    : R.drawable.ic_cell_unknown_classical;
        } else if (isFlaggedCell()) {
            id = (material_mode) ?
                    (light_mode) ? R.drawable.ic_cell_flag_light : R.drawable.ic_cell_flag
                    : R.drawable.ic_cell_unknownflag_classical;
        } else if (isRevealedNumCell()) {
            if (!material_mode) {
                id = gameActivity.getResources().getIdentifier(
                        "ic_cell_" + getValue() + "_classical", "drawable", PACKAGE_NAME);
            } else if (light_mode) {
                id = gameActivity.getResources().getIdentifier(
                        "ic_cell_" + getValue() + "_light", "drawable", PACKAGE_NAME);
            } else {
                id = gameActivity.getResources().getIdentifier(
                        "ic_cell_" + getValue(), "drawable", PACKAGE_NAME);
            }
            if (getValue() == 0) {
                id = (material_mode) ?
                        android.R.color.transparent
                        : R.drawable.ic_cell_0_classical;
            }
        } else if (isRevealedFlaggedBombCell()) {
            id = (material_mode) ?
                    (light_mode) ? R.drawable.ic_cell_bombflagged_light : R.drawable.ic_cell_bombflagged
                    : R.drawable.ic_cell_bombflagged_classical;
        } else if (isRevealedUnflaggedBombCell()) {
            id = (material_mode) ?
                    (light_mode) ? R.drawable.ic_cell_bomb_light : R.drawable.ic_cell_bomb
                    : R.drawable.ic_cell_bomb_classical;
        }

        getButton().setBackgroundResource(id);
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
    void updateClickedMine() {
        UiThemeModeEnum uiThemeMode = UserPrefStorage.getUiThemeMode(gameActivity);
        boolean light_mode = false;
        boolean material_mode = true;
        switch (uiThemeMode) {
            case LIGHT:
                light_mode = true;
                break;
            case DARK:
                light_mode = false;
                break;
            case AMOLED:
                light_mode = false;
                break;
            case CLASSICAL:
                light_mode = true;
                material_mode = false;
                break;
        }
        int id = (material_mode) ?
                (light_mode) ? R.drawable.ic_cell_bombpressed_light : R.drawable.ic_cell_bombpressed
                : R.drawable.ic_cell_bombpressed_classical;
        getButton().setBackgroundResource(id);
        updateButtonSize();
    }

    //revealed is when it is viewable by the user
    boolean isRevealed() {
        return reveal;
    }

    void setRevealed(boolean reveal) {
        this.reveal = reveal;
    }

    //flagged is when the user has it flagged as a bomb
    boolean isFlagged() {
        return flagged;
    }

    void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    //value is what the cell is, either a bomb or how many neighboring bombs near that cell
    int getValue() {
        return value;
    }

    void setValue(int value) {
        this.value = value;
    }

    //row and column of the cell for the board
    int getRow() {
        return row;
    }

    int getColumn() {
        return column;
    }

    //is the cell a bomb?
    boolean isMine() {
        return (value == MINE);
    }

    void setGameCellScale(double gameCellScale) {
        this.gameCellScale = gameCellScale;
        updateImageValue();
    }
}