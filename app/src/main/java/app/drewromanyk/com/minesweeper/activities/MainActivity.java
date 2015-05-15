package app.drewromanyk.com.minesweeper.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.example.games.basegameutils.BaseGameUtils;

import org.json.JSONArray;

import app.drewromanyk.com.minesweeper.models.Board;
import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.enums.GameStatus;
import app.drewromanyk.com.minesweeper.enums.ImageDownloadType;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.network.ImageDownloader;


public class MainActivity extends BaseActivity {
    //CONSTANTS
    private static final String TAG = "taco";
    private final Context context = this;

    //VARIABLES
    private CardView resumeButton;
    //GOOGLE GAMES
    protected static GoogleApiClient googleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mSignInClicked = false;
    protected boolean mAutoStartSignInFlow = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAutoStartSignInFlow = true;
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        // Create the Google API Client with access to Plus and Games
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        baseGoogleApiClient = googleApiClient;

        //Get a Tracker (should auto-report)
        ((MyApplication) getApplication()).getTracker(MyApplication.TrackerName.APP_TRACKER);

        resumeButton = (CardView) findViewById(R.id.resumeGame);
    }

    @Override
    protected void onStart() {
        super.onStart();

        googleApiClient.connect();

        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
        GoogleAnalytics.getInstance(this).reportActivityStart(this);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int status = preferences.getInt("STATUS", GameStatus.DEFEAT.ordinal());

        if(status == GameStatus.PLAYING.ordinal()) {
            resumeButton.setVisibility(View.VISIBLE);
        } else {
            resumeButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        //Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void initToolbar() {
        super.initToolbar();
        if (getSupportActionBar() != null) {
            toolbar.setNavigationIcon(R.drawable.ic_drawer);
        }
    }

    @Override
    protected void initDrawer() {
        super.initDrawer();

        if (getSupportActionBar() != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navDrawerInfo.getDrawerLayout().openDrawer(Gravity.START);
                }
            });
        }
    }

    @Override
    protected void doNavDrawerActions(int position) {
        super.doNavDrawerActions(position);

        if(position == 0) {
            if (googleApiClient.isConnected()) {
                Games.signOut(googleApiClient);
                googleApiClient.disconnect();
                navDrawerInfo.getHeaderInfo().setPlayerToEmpty();
                navDrawerInfo.getRecyclerView().getAdapter().notifyItemChanged(0);
            } else {
                mSignInClicked = true;
                googleApiClient.connect();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ResultCodes.SIGN_IN.ordinal()) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                googleApiClient.connect();
            }
        }
    }

    @Override
    public void doPositiveClick(int REQUEST_CODE) {
        super.doPositiveClick(REQUEST_CODE);
        if (REQUEST_CODE == ResultCodes.NEEDGOOGLE_DIALOG.ordinal()) {
            mSignInClicked = true;
            googleApiClient.connect();
        }else if(REQUEST_CODE == ResultCodes.RESUME_DIALOG.ordinal()) {
            loadResumeGameForStats();
            startGameIntent();
        }
    }

    /*
     * GOOGLE GAME CONNECTION
     */

    @Override
    public void onConnected(Bundle bundle) {
        if ( isOnline() && Plus.PeopleApi.getCurrentPerson(googleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
            navDrawerInfo.getHeaderInfo().setName(currentPerson.getDisplayName());
            navDrawerInfo.getHeaderInfo().setEmail(Plus.AccountApi.getAccountName(googleApiClient));

            String playerAvatarURL = currentPerson.getImage().getUrl();
            int index = playerAvatarURL.indexOf("?sz=");
            if (index != -1) {
                playerAvatarURL = playerAvatarURL.substring(0, index) + "?sz=200";
            }
            new ImageDownloader(ImageDownloadType.AVATAR).execute(playerAvatarURL);
            if(currentPerson.getCover() != null && currentPerson.getCover().hasCoverPhoto()) {
                String playerCoverURL = currentPerson.getCover().getCoverPhoto().getUrl();
                new ImageDownloader(ImageDownloadType.COVER).execute(playerCoverURL);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // Already resolving
            return;
        }

        // If the sign in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    googleApiClient, connectionResult,
                    ResultCodes.SIGN_IN.ordinal(), getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }

        // Put code here to display the sign-in button

    }

    /*
         * DIFFICULTY BUTTONS CHOICES
         */
    public void resumeGame(View view){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int status = preferences.getInt("STATUS", GameStatus.DEFEAT.ordinal());

        if(status == GameStatus.PLAYING.ordinal()) {
            gameMode = GameDifficulty.RESUME;
            startGame();
        }
    }
    public void customGame(View view) {
        gameMode = GameDifficulty.CUSTOM;
        startGame();
    }
    public void easyGame(View view) {
        gameMode = GameDifficulty.EASY;
        startGame();
    }
    public void mediumGame(View view) {
        gameMode = GameDifficulty.MEDIUM;
        startGame();
    }
    public void expertGame(View view) {
        gameMode = GameDifficulty.EXPERT;
        startGame();
    }

    private void startGame() {
        if(resumeButton.getVisibility() == View.VISIBLE) {
            //A current game exists, ask if they want to delete
            if(gameMode == GameDifficulty.RESUME) {
                startGameIntent();
            } else {
                showYesNoDialog(ResultCodes.RESUME_DIALOG.ordinal());
            }
        } else {
            //no current game exists, create new game
            startGameIntent();
        }
    }

    private void startGameIntent() {
        Intent startGame = new Intent(this, GameActivity.class);
        startActivity(startGame);
    }

    private void loadResumeGameForStats() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int rows = preferences.getInt("ROWS", 0);
        int columns = preferences.getInt("COLUMNS", 0);

        if(!(rows == 0 || columns == 0)) {
            int difficulty = preferences.getInt("DIFFICULTY", 0);
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


                Board resumeStatBoard = new Board(mineCount, cellValues, cellRevealed, cellFlagged, status, difficulty, time, context);
                resumeStatBoard.updateLocalStatistics();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}