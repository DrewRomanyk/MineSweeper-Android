package app.drewromanyk.com.minesweeper.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;

import app.drewromanyk.com.minesweeper.BuildConfig;
import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.activities.BaseActivity;
import app.drewromanyk.com.minesweeper.application.MinesweeperApp;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.util.Helper;
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
                BillingProcessor bp = ((BaseActivity) getActivity()).bp;
                if (bp != null) {
                    boolean isOneTimePurchaseSupported = bp.isOneTimePurchaseSupported();
                    if (isOneTimePurchaseSupported) {
                        Log.i("SettingsFrag", "onPreferenceClick: purchase");
                        bp.purchase(getActivity(), BuildConfig.PREMIUM_SKU);
                    }
                }
                return true;
            }
        });

//        Preference clear_purchases = findPreference("purchase_clear");
//        clear_purchases.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                BillingProcessor bp = ((BaseActivity) getActivity()).bp;
//                if (bp != null) {
//                    boolean isOneTimePurchaseSupported = bp.isOneTimePurchaseSupported();
//                    if (isOneTimePurchaseSupported) {
//                        Log.i("SettingsFrag", "onPreferenceClick: consume");
//                        bp.consumePurchase(BuildConfig.PREMIUM_SKU);
//                    }
//                }
//                return true;
//            }
//        });

        Preference send_feedback = findPreference("send_feedback");
        send_feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Helper.sendFeedback(getActivity());
                return true;
            }
        });

        if (((MinesweeperApp) getActivity().getApplication()).getIsPremium() == 1) {
            in_app_ads.setEnabled(false);
//            clear_purchases.setEnabled(true);
        }
    }
}
