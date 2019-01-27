package app.drewromanyk.com.minesweeper.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.ui.AboutFragment
import app.drewromanyk.com.minesweeper.ui.HelpFragment
import app.drewromanyk.com.minesweeper.ui.SettingsFragment


class InfoTabPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> AboutFragment()
            1 -> HelpFragment()
            2 -> SettingsFragment()
            else -> throw Exception()
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.getString(R.string.nav_about)
            1 -> context.getString(R.string.nav_help)
            2 -> context.getString(R.string.nav_settings)
            else -> null
        }
    }

}