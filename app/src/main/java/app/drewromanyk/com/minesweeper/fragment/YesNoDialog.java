package app.drewromanyk.com.minesweeper.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import app.drewromanyk.com.minesweeper.activities.BaseActivity;

/**
 * Created by drewi_000 on 12/15/2014.
 */
public class YesNoDialog extends DialogFragment {
    protected Activity activity;

    public YesNoDialog() { }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle args = getArguments();
        String title = args.getString("title", "");
        String message = args.getString("message", "");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((BaseActivity)getActivity()).doPositiveClick(getTargetRequestCode());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Look pretty
                    }
                })
                .create();
    }
}
