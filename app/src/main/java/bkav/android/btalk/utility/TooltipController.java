package bkav.android.btalk.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;

/**
 * Created by anhdt on 04/05/2017.
 */

public class TooltipController implements View.OnClickListener {

    public static final String PREFERENCE_TOOLTIP = "preference_tooltip";
    private static TooltipController sInstance;
    private static final int MSG_SHOW_TOOL_TIP_MAGIC_PAD = 1;
    private static final int MSG_KEY_PRESS = 2;
    private static final int MSG_REMOVE_TOOLTIP_MESSAGE = 3;
    private static final int MSG_REMOVE_TOOLTIP_CONTACT = 4;
    private static final int TIME_DELAY_REMOVE_TOOLTIP = 5000; // 5s
    public static final int LEARN_DOUBLE_CLICK_MESSAGE_TAB = 3;
    public static final int LEARN_DOUBLE_CLICK_CONTACT_TAB = 3;

    private static final int MSG_REMOVE_TOOLTIP_CONTACT_MAGICPAD = 5;

    private TextView mTvContactMagicpad;
    private boolean mIsInShowContactMagicPad = false;

    private WeakReference<BtalkActivity> mActivityRef;
    private Handler mTooltipHandler;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;

    private View mToolTipView;
    private View mToolTipViewTop;
    private View mTooltipMesssageTab;
    private View mTooltipContactTab;
    private View mTooltipContactTabMagicPad;
    private Button mButtonOk;
    private ImageView mImageViewNotShowAgain;
    private TextView mTextViewNotShowAgain;
    private TextView mTvMessageTop;
    private ImageView mIvArrowDown;
    private TextView mTvTooltipMessage;
    private View mZeroButton, mEightButton, mFiveButton, mTwoButton;
    private ImageView mIvArrowRight;

    private boolean mIsTooltipShowing = false;
    private boolean mIsTooltipTopShowing = false;
    private boolean mIsTooltipMessageShowing = false;
    private boolean mIsTooltipContactShowing = false;
    private boolean mIsTooltipContactMagicPadShowing = false;


    private int mHeightScreen = 0;
    private int mHeightTooltip = 0;
    private int mHeightTooltipTop = 0;
    private int mHeightTooltipLayout = 0;
    private int mHeightDialpad = 0;

    private boolean mChecked;
    private Animation mAnimationBlink;
    private SharedPreferences mPreference;
    private Context mContext;

    public static TooltipController getInstance(BtalkActivity activity) {
        if (sInstance == null) {
            sInstance = new TooltipController(activity);
        }
        return sInstance;
    }

    public TooltipController(BtalkActivity activity) {
        mActivityRef = new WeakReference<>(activity);
        mContext = activity.getApplicationContext();
        mPreference = mContext.getSharedPreferences(PREFERENCE_TOOLTIP, 0);
    }

    public void showMagicPadTooltip() {
        if (mActivityRef.get() != null) {
            int count = mPreference.getInt(mContext.getString(R.string.count_show_tooltip_dialpad), 0);
            if (mParams == null || mHeightDialpad == 0) {
                LayoutInflater layoutInflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mToolTipView = layoutInflater.inflate(R.layout.btalk_popup_tooltip_dialpad, null);

                mButtonOk = (Button) mToolTipView.findViewById(R.id.btnOK);
                mButtonOk.setOnClickListener(this);

                mImageViewNotShowAgain = (ImageView) mToolTipView.findViewById(R.id.ivNotShowAgain);
                mImageViewNotShowAgain.setOnClickListener(this);

                mTextViewNotShowAgain = (TextView) mToolTipView.findViewById(R.id.tvNotShowAgain);
                mTextViewNotShowAgain.setOnClickListener(this);

                // Anhdts tinh toan vi tri hien thi
                DisplayMetrics displayMetrics = new DisplayMetrics();
                mWindowManager = mActivityRef.get().getWindowManager();
                mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
                mHeightScreen = displayMetrics.heightPixels;
                mHeightDialpad = mActivityRef.get().getTabHeight();
                mParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                | WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
                                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSPARENT);
                mParams.gravity = Gravity.TOP | Gravity.LEFT;
                mParams.x = 0;
                mParams.y = mHeightScreen / 2;
            }
            if (count == 0) {
                setChecked(false);
            } else {
                setChecked(true);
            }
            if (count == 0) {
                mImageViewNotShowAgain.setVisibility(View.INVISIBLE);
                mTextViewNotShowAgain.setVisibility(View.INVISIBLE);
            } else {
                mImageViewNotShowAgain.setVisibility(View.VISIBLE);
                mTextViewNotShowAgain.setVisibility(View.VISIBLE);
            }
            count++;
            if (mHeightDialpad == 0) {
                mActivityRef.get().getWindow().getDecorView().getRootView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        showMagicPadTooltip();
                        mActivityRef.get().getWindow().getDecorView().getRootView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            } else {
                mIsTooltipShowing = true;
                mPreference.edit().putInt(mContext.getString(R.string.count_show_tooltip_dialpad), count).apply();
                mWindowManager.addView(mToolTipView, mParams);
            }
        }
    }

    public void showMagicPadTooltipTop() {

    }

    public void showMessageTabTooltip() {

    }

    public void showContactTabTooltip() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOK: {
                if (mChecked) {
                    // Anhdts Cap nhat khong bao gio hien thi len tooltip nua
                    mPreference.edit().putBoolean(mContext.getString(R.string.tooltip_pref_show_dialpad),
                            false).apply();
                } else {
                    // Anhdts Dem so lan da hien thi tooltip
                    // Neu da hien 3 lan thi cap nhat khong bao gio hien thi len tooltip nua
                    int countShow = mPreference.getInt(mContext.getString(R.string.count_show_tooltip_dialpad), 0);
                    if (countShow >= 30) {
                        mPreference.edit().putBoolean(mContext.getString(R.string.tooltip_pref_show_dialpad),
                                false).apply();
                    }
                }

                hideToolTipDialpad();
                break;
            }
            case R.id.ivNotShowAgain: {
                toggle();
                break;
            }
            case R.id.tvNotShowAgain: {
                toggle();
                break;
            }
        }
    }

    public void hideToolTipDialpad() {
        if (mToolTipView != null) {
            mWindowManager.removeView(mToolTipView);
        }

        mIsTooltipShowing = false;
    }

    private void toggle() {
        setChecked(!mChecked);
    }

    private void setChecked(boolean checked) {
        mChecked = checked;
        checkChanged();
    }

    private void checkChanged() {
        Drawable drawable = ContextCompat.getDrawable(mContext,
                mChecked ? R.drawable.btalk_ic_tooltip_checked : R.drawable.btalk_ic_tooltip_unchecked);
        mImageViewNotShowAgain.setImageDrawable(drawable);
    }

    public boolean isTooltipShowing() {
        return mIsTooltipShowing;
    }

    public boolean getShowTooltip() {
        return mPreference.getBoolean(mContext.getString(R.string.tooltip_pref_show_dialpad),
                true);
    }

    public int getTimesUseTooltipMessage() {
        return mPreference.getInt(mContext.getString(R.string.tooltip_times_use_message), 0);
    }

    public int getTimesUseTooltipContact() {
        return mPreference.getInt(mContext.getString(R.string.tooltip_times_use_contact), 0);
    }

    public boolean isTooltipMessageShowing() {
        return mIsTooltipMessageShowing;
    }

    public boolean isTooltipContactShowing() {
        return mIsTooltipContactShowing;
    }

    public boolean isTooltipTopShowing() {
        return mIsTooltipTopShowing;
    }
}
