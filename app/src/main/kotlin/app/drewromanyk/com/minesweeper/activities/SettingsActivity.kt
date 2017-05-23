package app.drewromanyk.com.minesweeper.activities

import android.os.Bundle
import android.support.v7.widget.Toolbar

import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.fragment.SettingsFragment

/**
 * Created by Drew on 9/11/15.
 * SettingsActivity
 * Activity for users to change preferences
 */
class SettingsActivity : BackActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupAds()

        setupToolbar(findViewById(R.id.toolbar) as Toolbar, getString(R.string.nav_settings))

        fragmentManager.beginTransaction()
                .replace(R.id.content, SettingsFragment())
                .commit()
    }
}
