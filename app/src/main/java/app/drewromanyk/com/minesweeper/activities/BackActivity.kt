package app.drewromanyk.com.minesweeper.activities

import android.support.v7.widget.Toolbar

import app.drewromanyk.com.minesweeper.R

/**
 * Created by Drew on 11/6/15.
 * This is a Activity that handles setting the back action for the toolbar
 */

abstract class BackActivity : AdsActivity() {

    protected open fun setupToolbar(toolbar: Toolbar, title: String) {
        setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.title = title
        toolbar.setNavigationOnClickListener { finish() }
    }
}
