package bkav.android.btalk.mutilsim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * Created by anhdt on 27/07/2017.
 */

public abstract class RevealOutlineAnimation extends ViewOutlineProvider {
    protected Rect mOutline;
    protected float mOutlineRadius;

    public RevealOutlineAnimation() {
        mOutline = new Rect();
    }

    /** Returns whether elevation should be removed for the duration of the reveal animation. */
    abstract boolean shouldRemoveElevationDuringAnimation();
    /** Sets the progress, from 0 to 1, of the reveal animation. */
    abstract void setProgress(float progress);

    public ValueAnimator createRevealAnimator(final View revealView) {
        return createRevealAnimator(revealView, false);
    }

    public ValueAnimator createRevealAnimator(final View revealView, boolean isReversed) {
        ValueAnimator va =
                isReversed ? ValueAnimator.ofFloat(1f, 0f) : ValueAnimator.ofFloat(0f, 1f);
        final float elevation = revealView.getElevation();

        va.addListener(new AnimatorListenerAdapter() {
            private boolean mWasCanceled = false;

            public void onAnimationStart(Animator animation) {
                // Anhdts bo thuoc tinh bo tron
//                revealView.setOutlineProvider(RevealOutlineAnimation.this);
//                revealView.setClipToOutline(true);
                if (shouldRemoveElevationDuringAnimation()) {
                    revealView.setTranslationZ(-elevation);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mWasCanceled = true;
            }

            public void onAnimationEnd(Animator animation) {
                if (!mWasCanceled) {
                    // Anhdts bo thuoc tinh bo tron
//                    revealView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
//                    revealView.setClipToOutline(false);
                    if (shouldRemoveElevationDuringAnimation()) {
                        revealView.setTranslationZ(0);
                    }
                }
            }

        });

        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                float progress = (Float) arg0.getAnimatedValue();
                setProgress(progress);
                revealView.invalidateOutline();
            }
        });
        return va;
    }

    @Override
    public void getOutline(View v, Outline outline) {
        outline.setRoundRect(mOutline, mOutlineRadius);
    }
}
