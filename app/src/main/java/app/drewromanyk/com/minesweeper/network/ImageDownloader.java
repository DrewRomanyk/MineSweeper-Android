package app.drewromanyk.com.minesweeper.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;

import app.drewromanyk.com.minesweeper.activities.MainActivity;
import app.drewromanyk.com.minesweeper.enums.ImageDownloadType;
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
//                MainActivity.navDrawerInfo.getHeaderInfo().setAvatar(result);
//                MainActivity.navDrawerInfo.getRecyclerView().getAdapter().notifyItemChanged(0);
                break;
            case COVER:
//                MainActivity.navDrawerInfo.getHeaderInfo().setCover(Helper.getResizedBitmap(result, 150, 300));
//                MainActivity.navDrawerInfo.getRecyclerView().getAdapter().notifyItemChanged(0);
                break;
        }
    }
}
