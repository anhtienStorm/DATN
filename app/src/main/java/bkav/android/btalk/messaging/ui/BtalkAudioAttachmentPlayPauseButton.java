package bkav.android.btalk.messaging.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.android.messaging.ui.AudioAttachmentPlayPauseButton;
import com.android.messaging.ui.ConversationDrawables;

/**
 * Created by quangnd on 12/04/2017.
 */

public class BtalkAudioAttachmentPlayPauseButton extends AudioAttachmentPlayPauseButton {

    public BtalkAudioAttachmentPlayPauseButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected ConversationDrawables getConversationDrawables() {
        return BtalkConversationDrawables.get();
    }
}
