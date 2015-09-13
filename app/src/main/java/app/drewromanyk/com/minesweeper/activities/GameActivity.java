package app.drewromanyk.com.minesweeper.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;

import org.json.JSONArray;

import java.util.ArrayList;

import app.drewromanyk.com.minesweeper.enums.GameSoundType;
import app.drewromanyk.com.minesweeper.models.Board;
import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.enums.GameStatus;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.models.YesNoDialogInfo;
import app.drewromanyk.com.minesweeper.util.BaseGameUtils;
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils;
import app.drewromanyk.com.minesweeper.util.Helper;
import app.drewromanyk.com.minesweeper.util.UserPrefStorage;


public class GameActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GameActivity";

    private Board minesweeperBoard;
    private AdView mAdView;
    // SOUNDS
    private SoundPool soundEffects;
    private ArrayList<Integer> soundIDs;
    // UI ELEMENTS
    private ScrollView vScroll;
    private ImageView boardBackground;
    public TextView mineKeeperView;
    public TextView scoreKeeperView;
    public MenuItem refreshButton;
    public MenuItem flagButton;
    public Chronometer chronometer;
    private long lastStopTime;
    //SETTING
    private boolean flagMode;
    public boolean gamePlaying = false;
    //GOOGLE
    //GOOGLE GAMES
    public GoogleApiClient googleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mSignInClicked = false;
    private boolean mAutoStartSignInFlow = false;
    private boolean mStarted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        flagMode = false;

        setupGoogleGames();
        setupToolbar((Toolbar) findViewById(R.id.toolbar));
        setupTaskActivityInfo();
        setupAds();
        setupBiDirectionalScrolling();
        setupSoundEffects();
        setupBoardInfoLayout();
        setupBoard(savedInstanceState == null);
        applySettings();
    }

    private void setupGoogleGames() {
        mAutoStartSignInFlow = true;

        // Create the Google API Client with access to Plus and Games
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void setupToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setTitle(R.string.title_activity_settings);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        toolbar.inflateMenu(R.menu.menu_game);
        flagButton = toolbar.getMenu().findItem(R.id.action_flag);
        refreshButton = toolbar.getMenu().findItem(R.id.action_refresh);
    }

    private void setupTaskActivityInfo() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // only for LOLLIPOP and newer versions
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
            ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_icon),
                    getResources().getColor(R.color.primary_task));
            setTaskDescription(tDesc);
        }
    }

    // AdView on bottom of screen
    private void setupAds() {
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setAdListener(new AdListener() { // no overrides
        });
        mAdView.loadAd(new AdRequest.Builder()
                        .build()
        );
    }

    private void setupSoundEffects() {
        soundEffects = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundIDs = new ArrayList<>();
        soundIDs.add(soundEffects.load(this, R.raw.click_short, 1));
        soundIDs.add(soundEffects.load(this, R.raw.click_long, 1));
        soundIDs.add(soundEffects.load(this, R.raw.effect_win, 1));
        soundIDs.add(soundEffects.load(this, R.raw.effect_lose, 1));
    }

    private void setupBoardInfoLayout() {
        mineKeeperView = (TextView) findViewById(R.id.mineKeeper);
        scoreKeeperView = (TextView) findViewById(R.id.scoreKeeper);
        boardBackground = (ImageView) findViewById(R.id.boardBackground);
        boardBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(minesweeperBoard != null && minesweeperBoard.getGameStatus() == GameStatus.PLAYING && UserPrefStorage.getSwiftChange(v.getContext())) {
                    changeFlagMode(minesweeperBoard);
                }
            }
        });
        chronometer = (Chronometer) findViewById(R.id.timeKeeper);
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if (minesweeperBoard != null) {
                    scoreKeeperView.setText("Score: " + minesweeperBoard.getGameScore());
                } else {
                    scoreKeeperView.setText("Score: 0.000");
                }

            }
        });
    }

    private void applySettings() {
        minesweeperBoard.updateCellSize();

        if(UserPrefStorage.getScreenOn(this)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // Theme Changing Background
        if(UserPrefStorage.getLightMode(this)) {
            boardBackground.setBackgroundColor(getResources().getColor(R.color.light_background));
            minesweeperBoard.updateCellSize();
        } else {
            boardBackground.setBackgroundColor(Color.parseColor("#424242"));
            minesweeperBoard.updateCellSize();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        mAdView.pause();
        super.onPause();
        gamePlaying = (minesweeperBoard.getGameStatus() == GameStatus.PLAYING);
        stopChronometer();

        saveDataForResume();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdView.resume();
        if(minesweeperBoard != null && !minesweeperBoard.getFirstRound())
            startChronometer();

        Helper.getGoogAnalyticsTracker(this).setScreenName("Screen~" + "Game");
        Helper.getGoogAnalyticsTracker(this).send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onDestroy() {
        mAdView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        flagButton = menu.findItem(R.id.action_flag);
        refreshButton = menu.findItem(R.id.action_refresh);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh :
                if(minesweeperBoard.getGameStatus() == GameStatus.PLAYING) {
                    YesNoDialogInfo dialogInfo = DialogInfoUtils.getInstance(this).getDialogInfo(ResultCodes.RESTART_DIALOG.ordinal());
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle(dialogInfo.getTitle())
                            .setMessage(dialogInfo.getDescription())
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    minesweeperBoard.gameOverByRestart();
                                    setupBoard(true);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            })
                            .create();
                    dialog.show();
                } else {
                    setupBoard(true);
                }
                return true;
            case R.id.action_flag :
                changeFlagMode(minesweeperBoard);
                return true;
            case android.R.id.home :
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        if(UserPrefStorage.getVolumeButton(this)) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (action == KeyEvent.ACTION_DOWN) {
                        changeFlagMode(minesweeperBoard);
                    }
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (action == KeyEvent.ACTION_DOWN) {
                        changeFlagMode(minesweeperBoard);
                    }
                    return true;
                default:
                    return super.dispatchKeyEvent(event);
            }
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    public void doPositiveClick(int REQUEST_CODE) {
        if(REQUEST_CODE == ResultCodes.RESTART_DIALOG.ordinal()) {
            minesweeperBoard.gameOverByRestart();
            setupBoard(true);
        } else if(REQUEST_CODE == ResultCodes.GAMEOVER_DIALOG.ordinal()) {
            setupBoard(true);
        }
    }

    /*
     * USER FEEDBACK
     */

    public void playSoundEffects(GameSoundType type) {
        if (UserPrefStorage.getSound(this)) {
            soundEffects.play(soundIDs.get(type.ordinal()), 1, 1, 1, 0, 1.0f);
        }
    }

    public void vibrate() {
        if (UserPrefStorage.getVibrate(this)) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(100);
        }
    }

    public int getPinchScale() {
        return 1;
    }

    /*
     * FLAG MODE
     */

    public boolean getFlagMode() { return flagMode; }

    public void changeFlagMode(Board board) {
        flagMode = !flagMode;
        int icon = (flagMode) ? R.drawable.ic_action_flag : R.drawable.ic_action_notflag;
        flagButton.setIcon(icon);
        board.updateCellSize();

    }


    /*
     * CHRONOMETER
     */
    public void startChronometer() {
        // on first start
        if ( lastStopTime == 0 ) {
            chronometer.setBase(SystemClock.elapsedRealtime());
        } else {
            long intervalOnPause = (SystemClock.elapsedRealtime() - lastStopTime);
            chronometer.setBase( chronometer.getBase() + intervalOnPause);
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

    /*
     * SETUP BOARD
     */

    private void setupBoard(boolean savedStateIsEmpty) {
        GameDifficulty gameDifficulty = GameDifficulty.valueOf(getIntent().getStringExtra("gameDifficulty"));
        stopChronometer();
        if(gameDifficulty == GameDifficulty.RESUME || !savedStateIsEmpty) {
            loadDataForResume();
            return;
        }

        int columns = UserPrefStorage.getColumnCount(this);
        int rows = UserPrefStorage.getRowCount(this);
        int mineCount = UserPrefStorage.getMineCount(this);

        getSupportActionBar().setTitle(getString(R.string.game_difficulty_custom));
        if (gameDifficulty == GameDifficulty.EASY) {
            columns = 9;
            rows = 9;
            mineCount = 10;
            getSupportActionBar().setTitle(getString(R.string.game_difficulty_easy));
        } else if (gameDifficulty == GameDifficulty.MEDIUM) {
            columns = 16;
            rows = 16;
            mineCount = 40;
            getSupportActionBar().setTitle(getString(R.string.game_difficulty_medium));
        } else if (gameDifficulty == GameDifficulty.EXPERT) {
            columns = 30;
            rows = 16;
            mineCount = 99;
            getSupportActionBar().setTitle(getString(R.string.game_difficulty_expert));
        }

        mineKeeperView.setText("Mines: " + mineCount);
        scoreKeeperView.setText("Score: 0.000");
        lastStopTime = 0;
        chronometer.setBase(SystemClock.elapsedRealtime());

        minesweeperBoard = new Board(rows, columns, mineCount, gameDifficulty, this);
        gamePlaying = (minesweeperBoard.getGameStatus() == GameStatus.PLAYING);

        addBoardToLayout();
    }

    private void setupResumeBoard(int mineCount, GameDifficulty gameDifficulty, GameStatus status, long time,
                                  int[][] cellValues, boolean[][] cellRevealed, boolean[][] cellFlagged) {
        lastStopTime = SystemClock.elapsedRealtime() + (time * 500) + 1000;

        getSupportActionBar().setTitle(getString(R.string.game_difficulty_custom));
        if (gameDifficulty == GameDifficulty.EASY) {
            getSupportActionBar().setTitle(getString(R.string.game_difficulty_easy));
        } else if (gameDifficulty == GameDifficulty.MEDIUM) {
            getSupportActionBar().setTitle(getString(R.string.game_difficulty_medium));
        } else if (gameDifficulty == GameDifficulty.EXPERT) {
            getSupportActionBar().setTitle(getString(R.string.game_difficulty_expert));
        }

        minesweeperBoard = new Board(mineCount, cellValues, cellRevealed, cellFlagged, gameDifficulty, status, this);
        gamePlaying = (minesweeperBoard.getGameStatus() == GameStatus.PLAYING);
        addBoardToLayout();
    }

    private void setupBiDirectionalScrolling() {

        final HorizontalScrollView hScroll = (HorizontalScrollView) findViewById(R.id.scrollHorizontal);
        vScroll = (ScrollView) findViewById(R.id.scrollVertical);
        // Method One ( IT WORKS )
        vScroll.setOnTouchListener(new View.OnTouchListener() { //inner scroll listener
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        hScroll.setOnTouchListener(new View.OnTouchListener() { //outer scroll listener
            private float mx, my, curX, curY;
            private boolean started = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                curX = event.getX();
                curY = event.getY();
                int dx = (int) (mx - curX);
                int dy = (int) (my - curY);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        if (started) {
                            vScroll.scrollBy(0, dy);
                            hScroll.scrollBy(dx, 0);
                        } else {
                            started = true;
                        }
                        mx = curX;
                        my = curY;
                        break;
                    case MotionEvent.ACTION_UP:
                        vScroll.scrollBy(0, dy);
                        hScroll.scrollBy(dx, 0);
                        started = false;
                        break;
                }
                return true;
            }
        });
    }

    private void addBoardToLayout() {
        vScroll.removeAllViews();
        vScroll.addView(minesweeperBoard.getLayout());
    }

    /*
     * RESUME GAME DATA
     */
    protected void saveDataForResume() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putInt("ROWS", minesweeperBoard.getRows());
        editor.putInt("COLUMNS", minesweeperBoard.getColumns());
        editor.putInt("MINE_COUNT", minesweeperBoard.getMineCount());
        editor.putInt("DIFFICULTY", minesweeperBoard.getGameDifficulty().ordinal());
        editor.putInt("STATUS", minesweeperBoard.getGameStatus().ordinal());
        editor.putLong("TIME", minesweeperBoard.getGameSeconds());

        JSONArray cellValues = new JSONArray();
        JSONArray cellRevealed = new JSONArray();
        JSONArray cellFlagged = new JSONArray();
        for(int r = 0; r < minesweeperBoard.getRows(); r++) {
            for(int c = 0; c < minesweeperBoard.getColumns(); c++) {
                cellValues.put(minesweeperBoard.getCellValue(r,c));
                cellRevealed.put(minesweeperBoard.getCellReveal(r, c));
                cellFlagged.put(minesweeperBoard.getCellFlag(r, c));
            }
        }

        editor.putString("CELL_VALUES", cellValues.toString());
        editor.putString("CELL_REVEALED", cellRevealed.toString());
        editor.putString("CELL_FLAGGED", cellFlagged.toString());

        editor.commit();
    }

    protected void loadDataForResume() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int rows = preferences.getInt("ROWS", 0);
        int columns = preferences.getInt("COLUMNS", 0);


        if(!(rows == 0 || columns == 0)) {
            GameDifficulty difficulty = GameDifficulty.values()[preferences.getInt("DIFFICULTY", GameDifficulty.CUSTOM.ordinal())];
            long time = preferences.getLong("TIME", 0);
            int mineCount = preferences.getInt("MINE_COUNT", 0);
            GameStatus status = GameStatus.values()[preferences.getInt("STATUS", GameStatus.DEFEAT.ordinal())];
            int[][] cellValues = new int[rows][columns];
            boolean[][] cellRevealed = new boolean[rows][columns];
            boolean[][] cellFlagged = new boolean[rows][columns];

            try {
                JSONArray cellValuesJ = new JSONArray(preferences.getString("CELL_VALUES", "[]"));
                JSONArray cellRevealedJ = new JSONArray(preferences.getString("CELL_REVEALED", "[]"));
                JSONArray cellFlaggedJ = new JSONArray(preferences.getString("CELL_FLAGGED", "[]"));
                int counter = 0;
                for(int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        cellValues[r][c] = cellValuesJ.getInt(counter);
                        cellRevealed[r][c] = cellRevealedJ.getBoolean(counter);
                        cellFlagged[r][c] = cellFlaggedJ.getBoolean(counter);
                        counter++;
                    }
                }

                setupResumeBoard(mineCount, difficulty, status, time, cellValues, cellRevealed, cellFlagged);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * GOOGLE GAME CONNECTION
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ResultCodes.SIGN_IN.ordinal()) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                googleApiClient.connect();
            }
        } else if(requestCode == ResultCodes.SETTINGS.ordinal()) {
            applySettings();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // Already resolving
            return;
        }

        // If the sign in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    googleApiClient, connectionResult,
                    ResultCodes.SIGN_IN.ordinal(), getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }

        // Put code here to display the sign-in button

    }
}