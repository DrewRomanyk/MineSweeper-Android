package app.drewromanyk.com.minesweeper.enums;

import android.content.Context;

import app.drewromanyk.com.minesweeper.R;

/**
 * Created by Drew on 4/17/2015.
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
                return context.getResources().getColor(R.color.resume_difficulty);
            case CUSTOM:
                return context.getResources().getColor(R.color.custom_difficulty);
            case EASY:
                return context.getResources().getColor(R.color.easy_difficulty);
            case MEDIUM:
                return context.getResources().getColor(R.color.medium_difficulty);
            case EXPERT:
                return context.getResources().getColor(R.color.expert_difficulty);
            default:
                return context.getResources().getColor(R.color.easy_difficulty);
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
