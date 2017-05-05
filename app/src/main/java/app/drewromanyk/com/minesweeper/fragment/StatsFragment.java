package app.drewromanyk.com.minesweeper.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;

import java.util.ArrayList;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.adapters.StatsGameDifficultyAdapter;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.models.YesNoDialogInfo;
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils;
import app.drewromanyk.com.minesweeper.util.Helper;
import app.drewromanyk.com.minesweeper.util.UserPrefStorage;

/**
 * Created by Drew on 9/11/15.
 * StatsFragment
 * Fragment that shows the User's local stats
 */
public class StatsFragment extends BaseFragment {

    StatsGameDifficultyAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_stats, container, false);

        setupToolbar((Toolbar) root.findViewById(R.id.toolbar), getString(R.string.nav_stats));
        setHasOptionsMenu(true);
        setupStatView(root);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_stats, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_trash:
                YesNoDialogInfo dialogInfo = DialogInfoUtils.getInstance(getActivity()).getDialogInfo(ResultCodes.TRASH_STATS_DIALOG.ordinal());
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(dialogInfo.getTitle())
                        .setMessage(dialogInfo.getDescription())
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteLocalStats();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create();
                dialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Helper.screenViewOnGoogleAnalytics(getActivity(), "Stats");
    }

    private void setupStatView(ViewGroup root) {
        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.statsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StatsGameDifficultyAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void deleteLocalStats() {
        for (int mode = GameDifficulty.EASY.ordinal(); mode <= GameDifficulty.EXPERT.ordinal(); mode++) {
            UserPrefStorage.updateStats(getActivity(), GameDifficulty.values()[mode], 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        adapter.notifyDataSetChanged();
    }
}