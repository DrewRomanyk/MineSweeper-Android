package app.drewromanyk.com.minesweeper.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import app.drewromanyk.com.minesweeper.BuildConfig;
import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.interfaces.UpdateAdViewHandler;
import app.drewromanyk.com.minesweeper.util.PremiumUtils;

/**
 * Created by Drew on 11/6/15.
 * This is a Base Activity for all activities in order to unify In-app purchases and Google Games
 */
public abstract class AdsActivity extends GameServicesActivity implements UpdateAdViewHandler {
    private static final String TAG = "AdsActivity";
    // ADS
    private AdView adView;

    protected void setupAds() {
        PremiumUtils.instance.updateContext(this, this);
        this.adView = (AdView) findViewById(R.id.adView);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MobileAds.initialize(getApplicationContext(), getString(R.string.ad_app_id));
                AdsActivity.this.adView.loadAd(new AdRequest.Builder().build());
                updateAdView();
            }
        }, 1000);
    }

    public void updateAdView() {
        if (PremiumUtils.instance.isPremiumUser()) {
            adView.pause();
            adView.setVisibility(View.GONE);
        } else {
            adView.resume();
            adView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        adView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAdView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adView != null) adView.destroy();
        PremiumUtils.instance.releaseContext();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!PremiumUtils.instance.handleBillingActivityResults(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
