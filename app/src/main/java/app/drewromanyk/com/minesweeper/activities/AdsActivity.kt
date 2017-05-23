package app.drewromanyk.com.minesweeper.activities

import android.content.Intent
import android.os.Handler
import android.view.View

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.interfaces.UpdateAdViewHandler
import app.drewromanyk.com.minesweeper.util.PremiumUtils

/**
 * Created by Drew on 11/6/15.
 * This is a Activity that handles Ads and In-app purchases through PremiumUtils
 */
abstract class AdsActivity : GameServicesActivity(), UpdateAdViewHandler {
    // ADS
    private var adView: AdView? = null

    protected fun setupAds() {
        PremiumUtils.instance.updateContext(this, this)
        this.adView = findViewById(R.id.adView) as AdView

        val handler = Handler()
        handler.postDelayed({
            MobileAds.initialize(applicationContext, getString(R.string.ad_app_id))
            this@AdsActivity.adView!!.loadAd(AdRequest.Builder().build())
            updateAdView()
        }, 1000)
    }

    override fun updateAdView() {
        if (PremiumUtils.instance.isPremiumUser) {
            adView!!.pause()
            adView!!.visibility = View.GONE
        } else {
            adView!!.resume()
            adView!!.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        adView!!.pause()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (!PremiumUtils.instance.handleBillingActivityResults(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
