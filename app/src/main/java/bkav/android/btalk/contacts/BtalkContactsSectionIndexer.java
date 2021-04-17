package bkav.android.btalk.contacts;


import com.android.contacts.common.list.ContactsSectionIndexer;

/**
 * AnhNDd class ke thua ContactsSectionIndexer. Dùng để thực hiện việc chỉnh sửa vị trí hiển thị của
 * text pinned.
 */
public class BtalkContactsSectionIndexer extends ContactsSectionIndexer {
    /**
     * Constructor.
     *
     * @param sections a non-null array
     * @param counts   a non-null array of the same size as <code>sections</code>
     */
    public BtalkContactsSectionIndexer(String[] sections, int[] counts) {
        super(sections, counts);
    }

    /**
     * AnhNDd: Đưa title cần hiển thị lên trên cùng.
     *
     * @param tile
     * @param countContacts
     */
    public void setTitleToHeader(String tile, int countContacts) {
        if (mSections != null) {
            // Don't do anything if the header is already set properly.
            if (mSections.length > 0 && tile.equals(mSections[0])) {
                return;
            }

            //AnhNDd: thực hiện đổi vị trí của text lên trên cùng.
            String[] tempSections = new String[mSections.length + 1];
            int[] tempPositions = new int[mPositions.length + 1];
            tempSections[0] = tile;
            tempPositions[0] = 0;
            for (int i = 1; i <= mPositions.length; i++) {
                tempSections[i] = mSections[i - 1];
                tempPositions[i] = mPositions[i - 1] + countContacts;
            }
            mSections = tempSections;
            mPositions = tempPositions;
            mCount += countContacts;
        }
    }
}
