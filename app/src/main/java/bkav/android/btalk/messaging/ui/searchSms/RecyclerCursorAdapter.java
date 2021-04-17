package bkav.android.btalk.messaging.ui.searchSms;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.HashMap;

/**
 * Created by quangnd on 10/06/2017.
 */

public abstract class RecyclerCursorAdapter<VH extends RecyclerView.ViewHolder, DataType>
        extends RecyclerView.Adapter<VH> {
    public interface ItemClickListener<DataType> {
        void onItemClick(DataType object, View view);

        void onItemLongClick(DataType object, View view);
    }

    public interface MultiSelectListener {
        void onMultiSelectStateChanged(boolean enabled);

        void onItemAdded(long id);

        void onItemRemoved(long id);
    }
    protected Context mContext;
    protected Cursor mCursor;

    public RecyclerCursorAdapter(Context context) {
        mContext = context;
    }

    protected HashMap<Long, DataType> mSelectedItems = new HashMap<>();

    protected ItemClickListener<DataType> mItemClickListener;
    protected RecyclerCursorAdapter.MultiSelectListener mMultiSelectListener;

    public void setItemClickListener(ItemClickListener<DataType> conversationClickListener) {
        mItemClickListener = conversationClickListener;
    }

    public void setMultiSelectListener(RecyclerCursorAdapter.MultiSelectListener multiSelectListener) {
        mMultiSelectListener = multiSelectListener;
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }

    }

    public Cursor swapCursor(Cursor cursor) {
        if (mCursor == cursor) {
            return null;
        }

        Cursor oldCursor = mCursor;
        mCursor = cursor;
        if (cursor != null) {
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    protected abstract DataType getItem(int position);

    public boolean isInMultiSelectMode() {
        return mSelectedItems.size() > 0;
    }

    public HashMap<Long, DataType> getSelectedItems() {
        return mSelectedItems;
    }

    public void disableMultiSelectMode(boolean requestCallback) {
        if (isInMultiSelectMode()) {
            mSelectedItems.clear();
            notifyDataSetChanged();

            if (requestCallback && mMultiSelectListener != null) {
                mMultiSelectListener.onMultiSelectStateChanged(false);
            }
        }
    }

    public boolean isSelected(long threadId) {
        return mSelectedItems.containsKey(threadId);
    }

    public void setSelected(long threadId, DataType object) {
        if (!mSelectedItems.containsKey(threadId)) {
            mSelectedItems.put(threadId, object);
            notifyDataSetChanged();

            if (mMultiSelectListener != null) {
                mMultiSelectListener.onItemAdded(threadId);

                if (mSelectedItems.size() == 1) {
                    mMultiSelectListener.onMultiSelectStateChanged(true);
                }
            }
        }
    }

    public void setUnselected(long threadId) {
        if (mSelectedItems.containsKey(threadId)) {
            mSelectedItems.remove(threadId);
            notifyDataSetChanged();

            if (mMultiSelectListener != null) {
                mMultiSelectListener.onItemRemoved(threadId);

                if (mSelectedItems.size() == 0) {
                    mMultiSelectListener.onMultiSelectStateChanged(false);
                }
            }
        }
    }

    public void toggleSelection(long threadId, DataType object) {
        if (isSelected(threadId)) {
            setUnselected(threadId);
        } else {
            setSelected(threadId, object);
        }
    }

    @Override
    public int getItemCount() {
        return (mCursor != null && !mCursor.isClosed()) ? mCursor.getCount() : 0;
    }
}
