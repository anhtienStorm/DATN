/**
 * Copyright (c) 2015 500px Inc.
 * Copyright 2017 Manabu-GT
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.ms_square.etsyblur;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

public class BlurringView extends View {

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    private static final String TAG = BlurringView.class.getSimpleName();

    protected BlurConfig blurConfig;

    protected BlurEngine blur;

    protected View blurredView;

    private int blurredViewWidth;

    private int blurredViewHeight;

    protected Bitmap bitmapToBlur;

    protected Canvas blurringCanvas;

    /**
     * Flag used to prevent draw() from being recursively called when blurredView is set to the parent view
     */
    protected boolean parentViewDrawn;

    private Bitmap mBlurredTemp;

    public BlurringView(Context context) {
        this(context, null);
    }

    public BlurringView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlurringView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mIsBlurStarted = false;
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.BlurringView);
        int overlayColor = typedArray.getInt(R.styleable.BlurringView_overlayColor, BlurConfig.DEFAULT_OVERLAY_COLOR);
        int blurRadius = typedArray.getInt(R.styleable.BlurringView_radius, BlurConfig.DEFAULT_RADIUS);
        int downScaleFactor = typedArray.getInt(R.styleable.BlurringView_downScaleFactor, BlurConfig.DEFAULT_DOWN_SCALE_FACTOR);
        boolean allowFallback = typedArray.getBoolean(R.styleable.BlurringView_allowFallback, BlurConfig.DEFAULT_ALLOW_FALLBACK);
        boolean debug = typedArray.getBoolean(R.styleable.BlurringView_debug, BlurConfig.DEFAULT_DEBUG);
        int filterColor = typedArray.getInt(R.styleable.BlurringView_filterColor, BlurConfig.DEFAULT_FILTER_COLOR);
        int filterDepth = typedArray.getInt(R.styleable.BlurringView_filterDepth, BlurConfig.DEFAULT_FILTER_DEPTH);
        typedArray.recycle();

        blurConfig = new BlurConfig.Builder()
                .radius(blurRadius)
                .downScaleFactor(downScaleFactor)
                .allowFallback(allowFallback)
                .overlayColor(overlayColor)
                .debug(debug)
                .colorFilter(filterColor)
                .depth(filterDepth)
                .build();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (blurConfig == null) {
            throw new IllegalStateException("BlurConfig must be set before onAttachedToWindow() gets called.");
        }
        if (isInEditMode()) {
            blur = new NoBlur();
        } else {
            blur = new Blur(getContext(), blurConfig);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.blurredView != null && this.blurredView.getViewTreeObserver().isAlive()) {
            this.blurredView.getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
        }
        blur.destroy();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mIsBlurStarted) { // TODO Bkav TrungTH luu y bien nay khi set blurview
            System.out.print(" Bkav Blur break");
            return;
        }
        if (mIsStartBlurNav && !mIsFirstRun) {
            if (mBlurredTemp != null) {
                canvas.save();
                canvas.translate(blurredView.getX() - getX(), blurredView.getY() - getY());
                canvas.scale(blurConfig.downScaleFactor(), blurConfig.downScaleFactor());
                canvas.drawBitmap(mBlurredTemp, 0, 0, null);
                canvas.restore();
            }

            if (blurConfig.overlayColor() != Color.TRANSPARENT) {
                canvas.drawColor(blurConfig.overlayColor());
            }
            return;
        }
        mIsFirstRun = false;
        boolean isParent = (blurredView == getParent());
        if (isParent) {
            if (parentViewDrawn) {
                return;
            }
            parentViewDrawn = true;
        }
        if (blurredView != null) {
            if (prepare()) {
                // If the background of the blurred view is a color drawable, we use it to clear
                // the blurring canvas, which ensures that edges of the child views are blurred
                // as well; otherwise we clear the blurring canvas with a transparent color.
                if (blurredView.getBackground() != null && blurredView.getBackground() instanceof ColorDrawable) {
                    bitmapToBlur.eraseColor(((ColorDrawable) blurredView.getBackground()).getColor());
                } else {
                    bitmapToBlur.eraseColor(Color.TRANSPARENT);
                }

                blurringCanvas.save();
                blurringCanvas.translate(-blurredView.getScrollX(), -blurredView.getScrollY());
                blurredView.draw(blurringCanvas);
                blurringCanvas.restore();

                mBlurredTemp = blur.execute(bitmapToBlur, true);

                if (mBlurredTemp != null) {
                    canvas.save();
                    canvas.translate(blurredView.getX() - getX(), blurredView.getY() - getY());
                    canvas.scale(blurConfig.downScaleFactor(), blurConfig.downScaleFactor());
                    canvas.drawBitmap(mBlurredTemp, 0, 0, null);
                    canvas.restore();
                }

                if (blurConfig.overlayColor() != Color.TRANSPARENT) {
                    canvas.drawColor(blurConfig.overlayColor());
                }
            }
        }
        if (isParent) {
            parentViewDrawn = false;
        }
    }

    public void blurredView(@NonNull View blurredView) {
        if (this.blurredView != null && this.blurredView != blurredView) {
            if (this.blurredView.getViewTreeObserver().isAlive()) {
                this.blurredView.getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
            }
        }
        this.blurredView = blurredView;
        if (this.blurredView.getViewTreeObserver().isAlive()) {
            this.blurredView.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
        }
    }

    public void blurConfig(@NonNull BlurConfig blurConfig) {
        if (blur != null) {
            throw new IllegalStateException("BlurConfig must be set before onAttachedToWindow() gets called.");
        }
        this.blurConfig = blurConfig;
    }

    protected boolean prepare() {
        int newWidth = blurredView.getWidth();
        int newHeight = blurredView.getHeight();

        if (newWidth != blurredViewWidth || newHeight != blurredViewHeight) {
            blurredViewWidth = newWidth;
            blurredViewHeight = newHeight;

            int downScaleFactor = blurConfig.downScaleFactor();
            int scaledWidth = newWidth / downScaleFactor;
            int scaledHeight = newHeight / downScaleFactor;

            if (bitmapToBlur == null || scaledWidth != bitmapToBlur.getWidth()
                    || scaledHeight != bitmapToBlur.getHeight()) {

                // check whether valid width/height is given to create a bitmap
                if (scaledWidth <= 0 || scaledHeight <= 0) {
                    return false;
                }

                bitmapToBlur = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);

                if (bitmapToBlur == null) {
                    return false;
                }
            }

            blurringCanvas = new Canvas(bitmapToBlur);
            blurringCanvas.scale(1f / downScaleFactor, 1f / downScaleFactor);
        }

        return true;
    }

    protected final ViewTreeObserver.OnPreDrawListener preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            if (!isDirty() && blurredView.isDirty() && isShown()) {
                // blurredView is dirty, but BlurringView is not dirty and shown; thus, call invalidate to force re-draw
                invalidate();
            }
            return true;
        }
    };

    // ============================ Bkav TrungTh =============================
    protected boolean mIsBlurStarted = false;

    /**
     * Bkav TrungTH bo xung them ham setconfig de dung ben lib blur activty
     *
     * @param blurConfig
     */
    public void setBlurConfig(BlurConfig blurConfig) {
        this.blurConfig = blurConfig;
    }

    // Bkav TrungTh ham de bat dau start Blur view
    public void startBlur(boolean b) {
        mIsBlurStarted = true;
        invalidate();
    }

    private boolean mIsStartBlurNav = false;

    private boolean mIsFirstRun = true;

    public void startBlur() {
        mIsStartBlurNav = true;
        mIsFirstRun = true;
    }

    /**
     * Anhdts
     * them view background
     */
    public void blurredView(View viewToBlur, int colorId) {
        blurredView(viewToBlur);
    }
}
