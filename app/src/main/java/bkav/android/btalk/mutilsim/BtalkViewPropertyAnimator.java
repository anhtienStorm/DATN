package bkav.android.btalk.mutilsim;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.view.View;
import android.view.ViewPropertyAnimator;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Created by anhdt on 27/07/2017.
 */

public class BtalkViewPropertyAnimator extends Animator implements Animator.AnimatorListener {

    enum Properties {
        TRANSLATION_X,
        TRANSLATION_Y,
        SCALE_X,
        SCALE_Y,
        ROTATION_Y,
        ALPHA,
        START_DELAY,
        DURATION,
        INTERPOLATOR,
        WITH_LAYER
    }
    EnumSet<Properties> mPropertiesToSet = EnumSet.noneOf(Properties.class);
    ViewPropertyAnimator mViewPropertyAnimator;
    View mTarget;

    float mTranslationX;
    float mTranslationY;
    float mScaleX;
    float mScaleY;
    float mRotationY;
    float mAlpha;
    long mStartDelay;
    long mDuration;
    TimeInterpolator mInterpolator;
    ArrayList<AnimatorListener> mListeners = new ArrayList<>();
    boolean mRunning = false;
    FirstFrameAnimatorHelper mFirstFrameHelper;

    public BtalkViewPropertyAnimator(View target) {
        mTarget = target;
    }

    @Override
    public void addListener(Animator.AnimatorListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void cancel() {
        if (mViewPropertyAnimator != null) {
            mViewPropertyAnimator.cancel();
        }
    }

    @Override
    public Animator clone() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void end() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    @Override
    public ArrayList<Animator.AnimatorListener> getListeners() {
        return mListeners;
    }

    @Override
    public long getStartDelay() {
        return mStartDelay;
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        for (int i = 0; i < mListeners.size(); i++) {
            Animator.AnimatorListener listener = mListeners.get(i);
            listener.onAnimationCancel(this);
        }
        mRunning = false;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        for (int i = 0; i < mListeners.size(); i++) {
            Animator.AnimatorListener listener = mListeners.get(i);
            listener.onAnimationEnd(this);
        }
        mRunning = false;
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        for (int i = 0; i < mListeners.size(); i++) {
            Animator.AnimatorListener listener = mListeners.get(i);
            listener.onAnimationRepeat(this);
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
        // This is the first time we get a handle to the internal ValueAnimator
        // used by the ViewPropertyAnimator.
        mFirstFrameHelper.onAnimationStart(animation);

        for (int i = 0; i < mListeners.size(); i++) {
            Animator.AnimatorListener listener = mListeners.get(i);
            listener.onAnimationStart(this);
        }
        mRunning = true;
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    @Override
    public boolean isStarted() {
        return mViewPropertyAnimator != null;
    }

    @Override
    public void removeAllListeners() {
        mListeners.clear();
    }

    @Override
    public void removeListener(Animator.AnimatorListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public Animator setDuration(long duration) {
        mPropertiesToSet.add(Properties.DURATION);
        mDuration = duration;
        return this;
    }

    @Override
    public void setInterpolator(TimeInterpolator value) {
        mPropertiesToSet.add(Properties.INTERPOLATOR);
        mInterpolator = value;
    }

    @Override
    public void setStartDelay(long startDelay) {
        mPropertiesToSet.add(Properties.START_DELAY);
        mStartDelay = startDelay;
    }

    @Override
    public void setTarget(Object target) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setupEndValues() {

    }

    @Override
    public void setupStartValues() {
    }

    @Override
    public void start() {
        mViewPropertyAnimator = mTarget.animate();

        // FirstFrameAnimatorHelper hooks itself up to the updates on the animator,
        // and then adjusts the play time to keep the first two frames jank-free
        mFirstFrameHelper = new FirstFrameAnimatorHelper(mViewPropertyAnimator, mTarget);

        if (mPropertiesToSet.contains(Properties.TRANSLATION_X)) {
            mViewPropertyAnimator.translationX(mTranslationX);
        }
        if (mPropertiesToSet.contains(Properties.TRANSLATION_Y)) {
            mViewPropertyAnimator.translationY(mTranslationY);
        }
        if (mPropertiesToSet.contains(Properties.SCALE_X)) {
            mViewPropertyAnimator.scaleX(mScaleX);
        }
        if (mPropertiesToSet.contains(Properties.ROTATION_Y)) {
            mViewPropertyAnimator.rotationY(mRotationY);
        }
        if (mPropertiesToSet.contains(Properties.SCALE_Y)) {
            mViewPropertyAnimator.scaleY(mScaleY);
        }
        if (mPropertiesToSet.contains(Properties.ALPHA)) {
            mViewPropertyAnimator.alpha(mAlpha);
        }
        if (mPropertiesToSet.contains(Properties.START_DELAY)) {
            mViewPropertyAnimator.setStartDelay(mStartDelay);
        }
        if (mPropertiesToSet.contains(Properties.DURATION)) {
            mViewPropertyAnimator.setDuration(mDuration);
        }
        if (mPropertiesToSet.contains(Properties.INTERPOLATOR)) {
            mViewPropertyAnimator.setInterpolator(mInterpolator);
        }
        if (mPropertiesToSet.contains(Properties.WITH_LAYER)) {
            mViewPropertyAnimator.withLayer();
        }
        mViewPropertyAnimator.setListener(this);
        mViewPropertyAnimator.start();
    }

    public BtalkViewPropertyAnimator translationX(float value) {
        mPropertiesToSet.add(Properties.TRANSLATION_X);
        mTranslationX = value;
        return this;
    }

    public BtalkViewPropertyAnimator translationY(float value) {
        mPropertiesToSet.add(Properties.TRANSLATION_Y);
        mTranslationY = value;
        return this;
    }

    public BtalkViewPropertyAnimator scaleX(float value) {
        mPropertiesToSet.add(Properties.SCALE_X);
        mScaleX = value;
        return this;
    }

    public BtalkViewPropertyAnimator scaleY(float value) {
        mPropertiesToSet.add(Properties.SCALE_Y);
        mScaleY = value;
        return this;
    }

    public BtalkViewPropertyAnimator rotationY(float value) {
        mPropertiesToSet.add(Properties.ROTATION_Y);
        mRotationY = value;
        return this;
    }

    public BtalkViewPropertyAnimator alpha(float value) {
        mPropertiesToSet.add(Properties.ALPHA);
        mAlpha = value;
        return this;
    }

    public BtalkViewPropertyAnimator withLayer() {
        mPropertiesToSet.add(Properties.WITH_LAYER);
        return this;
    }
}
