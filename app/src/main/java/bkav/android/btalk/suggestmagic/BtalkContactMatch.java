package bkav.android.btalk.suggestmagic;

import com.google.common.base.Objects;

/**
 * Created by anhdt on 02/11/2017.
 * Data format for finding duplicated contacts.
 */

class BtalkContactMatch {
    private final String mLookupKey;
    private final long mId;
    private final String mNumber;
    private final String mName;

    BtalkContactMatch(String lookupKey, long id, String number, String displayName) {
        this.mLookupKey = lookupKey;
        this.mId = id;
        this.mNumber = number;
        this.mName = displayName;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mLookupKey, mId, mNumber, mName);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof BtalkContactMatch) {
            final BtalkContactMatch that = (BtalkContactMatch) object;
            return (Objects.equal(this.mName, that.mName)
                    && Objects.equal(this.mNumber, this.mNumber)) ||
                    Objects.equal(this.mLookupKey, that.mLookupKey)
                            && Objects.equal(this.mId, that.mId)
                            && Objects.equal(this.mNumber, this.mNumber);
        }
        return false;
    }
}
