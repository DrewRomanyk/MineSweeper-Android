package app.drewromanyk.com.minesweeper.fragment;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.models.YesNoDialogInfo;
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils;
import app.drewromanyk.com.minesweeper.util.Helper;
import app.drewromanyk.com.minesweeper.util.UserPrefStorage;

/**
 * Created by Drew on 9/11/15.
 */
public class StatsFragment extends BaseFragment {

    private TextView[] titleTextView = new TextView[4];
    private TextView[] contentTextView = new TextView[4];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_stats, container, false);

        setupToolbar((Toolbar) root.findViewById(R.id.toolbar), "Statistics");
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
            case R.id.action_trash :
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
                            public void onClick(DialogInterface dialog, int which) {}
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
        Helper.getGoogAnalyticsTracker(getActivity()).setScreenName("Screen~" + "Stats");
        Helper.getGoogAnalyticsTracker(getActivity()).send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void setupStatView(ViewGroup root) {
        titleTextView[1] = (TextView) root.findViewById(R.id.easyStatsTitle);
        titleTextView[2] = (TextView) root.findViewById(R.id.mediumStatsTitle);
        titleTextView[3] = (TextView) root.findViewById(R.id.expertStatsTitle);
        contentTextView[1] = (TextView) root.findViewById(R.id.easyStatsContent);
        contentTextView[2] = (TextView) root.findViewById(R.id.mediumStatsContent);
        contentTextView[3] = (TextView) root.findViewById(R.id.expertStatsContent);

        updateStatTextViews();
    }

    private void updateStatTextViews() {
        for(int mode = GameDifficulty.EASY.ordinal() - 1; mode <= GameDifficulty.EXPERT.ordinal() - 1; mode++) {
            TextView modeText = contentTextView[mode];
            TextView modeTitle = titleTextView[mode];
            GameDifficulty difficulty = GameDifficulty.values()[mode + 1];

            String title = "";
            switch (difficulty) {
                case EASY :
                    title = "Easy";
                    break;
                case MEDIUM :
                    title = "Medium";
                    break;
                case EXPERT :
                    title = "Expert";
                    break;
            }

            // Get data
            int wins = UserPrefStorage.getWinsForDifficulty(getActivity(), difficulty);
            int loses = UserPrefStorage.getLosesForDifficulty(getActivity(), difficulty);
            int bestTime = UserPrefStorage.getBestTimeForDifficulty(getActivity(), difficulty);
            float avgTime = UserPrefStorage.getAvgTimeForDifficulty(getActivity(), difficulty);
            float explorPerct = UserPrefStorage.getExplorPercentForDifficulty(getActivity(), difficulty);
            int winStreak = UserPrefStorage.getWinStreakForDifficulty(getActivity(), difficulty);
            int losesStreak = UserPrefStorage.getLoseStreakForDifficulty(getActivity(), difficulty);
            int currentWinStreak = UserPrefStorage.getCurWinStreakForDifficulty(getActivity(), difficulty);
            int currentLosesStreak = UserPrefStorage.getCurLoseStreakForDifficulty(getActivity(), difficulty);
            int bestScore = UserPrefStorage.getBestScoreForDifficulty(getActivity(), difficulty);
            float avgScore = UserPrefStorage.getAvgScoreForDifficulty(getActivity(), difficulty);

            int totalGames = wins + loses;

            // Show data
            modeTitle.setText(title);
            modeText.setText(
                    "Best score: " + ((double) bestScore/1000) + "\nAverage score: " + ((double) avgScore/1000) +
                            "\nBest time: " + bestTime + "\nAverage time: " + avgTime +
                            "\nGames won: " + wins + "\nGames played: " + totalGames +
                            "\nWin percentage: " + ((totalGames != 0) ? ((((double) wins/totalGames)) * 100) : 0) + "%" +
                            "\nExploration percentage: " + explorPerct + "%" +
                            "\nLongest winning streak: " + winStreak +
                            "\nLongest losing streak: " + losesStreak +
                            "\nCurrent streak: " + ((currentWinStreak == 0) ? currentLosesStreak : currentWinStreak) +
                            "\n");
        }
    }

    private void deleteLocalStats() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();

        for(int mode = GameDifficulty.EASY.ordinal(); mode <= GameDifficulty.EXPERT.ordinal(); mode++) {
            //prefix
            String prefix = "";
            switch (GameDifficulty.values()[mode]) {
                case EASY :
                    prefix = "EASY_";
                    break;
                case MEDIUM :
                    prefix = "MEDIUM_";
                    break;
                case EXPERT :
                    prefix = "EXPERT_";
                    break;
            }
            //get data
            String winsKey = prefix + "WINS";
            String losesKey = prefix + "LOSES";
            String bestTimeKey = prefix + "BEST_TIME";
            String avgTimeKey = prefix + "AVG_TIME";
            String explorPerctKey = prefix + "EXPLOR_PERCT";
            String winStreakKey = prefix + "WIN_STREAK";
            String losesStreakKey = prefix + "LOSES_STREAK";
            String currentWinStreakKey = prefix + "CURRENTWIN_STREAK";
            String currentLosesStreakKey = prefix + "CURRENTLOSES_STREAK";
            String bestScoreKey = prefix + "BEST_SCORE";
            String avgScoreKey = prefix + "AVG_SCORE";

            //wipe data
            editor.putInt(winsKey, 0);
            editor.putInt(losesKey, 0);
            editor.putInt(bestTimeKey, 0);
            editor.putFloat(avgTimeKey, 0);
            editor.putFloat(explorPerctKey, 0);
            editor.putInt(winStreakKey, 0);
            editor.putInt(losesStreakKey, 0);
            editor.putInt(currentWinStreakKey, 0);
            editor.putInt(currentLosesStreakKey, 0);
            editor.putInt(bestScoreKey, 0);
            editor.putFloat(avgScoreKey, 0);
            editor.commit();
        }

        updateStatTextViews();
    }

}
