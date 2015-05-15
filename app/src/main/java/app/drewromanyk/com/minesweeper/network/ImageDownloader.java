package app.drewromanyk.com.minesweeper.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;

import app.drewromanyk.com.minesweeper.activities.BaseActivity;
import app.drewromanyk.com.minesweeper.enums.ImageDownloadType;
import app.drewromanyk.com.minesweeper.models.NavDrawerInfo;
import app.drewromanyk.com.minesweeper.models.NavDrawerInfoTemp;
import app.drewromanyk.com.minesweeper.util.Helper;

/**
 * Created by Drew on 4/18/2015.
 */
public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
    ImageDownloadType imageType; //avatar or cover image

    public ImageDownloader(ImageDownloadType imageType) {
        this.imageType = imageType;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        switch(imageType) {
            case AVATAR:
                BaseActivity.navDrawerInfo.getHeaderInfo().setAvatar(result);
                BaseActivity.navDrawerInfo.getRecyclerView().getAdapter().notifyItemChanged(0);
                break;
            case COVER:
                BaseActivity.navDrawerInfo.getHeaderInfo().setCover(Helper.getResizedBitmap(result, 150, 300));
                BaseActivity.navDrawerInfo.getRecyclerView().getAdapter().notifyItemChanged(0);
                break;
        }
    }
}
