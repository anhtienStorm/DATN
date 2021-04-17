package bkav.android.btalk.suggestmagic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import bkav.android.btalk.R;

public class ImageCallButton extends android.support.v7.widget.AppCompatImageView {

    private boolean mIsShowSim = false;

    private Paint mPaint;

    private Bitmap mIconSim;

    private boolean mIsPopup = false;

    private boolean mIsQuickContact = false;

    private int mRadius, mPadding;

    public ImageCallButton(Context context) {
        super(context, null);
    }

    public ImageCallButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mRadius = getResources().getDimensionPixelOffset(R.dimen.icon_sim_radius);
        mPadding = getResources().getDimensionPixelOffset(R.dimen.icon_sim_padding);
    }

    public ImageCallButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setShowSim(boolean hasDefaultSim, Bitmap iconSim) {
        mIsShowSim = hasDefaultSim;
        mIconSim = iconSim;
        if (isShown()) {
            invalidate();
        }
    }


    @Override
    public void setBackgroundResource(int resId) {
        super.setBackgroundResource(resId);
    }

    public void setIsPopup(boolean isPopup) {
        mIsPopup = isPopup;
    }

    public void setIsQuickContact() {
        mIsQuickContact = true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIsShowSim) {
            // TrungTH sua lai 1 chut ,
            // pading lay tu dimen ra va de cung gia tri khong chia nua
            // posTop = padtiong top
            // Phan pasStart = bat dau o vi tri 3/4 cua icon phone (lay tai center imageview + them 1/4 image resource do image resource luon o giua)
            int radius = mRadius;
            int padding = mPadding;
            int posTop = getPaddingTop()/*mIsPopup ? getPaddingTop() - padding : (mIsQuickContact ? getPaddingTop() - padding : getPaddingTop() + 1)*/;
            int centerPos = getMeasuredHeight() / 2;
            int imageSourceWidth = getImageSourceWidth();
            // Truong hop nao ko lay duoc bound thi cu ve o vi tri 3/4 imageview
            int posStart = (int) (centerPos + (imageSourceWidth == 0 ? 0.5f * centerPos : 0.25f * imageSourceWidth));
            drawRoundRect(posStart, posTop,
                    posStart + mIconSim.getWidth() + 2 * padding, posTop + mIconSim.getHeight() + 2 * padding, mPaint, radius, canvas);

            canvas.drawBitmap(mIconSim, posStart + padding, posTop + padding, mPaint);
        }
    }

    private void drawRoundRect(float left, float top, float right, float bottom, Paint
            paint, int radius, Canvas canvas) {
        Path path = new Path();
        path.moveTo(left, top + 2 * radius);
        path.lineTo(left + 2 * radius, top);
        path.lineTo(right - radius, top);
        path.quadTo(right, top, right, top + radius);
        path.lineTo(right, bottom - radius);
        path.quadTo(right, bottom, right - radius, bottom);
        path.lineTo(left + radius, bottom);
        path.quadTo(left, bottom, left, bottom - radius);
        canvas.drawPath(path, paint);
    }

    public boolean isShowingSim() {
        return mIsShowSim;
    }

    /**
     * // drawable dua vao to nhung kich thuoc thuc cua image bi scale lai nho hon nen can get lai bound
     *
     * @return
     */
    private int getImageSourceWidth() {
        RectF bounds = new RectF();
        Drawable drawable = getDrawable();
        Rect imageBount = drawable.getBounds();
        if (drawable != null && imageBount != null) {
            getImageMatrix().mapRect(bounds, new RectF(imageBount));
        }
        return (int) bounds.width();
    }
}
