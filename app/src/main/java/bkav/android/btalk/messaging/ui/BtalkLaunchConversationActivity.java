package bkav.android.btalk.messaging.ui;

import android.content.Context;

import com.android.messaging.Factory;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.conversation.LaunchConversationActivity;

import bkav.android.btalk.messaging.BtalkFactoryImpl;

/**
 * Created by anhdt on 22/04/2017.
 * custom {@link LaunchConversationActivity}
 */

public class BtalkLaunchConversationActivity extends LaunchConversationActivity {

    // Anhdts check gui tin nhan trong app
    @Override
    protected boolean checkSendMessageInApp(Context context, String conversationId) {
        if (mSmsBody == null && ((BtalkFactoryImpl) Factory.get()).getRegisterSendMessage()) {
            UIIntents.get().launchConversationActivityNewTask(context, conversationId);
            return true;
        }
        return false;
    }
}
