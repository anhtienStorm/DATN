package bkav.android.btalk.messaging.ui.block;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.android.messaging.datamodel.action.UpdateConversationOptionsAction;

/**
 * Anhdts lop xu ly mo khoa thong bao tin nhan
 */
public class NotificationBlockReceiver extends BroadcastReceiver {

    // Bkav TienNAb: uri am thanh mac dinh cua tin nhan
    private static final String DEFAULT_SOUND_URI = "content://settings/system/notification_sound";

    @Override
    public void onReceive(Context context, Intent intent) {
        String conversationId = intent.getStringExtra("conversationid");
        if (!TextUtils.isEmpty(conversationId)) {
            UpdateConversationOptionsAction.enableConversationNotifications(
                    conversationId, true);
        }
    }
}
