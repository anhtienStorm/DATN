package bkav.android.btalk.mutilsim;

import android.graphics.Rect;

/**
 * Created by anhdt on 27/07/2017.
 */

public class PillRevealOutlineProvider extends RevealOutlineAnimation {

    private int mCenterX;
    private int mCenterY;
    protected Rect mPillRect;

    /**
     * @param x reveal center x
     * @param y reveal center y
     * @param pillRect round rect that represents the final pill shape
     */
    public PillRevealOutlineProvider(int x, int y, Rect pillRect) {
        mCenterX = x;
        mCenterY = y;
        mPillRect = pillRect;
        mOutlineRadius = pillRect.height() / 2f;
    }

    @Override
    public boolean shouldRemoveElevationDuringAnimation() {
        return false;
    }

    @Override
    public void setProgress(float progress) {
        // Assumes width is greater than height.
        int centerToEdge = Math.max(mCenterX, mPillRect.width() - mCenterX);
        int currentSize = (int) (progress * centerToEdge);

        // Bound the outline to the final pill shape defined by mPillRect.
        mOutline.left = Math.max(mPillRect.left, mCenterX - currentSize);
        mOutline.top = Math.max(mPillRect.top, mCenterY - currentSize);
        mOutline.right = Math.min(mPillRect.right, mCenterX + currentSize);
        mOutline.bottom = Math.min(mPillRect.bottom, mCenterY + currentSize);
        mOutlineRadius = mOutline.height() / 2;
    }
}
