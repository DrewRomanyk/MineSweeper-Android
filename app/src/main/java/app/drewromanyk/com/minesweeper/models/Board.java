package app.drewromanyk.com.minesweeper.models;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import app.drewromanyk.com.minesweeper.BuildConfig;
import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.activities.GameActivity;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.enums.GameSoundType;
import app.drewromanyk.com.minesweeper.enums.GameStatus;
import app.drewromanyk.com.minesweeper.util.UserPrefStorage;

/**
 * Created by Drew on 12/7/2014.
 * Board
 * Main engine of the Minesweeper game
 */

public class Board {
    private GameActivity gameActivity;

    private Cell[][] cell;
    private CellNeighbors[][] cellNeighbors;
    private int columns;
    private int rows;
    private GameStatus gameStatus;
    private GameDifficulty gameDifficulty;
    private int mineCount;
    private int flaggedMines;
    private int flaggedCells;
    private int cellsInGame;
    private int revealedCells;
    private boolean firstRound;
    private GridLayout board;
    private boolean tappedOnRevealedCell = false;
    private ThreeBV score3BV;
    private long gameTime;
    private boolean isGameTimerOn = false;
    private Timer timer;
    private double gameCellScale = 1;

    /*
     * SETUP BOARD
     */
    //columns and rows to set how many there will be
    //mineCount to set the amount of bombs for the game
    public Board(int rows, int columns, int mineCount, double gameCellScale, GameDifficulty gameDifficulty, GameActivity gameActivity) {
        this.columns = columns;
        this.rows = rows;
        this.mineCount = mineCount;
        this.gameCellScale = gameCellScale;
        this.gameDifficulty = gameDifficulty;
        this.gameActivity = gameActivity;
        this.gameTime = 1;

        gameStatus = GameStatus.NOT_STARTED;
        firstRound = true;
        cellsInGame = rows * columns;
        board = new GridLayout(gameActivity);

        createCells();
        drawBoard();
    }

