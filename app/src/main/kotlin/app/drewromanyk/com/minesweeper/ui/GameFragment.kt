package app.drewromanyk.com.minesweeper.ui


import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NavUtils
import androidx.fragment.app.Fragment
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.ClickMode
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.enums.GameStatus
import app.drewromanyk.com.minesweeper.enums.ResultCodes
import app.drewromanyk.com.minesweeper.interfaces.GameUiHandler
import app.drewromanyk.com.minesweeper.interfaces.MinesweeperUiHandler
import app.drewromanyk.com.minesweeper.interfaces.UpdateAdViewHandler
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils
import app.drewromanyk.com.minesweeper.util.PremiumUtils
import app.drewromanyk.com.minesweeper.util.UserPrefStorage
import app.drewromanyk.com.minesweeper.views.BoardInfoView
import app.drewromanyk.com.minesweeper.views.MinesweeperUI
import com.google.android.gms.ads.AdRequest
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.fragment_game.*
import kotlinx.android.synthetic.main.game_board_frame.*
import kotlinx.android.synthetic.main.game_info_frame.*

/**
 * Fragment to allow users to play the game.
 */
class GameFragment : Fragment(), UpdateAdViewHandler, GameUiHandler {
    private lateinit var minesweeperUI: MinesweeperUI

    // UI ELEMENTS
    private lateinit var boardInfoView: BoardInfoView
    private var refreshButton: MenuItem? = null
    private var flagButton: MenuItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).gameUiHandler = this

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
                                refreshButton?.setIcon(R.drawable.ic_game_pending)
                                minesweeperUI.reset(requireContext())
                            }
                            .setNegativeButton(android.R.string.no) { _, _ -> }
                            .create()
                    dialog.show()
                } else {
                    refreshButton?.setIcon(R.drawable.ic_game_pending)
                    minesweeperUI.reset(requireContext())                }
                return true
            }
            R.id.action_flag -> {
                minesweeperUI.switchClickMode()
                return true
            }
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(requireActivity())
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun toggleClickMode() {
        minesweeperUI.switchClickMode()
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
        (requireActivity() as MainActivity).gameUiHandler = null
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
        val firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        firebaseAnalytics.setCurrentScreen(requireActivity(), AboutFragment::javaClass.javaClass.simpleName, AboutFragment::javaClass.javaClass.simpleName)
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
        zoomLayout.setOnClickListener {
            if (UserPrefStorage.getSwiftChange(requireContext())) {
                minesweeperUI.switchClickMode()
            }
        }

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
                    R.drawable.ic_flag_solid else R.drawable.ic_flag_outline
                flagButton?.setIcon(icon)
            }

            override fun onDefeat() {
                refreshButton?.setIcon(R.drawable.ic_game_defeat)
            }

            override fun onVictory(score: Long, time: Long) {
                refreshButton?.setIcon(R.drawable.ic_game_victory)
                (requireActivity() as MainActivity).updateLeaderboards(GameStatus.VICTORY, minesweeperUI.gameDifficulty, score, time)
            }
        })

        zoomContainer.removeAllViews()
        zoomContainer.addView(minesweeperUI.layout)
    }
}
