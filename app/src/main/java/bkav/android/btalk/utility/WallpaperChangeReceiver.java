package bkav.android.btalk.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import bkav.android.blur.activity.WallpaperBlurCompat;

/**
 * Created by anhdt on 16/06/2017.
 * lop lang nghe su kien doi hinh nen
 */
public class WallpaperChangeReceiver extends BroadcastReceiver {

    public static final String INTENT_BLUR_WALLPAPER_COMPLETE = "bkav.android.wallpaper.intent.action.WALLPAPER_BLUR_COMPLETE";

    public WallpaperChangeReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (intent.getAction().equals(INTENT_BLUR_WALLPAPER_COMPLETE)) {
                WallpaperBlurCompat.getInstance(context).notifyChangeWallpaper();
            }
        }
    }

}
