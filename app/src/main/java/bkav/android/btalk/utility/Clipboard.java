package bkav.android.btalk.utility;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * Created by anhdt on 07/09/2017.
 */

public class Clipboard {
    private static volatile Clipboard sInstance;

    public static Clipboard get() {
        if (sInstance == null) {
            sInstance = new Clipboard();
        }
        return sInstance;
    }
    public String getStringClipboard(Context context) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = clipboardManager.getPrimaryClip();
        if (data != null && data.getItemCount() > 0) {
            String firstItem = String.valueOf(data.getItemAt(0).getText());
            return filterNumericArray(firstItem);
        }
        return "";
    }

    private String filterNumericArray(String str) {
        if (str == null)
            return "";
        char[] data = str.toCharArray();
        if (data.length <= 0)
            return "";
        int index = 0;
        StringBuilder phone = new StringBuilder();
        for (; index < data.length; index++) {
            if ((data[index] >= '0' && data[index] <= '9')
                    || data[index] == ','
                    || data[index] == ';'
                    || data[index] == '+'
                    || data[index] == '*'
                    || data[index] == '-'
                    || data[index] == '#') {
                phone.append(data[index]);
            }
        }
        return phone.toString();
    }
}
