package bkav.android.btalk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by anhdt on 11/12/2017.
 */

public class EditTextKeyListener extends EditText {
    public EditTextKeyListener(Context context) {
        super(context);
    }

    public EditTextKeyListener(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextKeyListener(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return mListener.onBackState() || super.onKeyPreIme(keyCode, event);
        }
        return super.onKeyPreIme(keyCode, event);
    }

    private OnBackPressListener mListener;

    public void setOnBackPressListener(OnBackPressListener listener) {
        mListener = listener;
    }

    public interface OnBackPressListener {
        boolean onBackState();
    }

}
