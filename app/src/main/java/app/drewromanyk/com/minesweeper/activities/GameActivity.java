package app.drewromanyk.com.minesweeper.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Vibrator;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;

import app.drewromanyk.com.minesweeper.enums.GameSoundType;
import app.drewromanyk.com.minesweeper.enums.UiThemeModeEnum;
import app.drewromanyk.com.minesweeper.models.Board;
import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.enums.GameStatus;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.models.YesNoDialogInfo;
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils;
import app.drewromanyk.com.minesweeper.util.Helper;
import app.drewromanyk.com.minesweeper.util.SoundPlayer;
import app.drewromanyk.com.minesweeper.util.UserPrefStorage;
import app.drewromanyk.com.minesweeper.views.BoardInfoView;


public class GameActivity extends BackActivity {

    private static final String TAG = "GameActivity";

    private Board minesweeperBoard;
    // SOUNDS
    private SoundPlayer sound_player;
    // UI ELEMENTS
    private ScrollView vScroll;
    private View boardBackground;
    public BoardInfoView boardInfoView;
    public MenuItem refreshButton;
    public MenuItem flagButton;
    //SETTING
    private boolean flagMode = false;
    public boolean gamePlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setupAds();

        setupToolbar((Toolbar) findViewById(R.id.toolbar), getString(R.string.nav_play));

        setupBoardInfoLayout();
        setupBoardLayout(savedInstanceState);

