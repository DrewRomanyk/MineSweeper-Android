package app.drewromanyk.com.minesweeper.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;

import org.json.JSONArray;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.activities.GameActivity;
import app.drewromanyk.com.minesweeper.activities.MainActivity;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.enums.GameStatus;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.models.Board;
import app.drewromanyk.com.minesweeper.models.YesNoDialogInfo;
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils;
import app.drewromanyk.com.minesweeper.util.Helper;
import app.drewromanyk.com.minesweeper.util.UserPrefStorage;

/**
 * Created by Drew on 9/11/15.
 */
public class PlayFragment extends BaseFragment {

    private View resumeGame;
    private View customGame;
    private View easyGame;
    private View mediumGame;
    private View expertGame;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_play, container, false);

        setupToolbar((Toolbar) root.findViewById(R.id.toolbar), "Play");
        setupPlayButtons(root);


        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        showResumeButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        Helper.getGoogAnalyticsTracker(getActivity()).setScreenName("Screen~" + "Play");
        Helper.getGoogAnalyticsTracker(getActivity()).send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void setupPlayButtons(ViewGroup root) {
        resumeGame = root.findViewById(R.id.resumeGame);
        customGame = root.findViewById(R.id.customGame);
        easyGame = root.findViewById(R.id.easyGame);
        mediumGame = root.findViewById(R.id.mediumGame);
        expertGame = root.findViewById(R.id.expertGame);

        resumeGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserPrefStorage.getLastGameStatus(getActivity()) == GameStatus.PLAYING.ordinal()) {
                    startGame(GameDifficulty.RESUME);
                }
            }
        });
        customGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(GameDifficulty.CUSTOM);
            }
        });
        easyGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(GameDifficulty.EASY);
            }
        });
        mediumGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(GameDifficulty.MEDIUM);
            }
        });
        expertGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(GameDifficulty.EXPERT);
            }
        });
    }

    private void showResumeButton() {
        if(UserPrefStorage.getLastGameStatus(getActivity()) == GameStatus.PLAYING.ordinal()) {
            resumeGame.setVisibility(View.VISIBLE);
        } else {
            resumeGame.setVisibility(View.GONE);
        }
    }

    private void startGame(GameDifficulty difficulty) {
        if(resumeGame.getVisibility() == View.VISIBLE) {
            //A current game exists, ask if they want to delete
            if(difficulty == GameDifficulty.RESUME) {
                startGameIntent(difficulty);
            } else {
                final GameDifficulty savedDifficulity = difficulty;

                YesNoDialogInfo dialogInfo = DialogInfoUtils.getInstance(getActivity()).getDialogInfo(ResultCodes.RESUME_DIALOG.ordinal());
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(dialogInfo.getTitle())
                        .setMessage(dialogInfo.getDescription())
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadResumeGameForStats();
                                startGameIntent(savedDifficulity);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        })
                        .create();
                dialog.show();
            }
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

    private void loadResumeGameForStats() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int rows = preferences.getInt("ROWS", 0);
        int columns = preferences.getInt("COLUMNS", 0);

        if(!(rows == 0 || columns == 0)) {
            GameDifficulty gameDifficulty = GameDifficulty.values()[preferences.getInt("DIFFICULTY", GameDifficulty.CUSTOM.ordinal())];
            long time = preferences.getLong("TIME", 0);
            int mineCount = preferences.getInt("MINE_COUNT", 0);
            GameStatus status = GameStatus.values()[preferences.getInt("STATUS", GameStatus.DEFEAT.ordinal())];
            int[][] cellValues = new int[rows][columns];
            boolean[][] cellRevealed = new boolean[rows][columns];
            boolean[][] cellFlagged = new boolean[rows][columns];

            try {
                JSONArray cellValuesJ = new JSONArray(preferences.getString("CELL_VALUES", "[]"));
                JSONArray cellRevealedJ = new JSONArray(preferences.getString("CELL_REVEALED", "[]"));
                JSONArray cellFlaggedJ = new JSONArray(preferences.getString("CELL_FLAGGED", "[]"));

                int counter = 0;
                for(int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        cellValues[r][c] = cellValuesJ.getInt(counter);
                        cellRevealed[r][c] = cellRevealedJ.getBoolean(counter);
                        cellFlagged[r][c] = cellFlaggedJ.getBoolean(counter);
                        counter++;
                    }
                }


                Board resumeStatBoard = new Board(mineCount, cellValues, cellRevealed, cellFlagged, status, gameDifficulty, time);
                resumeStatBoard.updateLocalStatistics(getActivity());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
