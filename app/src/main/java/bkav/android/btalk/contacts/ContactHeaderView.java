package bkav.android.btalk.contacts;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;
import android.widget.TextView;

import bkav.android.btalk.R;
import butterknife.ButterKnife;

public class ContactHeaderView extends RelativeLayout {

    public ContactHeaderView(Context context) {
        super(context);
    }

    public ContactHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContactHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ContactHeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }
}
