package bkav.android.btalk.messaging.util;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;

import com.android.messaging.Factory;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by quangnd on 20/04/2017.
 * class dung de match so dien thoai co trong body tin nhan
 */

public class BtalkContactParser {

    private static final BtalkContactParser ourInstance = new BtalkContactParser();
    public static BtalkContactParser getInstance() {
        return ourInstance;
    }

    private BtalkContactParser() {
    }

    /**
     * Bkav QuangNDb add them contact name neu co so dt trong tin nhan
     */
    public CharSequence addContactName(CharSequence text) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Iterable<PhoneNumberMatch> matches = phoneUtil.findNumbers(text.toString(),
                Locale.JAPAN.getCountry(), PhoneNumberUtil.Leniency.POSSIBLE, Long.MAX_VALUE);
        String result = text.toString().replaceAll("\\s+$", "");
        int start;
        int end;
        int positionChange = 0;
        for (PhoneNumberMatch match : matches) {
            String phoneNumber = PhoneNumberUtils.normalizeNumber(match.rawString());
            String displayName = getDisPlayName(phoneNumber);
            if (!displayName.isEmpty()) {
                start = match.start();
                end = match.end();
                if (start == 0) {
                    result = displayName + " <" + result.substring(start, end) + "> " + result.substring(end, result.length());
                    positionChange += displayName.length() + 4;// 4 la " <" "> "
                } else {
                    if (!result.substring(start - 1).equalsIgnoreCase("<")
                            && !Pattern.compile(Pattern.quote(displayName), Pattern.CASE_INSENSITIVE).matcher(result).find()) {
                        result = result.substring(0, start - 1 + positionChange) + " " + displayName + " <" +
                                result.substring(start + positionChange, end + positionChange) + "> " + result.substring(end + positionChange, result.length());
                        positionChange += displayName.length() + 4;// 4 la " <" "> "
                    }
                }
            }
        }
        return result;
    }


    /**
     * Bkav QuangNDb truyen vao 1 sdt va lay ra ten trong danh ba neu co
     */

    private String getDisPlayName(String phoneNum) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNum));
        String displayName = "";
        Cursor cursor = null;
        try {
            cursor = Factory.get().getApplicationContext().getContentResolver().query(uri,
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                return displayName;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return displayName;
    }
}
