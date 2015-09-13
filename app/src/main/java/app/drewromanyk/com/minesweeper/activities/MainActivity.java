package app.drewromanyk.com.minesweeper.activities;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.fragment.PlayFragment;
import app.drewromanyk.com.minesweeper.fragment.StatsFragment;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.models.YesNoDialogInfo;
import app.drewromanyk.com.minesweeper.util.BaseGameUtils;
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils;
import app.drewromanyk.com.minesweeper.util.Helper;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Drew on 1/10/2015.
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //ADS
    private AdView mAdView;
    //NAV DRAWER
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    // GOOGLE GAMES
    private GoogleApiClient googleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mSignInClicked = false;
    private boolean mAutoStartSignInFlow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupGoogleGames();
        setupTaskActivityInfo();
        setupDrawerContent((DrawerLayout) findViewById(R.id.drawer_layout), (NavigationView) findViewById(R.id.nav_view));
        setupFragmentContent(savedInstanceState);
        setupAds();
    }

    private void setupGoogleGames() {
        mAutoStartSignInFlow = true;

        // Create the Google API Client with access to Plus and Games
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    // Task for Recent Apps
    private void setupTaskActivityInfo() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // only for LOLLIPOP and newer versions
            ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_icon),
                    getResources().getColor(R.color.primary_task));
            setTaskDescription(tDesc);
        }
    }

    protected void setupDrawerContent(final DrawerLayout drawerLayout, NavigationView navigationView) {
        this.drawerLayout = drawerLayout;
        this.navView = navigationView;

        navView.findViewById(R.id.nav_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (googleApiClient.isConnected()) {
                    Games.signOut(googleApiClient);
                    googleApiClient.disconnect();

                    ((TextView) navView.findViewById(R.id.name)).setText(getString(R.string.nav_header_playername_empty));
                    Picasso.with(v.getContext()).load(R.drawable.person_image_empty).into((ImageView) navView.findViewById(R.id.avatar));
                    Picasso.with(v.getContext()).load(R.color.background_material_dark).into((ImageView) navView.findViewById(R.id.cover));

                } else {
                    mSignInClicked = true;
                    googleApiClient.connect();
                }
            }
        });

        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(final MenuItem menuItem) {
                        menuItem.setChecked(true);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        switch (menuItem.getItemId()) {
                            case R.id.nav_play:
                                transaction.replace(R.id.content_fragment, new PlayFragment());
                                transaction.commit();
                                break;
                            case R.id.nav_leaderboards:
                                if (googleApiClient.isConnected())
                                    startActivityForResult(Games.Leaderboards
                                            .getAllLeaderboardsIntent(googleApiClient), ResultCodes.LEADERBOARDS.ordinal());
                                else
                                    showYesNoDialog(ResultCodes.NEEDGOOGLE_DIALOG.ordinal());
                                break;
                            case R.id.nav_achievements:
                                if (googleApiClient.isConnected())
                                    startActivityForResult(Games.Achievements
                                            .getAchievementsIntent(googleApiClient), ResultCodes.ACHIEVEMENTS.ordinal());
                                else
                                    showYesNoDialog(ResultCodes.NEEDGOOGLE_DIALOG.ordinal());
                                break;
                            case R.id.nav_statistics:
                                transaction.replace(R.id.content_fragment, new StatsFragment());
                                transaction.commit();
                                break;
                            case R.id.nav_help:
                                showYesNoDialog(ResultCodes.HELP_DIALOG.ordinal());
                                break;
                            case R.id.nav_about:
                                showYesNoDialog(ResultCodes.ABOUT_DIALOG.ordinal());
                                break;
                            case R.id.nav_settings:
                                startActivity(new Intent(navView.getContext(), SettingsActivity.class));
                                break;
                        }
                        drawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    protected void setupFragmentContent(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_fragment, new PlayFragment())
                    .commit();
        }
    }

    // AdView on bottom of screen
    private void setupAds() {
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setAdListener(new AdListener() { // no overrides
        });
        mAdView.loadAd(new AdRequest.Builder()
                        .build()
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        mAdView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdView.resume();
    }

    @Override
    protected void onDestroy() {
        mAdView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    /*
     * DIALOGS
     */

    public void showYesNoDialog(final int requestCode) {
        YesNoDialogInfo dialogInfo = DialogInfoUtils.getInstance(this).getDialogInfo(requestCode);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(dialogInfo.getTitle())
                .setMessage(dialogInfo.getDescription())
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(requestCode == ResultCodes.NEEDGOOGLE_DIALOG.ordinal()) {
                            mSignInClicked = true;
                            googleApiClient.connect();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .create();
        dialog.show();
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



    /*
     * GOOGLE PLAY GAMES
     */

    @Override
    public void onConnected(Bundle bundle) {
        if ( Helper.isOnline(this) && Plus.PeopleApi.getCurrentPerson(googleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
            ((TextView) navView.findViewById(R.id.name)).setText(currentPerson.getDisplayName());

            String playerAvatarURL = currentPerson.getImage().getUrl();
            int index = playerAvatarURL.indexOf("?sz=");
            if (index != -1) {
                playerAvatarURL = playerAvatarURL.substring(0, index) + "?sz=200";
            }

            Picasso.with(this).load(playerAvatarURL).placeholder(R.drawable.person_image_empty).into(((CircleImageView) navView.findViewById(R.id.avatar)));
            if(currentPerson.getCover() != null && currentPerson.getCover().hasCoverPhoto()) {
                String playerCoverURL = currentPerson.getCover().getCoverPhoto().getUrl();
                Picasso.with(this).load(playerCoverURL).placeholder(R.color.background_material_dark).into(((ImageView) navView.findViewById(R.id.cover)));
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
