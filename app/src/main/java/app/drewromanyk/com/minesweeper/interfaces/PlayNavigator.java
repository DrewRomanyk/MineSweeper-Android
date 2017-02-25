package app.drewromanyk.com.minesweeper.interfaces;

import app.drewromanyk.com.minesweeper.enums.GameDifficulty;

/**
 * Created by Drew on 12/11/15.
 */
public interface PlayNavigator {
    void startGame(final GameDifficulty difficulty);

    void startPlayStore();

    void sendFeedback();
}
