package app.drewromanyk.com.minesweeper.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.support.v4.app.NavUtils
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.TextView

import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.*
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils
import app.drewromanyk.com.minesweeper.util.Helper
import app.drewromanyk.com.minesweeper.util.UserPrefStorage
import app.drewromanyk.com.minesweeper.views.BoardInfoView
import app.drewromanyk.com.minesweeper.views.MinesweeperUI

/**
 * Created by drewromanyk on 5/19/17.
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

        setupToolbar(findViewById(R.id.toolbar) as Toolbar, getString(R.string.nav_play))

        boardInfoView = BoardInfoView(
                findViewById(R.id.timeKeeper) as TextView,
                findViewById(R.id.mineKeeper) as TextView,
                findViewById(R.id.scoreKeeper) as TextView)
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
        boardBackground.setOnClickListener { _ ->
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
            val orientation: Int
            val rotation = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
            when (rotation) {
                Surface.ROTATION_0 -> orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                Surface.ROTATION_90 -> orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                Surface.ROTATION_180 -> orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                Surface.ROTATION_270 -> orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                else -> orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }

            requestedOrientation = orientation
        }

        // Theme Changing Background
        val uiThemeMode = UserPrefStorage.getUiThemeMode(this)
        boardBackground.setBackgroundColor(ContextCompat.getColor(this, uiThemeMode.color))
    }

    override fun onPause() {
        super.onPause()
        // TODO Minesweeper UI Game timer should stop here

        UserPrefStorage.saveBoardInfo(this, minesweeperUI)
    }

    public override fun onResume() {
        super.onResume()
        // TODO Minesweeper UI Game timer should start here if it is in playing mode

        Helper.screenViewOnGoogleAnalytics(this, "Game")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_game, menu)
        flagButton = menu.findItem(R.id.action_flag)
        refreshButton = menu.findItem(R.id.action_refresh)

        val action_zoomin = findViewById(R.id.action_zoomin)
        action_zoomin?.setOnLongClickListener {
            minesweeperUI.zoomIn(true)
            true
        }
        val action_zoomout = findViewById(R.id.action_zoomout)
        action_zoomout?.setOnLongClickListener {
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
                                //TODO update local stats
                                minesweeperUI.reset()
                            }
                            .setNegativeButton(android.R.string.no) { _, _ -> }
                            .create()
                    dialog.show()
                } else {
                    minesweeperUI.reset()
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

        if (!savedStateIsEmpty || gameDifficulty == GameDifficulty.RESUME) {
            // Load game from storage
            TODO()
//            minesweeperUI = UserPrefStorage.loadSavedBoard(this, false)
        } else {
            // Set to custom game defaults
            minesweeperUI = MinesweeperUI(gameDifficulty, boardInfoView, this, this::updateBoardInfo) {
                val icon = if (minesweeperUI.clickMode == ClickMode.FLAG)
                    R.drawable.ic_action_flag else R.drawable.ic_action_notflag
                flagButton.setIcon(icon)
            }
        }

        supportActionBar?.title = minesweeperUI.gameDifficulty.getName(this)

        vScroll.removeAllViews()
        vScroll.addView(minesweeperUI.layout)
    }

    private fun updateBoardInfo(gameTime: Long, score: Double) {
        runOnUiThread {
            boardInfoView.setTimeKeeperText(gameTime)
            boardInfoView.setScoreKeeperText(score)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBiDirectionalScrolling() {

        val hScroll = findViewById(R.id.scrollHorizontal) as HorizontalScrollView

        vScroll = findViewById(R.id.scrollVertical) as ScrollView

        vScroll.setOnTouchListener { v, _ ->
            //inner scroll listener
            v.performClick()
            false
        }
        hScroll.setOnTouchListener(object : View.OnTouchListener { //outer scroll listener
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
                        v.performClick()
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