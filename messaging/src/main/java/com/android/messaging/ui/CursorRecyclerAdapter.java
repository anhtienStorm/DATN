/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.messaging.ui;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;

/**
 * Copy of CursorAdapter suited for RecyclerView.
 *
 * TODO: BUG 16327984. Replace this with a framework supported CursorAdapter for
 * RecyclerView when one is available.
 */
public abstract class CursorRecyclerAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private static final int INSERTED = 1;
    private static final int REMOVED = 2;
    private static final int CHANGED = 3;
    private static final int ALL = -1;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected boolean mDataValid;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected boolean mAutoRequery;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected Cursor mCursor;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected Context mContext;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected int mRowIDColumn;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected ChangeObserver mChangeObserver;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected DataSetObserver mDataSetObserver;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected FilterQueryProvider mFilterQueryProvider;

    /**
     * If set the adapter will call requery() on the cursor whenever a content change
     * notification is delivered. Implies {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
     *
     * @deprecated This option is discouraged, as it results in Cursor queries
     * being performed on the application's UI thread and thus can cause poor
     * responsiveness or even Application Not Responding errors.  As an alternative,
     * use {@link android.app.LoaderManager} with a {@link android.content.CursorLoader}.
     */
    @Deprecated
    public static final int FLAG_AUTO_REQUERY = 0x01;

    /**
     * If set the adapter will register a content observer on the cursor and will call
     * {@link #onContentChanged()} when a notification comes in.  Be careful when
     * using this flag: you will need to unset the current Cursor from the adapter
     * to avoid leaks due to its registered observers.  This flag is not needed
     * when using a CursorAdapter with a
     * {@link android.content.CursorLoader}.
     */
    public static final int FLAG_REGISTER_CONTENT_OBSERVER = 0x02;

    /**
     * Recommended constructor.
     *
     * @param c The cursor from which to get the data.
     * @param context The context
     * @param flags Flags used to determine the behavior of the adapter; may
     * be any combination of {@link #FLAG_AUTO_REQUERY} and
     * {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
     */
    public CursorRecyclerAdapter(final Context context, final Cursor c, final int flags) {
        init(context, c, flags);
    }

    void init(final Context context, final Cursor c, int flags) {
        if ((flags & FLAG_AUTO_REQUERY) == FLAG_AUTO_REQUERY) {
            flags |= FLAG_REGISTER_CONTENT_OBSERVER;
            mAutoRequery = true;
        } else {
            mAutoRequery = false;
        }
        final boolean cursorPresent = c != null;
        mCursor = c;
        mDataValid = cursorPresent;
        mContext = context;
        mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
        if ((flags & FLAG_REGISTER_CONTENT_OBSERVER) == FLAG_REGISTER_CONTENT_OBSERVER) {
            mChangeObserver = new ChangeObserver();
            mDataSetObserver = new MyDataSetObserver();
        } else {
            mChangeObserver = null;
            mDataSetObserver = null;
        }

        if (cursorPresent) {
            if (mChangeObserver != null) {
                c.registerContentObserver(mChangeObserver);
            }
            if (mDataSetObserver != null) {
                c.registerDataSetObserver(mDataSetObserver);
            }
        }
    }

    /**
     * Returns the cursor.
     * @return the cursor.
     */
    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    /**
     * @see android.support.v7.widget.RecyclerView.Adapter#getItem(int)
     */
    public Object getItem(final int position) {
        if (mDataValid && mCursor != null) {
            mCursor.moveToPosition(position);
            return mCursor;
        } else {
            return null;
        }
    }

    /**
     * @see android.support.v7.widget.RecyclerView.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(final int position) {
        if (mDataValid && mCursor != null) {
            if (mCursor.moveToPosition(position)) {
                return mCursor.getLong(mRowIDColumn);
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public VH onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return createViewHolder(mContext, parent, viewType);
    }

    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        bindViewHolder(holder, mContext, mCursor);
    }
    /**
     * Bind an existing view to the data pointed to by cursor
     * @param view Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param cursor The cursor from which to get the data. The cursor is already
     * moved to the correct position.
     */
    public abstract void bindViewHolder(VH holder, Context context, Cursor cursor);

    /**
     * @see android.support.v7.widget.RecyclerView.Adapter#createViewHolder(Context, ViewGroup, int)
     */
    public abstract VH createViewHolder(Context context, ViewGroup parent, int viewType);

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     *
     * @param cursor The new cursor to be used
     */
