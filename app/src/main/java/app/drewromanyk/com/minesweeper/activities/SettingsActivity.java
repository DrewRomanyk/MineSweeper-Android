package app.drewromanyk.com.minesweeper.activities;

import android.app.ActivityManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.views.SeekBarPreference;


public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = "tacotaco";
    //ADS
    private AdView mAdView;

    private static SeekBarPreference mineSeek;
    private static SeekBarPreference rowSeek;
    private static SeekBarPreference columnSeek;
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get a Tracker (should auto-report)
        ((MyApplication) getApplication()).getTracker(MyApplication.TrackerName.APP_TRACKER);

        //Task for Recent Apps
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // only for LOLLIPOP and newer versions
            ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher),
                    getResources().getColor(R.color.primary_task));
            setTaskDescription(tDesc);
        }

        //Ads
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setAdListener(new AdListener() { // no overrides
        });
        mAdView.loadAd(new AdRequest.Builder()
                        .build()
        );

        setupSimplePreferencesScreen();
    }

    @Override
    public void setContentView(int layoutResID) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.activity_settings, new LinearLayout(this), false);

        toolbar = (Toolbar) contentView.findViewById(R.id.toolbar_setting);
        if(toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setTitle(R.string.title_activity_settings);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));

        ViewGroup contentWrapper = (ViewGroup) contentView.findViewById(R.id.content);
        LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true);

        getWindow().setContentView(contentView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    private void setupSimplePreferencesScreen() {


        //PreferenceCategory fakeHeader = new PreferenceCategory(this);
        //fakeHeader.setTitle("Custom Game Settings");
        //getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_general);

        findPreference("column_seek").setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        findPreference("row_seek").setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        mineSeek = (SeekBarPreference) findPreference("mine_seek");
        mineSeek.setMineSeek();
        rowSeek = (SeekBarPreference) findPreference("row_seek");
        rowSeek.setRowSeek(true);
        columnSeek = (SeekBarPreference) findPreference("column_seek");
        columnSeek.setColumnSeek(true);


        //bindPreferenceSummaryToValue(findPreference("vibration_check"));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.

                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof SwitchPreference) {
                //dont do anything
            } else if (preference instanceof SeekBarPreference) {
                /*Log.v("seek", rowSeek.getValue() + " " + columnSeek.getValue());
                Log.v("seek", "" + ((((rowSeek.getValue() + columnSeek.getValue()) - 18) * 8) + 64));
                if(preference == rowSeek || preference == columnSeek) {
                    mineSeek.setMaxValue(((((rowSeek.getValue() + columnSeek.getValue()) - 18) * 8) + 64));
                }*/
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
}
