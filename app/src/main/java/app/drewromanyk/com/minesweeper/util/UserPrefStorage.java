package app.drewromanyk.com.minesweeper.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.enums.GameStatus;

/**
 * Created by Drew on 4/17/2015.
 */
public class UserPrefStorage {

    public static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /*
     * RESUME GAME
     */
    
    public static int getLastGameStatus(Context context) {
        return getPrefs(context).getInt(context.getString(R.string.game_status), GameStatus.DEFEAT.ordinal());
    }

    /*
     * STATS
     */

    private static String getPrefixForStats(GameDifficulty difficulty) {
        String prefix = "";

        switch (difficulty) {
            case EASY :
                prefix = "EASY_";
                break;
            case MEDIUM :
                prefix = "MEDIUM_";
                break;
            case EXPERT :
                prefix = "EXPERT_";
                break;
        }

        return prefix;
    }

    public static int getWinsForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(getPrefixForStats(difficulty) + "WINS", 0);
    }

    public static int getLosesForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(getPrefixForStats(difficulty) + "LOSES", 0);
    }

    public static int getBestTimeForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(getPrefixForStats(difficulty) + "BEST_TIME", 0);
    }

    public static float getAvgTimeForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getFloat(getPrefixForStats(difficulty) + "AVG_TIME", 0);
    }

    public static float getExplorPercentForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getFloat(getPrefixForStats(difficulty) + "EXPLOR_PERCT", 0);
    }

    public static int getWinStreakForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(getPrefixForStats(difficulty) + "WIN_STREAK", 0);
    }

    public static int getLoseStreakForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(getPrefixForStats(difficulty) + "LOSES_STREAK", 0);
    }

    public static int getCurWinStreakForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(getPrefixForStats(difficulty) + "CURRENTWIN_STREAK", 0);
    }

    public static int getCurLoseStreakForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(getPrefixForStats(difficulty) + "CURRENTLOSES_STREAK", 0);
    }

    public static int getBestScoreForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getInt(getPrefixForStats(difficulty) + "BEST_SCORE", 0);
    }

    public static float getAvgScoreForDifficulty(Context context, GameDifficulty difficulty) {
        return getPrefs(context).getFloat(getPrefixForStats(difficulty) + "AVG_SCORE", 0);
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
