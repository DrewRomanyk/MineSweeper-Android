package app.drewromanyk.com.minesweeper.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Locale;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.application.MinesweeperApp;

/**
 * Created by Drew on 4/18/2015.
 * Helper
 * Class is used as a general utils class for random things to improve code
 */
public class Helper {

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static Tracker screenViewOnGoogleAnalytics(Context context, String screenName) {
        MinesweeperApp application = (MinesweeperApp) context.getApplicationContext();
        application.getDefaultTracker().setScreenName("Screen~" + screenName);
        application.getDefaultTracker().send(new HitBuilders.ScreenViewBuilder().build());
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

    @SuppressWarnings("deprecation")
    public static Locale getLocale(Context context) {
        Resources resources = context.getResources();
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                ? resources.getConfiguration().getLocales()
                .getFirstMatch(resources.getAssets().getLocales())
                : resources.getConfiguration().locale;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }
}
