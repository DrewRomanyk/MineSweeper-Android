package app.drewromanyk.com.minesweeper.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import app.drewromanyk.com.minesweeper.BuildConfig;
import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.activities.BaseActivity;
import app.drewromanyk.com.minesweeper.application.MinesweeperApp;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.views.SeekBarPreference;

/**
 * Created by Drew on 9/11/15.
 */
public class SettingsFragment extends PreferenceFragment {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);

        SeekBarPreference mineSeek;
        SeekBarPreference rowSeek;
        SeekBarPreference columnSeek;

        mineSeek = (SeekBarPreference) findPreference("mine_seek");
        mineSeek.setMineSeek();
        rowSeek = (SeekBarPreference) findPreference("row_seek");
        rowSeek.setRowSeek(true);
        columnSeek = (SeekBarPreference) findPreference("column_seek");
        columnSeek.setColumnSeek(true);

        Preference in_app_ads = findPreference("purchase_remove_ads");
        in_app_ads.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(((BaseActivity) getActivity()).mHelper != null) {
                    preference.setEnabled(false);
                    ((BaseActivity) getActivity()).mHelper.launchPurchaseFlow(
                            getActivity(),
                            BuildConfig.PREMIUM_SKU,
                            ResultCodes.IN_APP_PREMIUM.ordinal(),
                            ((BaseActivity) getActivity()).getPurchaseFinishedListener(preference),
                            BuildConfig.PREMIUM_SKU);
                }
                return true;
            }
        });

        Preference send_feedback = findPreference("send_feedback");
        send_feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                sendFeedback();
                return true;
            }
        });

        if(((MinesweeperApp) getActivity().getApplication()).getIsPremium() == 1) {
            in_app_ads.setEnabled(false);
//            clear_purchases.setEnabled(true);
        }
    }

    private void sendFeedback() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"drew.romanyk@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.settings_feedback_subject));
        i.putExtra(Intent.EXTRA_TEXT   , "");
        try {
            startActivity(Intent.createChooser(i, getString(R.string.settings_feedback_chooser)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), getString(R.string.settings_feedback_error), Toast.LENGTH_SHORT).show();
        }
    }
}
