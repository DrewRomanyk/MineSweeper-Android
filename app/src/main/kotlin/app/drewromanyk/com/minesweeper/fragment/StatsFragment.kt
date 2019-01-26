package app.drewromanyk.com.minesweeper.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.adapters.StatsGameDifficultyAdapter
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.enums.ResultCodes
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils
import app.drewromanyk.com.minesweeper.util.Helper
import app.drewromanyk.com.minesweeper.util.UserPrefStorage

/**
 * Created by Drew on 9/11/15.
 * StatsFragment
 * Fragment that shows the User's local stats
 */
class StatsFragment : BaseFragment() {

    private lateinit var adapter: StatsGameDifficultyAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_stats, container, false) as ViewGroup

        setupToolbar(root.findViewById(R.id.toolbar), getString(R.string.nav_stats))
        setHasOptionsMenu(true)
        setupStatView(root)

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.menu_stats, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId

        when (id) {
            R.id.action_trash -> {
                val (title, description) = DialogInfoUtils.getInstance(activity!!).getDialogInfo(ResultCodes.TRASH_STATS_DIALOG.ordinal)
                val dialog = AlertDialog.Builder(activity!!)
                        .setTitle(title)
                        .setMessage(description)
                        .setPositiveButton(android.R.string.yes) { _, _ -> deleteLocalStats() }
                        .setNegativeButton(android.R.string.no) { _, _ -> }
                        .create()
                dialog.show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupStatView(root: ViewGroup) {
        val recyclerView = root.findViewById(R.id.statsRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = StatsGameDifficultyAdapter()
        recyclerView.adapter = adapter
    }

    private fun deleteLocalStats() {
        for (mode in GameDifficulty.EASY.ordinal..GameDifficulty.EXPERT.ordinal) {
            UserPrefStorage.updateStats(activity!!, GameDifficulty.values()[mode], 0, 0, 0, 0f, 0f, 0, 0, 0, 0, 0, 0f)
        }

        adapter.notifyDataSetChanged()
    }
}