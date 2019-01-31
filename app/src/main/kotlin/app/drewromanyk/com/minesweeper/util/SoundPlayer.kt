package app.drewromanyk.com.minesweeper.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool

import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.GameSoundType

/**
 * Created by Drew Romanyk on 5/5/17.
 * Model to help play sounds for the Minesweeper game
 */

class SoundPlayer(private val context: Context) {
    private val soundEffects: SoundPool = SoundPool.Builder().setMaxStreams(10).build()
    private val soundIDs: IntArray = IntArray(GameSoundType.values().size)

    init {
        soundIDs[GameSoundType.TAP.ordinal] = soundEffects.load(context, R.raw.click_short, 1)
        soundIDs[GameSoundType.LONGPRESS.ordinal] = soundEffects.load(context, R.raw.click_long, 1)
        soundIDs[GameSoundType.WIN.ordinal] = soundEffects.load(context, R.raw.effect_win, 1)
        soundIDs[GameSoundType.LOSE.ordinal] = soundEffects.load(context, R.raw.effect_lose, 1)
    }

    fun release() {
        soundEffects.release()
    }

    fun play(game_sound_type: GameSoundType) {
        if (UserPrefStorage.getSound(context)) {
            soundEffects.play(soundIDs[game_sound_type.ordinal], 1f, 1f, 1, 0, 1.0f)
        }
    }

}
