package app.drewromanyk.com.minesweeper.enums;

/**
 * Created by drewromanyk on 2/27/17.
 */

public enum UiThemeModeEnum {
    LIGHT("LIGHT"),
    DARK("DARK"),
    AMOLED("AMOLED");

    private String ui_theme_mode;

    UiThemeModeEnum(String ui_theme_mode) {
        this.ui_theme_mode = ui_theme_mode;
    }

    public String theme() {
        return ui_theme_mode;
    }
}
