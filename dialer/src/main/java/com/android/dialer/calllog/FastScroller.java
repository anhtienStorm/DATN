package com.android.dialer.calllog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v13.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

// Bkav TienNAb: Class tao giao dien thanh fast scroll va xu ly cac su kien tren no

public class FastScroller extends LinearLayout {
    private static final int BUBBLE_ANIMATION_DURATION = 100;
    private static final int TRACK_SNAP_RANGE = 5;

    private TextView mBubble;
    private View mHandle;
    private RecyclerView mRecyclerView;
    private int mHeight;
    private boolean mIsInitialized = false;
    private ObjectAnimator mCurrentAnimator = null;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
            updateBubbleAndHandlePosition();
        }
    };

    public interface BubbleTextGetter {
        String getTextToShowInBubble(int pos);
    }

    public FastScroller(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public FastScroller(final Context context) {
        super(context);
        init(context);
    }

    public FastScroller(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    // Bkav TienNAb: khoi tao thanh fast scroll
    protected void init(Context context) {
        if (mIsInitialized)
            return;
        mIsInitialized = true;
        setOrientation(HORIZONTAL);
        setClipChildren(false);
    }

    // Bkav TienNAb: tao layout cho thanh fast scroll
    public void setViewsToUse(@LayoutRes int layoutResId, @IdRes int bubbleResId, @IdRes int handleResId) {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(layoutResId, this, true);
        mBubble = findViewById(bubbleResId);
        if (mBubble != null)
            mBubble.setVisibility(INVISIBLE);
        mHandle = findViewById(handleResId);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        updateBubbleAndHandlePosition();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < mHandle.getX() - ViewCompat.getPaddingStart(mHandle))
                    return false;
                if (mCurrentAnimator != null)
                    mCurrentAnimator.cancel();
                if (mBubble != null && mBubble.getVisibility() == INVISIBLE)
                    showBubble();
                mHandle.setSelected(true);
            case MotionEvent.ACTION_MOVE:
                final float y = event.getY();
                setBubbleAndHandlePosition(y);
                setRecyclerViewPosition(y);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mHandle.setSelected(false);
                hideBubble();
                return true;
        }
        return super.onTouchEvent(event);
    }

    // Bkav TienNAb: gan su kien scroll vao recyclerview
    public void setRecyclerView(final RecyclerView recyclerView) {
        if (this.mRecyclerView != recyclerView) {
            if (this.mRecyclerView != null)
                this.mRecyclerView.removeOnScrollListener(mOnScrollListener);
            this.mRecyclerView = recyclerView;
            if (this.mRecyclerView == null)
                return;
            recyclerView.addOnScrollListener(mOnScrollListener);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mRecyclerView != null) {
            mRecyclerView.removeOnScrollListener(mOnScrollListener);
            mRecyclerView = null;
        }
    }

    // Bkav TienNAb: set view cua recyclerview theo vi tri thanh cuon fast scroll
    private void setRecyclerViewPosition(float y) {
        if (mRecyclerView != null) {
            final int itemCount = mRecyclerView.getAdapter().getItemCount();
            float proportion;
            if (mHandle.getY() == 0)
                proportion = 0f;
            else if (mHandle.getY() + mHandle.getHeight() >= mHeight - TRACK_SNAP_RANGE)
                proportion = 1f;
            else
                proportion = y / (float) mHeight;
            final int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
            ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(targetPos, 0);
            final String bubbleText = ((BubbleTextGetter) mRecyclerView.getAdapter()).getTextToShowInBubble(targetPos);
            if (mBubble != null) {
                mBubble.setText(bubbleText);
                if (TextUtils.isEmpty(bubbleText)) {
                    hideBubble();
                } else if (mBubble.getVisibility() == View.INVISIBLE) {
                    showBubble();
                }
            }
        }
    }

    // Bkav TienNAb: lay vi tri item trong recyclerview theo vi tri thanh cuon fast scroll
    private int getValueInRange(int min, int max, int value) {
        int minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    // Bkav TienNAb: update vi tri cua bubble va handle
    private void updateBubbleAndHandlePosition() {
        if (mBubble == null || mHandle.isSelected())
            return;

        final int verticalScrollOffset = mRecyclerView.computeVerticalScrollOffset();
        final int verticalScrollRange = mRecyclerView.computeVerticalScrollRange();
        float proportion = (float) verticalScrollOffset / ((float) verticalScrollRange - mHeight);
        setBubbleAndHandlePosition(mHeight * proportion);
    }

    // Bkav TienNAb: set vi tri cho bubble va handle
    private void setBubbleAndHandlePosition(float y) {
        final int handleHeight = mHandle.getHeight();
        mHandle.setY(getValueInRange(0, mHeight - handleHeight, (int) (y - handleHeight / 2)));
        if (mBubble != null) {
            int bubbleHeight = mBubble.getHeight();
            mBubble.setY(getValueInRange(0, mHeight - bubbleHeight - handleHeight / 2, (int) (y - bubbleHeight)));
        }
    }

    // Bkav TienNAb: hien thi bubble
    private void showBubble() {
        if (mBubble == null)
            return;
        mBubble.setVisibility(VISIBLE);
        if (mCurrentAnimator != null)
            mCurrentAnimator.cancel();
        mCurrentAnimator = ObjectAnimator.ofFloat(mBubble, "alpha", 0f, 1f).setDuration(BUBBLE_ANIMATION_DURATION);
        mCurrentAnimator.start();
    }

    // Bkav TienNAb: an bubble
    private void hideBubble() {
        if (mBubble == null)
            return;
        if (mCurrentAnimator != null)
            mCurrentAnimator.cancel();
        mCurrentAnimator = ObjectAnimator.ofFloat(mBubble, "alpha", 1f, 0f).setDuration(BUBBLE_ANIMATION_DURATION);
        mCurrentAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBubble.setVisibility(INVISIBLE);
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                mBubble.setVisibility(INVISIBLE);
                mCurrentAnimator = null;
            }
        });
        mCurrentAnimator.start();
    }
}
