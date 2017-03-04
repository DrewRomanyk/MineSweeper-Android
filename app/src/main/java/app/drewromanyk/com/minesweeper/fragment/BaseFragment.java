package app.drewromanyk.com.minesweeper.fragment;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import app.drewromanyk.com.minesweeper.R;

/**
 * Created by Drew on 9/11/15.
 * BaseFragment
 * Base Fragment to setup how child fragments should look like
 */
public class BaseFragment extends Fragment {

    protected void setupToolbar(Toolbar toolbar, String title) {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
