package bkav.android.btalk.contacts.editcontact;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.ImageView;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.ContactsUtils;
import com.android.contacts.common.lettertiles.LetterTileDrawable;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.ValuesDelta;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.common.model.account.GoogleAccountType;
import com.android.contacts.common.model.dataitem.DataKind;
import com.android.contacts.common.testing.NeededForTesting;
import com.android.contacts.common.util.MaterialColorMapUtils;
import com.android.contacts.util.ContactPhotoUtils;
import com.android.contacts.widget.QuickContactImageView;
import com.google.common.collect.Maps;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

import bkav.android.btalk.R;

import static com.android.contacts.common.util.MaterialColorMapUtils.getDefaultPrimaryAndSecondaryColors;

public class BtalkEditorUiUtils {
    // Maps DataKind.mimeType to editor view layouts.
    private static final HashMap<String, Integer> mimetypeLayoutMap = Maps.newHashMap();

    static {
        // Generally there should be a layout mapped to each existing DataKind mimetype but lots of
        // them use the default text_fields_editor_view which we return as default so they don't
        // need to be mapped.
        //
        // Other possible mime mappings are:
        // DataKind.PSEUDO_MIME_TYPE_DISPLAY_NAME
        // Nickname.CONTENT_ITEM_TYPE
        // Email.CONTENT_ITEM_TYPE
        // StructuredPostal.CONTENT_ITEM_TYPE
        // Im.CONTENT_ITEM_TYPE
        // Note.CONTENT_ITEM_TYPE
        // Organization.CONTENT_ITEM_TYPE
        // Phone.CONTENT_ITEM_TYPE
        // SipAddress.CONTENT_ITEM_TYPE
        // Website.CONTENT_ITEM_TYPE
        // Relation.CONTENT_ITEM_TYPE
        //
        // Un-supported mime types need to mapped with -1.

        mimetypeLayoutMap.put(DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME,
                R.layout.btalk_phonetic_name_editor_view);
        mimetypeLayoutMap.put(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                R.layout.btalk_structured_name_editor_view);
        mimetypeLayoutMap.put(ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE, -1);
        mimetypeLayoutMap.put(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE, -1);
        mimetypeLayoutMap.put(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, R.layout.btalk_event_field_editor_view);
    }

    /**
     * Fetches a layout for a given mimetype.
     *
     * @param mimetype The mime type (e.g. StructuredName.CONTENT_ITEM_TYPE)
     * @return The layout resource id.
     */
    public static int getLayoutResourceId(String mimetype) {
        final Integer id = mimetypeLayoutMap.get(mimetype);
        if (id == null) {
            return R.layout.btalk_text_fields_editor_view;
        }
        return id;
    }

    /**
     * Returns the account name and account type labels to display for local accounts.
     */
    @NeededForTesting
    public static Pair<String, String> getLocalAccountInfo(Context context,
                                                           String accountName, AccountType accountType) {
        if (TextUtils.isEmpty(accountName)) {
            return new Pair<>(
                    /* accountName =*/ null,
                    context.getString(R.string.local_profile_title));
        }
        return new Pair<>(
                accountName,
                context.getString(R.string.external_profile_title,
                        accountType.getDisplayLabel(context)));
    }

    /**
     * Returns the account name and account type labels to display for the given account type.
     */
    @NeededForTesting
    public static Pair<String, String> getAccountInfo(Context context, String accountName,
                                                      AccountType accountType) {
        CharSequence accountTypeDisplayLabel = accountType.getDisplayLabel(context);
        if (TextUtils.isEmpty(accountTypeDisplayLabel)) {
            accountTypeDisplayLabel = context.getString(R.string.account_phone);
        }

        if (TextUtils.isEmpty(accountName)) {
            return new Pair<>(
                    /* accountName =*/ null,
                    context.getString(R.string.account_type_format, accountTypeDisplayLabel));
        }

        final String accountNameDisplayLabel =
                context.getString(R.string.from_account_format, accountName);

        if (GoogleAccountType.ACCOUNT_TYPE.equals(accountType.accountType)
                && accountType.dataSet == null) {
            return new Pair<>(
                    accountNameDisplayLabel,
                    context.getString(R.string.google_account_type_format, accountTypeDisplayLabel));
        }
        return new Pair<>(
                accountNameDisplayLabel,
                context.getString(R.string.account_type_format, accountTypeDisplayLabel));
    }

