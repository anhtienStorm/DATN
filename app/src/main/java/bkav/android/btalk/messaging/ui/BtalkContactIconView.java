package bkav.android.btalk.messaging.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.GeoUtil;
import com.android.contacts.common.compat.CompatUtils;
import com.android.contacts.common.util.PermissionsUtil;
import com.android.dialer.calllog.CallLogQuery;
import com.android.dialer.calllog.ContactInfo;
import com.android.dialer.calllog.ContactInfoHelper;
import com.android.dialer.contactinfo.ContactInfoCache;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.PhoneNumberUtil;
import com.android.ex.chips.PhoneUtil;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.ui.ContactIconView;
import com.android.messaging.util.ContactUtil;
import com.google.i18n.phonenumbers.NumberParseException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by quangnd on 27/03/2017.
 */

public class BtalkContactIconView extends ContactIconView implements ContactInfoCache.OnContactInfoChangedListener {

    private Context mContext;

    private String mConversationId;

    private String mDisplayName;

    private String mPhotoProfileUri;

    private ContactPhotoManager mContactPhotoManager;

    public BtalkContactIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mContactPhotoManager = ContactPhotoManager.getInstance(mContext);
    }

    @Override
    protected void maybeInitializeOnClickListener() {
        if ((mContactId > ParticipantData.PARTICIPANT_CONTACT_ID_NOT_RESOLVED
                && !TextUtils.isEmpty(mContactLookupKey)) ||
                !TextUtils.isEmpty(mNormalizedDestination)) {
            setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    Uri lookupUri = getLookupUri();
                    if (lookupUri != null) {
                        ContactsContract.QuickContact.showQuickContact(view.getContext(), view, lookupUri,
                                ContactsContract.QuickContact.MODE_LARGE, null);
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
        } else if (PhoneUtil.isPhoneNumber(mNormalizedDestination)) {
            lookupUri = getLookupUriWithPhone(mNormalizedDestination);
        } else {
            lookupUri = null;
        }
        return lookupUri;
    }

    /**
     * Bkav quangndb get lookup uri cua sdt nhan tin den neu so dien thoai nay co trong callog
     */
    private Uri getLookupUriWithPhone(String phone) {
        Uri lookupUri = null;
        Cursor cursor = null;
        try {
            phone = com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance().parse(phone, Locale.getDefault().getCountry().toUpperCase()).getNationalNumber()+"";
            cursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, CallLogQuery._PROJECTION
                    , CallLog.Calls.NUMBER + " LIKE '%" + phone + "%'", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                ContactInfo contactInfo = getContactInfoFromCallLog(cursor);
                if (contactInfo != null && contactInfo.lookupUri != null) {
                    lookupUri = contactInfo.lookupUri;
                }
            }

        } catch (NumberParseException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return lookupUri;
    }

    /**
     * Bkav QuangNDb lay thong tin contact tu callLog
     */
    private ContactInfo getContactInfoFromCallLog(Cursor cursor) {
        ContactInfo contactInfo = null;
        ContactInfoCache mContactInfoCache = new ContactInfoCache(
                new ContactInfoHelper(mContext, GeoUtil.getCurrentCountryIso(mContext)), this);
        if (!PermissionsUtil.hasContactsPermissions(mContext)) {
            mContactInfoCache.disableRequestProcessing();
        }
        mContactInfoCache.start();
        final String postDialDigits = CompatUtils.isNCompatible() ?
                cursor.getString(CallLogQuery.POST_DIAL_DIGITS) : "";
        final String phoneNumber = cursor.getString(CallLogQuery.NUMBER);
        final String countryIso = cursor.getString(CallLogQuery.COUNTRY_ISO);
        Pattern pattern = Pattern.compile("[,;]");
        String[] num = pattern.split(phoneNumber);
        final String number = DialerUtils.isConferenceURICallLog(phoneNumber, postDialDigits) ?
                phoneNumber : num.length > 0 ? num[0] : "";
        final int numberPresentation = cursor.getInt(CallLogQuery.NUMBER_PRESENTATION);
        if (PhoneNumberUtil.canPlaceCallsTo(number, numberPresentation)) {
            boolean isConfCallLog = num != null && num.length > 1
                    && DialerUtils.isConferenceURICallLog(phoneNumber, postDialDigits);
            String queryNumber = isConfCallLog ? phoneNumber : number;
            contactInfo = mContactInfoCache.getValue(queryNumber, postDialDigits,
                    countryIso, ContactInfoHelper.getContactInfo(cursor), isConfCallLog);
        }
        return contactInfo;
    }

    @Override
    public void onContactInfoChanged() {
        //QuangNdb khong lam gi
    }

//    @Override
//    protected void setUpAvatarGroup(Uri uri) {
//        setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_group));
//    }
//
//    @Override
//    protected void setUpAvatarPersonal(Uri uri) {
//        ContactPhotoManager.DefaultImageRequest request = null;
//        if (mPhotoProfileUri == null) {
//            request = new ContactPhotoManager.DefaultImageRequest(mDisplayName, mConversationId, true);
//        }
//        Uri photoUri = (mPhotoProfileUri == null) ? null : Uri.parse(mPhotoProfileUri);
//        mContactPhotoManager.loadDirectoryPhoto(this, photoUri, false, true,
//                request);
//    }
}
