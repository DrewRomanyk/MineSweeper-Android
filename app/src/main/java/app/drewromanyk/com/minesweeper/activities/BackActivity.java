package app.drewromanyk.com.minesweeper.activities;

import android.support.v7.widget.Toolbar;
import android.view.View;

import app.drewromanyk.com.minesweeper.R;

/**
 * Created by Drew on 11/6/15.
 * This is a Activity that handles setting the back action for the toolbar
 */

public class BackActivity extends AdsActivity {

    protected void setupToolbar(Toolbar toolbar, String title) {
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
