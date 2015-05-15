package app.drewromanyk.com.minesweeper.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import app.drewromanyk.com.minesweeper.R;

/**
 * Created by Drew on 4/17/2015.
 */
public class UserPreferenceStorage {

    public static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

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

    public static boolean getNavDrawer(Context context) {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_navdrawer), true);
    }

    public static boolean getLightMode(Context context) {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_lightmode), false);
    }
}
