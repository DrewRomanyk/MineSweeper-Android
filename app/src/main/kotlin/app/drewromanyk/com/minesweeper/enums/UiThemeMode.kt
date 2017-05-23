package app.drewromanyk.com.minesweeper.enums

import app.drewromanyk.com.minesweeper.R

/**
 * Created by Drew Romanyk on 2/27/17.
 * UIThemeModeEnum
 * Enum for UI Theme Mode Preference
 */

enum class UiThemeMode constructor(val themeValue: String) {
    LIGHT("LIGHT"),
    DARK("DARK"),
    AMOLED("AMOLED"),
    CLASSICAL("CLASSICAL");

    val color: Int
        get() {
            when (this) {
                DARK -> return R.color.dark_background
                AMOLED -> return R.color.amoled_background
                CLASSICAL, LIGHT -> return R.color.light_background
            }
        }

    fun isLightMode(): Boolean = (this == LIGHT) || (this == CLASSICAL)

    fun isMaterialMode(): Boolean = (this != CLASSICAL)
}
