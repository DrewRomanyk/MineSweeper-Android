package app.drewromanyk.com.minesweeper.views

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.GridLayout
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.ClickMode
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.enums.GameSoundType
import app.drewromanyk.com.minesweeper.enums.GameStatus
import app.drewromanyk.com.minesweeper.interfaces.MinesweeperHandler
import app.drewromanyk.com.minesweeper.interfaces.MinesweeperUiHandler
import app.drewromanyk.com.minesweeper.models.Cell
import app.drewromanyk.com.minesweeper.models.Minesweeper
import app.drewromanyk.com.minesweeper.util.Helper
import app.drewromanyk.com.minesweeper.util.SoundPlayer
import app.drewromanyk.com.minesweeper.util.UserPrefStorage

/**
 * Created by Drew Romanyk on 5/18/17.
 * Glue for the Minesweeper model, it handles all UI interactions as it contains the visual cells
 *  for the game
 */
class MinesweeperUI(loadGame: Boolean, gameDifficulty: GameDifficulty, private val boardInfoView: BoardInfoView, context: Context, private val minesweeperUiHandler: MinesweeperUiHandler?) : MinesweeperHandler {

    val gameDifficulty: GameDifficulty
    private val minesweeper: Minesweeper
    private var clickMode: ClickMode = ClickMode.REVEAL
        set(value) {
            field = value
            updateUiCellImage()
            minesweeperUiHandler?.onFlagChange(value)
        }

    val layout: GridLayout = GridLayout(context)
    private val uiCells: Array<Array<UiCell>>
    private val soundPlayer: SoundPlayer = SoundPlayer(context)

    // Settings
    private val isSwiftOpenEnabled = UserPrefStorage.getSwiftOpen(context)
    private val isSwiftChangeEnabled = UserPrefStorage.getSwiftChange(context)

    init {
        var results: UserPrefStorage.GameStorageData? = null
        if (loadGame) {
            try {
                results = UserPrefStorage.loadGame(context, this)
            } catch (exception: IllegalArgumentException) {
                // This is due to rotating when not playing yet
                results = null
            }
        }

        if (results == null) {
            this.gameDifficulty = gameDifficulty
            minesweeper = Minesweeper(this.gameDifficulty.getRows(context), this.gameDifficulty.getColumns(context), this.gameDifficulty.getMineCount(context), this)
        } else {
            minesweeper = results.minesweeper
            this.gameDifficulty = results.gameDifficulty
            minesweeperUiHandler?.onGameTimerTick(results.minesweeper.getTime(), results.minesweeper.getScore())
        }

        uiCells = Array(this.gameDifficulty.getRows(context)) { Array(this.gameDifficulty.getColumns(context)) { UiCell(context) } }
        boardInfoView.reset(this.gameDifficulty.getMineCount(context))
        layout.rowCount = uiCells.size
        layout.columnCount = uiCells[0].size
        layout.setPadding(20, 20, 20, 20)

        for (r in 0 until layout.rowCount) {
            for (c in 0 until layout.columnCount) {
                layout.addView(uiCells[r][c])
                setUiCellListeners(uiCells[r][c], r, c)
            }
        }

        if (results != null) {
            clickMode = results.clickMode
        }

        updateUiCellImage()
    }

    private fun setUiCellListeners(cell: UiCell, row: Int, col: Int) {
        // Single tap
        cell.setOnClickListener { v ->
            if (v.tag as Boolean) {
                onUiCellTap(v, row, col, true)
            }
        }

        // Long tap
        cell.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                v.tag = true
            } else if (v.isPressed && v.tag as Boolean) {
                val eventDuration = event.eventTime - event.downTime
                if (eventDuration > UserPrefStorage.getLongPressLength(v.context)) {
                    v.tag = false
                    onUiCellTap(v, row, col, false)
                    Helper.vibrate(v.context)
                }
            }
            false
        }
    }

    fun onUiCellTap(view: View, row: Int, col: Int, shortTap: Boolean) {
        if (shortTap) {
            soundPlayer.play(GameSoundType.TAP)
        } else {
            if (clickMode == ClickMode.FLAG) {
                view.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.puff_in))
            }
            soundPlayer.play(GameSoundType.LONGPRESS)
        }
        if ((shortTap && (clickMode == ClickMode.REVEAL)) || (!shortTap && (clickMode != ClickMode.REVEAL))) {
            minesweeper.revealCell(row, col)
        } else if ((shortTap && (clickMode == ClickMode.FLAG)) || (!shortTap && (clickMode == ClickMode.REVEAL))) {
            minesweeper.flagCell(row, col)
        } else {
            throw IllegalStateException("Unsupported state with clickMode and shortTap")
        }
    }

    /***
     * Scale
     */

    private fun updateUiCellImage() {
        for ((r, rowUiCells) in uiCells.withIndex()) {
            for ((c, _) in rowUiCells.withIndex()) {
                onCellChange(minesweeper.cells[r][c], flagChange = false)
            }
        }
    }

    /***
     * UiHandler
     */

    override fun isSwiftOpenEnabled(): Boolean = isSwiftOpenEnabled

    override fun onSwiftChange() {
        if (isSwiftChangeEnabled) {
            switchClickMode()
        }
    }

    fun switchClickMode() {
        clickMode = if (clickMode == ClickMode.FLAG) ClickMode.REVEAL else ClickMode.FLAG
    }

    override fun onCellChange(cell: Cell, flagChange: Boolean) {
        if (flagChange) {
            boardInfoView.setMineKeeperText(minesweeper.getMinesLeftNumber())
            uiCells[cell.row][cell.column].startAnimation(AnimationUtils.loadAnimation(layout.context, R.anim.puff_in))
        }
        uiCells[cell.row][cell.column].updateImage(cell, clickMode, minesweeper.gameStatus)
    }

    override fun onGameStatusChange(cell: Cell) {
        soundPlayer.play(if (minesweeper.gameStatus == GameStatus.VICTORY) GameSoundType.WIN else GameSoundType.LOSE)
        Helper.vibrate(layout.context)

        updateUiCellImage()
        UserPrefStorage.updateStatsWithGame(layout.context, gameDifficulty, minesweeper)
        if (minesweeper.gameStatus == GameStatus.DEFEAT) {
            uiCells[cell.row][cell.column].updateImageClickedMine()
            minesweeperUiHandler?.onDefeat()
        } else {
            minesweeperUiHandler?.onVictory((minesweeper.getScore() * 1000).toLong(), minesweeper.getTime())
        }
    }

    override fun onTimerTick(gameTime: Long) {
        minesweeperUiHandler?.onGameTimerTick(gameTime, minesweeper.getScore())
    }

    /***
     * Minesweeper
     */

    fun isPlaying(): Boolean = minesweeper.gameStatus == GameStatus.PLAYING

    fun reset(context: Context) {
        if (isPlaying()) {
            UserPrefStorage.updateStatsWithGame(context, gameDifficulty, minesweeper)
        }
        minesweeper.reset()
        clickMode = ClickMode.REVEAL
        boardInfoView.reset(gameDifficulty.getMineCount(layout.context))
        updateUiCellImage()
    }

    fun save(context: Context) {
        UserPrefStorage.saveGame(context, UserPrefStorage.GameStorageData(minesweeper, gameDifficulty, clickMode))
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        Log.d("MinesweeperUI", "being finalize")
        soundPlayer.release()
    }

    /***
     * Timer
     */

    fun resumeTimer() {
        minesweeper.resumeTimer()
    }

    fun pauseTimer() {
        minesweeper.pauseTimer()
    }
}
