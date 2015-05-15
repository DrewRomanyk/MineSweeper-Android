package app.drewromanyk.com.minesweeper.models;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import app.drewromanyk.com.minesweeper.R;

/**
 * Created by Drew on 4/18/2015.
 */
public class NavDrawerInfo {

    public final static int DRAWER_DELAY = 200;
    private Activity activity;

    protected DrawerLayout drawerLayout;
    public static ImageView mCoverLayout;
    private static View mCoverOverlay;
    protected TextView mNameView;
    protected TextView mEmailView;
    public static ImageView mAvatarView;
    private TextView mLeaderboardsView;
    private TextView mAchievementsView;
    private TextView mStatsView;
    private TextView mSettingsView;
    private TextView mHelpView;
    private TextView mAboutView;

    public NavDrawerInfo(Activity activity) {
        this.activity = activity;

        drawerLayout = (DrawerLayout) activity.findViewById(R.id.my_drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(activity.getResources().getColor(R.color.primary_dark));

        // Player Views
        mCoverLayout = (ImageView) activity.findViewById(R.id.cover);
        mCoverOverlay = (View) activity.findViewById(R.id.coverOverlay);
        mNameView = (TextView) activity.findViewById(R.id.name);
        mEmailView = (TextView) activity.findViewById(R.id.email);
        mAvatarView = (ImageView) activity.findViewById(R.id.avatar);
        // Nav Buttons
        mLeaderboardsView = (TextView) activity.findViewById(R.id.leaderboards);
        mAchievementsView = (TextView) activity.findViewById(R.id.achievements);
        mStatsView = (TextView) activity.findViewById(R.id.stats);
        mSettingsView = (TextView) activity.findViewById(R.id.settings);
        mHelpView = (TextView) activity.findViewById(R.id.help);
        mAboutView = (TextView) activity.findViewById(R.id.about);

        setPlayerToEmpty();
    }

    // Set the drawer player set to default no player user
    public void setPlayerToEmpty() {
        mNameView.setText(activity.getString(R.string.nav_header_playername_empty));
        mEmailView.setText("");
        mAvatarView.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.person_image_empty));
        mCoverLayout.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.color.background_material_dark));
    }

    public static ImageView getCoverLayout() {
        return mCoverLayout;
    }

    public static View getCoverOverlay() {
        return mCoverOverlay;
    }

    public static ImageView getAvatarView() {
        return mAvatarView;
    }

    public TextView getLeaderboardsView() {
        return mLeaderboardsView;
    }

    public TextView getAchievementsView() {
        return mAchievementsView;
    }

    public TextView getStatsView() {
        return mStatsView;
    }

    public TextView getSettingsView() {
        return mSettingsView;
    }

    public TextView getHelpView() {
        return mHelpView;
    }

    public TextView getAboutView() {
        return mAboutView;
    }

    public TextView getNameView() {
        return mNameView;
    }

    public TextView getEmailView() {
        return mEmailView;
    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }
}
