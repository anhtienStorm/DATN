package bkav.android.btalk.messaging.ui.mediapicker.emoticon;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.style.DynamicDrawableSpan;

import com.android.messaging.Factory;

/**
 * Created by quangnd on 27/12/2017.
 * class custom span emoji
 */

public class EmojiSpan extends DynamicDrawableSpan {
    private Drawable mDeferredDrawable;
    private final int mSize;
    private final int mResId;

    public EmojiSpan(final int resId, int size) {
        this.mSize = size;
        mResId = resId;
    }

    @Override
    public Drawable getDrawable() {
        if (mDeferredDrawable == null) {
            mDeferredDrawable = ContextCompat.getDrawable(Factory.get().getApplicationContext(), mResId);
            mDeferredDrawable.setBounds(0, 0, mSize, mSize);
        }
        return mDeferredDrawable;
    }

//    @Override
//    public void draw(Canvas canvas, CharSequence text, int start, int end
//            , float x, int top, int y, int bottom, Paint paint) {
//        final Drawable drawable = getDrawable();
//        final Paint.FontMetrics paintFontMetrics = paint.getFontMetrics();
//        final float fontHeight = paintFontMetrics.descent - paintFontMetrics.ascent;
//        final float centerY = y + paintFontMetrics.descent - fontHeight / 2;
//        final float transitionY = centerY - mSize / 2;
//
//        canvas.save();
//        canvas.translate(x, transitionY);
//        drawable.draw(canvas);
//        canvas.restore();
//    }
}
