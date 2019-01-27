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

    val themeResourceId: Int
        get() {
            return when (this) {
                DARK -> R.style.AppTheme_Dark_NoActionBar
                AMOLED -> R.style.AppTheme_Amoled_NoActionBar
                CLASSICAL, LIGHT -> R.style.AppTheme_NoActionBar
            }
        }

    fun isLightMode(): Boolean = (this == LIGHT) || (this == CLASSICAL)

    fun isMaterialMode(): Boolean = (this != CLASSICAL)
}
