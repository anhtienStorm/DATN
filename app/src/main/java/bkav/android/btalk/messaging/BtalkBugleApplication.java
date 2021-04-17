package bkav.android.btalk.messaging;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.webkit.WebView;

import com.android.messaging.BugleApplication;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.Locale;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.datamodel.data.QuickResponseData;
import bkav.android.btalk.text_shortcut.DeepShortcutsContainer;

/**
 * Created by quangnd on 27/03/2017.
 */

public class BtalkBugleApplication extends BugleApplication {

    private final static String SHARE_PREFRENCE_APPLICATION = "preference_btalk";

    private final static String PREF_IS_FIRST_RUN = "pref_is_first_run";

    public BtalkBugleApplication(Application application) {
        super(application);
    }

    @Override
    protected void createFactory() {
        BtalkFactoryImpl.register(mApplication.getApplicationContext(), this);
        initLinkMap();
        // Anhdts de mac dinh chua co gi, xoa cac quickResponse cu di
        // insertQuickResponse();
        checkRemoveDefaultQuickMessage();
    }

    private void checkRemoveDefaultQuickMessage() {
        if (mApplication.getApplicationContext().getSharedPreferences(SHARE_PREFRENCE_APPLICATION, Context.MODE_PRIVATE).
                getBoolean(PREF_IS_FIRST_RUN, true)) {
            removeQuickResponse();
            mApplication.getApplicationContext().getSharedPreferences(SHARE_PREFRENCE_APPLICATION, Context.MODE_PRIVATE).
                    edit().putBoolean(PREF_IS_FIRST_RUN, false).apply();
        }
    }

    private void removeQuickResponse() {
        mApplication.getContentResolver().delete(MessagingContentProvider.QUICK_RESPONSE_URI,
                DatabaseHelper.QuickResponseColumns.IS_DEFAULT + " = " + "1", null);
    }

    private void insertQuickResponse() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isHasQuickResponse()) {
                    final String[] templates = mApplication.getResources().getStringArray(R.array.quick_reply_template);
                    for (String response : templates) {
                        final QuickResponseData data = new QuickResponseData(response, true);
                        data.insertToDb(mApplication);
                    }
                }
            }
        }).start();
    }

    /**Bkav QuangNDb kiem tra xem db da co quick reponse chua*/
    private boolean isHasQuickResponse() {
        Cursor cursor = null;
        try {
            cursor = mApplication.getContentResolver().query(MessagingContentProvider.QUICK_RESPONSE_URI, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                return true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    private boolean isHasMaxQuickResponse() {
        Cursor cursor = null;
        try {
            cursor = mApplication.getContentResolver().query(MessagingContentProvider.QUICK_RESPONSE_URI, null, null, null, null);
            if (cursor != null && cursor.getCount() == DeepShortcutsContainer.MAX_SHORTCUTS) {
                return true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    /**
     * Bkav QuangNDb khoi tao cac gia tri map link web va phone number de khong bi load cham
     * Them doan khoi tao du lieu nay trong thread moi de khong bi khoi dong cham app Btalk
     */
    private void initLinkMap() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Iterable<PhoneNumberMatch> matches = phoneUtil.findNumbers("test 0987956424 adn 01673371219",
                        Locale.getDefault().getCountry(), PhoneNumberUtil.Leniency.POSSIBLE, Long.MAX_VALUE);
                for (PhoneNumberMatch match : matches) {
                }
                WebView.findAddress("test");
            }
        }).start();
    }
}
