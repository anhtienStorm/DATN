package bkav.android.btalk.contacts.editcontact;

import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import bkav.android.btalk.R;

/**
 * Anhdts
 */
public class BtalkPhotoPickerView extends FrameLayout implements View.OnClickListener {

    private int mWidthScreen;

    private int mHeightScreen;

    private int mMargin;

    private int mSizeImage;

    private int mMarginHoz;

    public View mOverlayView;

    private int mNumberHor = 4;

    public BtalkPhotoPickerView(Context context) {
        this(context, null);
    }

    public BtalkPhotoPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mWidthScreen = metrics.widthPixels;
        mHeightScreen = metrics.heightPixels;

        mMargin = getResources().getDimensionPixelOffset(R.dimen.very_small_margin);

        int heightView = mHeightScreen - mWidthScreen;

        if (mWidthScreen / 4f * 3 - heightView < -(mMargin * 4)) {
            if (mWidthScreen - heightView > 4 * mMargin) {
                mNumberHor = 4;
                mSizeImage = (int) ((mWidthScreen - mMargin * 4.5) / 3);
                mMarginHoz = mMargin;
                return;
            } else {
               mNumberHor = 3;
            }
        }

        mSizeImage = (int) ((heightView - mMargin * 4.5) / 3);
        mMarginHoz = (mWidthScreen - mSizeImage * mNumberHor) / (mNumberHor + 1);
    }

    public BtalkPhotoPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        LayoutParams paramsRootView = (LayoutParams) getLayoutParams();
//        paramsRootView.height = 4 * mSizeImage + 6 * mMargin;
        new AsyncTaskPhotoLoader(mSizeImage, this).execute();

    }

    private boolean mIsDisableTouch = false;

    @Override
    public void onClick(final View v) {
        if (v.getId() == R.id.overlay_photo) {
            mListener.removePhoto();
            removeView(mOverlayView);
            return;
        }
        if (mIsDisableTouch) {
            return;
        }
        mIsDisableTouch = true;
        String imageDefault = String.valueOf(v.getContentDescription());
        int resId = getResources().getIdentifier(imageDefault, "drawable",
                "bkav.android.btalk");
        mListener.pickerPhoto(getUriToDrawable(getContext(), resId));

        if (mOverlayView != null) {
            removeView(mOverlayView);
        } else {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            mOverlayView = inflater.inflate(R.layout.btalk_view_overlay, null);
            mOverlayView.setOnClickListener(this);
            mOverlayView.setFocusable(true);
            mOverlayView.setClickable(true);
        }

        ValueAnimator animator = ValueAnimator.ofFloat(5);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                if (value < 2.5f) {
                    v.setScaleX(1 - value / 6.25f);
                    v.setScaleY(1 - value / 6.25f);
                } else {
                    v.setScaleX(value / 6.25f + 0.2f);
                    v.setScaleY(value / 6.25f + 0.2f);
                }
                if (value == 5) {
                    mOverlayView.setLayoutParams(v.getLayoutParams());
                    addView(mOverlayView);
                    mIsDisableTouch = false;
                }
            }
        });
        animator.start();
    }

    private static class AsyncTaskPhotoLoader extends AsyncTask<String, Bitmap, String> {

        private final int mSizeImage;

        private final WeakReference<BtalkPhotoPickerView> mPickerView;

        AsyncTaskPhotoLoader(int sizeImage, BtalkPhotoPickerView context) {
            mSizeImage = sizeImage;
            mPickerView = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            for (int i = 1; i <= 15; i++) {
                Drawable drawable = resizeBitmap(mPickerView.get().getResources().getIdentifier("thumbnail_" + i, "drawable",
                        "bkav.android.btalk"), mSizeImage);
                mPickerView.get().addImage(drawable, i);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            super.onProgressUpdate(values);
        }

        Drawable resizeBitmap(int resId, int targetSize) {
            Drawable drawable = ContextCompat.getDrawable(mPickerView.get().getContext(), resId);
            Bitmap b = ((BitmapDrawable) drawable).getBitmap();
            Bitmap bitmapResized =
                    Bitmap.createScaledBitmap(b, targetSize, targetSize, false);
            return new BitmapDrawable(mPickerView.get().getResources(), bitmapResized);
        }
    }

    private void addImage(final Drawable drawable, final int i) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = new ImageView(getContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                imageView.setImageDrawable(drawable);
                addView(imageView);

                LayoutParams params = (LayoutParams) imageView.getLayoutParams();
                params.leftMargin = mMarginHoz + (mMarginHoz + mSizeImage) * ((i - 1) % mNumberHor);
                params.topMargin = (int) (1.5 * mMargin + (mMargin + mSizeImage) * ((i - 1) / mNumberHor));
                params.height = mSizeImage;
                params.width = mSizeImage;

                imageView.setLayoutParams(params);
                imageView.setContentDescription(String.valueOf("default_" + i));
                imageView.setOnClickListener(BtalkPhotoPickerView.this);
            }
        });
    }

    public static Uri getUriToDrawable(@NonNull Context context,
                                       @AnyRes int drawableId) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(drawableId)
                + '/' + context.getResources().getResourceTypeName(drawableId)
                + '/' + context.getResources().getResourceEntryName(drawableId));
    }

    private PhotoPickerListener mListener;

    public void setPickerListener(PhotoPickerListener listener) {
        mListener = listener;
    }

    public interface PhotoPickerListener {
        void pickerPhoto(Uri uri);

        void removePhoto();
    }
}