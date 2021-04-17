package bkav.android.btalk.messaging.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.lettertiles.LetterTileDrawable;
import com.android.contacts.common.logging.ScreenEvent;
import com.android.contacts.common.util.Constants;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.dialer.contactinfo.ContactInfoCache;
import com.android.messaging.Factory;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.data.ConversationParticipantsData;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.util.AvatarUriUtil;
import com.android.messaging.util.ContactUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import bkav.android.btalk.contacts.BtalkQuickContactActivity;

/**
 * Created by quangnd on 05/04/2017.
 * view icon cua conversationlist custom lai de giong voi ben contact
 */

public class BtalkContactPhotoView extends android.support.v7.widget.AppCompatImageView implements ContactInfoCache.OnContactInfoChangedListener {

    private Context mContext;

    protected long mContactId;

    protected String mContactLookupKey;

    protected String mNormalizedDestination;

    protected String mDisplayName;

    protected Uri mAvatarUri;

    private static final int PARTICIPANT_TYPE = 1;

    private static final int CONVERSATION_TYPE = 2;
    //HienDTk: conversation id
    private String mConversationId = null;


    public BtalkContactPhotoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    /**
     * BKav QuangNDb set uri contact de lay cac thong so tu conversation
     */
    public void setImageResourceUriFromConversation(final Uri uri, final long contactId,
                                                    final String contactLookupKey, final String normalizedDestination
            , final String conversationId, final String displayName) {
        mConversationId = conversationId;
        setImageResourceUri(uri, contactId, contactLookupKey, normalizedDestination, conversationId, displayName, CONVERSATION_TYPE);
    }

    /**
     * BKav QuangNDb set uri contact de lay cac thong so tu participant
     */
    public void setImageResourceUriFromParticipant(final Uri uri, final long contactId,
                                                   final String contactLookupKey, final String normalizedDestination
            , final String participantId, final String displayName) {
        mConversationId = participantId;
        setImageResourceUri(uri, contactId, contactLookupKey, normalizedDestination, participantId, displayName, PARTICIPANT_TYPE);
    }

    /**
     * Bkav QuangNDb set uri contact
     */
    private void setImageResourceUri(final Uri uri, final long contactId,
                                     final String contactLookupKey, final String normalizedDestination
            , final String id, final String displayName, int type) {
        mNormalizedDestination = normalizedDestination;
        mAvatarUri = uri;
        mContactLookupKey = contactLookupKey;
        mContactId = contactId;
        mDisplayName = displayName;
        final String avatarType = AvatarUriUtil.getAvatarType(uri);
        if (AvatarUriUtil.TYPE_GROUP_URI.equals(avatarType)) {
            setGroupDrawables(id);
        } else {
            loadPhoto(type, displayName, id, contactLookupKey);
        }
        maybeInitializeOnClickListener();
    }

    /**
     * Set uri cho photo khi o che do search
     */
    public void setImageSearchResourceUri(final Uri uri, final long contactId,
                                          final String contactLookupKey, final String normalizedDestination, final String id, final String displayName) {
        mNormalizedDestination = normalizedDestination;
        mAvatarUri = uri;
        mContactLookupKey = contactLookupKey;
        mContactId = contactId;
        mDisplayName = displayName;
        final String avatarType = AvatarUriUtil.getAvatarType(uri);
        if (AvatarUriUtil.TYPE_GROUP_URI.equals(avatarType)) {
            setGroupDrawables(id);
        } else {
            String photoProfileUri = null;
            photoProfileUri = getParticipantProfileUriFromConversation(id);
            ContactPhotoManager.DefaultImageRequest imageRequest = null;
            if (photoProfileUri == null) {
                //HienDTk: neu khong co lookupkey thi set anh bang id de dong nhat mau avatar khi sreach
                if(contactLookupKey != null){
                    imageRequest = new ContactPhotoManager.DefaultImageRequest(mDisplayName, contactLookupKey, true);
                }else {
                    imageRequest = new ContactPhotoManager.DefaultImageRequest(mDisplayName, id, true);
                }

            }
            final Uri photoUri = (photoProfileUri == null) ? null : Uri.parse(photoProfileUri);
            ContactPhotoManager.getInstance(mContext).loadDirectoryPhoto(this, photoUri, false, true,
                    imageRequest);
        }

    }

