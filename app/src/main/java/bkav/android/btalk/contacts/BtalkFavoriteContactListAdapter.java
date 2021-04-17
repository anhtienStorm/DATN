package bkav.android.btalk.contacts;

import android.accounts.Account;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.ContactPresenceIconUtil;
import com.android.contacts.common.ContactStatusUtil;
import com.android.contacts.common.ContactTileLoaderFactory;
import com.android.contacts.common.list.ContactEntry;
import com.android.contacts.common.logging.ScreenEvent;
import com.android.contacts.common.util.ImplicitIntentsUtil;

import java.util.ArrayList;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;

/**
 * Bkav TienNAb: Tao adapter moi cho danh sach danh ba yeu thich
 */
public class BtalkFavoriteContactListAdapter extends RecyclerView.Adapter<BtalkFavoriteContactListAdapter.BtalkFavoriteContactItemViewHolder>{

    ArrayList<ContactEntry> mFavoriteContactList;
    Cursor mCursor;
    Context mContext;

    private int mStarredIndex;
    protected int mIdIndex;
    protected int mLookupIndex;
    protected int mPhotoUriIndex;
    protected int mNameIndex;
    protected int mPresenceIndex;
    protected int mStatusIndex;
    private int mAccountTypeIndex;
    private int mAccountNameIndex;

    public BtalkFavoriteContactListAdapter(Context context){
        mContext = context;
        bindColumnIndices();
    }

