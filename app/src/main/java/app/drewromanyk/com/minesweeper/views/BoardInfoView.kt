package app.drewromanyk.com.minesweeper.views

import android.content.Context
import android.widget.TextView

import com.squareup.phrase.Phrase

import java.util.concurrent.TimeUnit

import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.util.Helper
import app.drewromanyk.com.minesweeper.util.PhraseKeys

/**
 * Created by Drew on 11/7/15.
 * View for displaying statistical information to the user and also controlling the chronometer
 */
class BoardInfoView(private val timeKeeperView: TextView, private val mineKeeperView: TextView, private val scoreKeeperView: TextView) {

    fun reset(mineCount: Int) {
        setTimeKeeperText(0)
        setMineKeeperText(mineCount)
        setScoreKeeperText(0.0)
    }

    fun setTimeKeeperText(value: Long) {
        val context = timeKeeperView.context
        timeKeeperView.text = Phrase.from(context, R.string.game_bar_time_title)
                .put(PhraseKeys.AMOUNT, getTimeString(value, context))
                .format()
    }

    private fun getTimeString(millis: Long, context: Context): String {
        //hh:mm:ss
        return String.format(Helper.getLocale(context), "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)))
    }

    fun setMineKeeperText(value: Int) {
        val context = mineKeeperView.context
        mineKeeperView.text = Phrase.from(context, R.string.game_bar_mine_title)
                .put(PhraseKeys.AMOUNT, value)
                .format()
    }

    fun setScoreKeeperText(value: Double) {
        val context = mineKeeperView.context
        scoreKeeperView.text = Phrase.from(context, R.string.game_bar_score_title)
                .put(PhraseKeys.AMOUNT, String.format(Helper.getLocale(context), "%.3f", value))
                .format()
    }
}
