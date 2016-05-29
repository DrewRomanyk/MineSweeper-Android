package app.drewromanyk.com.minesweeper.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;

import org.json.JSONArray;

import java.util.ArrayList;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.activities.GameActivity;
import app.drewromanyk.com.minesweeper.activities.MainActivity;
import app.drewromanyk.com.minesweeper.adapters.PlayGameDifficultyAdapter;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.enums.GameStatus;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.interfaces.PlayNavigator;
import app.drewromanyk.com.minesweeper.models.Board;
import app.drewromanyk.com.minesweeper.models.YesNoDialogInfo;
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils;
import app.drewromanyk.com.minesweeper.util.Helper;
import app.drewromanyk.com.minesweeper.util.UserPrefStorage;

/**
 * Created by Drew on 9/11/15.
 */
public class PlayFragment extends BaseFragment implements PlayNavigator {

    private PlayGameDifficultyAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_play, container, false);

        setupToolbar((Toolbar) root.findViewById(R.id.toolbar), getString(R.string.nav_play));
        setupPlayButtons(root);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        updatePlaySelectButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        Helper.getGoogAnalyticsTracker(getActivity()).setScreenName("Screen~" + "Play");
        Helper.getGoogAnalyticsTracker(getActivity()).send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void setupPlayButtons(ViewGroup root) {
        adapter = new PlayGameDifficultyAdapter(this);
        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.playGameDifficultyRV);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updatePlaySelectButtons();
    }

    private boolean hasResumeGame() {
        return UserPrefStorage.getLastGameStatus(getActivity()) == GameStatus.PLAYING.ordinal()
                && UserPrefStorage.isCurrentSavedDataVersion(getActivity());
    }

    private void updatePlaySelectButtons() {
        ArrayList<GameDifficulty> gameDifficulties = new ArrayList<>();
        if(hasResumeGame()) {
            gameDifficulties.add(GameDifficulty.RESUME);
        }
        gameDifficulties.add(GameDifficulty.CUSTOM);
        gameDifficulties.add(GameDifficulty.EASY);
        gameDifficulties.add(GameDifficulty.MEDIUM);
        gameDifficulties.add(GameDifficulty.EXPERT);
        adapter.setGameDifficultyList(gameDifficulties);
    }

    public void startGame(final GameDifficulty difficulty) {
        if(hasResumeGame() &&  difficulty != GameDifficulty.RESUME) {
            // A current game exists, ask if they want to delete
            YesNoDialogInfo dialogInfo = DialogInfoUtils.getInstance(getActivity()).getDialogInfo(ResultCodes.RESUME_DIALOG.ordinal());
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(dialogInfo.getTitle())
                    .setMessage(dialogInfo.getDescription())
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Board statsBoard = UserPrefStorage.loadSavedBoard(getActivity(), true);
                            statsBoard.updateLocalStatistics(getActivity());
                            startGameIntent(difficulty);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    })
                    .create();
            dialog.show();
        } else {
            // No current game exists, create new game
            startGameIntent(difficulty);
        }
    }

    private void startGameIntent(GameDifficulty difficulty) {
        Intent startGame = new Intent(getActivity(), GameActivity.class);
        startGame.putExtra("gameDifficulty", difficulty.name());
        startActivity(startGame);
    }
}
