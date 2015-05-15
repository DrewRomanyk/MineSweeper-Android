package app.drewromanyk.com.minesweeper.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.example.games.basegameutils.BaseGameUtils;

import org.json.JSONArray;

import java.util.ArrayList;

import app.drewromanyk.com.minesweeper.models.Board;
import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.fragment.YesNoDialog;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.enums.GameStatus;
import app.drewromanyk.com.minesweeper.enums.ImageDownloadType;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.network.ImageDownloader;
import app.drewromanyk.com.minesweeper.util.UserPreferenceStorage;


public class GameActivity extends BaseActivity {

    private String TAG = "tacotaco";

    private Board minesweeperBoard;
    //GENERAL
    public static Context context;
    public static Activity activity;
    private static Menu menu;
    private static FragmentManager fragManager;
    //Sound
    public static SoundPool soundEffects;
    public static ArrayList<Integer> soundIDs;
    //UI ELEMENTS
    private ScrollView vScroll;
    private HorizontalScrollView hScroll;
    private ImageView boardBackground;
    public static TextView mineKeeperView;
    public static TextView scoreKeeperView;
    public static MenuItem refreshButton;
    public static Chronometer chronometer;
    public static long lastStopTime;
    //GAME STATUS
    public static boolean gamePlaying = false;
    //SETTING
    public static double pinchScale;
    private static boolean flagMode;
    //GOOGLE
    //GOOGLE GAMES
    public static GoogleApiClient googleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mSignInClicked = false;
    protected boolean mAutoStartSignInFlow = false;
    private boolean mStarted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAutoStartSignInFlow = false;
        setContentView(R.layout.activity_game);
        super.onCreate(savedInstanceState);

        // Create the Google API Client with access to Plus and Games
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        baseGoogleApiClient = googleApiClient;

        //Get a Tracker (should auto-report)
        ((MyApplication) getApplication()).getTracker(MyApplication.TrackerName.APP_TRACKER);


        context = this;
        activity = this;
        flagMode = false;
        fragManager = getSupportFragmentManager();
        pinchScale = 1;

        setupBiDirectionalScrolling();
        // Sound Effects
        soundEffects = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundIDs = new ArrayList<>();
        soundIDs.add(soundEffects.load(this, R.raw.click_short, 1));
        soundIDs.add(soundEffects.load(this, R.raw.click_long, 1));
        soundIDs.add(soundEffects.load(this, R.raw.effect_win, 1));
        soundIDs.add(soundEffects.load(this, R.raw.effect_lose, 1));
        mineKeeperView = (TextView) findViewById(R.id.mineKeeper);
        scoreKeeperView = (TextView) findViewById(R.id.scoreKeeper);
        boardBackground = (ImageView) findViewById(R.id.boardBackground);
        chronometer = (Chronometer) findViewById(R.id.timeKeeper);
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if(minesweeperBoard != null) {
                    scoreKeeperView.setText("Score: " + minesweeperBoard.getGameScore());
                } else {
                    scoreKeeperView.setText("Score: 0.000");
                }

            }
        });
        setupBoard(savedInstanceState == null);

        if(UserPreferenceStorage.getScreenOn(this)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // Theme Changing Background
        if(UserPreferenceStorage.getLightMode(this)) {
            boardBackground.setBackgroundColor(getResources().getColor(R.color.light_background));
            minesweeperBoard.updateCellSize();
        } else {
            boardBackground.setBackgroundColor(Color.parseColor("#424242"));
            minesweeperBoard.updateCellSize();
        }

    }

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
            minesweeperBoard.updateCellSize();
            // Theme Changing Background
            if(UserPreferenceStorage.getLightMode(this)) {
                boardBackground.setBackgroundColor(getResources().getColor(R.color.light_background));
            } else {
                boardBackground.setBackgroundColor(Color.parseColor("#424242"));
            }
