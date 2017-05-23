package app.drewromanyk.com.minesweeper.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast

import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails

import app.drewromanyk.com.minesweeper.BuildConfig
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.PremiumState
import app.drewromanyk.com.minesweeper.interfaces.UpdateAdViewHandler

/**
 * Created by Drew Romanyk on 5/5/17.
 * Util to help with the Premium functions
 */

class PremiumUtils private constructor() : BillingProcessor.IBillingHandler {
    companion object {
        val instance = PremiumUtils()
    }

    private var premium_state = PremiumState.NOT_SURE
    private var bp: BillingProcessor? = null
    private var adViewHandler: UpdateAdViewHandler? = null

    fun updateContext(context: Context, adViewHandler: UpdateAdViewHandler) {
        bp = BillingProcessor(context, BuildConfig.LICENSE_KEY, this)
        this.adViewHandler = adViewHandler
    }

    fun releaseContext() {
        if (bp != null) {
            bp!!.release()
        }
    }

    fun handleBillingActivityResults(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return bp!!.handleActivityResult(requestCode, resultCode, data)
    }


    val isPremiumUser: Boolean
        get() = premium_state === PremiumState.PREMIUM

    val isNotPremiumUser: Boolean
        get() = premium_state === PremiumState.NOT_PREMIUM

    fun purchase_premium(activity: Activity) {
        if (BillingProcessor.isIabServiceAvailable(activity) && (bp != null)) {
            if (bp!!.isOneTimePurchaseSupported) {
                bp!!.purchase(activity, BuildConfig.PREMIUM_SKU)
            } else {
                Toast.makeText(activity, R.string.google_play_error, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(activity, R.string.google_play_error, Toast.LENGTH_SHORT).show()
        }
    }

    fun clear_purchase() {
        bp?.let {
            if (it.isOneTimePurchaseSupported) {
                it.consumePurchase(BuildConfig.PREMIUM_SKU)
            }
        }
    }

    /*
     * IBillingHandler
     */

    override fun onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
        val td = bp!!.getPurchaseTransactionDetails(BuildConfig.PREMIUM_SKU)
        if (td?.purchaseInfo?.purchaseData?.productId == BuildConfig.PREMIUM_SKU) {
            premium_state = PremiumState.PREMIUM
        } else {
            premium_state = PremiumState.NOT_PREMIUM
        }
        adViewHandler!!.updateAdView()
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */
        if (details.purchaseInfo.purchaseData.productId == BuildConfig.PREMIUM_SKU) {
            premium_state = PremiumState.PREMIUM
        }
        adViewHandler!!.updateAdView()
    }

    override fun onBillingError(errorCode: Int, error: Throwable) {
        /*
         * Called when some error occurred. See Constants class for more details
         *
         * Note - this includes handling the case where the user canceled the buy dialog:
         * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
         */
        premium_state = PremiumState.NOT_PREMIUM
        adViewHandler!!.updateAdView()
    }

    override fun onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }
}
