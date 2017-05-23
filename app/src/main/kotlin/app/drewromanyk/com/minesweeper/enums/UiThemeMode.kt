package app.drewromanyk.com.minesweeper.enums

import app.drewromanyk.com.minesweeper.R

/**
 * Created by Drew Romanyk on 2/27/17.
 * UIThemeModeEnum
 * Enum for UI Theme Mode Preference
 */

enum class UiThemeMode constructor(private val ui_theme_mode: String) {
    LIGHT("LIGHT"),
    DARK("DARK"),
    AMOLED("AMOLED"),
    CLASSICAL("CLASSICAL");

    fun theme(): String {
        return ui_theme_mode
    }

    val color: Int
        get() {
            when (this) {
                DARK -> return R.color.dark_background
                AMOLED -> return R.color.amoled_background
                CLASSICAL, LIGHT -> return R.color.light_background
            }
        }
}
