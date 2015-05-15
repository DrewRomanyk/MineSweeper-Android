package app.drewromanyk.com.minesweeper.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import app.drewromanyk.com.minesweeper.R;

/**
 * Created by Drew on 5/14/2015.
 */
public class NavDrawerHeaderInfo {
    private static Bitmap emptyAvatar;
    private static Bitmap emptyCover;

    private Context context;
    private Bitmap avatar;
    private Bitmap cover;
    private String name;
    private String email;

    public NavDrawerHeaderInfo(Context context) {
        this.context = context;

        if(emptyAvatar == null) {
            emptyAvatar = BitmapFactory.decodeResource(context.getResources(), R.drawable.person_image_empty);
            emptyCover = BitmapFactory.decodeResource(context.getResources(), R.color.background_material_dark);
        }

        setPlayerToEmpty();
    }

    public void setPlayerToEmpty() {
        avatar = emptyAvatar;
        cover = emptyCover;
        name = context.getString(R.string.nav_header_playername_empty);
        email = "";
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }

    public Bitmap getCover() {
        return cover;
    }

    public void setCover(Bitmap cover) {
        this.cover = cover;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