//    public void changeCursor(final Cursor cursor) {
//        final Cursor old = swapCursor(cursor);
//        if (old != null) {
//            old.close();
//        }
//    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     *
     * @param cursor The new cursor to be used
     */
    public Cursor changeCursor(final Cursor cursor) {
        if (mCursor == null) {
            return swapCursor(cursor, null);
        } else {
            SparseIntArray changes = null;

            if (cursor != null && cursor != mCursor && !TextUtils.isEmpty("_id")) {
                changes = diffCursors(mCursor, cursor);
            }
//            Cursor old = swapCursor(cursor, changes);
            return swapCursor(cursor, changes);
//            if (old != null) {
//                old.close();
//            }
        }
    }


    /**
     * Processes two cursors, old/existing cursor and a new cursor, returning a list of indexes who's
     * records were inserted, deleted, or changed
     * @param oldCursor
     * @param newCursor
     * @return
     */
    private SparseIntArray diffCursors(Cursor oldCursor, Cursor newCursor) {

        SparseIntArray changedOrInserted = getChangeOrInsertRecords(oldCursor, newCursor);

        // all records were inserted in new cursor
        if (changedOrInserted.get(ALL) == INSERTED) {
            return changedOrInserted;
        }

        SparseIntArray deleted = getDeletedRecords(oldCursor, newCursor);

        if (deleted.get(ALL) == INSERTED) {
            return deleted;
        }
        SparseIntArray changes = new SparseIntArray(changedOrInserted.size() + deleted.size());

        for (int i = 0; i < changedOrInserted.size(); i++) {
            changes.put(changedOrInserted.keyAt(i), changedOrInserted.valueAt(i));
        }

        for (int i = 0; i < deleted.size(); i++) {
            changes.put(deleted.keyAt(i), deleted.valueAt(i));
        }
        return changes;
    }

    /**
     * Returns an array of indexes who's records were newly inserted or changed
     * Will also return whether or not all the records were inserted or removed
     * @param oldCursor
     * @param newCursor
     * @return
     */
    private SparseIntArray getChangeOrInsertRecords(Cursor oldCursor, Cursor newCursor) {
        SparseIntArray changes = new SparseIntArray();
        int newCursorPosition = newCursor.getPosition();

        if (newCursor.moveToFirst()) {
            int columnIndex = oldCursor.getColumnIndex("_id");
            int cursorIndex = 0;

            // loop
            do {

                if (oldCursor.moveToFirst()) {
                    boolean newRecordFound = false;

                    // loop
                    do {

                        // we found a record match
                        if (oldCursor.getInt(mRowIDColumn) == newCursor.getInt(mRowIDColumn)) {
                            newRecordFound = true;

                            // values are different, this record has changed
                            if (!oldCursor.getString(columnIndex).contentEquals(newCursor.getString(columnIndex))) {
                                changes.put(cursorIndex, CHANGED);
                            }
                            break;
                        }
                    } while (oldCursor.moveToNext());

                    // new record not found in old cursor, it was newly inserted
                    if (!newRecordFound) {
                        changes.put(cursorIndex, INSERTED);
                    }
                    cursorIndex++;
                }

                // unable to move the new cursor, all records in new are inserted
                else {
                    changes.put(ALL, INSERTED);
                    break;
                }
            } while (newCursor.moveToNext());
        }

        // unable to move new cursor to first
        else {
            changes.put(ALL, REMOVED);
        }
        newCursor.moveToPosition(newCursorPosition);
        return changes;
    }

    /**
     * Returns a list of indexes of records that were deleted
     * May also return whether or not ALL records were inserted
     * @param oldCursor
     * @param newCursor
     * @return
     */
    private SparseIntArray getDeletedRecords(Cursor oldCursor, Cursor newCursor) {
        SparseIntArray changes = new SparseIntArray();
        int newCursorPosition = newCursor.getPosition();

        if (oldCursor.moveToFirst()) {
            int cursorIndex = 0;

            // loop old cursor
            do {

                if (newCursor.moveToFirst()) {
                    boolean oldRecordFound = false;

                    // loop new cursor
                    do {

                        // we found a record match
                        if (oldCursor.getInt(mRowIDColumn) == newCursor.getInt(mRowIDColumn)) {
                            oldRecordFound = true;
                            break;
                        }
                    } while(newCursor.moveToNext());

                    if (!oldRecordFound) {
                        changes.put(cursorIndex, REMOVED);
                    }
                    cursorIndex++;
                }

            } while (oldCursor.moveToNext());
        }

        // unable to move the old cursor to the first record, all records in new were adde
        else {
            changes.put(ALL, INSERTED);
        }
        newCursor.moveToPosition(newCursorPosition);
        return changes;
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     *
     * @param newCursor The new cursor to be used.
     * @return Returns the previously set Cursor, or null if there wasa not one.
     * If the given new Cursor is the same instance is the previously set
     * Cursor, null is also returned.
     */
    public Cursor swapCursor(final Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null) {
            if (mChangeObserver != null) {
                oldCursor.unregisterContentObserver(mChangeObserver);
            }
            if (mDataSetObserver != null) {
                oldCursor.unregisterDataSetObserver(mDataSetObserver);
            }
        }
        mCursor = newCursor;
        if (newCursor != null) {
            if (mChangeObserver != null) {
                newCursor.registerContentObserver(mChangeObserver);
            }
            if (mDataSetObserver != null) {
                newCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
            // notify the observers about the lack of a data set
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    /**
     *
     * @param newCursor
     * @param changes
     * @return
     */
    private Cursor swapCursor(Cursor newCursor, SparseIntArray changes) {

        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;

        if (oldCursor != null) {
            if (mChangeObserver != null) {
                oldCursor.unregisterContentObserver(mChangeObserver);
            }
            if (mDataSetObserver != null) {
                oldCursor.unregisterDataSetObserver(mDataSetObserver);
            }
        }
        mCursor = newCursor;

        if (newCursor != null) {

            if (mDataSetObserver != null) {
                newCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
        }

        if (changes != null) {
            // process changes
            if (changes.get(ALL) == INSERTED) {
                notifyItemRangeInserted(newCursor.getCount(), 0);
            } else if (changes.get(ALL) == REMOVED) {
                notifyItemRangeRemoved(newCursor.getCount(), 0);
            } else {

                for (int i = 0; i < changes.size(); i++) {

                    switch (changes.valueAt(i)) {
                        case CHANGED:
                            notifyItemChanged(changes.keyAt(i));
                            break;
                        case INSERTED:
                            notifyItemInserted(changes.keyAt(i));
                            break;
                        case REMOVED:
                            notifyItemRemoved(changes.keyAt(i));
                            break;
                    }
                }
            }
        } else if (mCursor != null) {
            notifyItemRangeInserted(0, mCursor.getCount());
        }
        return oldCursor;
    }

    /**
     * <p>Converts the cursor into a CharSequence. Subclasses should override this
     * method to convert their results. The default implementation returns an
     * empty String for null values or the default String representation of
     * the value.</p>
     *
     * @param cursor the cursor to convert to a CharSequence
     * @return a CharSequence representing the value
     */
    public CharSequence convertToString(final Cursor cursor) {
        return cursor == null ? "" : cursor.toString();
    }

    /**
     * Called when the {@link ContentObserver} on the cursor receives a change notification.
     * The default implementation provides the auto-requery logic, but may be overridden by
     * sub classes.
     *
     * @see ContentObserver#onChange(boolean)
     */
    protected void onContentChanged() {
        if (mAutoRequery && mCursor != null && !mCursor.isClosed()) {
            if (false) {
                Log.v("Cursor", "Auto requerying " + mCursor + " due to update");
            }
            mDataValid = mCursor.requery();
        }
    }

    private class ChangeObserver extends ContentObserver {
        public ChangeObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(final boolean selfChange) {
            onContentChanged();
        }
    }

    private class MyDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            mDataValid = false;
            notifyDataSetChanged();
        }
    }

}