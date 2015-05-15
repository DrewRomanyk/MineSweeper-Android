package app.drewromanyk.com.minesweeper.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import java.util.HashMap;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.fragment.YesNoDialog;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.models.NavDrawerInfo;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.models.NavDrawerInfoTemp;
import app.drewromanyk.com.minesweeper.models.YesNoDialogInfo;

/**
 * Created by drewi_000 on 1/10/2015.
 */
public abstract class BaseActivity extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    //ACTIVITY
    private final Activity activity = this;
    private final Context context = this;

    //ADS
    private AdView mAdView;
    //NAV DRAWER
    protected static Toolbar toolbar;
    public static NavDrawerInfoTemp navDrawerInfo;
    //DIALOG
    private static HashMap<Integer, YesNoDialogInfo> yesNoDialogMap;
    //GAME INFO
    public static GameDifficulty gameMode = GameDifficulty.EASY;
    //INTERNET
    protected GoogleApiClient baseGoogleApiClient;

    /*
     * onActivity METHODS
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initYesNoDialogInformation();

        initRecentAppLayout();
        initAds();

        initToolbar();
        initDrawer();
    }

    // Task for Recent Apps
    private void initRecentAppLayout() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // only for LOLLIPOP and newer versions
            ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher),
                    getResources().getColor(R.color.primary_task));
            activity.setTaskDescription(tDesc);
        }
    }

    // AdView on bottom of screen
    private void initAds() {
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setAdListener(new AdListener() { // no overrides
        });
        mAdView.loadAd(new AdRequest.Builder()
                        .build()
        );
    }

    @Override
    public void onBackPressed() {
//        if(navDrawerInfo.getDrawerLayout().isDrawerOpen(Gravity.START | Gravity.START)){
//            navDrawerInfo.getDrawerLayout().closeDrawers();
//            return;
//        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        mAdView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdView.resume();
    }

    @Override
    protected void onDestroy() {
        mAdView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /*
     * UI ( TOOLBAR & DRAWER )
     */
    protected void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

    }

    // Setup drawer, and mostly is just left to setup the click listeners
    protected void initDrawer() {
        navDrawerInfo = new NavDrawerInfoTemp(this);

        final GestureDetector mGestureDetector = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        navDrawerInfo.getRecyclerView().addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(),e.getY());

                if(child != null && mGestureDetector.onTouchEvent(e)){
                    int position = navDrawerInfo.getRecyclerView().getChildPosition(child);
                    doNavDrawerActions(position);
                    return true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {}
        });
    }

    protected void doNavDrawerActions(int position) {
        switch (position) {
            case 1 :
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (baseGoogleApiClient.isConnected())
                            startActivityForResult(Games.Leaderboards
                                    .getAllLeaderboardsIntent(baseGoogleApiClient), ResultCodes.LEADERBOARDS.ordinal());
                        else
                            showYesNoDialog(ResultCodes.NEEDGOOGLE_DIALOG.ordinal());
                    }
                }, navDrawerInfo.DRAWER_DELAY);
                navDrawerInfo.getDrawerLayout().closeDrawers();
                break;
            case 2 :
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (baseGoogleApiClient.isConnected())
                            startActivityForResult(Games.Achievements
                                    .getAchievementsIntent(baseGoogleApiClient), ResultCodes.ACHIEVEMENTS.ordinal());
                        else
                            showYesNoDialog(ResultCodes.NEEDGOOGLE_DIALOG.ordinal());
                    }
                }, navDrawerInfo.DRAWER_DELAY);
                navDrawerInfo.getDrawerLayout().closeDrawers();
                break;
            case 3 :
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(context, StatsActivity.class));
                    }
                }, navDrawerInfo.DRAWER_DELAY);
                navDrawerInfo.getDrawerLayout().closeDrawers();
                break;
            case 4 :
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivityForResult(new Intent(context, SettingsActivity.class), ResultCodes.SETTINGS.ordinal());
                    }
                }, navDrawerInfo.DRAWER_DELAY);
                navDrawerInfo.getDrawerLayout().closeDrawers();
                break;
            case 5 :
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showYesNoDialog(ResultCodes.HELP_DIALOG.ordinal());
                    }
                }, navDrawerInfo.DRAWER_DELAY);
                navDrawerInfo.getDrawerLayout().closeDrawers();
                break;
            case 6 :
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showYesNoDialog(ResultCodes.ABOUT_DIALOG.ordinal());
                    }
                }, navDrawerInfo.DRAWER_DELAY);
                navDrawerInfo.getDrawerLayout().closeDrawers();
                break;
        }
    }

    /*
     * DIALOGS
     */
    private void initYesNoDialogInformation() {
        yesNoDialogMap = new HashMap<>();

        yesNoDialogMap.put(ResultCodes.ABOUT_DIALOG.ordinal(), new YesNoDialogInfo(getString(R.string.dialog_about_title),
                getString(R.string.dialog_about_message) + "\n\nVersion: " + getString(R.string.version_name)));

        yesNoDialogMap.put(ResultCodes.HELP_DIALOG.ordinal(), new YesNoDialogInfo(getString(R.string.dialog_help_title),
                getString(R.string.dialog_help_message)));

        yesNoDialogMap.put(ResultCodes.RESUME_DIALOG.ordinal(), new YesNoDialogInfo(getString(R.string.dialog_cancelresume_title),
                getString(R.string.dialog_cancelresume_message)));

        yesNoDialogMap.put(ResultCodes.CUSTOMGAMEERROR_DIALOG.ordinal(), new YesNoDialogInfo(getString(R.string.dialog_customgameerror_title),
                getString(R.string.dialog_customgameerror_message)));

        yesNoDialogMap.put(ResultCodes.TRASH_STATS_DIALOG.ordinal(), new YesNoDialogInfo(getString(R.string.dialog_trashstats_title),
                getString(R.string.dialog_trashstats_message)));

        yesNoDialogMap.put(ResultCodes.RESTART_DIALOG.ordinal(), new YesNoDialogInfo(getString(R.string.dialog_cancelresume_title),
                getString(R.string.dialog_cancelresume_message)));

        yesNoDialogMap.put(ResultCodes.NEEDGOOGLE_DIALOG.ordinal(), new YesNoDialogInfo(getString(R.string.dialog_needgoogle_title),
                getString(R.string.dialog_needgoogle_message)));
    }

    protected void showYesNoDialog(int RESULT_CODE) {
        DialogFragment dialog = new YesNoDialog();
        Bundle args = new Bundle();

        if(RESULT_CODE != ResultCodes.NEEDGOOGLE_DIALOG.ordinal() || !baseGoogleApiClient.isConnected()) {
            args.putString("title", yesNoDialogMap.get(RESULT_CODE).getTitle());
            args.putString("message", yesNoDialogMap.get(RESULT_CODE).getDescription());
        }

        dialog.setArguments(args);
        dialog.setTargetFragment(dialog, RESULT_CODE);
        dialog.show(getSupportFragmentManager(), "tag");
    }

    public void doPositiveClick(int REQUEST_CODE) { }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
