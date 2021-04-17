package bkav.android.btalk.messaging.ui.searchSms;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by quangnd on 10/06/2017.
 */

public abstract class ClickyViewHolder<DataType> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener  {
    public RecyclerCursorAdapter.ItemClickListener<DataType> mClickListener;
    public DataType mData;
    public Context mContext;

    public ClickyViewHolder(Context context, View itemView) {
        super(itemView);
        mContext = context;
    }

    @Override
    public void onClick(View v) {
        if (mClickListener != null) {
            mClickListener.onItemClick(mData, v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mClickListener != null) {
            mClickListener.onItemLongClick(mData, v);
        }

        return true;
    }
}
