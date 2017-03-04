package app.drewromanyk.com.minesweeper.enums;

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
}
