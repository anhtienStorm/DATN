package bkav.android.btalk.fragments.dialpad;

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
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.android.phone.common.dialpad.DialpadKeyButton;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.util.BtalkTypefaces;

/**
 * Created by anhdt on 05/12/2017.
 *
 */

public class BtalkDialpadKeyButton extends DialpadKeyButton {

    private Bitmap mMaskBitmap;

    private Canvas mMaskCanvas;

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

    private final Paint mPaint;

    private final Paint mPaintTransparent;

    private final int mTextSize;

    final int[] letterIds = new int[]{
            R.string.dialpad_0_letters,
            R.string.dialpad_1_letters,
            R.string.dialpad_2_letters,
            R.string.dialpad_3_letters,
            R.string.dialpad_4_letters,
            R.string.dialpad_5_letters,
            R.string.dialpad_6_letters,
            R.string.dialpad_7_letters,
            R.string.dialpad_8_letters,
            R.string.dialpad_9_letters,
            R.string.btalk_dialpad_star_letters,
            R.string.btalk_dialpad_pound_letters
    };

    private final int[] mButtonIds = new int[]{R.id.zero_under, R.id.one_under, R.id.two_under, R.id.three_under,
            R.id.four_under, R.id.five_under, R.id.six_under, R.id.seven_under, R.id.eight_under, R.id.nine_under, R.id.star_under,
            R.id.pound_under};

    private final int[] mButtonIdsNor = new int[]{R.id.zero, R.id.one, R.id.two, R.id.three, R.id.four,
            R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine, R.id.star,
            R.id.pound};

    public BtalkDialpadKeyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TextBlurCustom, 0, 0);
        try {
            mPaddingLeft = ta.getDimensionPixelOffset(R.styleable.TextBlurCustom_blur_padding_left, 0); // Mac dinh de 0
            mPaddingBottom = ta.getDimensionPixelOffset(R.styleable.TextBlurCustom_blur_padding_bottom, 0); // Mac dinh de 0
            mPaddingTop = ta.getDimensionPixelOffset(R.styleable.TextBlurCustom_blur_padding_top, 0);
            mAutoResize = ta.getBoolean(R.styleable.TextBlurCustom_blur_auto_resize, false);
            mIsCenter = ta.getBoolean(R.styleable.TextBlurCustom_blur_center, false);
            mTextSize = ta.getDimensionPixelSize(R.styleable.TextBlurCustom_blur_text_size,
                    getResources().getDimensionPixelSize(R.dimen.btalk_dialpad_key_letters_size));
        } finally {
            ta.recycle();
        }
        mPaint = new Paint();
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        mPaintTransparent = new Paint();
        mPaintTransparent.setColor(Color.BLACK);
        mPaintTransparent.setTypeface(BtalkTypefaces.sRobotoRegularFont);
        mPaintTransparent.setTextSize(mTextSize);
        mPaintTransparent.setAntiAlias(true);

        super.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    @Deprecated
    public void setBackgroundDrawable(final Drawable bg) {
        if (mBackground == bg) {
            return;
        }

//        mBackground = new ColorDrawable(ContextCompat.getColor(getContext(), R.color.btalk_color_for_dialpad));

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
            mPaintTransparent.setTextSize(sizeTextMain);
            Rect rect = new Rect();
            mPaintTransparent.getTextBounds("0", 0, 1, rect);
            mPaintTransparent.setTextSize(sizeTextBlur);

            int heightBottomMain = (getHeight() - rect.height()) / 2;
            int heightBottomBlur = heightBottomMain - mPaddingBottom - mTextBounds.height();

            if (heightBottomBlur < 20) {
                int range = MAGIC_DISTANCE - heightBottomBlur;
                mPaddingBottom = mPaddingBottom - range > 0 ? mPaddingBottom - range : 0;
            } else if (heightBottomBlur > 40) {
                int range = heightBottomBlur - MAGIC_DISTANCE;
                mPaddingBottom = mPaddingBottom + range;
            }
        }
        int marginIfNeed = mPaddingLeft;
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
        // The content description is used for Talkback key presses. The number is
        // separated by a "," to introduce a slight delay. Convert letters into a verbatim
        // span so that they are read as letters instead of as one word.
        int i = 0;
        for (int id : mButtonIds) {
            if (id == getId()) {
                break;
            }
            i++;
        }
        if (i >= mButtonIds.length) {
            i = 0;
            for (int id : mButtonIdsNor) {
                if (id == getId()) {
                    break;
                }
                i++;
            }
        }
        mTextStr = getResources().getString(letterIds[i]);
        mPaintTransparent.getTextBounds(mTextStr, 0, mTextStr.length(), mTextBounds);
    }

    @Override
    public void setBackgroundColor(final int color) {
        setBackground(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.btalk_color_for_dialpad)));
    }

    @Override
    public void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
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
        mIsDrawComplete = false;
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
        draw(mMaskCanvas, mPaintTransparent);
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

    /** **********************************Anhdts ***********************************/
    /**
     * Xu ly su kien cham nut, neu giu tren 200ms thi cho la click nut
     */
    private static final long MAGIC_DISTANCE_DELTA = 200;

    /**
     * Doi voi nut * va # co su kien long click rieng roi, nen neu giu qua 300ms
     * se nhan la an long click va k nhan click nua
     */
    private static final long TIME_LONG_CLICK = 300;

    private float mPosY;

    private float mPosX;

    private long mTimeClick = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (mOnPressedListener != null && Math.abs(ev.getY() - mPosY) < MAGIC_DISTANCE_DELTA
                    && Math.abs(ev.getX() - mPosX) < MAGIC_DISTANCE_DELTA) {
                if (!getContentDescription().equals("*") && !getContentDescription().equals("#")) {
                    mOnPressedListener.onPressed(this, true, true);
                } else if ((System.currentTimeMillis() - mTimeClick) < TIME_LONG_CLICK) {
                    mOnPressedListener.onPressed(this, true, true);
                }
            }
        } else if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mTimeClick = System.currentTimeMillis();
            mPosY = ev.getY();
            mPosX = ev.getX();
        }
        return super.dispatchTouchEvent(ev);
    }
}
