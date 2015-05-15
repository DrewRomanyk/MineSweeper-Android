package app.drewromanyk.com.minesweeper.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.example.games.basegameutils.BaseGameUtils;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.enums.GameDifficulty;
import app.drewromanyk.com.minesweeper.enums.ImageDownloadType;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.network.ImageDownloader;

/**
 * Created by Drew on 1/19/2015.
 */
public class StatsActivity extends BaseActivity {

    //VARIABLES
    private TextView[] titleTextView = new TextView[4];
    private TextView[] contentTextView = new TextView[4];

    //GOOGLE GAMES
    protected static GoogleApiClient googleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mSignInClicked = false;
    protected boolean mAutoStartSignInFlow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_stats);
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

        //show stats
        titleTextView[1] = (TextView) findViewById(R.id.easyStatsTitle);
        titleTextView[2] = (TextView) findViewById(R.id.mediumStatsTitle);
        titleTextView[3] = (TextView) findViewById(R.id.expertStatsTitle);
        contentTextView[1] = (TextView) findViewById(R.id.easyStatsContent);
        contentTextView[2] = (TextView) findViewById(R.id.mediumStatsContent);
        contentTextView[3] = (TextView) findViewById(R.id.expertStatsContent);
        updateStatTextViews();
    }

    private void updateStatTextViews() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        for(int mode = GameDifficulty.EASY.ordinal() - 1; mode <= GameDifficulty.EXPERT.ordinal() - 1; mode++) {
            //prefix
            String prefix = "";
            String title = "";
            TextView modeText = contentTextView[mode];
            TextView modeTitle = titleTextView[mode];
            switch (GameDifficulty.values()[mode + 1]) {
                case EASY :
                    title = "Easy Mode: ";
                    break;
                case MEDIUM :
                    title = "Medium Mode: ";
                    prefix = "MEDIUM_";
                    break;
                case EXPERT :
                    title = "Expert Mode: ";
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

            //initial data
            int wins = sharedPrefs.getInt(winsKey, 0);
            int loses = sharedPrefs.getInt(losesKey, 0);
            int bestTime = sharedPrefs.getInt(bestTimeKey, 0);
            float avgTime = sharedPrefs.getFloat(avgTimeKey, 0);
            float explorPerct = sharedPrefs.getFloat(explorPerctKey, 0);
            int winStreak = sharedPrefs.getInt(winStreakKey, 0);
            int losesStreak = sharedPrefs.getInt(losesStreakKey, 0);
            int currentWinStreak = sharedPrefs.getInt(currentWinStreakKey, 0);
            int currentLosesStreak = sharedPrefs.getInt(currentLosesStreakKey, 0);
            int bestScore = sharedPrefs.getInt(bestScoreKey, 0);
            float avgScore = sharedPrefs.getFloat(avgScoreKey, 0);

            int totalGames = wins+loses;
            //show data
            modeTitle.setText(title);
            modeText.setText(
                    "Best score: " + ((double) bestScore/1000) + "\nAverage score: " + ((double) avgScore/1000) +
                    "\nBest time: " + bestTime + "\nAverage time: " + avgTime +
                    "\nGames won: " + wins + "\nGames played: " + totalGames +
                    "\nWin percentage: " + ((totalGames != 0) ? (((double) (wins/totalGames)) * 100) : 0) + "%" +
                    "\nExploration percentage: " + explorPerct + "%" +
                    "\nLongest winning streak: " + winStreak +
                    "\nLongest losing streak: " + losesStreak +
                    "\nCurrent streak: " + ((currentWinStreak == 0) ? currentLosesStreak : currentWinStreak) +
                    "\n");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        googleApiClient.connect();

        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stats, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_trash :
                showYesNoDialog(ResultCodes.TRASH_STATS_DIALOG.ordinal());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initToolbar() {
        super.initToolbar();

        if (toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        toolbar.inflateMenu(R.menu.menu_stats);
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
    public void doPositiveClick(int REQUEST_CODE) {
        super.doPositiveClick(REQUEST_CODE);
        if (REQUEST_CODE == ResultCodes.NEEDGOOGLE_DIALOG.ordinal()) {
            mSignInClicked = true;
            googleApiClient.connect();
        } else if(REQUEST_CODE == ResultCodes.TRASH_STATS_DIALOG.ordinal()) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
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

    @Override
    public void onConnected(Bundle bundle) {
        if ( isOnline() && Plus.PeopleApi.getCurrentPerson(googleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
            navDrawerInfo.getHeaderInfo().setName(currentPerson.getDisplayName());
            navDrawerInfo.getHeaderInfo().setEmail(Plus.AccountApi.getAccountName(googleApiClient));
            navDrawerInfo.getRecyclerView().getAdapter().notifyItemChanged(0);

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
}
