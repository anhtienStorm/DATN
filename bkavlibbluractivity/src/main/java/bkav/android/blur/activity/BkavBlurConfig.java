package bkav.android.blur.activity;

import android.graphics.drawable.Drawable;

import com.ms_square.etsyblur.BlurConfig;

/**
 * Created by trungth on 16/05/2017.
 */

public class BkavBlurConfig extends BlurConfig {

    public static final boolean DEFAULT_USE_WALLPAPER_FOR_BG = false;

    public static final float DEFAULT_ALPHA_ACTION_BAR = 1.0f;

    public static final float DEFAULT_ALPHA_TOOLBAR = 1.0f;

    public static final float DEFAULT_ALPHA_ALL_VIEW = 1.0f;

    public static final boolean DEFAULT_STATUSBAR_IS_TRANSPARENT = false;

    private boolean mUseWallpaperForBackground;

    private Drawable mBackgroundDrawable;

    private float mAlphaActionbar;

    private float mAlphaToolbar;

    private float mAlphaALLView;

    private boolean statusBarIsTransparent;

    private BkavBlurConfig(boolean useWallpaper, Drawable windowBackground, int radius, int downScaleFactor, float alphaActionBar, float alphaToolBar, float alphaaAllView, boolean statusBarIsTransparent, int colorFilter, int depthFilter) {
        super(radius, downScaleFactor, DEFAULT_OVERLAY_COLOR, DEFAULT_ALLOW_FALLBACK, DEFAULT_ASYNC_POLICY, DEFAULT_DEBUG, colorFilter, depthFilter);
        this.mUseWallpaperForBackground = useWallpaper;
        this.mBackgroundDrawable = windowBackground;
        this.mAlphaActionbar = alphaActionBar;
        this.mAlphaToolbar = alphaToolBar;
        this.mAlphaALLView = alphaaAllView;
        this.statusBarIsTransparent = statusBarIsTransparent;
    }

    public boolean statusBarIsTransparent() {
        return statusBarIsTransparent;
    }

    public boolean isUseWallpaperForBackground() {
        return mUseWallpaperForBackground;
    }

    public Drawable getBackgroundDrawable() {
        return mBackgroundDrawable;
    }

    public float getAlphaActionbar() {
        return mAlphaActionbar;
    }

    public float getAlphaToolbar() {
        return mAlphaToolbar;
    }

    public float getAlphaALLView() {
        return mAlphaALLView;
    }


    public static void checkAlpha(float alpha) {
        if (alpha < 0.f || alpha > 1.0f) {
            throw new IllegalArgumentException("alpha must be greater than or equal 0.f and less than or equal to 1.0f");
        }
    }


    public static class BkavBuilder extends Builder  {

        private boolean useWallpaperForBackground;

        private Drawable backgroundDrawable;

        private float alphaActionbar;

        private float alphaToolbar;

        private float alphaALLView;

        private boolean statusBarIsTransparent;

        public BkavBuilder() {
            super();
            useWallpaperForBackground = DEFAULT_USE_WALLPAPER_FOR_BG;
            alphaActionbar = DEFAULT_ALPHA_ACTION_BAR;
            alphaToolbar = DEFAULT_ALPHA_TOOLBAR;
            alphaALLView = DEFAULT_ALPHA_ALL_VIEW;
            statusBarIsTransparent = DEFAULT_STATUSBAR_IS_TRANSPARENT;
        }

        public BkavBuilder setStatusBarIsTransparent(boolean statusBarIsTransparent) {
            this.statusBarIsTransparent = statusBarIsTransparent;
            return this;
        }

        public BkavBuilder useWallpaperForBackground(boolean useWallpaperForBackground) {
            this.useWallpaperForBackground = useWallpaperForBackground;
            return this;
        }

        public BkavBuilder backgroundDrawable(Drawable backgroundDrawable) {
            this.backgroundDrawable = backgroundDrawable;
            return this;
        }

        public BkavBuilder alphaActionbar(float alphaActionbar) {
            checkAlpha(alphaActionbar);
            this.alphaActionbar = alphaActionbar;
            return this;
        }

        public BkavBuilder alphaToolbar(float alphaToolbar) {
            checkAlpha(alphaToolbar);
            this.alphaToolbar = alphaToolbar;
            return this;
        }

        public BkavBuilder alphaALLView(float alphaALLView) {
            checkAlpha(alphaALLView);
            this.alphaALLView = alphaALLView;
            return this;
        }

        public BkavBlurConfig build() {
            return new BkavBlurConfig(useWallpaperForBackground, backgroundDrawable, radius, downScaleFactor, alphaActionbar,
                    alphaToolbar, alphaALLView, statusBarIsTransparent, mColorFilter, mDepth);
        }
    }
}
