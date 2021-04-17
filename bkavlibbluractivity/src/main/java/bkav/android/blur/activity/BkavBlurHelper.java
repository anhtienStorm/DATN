package bkav.android.blur.activity;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ms_square.etsyblur.BlurringView;

/**
 * Created by trungth on 16/05/2017.
 */

public class BkavBlurHelper {
    private Context mContext;

    // Anhdts
    private boolean mIsConfigBkav = false;

    private boolean mIsNoActionBar = false;

    private int mContentLayout;

    private ImageView mBlurredOverlay;

    private View mContentView;

    private FrameLayout mFrameLayout;

    private BkavBlurConfig mBkavBlurConfig;

    private BlurringView mBlurringView;

    private ImageView mFakeStatusBar;

    //private static BkavBlurHelper sInstance;

    private static final int USE_WALLPAPER_FOR_BG_INDEX = 0;

    private static final int WINDOW_BACKGROUND_INDEX = 1;

    private static final int BLUR_RADIUS_INDEX = 2;

    private static final int DOWN_SCALE_FACTOR_INDEX = 3;

    private static final int ALPHA_ACTION_BAR_INDEX = 4;

    private static final int ALPHA_TOOLBAR_INDEX = 5;

    private static final int ALPHA_ALL_VIEW_INDEX = 6;

    private static final int COLOR_STATUSBAR_INDEX = 7;

    private static final int BLUR_FILTER_COLOR = 8;

    private static final int BLUR_FILTER_DEPTH = 9;

    private int[] mAtrs = new int[]{
            R.attr.useWallpaperForBackground, // 0
            R.attr.windowBackground, // 1
            R.attr.blurRadius, // 2
            R.attr.blurDownScaleFactor, // 3
            R.attr.alphaActionBar, // 4
            R.attr.alphaToolBar, // 5
            R.attr.alphaAllView, // 6
            R.attr.statusBarIsTransparent, //7
            R.attr.blurFilterColor, //8
            R.attr.blurFilterDepth, //9
    };

    public BkavBlurHelper(Activity activity, int layout, boolean configBkav) {
        this.mContentLayout = layout;
        this.mContext = activity;
        mIsConfigBkav = configBkav;
        initBkavBlurConfig(mContext);
    }

