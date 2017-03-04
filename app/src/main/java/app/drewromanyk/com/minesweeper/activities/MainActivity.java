package app.drewromanyk.com.minesweeper.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.fragment.PlayFragment;
import app.drewromanyk.com.minesweeper.fragment.StatsFragment;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.models.YesNoDialogInfo;
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils;
import app.drewromanyk.com.minesweeper.util.Helper;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Drew on 1/10/2015.
 * MainActivity
 * Main view for users to select a new game or play an old one
 */

public class MainActivity extends BaseActivity {
    //NAV DRAWER
    private DrawerLayout drawerLayout;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupDrawerContent((DrawerLayout) findViewById(R.id.drawer_layout), (NavigationView) findViewById(R.id.nav_view));
        setupFragmentContent(savedInstanceState);
        setupAds((AdView) findViewById(R.id.adView));
        setupGoogleGames();
    }

    protected void setupDrawerContent(final DrawerLayout drawerLayout, final NavigationView navView) {
        this.drawerLayout = drawerLayout;
        this.navView = navView;
        View headerLayout = navView.getHeaderView(0);

        headerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getGoogleApiClient().isConnected()) {
                    Games.signOut(getGoogleApiClient());
                    getGoogleApiClient().disconnect();

                    ((TextView) navView.findViewById(R.id.name)).setText(getString(R.string.nav_header_playername_empty));
                    Picasso.with(v.getContext()).load(R.drawable.common_google_signin_btn_icon_dark).into((ImageView) navView.findViewById(R.id.avatar));
                    Picasso.with(v.getContext()).load(R.color.nav_drawer_header_background).into((ImageView) navView.findViewById(R.id.cover));
                } else {
                    setSignInClicked(true);
                    getGoogleApiClient().connect();
                }
            }
        });

        navView.getMenu().getItem(0).setChecked(true);
        navView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
                        menuItem.setChecked(true);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        switch (menuItem.getItemId()) {
                            case R.id.nav_play:
                                transaction.replace(R.id.content_fragment, new PlayFragment());
                                transaction.commit();
                                break;
                            case R.id.nav_leaderboards:
                                if (getGoogleApiClient().isConnected())
                                    startActivityForResult(Games.Leaderboards
                                            .getAllLeaderboardsIntent(getGoogleApiClient()), ResultCodes.LEADERBOARDS.ordinal());
                                else
                                    showYesNoDialog(ResultCodes.NEEDGOOGLE_DIALOG.ordinal());
                                break;
                            case R.id.nav_achievements:
                                if (getGoogleApiClient().isConnected())
                                    startActivityForResult(Games.Achievements
                                            .getAchievementsIntent(getGoogleApiClient()), ResultCodes.ACHIEVEMENTS.ordinal());
                                else
                                    showYesNoDialog(ResultCodes.NEEDGOOGLE_DIALOG.ordinal());
                                break;
                            case R.id.nav_statistics:
                                transaction.replace(R.id.content_fragment, new StatsFragment());
                                transaction.commit();
                                break;
                            case R.id.nav_help:
                                menuItem.setChecked(false);
                                showYesNoDialog(ResultCodes.HELP_DIALOG.ordinal());
                                break;
                            case R.id.nav_about:
                                menuItem.setChecked(false);
                                showYesNoDialog(ResultCodes.ABOUT_DIALOG.ordinal());
                                break;
                            case R.id.nav_settings:
                                menuItem.setChecked(false);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
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
                        if (requestCode == ResultCodes.NEEDGOOGLE_DIALOG.ordinal()) {
                            setSignInClicked(true);
                            getGoogleApiClient().connect();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        dialog.show();
    }

    /*
     * GOOGLE PLAY GAMES
     */

    @Override
    public void onConnected(Bundle bundle) {
        if (Helper.isOnline(this) && Plus.PeopleApi.getCurrentPerson(getGoogleApiClient()) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(getGoogleApiClient());
            TextView nameDisplay = (TextView) navView.findViewById(R.id.name);
            CircleImageView avatar = (CircleImageView) navView.findViewById(R.id.avatar);
            ImageView cover = (ImageView) navView.findViewById(R.id.cover);

            if (nameDisplay != null) {
                nameDisplay.setText(currentPerson.getDisplayName());
            }

            String playerAvatarURL = currentPerson.getImage().getUrl();
            int index = playerAvatarURL.indexOf("?sz=");
            if (index != -1) {
                playerAvatarURL = playerAvatarURL.substring(0, index) + "?sz=200";
            }

            if (avatar != null) {
                Picasso.with(this).load(playerAvatarURL).placeholder(R.drawable.person_image_empty).into(avatar);
            }

            if (cover != null && currentPerson.getCover() != null && currentPerson.getCover().hasCoverPhoto()) {
                String playerCoverURL = currentPerson.getCover().getCoverPhoto().getUrl();
                Picasso.with(this).load(playerCoverURL).placeholder(R.color.nav_drawer_header_background).into(cover);
            }
        }
    }
}
