package app.drewromanyk.com.minesweeper.activities;

/**
 * Created by drewi_000 on 1/13/2015.
 */

import android.app.Application;
import android.os.StrictMode;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

import app.drewromanyk.com.minesweeper.R;

public class MyApplication extends Application {

    //Logging TAG
    private static final String TAG = "MyApp";

    public static int GENERAL_TRACKER = 0;

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public MyApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //fix crash for lower API devices
        try {
            Class.forName("android.os.AsyncTask");
        } catch(Throwable ignore){}

        /*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDialog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
                .penaltyLog()
                .build());
        */
    }

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // Global GA Settings
            // <!-- Google Analytics SDK V4 BUG20141213 Using a GA global xml freezes the app! Do config by coding. -->
            analytics.setDryRun(false);
            analytics.setLocalDispatchPeriod(30);

            //analytics.getLogger().setLogLevel(Logger.LogLevel.INFO);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);

            // Create a new tracker
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.app_tracker)
                    : analytics.newTracker(getString(R.string.analytics_id));
            t.enableAdvertisingIdCollection(true);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }
}
