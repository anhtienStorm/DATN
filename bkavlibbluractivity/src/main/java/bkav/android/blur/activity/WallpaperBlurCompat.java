package bkav.android.blur.activity;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Anhdts on 07/06/2017.
 * lop check xem co phai bphone 2 khong
 * va dung de get wallpaper da duoc blur
 */

public class WallpaperBlurCompat {

    private WallpaperManager mWallpaperManager;

    private Bitmap mWallpaperBlur;

    private ArrayList<ChangeWallPaperListener> mListeners;

    // Anhdts co phai cau wallpaper da duoc custom khong
    private boolean mIsConfigCustomWallpaperBkav = false;

    private static WallpaperBlurCompat sInstance;

    public static WallpaperBlurCompat getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new WallpaperBlurCompat(context);
        }
        return sInstance;
    }

    private WallpaperBlurCompat(Context context) {
        mWallpaperManager = WallpaperManager.getInstance(context);
        mIsConfigCustomWallpaperBkav = tryGetBlur();
        mListeners = new ArrayList<>();
    }

    /**
     * Anhdts ham nay invoke dong de lay ra Bitmap da blur san trong bphone
     */
    private boolean tryGetBlur() {
        mWallpaperBlur = null;

        try {
            Method method = mWallpaperManager.getClass().getMethod("getBitmapWallpaperBlur");
            method.setAccessible(true);
            mWallpaperBlur = (Bitmap) method.invoke(mWallpaperManager);
            return mWallpaperBlur != null; // TrungTh truong hop wallpaper ma null thi ra return false
        } catch (NoSuchMethodException e) {
            return false;
        } catch (InvocationTargetException e) {
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Bitmap getWallpaperBlur() {
        return mWallpaperBlur;
    }

    public void notifyChangeWallpaper() {
        tryGetBlur();
        for (ChangeWallPaperListener listener : mListeners) {
            listener.onChangeWallpaper();
        }
    }

    public boolean isConfigBkav() {
        return mIsConfigCustomWallpaperBkav;
    }

    public void addOnChangeWallpaperListener(ChangeWallPaperListener listener) {
        mListeners.add(listener);
    }

    public void removeOnChangeWallpaperListener(ChangeWallPaperListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    public interface ChangeWallPaperListener {
        void onChangeWallpaper();
    }
}
