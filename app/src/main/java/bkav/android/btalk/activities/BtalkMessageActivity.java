package bkav.android.btalk.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.android.messaging.ui.UIIntents;

import bkav.android.btalk.messaging.ui.BtalkUIIntentsImpl;
import bkav.android.btalk.utility.PrefUtils;

/**
 * Created by trungth on 02/06/2017.
 */

public class BtalkMessageActivity extends Activity {

    public static final String IS_MESSAGE_SHORT_CUT = "message_shortcut";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Anhdts neu dang mo nhan tin thi chuyen luon vao giao dien nhan tin
        if (PrefUtils.get().loadBooleanPreferences(this, PrefUtils.KEEP_STATUS_APP, false)) {
            long deltaTime = System.currentTimeMillis() - PrefUtils.get().loadLongPreferences(this,
                    PrefUtils.TIME_PAUSE_APP, System.currentTimeMillis());
            if (convertMillisToMinutes(deltaTime) <= 15) {
                final String conversationId = PrefUtils.get().loadStringPreferences(this,
                        PrefUtils.CONVERSATION_ID, "-1");
                UIIntents.get().launchConversationActivity(
                        this, conversationId, null,
                        null,
                        false);
                finish();
                return;
            }
        }

        Intent intent = new Intent(this, BtalkActivity.class);
        intent.setAction(BtalkUIIntentsImpl.MESSAGE_ACTION);
        intent.putExtra(IS_MESSAGE_SHORT_CUT, true);
        startActivity(intent);
        finish();
    }

    /**
     * Bkav QuangNDb Chuyen doi thoi gian tu ms -> phut
     */
    private long convertMillisToMinutes(long time) {
        return time / 60000;
    }
}