    /**
    * Bkav QuangNDb Set drawable cho icon group
    */
    private void setGroupDrawables(String id) {
        final LetterTileDrawable drawable = new LetterTileDrawable(mContext.getResources(), mContext, null);
        drawable.setLetterAndColorFromContactDetails(null, id);
        drawable.setContactType(LetterTileDrawable.TYPE_GROUP);
        drawable.setScale(1.0f);
        drawable.setOffset(0.0f);
        drawable.setIsCircular(true);
        setImageDrawable(drawable);
    }

    /**
     * Bkav QuangNDb: dau vao la conversaionId
     * dau ra la profile uri cua participant trong conversation do
     */
    private String getParticipantProfileUriFromConversation(String conversationId) {
        final Uri uri = MessagingContentProvider.buildConversationParticipantsUri(conversationId);
        ConversationParticipantsData participantData = new ConversationParticipantsData();
        try (Cursor cursor = mContext.getContentResolver().query(uri, ParticipantData.ParticipantsQuery.PROJECTION, null, null, null)) {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                participantData.bind(cursor);
                if (participantData.getOtherParticipant() != null) {
                    return participantData.getOtherParticipant().getProfilePhotoUri();
                }
            }
        }
        return null;
    }

    /**
     * Bkav QuangNDb goi asynctask de load photo
     */
    protected void loadPhoto(int type, String displayName, String id, String lookupKey) {
        new LoadUriPhoto(this, type, displayName).execute(id, lookupKey);
    }


    public static class LoadUriPhoto extends AsyncTask<String, String, Void> {

        WeakReference<BtalkContactPhotoView> mReference;
        String mDisplayName;
        String mLookupKey;
        String mId;
        int mType;

        public LoadUriPhoto(BtalkContactPhotoView reference, int type, String disPlayName) {
            mReference = new WeakReference<>(reference);
            mType = type;
            mDisplayName = disPlayName;
        }

        @Override
        protected Void doInBackground(String... params) {
            mId = params[0];
            mLookupKey = params[1];
            String photoProfileUri = null;
            if (mReference != null) {
                switch (mType) {
                    case PARTICIPANT_TYPE:
                        photoProfileUri = getParticipantProfileUriFromParticipant(mId);
                        break;
                    case CONVERSATION_TYPE:
                        photoProfileUri = getParticipantProfileUriFromConversation(mId);
                        break;
                    default:
                        photoProfileUri = null;
                }
            }
            publishProgress(photoProfileUri);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            updateUi(values);

        }

        void updateUi(String[] values) {
            // Anhdts check reference null
            if (mReference != null && mReference.get() != null) {
                String photoProfileUri = values[0];
                ContactPhotoManager.DefaultImageRequest request = null;
                if (photoProfileUri == null) {
                    //HienDTk: khong co lookupkey thi dung id de dong nhat mau avatar
                    if(mLookupKey != null){
                        request = new ContactPhotoManager.DefaultImageRequest(mDisplayName, mLookupKey, true);
                    }else {
                        request = new ContactPhotoManager.DefaultImageRequest(mDisplayName, mId, true);
                    }
                }
                Uri photoUri = (photoProfileUri == null) ? null : Uri.parse(photoProfileUri);
                ContactPhotoManager.getInstance(mReference.get().mContext).loadDirectoryPhoto(mReference.get(), photoUri, false, true,
                        request);
            }
        }

        /**
         * Bkav QuangNDb: dau vao la participantId
         * dau ra la profile uri cua participant do
         */
        private String getParticipantProfileUriFromParticipant(String participantId) {
            if (mReference != null && mReference.get() != null) {
                final Uri uri = MessagingContentProvider.PARTICIPANTS_URI;
                ParticipantData participantData;
                Context context = mReference.get().mContext;
                try (Cursor cursor = context.getContentResolver().query(uri, ParticipantData.ParticipantsQuery.PROJECTION,
                        DatabaseHelper.ParticipantColumns._ID + " = '" + participantId + "'", null, null)) {
                    if (cursor != null && cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        participantData = ParticipantData.getFromCursor(cursor);
                        return participantData.getProfilePhotoUri();
                    }
                }
            }

            return null;
        }

        /**
         * Bkav QuangNDb: dau vao la conversaionId
         * dau ra la profile uri cua participant trong conversation do
         */
        private String getParticipantProfileUriFromConversation(String conversationId) {
            if (mReference != null && mReference.get() != null) {
                final Uri uri = MessagingContentProvider.buildConversationParticipantsUri(conversationId);
                ConversationParticipantsData participantData = new ConversationParticipantsData();
                Context context = mReference.get().mContext;
                try (Cursor cursor = context.getContentResolver().query(uri, ParticipantData.ParticipantsQuery.PROJECTION, null, null, null)) {
                    if (cursor != null && cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        participantData.bind(cursor);
                        if (participantData.getOtherParticipant() != null) {
                            return participantData.getOtherParticipant().getProfilePhotoUri();
                        }
                    }
                }
            }
            return null;
        }
    }

    /**
     * Bkav QuangNDb: ham init click cua icon contact
     */
    protected void maybeInitializeOnClickListener() {
        if ((mContactId > ParticipantData.PARTICIPANT_CONTACT_ID_NOT_RESOLVED
                && !TextUtils.isEmpty(mContactLookupKey)) ||
                !TextUtils.isEmpty(mNormalizedDestination)) {
            setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    Uri lookupUri = getLookupUri();
                    if (lookupUri != null) {
//                        ContactsContract.QuickContact.showQuickContact(view.getContext(), view, lookupUri,
//                                ContactsContract.QuickContact.MODE_LARGE, null);
                        // Bkav QuangNDb sua cach goi activity QuickContact set package de app khac khong bat duc intent nay
                        final Intent intentGo = ImplicitIntentsUtil.composeQuickContactIntent(
                                lookupUri, ContactsContract.QuickContact.MODE_LARGE);
                        intentGo.putExtra(BtalkQuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE, ScreenEvent.ScreenType.UNKNOWN);
                        //HienDTk: truyen conversationId sang de custom mau avatar
                        intentGo.putExtra(BtalkQuickContactActivity.CONVERSATION_ID, mConversationId);
                        ImplicitIntentsUtil.startActivityInApp(Factory.get().getApplicationContext(), intentGo);
                    } else {
                        ContactUtil.showOrAddContact(view, mContactId, mContactLookupKey,
                                mAvatarUri, mNormalizedDestination);
                    }
                }
            });
        } else {
            // This should happen when the phone number is not in the user's contacts or it is a
            // group conversation, group conversations don't have contact phone numbers. If this
            // is the case then absorb the click to prevent propagation.
            setOnClickListener(null);
        }

    }

    /**
     * Bkav QuangNDb ham lay lookup uri tu cac du lieu truyen vao
     */
    private Uri getLookupUri() {
        Uri lookupUri;
        if (mContactId > ParticipantData.PARTICIPANT_CONTACT_ID_NOT_RESOLVED
                && !TextUtils.isEmpty(mContactLookupKey)) {
            lookupUri =
                    ContactsContract.Contacts.getLookupUri(mContactId, mContactLookupKey);
        } else {//Bkav QuangNDb Sua logic de tao temp lookup uri cho ca cac sdt brand
            lookupUri = createTemporaryContactUri(mNormalizedDestination);
        }
        return lookupUri;
    }

    @Override
    public void onContactInfoChanged() {
        //Bkav QuangNDb khong lam gi
    }

    /**
     * Bkav QuangNDb ham tao temporary lookup uri cho 1 sdt khong co trong danh ba
     * Creates a JSON-encoded lookup uri for a unknown number without an associated contact
     *
     * @param number - Unknown phone number
     * @return JSON-encoded URI that can be used to perform a lookup when clicking on the quick
     * contact card.
     */
    public static Uri createTemporaryContactUri(String number) {
        try {
            final JSONObject contactRows = new JSONObject().put(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                    new JSONObject().put(ContactsContract.CommonDataKinds.Phone.NUMBER, number).put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM));

            final String jsonString = new JSONObject().put(ContactsContract.Contacts.DISPLAY_NAME, number)
                    .put(ContactsContract.Contacts.DISPLAY_NAME_SOURCE, ContactsContract.DisplayNameSources.PHONE)
                    .put(ContactsContract.Contacts.CONTENT_ITEM_TYPE, contactRows).toString();

            return ContactsContract.Contacts.CONTENT_LOOKUP_URI
                    .buildUpon()
                    .appendPath(Constants.LOOKUP_URI_ENCODED)
                    .appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY,
                            String.valueOf(Long.MAX_VALUE))
                    .encodedFragment(jsonString)
                    .build();
        } catch (JSONException e) {
            return null;
        }
    }
}
