package app.drewromanyk.com.minesweeper.ui

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.interfaces.UpdateAdViewHandler
import app.drewromanyk.com.minesweeper.util.PremiumUtils
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.fragment_bottom_nav.*

/**
 * Fragment to host all of the main screens
 */
class BottomNavFragment : Fragment(), UpdateAdViewHandler {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottom_nav, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = Navigation.findNavController(requireActivity(), R.id.bottomNavFragment)
        bottomNavigation.setupWithNavController(navController)

        PremiumUtils.instance.updateContext(requireContext(), this)
        val handler = Handler()
        handler.postDelayed({
            adView?.loadAd(AdRequest.Builder().build())
            updateAdView()
        }, 1000)
    }

    override fun updateAdView() {
        if (PremiumUtils.instance.isPremiumUser) {
            adView?.pause()
            adView?.visibility = View.GONE
        } else {
            adView?.resume()
            adView?.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        adView?.pause()
    }

    override fun onResume() {
        super.onResume()
        updateAdView()
    }

    override fun onDestroy() {
        super.onDestroy()
        adView?.destroy()
        PremiumUtils.instance.releaseContext()
    }
}
