package app.drewromanyk.com.minesweeper.enums

import android.content.Context
import androidx.core.content.ContextCompat

import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.util.UserPrefStorage

/**
 * Created by Drew on 4/17/2015.
 * GameDifficulty
 * Enum for the difficulty of Minesweeper, and helper methods for those difficulty's attributes.
 */
enum class GameDifficulty {
    RESUME, CUSTOM, EASY, MEDIUM, EXPERT;

    val storagePrefix: String
        get() {
            return when (this) {
                EASY -> "EASY_"
                MEDIUM -> "MEDIUM_"
                EXPERT -> "EXPERT_"
                else -> throw IllegalArgumentException()
            }
        }

    fun getName(context: Context): String {
        return when (this) {
            RESUME -> context.getString(R.string.game_difficulty_resume)
            CUSTOM -> context.getString(R.string.game_difficulty_custom)
            EASY -> context.getString(R.string.game_difficulty_easy)
            MEDIUM -> context.getString(R.string.game_difficulty_medium)
            EXPERT -> context.getString(R.string.game_difficulty_expert)
        }
    }

    fun getColor(context: Context): Int {
        return when (this) {
            RESUME -> ContextCompat.getColor(context, R.color.resume_difficulty)
            CUSTOM -> ContextCompat.getColor(context, R.color.custom_difficulty)
            EASY -> ContextCompat.getColor(context, R.color.easy_difficulty)
            MEDIUM -> ContextCompat.getColor(context, R.color.medium_difficulty)
            EXPERT -> ContextCompat.getColor(context, R.color.expert_difficulty)
        }
    }

    fun getDescription(context: Context): String {
        return when (this) {
            RESUME -> context.getString(R.string.game_difficulty_resume_details)
            CUSTOM -> context.getString(R.string.game_difficulty_custom_details)
            EASY -> context.getString(R.string.game_difficulty_easy_details)
            MEDIUM -> context.getString(R.string.game_difficulty_medium_details)
            EXPERT -> context.getString(R.string.game_difficulty_expert_details)
        }
    }

    fun getColumns(context: Context): Int {
        return when (this) {
            RESUME -> throw IllegalArgumentException()
            CUSTOM -> UserPrefStorage.getColumnCount(context)
            EASY -> 9
            MEDIUM -> 16
            EXPERT -> 30
        }
    }

    fun getRows(context: Context): Int {
        return when (this) {
            RESUME -> throw IllegalArgumentException()
            CUSTOM -> UserPrefStorage.getRowCount(context)
            EASY -> 9
            MEDIUM, EXPERT -> 16
        }
    }

    fun getMineCount(context: Context): Int {
        return when (this) {
            RESUME -> throw IllegalArgumentException()
            CUSTOM -> UserPrefStorage.getMineCount(context)
            EASY -> 10
            MEDIUM -> 40
            EXPERT -> 99
        }
    }
}
