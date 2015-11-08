package app.drewromanyk.com.minesweeper.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.ads.AdView;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.fragment.SettingsFragment;

/**
 * Created by Drew on 9/11/15.
 */
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar((Toolbar) findViewById(R.id.toolbar));
        setupAds((AdView) findViewById(R.id.adView));
        setupGoogleGames();

        getFragmentManager().beginTransaction()
                .replace(R.id.content, new SettingsFragment())
                .commit();
    }

    private void setupToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        getSupportActionBar().setTitle(R.string.title_activity_settings);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
