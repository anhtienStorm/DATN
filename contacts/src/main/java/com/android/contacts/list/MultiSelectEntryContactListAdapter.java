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

package com.android.contacts.list;

import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.common.list.DefaultContactListAdapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;

import java.util.TreeSet;

/**
 * An extension of the default contact adapter that adds checkboxes and the ability
 * to select multiple contacts.
 */
public class MultiSelectEntryContactListAdapter extends DefaultContactListAdapter {

    /*private*/protected SelectedContactsListener mSelectedContactsListener;
    /*private*/protected TreeSet<Long> mSelectedContactIds = new TreeSet<Long>();
    /*private*/protected boolean mDisplayCheckBoxes;

    public interface SelectedContactsListener {
        void onSelectedContactsChanged();
        void onSelectedContactsChangedViaCheckBox();
    }

    public MultiSelectEntryContactListAdapter(Context context) {
        super(context);
    }

    public void setSelectedContactsListener(SelectedContactsListener listener) {
        mSelectedContactsListener = listener;
    }

    /**
     * Returns set of selected contacts.
     */
    public TreeSet<Long> getSelectedContactIds() {
        return mSelectedContactIds;
    }

    /**
     * Update set of selected contacts. This changes which checkboxes are set.
     */
    public void setSelectedContactIds(TreeSet<Long> selectedContactIds) {
        this.mSelectedContactIds = selectedContactIds;
        notifyDataSetChanged();
        if (mSelectedContactsListener != null) {
            mSelectedContactsListener.onSelectedContactsChanged();
        }
    }

    /**
     * Shows checkboxes beside contacts if {@param displayCheckBoxes} is {@code TRUE}.
     * Not guaranteed to work with all configurations of this adapter.
     */
    public void setDisplayCheckBoxes(boolean showCheckBoxes) {
        if (!mDisplayCheckBoxes && showCheckBoxes) {
            setSelectedContactIds(new TreeSet<Long>());
        }
        mDisplayCheckBoxes = showCheckBoxes;
        notifyDataSetChanged();
        if (mSelectedContactsListener != null) {
            mSelectedContactsListener.onSelectedContactsChanged();
        }
    }

    /**
     * Checkboxes are being displayed beside contacts.
     */
    public boolean isDisplayingCheckBoxes() {
        return mDisplayCheckBoxes;
    }

    /**
     * Toggle the checkbox beside the contact for {@param contactId}.
     */
    public void toggleSelectionOfContactId(long contactId) {
        if (mSelectedContactIds.contains(contactId)) {
            mSelectedContactIds.remove(contactId);
        } else {
            mSelectedContactIds.add(contactId);
        }
        notifyDataSetChanged();
        if (mSelectedContactsListener != null) {
            mSelectedContactsListener.onSelectedContactsChanged();
        }
    }

    @Override
    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        super.bindView(itemView, partition, cursor, position);
        final ContactListItemView view = (ContactListItemView) itemView;
        bindCheckBox(view, cursor, position, partition == ContactsContract.Directory.DEFAULT);
    }

    private void bindCheckBox(ContactListItemView view, Cursor cursor, int position,
            boolean isLocalDirectory) {

        // Bkav HuyNQN fix loi khong bam duoc vao item Contact
        view.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        // Disable clicking on the ME profile and all contacts from remote directories
        // when showing check boxes. We do this by telling the view to handle clicking itself.
        view.setClickable((/*position == 0*//*AnhNDd: custom*/isPositionProfile(position) && hasProfile() || !isLocalDirectory)
                && mDisplayCheckBoxes);
        //AnhNDd: them doan code nay vi giong nhau nen khong can tach ra
        view.setLongClickable((isPositionProfile(position) && hasProfile() || !isLocalDirectory)
                && mDisplayCheckBoxes);

        // Only show checkboxes if mDisplayCheckBoxes is enabled. Also, never show the
        // checkbox for the Me profile entry and other directory contacts except local directory.
        if (/*position == 0*//*AnhNDd: custom*/isPositionProfile(position) && hasProfile() || !mDisplayCheckBoxes || !isLocalDirectory) {
            view.hideCheckBox();

            //AnhNDd: custom
            hideCheckableImage(view);
            return;
        }
        //final CheckBox checkBox = view.getCheckBox();
        final long contactId = cursor.getLong(ContactQuery.CONTACT_ID);
        /*checkBox.setChecked(mSelectedContactIds.contains(contactId));
        checkBox.setTag(contactId);
        checkBox.setOnClickListener(mCheckBoxClickListener);*/

        //AnhNDd:an checkbox
        bindCheckBoxORCheckImage(view,contactId);
    }

    private final OnClickListener mCheckBoxClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final CheckBox checkBox = (CheckBox) v;
            final Long contactId = (Long) checkBox.getTag();
            if (checkBox.isChecked()) {
                mSelectedContactIds.add(contactId);
            } else {
                mSelectedContactIds.remove(contactId);
            }
            if (mSelectedContactsListener != null) {
                mSelectedContactsListener.onSelectedContactsChangedViaCheckBox();
            }
        }
    };

    //=================================BKAV=========================

    /**
     * AnhNDd: hàm dủng để ẩn phím mũi tên trong itemview của contact.
     * @param itemView
     */
    public void needHideContactViewDetails(ContactListItemView itemView) {

    }


    /**
     * AnhNDd: Hàm custom để xem mình sử dụng checbox hay là checkable image.
     */
    public void bindCheckBoxORCheckImage(ContactListItemView view, long contactId){
        final CheckBox checkBox = view.getCheckBox();
        checkBox.setChecked(mSelectedContactIds.contains(contactId));
        checkBox.setTag(contactId);
        checkBox.setOnClickListener(mCheckBoxClickListener);
    }

    /**
     *AnhNDd: thực hiện đưa item về trạng thái ban đầu.
     */
    public void hideCheckableImage(ContactListItemView view){

    }

    /**
     * AnhNDd: kiểm tra xem position có phải là vị trí của profile hay không.
     * @param position
     * @return
     */
    public boolean isPositionProfile(int position){
        return position == 0;
    }
}
