package app.drewromanyk.com.minesweeper.models;

/**
 * Created by Drew on 12/7/2014.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.Toast;

import com.google.android.gms.games.Games;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.activities.BaseActivity;
import app.drewromanyk.com.minesweeper.activities.GameActivity;
import app.drewromanyk.com.minesweeper.activities.MainActivity;


public class Board {
    private String TAG = "tacotaco";
    private static final int RESULT_GAMEOVER_DIALOG = 8;

    //cell neighbor
    private static final Cell NULL_CELL = new Cell();
    //game status
    public final static int NOT_STARTED = -1;
    public final static int PLAYING = 0;
    public final static int DEFEAT = 1;
    public final static int VICTORY = 2;

    //game diff
    private final int RESUME = -1;
    private final int CUSTOM = 0;
    private final int EASY = 1;
    private final int MEDIUM = 2;
    private final int EXPERT = 3;

    //cell value
    private final static int MINE = -1;

    private Cell[][] cell;
    private CellNeighbors[][] cellNeighbors;
    private int columns;
    private int rows;
    private int gameStatus;
    private int mineCount;
    private int flaggedMines;
    private int flaggedCells;
    private int cellsInGame;
    private int revealedCells;
    private boolean firstRound;
    //private ZDGridLayout board;
    private GridLayout board;
    //private ZoomableGridLayout board;
    //private GridView boardView;
    private boolean tappedOnRevealedCell = false;
    private boolean loadingForStats = false;
    private int loadedGameTime;

    /*
     * SETUP BOARD
     */
    //columns and rows to set how many there will be
    //mineCount to set the amount of bombs for the game
    public Board(int columns, int rows, int mineCount) {
        this.columns = columns;
        this.rows = rows;
        this.mineCount = mineCount;

        gameStatus = NOT_STARTED;
        firstRound = true;
        cellsInGame = rows * columns;
        //boardView = new GridView(GameActivity.context);
        board = new GridLayout(MainActivity.context);
        //board = new ZoomableGridLayout(GameActivity.context);

        createCells();
        drawBoard();
    }

    //generates the cells for the Board, they are all empty cells
    private void createCells() {
        cell = new Cell[rows][columns];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                cell[r][c] = new Cell(r, c);
            }
        }
    }

    public void updateCellSize() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                cell[r][c].updateImageValue();
            }
        }
    }
    /*
     * UI & LISTENERS
     */
    //creates the board for the grid layout
    private void drawBoard() {
        board.setColumnCount(columns);
        board.setRowCount(rows);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                board.addView(cell[r][c].getButton());
                if(gameStatus == PLAYING || gameStatus == NOT_STARTED) {
                    setCellTapListeners(cell[r][c]);
                }
            }
        }

        GridLayout.LayoutParams LP = new GridLayout.LayoutParams(board.spec(0), board.spec(0));
        LP.setGravity(Gravity.CENTER);
        board.setLayoutParams(LP);
    }

    private void setCellTapListeners(Cell tgtCell) {
        final Cell currentCell = tgtCell;

        //single tap
        currentCell.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBoardByTap(currentCell, true);
                //longClickHandler.removeCallbacks(longClick);
            }
        });
        //long tap
        currentCell.getButton().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                updateBoardByTap(currentCell, false);
                vibrate();
                return true;
            }
        });
    }

    //removes the button listeners to make it more efficient
    private void removeCellListeners() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                cell[r][c].getButton().setOnClickListener(null);
                cell[r][c].getButton().setOnTouchListener(null);
                cell[r][c].getButton().setOnLongClickListener(null);
            }
        }
    }

    /*
     * CELL CLICKED, USER INPUT
     */
    //updates the game based on the player tapping on a cell
    private void updateBoardByTap(Cell clickedCell, boolean shortTap) {
        boolean longTap = !shortTap;

        if (isQuickChangeTap(shortTap, clickedCell)) {
            GameActivity.changeFlagMode(this);
        } else if(isRevealTap(shortTap, clickedCell)) {
            revealCell(clickedCell);
        } else if (isFlagTap(shortTap)) {
            flagCell(clickedCell);
            if(longTap && !clickedCell.isRevealed()) {
                //new PuffInAnimation(clickedCell.getButton()).animate();
                clickedCell.getButton().startAnimation(AnimationUtils.loadAnimation(GameActivity.context, R.anim.puff_in));
            }
        }
        checkIfVictorious();

        if(shortTap && gameStatus == PLAYING) {
            soundEffects(1);
        } else if (gameStatus == PLAYING) {
            soundEffects(2);
        }
    }

    private boolean isRevealTap(boolean shortTap, Cell clickedCell) {
        boolean longTap = !shortTap;
        return (shortTap && (!GameActivity.getFlagMode() || clickedCell.isRevealed())) || (longTap && GameActivity.getFlagMode());
    }

    private boolean isFlagTap(boolean shortTap) {
        boolean longTap = !shortTap;
        return (shortTap && GameActivity.getFlagMode()) || (longTap && !GameActivity.getFlagMode());
    }

    private boolean isQuickChangeTap(boolean shortTap, Cell clickedCell) {
        return shortTap && GameActivity.swiftchange_setting && clickedCell.getValue() == 0 && clickedCell.isRevealed();
    }

    /*
     * FIRST ROUND UPDATE
     */
    private void setupAfterFirstRound(Cell tgtCell) {
        firstRound = false;
        gameStatus = PLAYING;
        GameActivity.gamePlaying = true;
        GameActivity.startChronometer();
        createMines(tgtCell);
        findNeighborCells();
        setAllNeighborValues();
        score3BV = new ThreeBV(cell, rows, columns);
        score3BV.calculate3BV();
    }

    //creates which cells ( not the firstRound cell) are bombs
    private void createMines(Cell tgtCell) {
        int placedMines = 0;

        while (placedMines != mineCount) {
            int randomR = (int) (Math.random() * rows);
            int randomC = (int) (Math.random() * columns);

            boolean validSpot = true;

            for (int r = tgtCell.getRow() - 1; r <= tgtCell.getRow() + 1; r++) {
                for (int c = tgtCell.getColumn() - 1; c <= tgtCell.getColumn() + 1; c++) {
                    if (inbounds(r, c) && (r == randomR && c == randomC)) {
                        validSpot = false;
                    }
                }
            }

            if (validSpot && !cell[randomR][randomC].isMine()) {
                placedMines++;
                cell[randomR][randomC].setValue(MINE);
            }

        }
    }

    //finds all the cells that are neighbors to a cell
    private void findNeighborCells() {
        cellNeighbors =  new CellNeighbors[rows][columns];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                cellNeighbors[r][c] = new CellNeighbors(cell, cell[r][c], rows, columns);
            }
        }
    }

    //calls setNeighborValue for all cells
    private void setAllNeighborValues() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                //setNeighborValue(cell[r][c]);
                if(!cell[r][c].isMine()) {
                    cell[r][c].setValue(cellNeighbors[r][c].getNumMines());
                }
            }
        }
    }

    /*
     * FLAG MODE
     */
    //flag or unflag a cell, and update the image
    private void flagCell(Cell tgtCell) {
        if (!tgtCell.isRevealed()) {
            tgtCell.setFlagged(!tgtCell.isFlagged());

            if(tgtCell.isMine()) {
                flaggedMines = (tgtCell.isFlagged()) ? flaggedMines + 1 : flaggedMines - 1;
            }
            flaggedCells = (tgtCell.isFlagged()) ? flaggedCells + 1 : flaggedCells - 1;
            updateNeighborsOfFlagCell(tgtCell);
            tgtCell.updateImageValue();
            GameActivity.mineKeeperView.setText("Mines: " + (mineCount - flaggedCells));
        }
    }

    //updates the flaggedBomb & flaggedCells vars and finds the icon for the cell to be used
    private void updateNeighborsOfFlagCell(Cell tgtCell) {
        int value = (tgtCell.isFlagged()) ? 1 : -1;

        for (int r = tgtCell.getRow() - 1; r <= tgtCell.getRow() + 1; r++) {
            for (int c = tgtCell.getColumn() - 1; c <= tgtCell.getColumn() + 1; c++) {
                if (inbounds(r, c) && !firstRound) {
                    cellNeighbors[r][c].setNumFlags(cellNeighbors[r][c].getNumFlags() + value);
                }
            }
        }
    }
    /*
     * REVEAL MODE
     */
    //reveals cells based on its value
    //if firstround, it generates the board with bombs and find the difficulty and sets the cells values
    //if not bomb, then reveal
    //if revealed & matched value, then reveal neighbors
    //if cell is bomb, then DEFEAT
    private void revealCell(Cell tgtCell) {
        if (firstRound && !tgtCell.isFlagged()) {
            setupAfterFirstRound(tgtCell);
            updateRevealedCell(tgtCell);
        } else if (revealNonBombCell(tgtCell)) {
            updateRevealedCell(tgtCell);
        } else if (revealRevealedNeighbors(tgtCell)) {
            tappedOnRevealedCell = true;
            revealNeighborCells(tgtCell);
            tappedOnRevealedCell = false;
        } else if (defeatConditions(tgtCell)) {
            gameOver(DEFEAT, tgtCell);
        }
    }

    //updates the image and vars for the cell
    private void updateRevealedCell(Cell tgtCell) {
        revealedCells++;
        tgtCell.setRevealed(true);
        tgtCell.updateImageValue();

        if (tgtCell.getValue() == 0) {
            revealNeighborCells(tgtCell);
        }
    }

    //reveals neighbor cells around a cell
    //This is QUICK OPEN
    private void revealNeighborCells(Cell tgtCell) {
        for (int r = tgtCell.getRow() - 1; r <= tgtCell.getRow() + 1; r++) {
            for (int c = tgtCell.getColumn() - 1; c <= tgtCell.getColumn() + 1; c++) {
                if (inbounds(r, c) && !(tgtCell.getRow() == r && tgtCell.getColumn() == c)) {
                    revealCell(cell[r][c]);
                }
            }
        }
    }

    //Condition to reveal non bomb and non revealed and non flagged cells
    private boolean revealNonBombCell(Cell tgtCell) {
        return !tgtCell.isMine() && !tgtCell.isFlagged() && !tgtCell.isRevealed();
    }

    //Condition to reveal neighbors of revealed cell with flagged neighbor cells that match its value
    private boolean revealRevealedNeighbors(Cell tgtCell) {
        return GameActivity.swiftopen_setting && !tappedOnRevealedCell && (tgtCell.getValue() != 0)
                && !tgtCell.isFlagged() && tgtCell.isRevealed() && flaggedNeighborEqualsValue(tgtCell);
    }

    //checks if the revealed cell's value matches with flagged bombs, in order to reveal other neighbors
    private boolean flaggedNeighborEqualsValue(Cell tgtCell) {
        int value = tgtCell.getValue();
        int flaggedNeighbors = cellNeighbors[tgtCell.getRow()][tgtCell.getColumn()].getNumFlags();

        return (flaggedNeighbors == value);
    }

    /*
     * GAME OVER
     */
    //CONDITIONS TO WIN OR LOSE
    private boolean victoryConditions() { return (flaggedMines == mineCount && cellsInGame == (flaggedMines + revealedCells))
                                                    || (cellsInGame == (mineCount + revealedCells)); }
    private boolean defeatConditions(Cell tgtCell) { return tgtCell.isMine() && !tgtCell.isFlagged(); }

    //if the player wins do the actions
    private void checkIfVictorious() {
        if (victoryConditions()) {
            gameOver(VICTORY, null);
        }
    }
    //game over actions
    private void gameOver(int gameStatus, Cell clickedCell) {
        this.gameStatus = gameStatus;
        //play sound
        if(gameStatus == VICTORY) {
            soundEffects(3);
        } else { soundEffects(4); }
        //Update stats/leaderboard/achievements
        if(MainActivity.gameMode != CUSTOM) {
            updateLocalStatistics();
            updateGoogleGame();
        }
        //update UI
        int refreshButton = (gameStatus == VICTORY) ?
                R.drawable.ic_action_refresh_win : R.drawable.ic_action_refresh_lose;
        updateMineImage(gameStatus);
        if(gameStatus == DEFEAT) {
            clickedCell.updateClickedMine();
        }

        removeCellListeners();
        GameActivity.stopChronometer();
        GameActivity.refreshButton.setIcon(refreshButton);
        String message = (gameStatus == VICTORY) ? "Score: " + getGameScore() + "\nTime: " + getSecondsFromTime(GameActivity.chronometer.getText().toString()) + "\n\n" : "";
        message += "Press Ok to create a new game!";
        GameActivity.showGameOverDialog(gameStatus, message);
        vibrate();
    }

    public void gameOverByRestart() {
        gameStatus = DEFEAT;
        //Update stats
        if(MainActivity.gameMode != CUSTOM) {
            updateLocalStatistics();
            soundEffects(4);
        }
    }

    public double getGameScore() {
        if(score3BV == null) return 0;

        double scoreTemp = (score3BV.getThreeBV() / getSecondsFromTime(GameActivity.chronometer.getText().toString()));
        return ((double)((int) (scoreTemp * 1000)) / 1000.0);
    }

    //reveals the board when you lose
    private void updateMineImage(int gameStatus) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if(cell[r][c].isMine()) {
                    if (gameStatus == VICTORY) {
                        cell[r][c].setFlagged(true);
                    } else {
                        cell[r][c].setRevealed(true);
                    }
                    cell[r][c].updateImageValue();
                }
            }
        }
    }

    public void updateLocalStatistics() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(GameActivity.context);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        String prefix = "";
        boolean newBestTime = false;
        boolean newBestScore = false;
        switch (MainActivity.gameMode) {
            case EASY:
                prefix = "EASY_";
                break;
            case MEDIUM:
                prefix = "MEDIUM_";
                break;
            case EXPERT:
                prefix = "EXPERT_";
                break;
        }

        String winsKey = prefix + "WINS";
        String losesKey = prefix + "LOSES";
        String bestTimeKey = prefix + "BEST_TIME";
        String avgTimeKey = prefix + "AVG_TIME";
        String explorPerctKey = prefix + "EXPLOR_PERCT";
        String winStreakKey = prefix + "WIN_STREAK";
        String losesStreakKey = prefix + "LOSES_STREAK";
        String currentWinStreakKey = prefix + "CURRENTWIN_STREAK";
        String currentLosesStreakKey = prefix + "CURRENTLOSES_STREAK";
        String bestScoreKey = prefix + "BEST_SCORE";
        String avgScoreKey = prefix + "AVG_SCORE";

        //initial data
        int wins = sharedPrefs.getInt(winsKey, 0);
        int loses = sharedPrefs.getInt(losesKey, 0);
        int bestTime = sharedPrefs.getInt(bestTimeKey, 0);
        float avgTime = sharedPrefs.getFloat(avgTimeKey, 0);
        float explorPerct = sharedPrefs.getFloat(explorPerctKey, 0);
        int winStreak = sharedPrefs.getInt(winStreakKey, 0);
        int losesStreak = sharedPrefs.getInt(losesStreakKey, 0);
        int currentWinStreak = sharedPrefs.getInt(currentWinStreakKey, 0);
        int currentLosesStreak = sharedPrefs.getInt(currentLosesStreakKey, 0);
        int bestScore = sharedPrefs.getInt(bestScoreKey, 0);
        float avgScore = sharedPrefs.getFloat(avgScoreKey, 0);

        //update wins/losses/total
        if(gameStatus == VICTORY) {
            wins++;
        } else {
            loses++;
        }
        int total_games = wins + loses;
        //update best time and avg time
        int currentTime = getGameSeconds();
        if(gameStatus == VICTORY) {
            if(bestTime > currentTime || bestTime == 0) {
                newBestTime = true;
                bestTime = currentTime;
            }
            avgTime += ((float) currentTime - avgTime)/(wins);
        }
        //update exploration percentage
        float currentExplorPerct = ((float) revealedCells)/(cellsInGame - mineCount) * 100;
        explorPerct += (currentExplorPerct - explorPerct)/(total_games);
        //update streaks
        if(gameStatus == VICTORY) {
            currentWinStreak++;
            currentLosesStreak = 0;
        } else {
            currentWinStreak = 0;
            currentLosesStreak++;
        }
        if(currentWinStreak > winStreak)
            winStreak = currentWinStreak;
        if(currentLosesStreak > losesStreak)
            losesStreak = currentLosesStreak;
        //update best score & avg score
        int currentScore = (int) ((score3BV.getThreeBV() / currentTime) * 1000);
        if(gameStatus == VICTORY) {
            if(bestScore > currentScore || bestScore == 0) {
                newBestScore = true;
                bestScore = currentScore;
            }
            avgScore += ((float) currentScore - avgScore)/(wins);
        }

        //save the new values
        editor.putInt(winsKey, wins);
        editor.putInt(losesKey, loses);
        editor.putInt(bestTimeKey, bestTime);
        editor.putFloat(avgTimeKey, avgTime);
        editor.putFloat(explorPerctKey, explorPerct);
        editor.putInt(winStreakKey, winStreak);
        editor.putInt(losesStreakKey, losesStreak);
        editor.putInt(currentWinStreakKey, currentWinStreak);
        editor.putInt(currentLosesStreakKey, currentLosesStreak);
        editor.putInt(bestScoreKey, bestScore);
        editor.putFloat(avgScoreKey, avgScore);
        editor.commit();

        //display to user
        if(newBestTime) {
            Toast.makeText(GameActivity.context, "NEW BEST TIME!", Toast.LENGTH_SHORT).show();
        }
        if(newBestScore)
            Toast.makeText(GameActivity.context, "NEW BEST SCORE!", Toast.LENGTH_SHORT).show();
       /*String message = "Wins: " + wins + " Played: " + total_games +
                " Best Time: " + bestTime + " Avg Time: " + avgTime +
                " Expl: " + explorPerct + "%" +
                " currentWS: " + currentWinStreak + " currentLS: " + currentLosesStreak +
                " WS: " + winStreak + " LS: " + losesStreak +
                " Best Score: " + bestScore + " Avg Score: " + avgScore;
        Toast.makeText(GameActivity.context, message, Toast.LENGTH_LONG).show();*/

    }

    private void updateGoogleGame() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(GameActivity.context);
        int seconds = getSecondsFromTime(GameActivity.chronometer.getText().toString());
        long score = (long) ((score3BV.getThreeBV() / seconds) * 1000);

        if(gameStatus == VICTORY) {
            if(GameActivity.googleApiClient.isConnected()) {
                //EASY MODE STUFF
                if(MainActivity.gameMode == EASY) {
                    Games.Achievements.unlock(GameActivity.googleApiClient,
                            GameActivity.context.getString(R.string.achievement_easy));
                    if(seconds < 20) {
                        Games.Achievements.unlock(GameActivity.googleApiClient,
                                GameActivity.context.getString(R.string.achievement_fast));
                    }

                    Games.Leaderboards.submitScore(GameActivity.googleApiClient,
                            GameActivity.context.getString(R.string.leaderboard_easy_best_scores),
                            score);
                    Games.Leaderboards.submitScore(GameActivity.googleApiClient,
                            GameActivity.context.getString(R.string.leaderboard_easy_best_times),
                            seconds);
                    Games.Leaderboards.submitScore(GameActivity.googleApiClient,
                            GameActivity.context.getString(R.string.leaderboard_easy_best_streak),
                            sharedPrefs.getInt("EASY_CURRENTWIN_STREAK", 0));
                }
                //MEDIUM MODE STUFF
                else if(MainActivity.gameMode == MEDIUM) {
                    Games.Achievements.unlock(GameActivity.googleApiClient,
                            GameActivity.context.getString(R.string.achievement_medium));
                    if(seconds < 60) {
                        Games.Achievements.unlock(GameActivity.googleApiClient,
                                GameActivity.context.getString(R.string.achievement_quick));
                    }

                    Games.Leaderboards.submitScore(GameActivity.googleApiClient,
                            GameActivity.context.getString(R.string.leaderboard_medium_best_scores),
                            score);
                    Games.Leaderboards.submitScore(GameActivity.googleApiClient,
                            GameActivity.context.getString(R.string.leaderboard_medium_best_times),
                            seconds);
                    Games.Leaderboards.submitScore(GameActivity.googleApiClient,
                            GameActivity.context.getString(R.string.leaderboard_easy_best_streak),
                            sharedPrefs.getInt("MEDIUM_CURRENTWIN_STREAK", 0));
                }
                //EXPERT MODE STUFF
                else if(MainActivity.gameMode == EXPERT) {
                    Games.Achievements.unlock(GameActivity.googleApiClient,
                            GameActivity.context.getString(R.string.achievement_expert));
                    if(seconds < 150) {
                        Games.Achievements.unlock(GameActivity.googleApiClient,
                                GameActivity.context.getString(R.string.achievement_swift));
                    }

                    Games.Leaderboards.submitScore(GameActivity.googleApiClient,
                            GameActivity.context.getString(R.string.leaderboard_expert_best_scores),
                            score);
                    Games.Leaderboards.submitScore(GameActivity.googleApiClient,
                            GameActivity.context.getString(R.string.leaderboard_expert_best_times),
                            seconds);
                    Games.Leaderboards.submitScore(GameActivity.googleApiClient,
                            GameActivity.context.getString(R.string.leaderboard_easy_best_streak),
                            sharedPrefs.getInt("EXPERT_CURRENTWIN_STREAK", 0));
                }
                //CUSTOM MODE STUFF
                else if(MainActivity.gameMode == CUSTOM) {

                }
            }
        }
    }

    /*
     * FEEDBACK TO USER
     */
    //depending on the setting, will vibrate
    private static void vibrate() {
        if (MainActivity.vibration_settings) {
            Vibrator v = (Vibrator) GameActivity.context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(100);
        }
    }

    //depending on the setting, will make a sound
    private static void soundEffects(int TYPE) {
        if (MainActivity.soundEffects_settings) {
            switch (TYPE) {
                case 1:
                    GameActivity.click_short.start();
                    break;
                case 2:
                    GameActivity.click_long.start();
                    break;
                case 3:
                    GameActivity.effect_win.start();
                    break;
                case 4:
                    GameActivity.effect_lose.start();
                    break;


            }
        }
    }

    /*
     * HELPER METHODS
     */
    //checks if the cell being called is inbounds
    private boolean inbounds(int row, int column) {
        return (0 <= row && row < rows && 0 <= column && column < columns);
    }

    public GridLayout getLayout() { return board; }
    //public ZoomableGridLayout getLayout() { return board; }
    public int getGameStatus() { return gameStatus; }
    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public int getMineCount() { return mineCount; }
    public int getCellValue(int r, int c) { return cell[r][c].getValue(); }
    public boolean getCellReveal(int r, int c) { return cell[r][c].isRevealed(); }
    public boolean getCellFlag(int r, int c) { return cell[r][c].isFlagged(); }
    public boolean getFirstRound() { return firstRound; }
    public int getGameSeconds() {
        if(loadingForStats) {
            return loadedGameTime;
        } else {
            return getSecondsFromTime(GameActivity.chronometer.getText().toString());
        }
    }

    // Expects a string in the form MM:SS or HH:MM:SS
    protected static int getSecondsFromTime(String value) {

        String[] parts = value.split(":");

        // Wrong format, no value for you.
        if (parts.length < 2 || parts.length > 3)
            return 0;

        int seconds = 0, minutes = 0, hours = 0;

        if (parts.length == 2) {
            seconds = Integer.parseInt(parts[1]);
            minutes = Integer.parseInt(parts[0]);
        } else if (parts.length == 3) {
            seconds = Integer.parseInt(parts[2]);
            minutes = Integer.parseInt(parts[1]);
            hours = Integer.parseInt(parts[1]);
        }

        return seconds + (minutes * 60) + (hours * 3600);
    }
}