    private void initBkavBlurConfig(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.useWallpaperForBackground, outValue, true);
        TypedArray style = context.getTheme().obtainStyledAttributes(outValue.resourceId, mAtrs);
        boolean useWallpaperForBackground = style.getBoolean(USE_WALLPAPER_FOR_BG_INDEX, BkavBlurConfig.DEFAULT_USE_WALLPAPER_FOR_BG);
        Drawable backgroundDrawable = style.getDrawable(WINDOW_BACKGROUND_INDEX);
        int radius = style.getInt(BLUR_RADIUS_INDEX, BkavBlurConfig.DEFAULT_RADIUS);
        int downScaleFactor = style.getInt(DOWN_SCALE_FACTOR_INDEX, BkavBlurConfig.DEFAULT_DOWN_SCALE_FACTOR);
        float alphaActionbar = style.getFloat(ALPHA_ACTION_BAR_INDEX, BkavBlurConfig.DEFAULT_ALPHA_ACTION_BAR);
        float alphaToolbar = style.getFloat(ALPHA_TOOLBAR_INDEX, BkavBlurConfig.DEFAULT_ALPHA_TOOLBAR);
        float alphaALLView = style.getFloat(ALPHA_ALL_VIEW_INDEX, BkavBlurConfig.DEFAULT_ALPHA_ALL_VIEW);
        boolean statusBarIsTransparent = style.getBoolean(COLOR_STATUSBAR_INDEX, BkavBlurConfig.DEFAULT_STATUSBAR_IS_TRANSPARENT);
        int colorFilter = style.getColor(BLUR_FILTER_COLOR, BkavBlurConfig.DEFAULT_FILTER_COLOR);
        int depthFilter = style.getInt(BLUR_FILTER_DEPTH, BkavBlurConfig.DEFAULT_FILTER_DEPTH);
        style.recycle();
        mBkavBlurConfig = (BkavBlurConfig) new BkavBlurConfig.BkavBuilder()
                .useWallpaperForBackground(useWallpaperForBackground)
                .backgroundDrawable(backgroundDrawable)
                .setStatusBarIsTransparent(statusBarIsTransparent)
                .alphaActionbar(alphaActionbar)
                .alphaToolbar(alphaToolbar)
                .alphaALLView(alphaALLView)
                .radius(radius)
                .downScaleFactor(downScaleFactor)
                .colorFilter(colorFilter)
                .depth(depthFilter)
                .build();

    }

    /**
     * @param isStartBlur chay luon blur ngay tu dau
     * @return
     */
    public View createView(boolean isStartBlur) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mFrameLayout = (FrameLayout) inflater.inflate(R.layout.blur_frame, null);
        mContentView = inflater.inflate(mContentLayout, mFrameLayout, false);
        mFrameLayout.addView(mContentView, 3/*Vi tri so 2*/);
        mBlurredOverlay = (ImageView) mFrameLayout.findViewById(R.id.blurredOverlay);
        mBlurringView = (BlurringView) mFrameLayout.findViewById(R.id.blurring_view);
        if (!mIsConfigBkav) {
            mBlurringView.setBlurConfig(mBkavBlurConfig);
            mBlurringView.blurredView(mBlurredOverlay);
            // Xu ly layhinh nen de blur;
            mBlurringView.startBlur(isStartBlur);
        } else {
            mBlurringView.setVisibility(View.GONE);
        }
        startBlurView();
        return mFrameLayout;
    }

    // Anhdts
    public void changeWallpaper(Bitmap background) {
        if (mIsConfigBkav) {
            setBackground(background);
        } else {
            setBackground();
        }
    }

    public void startBlurView() {
        if (mIsConfigBkav) {
            setBackground(WallpaperBlurCompat.getInstance(mContext).getWallpaperBlur());
        } else {
            setBackground();
        }
        // THay doi do alpha cua background phia tren
        if (mBkavBlurConfig.getAlphaALLView() != BkavBlurConfig.DEFAULT_ALPHA_ALL_VIEW) {
            int alpha = (int) (mBkavBlurConfig.getAlphaALLView() == 1.0 ? 255 : mBkavBlurConfig.getAlphaALLView() * 255.0);
//            mContentView.getBackground().setAlpha(alpha);
            if (mContext instanceof Activity) {
                if (mBkavBlurConfig.statusBarIsTransparent()) {
                    setTransparentStatusBar();
                } else {
                    addFlagNoLimitsAndShowFakeView();
                }
            } else {
                throw new IllegalArgumentException("not activity don't change status bar color");
            }
        }
    }

    private void setBackground() {
        Drawable bitmapToBlur;
        if (mBkavBlurConfig.isUseWallpaperForBackground()) {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
            bitmapToBlur = wallpaperManager.getDrawable();
        } else {
            bitmapToBlur = mBkavBlurConfig.getBackgroundDrawable();
        }
        // Overlay StatusBar
        mBlurredOverlay.setBackgroundDrawable(bitmapToBlur);
    }

    private void setBackground(Bitmap bitmap) {
        mBlurredOverlay.setBackground(new BitmapDrawable(mContext.getResources(), bitmap));
    }

    private void setTransparentStatusBar() {
        ImageView fakeStatusBar = (ImageView) mFrameLayout.findViewById(R.id.fake_statusBar);
        fakeStatusBar.setVisibility(View.GONE);
        Window window = ((Activity) mContext).getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(mContext.getResources()
                .getColor(android.R.color.transparent));
    }

    // Anhdts cac thuoc tinh transparent status bar
    public void addFlagNoLimitsAndShowFakeView() {
        // Day giao dien xuong 1 doan status bar
        Window window = ((Activity) mContext).getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        int statusBarHeigt = getStatusBarHeight();
        // Day giao dien xuong 1 doan status bar
         FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
         params.topMargin = statusBarHeigt;
        // Doi mau statubar voi cung do alpha cua view
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        window.getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mFakeStatusBar = mFrameLayout.findViewById(R.id.fake_statusBar);
        mFakeStatusBar.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams fakeParams = (FrameLayout.LayoutParams) mFakeStatusBar.getLayoutParams();
        fakeParams.height = statusBarHeigt;
    }

    // Bkav TrungTh thay doi mau cua status bar theo do alpha
    public static void setStatusBarColor(final Activity activity, final int color, float alpha) {
        final int blendedRed = (int) Math.floor(alpha * Color.red(color));
        final int blendedGreen = (int) Math.floor(alpha * Color.green(color));
        final int blendedBlue = (int) Math.floor(alpha * Color.blue(color));
        activity.getWindow().setStatusBarColor(
                Color.rgb(blendedRed, blendedGreen, blendedBlue));
    }

    public static int getColorWithAlpha(int color, float alpha) {
        final int blendedRed = (int) Math.floor(alpha * Color.red(color));
        final int blendedGreen = (int) Math.floor(alpha * Color.green(color));
        final int blendedBlue = (int) Math.floor(alpha * Color.blue(color));
        return Color.rgb(blendedRed, blendedGreen, blendedBlue);
    }

    public int getStatusBarHeight() {
        // Anhdts
        if (mIsNoActionBar) {
            return 0;
        }
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void setFakeStatusBarColor(int color) {
        if (mFakeStatusBar != null) {
            mFakeStatusBar.setBackgroundColor(color);
        }
    }

    public void startBlur(boolean b) {
        if (!mIsConfigBkav) {
            mBlurringView.startBlur(b);
        }
    }

    public void setIsNoActionBar(boolean isNoActionBar) {
        this.mIsNoActionBar = isNoActionBar;
    }

    public BlurringView getBlurringView() {
        return mBlurringView;
    }
}