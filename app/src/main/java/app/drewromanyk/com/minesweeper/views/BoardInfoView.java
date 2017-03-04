package app.drewromanyk.com.minesweeper.views;

import android.content.Context;
import android.widget.TextView;

import com.squareup.phrase.Phrase;

import java.util.concurrent.TimeUnit;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.util.Helper;
import app.drewromanyk.com.minesweeper.util.PhraseKeys;

/**
 * Created by Drew on 11/7/15.
 * View for displaying statistical information to the user and also controlling the chronometer
 */
public class BoardInfoView {

    private TextView timeKeeperView;
    private TextView mineKeeperView;
    private TextView scoreKeeperView;

    public BoardInfoView(TextView timeKeeperView, TextView mineKeeperView, TextView scoreKeeperView) {
        this.timeKeeperView = timeKeeperView;
        this.mineKeeperView = mineKeeperView;
        this.scoreKeeperView = scoreKeeperView;
    }

    public void resetInfo(int mineCount) {
        setTimeKeeperText(0);
        setMineKeeperText(mineCount);
        setScoreKeeperText(0);
    }

    public void setTimeKeeperText(long value) {
        Context context = timeKeeperView.getContext();
        timeKeeperView.setText(Phrase.from(context, R.string.game_bar_time_title)
                .put(PhraseKeys.AMOUNT, getTimeString(value, context))
                .format());
    }

    private String getTimeString(long millis, Context context) {
        //hh:mm:ss
        return String.format(Helper.getLocale(context), "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    public void setMineKeeperText(int value) {
        Context context = mineKeeperView.getContext();
        mineKeeperView.setText(Phrase.from(context, R.string.game_bar_mine_title)
                .put(PhraseKeys.AMOUNT, value)
                .format());
    }

    public void setScoreKeeperText(double value) {
        Context context = mineKeeperView.getContext();
        scoreKeeperView.setText(Phrase.from(context, R.string.game_bar_score_title)
                .put(PhraseKeys.AMOUNT, String.format(Helper.getLocale(context), "%.3f", value))
                .format());
    }
}
