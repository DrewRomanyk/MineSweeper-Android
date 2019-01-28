package app.drewromanyk.com.minesweeper.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.adapters.InfoTabPagerAdapter
import kotlinx.android.synthetic.main.fragment_info_tab_nav.*

/**
 * Fragment to handle navigation for the info tab layout screens
 */
class InfoTabFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_info_tab_nav, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        infoTabViewPager.adapter = InfoTabPagerAdapter(requireContext(), childFragmentManager)
        tabs.setupWithViewPager(infoTabViewPager)
    }
}
