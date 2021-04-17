package bkav.android.btalk.mutilsim;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import bkav.android.btalk.R;

/**
 * Created by anhdt on 27/07/2017.
 */

public class BtalkSimLineChooser extends RelativeLayout implements ValueAnimator.AnimatorUpdateListener {

    private static final Point sTempPoint = new Point();

    private final Rect mPillRect;

    private final int mWidth;

    private final int mHeight;

    private TextView mNameSim;
    private ImageView mIconView;
    private TextView mPhoneSim;

    private float mOpenAnimationProgress;

    public BtalkSimLineChooser(Context context) {
        this(context, null, 0);
    }

    public BtalkSimLineChooser(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BtalkSimLineChooser(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mWidth = getResources().getDimensionPixelOffset(R.dimen.width_sim_line);
        mHeight = getResources().getDimensionPixelOffset(R.dimen.height_sim_line);
        mPillRect = new Rect(0, 0, mWidth, mHeight);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIconView = (ImageView) findViewById(R.id.sim_icon);
        mNameSim = (TextView) findViewById(R.id.sim_name);
        mPhoneSim = (TextView) findViewById(R.id.sim_phone);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mPillRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    /** package private **/
    void applyInfo(Drawable icon, String name) {
        mIconView.setImageDrawable(icon);
        mNameSim.setText(name);
    }

    void setPhone(String number) {
        if(mPhoneSim !=null) {
            mPhoneSim.setVisibility(GONE);
        }
        // TODO Tam thoi dong do khong lay duoc phone tu sim
//        if (TextUtils.isEmpty(number)) {
//            mPhoneSim.setVisibility(GONE);
//        } else {
//            mPhoneSim.setVisibility(VISIBLE);
//            mPhoneSim.setText(number);
//        }
    }

    /**
     * Creates an animator to play when the shortcut container is being opened.
     */
    public Animator createOpenAnimation(boolean isContainerAboveIcon, boolean pivotLeft) {
        Point center = getIconCenter();
        ValueAnimator openAnimator = new ZoomRevealOutlineProvider(center.x, center.y,
                mPillRect, this, mIconView, isContainerAboveIcon, pivotLeft)
                .createRevealAnimator(this, false);
        mOpenAnimationProgress = 0f;
        openAnimator.addUpdateListener(this);
        return openAnimator;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        mOpenAnimationProgress = valueAnimator.getAnimatedFraction();
    }

    public boolean isOpenOrOpening() {
        return mOpenAnimationProgress > 0;
    }

    /**
     * Creates an animator to play when the shortcut container is being closed.
     */
    public Animator createCloseAnimation(boolean isContainerAboveIcon, boolean pivotLeft,
                                         long duration) {
        Point center = getIconCenter();
        ValueAnimator closeAnimator = new ZoomRevealOutlineProvider(center.x, center.y,
                mPillRect, this, mIconView, isContainerAboveIcon, pivotLeft)
                .createRevealAnimator(this, true);
        // Scale down the duration and interpolator according to the progress
        // that the open animation was at when the close started.
        closeAnimator.setDuration((long) (duration * mOpenAnimationProgress));
        closeAnimator.setInterpolator(new CloseInterpolator(mOpenAnimationProgress));
        return closeAnimator;
    }

    /**
     * Creates an animator which clips the container to form a circle around the icon.
     */
    public Animator collapseToIcon() {
        int halfHeight = getMeasuredHeight() / 2;
        int iconCenterX = getIconCenter().x;
        return new PillWidthRevealOutlineProvider(mPillRect,
                iconCenterX - halfHeight, iconCenterX + halfHeight)
                .createRevealAnimator(this, true);
    }

    /**
     * Returns the position of the center of the icon relative to the container.
     */
    public Point getIconCenter() {
        sTempPoint.y = sTempPoint.x = mHeight / 2;
        return sTempPoint;
    }

    public boolean willDrawIcon() {
        return mIconView.getVisibility() == View.VISIBLE;
    }

    /**
     * Extension of {@link PillRevealOutlineProvider} which scales the icon based on the height.
     */
    private static class ZoomRevealOutlineProvider extends PillRevealOutlineProvider {

        private final View mTranslateView;
        private final View mZoomView;

        private final float mFullHeight;
        private final float mTranslateYMultiplier;

        private final boolean mPivotLeft;
        private final float mTranslateX;

        ZoomRevealOutlineProvider(int x, int y, Rect pillRect,
                                  View translateView, View zoomView, boolean isContainerAboveIcon, boolean pivotLeft) {
            super(x, y, pillRect);
            mTranslateView = translateView;
            mZoomView = zoomView;
            mFullHeight = pillRect.height();

            mTranslateYMultiplier = isContainerAboveIcon ? 0.5f : -0.5f;

            mPivotLeft = pivotLeft;
            mTranslateX = pivotLeft ? pillRect.height() / 2 : pillRect.right - pillRect.height() / 2;
        }

        @Override
        public void setProgress(float progress) {
            super.setProgress(progress);

            mZoomView.setScaleX(progress);
            mZoomView.setScaleY(progress);

            float height = mOutline.height();
            mTranslateView.setTranslationY(mTranslateYMultiplier * (mFullHeight - height));

            float pivotX = mPivotLeft ? (mOutline.left + height / 2) : (mOutline.right - height / 2);
            mTranslateView.setTranslationX(mTranslateX - pivotX);
        }
    }

    /**
     * An interpolator that reverses the current open animation progress.
     */
    private static class CloseInterpolator extends LogAccelerateInterpolator {
        private float mStartProgress;
        private float mRemainingProgress;

        /**
         * @param openAnimationProgress The progress that the open interpolator ended at.
         */
        CloseInterpolator(float openAnimationProgress) {
            super(100, 0);
            mStartProgress = 1f - openAnimationProgress;
            mRemainingProgress = openAnimationProgress;
        }

        @Override
        public float getInterpolation(float v) {
            return mStartProgress + super.getInterpolation(v) * mRemainingProgress;
        }
    }
}

