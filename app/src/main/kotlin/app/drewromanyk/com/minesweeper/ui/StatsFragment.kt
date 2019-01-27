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
import kotlinx.android.synthetic.main.fragment_stats.*

/**
 * A simple [Fragment] subclass.
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
            activity.startAchivementIntent()
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
}
