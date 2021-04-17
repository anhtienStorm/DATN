package bkav.android.btalk.database;

import android.content.Context;

import com.android.dialer.database.DialerDatabaseHelper;
import com.android.dialer.database.VoicemailArchiveProvider;
import com.google.common.annotations.VisibleForTesting;

import bkav.android.btalk.suggestmagic.BtalkDialerDatabaseHelper;

/**
 * Created by anhdt on 20/04/2017.
 * extends de su dung {@link BtalkDialerDatabaseHelper} custom
 */

public class BtalkVoiceMailArchiveProvider extends VoicemailArchiveProvider {
    @VisibleForTesting
    @Override
    protected DialerDatabaseHelper getDatabaseHelper(Context context) {
        return BtalkDatabaseHelperManager.getDatabaseHelper(context);
    }
}
