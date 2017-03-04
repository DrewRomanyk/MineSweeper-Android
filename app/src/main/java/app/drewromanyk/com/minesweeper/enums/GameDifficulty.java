package app.drewromanyk.com.minesweeper.enums;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import app.drewromanyk.com.minesweeper.R;

/**
 * Created by Drew on 4/17/2015.
 * GameDifficulty
 * Enum for the difficulty of Minesweeper, and helper methods for those difficulty's attributes.
 */
public enum GameDifficulty {
    RESUME, CUSTOM, EASY, MEDIUM, EXPERT;

    public String getStoragePrefix() {
        switch (this) {
            case EASY:
                return "EASY_";
            case MEDIUM:
                return "MEDIUM_";
            case EXPERT:
                return "EXPERT_";
            default:
                return "";
        }
    }

    public String getName(Context context) {
        switch (this) {
            case RESUME:
                return context.getString(R.string.game_difficulty_resume);
            case CUSTOM:
                return context.getString(R.string.game_difficulty_custom);
            case EASY:
                return context.getString(R.string.game_difficulty_easy);
            case MEDIUM:
                return context.getString(R.string.game_difficulty_medium);
            case EXPERT:
                return context.getString(R.string.game_difficulty_expert);
            default:
                return context.getString(R.string.game_difficulty_resume);
        }
    }

    public int getColor(Context context) {
        switch (this) {
            case RESUME:
                return ContextCompat.getColor(context, R.color.resume_difficulty);
            case CUSTOM:
                return ContextCompat.getColor(context, R.color.custom_difficulty);
            case EASY:
                return ContextCompat.getColor(context, R.color.easy_difficulty);
            case MEDIUM:
                return ContextCompat.getColor(context, R.color.medium_difficulty);
            case EXPERT:
                return ContextCompat.getColor(context, R.color.expert_difficulty);
            default:
                return ContextCompat.getColor(context, R.color.easy_difficulty);
        }
    }

    public String getDescription(Context context) {
        switch (this) {
            case RESUME:
                return context.getString(R.string.game_difficulty_resume_details);
            case CUSTOM:
                return context.getString(R.string.game_difficulty_custom_details);
            case EASY:
                return context.getString(R.string.game_difficulty_easy_details);
            case MEDIUM:
                return context.getString(R.string.game_difficulty_medium_details);
            case EXPERT:
                return context.getString(R.string.game_difficulty_expert_details);
            default:
                return context.getString(R.string.game_difficulty_resume_details);
        }
    }
}
