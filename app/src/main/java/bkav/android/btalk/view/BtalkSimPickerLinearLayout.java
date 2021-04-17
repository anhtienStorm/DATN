package bkav.android.btalk.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by quangnd on 15/12/2017.
 */

public class BtalkSimPickerLinearLayout extends LinearLayout {

    public BtalkSimPickerLinearLayout(Context context) {
        super(context);
    }

    public BtalkSimPickerLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BtalkSimPickerLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BtalkSimPickerLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private int mChildAt = 2;
    private View mSim1, mSim2;
    private static final int FRAME_AT = 0;
    private static final int SIM_SELECTOR_AT = 2;
    private static final int LIST_AT = 0;
    private static final int SIM_1_AT = 0;
    private static final int SIM_2_AT = 1;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        MotionEvent cancelEvent = getMotionEvent(ev, MotionEvent.ACTION_CANCEL);
        MotionEvent downEvent = getMotionEvent(ev, MotionEvent.ACTION_DOWN);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                View frame = getChildAt(FRAME_AT);
                if (frame instanceof ViewGroup) {
                    View simSelector = ((ViewGroup) frame).getChildAt(SIM_SELECTOR_AT);
                    if (simSelector instanceof ViewGroup) {
                        View listSim = ((ViewGroup) simSelector).getChildAt(LIST_AT);
                        if (listSim instanceof ViewGroup) {
                            mSim1 = ((ViewGroup) listSim).getChildAt(SIM_1_AT);
                            mSim2 = ((ViewGroup) listSim).getChildAt(SIM_2_AT);
                        }
                    }
                }
                if (mSim1 != null && mSim2 != null) {
                    if (ev.getY() <= getYScreen(mSim2) && ev.getY() > (getYScreen(mSim2)) - mSim2.getHeight()) {
                        mChildAt = 1;
                        mSim2.onTouchEvent(downEvent);
                        mSim1.onTouchEvent(cancelEvent);
                    } else if (ev.getY() <= getYScreen(mSim1) && ev.getY() > (getYScreen(mSim1) - mSim1.getHeight())) {
                        mSim1.onTouchEvent(downEvent);
                        mSim2.onTouchEvent(cancelEvent);
                        mChildAt = 0;
                    } else {
                        mSim1.onTouchEvent(cancelEvent);
                        mSim2.onTouchEvent(cancelEvent);
                        mChildAt = 2;
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                if (mChildAt == 0) {
                    mSim1.performClick();
                    mChildAt = 2;
                    return true;
                } else if (mChildAt == 1) {
                    mSim2.performClick();
                    mChildAt = 2;
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }


    private MotionEvent getMotionEvent(MotionEvent event, int action) {
        return MotionEvent.obtain(
                event.getDownTime(),
                event.getEventTime(),
                action,
                event.getX(),
                event.getY(),
                event.getMetaState());
    }

    private float getYScreen(View view) {
        int[] array = new int[2];
        view.getLocationInWindow(array);
        return array[1] - 72;// 72 la do khi o giao dien picker actionbar bi fix di 1 doan la 72 pixel
    }
}
