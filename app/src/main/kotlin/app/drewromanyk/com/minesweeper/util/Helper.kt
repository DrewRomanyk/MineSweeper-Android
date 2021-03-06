package app.drewromanyk.com.minesweeper.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Html
import android.text.Spanned
import android.widget.Toast

import java.util.Locale

import app.drewromanyk.com.minesweeper.R

/**
 * Created by Drew on 4/18/2015.
 * Helper
 * Class is used as a general utils class for random things to improve code
 */
object Helper {

    fun sendFeedback(activity: Activity) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "message/rfc822"
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf("drew.romanyk@gmail.com"))
        i.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.settings_feedback_subject))
        i.putExtra(Intent.EXTRA_TEXT, "")
        try {
            activity.startActivity(Intent.createChooser(i, activity.getString(R.string.settings_feedback_chooser)))
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(activity, activity.getString(R.string.settings_feedback_error), Toast.LENGTH_SHORT).show()
        }

    }

    @Suppress("DEPRECATION")
    fun vibrate(context: Context) {
        if (UserPrefStorage.getVibrate(context)) {
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val duration = UserPrefStorage.getVibrationDuration(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                v.vibrate(duration)
            }
        }
    }

    @Suppress("DEPRECATION")
    fun getLocale(context: Context): Locale {
        val resources = context.resources
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            resources.configuration.locales.getFirstMatch(resources.assets.locales)!!
        else
            resources.configuration.locale
    }

    @Suppress("DEPRECATION")
    fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }
}
