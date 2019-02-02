package app.drewromanyk.com.minesweeper.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import app.drewromanyk.com.minesweeper.BuildConfig
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.util.Helper
import app.drewromanyk.com.minesweeper.util.PhraseKeys
import app.drewromanyk.com.minesweeper.util.PremiumUtils
import com.franmontiel.attributionpresenter.AttributionPresenter
import com.franmontiel.attributionpresenter.entities.Library
import com.franmontiel.attributionpresenter.entities.License
import com.franmontiel.attributionpresenter.entities.Attribution
import com.google.firebase.analytics.FirebaseAnalytics
import com.squareup.phrase.Phrase
import kotlinx.android.synthetic.main.fragment_about.*

/**
 * Fragment that tells the user the version & gives links to the privacy policy and other things
 */
class AboutFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        about_label.text = Phrase.from(context, R.string.about_label)
            .put(PhraseKeys.AMOUNT, BuildConfig.VERSION_NAME)
            .format()
            .toString()

        send_feedback_button.setOnClickListener {
            Helper.sendFeedback(requireActivity())
        }

        remove_ads.setOnClickListener {
            PremiumUtils.instance.purchasePremium(requireActivity())
        }

        terms_of_service_button.setOnClickListener {
            CustomTabsIntent.Builder().build().launchUrl(requireActivity(), Uri.parse("http://www.drewromanyk.com/minesweeper/terms_and_conditions"))
        }

        privacy_policy_button.setOnClickListener {
            CustomTabsIntent.Builder().build().launchUrl(requireActivity(), Uri.parse("http://www.drewromanyk.com/minesweeper/privacy_policy"))
        }

        open_source_licenses_button.setOnClickListener {
            AttributionPresenter.Builder(requireContext())
                    .addAttributions(
                        Library.GSON
                    )
                    .addAttributions(
                        Attribution.Builder("Phrase")
                            .addCopyrightNotice("Copyright 2013 Square, Inc.")
                            .addLicense(License.APACHE)
                            .setWebsite("https://github.com/square/phrase")
                            .build(),
                        Attribution.Builder("ZoomLayout")
                            .addCopyrightNotice("Copyright 2017 Otalia Studios")
                            .addLicense(License.APACHE)
                            .setWebsite("https://github.com/natario1/ZoomLayout")
                            .build(),
                        Attribution.Builder("AttributionPresenter")
                            .addCopyrightNotice("Copyright 2017 Francisco José Montiel Navarro")
                            .addLicense(License.APACHE)
                            .setWebsite("https://github.com/franmontiel/AttributionPresenter")
                            .build(),
                        Attribution.Builder("MaterialSeekBarPreference")
                            .addCopyrightNotice("Copyright 2015 Pavel Sikun")
                            .addLicense(License.APACHE)
                            .setWebsite("https://github.com/MrBIMC/MaterialSeekBarPreference")
                            .build(),
                        Attribution.Builder("Phrase")
                            .addCopyrightNotice("Copyright 2013 Square, Inc.")
                            .addLicense(License.APACHE)
                            .setWebsite("https://github.com/square/phrase")
                            .build()
                    )
                    .build()
                    .showDialog(getString(R.string.open_source_licenses))
        }
    }

    override fun onResume() {
        super.onResume()
        val firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        firebaseAnalytics.setCurrentScreen(requireActivity(), "AboutFragment", "AboutFragment")
    }
}
