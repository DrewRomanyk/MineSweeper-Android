package app.drewromanyk.com.minesweeper.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;

import app.drewromanyk.com.minesweeper.BuildConfig;
import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.activities.AdsActivity;
import app.drewromanyk.com.minesweeper.util.Helper;
import app.drewromanyk.com.minesweeper.util.PremiumUtils;
import app.drewromanyk.com.minesweeper.views.SeekBarPreference;

/**
 * Created by Drew on 9/11/15.
 * SettingsFragment
 * Fragment that shows users preferences/configs/settings
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

        setup_purchase_pref();
        setup_clear_purchase_pref();

        Preference send_feedback = findPreference("send_feedback");
        send_feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Helper.sendFeedback(getActivity());
                return true;
            }
        });
    }

    private void setup_clear_purchase_pref() {
        // For testing only
        final boolean enable_clear_purchase = false && BuildConfig.DEBUG;

        if (enable_clear_purchase) {
            Preference clear_purchases = findPreference("purchase_clear");
            clear_purchases.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    PremiumUtils.instance.clear_purchase();
                    return true;
                }
            });

            clear_purchases.setEnabled(false);
            if (PremiumUtils.instance.isPremiumUser()) {
                clear_purchases.setEnabled(true);
            }
        }
    }

    private void setup_purchase_pref() {
        Preference in_app_ads = findPreference("purchase_remove_ads");
        in_app_ads.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                PremiumUtils.instance.purchase_premium(getActivity());
                return true;
            }
        });

        if (PremiumUtils.instance.isPremiumUser()) {
            in_app_ads.setEnabled(false);
        }
    }
}
