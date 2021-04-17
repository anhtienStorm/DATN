package bkav.android.btalk.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.android.phone.common.dialpad.DialpadKeyButton;

/**
 * Created by anhdt on 09/12/2017.
 */

public class OverlayButton extends RelativeLayout {

    /** **********************************Anhdts ***********************************/
    /**
     * Xu ly su kien cham nut, neu giu tren 200ms thi cho la click nut
     */
    private static final long MAGIC_DISTANCE_DELTA = 200;

    /**
     * Doi voi nut * va # co su kien long click rieng roi, nen neu giu qua 300ms
     * se nhan la an long click va k nhan click nua
     */
    private static final long TIME_LONG_CLICK = 300;

    private float mPosY;

    private float mPosX;

    private long mTimeClick = 0;

    public OverlayButton(Context context) {
        super(context);
    }

    public OverlayButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OverlayButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected DialpadKeyButton.OnPressedListener mOnPressedListener;

    public void setOnPressedListener(DialpadKeyButton.OnPressedListener onPressedListener) {
        mOnPressedListener = onPressedListener;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (mOnPressedListener != null && Math.abs(ev.getY() - mPosY) < MAGIC_DISTANCE_DELTA
                    && Math.abs(ev.getX() - mPosX) < MAGIC_DISTANCE_DELTA) {
                if (getContentDescription() != null && !getContentDescription().equals("*") && !getContentDescription().equals("#")) {
                    mOnPressedListener.onPressed(this, true, true);
//                    mOnPressedListener.onPressed(this, false);
                } else if ((System.currentTimeMillis() - mTimeClick) < TIME_LONG_CLICK) {
                    mOnPressedListener.onPressed(this, true, true);
//                    mOnPressedListener.onPressed(this, false);
                }
            }
        } else if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mTimeClick = System.currentTimeMillis();
            mPosY = ev.getY();
            mPosX = ev.getX();
        }
        return super.dispatchTouchEvent(ev);
    }
}
