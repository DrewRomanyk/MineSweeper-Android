package app.drewromanyk.com.minesweeper.views

import android.content.Context
import android.support.v7.widget.AppCompatImageButton
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.ClickMode
import app.drewromanyk.com.minesweeper.enums.GameStatus
import app.drewromanyk.com.minesweeper.models.Cell
import app.drewromanyk.com.minesweeper.util.UserPrefStorage

/**
 * Created by Drew Romanyk on 5/19/17.
 * The UI for the cell component
 */
class UiCell(context: Context) : AppCompatImageButton(context) {
    companion object {
        private val PACKAGE_NAME = "app.drewromanyk.com.minesweeper"
    }

    fun updateSize(zoomCellScale: Double) {
        val densityScale = (context.resources.displayMetrics.density / 3).toDouble()
        val cellSize = (1.0 * UserPrefStorage.getCellSize(context) / 100 * 100 * zoomCellScale * densityScale).toInt()
        layoutParams.width = cellSize
        layoutParams.height = cellSize
        maxWidth = cellSize
        minimumWidth = cellSize
        maxHeight = cellSize
        minimumHeight = cellSize
    }

    fun updateImage(cell: Cell, clickMode: ClickMode, gameStatus: GameStatus) {
        val uiThemeMode = UserPrefStorage.getUiThemeMode(context)
        val lightMode = uiThemeMode.isLightMode()
        val materialMode = uiThemeMode.isMaterialMode()

        var id = if (materialMode) {
            if (lightMode) R.drawable.ic_cell_unknown_light else R.drawable.ic_cell_unknown
        } else {
            R.drawable.ic_cell_unknown_classical
        }

        if (!gameStatus.isGameOver() && cell.isUnknown() && (clickMode == ClickMode.FLAG)) {
            id = if (materialMode)
                if (lightMode) R.drawable.ic_cell_unknownflag_light else R.drawable.ic_cell_unknownflag
            else
                R.drawable.ic_cell_unknown_classical
        } else if (cell.isFlagged()) {
            id = if (materialMode)
                if (lightMode) R.drawable.ic_cell_flag_light else R.drawable.ic_cell_flag
            else
                R.drawable.ic_cell_unknownflag_classical
        } else if (cell.isRevealed() && (cell.value >= 0)) {
            if (!materialMode) {
                id = context.resources.getIdentifier(
                        "ic_cell_${cell.value}_classical", "drawable", PACKAGE_NAME)
            } else if (lightMode) {
                id = context.resources.getIdentifier(
                        "ic_cell_${cell.value}_light", "drawable", PACKAGE_NAME)
            } else {
                id = context.resources.getIdentifier(
                        "ic_cell_${cell.value}", "drawable", PACKAGE_NAME)
            }
            if (cell.isEmpty()) {
                id = if (materialMode)
                    android.R.color.transparent
                else
                    R.drawable.ic_cell_0_classical
            }
        } else if ((gameStatus.isGameOver()) && cell.isFlagged() && cell.isMine()) {
            id = if (materialMode)
                if (lightMode) R.drawable.ic_cell_bombflagged_light else R.drawable.ic_cell_bombflagged
            else
                R.drawable.ic_cell_bombflagged_classical
        } else if ((gameStatus.isGameOver()) && !cell.isFlagged() && cell.isMine()) {
            id = if (materialMode)
                if (lightMode) R.drawable.ic_cell_bomb_light else R.drawable.ic_cell_bomb
            else
                R.drawable.ic_cell_bomb_classical
        }

        setBackgroundResource(id)
    }

    fun updateImageClickedMine() {
        val uiThemeMode = UserPrefStorage.getUiThemeMode(context)
        val lightMode = uiThemeMode.isLightMode()
        val materialMode = uiThemeMode.isMaterialMode()

        val id = if (materialMode)
            if (lightMode) R.drawable.ic_cell_bombpressed_light else R.drawable.ic_cell_bombpressed
        else
            R.drawable.ic_cell_bombpressed_classical
        setBackgroundResource(id)
    }


}