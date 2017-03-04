package app.drewromanyk.com.minesweeper.interfaces;

import app.drewromanyk.com.minesweeper.enums.GameDifficulty;

/**
 * Created by Drew on 12/11/15.
 * PlayNavigator
 * Interface to enable activity needed functions in the recycler view
 */
public interface PlayNavigator {
    void startGame(final GameDifficulty difficulty);

    void startPlayStore();

    void sendFeedback();
}
