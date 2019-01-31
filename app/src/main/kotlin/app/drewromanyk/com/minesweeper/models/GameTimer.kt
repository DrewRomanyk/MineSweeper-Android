package app.drewromanyk.com.minesweeper.models

import app.drewromanyk.com.minesweeper.interfaces.GameTimerHandler
import java.util.*

/**
 * Created by Drew Romanyk on 5/20/17.
 * Model for handling the game time and the timer
 */
class GameTimer constructor(startTime: Long = GameTimer.DEFAULT_START_TIME, private val gameTimerHandler: GameTimerHandler) {
    companion object {
        const val DEFAULT_START_TIME: Long = 1
    }

    var time: Long
        private set
    private var timer: Timer? = null
    private var isOn = false

    init {
        time = startTime
    }

    fun startGameTime() {
        isOn = true
        timer?.cancel()
        timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                if (isOn) {
                    time += 1000 // increase every sec
                    time -= time % 1000
                    gameTimerHandler.onTimerTick(time)
                }
            }
        }

        timer!!.scheduleAtFixedRate(timerTask, 0, 1000)
    }

    fun stopGameTime() {
        isOn = false
        timer?.cancel()
    }

    fun reset() {
        stopGameTime()
        time = DEFAULT_START_TIME
    }
}