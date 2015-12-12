package app.drewromanyk.com.minesweeper.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.activities.GameActivity;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.enums.GameStatus;
import app.drewromanyk.com.minesweeper.models.Board;

/**
 * Created by Drew on 4/17/2015.
 */
public class UserPrefStorage {

    public static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /*
     * FIRST TIME INFO
     */

    public static boolean isFirstTime(Context context) {
        return getPrefs(context).getBoolean("FIRST_TIME", true);
    }

    public static void setFirstTime(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean("FIRST_TIME", value);
        editor.commit();
    }

    /*
     * SAVED GAME INFO
     */
    
    public static int getLastGameStatus(Context context) {
        return getPrefs(context).getInt(context.getString(R.string.game_status), GameStatus.DEFEAT.ordinal());
    }

    public static long getGameDuration(Context context) {
        return getPrefs(context).getLong("TIME", 0);
    }

    public static Board loadSavedBoard(Context context, boolean statisticsLoad) {
        SharedPreferences preferences = getPrefs(context);

        Board result = null;

        int rows = preferences.getInt("ROWS", 0);
        int columns = preferences.getInt("COLUMNS", 0);


        if(!(rows == 0 || columns == 0)) {
            GameDifficulty difficulty = GameDifficulty.values()[preferences.getInt("DIFFICULTY", GameDifficulty.CUSTOM.ordinal())];
            int mineCount = preferences.getInt("MINE_COUNT", 0);
            GameStatus status = GameStatus.values()[preferences.getInt("STATUS", GameStatus.DEFEAT.ordinal())];
            int[][] cellValues = new int[rows][columns];
            boolean[][] cellRevealed = new boolean[rows][columns];
            boolean[][] cellFlagged = new boolean[rows][columns];

            try {
                JSONArray cellValuesJ = new JSONArray(preferences.getString("CELL_VALUES", "[]"));
                JSONArray cellRevealedJ = new JSONArray(preferences.getString("CELL_REVEALED", "[]"));
                JSONArray cellFlaggedJ = new JSONArray(preferences.getString("CELL_FLAGGED", "[]"));
                int counter = 0;
                for(int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        cellValues[r][c] = cellValuesJ.getInt(counter);
                        cellRevealed[r][c] = cellRevealedJ.getBoolean(counter);
                        cellFlagged[r][c] = cellFlaggedJ.getBoolean(counter);
                        counter++;
                    }
                }

                if(statisticsLoad) {
                    result = new Board(mineCount, cellValues, cellRevealed, cellFlagged, status, difficulty, getGameDuration(context));
                } else {
                    result = new Board(mineCount, cellValues, cellRevealed, cellFlagged, difficulty, status, (GameActivity) context);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static void saveBoardInfo(Context context, Board minesweeperBoard) {
        SharedPreferences.Editor editor = getPrefs(context).edit();

        editor.putInt("ROWS", minesweeperBoard.getRows());
        editor.putInt("COLUMNS", minesweeperBoard.getColumns());
        editor.putInt("MINE_COUNT", minesweeperBoard.getMineCount());
        editor.putInt("DIFFICULTY", minesweeperBoard.getGameDifficulty().ordinal());
        editor.putInt("STATUS", minesweeperBoard.getGameStatus().ordinal());
        editor.putLong("TIME", minesweeperBoard.getGameSeconds());

        JSONArray cellValues = new JSONArray();
        JSONArray cellRevealed = new JSONArray();
        JSONArray cellFlagged = new JSONArray();
        for(int r = 0; r < minesweeperBoard.getRows(); r++) {
            for(int c = 0; c < minesweeperBoard.getColumns(); c++) {
                cellValues.put(minesweeperBoard.getCellValue(r,c));
                cellRevealed.put(minesweeperBoard.getCellReveal(r, c));
                cellFlagged.put(minesweeperBoard.getCellFlag(r, c));
            }
        }

        editor.putString("CELL_VALUES", cellValues.toString());
        editor.putString("CELL_REVEALED", cellRevealed.toString());
        editor.putString("CELL_FLAGGED", cellFlagged.toString());

        editor.commit();
    }

    /*
     * STATS
     */

    public static int getWinsForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(difficulty.getStoragePrefix() + "WINS", 0);
    }

    public static int getLosesForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(difficulty.getStoragePrefix() + "LOSES", 0);
    }

    public static int getBestTimeForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(difficulty.getStoragePrefix() + "BEST_TIME", 0);
    }

    public static float getAvgTimeForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getFloat(difficulty.getStoragePrefix() + "AVG_TIME", 0);
    }

    public static float getExplorPercentForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getFloat(difficulty.getStoragePrefix() + "EXPLOR_PERCT", 0);
    }

    public static int getWinStreakForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(difficulty.getStoragePrefix() + "WIN_STREAK", 0);
    }

    public static int getLoseStreakForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(difficulty.getStoragePrefix() + "LOSES_STREAK", 0);
    }

    public static int getCurWinStreakForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(difficulty.getStoragePrefix() + "CURRENTWIN_STREAK", 0);
    }

    public static int getCurLoseStreakForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(difficulty.getStoragePrefix() + "CURRENTLOSES_STREAK", 0);
    }

    public static int getBestScoreForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(difficulty.getStoragePrefix() + "BEST_SCORE", 0);
    }

    public static float getAvgScoreForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getFloat(difficulty.getStoragePrefix() + "AVG_SCORE", 0);
    }

    public static void updateStats(Context context, GameDifficulty difficulty, int wins, int loses,
                                   int bestTime, float avgTime, float explorPerct, int winStreak,
                                   int losesStreak, int currentWinStreak, int currentLosesStreak,
                                   int bestScore, float avgScore) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        String prefix = difficulty.getStoragePrefix();
        // Get key info
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

        // Put data into storage
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
    }

    /*
     * SETTINGS
     */

    public static int getCellSize(Context context) {
        return getPrefs(context).getInt(context.getString(R.string.preference_cellsize), 100);
    }

    public static int getRowCount(Context context) {
        return getPrefs(context).getInt(context.getString(R.string.preference_rowcount), 9);
    }

    public static int getColumnCount(Context context) {
        return getPrefs(context).getInt(context.getString(R.string.preference_columncount), 9);
    }

    public static int getMineCount(Context context) {
        return getPrefs(context).getInt(context.getString(R.string.preference_minecount), 10);
    }

    public static boolean getVibrate(Context context) {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_vibration), true);
    }

    public static boolean getSound(Context context) {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_sound), true);
    }

    public static boolean getSwiftOpen(Context context) {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_swiftopen), true);
    }

    public static boolean getSwiftChange(Context context) {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_swiftchange), true);
    }

    public static boolean getVolumeButton(Context context) {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_volumebutton), true);
    }

    public static boolean getScreenOn(Context context) {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_screenon), false);
    }

    public static boolean getLightMode(Context context) {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_lightmode), true);
    }
}
