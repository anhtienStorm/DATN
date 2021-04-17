package bkav.android.btalk.contacts;

import android.app.Activity;
import android.content.Intent;

import com.android.contacts.common.list.ContactListFilter;
import com.android.contacts.common.util.AccountFilterUtil;

/**
 * AnhNDd: class tiện ích để lựa chọn danh bạ hiển thị
 */
public class BtalkAccountFilterUtil extends AccountFilterUtil {

    /**
     * AnhNDd: start activity cua minh
     */
    public static void startBtalkAccountFilterActivityForResult(
            Activity activity, int requestCode, ContactListFilter currentFilter) {
        final Intent intent = new Intent(activity, BtalkAccountFilterActivity.class);
        intent.putExtra(BtalkAccountFilterActivity.KEY_EXTRA_CURRENT_FILTER, currentFilter);
        activity.startActivityForResult(intent, requestCode);
    }

}
