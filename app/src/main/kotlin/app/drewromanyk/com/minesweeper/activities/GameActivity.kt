package app.drewromanyk.com.minesweeper.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat

import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.*
import app.drewromanyk.com.minesweeper.interfaces.MinesweeperUiHandler
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils
import app.drewromanyk.com.minesweeper.util.Helper
import app.drewromanyk.com.minesweeper.util.UserPrefStorage
import app.drewromanyk.com.minesweeper.views.BoardInfoView
import app.drewromanyk.com.minesweeper.views.MinesweeperUI

/**
 * Created by Drew Romanyk on 5/19/17.
 * Activity that handles the whole game and its interactions
 */


class GameActivity : BackActivity() {

    private lateinit var minesweeperUI: MinesweeperUI
    // UI ELEMENTS
    private lateinit var vScroll: ScrollView
    private lateinit var boardBackground: View
    private lateinit var boardInfoView: BoardInfoView
    private lateinit var refreshButton: MenuItem
    private lateinit var flagButton: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setupAds()

        setupToolbar(findViewById(R.id.toolbar), getString(R.string.nav_play))

        boardInfoView = BoardInfoView(
                findViewById(R.id.timeKeeper),
                findViewById(R.id.mineKeeper),
                findViewById(R.id.scoreKeeper))
        setupBoardLayout(savedInstanceState)

        applySettings()
    }

    override fun setupToolbar(toolbar: Toolbar, title: String) {
        super.setupToolbar(toolbar, title)

        toolbar.inflateMenu(R.menu.menu_game)
        flagButton = toolbar.menu.findItem(R.id.action_flag)
        refreshButton = toolbar.menu.findItem(R.id.action_refresh)
    }

    private fun setupBoardLayout(savedInstanceState: Bundle?) {
        boardBackground = findViewById(R.id.boardBackground)
        boardBackground.setOnClickListener {
            if (UserPrefStorage.getSwiftChange(this)) {
                minesweeperUI.switchClickMode()
            }
        }

        setupBiDirectionalScrolling()
        setupGame(savedInstanceState == null)
    }

    private fun applySettings() {
        if (UserPrefStorage.getScreenOn(this)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        if (UserPrefStorage.getLockRotate(this)) {
            val rotation = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
            requestedOrientation = when (rotation) {
                Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                else -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
        }

        // Theme Changing Background
        val uiThemeMode = UserPrefStorage.getUiThemeMode(this)
        boardBackground.setBackgroundColor(ContextCompat.getColor(this, uiThemeMode.color))
    }

    override fun onPause() {
        super.onPause()
        minesweeperUI.pauseTimer()
        minesweeperUI.save(this)
    }

    public override fun onResume() {
        super.onResume()
        minesweeperUI.resumeTimer()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_game, menu)
        flagButton = menu.findItem(R.id.action_flag)
        refreshButton = menu.findItem(R.id.action_refresh)

        val actionZoomIn = findViewById<View>(R.id.action_zoomin)
        actionZoomIn.setOnLongClickListener {
            minesweeperUI.zoomIn(true)
            true
        }
        val actionZoomOut = findViewById<View>(R.id.action_zoomout)
        actionZoomOut.setOnLongClickListener {
            minesweeperUI.zoomOut(true)
            true
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_refresh -> {
                if (minesweeperUI.isPlaying()) {
                    val dialogInfo = DialogInfoUtils.getInstance(this).getDialogInfo(ResultCodes.RESTART_DIALOG.ordinal)
                    val dialog = AlertDialog.Builder(this)
                            .setTitle(dialogInfo.title)
                            .setMessage(dialogInfo.description)
                            .setPositiveButton(android.R.string.yes) { _, _ ->
                                minesweeperUI.reset(this)
                            }
                            .setNegativeButton(android.R.string.no) { _, _ -> }
                            .create()
                    dialog.show()
                } else {
                    minesweeperUI.reset(this)
                }
                return true
            }
            R.id.action_flag -> {
                minesweeperUI.switchClickMode()
                return true
            }
            R.id.action_zoomin -> {
                minesweeperUI.zoomIn(false)
                return true
            }
            R.id.action_zoomout -> {
                minesweeperUI.zoomOut(false)
                return true
            }
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode
        if (!isFinishing) {
            if (UserPrefStorage.getVolumeButton(this)) {
                when (keyCode) {
                    KeyEvent.KEYCODE_VOLUME_UP -> {
                        if (action == KeyEvent.ACTION_DOWN) {
                            minesweeperUI.switchClickMode()
                        }
                        return true
                    }
                    KeyEvent.KEYCODE_VOLUME_DOWN -> {
                        if (action == KeyEvent.ACTION_DOWN) {
                            minesweeperUI.switchClickMode()
                        }
                        return true
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    /*
     * SETUP BOARD
     */

    private fun setupGame(savedStateIsEmpty: Boolean) {
        val gameDifficulty = GameDifficulty.valueOf(intent.getStringExtra("gameDifficulty"))

        minesweeperUI = MinesweeperUI(!savedStateIsEmpty, gameDifficulty, boardInfoView, this, object : MinesweeperUiHandler {
            override fun onGameTimerTick(gameTime: Long, score: Double) {
                runOnUiThread {
                    boardInfoView.setTimeKeeperText(gameTime)
                    boardInfoView.setScoreKeeperText(score)
                }
            }

            override fun onFlagChange(clickMode: ClickMode) {
                val icon = if (clickMode == ClickMode.FLAG)
                    R.drawable.ic_action_flag else R.drawable.ic_action_notflag
                flagButton.setIcon(icon)
            }

            override fun onVictory(score: Long, time: Long) {
                updateLeaderboards(GameStatus.VICTORY, minesweeperUI.gameDifficulty, score, time)
            }
        })

        supportActionBar?.title = minesweeperUI.gameDifficulty.getName(this)

        vScroll.removeAllViews()
        vScroll.addView(minesweeperUI.layout)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBiDirectionalScrolling() {

        val hScroll = findViewById<HorizontalScrollView>(R.id.scrollHorizontal)

        vScroll = findViewById(R.id.scrollVertical)

        //inner scroll listener
        vScroll.setOnTouchListener { _, _ ->
            false
        }

        //outer scroll listener
        hScroll.setOnTouchListener(object : View.OnTouchListener {
            private var mx: Float = 0.toFloat()
            private var my: Float = 0.toFloat()
            private var curX: Float = 0.toFloat()
            private var curY: Float = 0.toFloat()
            private var started = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                curX = event.x
                curY = event.y
                val dx = (mx - curX).toInt()
                val dy = (my - curY).toInt()
                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {
                        if (started) {
                            vScroll.scrollBy(0, dy)
                            hScroll.scrollBy(dx, 0)
                        } else {
                            started = true
                        }
                        mx = curX
                        my = curY
                    }
                    MotionEvent.ACTION_UP -> {
                        vScroll.scrollBy(0, dy)
                        hScroll.scrollBy(dx, 0)
                        started = false
                    }
                }
                return true
            }
        })
    }
}