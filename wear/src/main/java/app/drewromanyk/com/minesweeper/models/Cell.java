package app.drewromanyk.com.minesweeper.models;

import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.activities.GameActivity;
import app.drewromanyk.com.minesweeper.activities.MainActivity;

/**
 * Created by Drew on 12/7/2014.
 */

public class Cell {
    //package name string
    private static final String PACKAGE_NAME = "app.drewromanyk.com.minesweeper";
    //cell value
    private final static int MINE = -1;

    private boolean reveal;
    private int value; // -1 is bomb, then the rest is the amount of neighbors
    private boolean flagged;
    private int row;
    private int column;
    private ImageView cellButton;

    //empty constructor for null cells
    public Cell() { }
    //context is for the creation of the image button
    public Cell(int row, int column) {
        reveal = false;
        value = 0;
        this.row = row;
        this.column = column;

        createButton();

    }

    //create exisiting cell for resume
    public Cell(int row, int column, int value, boolean reveal, boolean flagged) {
        this.reveal = reveal;
        this.flagged = flagged;
        this.value = value;
        this.row = row;
        this.column = column;

        createButton();
    }

    private void createButton() {
        cellButton = new ImageButton(GameActivity.context);
        cellButton.setLayoutParams(new GridLayout.LayoutParams());
        updateImageValue();
    }

    private void updateButtonSize() {
        final double densityScale = GameActivity.context.getResources().getDisplayMetrics().density / 3;
        int cellSize =(int) ((((1.0 * MainActivity.cell_size) / 100) * 100) * GameActivity.pinchScale * densityScale);
        cellButton.getLayoutParams().width = cellSize;
        cellButton.getLayoutParams().height = cellSize;
        cellButton.setMinimumHeight(1);
        cellButton.setMinimumWidth(1);
        //cellButton.setMaxWidth(150);
        //cellButton.setMaxHeight(150);
    }

    //returns the image button for the cell
    public ImageView getButton() { return cellButton; }

    //returns the cell image value
    protected void updateImageValue() {
        int id = (GameActivity.lightmode_setting) ? R.drawable.ic_cell_unknown_light : R.drawable.ic_cell_unknown;

        if(!isRevealed() && !isFlagged() && GameActivity.getFlagMode()) {
            id = (GameActivity.lightmode_setting) ? R.drawable.ic_cell_unknownflag_light : R.drawable.ic_cell_unknownflag;
        } else if(!isRevealed() && isFlagged()) {
            id = (GameActivity.lightmode_setting) ? R.drawable.ic_cell_flag_light : R.drawable.ic_cell_flag;
        } else if(isRevealed() && (getValue() >= 0)) {
            if(GameActivity.lightmode_setting) {
                id = GameActivity.activity.getResources().getIdentifier(
                        "ic_cell_" + getValue() + "_light", "drawable", PACKAGE_NAME);
            } else {
                id = GameActivity.activity.getResources().getIdentifier(
                        "ic_cell_" + getValue(), "drawable", PACKAGE_NAME);
            }
        } else if(isRevealed() && isMine() && isFlagged()) {
            id = (GameActivity.lightmode_setting) ? R.drawable.ic_cell_bombflagged_light : R.drawable.ic_cell_bombflagged;
        } else if(isRevealed() && isMine()) {
            id = (GameActivity.lightmode_setting) ? R.drawable.ic_cell_bomb_light : R.drawable.ic_cell_bomb;
        }

        getButton().setBackgroundDrawable(GameActivity.context.getResources().getDrawable(id));
        updateButtonSize();
    }

    //returns the clicked mine cell image
    protected void updateClickedMine() {
        int id = (GameActivity.lightmode_setting) ? R.drawable.ic_cell_bombpressed_light : R.drawable.ic_cell_bombpressed;
        getButton().setBackgroundDrawable(GameActivity.context.getResources().getDrawable(id));
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