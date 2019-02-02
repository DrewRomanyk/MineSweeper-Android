package app.drewromanyk.com.minesweeper.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.ClickMode
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.enums.GameStatus
import app.drewromanyk.com.minesweeper.enums.UiThemeMode
import app.drewromanyk.com.minesweeper.interfaces.MinesweeperHandler
import app.drewromanyk.com.minesweeper.models.Minesweeper
import org.json.JSONArray
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics


/**
 * UserPrefStorage
 * Class designed to handle all user pref storage information
 * - Settings
 * - Game status/saved
 * - Stats
 * - first time
 * Created by Drew on 4/17/2015.
 */
object UserPrefStorage {

    private fun getPrefs(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    /*
     * FIRST TIME INFO
     */

    fun isFirstTime(context: Context): Boolean {
        return getPrefs(context).getBoolean("FIRST_TIME", true)
    }

    fun setFirstTime(context: Context, value: Boolean) {
        val editor = getPrefs(context).edit()
        editor.putBoolean("FIRST_TIME", value)
        editor.apply()
    }

    /*
     * RATING IN-APP
     */

    fun canShowRatingDialog(context: Context): Boolean {
        val hasFinishedRatingDialog = getPrefs(context).getBoolean("FINISHED_RATING", false)
        val hasOpenedApp5times = getPrefs(context).getInt("APP_OPEN_COUNT", 0) >= 5
        var hasWonGame = false
        for (difficulty in GameDifficulty.values()) {
            if (difficulty == GameDifficulty.CUSTOM || difficulty == GameDifficulty.RESUME) continue
            hasWonGame = getWinsForDifficulty(context, difficulty) > 0
            if (hasWonGame) break
        }

        return !hasFinishedRatingDialog && hasOpenedApp5times && hasWonGame
    }

    fun setHasFinishedRatingDialog(context: Context) {
        val editor = getPrefs(context).edit()
        editor.putBoolean("FINISHED_RATING", true)
        editor.apply()
    }

    fun increaseAppOpenCount(context: Context) {
        val appOpenCount = getPrefs(context).getInt("APP_OPEN_COUNT", 0)

        val editor = getPrefs(context).edit()
        editor.putInt("APP_OPEN_COUNT", appOpenCount + 1)
        editor.apply()
    }

    /*
     * SAVED GAME INFO
     */

    private fun getSavedDataVersion(context: Context): String {
        return getPrefs(context).getString("SAVED_VERSION", "")!!
    }

    private fun isCurrentSavedDataVersion(context: Context): Boolean {
        return getSavedDataVersion(context) == context.getString(R.string.preference_saved_current_version)
    }

    private fun getLastGameStatus(context: Context): Int {
        return getPrefs(context).getInt("STATUS", GameStatus.DEFEAT.ordinal)
    }

    fun getGameDifficulty(context: Context): GameDifficulty {
        return GameDifficulty.values()[getPrefs(context).getInt("DIFFICULTY", GameDifficulty.CUSTOM.ordinal)]
    }

    private fun getGameDuration(context: Context): Long {
        return getPrefs(context).getLong("TIME_MILLIS", 1)
    }

    data class GameStorageData(val minesweeper: Minesweeper, val gameDifficulty: GameDifficulty, val clickMode: ClickMode)

    fun loadGame(context: Context, gameHandler: MinesweeperHandler): GameStorageData {
        val preferences = getPrefs(context)

        val rows = preferences.getInt("ROWS", 0)
        val columns = preferences.getInt("COLUMNS", 0)

        if (!((rows == 0) || (columns == 0))) {
            val difficulty = getGameDifficulty(context)
            val clickMode = ClickMode.values()[preferences.getInt("CLICK_MODE", ClickMode.REVEAL.ordinal)]
            val mineCount = preferences.getInt("MINE_COUNT", 0)
            val status = GameStatus.values()[preferences.getInt("STATUS", GameStatus.DEFEAT.ordinal)]
            val startGameTime = getGameDuration(context)

            try {
                val cellValuesJ = JSONArray(preferences.getString("CELL_VALUES", "[]"))
                val cellRevealedJ = JSONArray(preferences.getString("CELL_REVEALED", "[]"))
                val cellFlaggedJ = JSONArray(preferences.getString("CELL_FLAGGED", "[]"))

                return GameStorageData(
                        Minesweeper(gameHandler, rows, columns, mineCount, startGameTime, status,
                                cellValuesJ, cellRevealedJ, cellFlaggedJ),
                        difficulty, clickMode)
            } catch (e: Exception) {
                val fba = FirebaseAnalytics.getInstance(context)
                fba.logEvent("loadGameError", null)
                e.printStackTrace()
            }

        }

        throw IllegalArgumentException()
    }

    fun saveGame(context: Context, gameStorageData: GameStorageData) {
        val minesweeper = gameStorageData.minesweeper
        val gameDifficulty = gameStorageData.gameDifficulty
        val clickMode = gameStorageData.clickMode

        val time = minesweeper.getTime()
        val editor = getPrefs(context).edit()

        editor.putString("SAVED_VERSION", context.getString(R.string.preference_saved_current_version))
        editor.putInt("ROWS", minesweeper.cells.size)
        editor.putInt("COLUMNS", minesweeper.cells[0].size)
        editor.putInt("MINE_COUNT", gameDifficulty.getMineCount(context))
        editor.putInt("DIFFICULTY", gameDifficulty.ordinal)
        editor.putInt("CLICK_MODE", clickMode.ordinal)
        editor.putInt("STATUS", minesweeper.gameStatus.ordinal)
        editor.putLong("TIME_MILLIS", time)

        val cellValues = JSONArray()
        val cellRevealed = JSONArray()
        val cellFlagged = JSONArray()
        for (r in 0 until minesweeper.cells.size) {
            for (c in 0 until minesweeper.cells[0].size) {
                cellValues.put(minesweeper.cells[r][c].value)
                cellRevealed.put(minesweeper.cells[r][c].isRevealed())
                cellFlagged.put(minesweeper.cells[r][c].isFlagged())
            }
        }

        editor.putString("CELL_VALUES", cellValues.toString())
        editor.putString("CELL_REVEALED", cellRevealed.toString())
        editor.putString("CELL_FLAGGED", cellFlagged.toString())

        editor.apply()
    }

    fun invalidateSavedGame(context: Context) {
        val editor = getPrefs(context).edit()
        editor.putInt("ROWS", 0)
        editor.putInt("COLUMNS", 0)
        editor.putInt("STATUS", GameStatus.DEFEAT.ordinal)
        editor.apply()
    }

    fun hasResumeGame(context: Context): Boolean {
        return getLastGameStatus(context) == GameStatus.PLAYING.ordinal && getRowCount(context) > 0 && getColumnCount(context) > 0 && isCurrentSavedDataVersion(context)
    }

    /*
     * STATS
     */

    fun updateStatsWithGame(context: Context, gameDifficulty: GameDifficulty, minesweeper: Minesweeper) {

        var newBestTime = false
        var newBestScore = false

        // skip the resume/custom modes
        if (gameDifficulty == GameDifficulty.RESUME || gameDifficulty == GameDifficulty.CUSTOM)
            return

        //initial data
        var wins = getWinsForDifficulty(context, gameDifficulty)
        var loses = getLosesForDifficulty(context, gameDifficulty)
        var bestTime = getBestTimeForDifficulty(context, gameDifficulty)
        var avgTime = getAvgTimeForDifficulty(context, gameDifficulty)
        var explorePercent = getExplorPercentForDifficulty(context, gameDifficulty)
        var winStreak = getWinStreakForDifficulty(context, gameDifficulty)
        var losesStreak = getLoseStreakForDifficulty(context, gameDifficulty)
        var currentWinStreak = getCurWinStreakForDifficulty(context, gameDifficulty)
        var currentLosesStreak = getCurLoseStreakForDifficulty(context, gameDifficulty)
        var bestScore = getBestScoreForDifficulty(context, gameDifficulty)
        var avgScore = getAvgScoreForDifficulty(context, gameDifficulty)

        // Update wins/losses/total
        if (minesweeper.gameStatus == GameStatus.VICTORY) {
            wins++
        } else {
            loses++
        }
        val totalGames = wins + loses

        // Update best time and avg time
        val currentTime: Int = (minesweeper.getTime() / 1000).toInt()
        if (minesweeper.gameStatus == GameStatus.VICTORY) {
            // Smaller currentTime is better than bestTime
            if (bestTime > currentTime || bestTime == 0) {
                newBestTime = true
                bestTime = currentTime
            }
            avgTime += (currentTime - avgTime) / wins
        }

        // Update exploration percentage
        val curExplorePercent = minesweeper.getExplorePercent()
        explorePercent += (curExplorePercent - explorePercent) / totalGames

        // Update streaks
        if (minesweeper.gameStatus == GameStatus.VICTORY) {
            currentWinStreak++
            currentLosesStreak = 0
        } else {
            currentWinStreak = 0
            currentLosesStreak++
        }
        if (currentWinStreak > winStreak)
            winStreak = currentWinStreak
        if (currentLosesStreak > losesStreak)
            losesStreak = currentLosesStreak

        // Update best score & avg score
        val currentScore: Int = (minesweeper.getScore() * 1000).toInt()
        if (minesweeper.gameStatus == GameStatus.VICTORY) {
            // Bigger currentScore is better than bestScore
            if (bestScore < currentScore || bestScore == 0) {
                newBestScore = true
                bestScore = currentScore
            }
            avgScore += (currentScore.toFloat() - avgScore) / wins
        }

        updateStats(context, gameDifficulty, wins, loses, bestTime, avgTime,
                explorePercent, winStreak, losesStreak, currentWinStreak, currentLosesStreak,
                bestScore, avgScore)
        invalidateSavedGame(context)

        // Display new bests
        if (newBestTime)
            Toast.makeText(context, R.string.game_best_time, Toast.LENGTH_SHORT).show()

        if (newBestScore)
            Toast.makeText(context, R.string.game_best_score, Toast.LENGTH_SHORT).show()
    }

    fun updateStats(context: Context, difficulty: GameDifficulty, wins: Int, loses: Int,
                    bestTime: Int, avgTime: Float, explorePercent: Float, winStreak: Int,
                    losesStreak: Int, currentWinStreak: Int, currentLosesStreak: Int,
                    bestScore: Int, avgScore: Float) {
        val editor = getPrefs(context).edit()
        val prefix = difficulty.storagePrefix
        // Get key info
        val winsKey = "${prefix}WINS"
        val losesKey = "${prefix}LOSES"
        val bestTimeKey = "${prefix}BEST_TIME"
        val avgTimeKey = "${prefix}AVG_TIME"
        val explorePercentKey = "${prefix}EXPLOR_PERCT"
        val winStreakKey = "${prefix}WIN_STREAK"
        val losesStreakKey = "${prefix}LOSES_STREAK"
        val currentWinStreakKey = "${prefix}CURRENTWIN_STREAK"
        val currentLosesStreakKey = "${prefix}CURRENTLOSES_STREAK"
        val bestScoreKey = "${prefix}BEST_SCORE"
        val avgScoreKey = "${prefix}AVG_SCORE"

        // Put data into storage
        editor.putInt(winsKey, wins)
        editor.putInt(losesKey, loses)
        editor.putInt(bestTimeKey, bestTime)
        editor.putFloat(avgTimeKey, avgTime)
        editor.putFloat(explorePercentKey, explorePercent)
        editor.putInt(winStreakKey, winStreak)
        editor.putInt(losesStreakKey, losesStreak)
        editor.putInt(currentWinStreakKey, currentWinStreak)
        editor.putInt(currentLosesStreakKey, currentLosesStreak)
        editor.putInt(bestScoreKey, bestScore)
        editor.putFloat(avgScoreKey, avgScore)
        editor.apply()
    }

    fun getWinsForDifficulty(context: Context, difficulty: GameDifficulty): Int {
        return getPrefs(context).getInt("${difficulty.storagePrefix}WINS", 0)
    }

    fun getLosesForDifficulty(context: Context, difficulty: GameDifficulty): Int {
        return getPrefs(context).getInt("${difficulty.storagePrefix}LOSES", 0)
    }

    fun getBestTimeForDifficulty(context: Context, difficulty: GameDifficulty): Int {
        return getPrefs(context).getInt("${difficulty.storagePrefix}BEST_TIME", 0)
    }

    fun getAvgTimeForDifficulty(context: Context, difficulty: GameDifficulty): Float {
        return getPrefs(context).getFloat("${difficulty.storagePrefix}AVG_TIME", 0f)
    }

    fun getExplorPercentForDifficulty(context: Context, difficulty: GameDifficulty): Float {
        return getPrefs(context).getFloat("${difficulty.storagePrefix}EXPLOR_PERCT", 0f)
    }

    fun getWinStreakForDifficulty(context: Context, difficulty: GameDifficulty): Int {
        return getPrefs(context).getInt("${difficulty.storagePrefix}WIN_STREAK", 0)
    }

    fun getLoseStreakForDifficulty(context: Context, difficulty: GameDifficulty): Int {
        return getPrefs(context).getInt("${difficulty.storagePrefix}LOSES_STREAK", 0)
    }

    fun getCurWinStreakForDifficulty(context: Context, difficulty: GameDifficulty): Int {
        return getPrefs(context).getInt("${difficulty.storagePrefix}CURRENTWIN_STREAK", 0)
    }

    fun getCurLoseStreakForDifficulty(context: Context, difficulty: GameDifficulty): Int {
        return getPrefs(context).getInt("${difficulty.storagePrefix}CURRENTLOSES_STREAK", 0)
    }

    fun getBestScoreForDifficulty(context: Context, difficulty: GameDifficulty): Int {
        return getPrefs(context).getInt("${difficulty.storagePrefix}BEST_SCORE", 0)
    }

    fun getAvgScoreForDifficulty(context: Context, difficulty: GameDifficulty): Float {
        return getPrefs(context).getFloat("${difficulty.storagePrefix}AVG_SCORE", 0f)
    }

    /*
     * SETTINGS
     */

    fun getRowCount(context: Context): Int {
        return getPrefs(context).getInt(context.getString(R.string.preference_rowcount), 9)
    }

    fun getColumnCount(context: Context): Int {
        return getPrefs(context).getInt(context.getString(R.string.preference_columncount), 9)
    }

    fun getMineCount(context: Context): Int {
        return getPrefs(context).getInt(context.getString(R.string.preference_minecount), 10)
    }

    fun getVibrate(context: Context): Boolean {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_vibration), true)
    }

    fun getVibrationDuration(context: Context): Long {
        return getPrefs(context).getInt(context.getString(R.string.preference_vibration_duration), 100).toLong()
    }

    fun getAnimationCheck(context: Context): Boolean {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_animation), true)
    }

    fun getInfoBarVisibility(context: Context): Boolean {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_info_bar_visibility), true)
    }

    fun getSound(context: Context): Boolean {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_sound), true)
    }

    fun getSwiftOpen(context: Context): Boolean {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_swiftopen), true)
    }

    fun getSwiftChange(context: Context): Boolean {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_swiftchange), true)
    }

    fun getVolumeButton(context: Context): Boolean {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_volumebutton), true)
    }

    fun getScreenOn(context: Context): Boolean {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_screenon), false)
    }

    fun getLockRotate(context: Context): Boolean {
        return getPrefs(context).getBoolean(context.getString(R.string.preference_lockrotate), false)
    }

    fun getLongPressLength(context: Context): Int {
        return getPrefs(context).getInt(context.getString(R.string.preference_longclick_duration), 400)
    }

    fun getUiThemeMode(context: Context): UiThemeMode {
        var curThemeValue: String = getPrefs(context).getString(context.getString(R.string.preference_ui_theme_mode), "LIGHT")

        // I messed up the code in v1.4.0 as I put an invalid name for the enum
        if (curThemeValue == "light") {
            curThemeValue = "LIGHT"
            getPrefs(context).edit().putString(context.getString(R.string.preference_ui_theme_mode), "LIGHT").apply()
        }

        return UiThemeMode.valueOf(curThemeValue)
    }
}
