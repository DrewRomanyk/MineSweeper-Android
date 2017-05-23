package app.drewromanyk.com.minesweeper.fragment

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment

import app.drewromanyk.com.minesweeper.BuildConfig
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.util.Helper
import app.drewromanyk.com.minesweeper.util.PremiumUtils
import app.drewromanyk.com.minesweeper.views.SeekBarPreference

/**
 * Created by Drew on 9/11/15.
 * SettingsFragment
 * Fragment that shows users preferences/configs/settings
 */
class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.pref_general)

        val mineSeek: SeekBarPreference = findPreference("mine_seek") as SeekBarPreference
        val rowSeek: SeekBarPreference = findPreference("row_seek") as SeekBarPreference
        val columnSeek: SeekBarPreference = findPreference("column_seek") as SeekBarPreference

        mineSeek.setMineSeek()
        rowSeek.setRowSeek(true)
        columnSeek.setColumnSeek(true)

        setup_purchase_pref()
        setup_clear_purchase_pref()

        val send_feedback = findPreference("send_feedback")
        send_feedback.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            Helper.sendFeedback(activity)
            true
        }
    }

    private fun setup_clear_purchase_pref() {
        // For testing only
        val enable_clear_purchase = false and BuildConfig.DEBUG

        if (enable_clear_purchase) {
            val clear_purchases = findPreference("purchase_clear")
            clear_purchases.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                PremiumUtils.instance.clear_purchase()
                true
            }

            clear_purchases.isEnabled = false
            if (PremiumUtils.instance.isPremiumUser) {
                clear_purchases.isEnabled = true
            }
        }
    }

    private fun setup_purchase_pref() {
        val in_app_ads = findPreference("purchase_remove_ads")
        in_app_ads.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            PremiumUtils.instance.purchase_premium(activity)
            true
        }

        if (PremiumUtils.instance.isPremiumUser) {
            in_app_ads.isEnabled = false
        }
    }
}
