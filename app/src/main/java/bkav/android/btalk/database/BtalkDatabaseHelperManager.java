package bkav.android.btalk.database;

import android.content.Context;

import com.android.dialer.database.DialerDatabaseHelper;

import bkav.android.btalk.suggestmagic.BtalkDialerDatabaseHelper;

/**
 * Created by anhdt on 20/04/2017.
 */

public class BtalkDatabaseHelperManager {
    public static DialerDatabaseHelper getDatabaseHelper(Context context) {
        return BtalkDialerDatabaseHelper.getInstance(context);
    }
}
