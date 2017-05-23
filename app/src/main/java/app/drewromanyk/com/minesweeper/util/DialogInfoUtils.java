package app.drewromanyk.com.minesweeper.util;

import android.content.Context;
import android.util.SparseArray;

import com.squareup.phrase.Phrase;

import app.drewromanyk.com.minesweeper.BuildConfig;
import app.drewromanyk.com.minesweeper.R;
import app.drewromanyk.com.minesweeper.enums.ResultCodes;
import app.drewromanyk.com.minesweeper.models.YesNoDialogInfo;

/**
 * Created by Drew on 9/12/15.
 * DialogInfoUtils
 * Class designed to handle all Dialog information and what the text should be for each dialog
 */
public class DialogInfoUtils {

    private static SparseArray<YesNoDialogInfo> yesNoDialogMap;

    private static DialogInfoUtils instance = null;

    public static DialogInfoUtils getInstance(Context context) {
        if (instance == null) {
            instance = new DialogInfoUtils(context);
        }
        return instance;
    }

    private DialogInfoUtils(Context context) {
        yesNoDialogMap = new SparseArray<>();

        yesNoDialogMap.put(ResultCodes.ABOUT_DIALOG.ordinal(), new YesNoDialogInfo(context.getString(R.string.dialog_about_title),
                Phrase.from(context, R.string.dialog_about_message)
                        .put(PhraseKeys.INSTANCE.getAMOUNT(), BuildConfig.VERSION_NAME)
                        .format()
                        .toString()));

        yesNoDialogMap.put(ResultCodes.HELP_DIALOG.ordinal(), new YesNoDialogInfo(context.getString(R.string.dialog_help_title),
                context.getString(R.string.dialog_help_message)));

        yesNoDialogMap.put(ResultCodes.RESUME_DIALOG.ordinal(), new YesNoDialogInfo(context.getString(R.string.dialog_cancelresume_title),
                context.getString(R.string.dialog_cancelresume_message)));

        yesNoDialogMap.put(ResultCodes.CUSTOMGAMEERROR_DIALOG.ordinal(), new YesNoDialogInfo(context.getString(R.string.dialog_customgameerror_title),
                context.getString(R.string.dialog_customgameerror_message)));

        yesNoDialogMap.put(ResultCodes.TRASH_STATS_DIALOG.ordinal(), new YesNoDialogInfo(context.getString(R.string.dialog_trashstats_title),
                context.getString(R.string.dialog_trashstats_message)));

        yesNoDialogMap.put(ResultCodes.RESTART_DIALOG.ordinal(), new YesNoDialogInfo(context.getString(R.string.dialog_cancelresume_title),
                context.getString(R.string.dialog_cancelresume_message)));

        yesNoDialogMap.put(ResultCodes.NEEDGOOGLE_DIALOG.ordinal(), new YesNoDialogInfo(context.getString(R.string.dialog_needgoogle_title),
                context.getString(R.string.dialog_needgoogle_message)));

        yesNoDialogMap.put(ResultCodes.CUSTOM_SETTING_CHANGE.ordinal(), new YesNoDialogInfo(context.getString(R.string.dialog_custom_settings_change_title),
                context.getString(R.string.dialog_custom_settings_change_message)));
    }

    public YesNoDialogInfo getDialogInfo(int requestCode) {
        return yesNoDialogMap.get(requestCode);
    }
}
