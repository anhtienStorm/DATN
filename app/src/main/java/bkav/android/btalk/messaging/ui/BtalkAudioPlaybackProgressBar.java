package bkav.android.btalk.messaging.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.android.messaging.ui.AudioPlaybackProgressBar;
import com.android.messaging.ui.ConversationDrawables;

/**
 * Created by quangnd on 12/04/2017.
 */

public class BtalkAudioPlaybackProgressBar extends AudioPlaybackProgressBar {

    public BtalkAudioPlaybackProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected ConversationDrawables getConversationDrawables() {
        return BtalkConversationDrawables.get();
    }
}
