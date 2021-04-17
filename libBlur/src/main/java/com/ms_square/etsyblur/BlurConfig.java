package com.ms_square.etsyblur;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.zomato.photofilters.geometry.Point;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ToneCurveSubfilter;

/**
 * BlurConfig.java
 *
 * @author Manabu-GT on 3/17/17.
 */
public class BlurConfig {

    public static final int DEFAULT_RADIUS = 25;

    public static final int DEFAULT_DOWN_SCALE_FACTOR = 10;

    public static final int DEFAULT_OVERLAY_COLOR = Color.TRANSPARENT;

    public static final boolean DEFAULT_ALLOW_FALLBACK = true;

    public static final AsyncPolicy DEFAULT_ASYNC_POLICY = new SimpleAsyncPolicy();

    public static final boolean DEFAULT_DEBUG = false;

    // Bkav TrungTH bo xung truong f

    public static final int DEFAULT_FILTER_COLOR = Color.TRANSPARENT;

    public static final int DEFAULT_FILTER_DEPTH = 100; // 0 - > 255

    public static final BlurConfig DEFAULT_CONFIG = new BlurConfig(DEFAULT_RADIUS,
            DEFAULT_DOWN_SCALE_FACTOR, DEFAULT_OVERLAY_COLOR, DEFAULT_ALLOW_FALLBACK,
            DEFAULT_ASYNC_POLICY, DEFAULT_DEBUG, DEFAULT_FILTER_COLOR, DEFAULT_FILTER_DEPTH);

    private final int radius;

    private final int downScaleFactor;

    @ColorInt
    private final int overlayColor;

    private final boolean allowFallback;

    private final AsyncPolicy asyncPolicy;

    private final boolean debug;

    protected BlurConfig(int radius, int downScaleFactor, @ColorInt int overlayColor,
                         boolean allowFallback, @NonNull AsyncPolicy asyncPolicy, boolean debug, int colorFilter, int depth) {
        this.radius = radius;
        this.downScaleFactor = downScaleFactor;
        this.overlayColor = overlayColor;
        this.allowFallback = allowFallback;
        this.asyncPolicy = asyncPolicy;
        this.debug = debug;
        // Bkav Them vao
        this.mDepth = depth;
        this.mColorFilter = colorFilter;

    }

    public int radius() {
        return radius;
    }

    public int downScaleFactor() {
        return downScaleFactor;
    }

    public int overlayColor() {
        return overlayColor;
    }

    public boolean allowFallback() {
        return allowFallback;
    }

    public AsyncPolicy asyncPolicy() {
        return asyncPolicy;
    }

    public boolean debug() {
        return debug;
    }

    public static void checkRadius(int radius) {
        if (radius <= 0 || radius > 25) {
            throw new IllegalArgumentException("radius must be greater than 0 and less than or equal to 25");
        }
    }

    public static void checkDownScaleFactor(int downScaleFactor) {
        if (downScaleFactor <= 0) {
            throw new IllegalArgumentException("downScaleFactor must be greater than 0.");
        }
    }

    public static class Builder {

        protected int radius;

        protected int downScaleFactor;

        @ColorInt
        protected int overlayColor;

        protected boolean allowFallback;

        protected AsyncPolicy asyncPolicy;

        protected boolean debug;

        // Bkav TrungTh them 2 truong moi
        protected int mColorFilter;

        protected int mDepth;

        public Builder() {
            radius = DEFAULT_RADIUS;
            downScaleFactor = DEFAULT_DOWN_SCALE_FACTOR;
            overlayColor = DEFAULT_OVERLAY_COLOR;
            allowFallback = DEFAULT_ALLOW_FALLBACK;
            asyncPolicy = DEFAULT_ASYNC_POLICY;
            debug = DEFAULT_DEBUG;
            // Bkav TrungTH
            mColorFilter = DEFAULT_FILTER_COLOR;
            mDepth = DEFAULT_FILTER_DEPTH;
        }

        public Builder colorFilter(int colorFilter) {
            this.mColorFilter = colorFilter;
            return this;
        }

        public Builder depth(int depth) {
            checkDepth(depth);
            this.mDepth = depth;
            return this;
        }

        public Builder radius(int radius) {
            checkRadius(radius);
            this.radius = radius;
            return this;
        }

        public Builder downScaleFactor(int downScaleFactor) {
            checkDownScaleFactor(downScaleFactor);
            this.downScaleFactor = downScaleFactor;
            return this;
        }

        public Builder overlayColor(int overlayColor) {
            this.overlayColor = overlayColor;
            return this;
        }

        public Builder allowFallback(boolean allowFallback) {
            this.allowFallback = allowFallback;
            return this;
        }

        public Builder asyncPolicy(@NonNull AsyncPolicy asyncPolicy) {
            this.asyncPolicy = asyncPolicy;
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public BlurConfig build() {
            return new BlurConfig(radius, downScaleFactor, overlayColor,
                    allowFallback, asyncPolicy, debug, mColorFilter, mDepth);
        }
    }

    //======================= Bkav TrungTH ============================//

    protected int mColorFilter;

    protected int mDepth;

    private Filter mFilter;


    public static void checkDepth(int radius) {
        if (radius < 0 || radius > 255) {
            throw new IllegalArgumentException("mDepth must be greater than 0 and less than or equal to 25");
        }
    }

    public Filter getFilter() {
        if (mFilter == null && mColorFilter != DEFAULT_FILTER_COLOR) {
            mFilter = setFilter(mColorFilter, mDepth);
        }
        return mFilter;
    }

    /**
     * Initialize Color Overlay Subfilter
     *
     * @param color
     * @param depth Value ranging from 0-255 {Defining intensity of color overlay}
     */
    public Filter setFilter(int color, int depth) {
        float red = Color.red(color) / 255.0f;
        float green = Color.green(color) / 255.0f;
        float blue = Color.blue(color) / 255.0f;
        return setFilter(depth, red, green, blue);
    }

    /**
     * Initialize Color Overlay Subfilter
     *
     * @param depth Value ranging from 0-255 {Defining intensity of color overlay}
     * @param red   Red value between 0-1
     * @param green Green value between 0-1
     * @param blue  Blue value between 0-1
     */
    public Filter setFilter(int depth, float red, float green, float blue) {
        Filter filter = new Filter();
        Point[] blueKnots;
        blueKnots = new Point[2];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(255, 128);
        // Check whether output is null or not.
        filter.addSubFilter(new ToneCurveSubfilter(blueKnots, null, null, null));
        filter.addSubFilter(new BrightnessSubfilter(60));
        filter.addSubFilter(new ContrastSubfilter(2f));
        return filter;
    }

    public boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        if (darkness < 0.5) {
            return false; // It's a light color
        } else {
            return true; // It's a dark color
        }
    }

}
