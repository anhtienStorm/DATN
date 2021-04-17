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

public class BtalkDialpadTextViewBlur extends AppCompatTextView {
    private Bitmap mMaskBitmap;

    private Canvas mMaskCanvas;

    private Paint mPaint;

    private Drawable mBackground;

    private Bitmap mBackgroundBitmap;

    private Canvas mBackgroundCanvas;

    private Rect mTextBounds = new Rect();

    private String mTextStr;

    public BtalkDialpadTextViewBlur(final Context context) {
        super(context);
        init();
    }

    public BtalkDialpadTextViewBlur(final Context context, final AttributeSet attrs) {
        super(context, attrs);
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

    /**
     * Draw the text to fit within the height/width which have been specified during measurement.
     */
    public void draw(Canvas canvas, Paint paint) {
        int paddingTop = getContext().getResources().getDimensionPixelOffset(R.dimen.padding_text_number_dialpad);
        int marginIfNeed = 0;
        // The text bounds values are relative and can be negative,, so rather than specifying a
        // standard origin such as 0, 0, we need to use negative of the left/top bounds.
        // For example, the bounds may be: Left: 11, Right: 37, Top: -77, Bottom: 0
        // de can giua cai phim * # su dung chieu cao chuan cua text tru di chieu cao chu chia 2
        int top = (77 - mTextBounds.height()) / 2;
        int width = mTextBounds.width();
        if (mTextStr.equals("1")) {
            width = 35;
        }

        canvas.drawText(mTextStr, (getWidth() - width) / 2 + marginIfNeed,
                paddingTop + top, paint);
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
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        if (isNothingToDraw()) {
            return;
        }
        // ve bitmap voi text mau den
        drawMask();
        // ve transparent phan text, su dung bitmap voi text den ve de len
        drawBackground();
        // ve lai background
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
    }

    private static void clear(Canvas canvas) {
        canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
    }


}
