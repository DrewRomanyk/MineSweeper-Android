package app.drewromanyk.com.minesweeper.ui


import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NavUtils
import androidx.fragment.app.Fragment
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.ClickMode
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.enums.GameStatus
import app.drewromanyk.com.minesweeper.enums.ResultCodes
import app.drewromanyk.com.minesweeper.interfaces.MinesweeperUiHandler
import app.drewromanyk.com.minesweeper.interfaces.UpdateAdViewHandler
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils
import app.drewromanyk.com.minesweeper.util.PremiumUtils
import app.drewromanyk.com.minesweeper.util.UserPrefStorage
import app.drewromanyk.com.minesweeper.views.BoardInfoView
import app.drewromanyk.com.minesweeper.views.MinesweeperUI
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.fragment_game.*
import kotlinx.android.synthetic.main.game_board_frame.*
import kotlinx.android.synthetic.main.game_info_frame.*

/**
 * Fragment to allow users to play the game.
 */
class GameFragment : Fragment(), UpdateAdViewHandler {
    private lateinit var minesweeperUI: MinesweeperUI

    // UI ELEMENTS
    private lateinit var vScroll: ScrollView
    private lateinit var boardInfoView: BoardInfoView
    private lateinit var refreshButton: MenuItem
    private var flagButton: MenuItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        boardInfoView = BoardInfoView(
                timeKeeper,
                mineKeeper,
                scoreKeeper)
        setupBoardLayout(savedInstanceState)

        applySettings()

        PremiumUtils.instance.updateContext(requireContext(), this)
        val handler = Handler()
        handler.postDelayed({
            adView?.loadAd(AdRequest.Builder().build())
            updateAdView()
        }, 1000)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_game, menu)
        flagButton = menu!!.findItem(R.id.action_flag)
        refreshButton = menu.findItem(R.id.action_refresh)

        requireActivity().findViewById<View>(R.id.action_zoomin)

        val actionZoomIn = requireActivity().findViewById<View>(R.id.action_zoomin)
        actionZoomIn.setOnLongClickListener {
            minesweeperUI.zoomIn(true)
            true
        }
        val actionZoomOut = requireActivity().findViewById<View>(R.id.action_zoomout)
        actionZoomOut.setOnLongClickListener {
            minesweeperUI.zoomOut(true)
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_refresh -> {
                if (minesweeperUI.isPlaying()) {
                    val dialogInfo = DialogInfoUtils.getInstance(requireContext()).getDialogInfo(ResultCodes.RESTART_DIALOG.ordinal)
                    val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(dialogInfo.title)
                            .setMessage(dialogInfo.description)
                            .setPositiveButton(android.R.string.yes) { _, _ ->
                                minesweeperUI.reset(requireContext())
                            }
                            .setNegativeButton(android.R.string.no) { _, _ -> }
                            .create()
                    dialog.show()
                } else {
                    minesweeperUI.reset(requireContext())
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
                NavUtils.navigateUpFromSameTask(requireActivity())
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun updateAdView() {
        if (PremiumUtils.instance.isPremiumUser) {
            adView?.pause()
            adView?.visibility = View.GONE
        } else {
            adView?.resume()
            adView?.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adView?.destroy()
        PremiumUtils.instance.releaseContext()
    }

    override fun onPause() {
        super.onPause()
        minesweeperUI.pauseTimer()
        minesweeperUI.save(requireContext())
        adView?.pause()
    }

    override fun onResume() {
        super.onResume()
        minesweeperUI.resumeTimer()
        updateAdView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
    }

    private fun setupToolbar() {
        (requireActivity() as MainActivity).setSupportActionBar(toolbar)
        toolbar.inflateMenu(R.menu.menu_game)
        toolbar.setNavigationIcon(R.drawable.ic_back_button)
        toolbar.setNavigationOnClickListener {
            (requireActivity() as MainActivity).onSupportNavigateUp()
        }
        toolbar.title = getString(R.string.nav_play)
        setHasOptionsMenu(true)
    }

    private fun setupBoardLayout(savedInstanceState: Bundle?) {
        boardBackground.setOnClickListener {
            if (UserPrefStorage.getSwiftChange(requireContext())) {
                minesweeperUI.switchClickMode()
            }
        }

        setupBiDirectionalScrolling()
        setupGame(savedInstanceState)
    }

    private fun applySettings() {
        if (UserPrefStorage.getScreenOn(requireContext())) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        if (UserPrefStorage.getLockRotate(requireContext())) {
            val rotation = (requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
            requireActivity().requestedOrientation = when (rotation) {
                Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                else -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
        }
    }

    /*
     * SETUP BOARD
     */

    private fun setupGame(savedInstanceState: Bundle?) {
        val argsGameDifficulty = GameDifficulty.valueOf(arguments!!.getString("gameDifficulty", GameDifficulty.EASY.name))
        var gameDifficulty = argsGameDifficulty
        if (argsGameDifficulty == GameDifficulty.RESUME) {
            gameDifficulty = UserPrefStorage.getGameDifficulty(requireContext())
        }

        val shouldLoadGame = savedInstanceState != null || (argsGameDifficulty == GameDifficulty.RESUME)

        minesweeperUI = MinesweeperUI(shouldLoadGame, gameDifficulty, boardInfoView, requireContext(), object : MinesweeperUiHandler {
            override fun onGameTimerTick(gameTime: Long, score: Double) {
                requireActivity().runOnUiThread {
                    boardInfoView.setTimeKeeperText(gameTime)
                    boardInfoView.setScoreKeeperText(score)
                }
            }

            override fun onFlagChange(clickMode: ClickMode) {
                val icon = if (clickMode == ClickMode.FLAG)
                    R.drawable.ic_flag_black else R.drawable.ic_flag_black
                flagButton?.setIcon(icon)
            }

            override fun onVictory(score: Long, time: Long) {
                (requireActivity() as MainActivity).updateLeaderboards(GameStatus.VICTORY, minesweeperUI.gameDifficulty, score, time)
            }
        })

        vScroll.removeAllViews()
        vScroll.addView(minesweeperUI.layout)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBiDirectionalScrolling() {

        val hScroll = scrollHorizontal

        vScroll = scrollVertical

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
