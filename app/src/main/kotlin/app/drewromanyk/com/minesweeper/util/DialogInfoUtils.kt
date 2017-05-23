package app.drewromanyk.com.minesweeper.util

import android.content.Context
import android.util.SparseArray

import com.squareup.phrase.Phrase

import app.drewromanyk.com.minesweeper.BuildConfig
import app.drewromanyk.com.minesweeper.R
import app.drewromanyk.com.minesweeper.enums.ResultCodes
import app.drewromanyk.com.minesweeper.models.YesNoDialogInfo

/**
 * Created by Drew on 9/12/15.
 * DialogInfoUtils
 * Class designed to handle all Dialog information and what the text should be for each dialog
 */
class DialogInfoUtils private constructor(context: Context) {

    companion object {

        private val yesNoDialogMap: SparseArray<YesNoDialogInfo> = SparseArray<YesNoDialogInfo>()

        private var instance: DialogInfoUtils? = null

        fun getInstance(context: Context): DialogInfoUtils {
            if (instance == null) {
                instance = DialogInfoUtils(context)
            }
            return instance as DialogInfoUtils
        }
    }

    init {

        yesNoDialogMap.put(ResultCodes.ABOUT_DIALOG.ordinal, YesNoDialogInfo(context.getString(R.string.dialog_about_title),
                Phrase.from(context, R.string.dialog_about_message)
                        .put(PhraseKeys.AMOUNT, BuildConfig.VERSION_NAME)
                        .format()
                        .toString()))

        yesNoDialogMap.put(ResultCodes.HELP_DIALOG.ordinal, YesNoDialogInfo(context.getString(R.string.dialog_help_title),
                context.getString(R.string.dialog_help_message)))

        yesNoDialogMap.put(ResultCodes.RESUME_DIALOG.ordinal, YesNoDialogInfo(context.getString(R.string.dialog_cancelresume_title),
                context.getString(R.string.dialog_cancelresume_message)))

        yesNoDialogMap.put(ResultCodes.CUSTOMGAMEERROR_DIALOG.ordinal, YesNoDialogInfo(context.getString(R.string.dialog_customgameerror_title),
                context.getString(R.string.dialog_customgameerror_message)))

        yesNoDialogMap.put(ResultCodes.TRASH_STATS_DIALOG.ordinal, YesNoDialogInfo(context.getString(R.string.dialog_trashstats_title),
                context.getString(R.string.dialog_trashstats_message)))

        yesNoDialogMap.put(ResultCodes.RESTART_DIALOG.ordinal, YesNoDialogInfo(context.getString(R.string.dialog_cancelresume_title),
                context.getString(R.string.dialog_cancelresume_message)))

        yesNoDialogMap.put(ResultCodes.NEEDGOOGLE_DIALOG.ordinal, YesNoDialogInfo(context.getString(R.string.dialog_needgoogle_title),
                context.getString(R.string.dialog_needgoogle_message)))

        yesNoDialogMap.put(ResultCodes.CUSTOM_SETTING_CHANGE.ordinal, YesNoDialogInfo(context.getString(R.string.dialog_custom_settings_change_title),
                context.getString(R.string.dialog_custom_settings_change_message)))
    }

    fun getDialogInfo(requestCode: Int): YesNoDialogInfo {
        return yesNoDialogMap.get(requestCode)
    }
}
