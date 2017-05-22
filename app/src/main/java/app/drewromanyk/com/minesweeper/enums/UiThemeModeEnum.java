package app.drewromanyk.com.minesweeper.enums;

import app.drewromanyk.com.minesweeper.R;

/**
 * Created by drewromanyk on 2/27/17.
 * UIThemeModeEnum
 * Enum for UI Theme Mode Preference
 */

public enum UiThemeModeEnum {
    LIGHT("LIGHT"),
    DARK("DARK"),
    AMOLED("AMOLED"),
    CLASSICAL("CLASSICAL");

    private String ui_theme_mode;

    UiThemeModeEnum(String ui_theme_mode) {
        this.ui_theme_mode = ui_theme_mode;
    }

    public String theme() {
        return ui_theme_mode;
    }

    public int getColor() {
        switch (this) {
            case DARK:
                return R.color.dark_background;
            case AMOLED:
                return R.color.amoled_background;
            default:
            case CLASSICAL:
            case LIGHT:
                return R.color.light_background;
        }
    }
}
