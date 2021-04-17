package bkav.android.btalk.fragments.dialpad;

/*
 * Copyright (C) 2016 Gil Vegliach
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


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import bkav.android.btalk.R;

/**
 * Created by anhdt on 20/05/2017.
 * Textview trong suot cho dialpad
 */

public class BtalkDialpadLetterTextViewBlur extends AppCompatTextView {
    private Bitmap mMaskBitmap;

    private Canvas mMaskCanvas;

    private Paint mPaint;

    private Drawable mBackground;

    private Bitmap mBackgroundBitmap;

    private Canvas mBackgroundCanvas;

    private Rect mTextBounds = new Rect();

    private String mTextStr;

    private int mPaddingLeft;

    private int mPaddingBottom;

    private int mPaddingTop;

    private boolean mAutoResize;

    private boolean mIsCenter;


    public BtalkDialpadLetterTextViewBlur(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TextBlurCustom, 0, 0);
        try {
            mPaddingLeft = ta.getDimensionPixelOffset(R.styleable.TextBlurCustom_blur_padding_left, 0); // Mac dinh de 0
            mPaddingBottom = ta.getDimensionPixelOffset(R.styleable.TextBlurCustom_blur_padding_bottom, 0); // Mac dinh de 0
            mPaddingTop = ta.getDimensionPixelOffset(R.styleable.TextBlurCustom_blur_padding_top, 0);
            mAutoResize = ta.getBoolean(R.styleable.TextBlurCustom_blur_auto_resize, false);
            mIsCenter = ta.getBoolean(R.styleable.TextBlurCustom_blur_center, false);
        } finally {
            ta.recycle();
        }
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        super.setTextColor(Color.BLACK);
        super.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    @Deprecated
    public void setBackgroundDrawable(final Drawable bg) {
        if (mBackground == bg) {
            return;
        }

        mBackground = bg;

        // Will always draw drawable using view bounds. This might be a
        // problem if the drawable should force the view to be bigger, e.g.
        // the view sets its dimensions to wrap_content and the drawable
        // is larger than the text.
        int w = getWidth();
        int h = getHeight();
        if (mBackground != null && w != 0 && h != 0) {
            mBackground.setBounds(0, 0, w, h);
        }
        requestLayout();
        invalidate();
    }

    private static final int MAGIC_DISTANCE = 30;

    /**
     * Draw the text to fit within the height/width which have been specified during measurement.
     */
    public void draw(Canvas canvas, Paint paint) {
        if (mAutoResize) {
            float sizeTextMain = getResources().getDimensionPixelSize(R.dimen.dialpad_key_numbers_size);
            float sizeTextBlur = getResources().getDimensionPixelSize(R.dimen.btalk_dialpad_key_letters_size);
            getPaint().setTextSize(sizeTextMain);
            Rect rect = new Rect();
            getPaint().getTextBounds("0", 0, 1, rect);
            getPaint().setTextSize(sizeTextBlur);

            int heightBottomMain = (getHeight() - rect.height()) / 2;
            int heightBottomBlur = heightBottomMain - mPaddingBottom - mTextBounds.height();

            if (heightBottomBlur < 20) {
                int range = MAGIC_DISTANCE - heightBottomBlur;
                mPaddingBottom = mPaddingBottom - range > 0 ? mPaddingBottom - range : 0;
            } else if (heightBottomBlur > 40) {
                int range = heightBottomBlur - MAGIC_DISTANCE;
                mPaddingBottom = mPaddingBottom + range;
            }
//            Log.v("Anhdts", "heightBottomBlur " + heightBottomBlur + " height " + mPaddingBottom);
        }
        int marginIfNeed = mPaddingLeft;
        // The text bounds values are relative and can be negative,, so rather than specifying a
        // standard origin such as 0, 0, we need to use negative of the left/top bounds.
        // For example, the bounds may be: Left: 11, Right: 37, Top: -77, Bottom: 0
        // de can giua cai phim * # su dung chieu cao chuan cua text tru di chieu cao chu chia 2
        int width = mTextBounds.width();

        if (mIsCenter) {
            canvas.drawText(mTextStr, (getWidth() - width) / 2 + marginIfNeed,
                    getMeasuredHeight() / 2 + mPaddingTop, paint);
        } else {
            canvas.drawText(mTextStr, (getWidth() - width) / 2 + marginIfNeed,
                    getMeasuredHeight() - getPaddingBottom() - mPaddingBottom, paint);
        }
    }

    /**
     * Calculate the pixel-accurate bounds of the text when rendered, and use that to specify the
     * height and width.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mTextStr = getText().toString();
        getPaint().getTextBounds(mTextStr, 0, mTextStr.length(), mTextBounds);
    }

    @Override
    public void setBackgroundColor(final int color) {
        setBackground(new ColorDrawable(color));
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w == 0 || h == 0) {
            freeBitmaps();
            return;
        }

        createBitmaps(w, h);
        if (mBackground != null) {
            mBackground.setBounds(0, 0, w, h);
        }
    }

    private void createBitmaps(int w, int h) {
        mBackgroundBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mBackgroundCanvas = new Canvas(mBackgroundBitmap);
        mMaskBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8);
        mMaskCanvas = new Canvas(mMaskBitmap);
    }

    private void freeBitmaps() {
        mBackgroundBitmap = null;
        mBackgroundCanvas = null;
        mMaskBitmap = null;
        mMaskCanvas = null;
        mIsDrawComplete = false;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        if (isNothingToDraw()) {
            return;
        }
        if (!mIsDrawComplete) {
            // ve bitmap voi text mau den
            drawMask();
            // ve transparent phan text, su dung bitmap voi text den ve de len
            drawBackground();
            // ve lai background
        }
        canvas.drawBitmap(mBackgroundBitmap, 0.f, 0.f, null);

    }

    private boolean isNothingToDraw() {
        return mBackground == null
                || getWidth() == 0
                || getHeight() == 0;
    }

    // draw() calls onDraw() leading to stack overflow
    @SuppressLint("WrongCall")
    private void drawMask() {
        clear(mMaskCanvas);
        draw(mMaskCanvas, getPaint());
    }

    private void drawBackground() {
        clear(mBackgroundCanvas);
        mBackground.draw(mBackgroundCanvas);
        mBackgroundCanvas.drawBitmap(mMaskBitmap, 0.f, 0.f, mPaint);
        mIsDrawComplete = true;
    }

    private boolean mIsDrawComplete = false;

    private static void clear(Canvas canvas) {
        canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
    }


}
