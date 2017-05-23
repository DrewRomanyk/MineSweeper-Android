package app.drewromanyk.com.minesweeper.enums

/**
 * Created by Drew on 4/17/2015.
 * This is for standardizing the status of the game
 */
enum class GameStatus {
    NOT_STARTED, PLAYING, DEFEAT, VICTORY;

    fun isGameOver(): Boolean = (this == DEFEAT) or (this == VICTORY)

}
