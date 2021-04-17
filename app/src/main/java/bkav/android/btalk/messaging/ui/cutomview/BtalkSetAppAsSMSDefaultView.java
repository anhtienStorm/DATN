package bkav.android.btalk.messaging.ui.cutomview;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.UiUtils;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 28/03/2017.
 * Bkav QuangNDb: class custom view set as sms default app cho btalk
 */

public class BtalkSetAppAsSMSDefaultView extends RelativeLayout
        implements View.OnClickListener {

    private AppCompatButton mChangeButton;

    private Fragment mFragment;

    private Context mContext;

    private static final int REQUEST_SET_DEFAULT_SMS_APP = 1;


    public BtalkSetAppAsSMSDefaultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext =  context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.btalk_custom_set_defaut_sms_view, this, true);
        mChangeButton = (AppCompatButton) view.findViewById(R.id.btn_change);
        mChangeButton.setOnClickListener(this);
    }

    public void setFragment(Fragment fragment) {
        mFragment = fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_change:
                if (mContext instanceof Activity) {
                    ChangeSmsAppSettingRunnable changeSmsAppSettingRunnable
                            = new ChangeSmsAppSettingRunnable((Activity)mContext, mFragment);
                    changeSmsAppSettingRunnable.run();
                }

                break;
        }
    }
    public static class ChangeSmsAppSettingRunnable implements Runnable {
        private final Activity mActivity;
        private final Fragment mFragment;

        public ChangeSmsAppSettingRunnable(final Activity activity, final Fragment fragment) {
            mActivity = activity;
            mFragment = fragment;
        }

        @Override
        public void run() {
            try {
                final Intent intent = UIIntents.get().getChangeDefaultSmsAppIntent(mActivity);
                if (mFragment != null) {
                    mFragment.startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP);
                } else {
                    mActivity.startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP);
                }
            } catch (final ActivityNotFoundException ex) {
                // We shouldn't get here, but the monkey on JB MR0 can trigger it.
                LogUtil.w(LogUtil.BUGLE_TAG, "Couldn't find activity:", ex);
                UiUtils.showToastAtBottom(com.android.messaging.R.string.activity_not_found_message);
            }
        }
    }
}
