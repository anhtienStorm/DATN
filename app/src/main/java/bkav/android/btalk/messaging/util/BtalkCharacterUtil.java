package bkav.android.btalk.messaging.util;

import java.text.Normalizer;

/**
 * Created by quangnd on 19/06/2017.
 * class de xu ly cac ky tu trong 1 sau
 */

public class BtalkCharacterUtil {
    private static volatile BtalkCharacterUtil sInstance;

    public static BtalkCharacterUtil get() {
        if (sInstance == null) {
            sInstance = new BtalkCharacterUtil();
        }
        return sInstance;
    }
    /**
     * Bkav QuangNDb chuyen doi doan text co dau thanh khong dau
     */
    public String convertToNotLatinCode(String latinCode) {
        String result = latinCode != null ? Normalizer.normalize(latinCode, Normalizer.Form.NFKD).replaceAll(
                "\\p{InCombiningDiacriticalMarks}+", "") : null;
        // Ham do java convert latin code ben tren chi bi loi chu đ và Đ nên xử lý bằng tay đoạn này
        if (result != null && (result.contains("đ") || result.contains("Đ"))) {
            result = result.replace("đ", "d");
            result = result.replace("Đ", "D");
        }
        return result;
    }
}
