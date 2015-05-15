package app.drewromanyk.com.minesweeper.models;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.activities.MainActivity;
import app.drewromanyk.com.minesweeper.views.adapters.MyAdapter;
import app.drewromanyk.com.minesweeper.views.adapters.NavDrawerAdapter;

/**
 * Created by Drew on 4/18/2015.
 */
public class NavDrawerInfoTemp {

    public final static int DRAWER_DELAY = 200;
    private Activity activity;

    protected DrawerLayout drawerLayout;
    protected RecyclerView mRecyclerView;
    protected RecyclerView.Adapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    protected NavDrawerHeaderInfo headerInfo;

    public NavDrawerInfoTemp(Activity activity) {
        this.activity = activity;

        drawerLayout = (DrawerLayout) activity.findViewById(R.id.my_drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(activity.getResources().getColor(R.color.primary_dark));

        headerInfo = new NavDrawerHeaderInfo(activity);
        String[] navTitles = activity.getResources().getStringArray(R.array.nav_titles);
        int[] navIcons = {R.drawable.ic_nav_leaderboards,
                R.drawable.ic_nav_achievements,
                R.drawable.ic_nav_stats,
                R.drawable.ic_nav_settings,
                R.drawable.ic_nav_help,
                R.drawable.ic_nav_about};

        mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycleView);
        mAdapter = new NavDrawerAdapter(headerInfo, navTitles, navIcons);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    public RecyclerView getRecyclerView() { return mRecyclerView; }

    public NavDrawerHeaderInfo getHeaderInfo() { return headerInfo; }
}