    //RESUME GAME
    public Board(int mineCount, double gameCellScale, int[][] values, boolean[][] revealed, boolean[][] flagged, GameDifficulty gameDifficulty, GameStatus status, GameActivity gameActivity, long gameTime) {
        this.mineCount = mineCount;
        this.gameCellScale = gameCellScale;
        this.gameDifficulty = gameDifficulty;
        this.gameActivity = gameActivity;
        this.gameTime = gameTime;
        rows = values.length;
        columns = values[0].length;

        cellsInGame = rows * columns;
        gameStatus = status;
        revealedCells = 0;
        flaggedMines = 0;
        flaggedCells = 0;
        firstRound = true;

        cell = new Cell[rows][columns];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                cell[r][c] = new Cell(r, c, values[r][c], revealed[r][c], flagged[r][c], gameCellScale, gameActivity);
                if (cell[r][c].isRevealed()) {
                    revealedCells++;
                    if (cell[r][c].isFlagged()) {
                        flaggedCells++;
                    }
                    firstRound = false;
                } else if (cell[r][c].isFlagged()) {
                    flaggedCells++;
                    if (cell[r][c].isMine()) {
                        flaggedMines++;
                    }
                }
            }
        }

        if (!firstRound) {
            startGameTime();
            findNeighborCells();
            score3BV = new ThreeBV(cell, rows, columns);
            score3BV.calculate3BV();

            // Gack code to fix flag issue
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    if (cell[r][c].isFlagged()) {
                        updateNeighborsOfFlagCell(cell[r][c]);
                    }
                }
            }

        }

        gameActivity.boardInfoView.setMineKeeperText(mineCount - flaggedCells);
        gameActivity.boardInfoView.setScoreKeeperText(getGameScore());
        board = new GridLayout(gameActivity);
        drawBoard();
    }

    //MAIN RESUME FOR STATS
    public Board(int mineCount, int[][] values, boolean[][] revealed, boolean[][] flagged, GameStatus status, GameDifficulty gameDifficulty, long gameTime) {
        this.gameTime = gameTime;
        this.mineCount = mineCount;
        this.gameDifficulty = gameDifficulty;
        rows = values.length;
        columns = values[0].length;

        cellsInGame = rows * columns;
        gameStatus = status;
        revealedCells = 0;
        flaggedMines = 0;
        flaggedCells = 0;
        firstRound = true;

        cell = new Cell[rows][columns];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                cell[r][c] = new Cell(r, c, values[r][c], revealed[r][c], flagged[r][c], gameCellScale);
                if (cell[r][c].isRevealed()) {
                    revealedCells++;
                    if (cell[r][c].isFlagged()) {
                        flaggedCells++;
                    }
                    firstRound = false;
                } else if (cell[r][c].isFlagged()) {
                    flaggedCells++;
                    if (cell[r][c].isMine()) {
                        flaggedMines++;
                    }
                }
            }
        }

        score3BV = new ThreeBV(cell, rows, columns);
        score3BV.calculate3BV();
    }

    // Generates the cells for the Board, they are all empty cells
    private void createCells() {
        cell = new Cell[rows][columns];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                cell[r][c] = new Cell(r, c, gameCellScale, gameActivity);
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
    // Creates the board for the grid layout
    private void drawBoard() {
        gameActivity.refreshButton.setIcon(R.drawable.ic_action_refresh_playing);
        board.setColumnCount(columns);
        board.setRowCount(rows);
        board.setPadding(20, 20, 20, 20);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                board.addView(cell[r][c].getButton());
                if (gameStatus == GameStatus.PLAYING || gameStatus == GameStatus.NOT_STARTED) {
                    setCellTapListeners(cell[r][c]);
                }
            }
        }
    }

    private void setCellTapListeners(Cell tgtCell) {
        final Cell currentCell = tgtCell;

        // Single tap
        currentCell.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((boolean) v.getTag()) {
                    updateBoardByTap(currentCell, true);
                }
            }
        });

        // Long tap
        currentCell.getButton().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setTag(true);
                } else if (v.isPressed() && (boolean) v.getTag()) {
                    long eventDuration = event.getEventTime() - event.getDownTime();
                    if (eventDuration > UserPrefStorage.getLongPressLength(gameActivity)) {
                        v.setTag(false);
                        updateBoardByTap(currentCell, false);
                        gameActivity.vibrate();
                    }
                }
                return false;
            }
        });
    }

    /*
     * CELL CLICKED, USER INPUT
     */
    //updates the game based on the player tapping on a cell
    private void updateBoardByTap(Cell clickedCell, boolean shortTap) {
        boolean longTap = !shortTap;

        if (isQuickChangeTap(shortTap, clickedCell)) {
            gameActivity.changeFlagMode(this);
        } else if (isRevealTap(shortTap, clickedCell)) {
            gameActivity.playSoundEffects(GameSoundType.TAP);
            revealCell(clickedCell);
        } else if (isFlagTap(shortTap)) {
            flagCell(clickedCell);
            if (longTap && !clickedCell.isRevealed()) {
                gameActivity.playSoundEffects(GameSoundType.LONGPRESS);
                clickedCell.getButton().startAnimation(AnimationUtils.loadAnimation(gameActivity, R.anim.puff_in));
            } else {
                gameActivity.playSoundEffects(GameSoundType.TAP);
            }
        }
        checkIfVictorious();
    }

    private boolean isRevealTap(boolean shortTap, Cell clickedCell) {
        boolean longTap = !shortTap;
        return (shortTap && (!gameActivity.getFlagMode() || clickedCell.isRevealed())) || (longTap && gameActivity.getFlagMode());
    }

    private boolean isFlagTap(boolean shortTap) {
        boolean longTap = !shortTap;
        return (shortTap && gameActivity.getFlagMode()) || (longTap && !gameActivity.getFlagMode());
    }

    private boolean isQuickChangeTap(boolean shortTap, Cell clickedCell) {
        return shortTap && UserPrefStorage.getSwiftChange(gameActivity) && clickedCell.getValue() == 0 && clickedCell.isRevealed();
    }

    /*
     * FIRST ROUND UPDATE
     */
    private void setupAfterFirstRound(Cell tgtCell) {
        firstRound = false;
        gameStatus = GameStatus.PLAYING;
        gameActivity.gamePlaying = true;
        startGameTime();

        boolean validBoard = false;

        while (!validBoard) {
            wipeBoard();
            createMines(tgtCell);
            findNeighborCells();
            setAllNeighborValues();
            score3BV = new ThreeBV(cell, rows, columns);
            score3BV.calculate3BV();

            validBoard = score3BV.getThreeBV() > 1;
        }
    }

    private void wipeBoard() {
        for (Cell[] row_cells : cell) {
            for (Cell cell : row_cells) {
                cell.setValue(0);
            }
        }
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
                cell[randomR][randomC].setValue(Cell.MINE);
            }
        }
    }

    //finds all the cells that are neighbors to a cell
    private void findNeighborCells() {
        cellNeighbors = new CellNeighbors[cell.length][cell[0].length];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                cellNeighbors[r][c] = new CellNeighbors(cell, cell[r][c]);
            }
        }
    }

    //calls setNeighborValue for all cells
    private void setAllNeighborValues() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                //setNeighborValue(cell[r][c]);
                if (!cell[r][c].isMine()) {
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

            if (tgtCell.isMine()) {
                flaggedMines = (tgtCell.isFlagged()) ? flaggedMines + 1 : flaggedMines - 1;
            }
            flaggedCells = (tgtCell.isFlagged()) ? flaggedCells + 1 : flaggedCells - 1;
            updateNeighborsOfFlagCell(tgtCell);
            tgtCell.updateImageValue();
            gameActivity.boardInfoView.setMineKeeperText((mineCount - flaggedCells));
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
        LinkedList<Cell> cellQueue = new LinkedList<>();
        cellQueue.add(tgtCell);

        while (!cellQueue.isEmpty()) {
            Cell currCell = cellQueue.poll();

            if (firstRound && !currCell.isFlagged()) {
                setupAfterFirstRound(currCell);
                updateRevealedCell(cellQueue, currCell);
            } else if (revealNonBombCell(currCell)) {
                updateRevealedCell(cellQueue, currCell);
            } else if (revealRevealedNeighbors(currCell)) {
                tappedOnRevealedCell = true;
                addNeighborCellsToQueue(cellQueue, currCell);
                tappedOnRevealedCell = false;
            } else if (defeatConditions(currCell)) {
                gameOver(GameStatus.DEFEAT, currCell);
            }
        }
    }

    private void updateRevealedCell(LinkedList<Cell> cellQueue, Cell currCell) {
        revealedCells++;
        currCell.setRevealed(true);
        currCell.updateImageValue();

        if (currCell.getValue() == 0) {
            addNeighborCellsToQueue(cellQueue, currCell);
        }
    }

    private void addNeighborCellsToQueue(LinkedList<Cell> cellQueue, Cell currCell) {
        for (int r = currCell.getRow() - 1; r <= currCell.getRow() + 1; r++) {
            for (int c = currCell.getColumn() - 1; c <= currCell.getColumn() + 1; c++) {
                if (inbounds(r, c) && !cell[r][c].isRevealed() && !(currCell.getRow() == r && currCell.getColumn() == c)) {
                    cellQueue.add(cell[r][c]);
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
        return UserPrefStorage.getSwiftOpen(gameActivity) && !tappedOnRevealedCell && (tgtCell.getValue() != 0)
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
    private boolean victoryConditions() {
        return (flaggedMines == mineCount && cellsInGame == (flaggedMines + revealedCells))
                || (cellsInGame == (mineCount + revealedCells));
    }

    private boolean defeatConditions(Cell tgtCell) {
        return tgtCell.isMine() && !tgtCell.isFlagged();
    }

    //if the player wins do the actions
    private void checkIfVictorious() {
        if (victoryConditions()) {
            gameOver(GameStatus.VICTORY, null);
        }
    }

    //game over actions
    private void gameOver(GameStatus gameStatus, Cell clickedCell) {
        this.gameStatus = gameStatus;
        //play sound
        gameActivity.playSoundEffects((gameStatus == GameStatus.VICTORY) ? GameSoundType.WIN : GameSoundType.LOSE);
        //Update stats/leaderboard/achievements
        if (gameDifficulty != GameDifficulty.CUSTOM) {
            updateLocalStatistics(gameActivity);
            updateGoogleGame();
        }
        //update UI
        int refreshIcon = (gameStatus == GameStatus.VICTORY) ?
                R.drawable.ic_action_refresh_win : R.drawable.ic_action_refresh_lose;
        updateToGameOverCells(gameStatus);

        if (gameStatus == GameStatus.DEFEAT) {
            clickedCell.updateClickedMine();
        }

        stopGameTime();
        gameActivity.refreshButton.setIcon(refreshIcon);
        gameActivity.vibrate();
    }

    public void gameOverByRestart() {
        gameStatus = GameStatus.DEFEAT;
        //Update stats
        if (gameDifficulty != GameDifficulty.CUSTOM) {
            updateLocalStatistics(gameActivity);
            gameActivity.playSoundEffects(GameSoundType.LOSE);
        }
    }

    private double getGameScore() {
        if (score3BV == null) return 0;
        long time = getGameTime();

        double scoreTemp = (score3BV.getThreeBV() / time);
        return (scoreTemp * 1000.0);
    }

    //reveals the board when you lose
    private void updateToGameOverCells(GameStatus gameStatus) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                cell[r][c].getButton().setOnClickListener(null);
                cell[r][c].getButton().setOnTouchListener(null);
                if (cell[r][c].isMine()) {
                    if (gameStatus == GameStatus.VICTORY) {
                        cell[r][c].setFlagged(true);
                    } else {
                        cell[r][c].setRevealed(true);
                    }
                    cell[r][c].updateImageValue();
                }
            }
        }
    }

    public void updateLocalStatistics(Context context) {

        boolean newBestTime = false;
        boolean newBestScore = false;

        // skip the resume/custom modes
        if (gameDifficulty == GameDifficulty.RESUME || gameDifficulty == GameDifficulty.CUSTOM)
            return;

        //initial data
        int wins = UserPrefStorage.getWinsForDifficulty(context, gameDifficulty);
        int loses = UserPrefStorage.getLosesForDifficulty(context, gameDifficulty);
        int bestTime = UserPrefStorage.getBestTimeForDifficulty(context, gameDifficulty);
        float avgTime = UserPrefStorage.getAvgTimeForDifficulty(context, gameDifficulty);
        float explorPerct = UserPrefStorage.getExplorPercentForDifficulty(context, gameDifficulty);
        int winStreak = UserPrefStorage.getWinStreakForDifficulty(context, gameDifficulty);
        int losesStreak = UserPrefStorage.getLoseStreakForDifficulty(context, gameDifficulty);
        int currentWinStreak = UserPrefStorage.getCurWinStreakForDifficulty(context, gameDifficulty);
        int currentLosesStreak = UserPrefStorage.getCurLoseStreakForDifficulty(context, gameDifficulty);
        int bestScore = UserPrefStorage.getBestScoreForDifficulty(context, gameDifficulty);
        float avgScore = UserPrefStorage.getAvgScoreForDifficulty(context, gameDifficulty);

        // Update wins/losses/total
        if (gameStatus == GameStatus.VICTORY) {
            wins++;
        } else {
            loses++;
        }
        int total_games = wins + loses;

        // Update best time and avg time
        int currentTime = (int) (getGameTime() / 1000);
        if (gameStatus == GameStatus.VICTORY) {
            // Smaller currentTime is better than bestTime
            if (bestTime > currentTime || bestTime == 0) {
                newBestTime = true;
                bestTime = currentTime;
            }
            avgTime += (currentTime - avgTime) / (wins);
        }

        // Update exploration percentage
        float currentExplorPerct = ((float) revealedCells) / (cellsInGame - mineCount) * 100;
        explorPerct += (currentExplorPerct - explorPerct) / (total_games);

        // Update streaks
        if (gameStatus == GameStatus.VICTORY) {
            currentWinStreak++;
            currentLosesStreak = 0;
        } else {
            currentWinStreak = 0;
            currentLosesStreak++;
        }
        if (currentWinStreak > winStreak)
            winStreak = currentWinStreak;
        if (currentLosesStreak > losesStreak)
            losesStreak = currentLosesStreak;

        // Update best score & avg score
        int currentScore = (int) (getGameScore() * 1000);
        if (gameStatus == GameStatus.VICTORY) {
            // Bigger currentScore is better than bestScore
            if (bestScore < currentScore || bestScore == 0) {
                newBestScore = true;
                bestScore = currentScore;
            }
            avgScore += ((float) currentScore - avgScore) / (wins);
        }

        UserPrefStorage.updateStats(context, gameDifficulty, wins, loses, bestTime, avgTime,
                explorPerct, winStreak, losesStreak, currentWinStreak, currentLosesStreak,
                bestScore, avgScore);

        // Display new bests
        if (newBestTime)
            Toast.makeText(context, R.string.game_best_time, Toast.LENGTH_SHORT).show();

        if (newBestScore)
            Toast.makeText(context, R.string.game_best_score, Toast.LENGTH_SHORT).show();
    }

    private void updateGoogleGame() {
        long millis = getGameTime();
        long score = (long) (getGameScore() * 1000);
        gameActivity.updateLeaderboards(gameStatus, gameDifficulty, score, millis);
    }

    /*
     * GAME TIME FUNCTIONS
     */

    public void startGameTime() {
        isGameTimerOn = true;
        if (timer != null)
            timer.cancel();
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                gameActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gameTime += 1000; //increase every sec
                        gameTime -= gameTime % 1000;
                        if (isGameTimerOn) {
                            gameActivity.boardInfoView.setTimeKeeperText(gameTime);
                            gameActivity.boardInfoView.setScoreKeeperText(getGameScore());
                        }
                    }
                });
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    public void stopGameTime() {
        isGameTimerOn = false;
        if (timer != null)
            timer.cancel();
    }

    /*
     * ZOOM METHODS
     */

    public void zoomIn() {
        gameCellScale += .2;
        if (gameCellScale > 2.0) gameCellScale = 2.0;
        updateCellsZoom();
    }

    public void zoomInFully() {
        gameCellScale = 2.0;
        updateCellsZoom();
    }

    public void zoomOut() {
        gameCellScale -= .2;
        if (gameCellScale < .4) gameCellScale = .4;
        updateCellsZoom();
    }

    public void zoomOutFully() {
        gameCellScale = .4;
        updateCellsZoom();
    }

    private void updateCellsZoom() {
        for (Cell[] row_cells : cell) {
            for (Cell cell : row_cells) {
                cell.setGameCellScale(gameCellScale);
            }
        }
    }

    /*
     * HELPER METHODS
     */
    // Checks if the cell being called is inbounds
    private boolean inbounds(int row, int column) {
        return (0 <= row && row < rows && 0 <= column && column < columns);
    }

    public GridLayout getLayout() {
        return board;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public GameDifficulty getGameDifficulty() {
        return gameDifficulty;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getMineCount() {
        return mineCount;
    }

    public int getCellValue(int r, int c) {
        return cell[r][c].getValue();
    }

    public boolean getCellReveal(int r, int c) {
        return cell[r][c].isRevealed();
    }

    public boolean getCellFlag(int r, int c) {
        return cell[r][c].isFlagged();
    }

    public boolean getFirstRound() {
        return firstRound;
    }

    public long getGameTime() {
        return (gameTime == 0) ? 1 : gameTime;
    }

    public double getGameCellScale() {
        return gameCellScale;
    }
}
