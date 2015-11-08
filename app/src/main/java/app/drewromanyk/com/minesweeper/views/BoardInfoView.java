package app.drewromanyk.com.minesweeper.views;

import android.content.Context;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.models.Board;
import app.drewromanyk.com.minesweeper.util.Helper;

/**
 * Created by Drew on 11/7/15.
 */
public class BoardInfoView {

    private TextView mineKeeperView;
    private TextView scoreKeeperView;
    private Chronometer chronometer;
    private long lastStopTime;

    public BoardInfoView(TextView mineKeeperView, TextView scoreKeeperView, Chronometer chronometer) {
        this.mineKeeperView = mineKeeperView;
        this.scoreKeeperView = scoreKeeperView;
        this.chronometer = chronometer;
    }

    public void resetInfo(int mineCount) {
        setMineKeeperText(mineCount);
        setScoreKeeperText(0);
        lastStopTime = 0;
        chronometer.setBase(SystemClock.elapsedRealtime());
    }

    public void setMineKeeperText(int value) {
        Context context = mineKeeperView.getContext();
        mineKeeperView.setText(context.getString(R.string.game_bar_mine_title) + value);
    }
    public void setScoreKeeperText(double value) {
        Context context = mineKeeperView.getContext();
        scoreKeeperView.setText(context.getString(R.string.game_bar_score_title) + String.format( "%.4f", value ));
    }
    public void setChronometerTime(long curDuration) { lastStopTime = SystemClock.elapsedRealtime() + (curDuration * 500) + 1000; }
    public void setChronometerTickListener(Chronometer.OnChronometerTickListener listener) { chronometer.setOnChronometerTickListener(listener); }

    /*
     * CHRONOMETER
     */
    public void startChronometer(boolean gamePlaying) {
        // on first start
        if ( lastStopTime == 0 ) {
            chronometer.setBase(SystemClock.elapsedRealtime());
        } else {
            long intervalOnPause = (SystemClock.elapsedRealtime() - lastStopTime);
            chronometer.setBase(chronometer.getBase() + intervalOnPause);
        }
        if(gamePlaying) {
            chronometer.start();
        } else {
            chronometer.stop();
        }
    }

    public void stopChronometer() {
        lastStopTime = SystemClock.elapsedRealtime();
        chronometer.stop();
    }

    public int getChronometerSeconds() {
        return Helper.getSecondsFromTime(chronometer.getText().toString());
    }
}