//            if(!UserPreferenceStorage.getNavDrawer(this))
//                navDrawerInfo.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        googleApiClient.connect();

        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gamePlaying = (minesweeperBoard.getGameStatus() == GameStatus.PLAYING);
        stopChronometer();

        saveDataForResume();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(minesweeperBoard != null && !minesweeperBoard.getFirstRound())
            startChronometer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        //Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        this.menu = menu;
        refreshButton = menu.findItem(R.id.action_refresh);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_refresh :
                if(minesweeperBoard.getGameStatus() == GameStatus.PLAYING) {
                    showYesNoDialog(ResultCodes.RESTART_DIALOG.ordinal());
                } else {
                    setupBoard(true);
                }
                return true;
            case R.id.action_flag :
                changeFlagMode(minesweeperBoard);
                return true;
            case android.R.id.home :
                NavUtils.navigateUpFromSameTask(activity);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    //use volume buttons to change flag mode
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        if(UserPreferenceStorage.getVolumeButton(this)) {
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

    @Override
    protected void initToolbar() {
        super.initToolbar();

        if (toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_game);
        refreshButton = toolbar.getMenu().findItem(R.id.action_refresh);
    }

    @Override
    protected void initDrawer() {
        super.initDrawer();

        if(!UserPreferenceStorage.getNavDrawer(this))
            navDrawerInfo.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    protected void doNavDrawerActions(int position) {
        super.doNavDrawerActions(position);

        if(position == 0) {
            if (googleApiClient.isConnected()) {
                Games.signOut(googleApiClient);
                googleApiClient.disconnect();
                navDrawerInfo.getHeaderInfo().setPlayerToEmpty();
                navDrawerInfo.getRecyclerView().getAdapter().notifyItemChanged(0);
            } else {
                mSignInClicked = true;
                googleApiClient.connect();
            }
        }
    }

    @Override
    public void doPositiveClick(int REQUEST_CODE) {
        super.doPositiveClick(REQUEST_CODE);
        if (REQUEST_CODE == ResultCodes.NEEDGOOGLE_DIALOG.ordinal()) {
            mSignInClicked = true;
            googleApiClient.connect();
        }else if(REQUEST_CODE == ResultCodes.RESTART_DIALOG.ordinal()) {
            minesweeperBoard.gameOverByRestart();
            setupBoard(true);
        }else if(REQUEST_CODE == ResultCodes.GAMEOVER_DIALOG.ordinal()) {
            setupBoard(true);
        }
    }

    /*
     * GOOGLE GAME CONNECTION
     */

    @Override
    public void onConnected(Bundle bundle) {
        if ( isOnline() && Plus.PeopleApi.getCurrentPerson(googleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
            navDrawerInfo.getHeaderInfo().setName(currentPerson.getDisplayName());
            navDrawerInfo.getHeaderInfo().setEmail(Plus.AccountApi.getAccountName(googleApiClient));

            String playerAvatarURL = currentPerson.getImage().getUrl();
            int index = playerAvatarURL.indexOf("?sz=");
            if (index != -1) {
                playerAvatarURL = playerAvatarURL.substring(0, index) + "?sz=200";
            }
            new ImageDownloader(ImageDownloadType.AVATAR).execute(playerAvatarURL);
            if(currentPerson.getCover() != null && currentPerson.getCover().hasCoverPhoto()) {
                String playerCoverURL = currentPerson.getCover().getCoverPhoto().getUrl();
                new ImageDownloader(ImageDownloadType.COVER).execute(playerCoverURL);
            }
        }
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

    /*
     *
     */
    public static void showGameOverDialog(GameStatus gameStatus, String message) {
        DialogFragment dialog = new YesNoDialog();
        Bundle args = new Bundle();
        String title = (gameStatus == GameStatus.VICTORY) ? "Victory!" : "Defeat!";

        args.putString("title", title);
        args.putString("message", message);

        dialog.setArguments(args);
        dialog.setTargetFragment(dialog, ResultCodes.GAMEOVER_DIALOG.ordinal());
        dialog.show(fragManager, "tag");


    }



    /*
     * FLAG MODE
     */

    public static boolean getFlagMode() { return flagMode; }

    public static void changeFlagMode(Board board) {
        flagMode = !flagMode;
        int icon = (flagMode) ? R.drawable.ic_action_flag : R.drawable.ic_action_notflag;
        menu.getItem(1).setIcon(icon);
        board.updateCellSize();

    }


    /*
     * CHRONOMETER
     */
    public static void startChronometer() {
        // on first start
        if ( lastStopTime == 0 ) {
            chronometer.setBase(SystemClock.elapsedRealtime());
        } else {
            long intervalOnPause = (SystemClock.elapsedRealtime() - lastStopTime);
            chronometer.setBase( chronometer.getBase() + intervalOnPause + 500);
        }
        if(gamePlaying) {
            chronometer.start();
        } else {
            chronometer.stop();
        }
    }

    public static void stopChronometer() {
        lastStopTime = SystemClock.elapsedRealtime();
        chronometer.stop();
    }

    /*
     * SETUP BOARD
     */

    private void setupBoard(boolean savedStateIsEmpty) {
        stopChronometer();
        if(gameMode == GameDifficulty.RESUME || !savedStateIsEmpty) {
            loadDataForResume();
            return;
        }

        int columns = UserPreferenceStorage.getColumnCount(this);
        int rows = UserPreferenceStorage.getRowCount(this);
        int mineCount = UserPreferenceStorage.getMineCount(this);

        getSupportActionBar().setTitle(getString(R.string.game_difficulty_custom));
        if (gameMode == GameDifficulty.EASY) {
            columns = 9;
            rows = 9;
            mineCount = 10;
            getSupportActionBar().setTitle(getString(R.string.game_difficulty_easy));
        } else if (gameMode == GameDifficulty.MEDIUM) {
            columns = 16;
            rows = 16;
            mineCount = 40;
            getSupportActionBar().setTitle(getString(R.string.game_difficulty_medium));
        } else if (gameMode == GameDifficulty.EXPERT) {
            columns = 30;
            rows = 16;
            mineCount = 99;
            getSupportActionBar().setTitle(getString(R.string.game_difficulty_expert));
        }

        mineKeeperView.setText("Mines: " + mineCount);
        scoreKeeperView.setText("Score: 0.000");
        lastStopTime = 0;
        chronometer.setBase(SystemClock.elapsedRealtime());

        minesweeperBoard = new Board(rows, columns, mineCount, this);
        gamePlaying = (minesweeperBoard.getGameStatus() == GameStatus.PLAYING);
        addBoardToLayout();
    }

    private void setupResumeBoard(int mineCount, GameDifficulty difficulty, GameStatus status, long time,
                                  int[][] cellValues, boolean[][] cellRevealed, boolean[][] cellFlagged) {
        gameMode = difficulty;
        lastStopTime = SystemClock.elapsedRealtime() + ((time + 1) * (1000/2));

        getSupportActionBar().setTitle(getString(R.string.game_difficulty_custom));
        if (gameMode == GameDifficulty.EASY) {
            getSupportActionBar().setTitle(getString(R.string.game_difficulty_easy));
        } else if (gameMode == GameDifficulty.MEDIUM) {
            getSupportActionBar().setTitle(getString(R.string.game_difficulty_medium));
        } else if (gameMode == GameDifficulty.EXPERT) {
            getSupportActionBar().setTitle(getString(R.string.game_difficulty_expert));
        }

        minesweeperBoard = new Board(mineCount, cellValues, cellRevealed, cellFlagged, status, this);
        gamePlaying = (minesweeperBoard.getGameStatus() == GameStatus.PLAYING);
        addBoardToLayout();
    }

    private void setupBiDirectionalScrolling() {

        hScroll = (HorizontalScrollView) findViewById(R.id.scrollHorizontal);
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
        editor.putInt("DIFFICULTY", gameMode.ordinal());
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
}
