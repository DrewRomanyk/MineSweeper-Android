package app.drewromanyk.com.minesweeper.views

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.widget.GridLayout
import app.drewromanyk.com.minesweeper.enums.ClickMode
import app.drewromanyk.com.minesweeper.enums.GameDifficulty
import app.drewromanyk.com.minesweeper.enums.GameSoundType
import app.drewromanyk.com.minesweeper.enums.GameStatus
import app.drewromanyk.com.minesweeper.interfaces.MinesweeperHandler
import app.drewromanyk.com.minesweeper.models.Cell
import app.drewromanyk.com.minesweeper.models.Minesweeper
import app.drewromanyk.com.minesweeper.util.Helper
import app.drewromanyk.com.minesweeper.util.SoundPlayer
import app.drewromanyk.com.minesweeper.util.UserPrefStorage

/**
 * Created by drewromanyk on 5/18/17.
 */
class MinesweeperUI(gameDifficulty: GameDifficulty, private val boardInfoView: BoardInfoView, context: Context, private val onGameTimerTick: (Long, Double) -> Unit, private val onFlagChange: () -> Unit) : MinesweeperHandler {
    companion object {
        private val MIN_SCALE = .4
        private val MAX_SCALE = 2.0
    }

    val gameDifficulty: GameDifficulty
    private var minesweeper: Minesweeper
    var clickMode: ClickMode = ClickMode.REVEAL
        private set(value) {
            field = value
            updateUiCellImage()
            onFlagChange()
        }
    private var zoomCellScale: Double = 1.0

    val layout: GridLayout = GridLayout(context)
    private val uiCells: Array<Array<UiCell>>
    private val soundPlayer: SoundPlayer = SoundPlayer(context)

    // Settings
    private val isSwiftOpenEnabled = UserPrefStorage.getSwiftOpen(context)
    private val isSwiftChangeEnabled = UserPrefStorage.getSwiftChange(context)

    init {
        this.gameDifficulty = gameDifficulty
        minesweeper = Minesweeper(gameDifficulty.getRows(context), gameDifficulty.getColumns(context), gameDifficulty.getMineCount(context), this)
        uiCells = Array(gameDifficulty.getRows(context)) { Array(gameDifficulty.getColumns(context)) { UiCell(context) } }

        boardInfoView.setMineKeeperText(gameDifficulty.getMineCount(context))

        layout.rowCount = uiCells.size
        layout.columnCount = uiCells[0].size
        layout.setPadding(20, 20, 20, 20)

        for (r in 0 until layout.rowCount) {
            for (c in 0 until layout.columnCount) {
                layout.addView(uiCells[r][c])
                setUiCellListeners(uiCells[r][c], r, c)
            }
        }

        updateUiCellSize()
        updateUiCellImage()
    }

    private fun setUiCellListeners(cell: UiCell, row: Int, col: Int) {
        // Single tap
        cell.setOnClickListener { v ->
            Log.v("clickListener", "outside")
            if (v.tag as Boolean) {
                Log.v("clickListener", "inside")
                onUiCellTap(row, col, true)
            }
        }

        // Long tap
        cell.setOnTouchListener { v, event ->
            Log.v("touchListener", "outside")
            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.v("touchListener", "action down")
                v.tag = true
            } else if (v.isPressed && v.tag as Boolean) {
                Log.v("touchListener", "pressed and tag")
                val eventDuration = event.eventTime - event.downTime
                if (eventDuration > UserPrefStorage.getLongPressLength(v.context)) {
                    v.tag = false
                    onUiCellTap(row, col, false)
                    Helper.vibrate(v.context)
                }
            }
            false
        }
    }

    fun onUiCellTap(row: Int, col: Int, shortTap: Boolean) {
        Log.v("onUiCellTap", "$row $col $shortTap")
        if ((shortTap && clickMode == ClickMode.REVEAL) || !shortTap && clickMode != ClickMode.REVEAL) {
            minesweeper.revealCell(row, col)
        } else if ((shortTap && clickMode == ClickMode.FLAG) || (!shortTap && clickMode == ClickMode.REVEAL)) {
            minesweeper.flagCell(row, col)
        } else {
            throw IllegalStateException("Unsupported state with clickMode and shortTap")
        }
    }

    /***
     * Scale
     */

    fun zoomIn(fully: Boolean) {
        zoomCellScale = if (fully) MAX_SCALE else zoomCellScale + .2

        if (zoomCellScale > MAX_SCALE) zoomCellScale = MAX_SCALE
        updateUiCellSize()
    }

    fun zoomOut(fully: Boolean) {
        zoomCellScale = if (fully) MIN_SCALE else zoomCellScale - .2

        if (zoomCellScale < MIN_SCALE) zoomCellScale = MIN_SCALE
        updateUiCellSize()
    }

    private fun updateUiCellSize() {
        for (rowUiCells in uiCells) {
            for (uiCell in rowUiCells) {
                uiCell.updateSize(zoomCellScale)
            }
        }
    }

    private fun updateUiCellImage() {
        for ((r, rowUiCells) in uiCells.withIndex()) {
            for ((c, uiCell) in rowUiCells.withIndex()) {
                onCellChange(minesweeper.cells[r][c])
            }
        }
    }

    /***
     * UiHandler
     */

    override fun isSwiftOpenEnabled(): Boolean = isSwiftOpenEnabled

    override fun onSwiftChange() {
        Log.v("onSwiftChange", "going...")
        if (isSwiftChangeEnabled) {
            Log.v("onSwiftChange", "is Enabled")
            switchClickMode()
        }
    }

    fun switchClickMode() {
        clickMode = if (clickMode == ClickMode.FLAG) ClickMode.REVEAL else ClickMode.FLAG
    }

    override fun onCellChange(cell: Cell) {
        uiCells[cell.row][cell.column].updateImage(cell, clickMode, minesweeper.gameStatus)
    }

    override fun onGameStatusChange(cell: Cell) {
        soundPlayer.play(if (minesweeper.gameStatus == GameStatus.VICTORY) GameSoundType.WIN else GameSoundType.LOSE)
        Helper.vibrate(layout.context)

        updateUiCellImage()
        if (minesweeper.gameStatus == GameStatus.DEFEAT) {
            uiCells[cell.row][cell.column].updateImageClickedMine()
        }
    }

    override fun onNewTimerTick(gameTime: Long) {
        onGameTimerTick(gameTime, minesweeper.getScore())
    }

    /***
     * Minesweeper
     */

    fun isPlaying(): Boolean = minesweeper.gameStatus == GameStatus.PLAYING

    fun reset() {
        minesweeper.reset()
        clickMode = ClickMode.REVEAL
        updateUiCellImage()
    }

    protected fun finalize() {
        soundPlayer.release()
    }
}
