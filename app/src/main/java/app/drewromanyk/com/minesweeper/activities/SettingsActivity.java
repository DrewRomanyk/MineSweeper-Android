package app.drewromanyk.com.minesweeper.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.fragment.SettingsFragment;

/**
 * Created by Drew on 9/11/15.
 * SettingsActivity
 * Activity for users to change preferences
 */
public class SettingsActivity extends BackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupAds();

        setupToolbar((Toolbar) findViewById(R.id.toolbar), getString(R.string.nav_settings));

        getFragmentManager().beginTransaction()
                .replace(R.id.content, new SettingsFragment())
                .commit();
    }
}
