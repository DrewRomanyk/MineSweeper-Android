package app.drewromanyk.com.minesweeper.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.application.MinesweeperApp;

/**
 * Created by Drew on 4/18/2015.
 */
public class Helper {

    // resizes a bitmap to new dimensions
    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static Tracker getGoogAnalyticsTracker(Context context) {
        MinesweeperApp application = (MinesweeperApp) context.getApplicationContext();
        return application.getDefaultTracker();
    }

    public static void sendFeedback(Activity activity) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"drew.romanyk@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.settings_feedback_subject));
        i.putExtra(Intent.EXTRA_TEXT, "");
        try {
            activity.startActivity(Intent.createChooser(i, activity.getString(R.string.settings_feedback_chooser)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(activity, activity.getString(R.string.settings_feedback_error), Toast.LENGTH_SHORT).show();
        }
    }
}
