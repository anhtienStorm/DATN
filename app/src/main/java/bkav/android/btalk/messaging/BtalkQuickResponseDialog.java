package bkav.android.btalk.messaging;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.messaging.datamodel.MessagingContentProvider;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.datamodel.data.QuickResponseData;

/**
 * Created by quangnd on 29/11/2017.
 */

public class BtalkQuickResponseDialog extends DialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, QuickResponseListAdapter.OnQuickResponseListener {

    private RecyclerView mRecyclerView;
    private QuickResponseListAdapter mAdapter;
    private static final int ID_LOAD_RESPONSE = 3;
    private ImageButton mAddQuickResponse;

    public interface OnResponseListener{

        void onResponseClick(QuickResponseData data);
    }

    private OnResponseListener mListener;

    public void setListener(OnResponseListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(ID_LOAD_RESPONSE, null, this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.btalk_dialog_quick_response, null);
        builder.setView(view);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_response);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new QuickResponseListAdapter(getActivity(), null, this);
        mRecyclerView.setAdapter(mAdapter);
        mAddQuickResponse = (ImageButton) view.findViewById(R.id.btn_add_response);
        mAddQuickResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder newResponseBuilder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.btalk_dialog_add_or_edit_response, null);
                final EditText mContent = (EditText) view.findViewById(R.id.response);
                newResponseBuilder.setView(view).setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(mContent.getText())) {
                            Toast.makeText(getActivity(), R.string.notify_empty_response_content, Toast.LENGTH_SHORT).show();
                        }else {
                            final QuickResponseData data = new QuickResponseData(mContent.getText().toString(), false);
                            data.insertToDb(getActivity());
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                newResponseBuilder.create().show();
            }
        });
        return builder.create();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), MessagingContentProvider.QUICK_RESPONSE_URI, null, null, null, "_id desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null) {
            mAdapter.swapCursor(null);
        }
    }

    @Override
    public void onResponseClick(QuickResponseData data) {
        if (mListener != null) {
            mListener.onResponseClick(data);
            dismiss();
        }
    }
}
