package bkav.android.btalk.messaging;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.ui.CursorRecyclerAdapter;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.datamodel.data.QuickResponseData;

/**
 * Created by quangnd on 29/11/2017.
 * adapter chuyen du lieu list respone ra recycler
 */

public class QuickResponseListAdapter extends CursorRecyclerAdapter<QuickResponseListAdapter.QuickResponseListViewHolder> {

    public interface OnQuickResponseListener {
        void onResponseClick(QuickResponseData data);
    }

    private OnQuickResponseListener mHost;
    public QuickResponseListAdapter(Context context, Cursor c, OnQuickResponseListener listener) {
        super(context, c, 0);
        mHost = listener;
    }

    @Override
    public void bindViewHolder(QuickResponseListViewHolder holder, Context context, Cursor cursor) {
        holder.bind(cursor);
    }

    @Override
    public QuickResponseListViewHolder createViewHolder(Context context, ViewGroup parent, int viewType) {
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View itemView = layoutInflater.inflate(R.layout.btalk_quick_respone_list_view, null);
        return new QuickResponseListViewHolder(itemView, context, mHost);
    }

    /**
     * ViewHolder that holds a ConversationListItemView.
     */
    public static class QuickResponseListViewHolder extends RecyclerView.ViewHolder {
        final TextView mResponse;
        final View mView;
        private OnQuickResponseListener mHost;
        private Context mContext;
        public QuickResponseListViewHolder(final View itemView,Context context, OnQuickResponseListener listener) {
            super(itemView);
            mView = itemView;
            mResponse = (TextView) mView.findViewById(R.id.response);
            mContext = context;
            mHost = listener;
        }

        public void bind(Cursor cursor) {
            mResponse.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.QuickResponseColumns.RESPONSE)));
            final QuickResponseData data = new QuickResponseData();
            data.bind(cursor);
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mHost != null) {
                        mHost.onResponseClick(data);
                    }
                }
            });
//            mView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    PopupMenu popupMenu = new PopupMenu(mContext, mView, Gravity.END);
//                    popupMenu.getMenuInflater().inflate(R.menu.btalk_response_long_click_menu
//                            , popupMenu.getMenu());
//                    MenuItem deleteItem = popupMenu.getMenu().findItem(R.id.action_delete);
//                    deleteItem.setVisible(!data.isDefault());
//                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                        @Override
//                        public boolean onMenuItemClick(MenuItem item) {
//                            switch (item.getItemId()) {
//                                case R.id.action_edit:
//                                    showDialogEditResponse(data);
//                                    break;
//                                case R.id.action_delete:
//                                    data.deleteToDb(mContext);
//                                    break;
//                            }
//                            return true;
//                        }
//                        /**Bkav QuangNDb show dialog sua response*/
//                        private void showDialogEditResponse(final QuickResponseData data) {
//                            AlertDialog.Builder newResponseBuilder = new AlertDialog.Builder(mContext);
//                            LayoutInflater inflater = LayoutInflater.from(mContext);
//                            View view = inflater.inflate(R.layout.btalk_dialog_add_or_edit_response, null);
//                            TextView title = (TextView) view.findViewById(R.id.title);
//                            title.setText(data.getResponse());
//                            final EditText mContent = (EditText) view.findViewById(R.id.response);
//                            mContent.setText(data.getResponse());
//                            mContent.setSelection(mContent.length());
//                            newResponseBuilder.setView(view).setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    if (TextUtils.isEmpty(mContent.getText())) {
//                                        Toast.makeText(mContext, R.string.notify_empty_response_content, Toast.LENGTH_SHORT).show();
//                                    }else {
//                                        data.setResponse(mContent.getText().toString());
//                                        data.updateToDb(mContext);
//                                    }
//                                }
//                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                }
//                            });
//                            newResponseBuilder.create().show();
//                        }
//                    });
//                    popupMenu.show();
//                    return true;
//                }
//            });
        }
    }
}
