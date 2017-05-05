package app.drewromanyk.com.minesweeper.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import app.drewromanyk.com.minesweeper.BuildConfig;
import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.enums.PremiumState;
import app.drewromanyk.com.minesweeper.interfaces.UpdateAdViewHandler;

/**
 * Created by drewromanyk on 5/5/17.
 */

public class PremiumUtils implements BillingProcessor.IBillingHandler {
    public static final PremiumUtils instance = new PremiumUtils();
    private PremiumState premium_state = PremiumState.NOT_SURE;
    private BillingProcessor bp;
    private UpdateAdViewHandler adViewHandler;

    private PremiumUtils() {
    }

    public void updateContext(Context context, UpdateAdViewHandler adViewHandler) {
        bp = new BillingProcessor(context, BuildConfig.LICENSE_KEY, this);
        this.adViewHandler = adViewHandler;
    }

    public void releaseContext() {
        if (bp != null) {
            bp.release();
        }
    }

    public boolean handleBillingActivityResults(int requestCode, int resultCode, Intent data) {
        return bp.handleActivityResult(requestCode, resultCode, data);
    }


    public boolean isPremiumUser() {
        return premium_state == PremiumState.PREMIUM;
    }

    public boolean is_not_premium_user() {
        return premium_state == PremiumState.NOT_PREMIUM;
    }

    public void purchase_premium(Activity activity) {
        boolean isAvailable = BillingProcessor.isIabServiceAvailable(activity);
        if (isAvailable && bp != null) {
            boolean isOneTimePurchaseSupported = bp.isOneTimePurchaseSupported();
            if (isOneTimePurchaseSupported) {
                bp.purchase(activity, BuildConfig.PREMIUM_SKU);
            } else {
                Toast.makeText(activity, R.string.google_play_error, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, R.string.google_play_error, Toast.LENGTH_SHORT).show();
        }
    }

    public void clear_purchase() {
        if (bp != null) {
            boolean isOneTimePurchaseSupported = bp.isOneTimePurchaseSupported();
            if (isOneTimePurchaseSupported) {
                bp.consumePurchase(BuildConfig.PREMIUM_SKU);
            }
        }
    }

    /*
     * IBillingHandler
     */

    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
        TransactionDetails td = bp.getPurchaseTransactionDetails(BuildConfig.PREMIUM_SKU);
        if (td != null && td.purchaseInfo.purchaseData.productId.equals(BuildConfig.PREMIUM_SKU)) {
            premium_state = PremiumState.PREMIUM;
        } else {
            premium_state = PremiumState.NOT_PREMIUM;
        }
        adViewHandler.updateAdView();
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */
        if (details.purchaseInfo.purchaseData.productId.equals(BuildConfig.PREMIUM_SKU)) {
            premium_state = PremiumState.PREMIUM;
        }
        adViewHandler.updateAdView();
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        /*
         * Called when some error occurred. See Constants class for more details
         *
         * Note - this includes handling the case where the user canceled the buy dialog:
         * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
         */
        premium_state = PremiumState.NOT_PREMIUM;
        adViewHandler.updateAdView();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }
}
