package bkav.android.btalk.suggestmagic;

import com.android.dialer.database.DialerDatabaseHelper;

/**
 * Created by anhdt on 02/11/2017.
 * Lop luu lai cac gia tri da hien thi goi y cho nguoi dung
 */

class SuggestContactDetail {

    private String mLookupKey;

    private long mId;

    String mTextQuery;

    int mPosTakeSuggest;

    SuggestContactDetail(DialerDatabaseHelper.ContactMatch contactMatch) {
        mId = contactMatch.id;
        mLookupKey = contactMatch.lookupKey;
    }

    SuggestContactDetail(DialerDatabaseHelper.ContactNumber contactNumber) {
        mId = contactNumber.id;
        mLookupKey = contactNumber.lookupKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SuggestContactDetail) {
            SuggestContactDetail contact = (SuggestContactDetail) obj;
            return mLookupKey.equals(contact.mLookupKey) && mId == contact.mId;
        }
        return false;
    }

    void setMakeSuggest(String textQuery, int posTakeSuggest) {
        mTextQuery = textQuery;
        mPosTakeSuggest = posTakeSuggest;
    }
}
