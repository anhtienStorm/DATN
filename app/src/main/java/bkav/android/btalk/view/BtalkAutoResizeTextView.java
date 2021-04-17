package bkav.android.btalk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * Created by anhdt on 13/01/2018.
 */

public class BtalkAutoResizeTextView extends TextView {

    private final int mOriginalTextSize;
    private final int mMinTextSize;

    private boolean isEnoughSpace = true;

    public BtalkAutoResizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mOriginalTextSize = (int) getTextSize();
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.phone.common.R.styleable.ResizingText);
        mMinTextSize = (int) a.getDimension(com.android.phone.common.R.styleable.ResizingText_resizing_text_min_size,
                mOriginalTextSize);
        a.recycle();
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        resizeText(this, mOriginalTextSize, mMinTextSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resizeText(this, mOriginalTextSize, mMinTextSize);
    }

    public void resizeText(TextView textView, int originalTextSize, int minTextSize) {
        final Paint paint = textView.getPaint();
        final int width = textView.getWidth();
        if (width == 0) return;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize);
        float ratio = width / paint.measureText(textView.getText().toString());
        if (ratio <= 1.0f) {
            isEnoughSpace = originalTextSize * ratio >= minTextSize;
            // Bkav TienNAb: sua lai text size khi text qua dai
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    isEnoughSpace ? originalTextSize * ratio : minTextSize);
        } else {
            isEnoughSpace = true;
        }
        if (mListener != null) {
            if (isEnoughSpace) {
                mListener.hideExpanded();
            } else {
                mListener.showExpanded();
            }
        }
    }

    public boolean getIsEnoughSpace() {
        return isEnoughSpace;
    }

    private ShowExpandedName mListener;

    public void setShowExpandedListener(ShowExpandedName listener) {
        mListener = listener;
    }

    public interface ShowExpandedName {
        void showExpanded();

        void hideExpanded();
    }
}