    /**
     * Returns a content description String for the container of the account information
     * returned by {@link #getAccountInfo}.
     */
    public static String getAccountInfoContentDescription(CharSequence accountName,
                                                          CharSequence accountType) {
        final StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(accountType)) {
            builder.append(accountType).append('\n');
        }
        if (!TextUtils.isEmpty(accountName)) {
            builder.append(accountName);
        }
        return builder.toString();
    }

    /**
     * Return an icon that represents {@param mimeType}.
     */
    public static Drawable getMimeTypeDrawable(Context context, String mimeType) {
        switch (mimeType) {
            case ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
                return context.getResources().getDrawable(R.drawable.ic_display_name);
            case ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE:
                return context.getResources().getDrawable(R.drawable.ic_location);
            case ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE:
                return context.getResources().getDrawable(R.drawable.ic_sip);
            case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                return context.getResources().getDrawable(R.drawable.ic_phone_number);
            case ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE:
                return context.getResources().getDrawable(R.drawable.ic_im);
            case ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE:
                return context.getResources().getDrawable(R.drawable.ic_calender);
            case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE:
                return context.getResources().getDrawable(R.drawable.ic_mail);
            case ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE:
                return context.getResources().getDrawable(R.drawable.ic_web);
            case ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE:
                return context.getResources().getDrawable(R.drawable.ic_camera_alt_black_24dp);
            case ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE:
                return context.getResources().getDrawable(R.drawable.ic_company);
            case ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE:
                return context.getResources().getDrawable(R.drawable.ic_note);
            case ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE:
                return context.getResources().getDrawable(
                        R.drawable.ic_relationship);
            // Bkav HaiKH - Fix bug BOS-3229- Start
            // Khi có tài khoản đăng nhập thì mới hiển thị icon nhóm
            case ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE:
                List<AccountWithDataSet> list = AccountTypeManager
                        .getInstance(context).getAccounts(true);
                if (list.size() > 0){
                    return context.getResources().getDrawable(R.drawable.ic_group);
                }
                // Bkav HaiKH - Fix bug BOS-3229- End
            default:
                return null;
        }
    }

    /**
     * Returns a ringtone string based on the ringtone URI and version #.
     */
    @NeededForTesting
    public static String getRingtoneStringFromUri(Uri pickedUri, int currentVersion) {
        if (isNewerThanM(currentVersion)) {
            if (pickedUri == null) return ""; // silent ringtone
            if (RingtoneManager.isDefault(pickedUri)) return null; // default ringtone
        }
        if (pickedUri == null || RingtoneManager.isDefault(pickedUri)) return null;
        return pickedUri.toString();
    }

    /**
     * Returns a ringtone URI, based on the string and version #.
     */
    @NeededForTesting
    public static Uri getRingtoneUriFromString(String str, int currentVersion) {
        if (str != null) {
            if (isNewerThanM(currentVersion) && TextUtils.isEmpty(str)) return null;
            return Uri.parse(str);
        }
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    }

    private static boolean isNewerThanM(int currentVersion) {
        return currentVersion > Build.VERSION_CODES.M;
    }

    /** Returns the {@link ContactsContract.CommonDataKinds.Photo#PHOTO_FILE_ID} from the given ValuesDelta. */
    public static Long getPhotoFileId(ValuesDelta valuesDelta) {
        if (valuesDelta == null) return null;
        if (valuesDelta.getAfter() == null || valuesDelta.getAfter().get(ContactsContract.CommonDataKinds.Photo.PHOTO) == null) {
            return valuesDelta.getAsLong(ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID);
        }
        return null;
    }

    /** Binds the full resolution image at the given Uri to the provided ImageView. */
    static void loadPhoto(ContactPhotoManager contactPhotoManager, ImageView imageView,
                          Uri photoUri) {
        final ContactPhotoManager.DefaultImageProvider fallbackToPreviousImage = new ContactPhotoManager.DefaultImageProvider() {
            @Override
            public void applyDefaultImage(ImageView view, int extent, boolean darkTheme,
                                          ContactPhotoManager.DefaultImageRequest defaultImageRequest) {
                // Before we finish setting the full sized image, don't change the current
                // image that is set in any way.
            }
        };
        contactPhotoManager.loadPhoto(imageView, photoUri, imageView.getWidth(),
                /* darkTheme =*/ false, /* isCircular =*/ false,
                /* defaultImageRequest =*/ null, fallbackToPreviousImage);
    }

    /** Decodes the Bitmap from the photo bytes from the given ValuesDelta. */
    public static Bitmap getPhotoBitmap(ValuesDelta valuesDelta) {
        if (valuesDelta == null) return null;
        final byte[] bytes = valuesDelta.getAsByteArray(ContactsContract.CommonDataKinds.Photo.PHOTO);
        if (bytes == null) return null;
        return BitmapFactory.decodeByteArray(bytes, /* offset =*/ 0, bytes.length);
    }

    /** Binds the default avatar to the given ImageView and tints it to match QuickContacts. */
    public static void setDefaultPhoto(ImageView imageView, Resources resources,
                                       MaterialColorMapUtils.MaterialPalette materialPalette) {
        // Use the default avatar drawable
        imageView.setImageDrawable(ContactPhotoManager.getDefaultAvatarDrawableForContact(
                resources, /* hires =*/ false, /* defaultImageRequest =*/ null));

        // Tint it to match the quick contacts
        if (imageView instanceof QuickContactImageView) {
            ((QuickContactImageView) imageView).setTint(materialPalette == null
                    ? getDefaultPrimaryAndSecondaryColors(resources).mPrimaryColor
                    : materialPalette.mPrimaryColor);
        }
    }

    /**  Returns compressed bitmap bytes from the given Uri, scaled to the thumbnail dimensions. */
    public static byte[] getCompressedThumbnailBitmapBytes(Context context, Uri uri)
            throws FileNotFoundException {
        final Bitmap bitmap = ContactPhotoUtils.getBitmapFromUri(context, uri);
        final int size = ContactsUtils.getThumbnailSize(context);
        final Bitmap bitmapScaled = Bitmap.createScaledBitmap(
                bitmap, size, size, /* filter =*/ false);
        return ContactPhotoUtils.compressBitmap(bitmapScaled);
    }


    /** Binds the default avatar with tint tile. */
    /**
     * Anhdts
     */
    public static void setDefaultPhoto(Resources resources, ImageView imageView,
                                       MaterialColorMapUtils.MaterialPalette materialPalette) {
        // Use the default avatar drawable
        Drawable drawable = ContactPhotoManager.getDefaultAvatarDrawableForContact(
                resources, /* hires =*/ false, /* defaultImageRequest =*/ null);
        if (drawable instanceof LetterTileDrawable && materialPalette != null) {
            ((LetterTileDrawable) drawable).setColor(materialPalette.mPrimaryColor);
        }
        imageView.setImageDrawable(drawable);

        // Tint it to match the quick contacts
        if (imageView instanceof QuickContactImageView) {
            ((QuickContactImageView) imageView).setTint(materialPalette == null
                    ? getDefaultPrimaryAndSecondaryColors(resources).mPrimaryColor
                    : materialPalette.mPrimaryColor);
        }
    }
}
