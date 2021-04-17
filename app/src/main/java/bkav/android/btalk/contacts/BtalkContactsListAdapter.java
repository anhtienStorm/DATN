package bkav.android.btalk.contacts;

import android.accounts.Account;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SectionIndexer;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.compat.ContactsCompat;
import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.common.list.ContactsSectionIndexer;
import com.android.contacts.common.util.PermissionsUtil;
import com.android.contacts.common.util.SearchUtil;
import com.android.contacts.common.widget.CheckableImageView;
import com.android.dialer.calllog.FastScroller;

import java.util.HashMap;
import java.util.TreeSet;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.messaging.custom_view.CursorRecyclerViewAdapter;

/**
 * Bkav TienNAb: Tao adapter moi cho recyclerview danh ba
 */
public class BtalkContactsListAdapter extends CursorRecyclerViewAdapter<BtalkContactsListAdapter.BtalkContactsListViewHolder> implements BtalkContactListItemView.ContactListItemListener, FastScroller.BubbleTextGetter {

    private OnBtalkContactListItemViewListener mListener;
    private OnBtalkItemListener mItemListener;
    private SectionIndexer mIndexer;
    private IOnClickMessageButton mOnClickMessageButton;
    private Placement mPlacementCache = new Placement();
    private int mPositionUserProfile = -1;      // Bkav TienNAb: vi tri hien thi cua contact user
    private boolean mIsShowPhoneNumber = false;
    private boolean mProfileExists;     // Bkav TienNAb: check xem co ton tai contact user khong
    private boolean mSearchMode;
    private String mQueryString;
    private int mDisplayOrder;      // Bkav TienNAb: che do hien thi (Hien thi ten truoc hay ho truoc)

    protected TreeSet<Long> mSelectedContactIds = new TreeSet<Long>();      // Bkav TienNAb: danh sach cac lien he dang duoc chon
    protected SelectedContactsListener mSelectedContactsListener;
    protected boolean mDisplayCheckBoxes;

    // Bkav TienNAb: tao hashmap luu cac cap gia tri (vi tri, header)
    private HashMap<Integer, String> mPositionToHeader = new HashMap<>();


    public BtalkContactsListAdapter(Context context, Cursor c) {
        super(context, null);
    }

    // Bkav TienNAb: tra ve header theo tung vi tri
    @Override
    public String getTextToShowInBubble(int pos) {
        return mPositionToHeader.get(pos);
    }


    public interface OnBtalkContactListItemViewListener {
        void showContactAction(Uri contactLookupUri, boolean isEnterpriseContact);

        default void showDialog(String number, String action) {
        }

        default void actionCall(String number) {
        }
    }

    public interface OnBtalkItemListener {
        void onContactItemClick(Cursor cursor, int position, boolean isEnterpriseContact);

        boolean onContactItemLongClick(Cursor cursor, int position);
    }

    public interface SelectedContactsListener {
        void onSelectedContactsChanged();

        void onSelectedContactsChangedViaCheckBox();
    }

    public interface IOnClickMessageButton{
        void onClickMessageButton();
    }

    public void setListener(OnBtalkContactListItemViewListener listener) {
        mListener = listener;
    }

    public void setItemListener(OnBtalkItemListener listener) {
        mItemListener = listener;
    }

    public void setSelectedContactsListener(SelectedContactsListener listener) {
        mSelectedContactsListener = listener;
    }

    public void setOnclickMessageButton(IOnClickMessageButton onclickMessageButton){
        mOnClickMessageButton = onclickMessageButton;
    }

    @Override
    public void changeCursor(Cursor cursor) {
        updateIndexer(cursor);      // Bkav TienNAb: bind header
        customSetProfileExists(cursor);     // Bkav TienNAb: bind header user contact
        setStarredContactsExists(cursor);   // Bkav TienNAb: bind header yeu thich

        mPositionToHeader.clear();
        if (cursor != null && cursor.getCount() != 0){
            for (int i = 0; i < cursor.getCount(); i++){
                // Bkav TienNAb: luu cac cap gia tri (vi tri, header)
                if(getSectionForPosition(i) > -1) { // Bkav HuyNQN chi thuc hien khi index > -1
                    mPositionToHeader.put(i, (String) getSections()[getSectionForPosition(i)]);
                }
            }
        }

        super.changeCursor(cursor);
    }

