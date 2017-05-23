package app.drewromanyk.com.minesweeper.application

import android.app.Application
import android.util.Log

import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker

import app.drewromanyk.com.minesweeper.BuildConfig
import app.drewromanyk.com.minesweeper.util.UserPrefStorage

/**
 * Created by Drew on 1/13/2015.
 * MinesweeperApp
 * Application code for the whole app
 */

class MinesweeperApp : Application() {
    private var mTracker: Tracker? = null

    override fun onCreate() {
        super.onCreate()
        disableFirebaseDuringDebugBuilds()

        UserPrefStorage.increaseAppOpenCount(applicationContext)
        //fix crash for lower API devices
        try {
            Class.forName("android.os.AsyncTask")
        } catch (ignore: Throwable) {
        }

    }

    private fun disableFirebaseDuringDebugBuilds() {
        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler { paramThread, paramThrowable ->
                Log.wtf("Alert", paramThrowable.message, paramThrowable)
                System.exit(2) //Prevents the service/app from freezing
            }
        }
    }

    /**
     * Gets the default [Tracker] for this [Application].

     * @return tracker
     */
    // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
    // Provide unhandled exceptions reports. Do that first after creating the tracker
    // Enable automatic activity tracking for your app
    val defaultTracker: Tracker
        @Synchronized get() {
            if (mTracker == null) {
                val analytics = GoogleAnalytics.getInstance(this)
                mTracker = analytics.newTracker(BuildConfig.ANALYTICS_ID)
                mTracker!!.enableExceptionReporting(true)
                mTracker!!.enableAutoActivityTracking(true)
            }
            return mTracker as Tracker
        }
}
