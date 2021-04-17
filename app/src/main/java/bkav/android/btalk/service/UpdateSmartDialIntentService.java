package bkav.android.btalk.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;

import bkav.android.btalk.suggestmagic.BtalkDialerDatabaseHelper;

/**
 * Bkav QuangNDb service update db smart dial
 */
public class UpdateSmartDialIntentService extends IntentService {

    private static final String ACTION_UPDATE_SMART_DIAL = "bkav.android.btalk.service.action.update_smart_dial";

    public UpdateSmartDialIntentService() {
        super("UpdateSmartDialIntentService");
    }

    //Bkav QuangNDb kich hoat service
    public static void startActionUpdate(Context context) {
        Intent intent = new Intent(context, UpdateSmartDialIntentService.class);
        intent.setAction(ACTION_UPDATE_SMART_DIAL);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_SMART_DIAL.equals(action)) {
                handleActionUpdate();
            }
        }
    }

    //Bkav QuangNDb xu ly update
    private void handleActionUpdate() {
        BtalkDialerDatabaseHelper dialerDatabaseHelper = BtalkDialerDatabaseHelper.getInstance(getApplicationContext());
        dialerDatabaseHelper.startSmartDialUpdateThread();
    }
}
