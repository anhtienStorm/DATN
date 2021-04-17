package bkav.android.btalk.calllog.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.QuickContactBadge;

/**
 * Created by anhdt on 10/06/2017.
 */

public class BtalkQuickContactBadge extends QuickContactBadge implements View.OnClickListener {


    public BtalkQuickContactBadge(Context context) {
        this(context, null);
    }

    public BtalkQuickContactBadge(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BtalkQuickContactBadge(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BtalkQuickContactBadge(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    @Override
    public void onClick(View v) {
        listener.onClickQuickContact();
    }

    OnClickQuickContactBadgeListener listener;

    public void setQuickContactListener(OnClickQuickContactBadgeListener listener){
        this.listener = listener;
    }

    interface OnClickQuickContactBadgeListener {
        void onClickQuickContact();
    }
}