    @NonNull
    @Override
    public BtalkFavoriteContactItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View contactItemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.favorite_contact_list_item,viewGroup,false);
        return new BtalkFavoriteContactItemViewHolder(contactItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BtalkFavoriteContactItemViewHolder btalkFavoriteContactItemViewHolder, int i) {
        if (mFavoriteContactList != null){
            btalkFavoriteContactItemViewHolder.bindData(mFavoriteContactList.get(i));
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null){
            return 0;
        }
        if (!cursorIsValid(mCursor)){
            return 0;
        }
        return mCursor.getCount();
    }

    public void setCursor(Cursor c){
        if (c == null || c.isClosed()) {
            return;
        }
        mCursor = c;
        mFavoriteContactList = getFavoriteContactList(c);
        notifyDataSetChanged();
    }

    protected void bindColumnIndices() {
        if (!BtalkActivity.isAndroidQ()){
            mIdIndex = ContactTileLoaderFactory.CONTACT_ID;
            mLookupIndex = ContactTileLoaderFactory.LOOKUP_KEY;
            mPhotoUriIndex = ContactTileLoaderFactory.PHOTO_URI;
            mNameIndex = ContactTileLoaderFactory.DISPLAY_NAME;
            mStarredIndex = ContactTileLoaderFactory.STARRED;
            mPresenceIndex = ContactTileLoaderFactory.CONTACT_PRESENCE;
            mStatusIndex = ContactTileLoaderFactory.CONTACT_STATUS;
            mAccountTypeIndex = ContactTileLoaderFactory.ACCOUNT_TYPE;
            mAccountNameIndex = ContactTileLoaderFactory.ACCOUNT_NAME;
        } else {
            mIdIndex = ContactTileLoaderFactory.CONTACT_ID;
            mLookupIndex = ContactTileLoaderFactory.LOOKUP_KEY;
            mPhotoUriIndex = ContactTileLoaderFactory.PHOTO_URI;
            mNameIndex = ContactTileLoaderFactory.DISPLAY_NAME;
            mStarredIndex = ContactTileLoaderFactory.STARRED;
            mPresenceIndex = ContactTileLoaderFactory.CONTACT_PRESENCE;
            mStatusIndex = ContactTileLoaderFactory.CONTACT_STATUS;
        }
    }

    private static boolean cursorIsValid(Cursor cursor) {
        return cursor != null && !cursor.isClosed();
    }

    protected ArrayList<ContactEntry> getFavoriteContactList(Cursor cursor) {
        ArrayList<ContactEntry> list = new ArrayList<>();

        // If the loader was canceled we will be given a null cursor.
        // In that case, show an empty list of contacts.
        if (!cursorIsValid(cursor)) {
            return null;
        }

        while (cursor.moveToNext()){
            long id = cursor.getLong(mIdIndex);
            String photoUri = cursor.getString(mPhotoUriIndex);
            String lookupKey = cursor.getString(mLookupIndex);

            ContactEntry contact = new ContactEntry();
            String name = cursor.getString(mNameIndex);
            contact.namePrimary = (name != null) ? name : mContext.getResources().getString(com.android.contacts.common.R.string.missing_name);
            contact.status = cursor.getString(mStatusIndex);
            contact.photoUri = (photoUri != null ? Uri.parse(photoUri) : null);
            contact.lookupKey = lookupKey;
            contact.lookupUri = ContentUris.withAppendedId(
                    Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey), id);
            contact.isFavorite = cursor.getInt(mStarredIndex) > 0;

            // Set presence icon and status message
            Drawable icon = null;
            int presence = 0;
            if (!cursor.isNull(mPresenceIndex)) {
                presence = cursor.getInt(mPresenceIndex);
                icon = ContactPresenceIconUtil.getPresenceIcon(mContext, presence);
            }
            contact.presenceIcon = icon;

            String statusMessage = null;
            if (mStatusIndex != 0 && !cursor.isNull(mStatusIndex)) {
                statusMessage = cursor.getString(mStatusIndex);
            }
            // If there is no status message from the contact, but there was a presence value,
            // then use the default status message string
            if (statusMessage == null && presence != 0) {
                statusMessage = ContactStatusUtil.getStatusString(mContext, presence);
            }
            contact.status = statusMessage;

            if (!BtalkActivity.isAndroidQ()){
                if (!cursor.isNull(mAccountTypeIndex) && !cursor.isNull(mAccountTypeIndex)) {
                    final String accountType = cursor.getString(mAccountTypeIndex);
                    final String accountName = cursor.getString(mAccountNameIndex);
                    contact.account = new Account(accountName, accountType);
                } else {
                    contact.account = null;
                }
            } else {
                contact.account = null;
            }

            list.add(contact);
        }

        return list;
    }


    public class BtalkFavoriteContactItemViewHolder extends RecyclerView.ViewHolder{

        ImageView mImageView;
        TextView mTextView;
        ContactPhotoManager mPhotomanager;
        ContactEntry mContactEntry;

        private static final float DEFAULT_IMAGE_LETTER_SCALE = 0.8f;

        public BtalkFavoriteContactItemViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.contact_tile_image);
            mTextView = itemView.findViewById(R.id.contact_tile_name);
        }

        void bindData(ContactEntry contactEntry){
            mContactEntry = contactEntry;
            mPhotomanager = ContactPhotoManager.getInstance(mContext);
            ContactPhotoManager.DefaultImageRequest request = getDefaultImageRequest(contactEntry.namePrimary,
                    contactEntry.lookupKey);
            mTextView.setText(contactEntry.getPreferredDisplayName());
            mPhotomanager.loadPhoto(mImageView,contactEntry.photoUri,contactEntry.account,360,false,false,request);
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDetailContact(contactEntry);
                }
            });
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDetailContact(contactEntry);
                }
            });
        }

        protected ContactPhotoManager.DefaultImageRequest getDefaultImageRequest(String displayName, String lookupKey) {
            return new ContactPhotoManager.DefaultImageRequest(displayName, lookupKey, ContactPhotoManager.TYPE_DEFAULT,
                    DEFAULT_IMAGE_LETTER_SCALE, /* offset = */ 0, /* isCircular = */ true);
        }

        private void showDetailContact(ContactEntry entry) {
            final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(entry.lookupUri,
                    BtalkQuickContactActivity.MODE_FULLY_EXPANDED);
            intent.putExtra(BtalkQuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE, ScreenEvent.ScreenType.FAVORITES);
            ImplicitIntentsUtil.startActivityInApp(mContext, intent);
        }
    }
}
