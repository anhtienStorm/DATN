package bkav.android.btalk.calllog;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;
import android.widget.TextView;

import bkav.android.btalk.R;
import butterknife.ButterKnife;

public class CallLogHeaderView extends RelativeLayout {

    public CallLogHeaderView(Context context) {
        super(context);
    }

    public CallLogHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CallLogHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CallLogHeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }
}
