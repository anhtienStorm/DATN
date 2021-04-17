package bkav.android.btalk.messaging.ui.conversation;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.android.messaging.ui.conversation.SimSelectorItemView;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.custom_view.BtalkSimIconView;

/**
 * Created by quangnd on 24/07/2017.
 */

public class BtalkSimSelectorItemView extends SimSelectorItemView {

    public BtalkSimSelectorItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private BtalkSimIconView mSimView;

    @Override
    protected void initSimIconView() {
        mSimView = (BtalkSimIconView)findViewById(R.id.sim_icon);
    }

    @Override
    protected void setUriSimIconView() {
        mSimView.setImageResourceUri(mData.iconUri,getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size));
    }

    @Override
    protected void sendEventTouchSimIcon() {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 0.0f;
        float y = 0.0f;
// List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x,
                y,
                metaState
        );
        mSimView.onTouchEvent(motionEvent);
    }
}
