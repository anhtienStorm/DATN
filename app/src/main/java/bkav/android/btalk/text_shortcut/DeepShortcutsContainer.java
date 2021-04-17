/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bkav.android.btalk.text_shortcut;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.datamodel.data.QuickResponseData;

/**
 * A container for shortcuts to deep links within apps.
 */

public class DeepShortcutsContainer extends LinearLayout implements View.OnTouchListener {

    private static final String TAG = "ShortcutsContainer";

    public static final int MAX_SHORTCUTS = 5;

    public static final int MORE_ITEM_ID = 10;

    private final Point mIconShift = new Point();

    private final int mStartDragThreshold;

    private final boolean mIsRtl;

    private Point mIconLastTouchPos = new Point();

    private boolean mIsLeftAligned;

    private boolean mIsAboveIcon;

    private View mArrow;

    private Animator mOpenCloseAnimator;

    private boolean mDeferContainerRemoval;

    private boolean mIsOpen;

    private int mScreenHeight;

    private int mScreenWidth;

    public DeepShortcutsContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mStartDragThreshold = getResources().getDimensionPixelSize(
                R.dimen.deep_shortcuts_start_drag_threshold);
        mIsRtl = Utilities.isRtl(getResources());

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mScreenHeight = dm.heightPixels;
        mScreenWidth = dm.widthPixels;
    }

    public void init(Rect archoView, ViewGroup parentView) {
        mArchorView = archoView;
        mParentView = parentView;
    }

    public DeepShortcutsContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeepShortcutsContainer(Context context) {
        this(context, null, 0);
    }

    public void populateAndShow(final List<ActionItem> items, boolean showAll) {
        mMenuItems = new ArrayList<>();
        mMenuItems.addAll(items);

        final Resources resources = getResources();
        final int arrowWidth = resources.getDimensionPixelSize(R.dimen.deep_shortcuts_arrow_width);
        final int arrowHeight = resources
                .getDimensionPixelSize(R.dimen.deep_shortcuts_arrow_height);
        final int arrowHorizontalOffset = resources
                .getDimensionPixelSize(R.dimen.deep_shortcuts_arrow_horizontal_offset);
        final int arrowVerticalOffset = resources
                .getDimensionPixelSize(R.dimen.deep_shortcuts_arrow_vertical_offset);

        // Add dummy views first, and populate with real shortcut info when ready.
        final int spacing = getResources().getDimensionPixelSize(R.dimen.deep_shortcuts_spacing);
        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mHaveMoreItem = !showAll && items.size() > MAX_SHORTCUTS;
        int numShortcuts = mHaveMoreItem ? MAX_SHORTCUTS : items.size();
        for (int i = 0; i < numShortcuts; i++) {
            final DeepShortcutView shortcut = (DeepShortcutView) inflater.inflate(
                    R.layout.deep_shortcut, this, false);
            if (i == 0) {
                shortcut.setWillDrawIcon(true);
            }else {
                shortcut.setWillDrawIcon(false);
            }
            if (i < numShortcuts - 1) {
                ((LayoutParams) shortcut.getLayoutParams()).bottomMargin = spacing;
            }
            addView(shortcut);
            mShortcutItem.add(shortcut);
        }

        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        orientAboutIcon(arrowHeight + arrowVerticalOffset);

        // Add the arrow.
        mArrow = addArrowView(arrowHorizontalOffset, arrowVerticalOffset, arrowWidth, arrowHeight);
        mArrow.setPivotX(arrowWidth / 2);
        mArrow.setPivotY(mIsAboveIcon ? 0 : arrowHeight);

        animateOpen();

        //QuangNHe: cap nhat thu tu xuat hien item
//        sortActionItem();

        // Load the shortcuts on a background thread and update the container as it animates.
        final Handler uiHandler = new Handler();
        uiHandler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                int numShortcuts = mHaveMoreItem ? MAX_SHORTCUTS : items.size();
                int start, end;
                if (mHaveMoreItem) {
                    final ActionItem menuItemInfo = new ActionItem(
                            ApiCompatibilityUtils.getDrawable(getResources(),
                                    R.drawable.ic_context_menu_copy_link_text), null);
                    uiHandler.post(new UpdateMenuChild(mIsAboveIcon ? 0 : MAX_SHORTCUTS - 1,
                            menuItemInfo));
                    start = mIsAboveIcon ? numShortcuts - 1 : 0;
                    end = mIsAboveIcon ? 1 : numShortcuts - 2;
                } else {
                    start = mIsAboveIcon ? numShortcuts - 1 : 0;
                    end = mIsAboveIcon ? 0 : numShortcuts - 1;
                }
                if (start < end)
                    for (int i = start; i <= end; i++) {
                        final ActionItem menuItemInfo = mMenuItems.get(i - start);
                        uiHandler.post(new UpdateMenuChild(i, menuItemInfo));
                    }
                else
                    for (int i = start; i >= end; i--) {
                        final ActionItem menuItemInfo = mMenuItems.get(start - i);
                        uiHandler.post(new UpdateMenuChild(i, menuItemInfo));
                    }
            }
        });
    }

    /**
     * Updates the child of this container at the given index based on the given shortcut info.
     */
    private class UpdateMenuChild implements Runnable {

        private int mShortcutChildIndex;

        private ActionItem mMenuItemInfo;

        public UpdateMenuChild(int shortcutChildIndex, ActionItem info) {
            mShortcutChildIndex = shortcutChildIndex;
            mMenuItemInfo = info;
        }

        @Override
        public void run() {
            getShortcutAt(mShortcutChildIndex).applyShortcutInfo(mMenuItemInfo,
                    DeepShortcutsContainer.this);
        }
    }

    private DeepShortcutView getShortcutAt(int index) {
        if (!mIsAboveIcon) {
            // Opening down, so arrow is the first view.
            index++;
        }
        return (DeepShortcutView) getChildAt(index);
    }

    private int getShortcutCount() {
        // All children except the arrow are shortcuts.
        return getChildCount() - 1;
    }

    public void animateOpen() {
        setVisibility(View.VISIBLE);
        mIsOpen = true;

        final AnimatorSet shortcutAnims = new AnimatorSet();
        final int shortcutCount = getShortcutCount();

        final long duration = getResources().getInteger(R.integer.config_deepShortcutOpenDuration)
                * Utilities.BKAV_DURATION_ANIMATION_FACTOR;
        final long arrowScaleDuration = getResources().getInteger(
                R.integer.config_deepShortcutArrowOpenDuration)
                * Utilities.BKAV_DURATION_ANIMATION_FACTOR;
        final long arrowScaleDelay = duration - arrowScaleDuration;
        final long stagger = getResources().getInteger(R.integer.config_deepShortcutOpenStagger)
                * Utilities.BKAV_DURATION_ANIMATION_FACTOR;
        final TimeInterpolator fadeInterpolator = new LogAccelerateInterpolator(100, 0);

        // Animate shortcuts
        DecelerateInterpolator interpolator = new DecelerateInterpolator();
        for (int i = 0; i < shortcutCount; i++) {
            final DeepShortcutView deepShortcutView = getShortcutAt(i);
            deepShortcutView.setVisibility(INVISIBLE);
            deepShortcutView.setAlpha(0);

            Animator anim = deepShortcutView.createOpenAnimation(mIsAboveIcon, mIsLeftAligned);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    deepShortcutView.setVisibility(VISIBLE);
                }
            });
            anim.setDuration(duration);
            int animationIndex = mIsAboveIcon ? shortcutCount - i - 1 : i;
            anim.setStartDelay(stagger * animationIndex);
            anim.setInterpolator(interpolator);
            shortcutAnims.play(anim);

            Animator fadeAnim = ObjectAnimator.ofFloat(deepShortcutView, "alpha", 1f);
            fadeAnim.setInterpolator(fadeInterpolator);
            // We want the shortcut to be fully opaque before the arrow starts animating.
            fadeAnim.setDuration(arrowScaleDelay);
            shortcutAnims.play(fadeAnim);
        }

        // Animate the arrow
        mArrow.setScaleX(0);
        mArrow.setScaleY(0);

        PropertyValuesHolder scaleXProp = PropertyValuesHolder.ofFloat(SCALE_X, 1f);
        PropertyValuesHolder scaleYProp = PropertyValuesHolder.ofFloat(SCALE_Y, 1f);
        ObjectAnimator arrowScale = ObjectAnimator.ofPropertyValuesHolder(mArrow, scaleXProp,
                scaleYProp);

        arrowScale.setStartDelay(arrowScaleDelay);
        arrowScale.setDuration(arrowScaleDuration);
        shortcutAnims.play(arrowScale);

        mOpenCloseAnimator = shortcutAnims;
        shortcutAnims.start();
    }

    /**
     * Orients this container above or below the given icon, aligning with the left or right.
     * <p>
     * These are the preferred orientations, in order (RTL prefers right-aligned over left):
     * - Above and left-aligned
     * - Above and right-aligned
     * - Below and left-aligned
     * - Below and right-aligned
     * <p>
     * So we always align left if there is enough horizontal space
     * and align above if there is enough vertical space.
     */
    private void orientAboutIcon(int arrowHeight) {
        // Bkav QuangLH: TODO: code goc cua Launcher thay co cai Rect insets cung tham gia vao
        // tinh toan. Debug thay la (0, 72, 0, 0). Khong biet la tinh the nao, de lam gi. Co the
        // anh huong toi tinh toan vi tri.

        int width = getMeasuredWidth();
        int height = getMeasuredHeight() + arrowHeight;

        // Align left (right in RTL) if there is room.
        int leftAlignedX = mArchorView.left;
        int rightAlignedX = mArchorView.right - width;
        int x = leftAlignedX;
        boolean canBeLeftAligned = leftAlignedX + width < mScreenWidth;
        boolean canBeRightAligned = rightAlignedX > 0;
        if (!canBeLeftAligned || (mIsRtl && canBeRightAligned)) {
            x = rightAlignedX;
        }
        mIsLeftAligned = x == leftAlignedX;
        if (mIsRtl) {
            x -= mScreenWidth - width;
        }

        // Offset x so that the arrow and shortcut icons are center-aligned with the original icon.
        Resources resources = getResources();
        int xOffset;
        if (isAlignedWithStart()) {
            // Aligning with the shortcut icon.
            int shortcutIconWidth = resources
                    .getDimensionPixelSize(R.dimen.deep_shortcut_icon_size);
            int shortcutPaddingStart = resources
                    .getDimensionPixelSize(R.dimen.deep_shortcut_padding_start);
            xOffset = mArchorView.width() / 2 - shortcutIconWidth / 2 - shortcutPaddingStart;
        } else {
            // Aligning with the drag handle.
            int shortcutDragHandleWidth = resources
                    .getDimensionPixelSize(R.dimen.deep_shortcut_drag_handle_size);
            int shortcutPaddingEnd = resources
                    .getDimensionPixelSize(R.dimen.deep_shortcut_padding_end);
            xOffset = mArchorView.width() / 2 - shortcutDragHandleWidth / 2 - shortcutPaddingEnd;
        }
        x += mIsLeftAligned ? xOffset : -xOffset;

        // Open above icon if there is room.
        int y = mArchorView.top - height;
        mIsAboveIcon = true;//Bkav QuangNDb sua de cho no luon above icon
//        mIsAboveIcon = y > 0;
//        if (!mIsAboveIcon) {
//            y = mArchorView.bottom;
//        }

        y += getContext().getResources().getDimensionPixelSize(R.dimen.status_bar_height);

        setX(x);
        setY(y);
    }

    private boolean isAlignedWithStart() {
        return mIsLeftAligned && !mIsRtl || !mIsLeftAligned && mIsRtl;
    }

    /**
     * Adds an arrow view pointing at the original icon.
     *
     * @param horizontalOffset the horizontal offset of the arrow, so that it
     *                         points at the center of the original icon
     */
    private View addArrowView(int horizontalOffset, int verticalOffset, int width, int height) {
        LayoutParams layoutParams = new LayoutParams(width, height);
        if (mIsLeftAligned) {
            layoutParams.gravity = Gravity.LEFT;
            layoutParams.leftMargin = horizontalOffset;
        } else {
            layoutParams.gravity = Gravity.RIGHT;
            layoutParams.rightMargin = horizontalOffset;
        }
        if (mIsAboveIcon) {
            layoutParams.topMargin = verticalOffset;
        } else {
            layoutParams.bottomMargin = verticalOffset;
        }

        View arrowView = new View(getContext());
        ShapeDrawable arrowDrawable = new ShapeDrawable(TriangleShape.create(width, height,
                !mIsAboveIcon));
        arrowDrawable.getPaint().setColor(Color.WHITE);
        arrowView.setBackground(arrowDrawable);
        arrowView.setElevation(getElevation());
        addView(arrowView, mIsAboveIcon ? getChildCount() : 0, layoutParams);
        return arrowView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        // Touched a shortcut, update where it was touched so we can drag from there on long click.
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mIconLastTouchPos.set((int) ev.getX(), (int) ev.getY());
                break;
        }
        return false;
    }

    public void animateClose() {
        // QuangNhe:
        mState = POPUP_STATE.NONE;
        mShortcutItem.clear();
        mCallback.setDeepShortcutContainer(null);

        if (!mIsOpen) {
            return;
        }
        if (mOpenCloseAnimator != null) {
            mOpenCloseAnimator.cancel();
        }
        mIsOpen = false;

        final AnimatorSet shortcutAnims = new AnimatorSet();
        final int shortcutCount = getShortcutCount();
        int numOpenShortcuts = 0;
        for (int i = 0; i < shortcutCount; i++) {
            if (getShortcutAt(i).isOpenOrOpening()) {
                numOpenShortcuts++;
            }
        }
        final long duration = getResources().getInteger(R.integer.config_deepShortcutCloseDuration)
                * Utilities.BKAV_DURATION_ANIMATION_FACTOR;
        final long arrowScaleDuration = getResources().getInteger(
                R.integer.config_deepShortcutArrowOpenDuration)
                * Utilities.BKAV_DURATION_ANIMATION_FACTOR;
        final long stagger = getResources().getInteger(R.integer.config_deepShortcutCloseStagger)
                * Utilities.BKAV_DURATION_ANIMATION_FACTOR;
        final TimeInterpolator fadeInterpolator = new LogAccelerateInterpolator(100, 0);

        int firstOpenShortcutIndex = mIsAboveIcon ? shortcutCount - numOpenShortcuts : 0;
        for (int i = firstOpenShortcutIndex; i < firstOpenShortcutIndex + numOpenShortcuts; i++) {
            final DeepShortcutView view = getShortcutAt(i);
            Animator anim;
            //            if (view.willDrawIcon()) {
            anim = view.createCloseAnimation(mIsAboveIcon, mIsLeftAligned, duration);
            int animationIndex = mIsAboveIcon ? i - firstOpenShortcutIndex : numOpenShortcuts - i
                    - 1;
            anim.setStartDelay(stagger * animationIndex);

            Animator fadeAnim = ObjectAnimator.ofFloat(view, "alpha", 0f);
            // Don't start fading until the arrow is gone.
            fadeAnim.setStartDelay(stagger * animationIndex + arrowScaleDuration);
            fadeAnim.setDuration(duration - arrowScaleDuration);
            fadeAnim.setInterpolator(fadeInterpolator);
            shortcutAnims.play(fadeAnim);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(INVISIBLE);
                }
            });
            shortcutAnims.play(anim);
        }

        PropertyValuesHolder scaleXProp = PropertyValuesHolder.ofFloat(SCALE_X, 0f);
        PropertyValuesHolder scaleYProp = PropertyValuesHolder.ofFloat(SCALE_Y, 0f);
        ObjectAnimator arrowAnim = ObjectAnimator.ofPropertyValuesHolder(mArrow, scaleXProp,
                scaleYProp).setDuration(arrowScaleDuration);
        arrowAnim.setStartDelay(0);
        shortcutAnims.play(arrowAnim);

        shortcutAnims.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mOpenCloseAnimator = null;
                if (mDeferContainerRemoval) {
                    setVisibility(INVISIBLE);
                } else {
                    close();
                }
            }
        });
        mOpenCloseAnimator = shortcutAnims;
        shortcutAnims.start();
    }

    /**
     * Closes the folder without animation.
     */
    public void close() {
        if (mOpenCloseAnimator != null) {
            mOpenCloseAnimator.cancel();
            mOpenCloseAnimator = null;
        }
        mIsOpen = false;
        mDeferContainerRemoval = false;

        mParentView.removeView(this);
    }

    public boolean isOpen() {
        return mIsOpen;
    }

    /**
     * Shows the shortcuts container for {@param icon}
     *
     * @return the container if shown or null.
     */
    public static DeepShortcutsContainer showForIcon(Rect archorView, ViewGroup parentView,
                                                     List<ActionItem> items, UpdateDeepShortcutsContainerCallback callback, boolean showAll) {
        if (getOpenShortcutsContainer(parentView) != null) {
            // There is already a shortcuts container open, so don't open this one.
            return null;
        }

        if (!items.isEmpty()) {
            // There are shortcuts associated with the app, so defer its drag.
            LayoutInflater layoutInflater = (LayoutInflater) parentView.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final DeepShortcutsContainer container = (DeepShortcutsContainer) layoutInflater
                    .inflate(R.layout.deep_shortcuts_container, parentView, false);
            container.init(archorView, parentView);
            container.setVisibility(View.INVISIBLE);
            parentView.addView(container);
            container.setCallback(callback);

            container.populateAndShow(items, showAll);
            mState = POPUP_STATE.DRAGGING;
            return container;
        }
        return null;
    }

    public static DeepShortcutsContainer showForIcon(float x, float y, ViewGroup parentView,
                                                     List<ActionItem> items, UpdateDeepShortcutsContainerCallback callback) {
        int anchorViewFakeHeight = parentView.getContext().getResources()
                .getDimensionPixelSize(R.dimen.anchor_view_fake_height);

        Rect archorView = new Rect((int) x - anchorViewFakeHeight / 2, (int) y
                - anchorViewFakeHeight / 2, (int) x + anchorViewFakeHeight / 2, (int) y
                - anchorViewFakeHeight / 2);

        return showForIcon(archorView, parentView, items, callback, false);
    }

    public static DeepShortcutsContainer showForIconTest(float x, float y, ViewGroup parentView,
                                                         List<ActionItem> items, UpdateDeepShortcutsContainerCallback callback) {
        int anchorViewFakeHeight = parentView.getContext().getResources()
                .getDimensionPixelSize(R.dimen.anchor_view_fake_height);

        Rect archorView = new Rect((int) x - anchorViewFakeHeight / 2, (int) y
                - anchorViewFakeHeight / 2, (int) x + anchorViewFakeHeight / 2, (int) y
                - anchorViewFakeHeight / 2);

        return showForIcon(archorView, parentView, items, callback, false);
    }

    public static DeepShortcutsContainer getOpenShortcutsContainer(ViewGroup parentView) {
        // Iterate in reverse order. Shortcuts container is added later to the dragLayer,
        // and will be one of the last views.
        for (int i = parentView.getChildCount() - 1; i >= 0; i--) {
            View child = parentView.getChildAt(i);
            if (child instanceof DeepShortcutsContainer
                    && ((DeepShortcutsContainer) child).isOpen()) {
                return (DeepShortcutsContainer) child;
            }
        }
        return null;
    }

    /*******************BKAV****************/
    private static final int VIEW_ZOOM_DURATION = 150;

    private Rect mArchorView;

    private ViewGroup mParentView;

    private List<ActionItem> mMenuItems;

    private List<DeepShortcutView> mShortcutItem = new ArrayList<>();

    private DeepShortcutView mSelectedShortcut = null;

    private boolean mHaveMoreItem = false;

    private UpdateDeepShortcutsContainerCallback mCallback;

    private static enum POPUP_STATE {
        NONE, DRAGGING, SHOWING
    }

    private static POPUP_STATE mState = POPUP_STATE.NONE;

    public void setArrowDrawable(int color) {
        ShapeDrawable arrowDrawable = new ShapeDrawable(TriangleShape.create(mArrow.getWidth(),
                mArrow.getHeight(), !mIsAboveIcon));
        arrowDrawable.getPaint().setColor(color);
        mArrow.setBackground(arrowDrawable);
    }

    public boolean isIsAboveIcon() {
        return mIsAboveIcon;
    }

    public boolean onProcessEvent(MotionEvent event) {
        switch (mState) {
            case NONE:
                return false;

            case DRAGGING:
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // QuangNHe: check getActionItem != null vi co ve nhu doi luc loi xay ra do luong cap nhat item khong
                    // chay kip nen tra ve null
                    if (mSelectedShortcut != null && mSelectedShortcut.getActionItem() != null) {
                        onItemClick(mSelectedShortcut.getActionItem().getQuickResponseData());
                        mSelectedShortcut = null;
                        return true;
                    }
                    mState = POPUP_STATE.SHOWING;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    sendTouchEventToMenuItemViewChangeBackGround(event);
                }
                return true;

            case SHOWING:
                if (event.getAction() == MotionEvent.ACTION_DOWN && !Util.isInBounds(event, this)) {
                    animateClose();
                    return false;
                } else {
                    return false;
                }

            default:
                break;
        }

        return false;
    }

    private boolean sendTouchEventToMenuItemViewChangeBackGround(MotionEvent event) {
        int[] bubbleTextViewPos = new int[2];
        getLocationOnScreen(bubbleTextViewPos);

        int size = mShortcutItem.size();
        for (int index = 0; index < size; index++) {
            DeepShortcutView view = mShortcutItem.get(index);
            if (index == 0) {
                view.setWillDrawIcon(true);
            }else {
                view.setWillDrawIcon(false);
            }

            if (Util.isInBounds(event, view)) {
                // Bkav QuangLH: doi background cua View dang duoc di tay len va khoi phuc background
                // cua View truoc do.
                if (mSelectedShortcut != view) {
                    view.setBackgroundResource(R.drawable.bg_gray_pill);

                    int nextToArrowIndex = isIsAboveIcon() ? size - 1 : 0;
                    setArrowDrawable(index == nextToArrowIndex ? getResources().getColor(
                            R.color.gray_pill_color) : Color.WHITE);

                    if (mSelectedShortcut != null) {
                        mSelectedShortcut.setBackgroundResource(R.drawable.bg_white_pill);
                    }

                    mSelectedShortcut = view;
                }

                view.setFocusable(true);
                return true;
            } else {
                view.setFocusable(false);
            }
        }
        setArrowDrawable(Color.WHITE);

        if (mSelectedShortcut != null) {
            mSelectedShortcut.setBackgroundResource(R.drawable.bg_white_pill);
            mSelectedShortcut = null;
        }

        return false;
    }

    public interface OnActionItemClickListener {

        void onItemClick(QuickResponseData data);
        void onEditClick(QuickResponseData data);
    }

    protected OnActionItemClickListener mActionItemClickListener;

    public void setOnActionItemClickListener(OnActionItemClickListener listener) {
        mActionItemClickListener = listener;
    }

    public void onItemClick(QuickResponseData data) {
        animateClose();
        mActionItemClickListener.onItemClick(data);
    }

    /**Bkav QuangNDb click vao icon edit quick response*/
    public void onEditClick(QuickResponseData data) {
        animateClose();
        mActionItemClickListener.onEditClick(data);
    }


    /**
     * QuangNHe: check xem popup dang hien thi hay ko
     *
     * @author quangnhe
     */
    public static boolean isShowing() {
        return !POPUP_STATE.NONE.equals(mState);
    }

    void setCallback(UpdateDeepShortcutsContainerCallback callback) {
        mCallback = callback;
    }

    /**
     * QuangNHe: inteface dung de update DeepShortcutsContainer dang hien thi
     *
     * @author quangnhe
     */
    public interface UpdateDeepShortcutsContainerCallback {

        void setDeepShortcutContainer(DeepShortcutsContainer obj);
    }

    public void unbind() {
        mActionItemClickListener = null;
    }
}