        applySettings();
    }

    @Override
    protected void setupToolbar(Toolbar toolbar, String title) {
        super.setupToolbar(toolbar, title);

        toolbar.inflateMenu(R.menu.menu_game);
        flagButton = toolbar.getMenu().findItem(R.id.action_flag);
        refreshButton = toolbar.getMenu().findItem(R.id.action_refresh);
    }

    private void setupBoardInfoLayout() {
        boardInfoView = new BoardInfoView(
                (TextView) findViewById(R.id.timeKeeper),
                (TextView) findViewById(R.id.mineKeeper),
                (TextView) findViewById(R.id.scoreKeeper));
    }

    private void setupBoardLayout(Bundle savedInstanceState) {
        boardBackground = findViewById(R.id.boardBackground);
        boardBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (minesweeperBoard != null && minesweeperBoard.getGameStatus() == GameStatus.PLAYING && UserPrefStorage.getSwiftChange(v.getContext())) {
                    changeFlagMode(minesweeperBoard);
                }
            }
        });

        setupBiDirectionalScrolling();
        setupBoard(savedInstanceState == null);
    }

    private void applySettings() {
        if (UserPrefStorage.getScreenOn(this)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (UserPrefStorage.getLockRotate(this)) {
            int orientation;
            int rotation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                case Surface.ROTATION_270:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
            }

            setRequestedOrientation(orientation);
        }

        // Theme Changing Background
        UiThemeModeEnum uiThemeMode = UserPrefStorage.getUiThemeMode(this);
        switch (uiThemeMode) {
            case LIGHT:
                boardBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.light_background));
                break;
            case DARK:
                boardBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_background));
                break;
            case AMOLED:
                boardBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.amoled_background));
                break;
            case CLASSICAL:
                boardBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.light_background));
                break;
        }
        minesweeperBoard.updateCellSize();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gamePlaying = (minesweeperBoard.getGameStatus() == GameStatus.PLAYING);
        if (minesweeperBoard != null) {
            minesweeperBoard.stopGameTime();
        }

        UserPrefStorage.saveBoardInfo(this, minesweeperBoard);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (minesweeperBoard != null && !minesweeperBoard.getFirstRound() && gamePlaying)
            minesweeperBoard.startGameTime();

        Helper.screenViewOnGoogleAnalytics(this, "Game");
    }

    @Override
    protected void onStart() {
        super.onStart();
        sound_player = new SoundPlayer(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sound_player.release();
        sound_player = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        flagButton = menu.findItem(R.id.action_flag);
        refreshButton = menu.findItem(R.id.action_refresh);

        View action_zoomin = findViewById(R.id.action_zoomin);
        if (action_zoomin != null) action_zoomin.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (minesweeperBoard != null) {
                            minesweeperBoard.zoomInFully();
                        }
                        return true;
                    }
                }
        );
        View action_zoomout = findViewById(R.id.action_zoomout);
        if (action_zoomout != null) action_zoomout.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (minesweeperBoard != null) {
                            minesweeperBoard.zoomOutFully();
                        }
                        return true;
                    }
                }
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                if (minesweeperBoard.getGameStatus() == GameStatus.PLAYING) {
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
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create();
                    dialog.show();
                } else {
                    setupBoard(true);
                }
                return true;
            case R.id.action_flag:
                changeFlagMode(minesweeperBoard);
                return true;
            case R.id.action_zoomin:
                if (minesweeperBoard != null) {
                    minesweeperBoard.zoomIn();
                }
                return true;
            case R.id.action_zoomout:
                if (minesweeperBoard != null) {
                    minesweeperBoard.zoomOut();
                }
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (!isFinishing()) {
            if (UserPrefStorage.getVolumeButton(this)) {
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
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /*
     * USER FEEDBACK
     */

    public void playSoundEffects(GameSoundType type) {
        sound_player.play(type);
    }

    public void vibrate() {
        if (UserPrefStorage.getVibrate(this)) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(100);
        }
    }

    /*
     * FLAG MODE
     */

    public boolean getFlagMode() {
        return flagMode;
    }

    public void changeFlagMode(Board board) {
        flagMode = !flagMode;
        int icon = (flagMode) ? R.drawable.ic_action_flag : R.drawable.ic_action_notflag;
        flagButton.setIcon(icon);
        board.updateCellSize();
    }

    /*
     * SETUP BOARD
     */

    private void setupBoard(boolean savedStateIsEmpty) {
        GameDifficulty gameDifficulty = GameDifficulty.valueOf(getIntent().getStringExtra("gameDifficulty"));
        if (gamePlaying) {
            gameDifficulty = minesweeperBoard.getGameDifficulty();
        }
        if (minesweeperBoard != null) {
            minesweeperBoard.stopGameTime();
        }

        // Create or load a new board
        if ((gameDifficulty == GameDifficulty.RESUME && minesweeperBoard == null) || !savedStateIsEmpty) {
            minesweeperBoard = UserPrefStorage.loadSavedBoard(this, false);
        } else {
            // Set to custom game defaults
            int columns = UserPrefStorage.getColumnCount(this);
            int rows = UserPrefStorage.getRowCount(this);
            int mineCount = UserPrefStorage.getMineCount(this);

            if (gameDifficulty == GameDifficulty.EASY) {
                columns = 9;
                rows = 9;
                mineCount = 10;
            } else if (gameDifficulty == GameDifficulty.MEDIUM) {
                columns = 16;
                rows = 16;
                mineCount = 40;
            } else if (gameDifficulty == GameDifficulty.EXPERT) {
                columns = 30;
                rows = 16;
                mineCount = 99;
            }

            boardInfoView.resetInfo(mineCount);
            double gameCellScale = (minesweeperBoard != null) ? minesweeperBoard.getGameCellScale() : 1;
            minesweeperBoard = new Board(rows, columns, mineCount, gameCellScale, gameDifficulty, this);
        }

        assert minesweeperBoard != null;
        assert getSupportActionBar() != null;

        switch (minesweeperBoard.getGameDifficulty()) {
            case EASY:
                getSupportActionBar().setTitle(getString(R.string.game_difficulty_easy));
                break;
            case MEDIUM:
                getSupportActionBar().setTitle(getString(R.string.game_difficulty_medium));
                break;
            case EXPERT:
                getSupportActionBar().setTitle(getString(R.string.game_difficulty_expert));
                break;
            default:
                getSupportActionBar().setTitle(getString(R.string.game_difficulty_custom));
                break;
        }

        gamePlaying = (minesweeperBoard.getGameStatus() == GameStatus.PLAYING);
        addBoardToLayout();
    }

    private void setupBiDirectionalScrolling() {

        final HorizontalScrollView hScroll = (HorizontalScrollView) findViewById(R.id.scrollHorizontal);
        if (hScroll == null) return;

        vScroll = (ScrollView) findViewById(R.id.scrollVertical);

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
        if (flagMode) {
            changeFlagMode(minesweeperBoard);
        }
    }
}