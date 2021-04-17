package bkav.android.btalk.utility;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.android.dialer.database.DialerDatabaseHelper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;

import bkav.android.btalk.R;

/**
 * Created by anhdt on 11/12/2017.
 *
 */

public class TelephoneExchangeUtils {
    public static final String BKAV_SUPPORT_MY = "Bphone Support";
    public static final String BKAV_NUMBER_MY = "09690000966";

    private final HashMap<String, String> EXCHANGE_TELEPHONE = new HashMap<>();

    public TelephoneExchangeUtils(Context context) {
        /*String otherText = context.getResources().getString(R.string.other_telephone_exchange);*/

        /*EXCHANGE_TELEPHONE.put("112", context.getResources().getString(R.string.rescue_text));
        EXCHANGE_TELEPHONE.put("113", context.getResources().getString(R.string.police_text));
        EXCHANGE_TELEPHONE.put("114", context.getResources().getString(R.string.firefighting_text));
        EXCHANGE_TELEPHONE.put("115", context.getResources().getString(R.string.ambulance_text));*/

        EXCHANGE_TELEPHONE.put("197", "VIETTEL");
        EXCHANGE_TELEPHONE.put("198", "VIETTEL");
        EXCHANGE_TELEPHONE.put("1222", "VIETTEL");
        EXCHANGE_TELEPHONE.put("1789", "VIETTEL");
        EXCHANGE_TELEPHONE.put("9189", "VIETTEL");
        EXCHANGE_TELEPHONE.put("9198", "VIETTEL");
        EXCHANGE_TELEPHONE.put("19008198", "VIETTEL");
        EXCHANGE_TELEPHONE.put("18008198", "VIETTEL");
        EXCHANGE_TELEPHONE.put("19008062", "VIETTEL");
        EXCHANGE_TELEPHONE.put("19008099", "VIETTEL");
        EXCHANGE_TELEPHONE.put("19008098", "VIETTEL");

        EXCHANGE_TELEPHONE.put("18008098", "VIETTEL");
        EXCHANGE_TELEPHONE.put("18008119", "VIETTEL");
        EXCHANGE_TELEPHONE.put("18008000", "VIETTEL");
        EXCHANGE_TELEPHONE.put("18008168", "VIETTEL");

        EXCHANGE_TELEPHONE.put("9090", "Mobiphone");
        EXCHANGE_TELEPHONE.put("9393", "Mobiphone");

        EXCHANGE_TELEPHONE.put("9191", "Vinaphone");
        EXCHANGE_TELEPHONE.put("9192", "Vinaphone");
        EXCHANGE_TELEPHONE.put("888", "Vinaphone");
        EXCHANGE_TELEPHONE.put("18001091", "Vinaphone");

        EXCHANGE_TELEPHONE.put("789", "Vietnamobile");
        EXCHANGE_TELEPHONE.put("123", "Vietnamobile");
        EXCHANGE_TELEPHONE.put("360", "Vietnamobile");
        EXCHANGE_TELEPHONE.put("3636", "Vietnamobile");
        EXCHANGE_TELEPHONE.put("366", "Vietnamobile");

        // Anhdts tong dai ben myanmar
        if (isBMyanmar()) {
            EXCHANGE_TELEPHONE.put(BKAV_NUMBER_MY, BKAV_SUPPORT_MY);
        }

        EXCHANGE_TELEPHONE.put("19001100", "Vietnam Airlines");
        EXCHANGE_TELEPHONE.put("19001800", "Vietnam Airlines");

        /*EXCHANGE_TELEPHONE.put("199", otherText);
        EXCHANGE_TELEPHONE.put("121", otherText);
        EXCHANGE_TELEPHONE.put("900", otherText);
        //Bkav QuangNDb them so 1080
        EXCHANGE_TELEPHONE.put("1080", otherText);*/
    }

    public DialerDatabaseHelper.ContactNumber getTelephoneExchange(String number, Context context) {
        String type = EXCHANGE_TELEPHONE.get(number);

        // Bkav HuyNQN fix loi khi thay doi ngon ngu khong hien thi duoc dung text, do BtalkDialerDB la singleton nen xu ly the nay
        if(number.equals("112")){
            type = context.getResources().getString(R.string.rescue_text);
        }else if(number.equals("113")){
            type = context.getResources().getString(R.string.police_text);
        }else if(number.equals("114")){
            type = context.getResources().getString(R.string.firefighting_text);
        }else if(number.equals("115")){
            type = context.getResources().getString(R.string.ambulance_text);
            // Bkav TienNAb: them text hien thi cho mot vai dau so
        }else if(number.equals("900") || number.equals("199") || number.equals("121") || number.equals("1080")){
            type = context.getResources().getString(R.string.other_telephone_exchange);
        }

        if (!TextUtils.isEmpty(type)) {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            return new DialerDatabaseHelper.ContactNumber(type, number, uri.toString(), uri);
        }
        return null;
    }

    private boolean isBMyanmar() {
        return "myanmar".equals(getSystemProperty("ro.bkav.market",""));
    }

    private String getSystemProperty(String property, String defaultValue) {
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method getter = clazz.getDeclaredMethod("get", String.class);
            String value = (String) getter.invoke(null, property);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        } catch (Exception e) {
        }
        return defaultValue;
    }
}
