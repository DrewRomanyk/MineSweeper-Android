package app.drewromanyk.com.minesweeper.fragment

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

import app.drewromanyk.com.minesweeper.R

/**
 * Created by Drew on 9/11/15.
 * BaseFragment
 * Base Fragment to setup how child fragments should look like
 */
open class BaseFragment : Fragment() {

    protected fun setupToolbar(toolbar: Toolbar, title: String) {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.title = title
            actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }
}
