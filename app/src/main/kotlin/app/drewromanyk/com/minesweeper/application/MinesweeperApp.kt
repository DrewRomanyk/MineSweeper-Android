package app.drewromanyk.com.minesweeper.application

import android.app.Application
import android.util.Log

import app.drewromanyk.com.minesweeper.BuildConfig
import app.drewromanyk.com.minesweeper.util.UserPrefStorage

/**
 * Created by Drew on 1/13/2015.
 * MinesweeperApp
 * Application code for the whole app
 */

class MinesweeperApp : Application() {
    override fun onCreate() {
        super.onCreate()
        //disableFirebaseDuringDebugBuilds()

        UserPrefStorage.increaseAppOpenCount(applicationContext)
        //fix crash for lower API devices
        try {
            Class.forName("android.os.AsyncTask")
        } catch (ignore: Throwable) {
        }

    }

    private fun disableFirebaseDuringDebugBuilds() {
        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler { _, paramThrowable ->
                Log.wtf("Alert", paramThrowable.message, paramThrowable)
                System.exit(2) //Prevents the service/app from freezing
            }
        }
    }
}
