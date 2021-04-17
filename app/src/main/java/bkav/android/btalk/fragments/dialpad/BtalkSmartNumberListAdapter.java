package bkav.android.btalk.fragments.dialpad;

import android.accounts.Account;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.view.ViewGroup;
import android.widget.QuickContactBadge;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.compat.CompatUtils;
import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.common.util.UriUtils;
import com.android.dialer.list.SmartDialNumberListAdapter;

/**
 * Created by anhdt on 18/05/2017.
 * lop adpter view smart search giao dien phone fragment
 */

public class BtalkSmartNumberListAdapter extends SmartDialNumberListAdapter {

    private boolean mIsKeyBroadShow = true;

    BtalkSmartNumberListAdapter(Context context) {
        super(context);
    }

    /**
     * Anhdts custom lai {@link ContactListItemView}
     */
    protected ContactListItemView newContactListItemView(Context context, boolean mVideoCallingEnabled) {
        return new BtalkContactListItemViewSmart(context, null,
                mVideoCallingEnabled);
    }

    /**
     * Anhdts item view
     */
    @Override
    public ContactListItemView getContactListItemView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        final ContactListItemView view = new BtalkContactListItemViewSmart(context, null);
        view.setIsSectionHeaderEnabled(isSectionHeaderDisplayEnabled());
        view.setAdjustSelectionBoundsEnabled(isAdjustSelectionBoundsEnabled());
        return view;
    }

    public void updateTextColor(boolean dialpadShown) {
        mIsKeyBroadShow = dialpadShown;
        notifyDataSetChanged();
    }

    @Override
    public void bindColorText(ContactListItemView view) {
        if (view instanceof BtalkContactListItemViewSmart) {
            ((BtalkContactListItemViewSmart) view).updateTextColor(mIsKeyBroadShow);
        }
//        view.getPhoneticNameTextView().setAlpha(mIsKeyBroadShow ? 0.8f : 1.0f);
//        view.getNameTextView().setAlpha(mIsKeyBroadShow ? 0.8f : 1.0f);
    }

    @Override
    public int getShortcutCount() {
        return 0;
    }

    /**
     * Anhdts
     * cau hinh khong su dung config bo cac ten va anh phia sau
     */
    protected boolean isConfigNotRemoveView() {
        return true;
    }


    /**
     * Anhdts
     * set onClick cho icon
     */
    @Override
    protected void setOnClickPhotoView(QuickContactBadge onClickPhotoView, long id, String lookupKey) {
        if (onClickPhotoView instanceof BtalkCheckableQuickContactBadge) {
            // Anhdts du lieu calllog truyen vao lookupUri do lookupKey bang null
            if (lookupKey.contains("content")) {
                ((BtalkCheckableQuickContactBadge) onClickPhotoView).setData(id, UriUtils.parseUriOrNull(lookupKey));
            } else {
                ((BtalkCheckableQuickContactBadge) onClickPhotoView).setData(id, lookupKey);
            }
        }
    }

    /**
     * Loads the photo for the quick contact view and assigns the contact uri.
     *
     * @param photoIdColumn     Index of the photo id column
     * @param photoUriColumn    Index of the photo uri column. Optional: Can be -1
     * @param contactIdColumn   Index of the contact id column
     * @param lookUpKeyColumn   Index of the lookup key column
     * @param displayNameColumn Index of the display name column
     * @param accountTypeColume Index of the account type column
     * @param accountNameColume Index of the account name column
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void bindQuickContact(final ContactListItemView view, int partitionIndex,
                                    Cursor cursor, int photoIdColumn, int photoUriColumn, int contactIdColumn,
                                    int lookUpKeyColumn, int displayNameColumn, int accountTypeColume,
                                    int accountNameColume) {
        long photoId = 0;
        if (!cursor.isNull(photoIdColumn)) {
            photoId = cursor.getLong(photoIdColumn);
        }

        Account account = null;
        if (!cursor.isNull(accountTypeColume) && !cursor.isNull(accountNameColume)) {
            final String accountType = cursor.getString(accountTypeColume);
            final String accountName = cursor.getString(accountNameColume);
            account = new Account(accountName, accountType);
        }
        QuickContactBadge quickContact = view.getQuickContact();
        quickContact.assignContactUri(
                getContactUri(partitionIndex, cursor, contactIdColumn, lookUpKeyColumn));
        if (CompatUtils.hasPrioritizedMimeType()) {
            // The Contacts app never uses the QuickContactBadge. Therefore, it is safe to assume
            // that only Dialer will use this QuickContact badge. This means prioritizing the phone
            // mimetype here is reasonable.
            quickContact.setPrioritizedMimeType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        }

        if (photoId != 0 || photoUriColumn == -1) {
            getPhotoLoader().loadThumbnail(quickContact, photoId, account,
                    mDarkTheme, mCircularPhotos, null);
        } else {
            final String photoUriString = cursor.getString(photoUriColumn);
            final Uri photoUri = photoUriString == null ? null : Uri.parse(photoUriString);
            ContactPhotoManager.DefaultImageRequest request = null;
            if (photoUri == null) {
                request = getDefaultImageRequestFromCursor(cursor, displayNameColumn,
                        lookUpKeyColumn);
            }
            getPhotoLoader().loadPhoto(quickContact, photoUri, account, -1,
                    mDarkTheme, mCircularPhotos, request);
        }

        // Anhdts set onClick cho icon contact
        setOnClickPhotoView(quickContact, cursor.getLong(contactIdColumn), cursor.getString(lookUpKeyColumn));
    }

}