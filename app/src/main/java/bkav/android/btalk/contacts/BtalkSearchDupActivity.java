package bkav.android.btalk.contacts;


import android.content.Intent;

import com.android.contacts.activities.SearchDupActivity;
import com.android.contacts.util.DuplicatesUtils;

import java.util.ArrayList;

import bkav.android.btalk.activities.BtalkMergeContactActivity;

/**
 * AnhNDd: activity thực hiện kiểm tra các số bị trùng
 */
public class BtalkSearchDupActivity extends SearchDupActivity {
    @Override
    public Intent createIntentFindDuplicated() {
        return new Intent(BtalkSearchDupActivity.this, BtalkMergeContactActivity.class);
    }

    /**
     * Anhdts
     */
    @Override
    protected boolean checkDuplicate() {
        ArrayList<DuplicatesUtils.MergeContacts> mergePhoneContacts =
                DuplicatesUtils.getMergePhoneContacts();
        if (mergePhoneContacts != null && mergePhoneContacts.size() > 0) {
            return true;
        }
        ArrayList<DuplicatesUtils.MergeContacts> mergeMailContacts =
                DuplicatesUtils.getMergeMailContacts();
        if (mergeMailContacts != null && mergeMailContacts.size() > 0) {
            return true;
        }
        return false;
    }
}
