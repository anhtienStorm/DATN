package bkav.android.btalk.messaging.ui;

import com.android.messaging.ui.PermissionCheckActivity;
import com.android.messaging.ui.UIIntents;

/**
 * Created by quangnd on 17/11/2017.
 */

public class BtalkPermissionCheckActivity extends PermissionCheckActivity {

    @Override
    protected void redirect() {
        UIIntents.get().launchCreateNewConversationActivity(this, null);
        finish();
    }
}
