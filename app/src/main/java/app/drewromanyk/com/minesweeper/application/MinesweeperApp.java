package app.drewromanyk.com.minesweeper.application;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import app.drewromanyk.com.minesweeper.BuildConfig;
import app.drewromanyk.com.minesweeper.util.UserPrefStorage;

/**
 * Created by Drew on 1/13/2015.
 * MinesweeperApp
 * Application code for the whole app
 */

public class MinesweeperApp extends Application {
    private Tracker mTracker;
    private int isPremium = -1;

    public MinesweeperApp() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        disableFirebaseDuringDebugBuilds();

        UserPrefStorage.increaseAppOpenCount(getApplicationContext());
        //fix crash for lower API devices
        try {
            Class.forName("android.os.AsyncTask");
        } catch (Throwable ignore) {
        }
    }

    private void disableFirebaseDuringDebugBuilds() {
        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                    Log.wtf("Alert", paramThrowable.getMessage(), paramThrowable);
                    System.exit(2); //Prevents the service/app from freezing
                }
            });
        }
    }

    public int getIsPremium() {
        return isPremium;
    }

    public void setIsPremium(int isPremium) {
        this.isPremium = isPremium;
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(BuildConfig.ANALYTICS_ID);

            // Provide unhandled exceptions reports. Do that first after creating the tracker
            mTracker.enableExceptionReporting(true);

            // Enable automatic activity tracking for your app
            mTracker.enableAutoActivityTracking(true);
        }
        return mTracker;
    }
}
