package app.drewromanyk.com.minesweeper.enums

import android.content.Context
import android.support.v4.content.ContextCompat

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
            when (this) {
                EASY -> return "EASY_"
                MEDIUM -> return "MEDIUM_"
                EXPERT -> return "EXPERT_"
                else -> throw IllegalArgumentException()
            }
        }

    fun getName(context: Context): String {
        when (this) {
            RESUME -> return context.getString(R.string.game_difficulty_resume)
            CUSTOM -> return context.getString(R.string.game_difficulty_custom)
            EASY -> return context.getString(R.string.game_difficulty_easy)
            MEDIUM -> return context.getString(R.string.game_difficulty_medium)
            EXPERT -> return context.getString(R.string.game_difficulty_expert)
            else -> throw IllegalArgumentException()
        }
    }

    fun getColor(context: Context): Int {
        when (this) {
            RESUME -> return ContextCompat.getColor(context, R.color.resume_difficulty)
            CUSTOM -> return ContextCompat.getColor(context, R.color.custom_difficulty)
            EASY -> return ContextCompat.getColor(context, R.color.easy_difficulty)
            MEDIUM -> return ContextCompat.getColor(context, R.color.medium_difficulty)
            EXPERT -> return ContextCompat.getColor(context, R.color.expert_difficulty)
            else -> throw IllegalArgumentException()
        }
    }

    fun getDescription(context: Context): String {
        when (this) {
            RESUME -> return context.getString(R.string.game_difficulty_resume_details)
            CUSTOM -> return context.getString(R.string.game_difficulty_custom_details)
            EASY -> return context.getString(R.string.game_difficulty_easy_details)
            MEDIUM -> return context.getString(R.string.game_difficulty_medium_details)
            EXPERT -> return context.getString(R.string.game_difficulty_expert_details)
            else -> throw IllegalArgumentException()
        }
    }

    fun getColumns(context: Context): Int {
        when (this) {
            RESUME -> return 0
            CUSTOM -> return UserPrefStorage.getColumnCount(context)
            EASY -> return 9
            MEDIUM -> return 16
            EXPERT -> return 30
            else -> throw IllegalArgumentException()
        }
    }

    fun getRows(context: Context): Int {
        when (this) {
            RESUME -> return 0
            CUSTOM -> return UserPrefStorage.getRowCount(context)
            EASY -> return 9
            MEDIUM, EXPERT -> return 16
            else -> throw IllegalArgumentException()
        }
    }

    fun getMineCount(context: Context): Int {
        when (this) {
            RESUME -> return 0
            CUSTOM -> return UserPrefStorage.getMineCount(context)
            EASY -> return 10
            MEDIUM -> return 40
            EXPERT -> return 99
            else -> throw IllegalArgumentException()
        }
    }
}
