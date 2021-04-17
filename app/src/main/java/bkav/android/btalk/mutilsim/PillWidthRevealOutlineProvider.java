package bkav.android.btalk.mutilsim;

import android.graphics.Rect;

/**
 * Created by anhdt on 27/07/2017.
 */

public class PillWidthRevealOutlineProvider extends PillRevealOutlineProvider {

    private final int mStartLeft;
    private final int mStartRight;

    public PillWidthRevealOutlineProvider(Rect pillRect, int left, int right) {
        super(0, 0, pillRect);
        mOutline.set(pillRect);
        mStartLeft = left;
        mStartRight = right;
    }

    @Override
    public void setProgress(float progress) {
        mOutline.left = (int) (progress * mPillRect.left + (1 - progress) * mStartLeft);
        mOutline.right = (int) (progress * mPillRect.right + (1 - progress) * mStartRight);
    }
}

