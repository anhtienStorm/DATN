package bkav.android.btalk.messaging.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 24/10/2017.
 */

public class BtalkMissingStoragePermissionDialog extends DialogFragment {

    public interface OnRequestStoragePermission {
        void onRequest();
    }

    private OnRequestStoragePermission mOnRequestStoragePermission;

    public void setOnRequestStoragePermission(OnRequestStoragePermission onRequestStoragePermission) {
        this.mOnRequestStoragePermission = onRequestStoragePermission;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_missing_storage_permission, null);
        builder.setCancelable(false);
        builder.setView(view).setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mOnRequestStoragePermission != null) {
                    mOnRequestStoragePermission.onRequest();
                }
            }
        });
        return builder.create();
    }
}
