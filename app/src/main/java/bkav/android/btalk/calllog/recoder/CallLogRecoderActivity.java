package bkav.android.btalk.calllog.recoder;

import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.dialer.calllog.CallLogFragment;

import bkav.android.blur.activity.BkavBlurHelper;
import bkav.android.blur.activity.WallpaperBlurCompat;
import bkav.android.btalk.R;
import bkav.android.btalk.utility.BtalkUiUtils;

/**
 * Created by HuyNQn 4.7.2019
 */

public class CallLogRecoderActivity extends AppCompatActivity implements WallpaperBlurCompat.ChangeWallPaperListener, CallLogFragment.HostInterface{
    private BkavBlurHelper mBkavBlurHelper; // Bkav HuyNQN lop ho tro blur
    private View mRootView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_call_log_recoder);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); // Bkav HuyNQN tat mo ban phim khi mo CallLogRecoder
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // Bkav HuyNQN set lai FLAG_TRANSLUCENT_STATUS cho statusbar
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//            // Bkav HuyNQN thay doi mau chu statusbar
//            Window window = getWindow();
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        }

        // Bkav HuyNQN dieu chinh lai view cua activity_call_log_recoder
        WallpaperBlurCompat blurCompat = WallpaperBlurCompat.getInstance(getApplicationContext());
        mBkavBlurHelper = new BkavBlurHelper(this, R.layout.activity_call_log_recoder,
                blurCompat.isConfigBkav());
        mRootView = mBkavBlurHelper.createView(false);
        // Bkav HuyNQN Lang nghe thay doi hinh nen
        blurCompat.addOnChangeWallpaperListener(this);
        if (WallpaperBlurCompat.getInstance(getApplicationContext()).isConfigBkav()) {
            mRootView.setBackground(
                    new BitmapDrawable(getResources(), blurCompat.getWallpaperBlur()));
        }
        mBkavBlurHelper.setIsNoActionBar(true); // Bkav HuyNQN loai bo statusbar
        mBkavBlurHelper.addFlagNoLimitsAndShowFakeView();
        mBkavBlurHelper.setFakeStatusBarColor(
                ContextCompat.getColor(CallLogRecoderActivity.this, R.color.btalk_white_opacity_bg));
        BtalkUiUtils.setSystemUiVisibility(mRootView, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        mRootView = findViewById(R.id.root_view);
        if(isInMultiWindowMode()){
            BtalkCallLogRecoderFragment fragment = (BtalkCallLogRecoderFragment) getFragmentManager().findFragmentById(R.id.fragment_call_log_recoder);
            if(fragment.isBoundService()){
                fragment.setupService();
            }
        }// Bkav HuyNQN bugfix 3412 end
        setContentView(mRootView);
    }

    @Override
    public void onChangeWallpaper() {
        mBkavBlurHelper.changeWallpaper(
                WallpaperBlurCompat.getInstance(getApplicationContext()).getWallpaperBlur());
    }

    // Bkav HuyNQN khong thuc hien goi toi DialtactsActivity nua
    @Override
    public void showDialpad() {
//        finish();
//        startActivity(new Intent(CallLogRecoderActivity.this, DialtactsActivity.class));
    }

    /**
     * Bkav HuyNQn doi mau status bar khi bat che do action mode trong ghi am cuoc goi
     */
    public void setStatusbarOnActionModeMessage() {
        BtalkUiUtils.resetSystemUiVisibility(mRootView);
        if (mBkavBlurHelper != null) {
            mBkavBlurHelper.setFakeStatusBarColor(
                    ContextCompat.getColor(CallLogRecoderActivity.this, R.color.action_mode_color));
        }
    }

    /**
     * Bkav HuyNQn doi mau status bar khi bat che do action mode trong ghi am cuoc goi
     */
    public void exitActionModeMessage() {
        if (mBkavBlurHelper != null) {
            mBkavBlurHelper.setFakeStatusBarColor(
                    ContextCompat.getColor(CallLogRecoderActivity.this, R.color.btalk_white_opacity_bg));
        }
        BtalkUiUtils.setSystemUiVisibility(mRootView, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}
