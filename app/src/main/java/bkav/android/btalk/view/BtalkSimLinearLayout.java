package bkav.android.btalk.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by quangnd on 20/07/2017.
 */

public class BtalkSimLinearLayout extends LinearLayout {

    public BtalkSimLinearLayout(Context context) {
        super(context);
    }

    public BtalkSimLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BtalkSimLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BtalkSimLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private static final String TAG = "BtalkSimLinearLayout";
    private int mChildAt = 2;
    private View mSim1, mSim2, mSimIcon1, mSimIcon2;
    private static final int FRAME_AT = 1;
    private static final int SIM_SELECTOR_AT = 3;
    private static final int LIST_AT = 0;
    private static final int SIM_1_AT = 0;
    private static final int SIM_2_AT = 1;
    private static final int SIM_ICON_1_AT = 1;
    private static final int SIM_ICON_2_AT = 1;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        Log.d(TAG, "onInterceptTouchEvent:Move  Y "+ev.getAction());
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
                            if (mSim1 instanceof ViewGroup) {
                                mSimIcon1 = ((ViewGroup) mSim1).getChildAt(SIM_ICON_1_AT);
                            }
                            mSim2 = ((ViewGroup) listSim).getChildAt(SIM_2_AT);
                            if (mSim2 instanceof ViewGroup) {
                                mSimIcon2 = ((ViewGroup) mSim2).getChildAt(SIM_ICON_2_AT);
                            }
                        }
                    }
                }
                if (mSim1 != null && mSim2 != null && mSim1.isShown() && mSim2.isShown()) {
                    if (ev.getY() <= getYScreen(mSim2) && ev.getY() > (getYScreen(mSim2)) - mSim2.getHeight()) {
                        mChildAt = 1;
                        mSim2.onTouchEvent(downEvent);
                        mSimIcon2.onTouchEvent(downEvent);
                        mSim1.onTouchEvent(cancelEvent);
                        mSimIcon1.onTouchEvent(cancelEvent);
                    } else if (ev.getY() <= getYScreen(mSim1) && ev.getY() > (getYScreen(mSim1) - mSim1.getHeight())) {
                        mSim1.onTouchEvent(downEvent);
                        mSimIcon1.onTouchEvent(downEvent);
                        mSim2.onTouchEvent(cancelEvent);
                        mSimIcon2.onTouchEvent(cancelEvent);
                        mChildAt = 0;
                    } else {
                        mSim1.onTouchEvent(cancelEvent);
                        mSim2.onTouchEvent(cancelEvent);
                        mSimIcon2.onTouchEvent(cancelEvent);
                        mSimIcon1.onTouchEvent(cancelEvent);
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
        return array[1];
    }
}
