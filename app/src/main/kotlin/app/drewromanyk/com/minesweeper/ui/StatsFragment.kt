package app.drewromanyk.com.minesweeper.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.adapters.StatsGameDifficultyAdapter
import app.drewromanyk.com.minesweeper.views.CircularOutlineProvider
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.fragment_stats.*

/**
 * Fragment to show statistics on game play
 */
class StatsFragment : Fragment() {
    private lateinit var adapter: StatsGameDifficultyAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        achievements.setOnClickListener {
            activity.startAchievementIntent()
        }
        leaderboards.setOnClickListener {
            activity.startLeaderboardIntent()
        }
        achievements.clipToOutline = true
        achievements.outlineProvider = CircularOutlineProvider
        leaderboards.clipToOutline = true
        leaderboards.outlineProvider = CircularOutlineProvider

        statsRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = StatsGameDifficultyAdapter()
        statsRecyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        val firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        firebaseAnalytics.setCurrentScreen(requireActivity(), "StatsFragment", "StatsFragment")
    }
}
