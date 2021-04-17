package bkav.android.btalk.messaging.ui;

import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;

import com.android.messaging.ui.BlockedParticipantsActivity;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 26/05/2017.
 */

public class BtalkBlockedParticipantsActivity extends BlockedParticipantsActivity {

    @Override
    protected void hideElevation() {
        getSupportActionBar().setElevation(0f);
        // Bkav QuangNDB doi mau action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.actionbar_setting_color)));
    }

    @Override
    public int getIdResLayout() {
        return R.layout.btalk_blocked_participants_activity;
    }
}
