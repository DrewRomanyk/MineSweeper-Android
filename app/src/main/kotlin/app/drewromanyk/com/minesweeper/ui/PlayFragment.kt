package app.drewromanyk.com.minesweeper.ui


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.adapters.PlayGameDifficultyAdapter
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.interfaces.PlayNavigator
import app.drewromanyk.com.minesweeper.interfaces.ProfileUiHandler
import app.drewromanyk.com.minesweeper.util.Helper
import app.drewromanyk.com.minesweeper.util.UserPrefStorage
import com.google.android.gms.common.images.ImageManager
import com.google.android.gms.games.Player
import kotlinx.android.synthetic.main.fragment_play.*
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AlertDialog
import app.drewromanyk.com.minesweeper.enums.ResultCodes
import app.drewromanyk.com.minesweeper.interfaces.MinesweeperHandler
import app.drewromanyk.com.minesweeper.models.Cell
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils
import app.drewromanyk.com.minesweeper.views.CircularOutlineProvider
import com.google.android.gms.common.images.ImageManager.OnImageLoadedListener



/**
 * A simple [Fragment] subclass.
 */
class PlayFragment : Fragment(), PlayNavigator, ProfileUiHandler {
    private lateinit var adapter: PlayGameDifficultyAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_play, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = (requireActivity() as MainActivity)
        activity.profileUiHandler = this
        sign_in.setOnClickListener {
            if (activity.isSignedIn()) {
                onSignIn(null)
                activity.startSignOutProcess()
            } else {
                activity.startSignInIntent()
            }
        }
        sign_in.clipToOutline = true
        sign_in.outlineProvider = CircularOutlineProvider
        setSignInButtons(activity.currentPlayer)
        setupPlayButtons()
    }

    private fun setupPlayButtons() {
        adapter = PlayGameDifficultyAdapter(this)
        playGameDifficultyRV.adapter = adapter
        playGameDifficultyRV.layoutManager = LinearLayoutManager(activity)
        updatePlaySelectButtons()
    }

    private fun updatePlaySelectButtons() {
        val gameDifficulties = ArrayList<GameDifficulty>()
        if (UserPrefStorage.hasResumeGame(requireActivity())) {
            gameDifficulties.add(GameDifficulty.RESUME)
        }
        gameDifficulties.add(GameDifficulty.CUSTOM)
        gameDifficulties.add(GameDifficulty.EASY)
        gameDifficulties.add(GameDifficulty.MEDIUM)
        gameDifficulties.add(GameDifficulty.EXPERT)

        adapter.setCanShowRating(UserPrefStorage.canShowRatingDialog(requireActivity()))
        adapter.setGameDifficultyList(gameDifficulties)
        adapter.notifyDataSetChanged()
    }

    override fun startGame(difficulty: GameDifficulty) {
        if (UserPrefStorage.hasResumeGame(requireContext()) && (difficulty !== GameDifficulty.RESUME)) {
            // A current game exists, ask if they want to delete
            val (title, description) = DialogInfoUtils.getInstance(requireActivity()).getDialogInfo(ResultCodes.RESUME_DIALOG.ordinal)
            val dialog = AlertDialog.Builder(requireActivity())
                    .setTitle(title)
                    .setMessage(description)
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        val data = UserPrefStorage.loadGame(requireContext(), object : MinesweeperHandler {
                            override fun isSwiftOpenEnabled(): Boolean = false

                            override fun onSwiftChange() {}

                            override fun onCellChange(cell: Cell, flagChange: Boolean) {}

                            override fun onGameStatusChange(cell: Cell) {}

                            override fun onTimerTick(gameTime: Long) {}
                        })
                        UserPrefStorage.updateStatsWithGame(requireContext(), data.gameDifficulty, data.minesweeper)
                        (requireActivity() as MainActivity).navigateToGame(difficulty)
                    }
                    .setNegativeButton(android.R.string.no) { _, _ -> }
                    .create()
            dialog.show()
        } else {
            (requireActivity() as MainActivity).navigateToGame(difficulty)
        }
    }

    override fun startPlayStore() {
        val uri = Uri.parse("market://details?id=" + requireActivity().packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market back stack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + requireActivity().packageName)))
        }

    }

    override fun sendFeedback() {
        Helper.sendFeedback(requireActivity())
    }

   override fun onSignIn(player: Player?) {
       setSignInButtons(player)
   }

    private fun setSignInButtons(player: Player?) {
        sign_in?.setImageResource(R.drawable.ic_account_circle)
        if (player != null) {
            ImageManager.create(requireContext()).loadImage(object : OnImageLoadedListener {
                override fun onImageLoaded(p0: Uri?, p1: Drawable?, p2: Boolean) {
                    sign_in?.setImageDrawable(p1)
                }
            }, player.iconImageUri, R.drawable.ic_account_circle)
        }
    }
}
