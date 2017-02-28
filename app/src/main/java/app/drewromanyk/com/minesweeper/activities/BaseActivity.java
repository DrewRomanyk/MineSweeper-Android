package app.drewromanyk.com.minesweeper.activities;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;

import app.drewromanyk.com.minesweeper.BuildConfig;
import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.application.MinesweeperApp;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.util.BaseGameUtils;
import app.drewromanyk.com.minesweeper.util.UserPrefStorage;

/**
 * Created by Drew on 11/6/15.
 * This is a Base Activity for all activities in order to unify In-app purchases and Google Games
 */
public abstract class BaseActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        BillingProcessor.IBillingHandler {
    private static final String TAG = "BaseActivity";
    // ADS
    private AdView mAdView;

    // GOOGLE GAMES
    private GoogleApiClient googleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mSignInClicked = false;
    private boolean mAutoStartSignInFlow = true;

    // IN APP PURCHASES
    public BillingProcessor bp;

    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
        TransactionDetails td = bp.getPurchaseTransactionDetails(BuildConfig.PREMIUM_SKU);
        if (td != null && td.purchaseInfo.purchaseData.productId.equals(BuildConfig.PREMIUM_SKU)) {
            updateAds(1);
        } else {
            updateAds(0);
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */
        if (details.purchaseInfo.purchaseData.productId.equals(BuildConfig.PREMIUM_SKU)) {
            updateAds(1);
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        /*
         * Called when some error occurred. See Constants class for more details
         *
         * Note - this includes handling the case where the user canceled the buy dialog:
         * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
         */
        updateAds(0);
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    protected void setSignInClicked(boolean value) {
        mSignInClicked = value;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupTaskActivityInfo();

        bp = new BillingProcessor(this, BuildConfig.LICENSE_KEY, this);
    }

    private void setupTaskActivityInfo() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Only for LOLLIPOP and newer versions
            if (!(this instanceof MainActivity))
                getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));

            ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_icon),
                    getResources().getColor(R.color.primary_task));
            setTaskDescription(tDesc);
        }
    }

    protected void setupGoogleGames() {
        mAutoStartSignInFlow = (this instanceof MainActivity) && UserPrefStorage.isFirstTime(this);
        UserPrefStorage.setFirstTime(this, false);

        // Create the Google API Client with access to Plus and Games
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    protected void setupAds(AdView adView) {
        mAdView = adView;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MobileAds.initialize(getApplicationContext(), getString(R.string.ad_app_id));
                mAdView.loadAd(new AdRequest.Builder().build());
                if (((MinesweeperApp) getApplication()).getIsPremium() == 1) {
                    mAdView.pause();
                    mAdView.setVisibility(View.GONE);
                }
            }
        }, 1000);
    }

    private void updateAds(int isPremium) {
        ((MinesweeperApp) getApplication()).setIsPremium(isPremium);
        if (isPremium == 1) {
            mAdView.pause();
            mAdView.setVisibility(View.GONE);
        } else {
            mAdView.resume();
            mAdView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (((MinesweeperApp) getApplication()).getIsPremium() != 1) {
            mAdView.resume();
            mAdView.setVisibility(View.VISIBLE);
        } else {
            mAdView.pause();
            mAdView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdView != null) mAdView.destroy();
        if (bp != null) bp.release();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    /*
     * GOOGLE GAME CONNECTION
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == ResultCodes.SIGN_IN.ordinal()) {
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                if (googleApiClient != null && resultCode == RESULT_OK) {
                    googleApiClient.connect();
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // Already resolving
            return;
        }

        // If the sign in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    googleApiClient, connectionResult,
                    ResultCodes.SIGN_IN.ordinal(), getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }
    }
}
