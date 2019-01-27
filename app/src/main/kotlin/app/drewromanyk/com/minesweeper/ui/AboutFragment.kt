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
import com.squareup.phrase.Phrase
import kotlinx.android.synthetic.main.fragment_about.*

/**
 * A simple [Fragment] subclass.
 */
class AboutFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        about_label.text = Phrase.from(context, R.string.dialog_about_message)
            .put(PhraseKeys.AMOUNT, BuildConfig.VERSION_NAME)
            .format()
            .toString()

        send_feedback_button.setOnClickListener {
            Helper.sendFeedback(requireActivity())
        }

        remove_ads.setOnClickListener {
            PremiumUtils.instance.purchase_premium(requireActivity())
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
                    .build()
                    .showDialog("Open source licenses")
        }
    }
}
