package app.drewromanyk.com.minesweeper.fragment

import android.content.Intent
import android.os.Bundle
import android.net.Uri
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
        val view_privacy_policy = findPreference("view_privacy_policy")
        view_privacy_policy.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://www.drewromanyk.com/minesweeper/privacy_policy")))
            true
        }
        val view_terms_and_conditions = findPreference("view_terms_and_conditions")
        view_terms_and_conditions.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://www.drewromanyk.com/minesweeper/terms_and_conditions")))
            true
        }
    }

    private fun setup_clear_purchase_pref() {
        // For testing only
        val enable_clear_purchase = false && BuildConfig.DEBUG

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
