package app.drewromanyk.com.minesweeper.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceFragmentCompat
import app.drewromanyk.com.minesweeper.BuildConfig
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.enums.ResultCodes
import app.drewromanyk.com.minesweeper.util.DialogInfoUtils
import app.drewromanyk.com.minesweeper.util.PremiumUtils
import app.drewromanyk.com.minesweeper.util.UserPrefStorage
import com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat


/**
 * Fragment to handle user preferences
 */
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (findPreference("longclick_duration_seek") as SeekBarPreferenceCompat).setDialogStyle(R.style.Widget_Minesweeper_Dialog)
        (findPreference("row_seek") as SeekBarPreferenceCompat).setDialogStyle(R.style.Widget_Minesweeper_Dialog)
        (findPreference("column_seek") as SeekBarPreferenceCompat).setDialogStyle(R.style.Widget_Minesweeper_Dialog)
        (findPreference("mine_seek") as SeekBarPreferenceCompat).setDialogStyle(R.style.Widget_Minesweeper_Dialog)

//        setupClearPurchase()
        setupPurchase()

        findPreference("reset_stats").setOnPreferenceClickListener {
            val (title, description) = DialogInfoUtils.getInstance(requireActivity()).getDialogInfo(ResultCodes.TRASH_STATS_DIALOG.ordinal)
            val dialog = AlertDialog.Builder(requireActivity())
                    .setTitle(title)
                    .setMessage(description)
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        for (mode in GameDifficulty.EASY.ordinal..GameDifficulty.EXPERT.ordinal) {
                            UserPrefStorage.updateStats(activity!!, GameDifficulty.values()[mode], 0, 0, 0, 0f, 0f, 0, 0, 0, 0, 0, 0f)
                        }
                    }
                    .setNegativeButton(android.R.string.no) { _, _ -> }
                    .create()
            dialog.show()

            true
        }
    }

    private fun setupClearPurchase() {
        // For testing only
        val enableClearPurchase = BuildConfig.DEBUG

        if (enableClearPurchase) {
            val clearPurchase = findPreference("purchase_clear")
            clearPurchase.setOnPreferenceClickListener {
                PremiumUtils.instance.clearPurchase()
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
            PremiumUtils.instance.purchasePremium(requireActivity())
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
