package bkav.android.btalk.messaging.ui;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import com.android.messaging.ui.PlainTextEditText;

import bkav.android.btalk.messaging.util.BtalkIconParser;

/**
 * Created by quangnd on 27/12/2017.
 * class custom lai edittext cua compose
 */

public class BtalkComposeEditText extends PlainTextEditText {

    public BtalkComposeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void input(final String emoji) {
        if (emoji != null) {
            final int start = getSelectionStart();
            final int end = getSelectionEnd();
            if (start < 0) {
                append(emoji);
            } else {
                getText().replace(Math.min(start, end), Math.max(start, end), emoji, 0, emoji.length());
            }
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        final Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
        final int defaultEmojiSize = (int) (fontMetrics.descent - fontMetrics.ascent);
        BtalkIconParser.getInstance().replaceWithImages(getText(), defaultEmojiSize);
    }
}
