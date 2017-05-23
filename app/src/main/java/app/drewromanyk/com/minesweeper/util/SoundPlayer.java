package app.drewromanyk.com.minesweeper.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.enums.GameSoundType;

/**
 * Created by Drew Romanyk on 5/5/17.
 * Model to help play sounds for the Minesweeper game
 */

public class SoundPlayer {
    private SoundPool soundEffects;
    private int[] soundIDs;
    private Context context;

    public SoundPlayer(Context context) {
        this.context = context;

        soundEffects = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundIDs = new int[GameSoundType.values().length];
        soundIDs[GameSoundType.TAP.ordinal()] = soundEffects.load(context, R.raw.click_short, 1);
        soundIDs[GameSoundType.LONGPRESS.ordinal()] = soundEffects.load(context, R.raw.click_long, 1);
        soundIDs[GameSoundType.WIN.ordinal()] = soundEffects.load(context, R.raw.effect_win, 1);
        soundIDs[GameSoundType.LOSE.ordinal()] = soundEffects.load(context, R.raw.effect_lose, 1);
    }

    public void release() {
        soundEffects.release();
    }

    public void play(GameSoundType game_sound_type) {
        if (UserPrefStorage.INSTANCE.getSound(context)) {
            soundEffects.play(soundIDs[game_sound_type.ordinal()], 1, 1, 1, 0, 1.0f);
        }
    }

}
