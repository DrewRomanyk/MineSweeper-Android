package app.drewromanyk.com.minesweeper.activities;

import android.content.Intent;
import android.os.Handler;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.interfaces.UpdateAdViewHandler;
import app.drewromanyk.com.minesweeper.util.PremiumUtils;

/**
 * Created by Drew on 11/6/15.
 * This is a Activity that handles Ads and In-app purchases through PremiumUtils
 */
public abstract class AdsActivity extends GameServicesActivity implements UpdateAdViewHandler {
    // ADS
    private AdView adView;

    protected void setupAds() {
        PremiumUtils.Companion.getInstance().updateContext(this, this);
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
        if (PremiumUtils.Companion.getInstance().isPremiumUser()) {
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
        PremiumUtils.Companion.getInstance().releaseContext();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!PremiumUtils.Companion.getInstance().handleBillingActivityResults(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