    @Override
    public void onBindViewHolder(BtalkContactsListViewHolder viewHolder, Cursor cursor, int position) {
        BtalkContactListItemView btalkContactListItemView = viewHolder.btalkContactListItemView;

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemListener.onContactItemClick(cursor, position, isEnterpriseContact(cursor));
            }
        });

        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mItemListener.onContactItemLongClick(cursor, position);
                return false;
            }
        });

        // Bkav TienNAb: lang nghe su kien click vao icon tin nhan
        btalkContactListItemView.setOnClickMessageButton(new BtalkContactListItemView.IOnClickMessageButton() {
            @Override
            public void onClick() {
                mOnClickMessageButton.onClickMessageButton();
            }
        });

        // Bkav TienNAb: set mau khi text search thay doi
        btalkContactListItemView.setHighlightedPrefix(mSearchMode ? SearchUtil
                .cleanStartAndEndOfSearchQuery(mQueryString.toUpperCase()) : null);

        // Bkav TienNAb: show snippet khi search
        if (mSearchMode){
            // Bkav HienDTk: fix bug - BOS-3363 - start
            // Bkav HienDTk: neu cho hien thi sdt thi luc search khong cho hien thi snippet nua
            if(!mIsShowPhoneNumber)
                // Bkav HienDTk: fix bug - BOS-3363 - End
            btalkContactListItemView.showSnippet(cursor, ContactQuery.CONTACT_SNIPPET);
        } else {
            btalkContactListItemView.setSnippet(null);
        }

        bindNameAndViewId(btalkContactListItemView, cursor);
        bindPhoto(btalkContactListItemView, cursor);
        btalkContactListItemView.getViewDividerSection().setVisibility(View.GONE);
        btalkContactListItemView.setLookupUri(getContactUri(cursor, position), mIsShowPhoneNumber, cursor);
        btalkContactListItemView.setItemListener(this);

        // Bkav TienNAb: show header
        Placement placement = getItemPlacementInSection(position);
        btalkContactListItemView.setIsSectionHeaderEnabled(placement.sectionHeader != null);
        btalkContactListItemView.setSectionHeader(placement.sectionHeader);



        // Bkav TienNAb: sự kiện bấm vào hinh anh để xem thông tin contact.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btalkContactListItemView.getPhotoView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.showContactAction(getContactUri(cursor, position), isEnterpriseContact(cursor));
                    }
                });
                btalkContactListItemView.getPhotoView().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mListener.showContactAction(getContactUri(cursor, position), isEnterpriseContact(cursor));
                        return true;
                    }
                });
            }
        }, 100);

        // Bkav TienNAb: chan click contact user khi o action mode
        viewHolder.itemView.setClickable(!((position == getPositionUserContact()) && hasProfile() && mDisplayCheckBoxes));
        viewHolder.itemView.setLongClickable(!((position == getPositionUserContact()) && hasProfile() && mDisplayCheckBoxes));

        if ((position == getPositionUserContact()) && hasProfile() || !mDisplayCheckBoxes) {
            btalkContactListItemView.hideCheckBox();
            hideCheckableImage(btalkContactListItemView);
            return;
        }

        final long contactId = cursor.getLong(ContactQuery.CONTACT_ID);
        bindCheckBoxORCheckImage(btalkContactListItemView, contactId);
    }

    @NonNull
    @Override
    public BtalkContactsListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        return new BtalkContactsListViewHolder(layoutInflater.inflate(R.layout.btalk_item_contact_recyclerview, null));
    }

    @Override
    public void showDialog(String number, String action) {
        if (mListener != null) {
            mListener.showDialog(number, action);
        }
    }

    @Override
    public void actionCall(String number) {
        if (mListener != null) {
            mListener.actionCall(number);
        }
    }


    public Uri getContactUri(Cursor cursor, int position) {
        if (cursor == null || cursor.isClosed() || !cursor.moveToPosition(position)) {
            return null;
        }
        long contactId = cursor.getLong(ContactQuery.CONTACT_ID);
        String lookupKey = cursor.getString(ContactQuery.CONTACT_LOOKUP_KEY);
        Uri uri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
        return uri;
    }

    // Bkav TienNAb: bind name
    protected void bindNameAndViewId(final ContactListItemView view, Cursor cursor) {
        view.showDisplayName(cursor, ContactQuery.CONTACT_DISPLAY_NAME, mDisplayOrder);
    }

    public void setDisplayOrder(int displayOrder){
        mDisplayOrder = displayOrder;
    }

    // Bkav TienNAb: bind photo
    void bindPhoto(final ContactListItemView view, Cursor cursor) {
//        if (!isPhotoSupported(partitionIndex)) {
//            view.removePhotoView();
//            return;
//        }

        // Set the photo, if available
        long photoId = 0;
        if (!cursor.isNull(ContactQuery.CONTACT_PHOTO_ID)) {
            photoId = cursor.getLong(ContactQuery.CONTACT_PHOTO_ID);
        }

        Account account = null;
        if (!BtalkActivity.isAndroidQ()){
            if (!cursor.isNull(ContactQuery.CONTACT_ACCOUNT_TYPE)
                    && !cursor.isNull(ContactQuery.CONTACT_ACCOUNT_NAME)) {
                final String accountType = cursor.getString(ContactQuery.CONTACT_ACCOUNT_TYPE);
                final String accountName = cursor.getString(ContactQuery.CONTACT_ACCOUNT_NAME);
                account = new Account(accountName, accountType);
            }
        }
        if (photoId != 0) {
            ContactPhotoManager.getInstance(mContext).loadThumbnail(view.getPhotoView(), photoId, account, false,
                    true, null);
        } else {
            final String photoUriString = cursor.getString(ContactQuery.CONTACT_PHOTO_URI);
            final Uri photoUri = photoUriString == null ? null : Uri.parse(photoUriString);
            ContactPhotoManager.DefaultImageRequest request = null;
            if (photoUri == null) {
                request = getDefaultImageRequestFromCursor(cursor,
                        ContactQuery.CONTACT_DISPLAY_NAME,
                        ContactQuery.CONTACT_LOOKUP_KEY);
            }
            ContactPhotoManager.getInstance(mContext).loadDirectoryPhoto(view.getPhotoView(), photoUri, account, false,
                    true, request);
        }
    }

    public ContactPhotoManager.DefaultImageRequest getDefaultImageRequestFromCursor(Cursor cursor,
                                                                                    int displayNameColumn, int lookupKeyColumn) {
        final String displayName = cursor.getString(displayNameColumn);
        final String lookupKey = cursor.getString(lookupKeyColumn);
        return new ContactPhotoManager.DefaultImageRequest(displayName, lookupKey, true);
    }

    public boolean isEnterpriseContact(Cursor cursor) {
        // Bkav HienDTk: fix loi cursor da close roi ma van truy van du lieu
        if (cursor != null && !cursor.isClosed()) {
            final long contactId = cursor.getLong(ContactQuery.CONTACT_ID);
            return ContactsCompat.isEnterpriseContactId(contactId);
        }
        return false;
    }

    // Bkav TienNAb: bind header
    private void updateIndexer(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            setIndexer(null);
            return;
        }
        int countTest = cursor.getCount();
        Bundle bundle = cursor.getExtras();
        if (bundle.containsKey(ContactsContract.Contacts.EXTRA_ADDRESS_BOOK_INDEX_TITLES) &&
                bundle.containsKey(ContactsContract.Contacts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS)) {
            String sections[] =
                    bundle.getStringArray(ContactsContract.Contacts.EXTRA_ADDRESS_BOOK_INDEX_TITLES);
            int counts[] = bundle.getIntArray(
                    ContactsContract.Contacts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS);

//            if (getExtraStartingSection()) {
//                // Insert an additional unnamed section at the top of the list.
//                String allSections[] = new String[sections.length + 1];
//                int allCounts[] = new int[counts.length + 1];
//                for (int i = 0; i < sections.length; i++) {
//                    allSections[i + 1] = sections[i];
//                    allCounts[i + 1] = counts[i];
//                }
//                allCounts[0] = 1;
//                allSections[0] = "";
//                setIndexer(new BtalkContactsSectionIndexer(allSections, allCounts));
//            } else {
            setIndexer(new BtalkContactsSectionIndexer(sections, counts));
//            }
        } else {
            setIndexer(null);
        }
    }

    public void setIndexer(SectionIndexer indexer) {
        mIndexer = indexer;
        mPlacementCache.invalidate();
    }

    public Placement getItemPlacementInSection(int position) {
        if (mPlacementCache.position == position) {
            return mPlacementCache;
        }
        mPlacementCache.position = position;
        int section = getSectionForPosition(position);
        if (section != -1 && getPositionForSection(section) == position) {
            mPlacementCache.firstInSection = true;
            mPlacementCache.sectionHeader = (String) getSections()[section];
        } else {
            mPlacementCache.firstInSection = false;
            mPlacementCache.sectionHeader = null;
        }
        mPlacementCache.lastInSection = (getPositionForSection(section + 1) - 1 == position);
        return mPlacementCache;
    }

    public int getSectionForPosition(int position) {
        if (mIndexer == null) {
            return -1;
        }
        return mIndexer.getSectionForPosition(position);
    }

    public int getPositionForSection(int sectionIndex) {
        if (mIndexer == null) {
            return -1;
        }
        return mIndexer.getPositionForSection(sectionIndex);
    }

    public Object[] getSections() {
        if (mIndexer == null) {
            return new String[]{" "};
        } else {
            return mIndexer.getSections();
        }
    }

    // Bkav TienNAb: bind header "Yêu Thích"
    public void setStarredContactsExists(Cursor cursor) {
        if (mSearchMode) {
            return;
        }
        //AnhNDd: Thực hiện kiểm tra xem có số điện thoại yêu thích hay không.
        if (cursor != null && BtalkProfileAndContactsLoader.getCountStarredContacts() > 0) {
            if (mIndexer != null) {
                ((BtalkContactsSectionIndexer) mIndexer).setTitleToHeader(
                        mContext.getString(R.string.btalk_contactsFavoritesLabel), BtalkProfileAndContactsLoader.getCountStarredContacts());
            }
        }
    }

    public void customSetProfileExists(Cursor cursor) {
        if (mSearchMode) {
            // Bkav TienNAb: Khi dang trong che do search thi khong set vi tri cho user profile
            mPositionUserProfile = -1;
            return;
        }
        if (cursor != null && cursor.moveToFirst()) {
            if (BtalkProfileAndContactsLoader.getCountStarredContacts() > 0 || BtalkProfileAndContactsLoader.getCountFrequentContacts() > 0) {
                //AnhNDd: Thay đổi lại ví trí con trỏ vì có query thêm các số điện thoại khác.
                cursor.move(BtalkProfileAndContactsLoader.getCountStarredContacts() + BtalkProfileAndContactsLoader.getCountFrequentContacts());
            }
            if (cursor.getInt(ContactQuery.CONTACT_IS_USER_PROFILE) == 1) {
                //set vi tri neu co user profile
                mPositionUserProfile = BtalkProfileAndContactsLoader.getCountStarredContacts() + BtalkProfileAndContactsLoader.getCountFrequentContacts();
            } else {
                mPositionUserProfile = -1;
            }
            setProfileExists(cursor.getInt(ContactQuery.CONTACT_IS_USER_PROFILE) == 1);
        }
    }

    // Bkav TienNAb: bind header "Tôi"
    public void setProfileExists(boolean exists) {
        mProfileExists = exists;
        // Stick the "ME" header for the profile
        if (exists) {
            if (mIndexer != null) {
                ((ContactsSectionIndexer) mIndexer).setProfileHeader(
                        mContext.getString(com.android.contacts.common.R.string.user_profile_contacts_list_header));
            }
        }
    }

    public void setShowPhoneNumber(boolean isShowPhoneNumber){
        mIsShowPhoneNumber = isShowPhoneNumber;
        notifyDataSetChanged();
    }

    public void setSearchMode(boolean b){
        mSearchMode = b;
    }

    public void setQueryString(String query){
        mQueryString = query;
    }

    public boolean hasProfile() {
        return mProfileExists;
    }

    public int getPositionUserContact() {
        return mPositionUserProfile;
    }

    public void setSelectedContactIds(TreeSet<Long> selectedContactIds) {
        this.mSelectedContactIds = selectedContactIds;
        notifyDataSetChanged();
        if (mSelectedContactsListener != null) {
            mSelectedContactsListener.onSelectedContactsChanged();
        }
    }

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

    public TreeSet<Long> getSelectedContactIds() {
        return mSelectedContactIds;
    }

    public boolean isDisplayingCheckBoxes() {
        return mDisplayCheckBoxes;
    }

    public void hideCheckableImage(ContactListItemView view) {
        BtalkContactListItemView itemview = (BtalkContactListItemView) view;
        itemview.setClickable(!mDisplayCheckBoxes);
        itemview.setChecked(false);

        CheckableImageView checkableImageView = (CheckableImageView) view.getPhotoView();
        checkableImageView.setChecked(false, false);
        checkableImageView.setTag(null);
        checkableImageView.setOnClickListener(null);
    }

    // Bkav TienNAb: cap nhat giao dien khi contact duoc chon hoac bo chon
    public void bindCheckBoxORCheckImage(ContactListItemView view, long contactId) {
        if (!mDisplayCheckBoxes) {
            return;
        }
        //AnhNDd: Thực hiện ẩn checkbox và nếu là checkbox được click thì thay đổi ảnh và làm mờ item đó.
        if (view.getPhotoView() instanceof CheckableImageView) {
            CheckableImageView checkableImageView = (CheckableImageView) view.getPhotoView();
            checkableImageView.setChecked(mSelectedContactIds.contains(contactId), false);
            checkableImageView.setTag(contactId);

            //AnhNDd: Thay đổi màu nền background itemview.
            BtalkContactListItemView itemview = (BtalkContactListItemView) view;
            itemview.setChecked(mSelectedContactIds.contains(contactId));
            itemview.setClickable(false);
        }
    }


    public static class BtalkContactsListViewHolder extends RecyclerView.ViewHolder {

        BtalkContactListItemView btalkContactListItemView;

        public BtalkContactsListViewHolder(@NonNull View itemView) {
            super(itemView);
            btalkContactListItemView = itemView.findViewById(R.id.item_contact_recyclerview);
        }

    }

    protected static class ContactQuery {
        public static final int CONTACT_ID = 0;
        public static final int CONTACT_DISPLAY_NAME = 1;
        public static final int CONTACT_PRESENCE_STATUS = 2;
        public static final int CONTACT_CONTACT_STATUS = 3;
        public static final int CONTACT_PHOTO_ID = 4;
        public static final int CONTACT_PHOTO_URI = 5;
        public static final int CONTACT_LOOKUP_KEY = 6;
        public static final int CONTACT_IS_USER_PROFILE = 7;
        public static final int CONTACT_PHONETIC_NAME = 8;
        public static final int CONTACT_ACCOUNT_TYPE = 9;
        public static final int CONTACT_ACCOUNT_NAME = 10;
        public static final int CONTACT_LAST_TIME_CONTACTED = !PermissionsUtil.isAndroidQ() ? 11 : 9;
        public static final int CONTACT_STARRED = !PermissionsUtil.isAndroidQ() ? 12 : 10;
        public static final int CONTACT_SNIPPET = !PermissionsUtil.isAndroidQ() ? 13 : 11;
    }

    public static final class Placement {
        private int position = ListView.INVALID_POSITION;
        public boolean firstInSection;
        public boolean lastInSection;
        public String sectionHeader;

        public void invalidate() {
            position = ListView.INVALID_POSITION;
        }
    }
}
