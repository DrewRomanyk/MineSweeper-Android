package app.drewromanyk.com.minesweeper.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.activities.GameActivity
import app.drewromanyk.com.minesweeper.activities.SettingsActivity
import app.drewromanyk.com.minesweeper.adapters.PlayGameDifficultyAdapter
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.enums.GameStatus
import app.drewromanyk.com.minesweeper.enums.ResultCodes
import app.drewromanyk.com.minesweeper.interfaces.PlayNavigator
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils
import app.drewromanyk.com.minesweeper.util.Helper
import app.drewromanyk.com.minesweeper.util.UserPrefStorage

/**
 * Created by Drew on 9/11/15.
 * PlayFragment
 * Fragment Main home screen to allow a user to play a minesweeper game with their choice of
 * difficulty.
 */
class PlayFragment : BaseFragment(), PlayNavigator {

    private lateinit var adapter: PlayGameDifficultyAdapter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_play, container, false) as ViewGroup

        setupToolbar(root.findViewById(R.id.toolbar) as Toolbar, getString(R.string.nav_play))
        setupPlayButtons(root)

        return root
    }

    override fun onStart() {
        super.onStart()
        updatePlaySelectButtons()
    }

    override fun onResume() {
        super.onResume()
        Helper.screenViewOnGoogleAnalytics(activity, "Play")
    }

    private fun setupPlayButtons(root: ViewGroup) {
        adapter = PlayGameDifficultyAdapter(this)
        val recyclerView = root.findViewById(R.id.playGameDifficultyRV) as RecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        updatePlaySelectButtons()
    }

    private fun hasResumeGame(): Boolean {
        return (UserPrefStorage.getLastGameStatus(activity) == GameStatus.PLAYING.ordinal) and (UserPrefStorage.isCurrentSavedDataVersion(activity))
    }

    private fun updatePlaySelectButtons() {
        val gameDifficulties = ArrayList<GameDifficulty>()
        if (hasResumeGame()) {
            gameDifficulties.add(GameDifficulty.RESUME)
        }
        gameDifficulties.add(GameDifficulty.CUSTOM)
        gameDifficulties.add(GameDifficulty.EASY)
        gameDifficulties.add(GameDifficulty.MEDIUM)
        gameDifficulties.add(GameDifficulty.EXPERT)

        //TODO fix this comment
        //        adapter.setCanShowRating(UserPrefStorage.INSTANCE.canShowRatingDialog(getActivity()));
        adapter.setGameDifficultyList(gameDifficulties)
        adapter.notifyDataSetChanged()
    }

    override fun startGame(difficulty: GameDifficulty) {
        if (hasResumeGame() and (difficulty !== GameDifficulty.RESUME)) {
            // A current game exists, ask if they want to delete
            val (title, description) = DialogInfoUtils.getInstance(activity).getDialogInfo(ResultCodes.RESUME_DIALOG.ordinal)
            val dialog = AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(description)
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        //TODO
                        //                            BoardOld statsBoard = UserPrefStorage.loadSavedBoard(getActivity(), true);
                        //                            statsBoard.updateLocalStatistics(getActivity());
                        //                            startGameIntent(difficulty);
                    }
                    .setNegativeButton(android.R.string.no) { _, _ -> }
                    .create()
            dialog.show()
        } else {
            if (difficulty === GameDifficulty.CUSTOM) {
                // Ask if they want to change their custom settings
                val (title, description) = DialogInfoUtils.getInstance(activity).getDialogInfo(ResultCodes.CUSTOM_SETTING_CHANGE.ordinal)
                val dialog = AlertDialog.Builder(activity)
                        .setTitle(title)
                        .setMessage(description)
                        .setPositiveButton(android.R.string.yes) { _, _ ->
                            // Change settings
                            startActivity(Intent(activity, SettingsActivity::class.java))
                        }
                        .setNegativeButton(R.string.nav_play) { _, _ ->
                            // No current game exists, create new game
                            startGameIntent(difficulty)
                        }
                        .create()
                dialog.show()
            } else {
                // No current game exists, create new game
                startGameIntent(difficulty)
            }
        }
    }

    private fun startGameIntent(difficulty: GameDifficulty) {
        val startGame = Intent(activity, GameActivity::class.java)
        startGame.putExtra("gameDifficulty", difficulty.name)
        startActivity(startGame)
    }

    override fun startPlayStore() {
        val uri = Uri.parse("market://details?id=" + activity.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + activity.packageName)))
        }

    }

    override fun sendFeedback() {
        Helper.sendFeedback(activity)
    }
}
