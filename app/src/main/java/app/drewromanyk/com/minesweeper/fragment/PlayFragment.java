package app.drewromanyk.com.minesweeper.fragment;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;

import java.util.ArrayList;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.activities.GameActivity;
import app.drewromanyk.com.minesweeper.activities.SettingsActivity;
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
 * PlayFragment
 * Fragment Main home screen to allow a user to play a minesweeper game with their choice of
 * difficulty.
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
        Helper.screenViewOnGoogleAnalytics(getActivity(), "Play");
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
        if (hasResumeGame()) {
            gameDifficulties.add(GameDifficulty.RESUME);
        }
        gameDifficulties.add(GameDifficulty.CUSTOM);
        gameDifficulties.add(GameDifficulty.EASY);
        gameDifficulties.add(GameDifficulty.MEDIUM);
        gameDifficulties.add(GameDifficulty.EXPERT);

        adapter.setCanShowRating(UserPrefStorage.canShowRatingDialog(getActivity()));
        adapter.setGameDifficultyList(gameDifficulties);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void startGame(final GameDifficulty difficulty) {
        if (hasResumeGame() && difficulty != GameDifficulty.RESUME) {
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
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create();
            dialog.show();
        } else {
            if (difficulty == GameDifficulty.CUSTOM) {
                // Ask if they want to change their custom settings
                YesNoDialogInfo dialogInfo = DialogInfoUtils.getInstance(getActivity()).getDialogInfo(ResultCodes.CUSTOM_SETTING_CHANGE.ordinal());
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(dialogInfo.getTitle())
                        .setMessage(dialogInfo.getDescription())
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Change settings
                                startActivity(new Intent(getActivity(), SettingsActivity.class));
                            }
                        })
                        .setNegativeButton(R.string.nav_play, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // No current game exists, create new game
                                startGameIntent(difficulty);
                            }
                        })
                        .create();
                dialog.show();
            } else {
                // No current game exists, create new game
                startGameIntent(difficulty);
            }
        }
    }

    private void startGameIntent(GameDifficulty difficulty) {
        Intent startGame = new Intent(getActivity(), GameActivity.class);
        startGame.putExtra("gameDifficulty", difficulty.name());
        startActivity(startGame);
    }

    @Override
    public void startPlayStore() {
        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
        }
    }

    @Override
    public void sendFeedback() {
        Helper.sendFeedback(getActivity());
    }
}
