package app.drewromanyk.com.minesweeper.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.core.view.ViewCompat
import androidx.preference.PreferenceFragmentCompat
import app.drewromanyk.com.minesweeper.BuildConfig
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.util.PremiumUtils
import app.drewromanyk.com.minesweeper.util.UserPrefStorage


/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val list = view.findViewById<ListView>(android.R.id.list)
//        if (list != null) {
//            ViewCompat.setNestedScrollingEnabled(list, true)
//        }

        setupClearPurchase()
        setupPurchase()

        findPreference("reset_stats").setOnPreferenceClickListener {
            for (mode in GameDifficulty.EASY.ordinal..GameDifficulty.EXPERT.ordinal) {
                UserPrefStorage.updateStats(activity!!, GameDifficulty.values()[mode], 0, 0, 0, 0f, 0f, 0, 0, 0, 0, 0, 0f)
            }
            true
        }
    }

    private fun setupClearPurchase() {
        // For testing only
        val enableClearPurchase = true && BuildConfig.DEBUG

        if (enableClearPurchase) {
            val clearPurchase = findPreference("purchase_clear")
            clearPurchase.setOnPreferenceClickListener {
                PremiumUtils.instance.clear_purchase()
                true
            }

            clearPurchase.isEnabled = false
            if (PremiumUtils.instance.isPremiumUser) {
                clearPurchase.isEnabled = true
            }
        }
    }

    private fun setupPurchase() {
        val purchaseAdRemoval = findPreference("purchase_remove_ads")
        purchaseAdRemoval.setOnPreferenceClickListener {
            PremiumUtils.instance.purchase_premium(requireActivity())
            true
        }

        if (PremiumUtils.instance.isPremiumUser) {
            purchaseAdRemoval.isEnabled = false
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "ui_theme_mode" -> {
                requireActivity().startActivity(Intent(requireActivity(), MainActivity::class.java))
                requireActivity().finish()
            }
        }

    }
}
