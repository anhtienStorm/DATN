package bkav.android.btalk.messaging.ui.cutomview;

import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;

import com.android.messaging.ui.ConversationDrawables;

import bkav.android.btalk.messaging.ui.BtalkConversationDrawables;

/**
 * Created by quangnd on 08/10/2017.
 */

public class BtalkPlaybackSeekbar extends android.support.v7.widget.AppCompatSeekBar {

    private boolean mIncoming = false;

    public BtalkPlaybackSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        updateAppearance();
    }

    private void updateAppearance() {
        // Bkav QuangNDb tach code doan int ConversationDrawables
        final Drawable drawable =
                getConversationDrawables().getAudioProgressDrawable(mIncoming);
        final ClipDrawable clipDrawable = new ClipDrawable(drawable, Gravity.START,
                ClipDrawable.HORIZONTAL);
        setProgressDrawable(clipDrawable);
        setBackground(getConversationDrawables()
                .getAudioProgressBackgroundDrawable(mIncoming));
    }

    public void setVisualStyle(final boolean incoming) {
        if (mIncoming != incoming) {
            mIncoming = incoming;
            updateAppearance();
        }
    }

    protected ConversationDrawables getConversationDrawables() {
        return BtalkConversationDrawables.get();
    }
}
