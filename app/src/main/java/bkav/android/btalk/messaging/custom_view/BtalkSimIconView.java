package bkav.android.btalk.messaging.custom_view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.android.messaging.util.AvatarUriUtil;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 27/09/2017.
 */

public class BtalkSimIconView extends ImageView {
    protected int mShapeBig;
    private Context mContext;

    public String getIdSim() {
        return mIdSim;
    }

    public void setIdSim(String mIdSim) {
        this.mIdSim = mIdSim;
    }

    private String mIdSim; // Bkav HuyNQN them bien lay id cua sim
    public BtalkSimIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mShapeBig = context.getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size);
    }

    public void setImageResourceUri(Uri uri, int withAndHeight) {
        if (uri == null) {
            return;
        }
        final String identifier = AvatarUriUtil.getIdentifier(uri);
        final int simColor = AvatarUriUtil.getSimColor(uri);
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final int textColor = Color.WHITE;
        final int minOfWidthAndHeight = Math.min(withAndHeight, withAndHeight);
        final BitmapDrawable regularSim = getRegularSim(withAndHeight);

        final Bitmap bitmap = regularSim.getBitmap();
        final Bitmap bitmapTest = Bitmap.createBitmap(withAndHeight, withAndHeight, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmapTest);

        final float halfWidth = withAndHeight / 2;
        final float halfHeight = withAndHeight / 2;
        paint.setColorFilter(new PorterDuffColorFilter(simColor, PorterDuff.Mode.SRC_ATOP));
        paint.setAlpha(0xff);
        canvas.drawBitmap(bitmap, halfWidth - bitmap.getWidth() / 2,
                halfHeight - bitmap.getHeight() / 2, paint);

        if (!TextUtils.isEmpty(identifier)) {
            paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            paint.setColor(textColor);
            paint.setColorFilter(new PorterDuffColorFilter(textColor, PorterDuff.Mode.SRC_ATOP));
            //Bkav QuangNDb Tach code doan get dimens value ratio
            final float letterToTileRatio =
                    mContext.getResources().getFraction(getSimTitleRatio(withAndHeight), 1, 1);
            paint.setTextSize(letterToTileRatio * minOfWidthAndHeight);

            final String firstCharString = identifier.substring(0, 1).toUpperCase();
            final Rect textBound = new Rect();
            paint.getTextBounds(firstCharString, 0, 1, textBound);

            final float xOffset = halfWidth - textBound.centerX();
            final float yOffset = halfHeight - textBound.centerY();
            if(mFlag){ // Bkav HuyNQN khong thuc hien ve so cua sim voi icon may bay
                canvas.drawText("", xOffset, yOffset, paint);
            }else {
                canvas.drawText(firstCharString, xOffset, yOffset, paint);
            }
        }
        setImageBitmap(bitmapTest);
    }

    public void setImageResourceUriEsim(String pos, int withAndHeight, int color) {
        mIdSim = pos;
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final int simColor =color;
        final BitmapDrawable regularSim = getRegularSim(withAndHeight);

        final Bitmap bitmap = regularSim.getBitmap();
        final Bitmap bitmapTest = Bitmap.createBitmap(withAndHeight, withAndHeight, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmapTest);

        final float halfWidth = withAndHeight / 2;
        final float halfHeight = withAndHeight / 2;
        paint.setColorFilter(new PorterDuffColorFilter(simColor, PorterDuff.Mode.SRC_ATOP));
        paint.setAlpha(0xff);
        canvas.drawBitmap(bitmap, halfWidth - bitmap.getWidth() / 2,
                halfHeight - bitmap.getHeight() / 2, paint);

        if (!TextUtils.isEmpty(pos)) {
            paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            paint.setColor(simColor);
            paint.setColorFilter(new PorterDuffColorFilter(simColor, PorterDuff.Mode.SRC_ATOP));
            //Bkav QuangNDb Tach code doan get dimens value ratio
            paint.setTextSize(30);

            final String firstCharString = pos.substring(0, 1).toUpperCase();
            final Rect textBound = new Rect();
            paint.getTextBounds(firstCharString, 0, 1, textBound);

            final float xOffset = withAndHeight - (withAndHeight / 4 - withAndHeight / 40) - textBound.centerX();
            final float yOffset = withAndHeight - (withAndHeight / 3) - textBound.centerY();

            canvas.drawText(firstCharString, xOffset, yOffset, paint);
        }
        setImageBitmap(bitmapTest);
    }
    private int getSimTitleRatio(int width) {
        if (width < mShapeBig) {
            return R.dimen.sim_identifier_to_tile_ratio_small;
        }else {
            return R.dimen.sim_identifier_to_tile_ratio_medium;
        }
    }

    // Bkav HuyNQN flag nay de check xem co ve so vao icon hay ko
    private boolean mFlag;

    protected BitmapDrawable getRegularSim(int withAndHeight) {
        if (withAndHeight < mShapeBig) {
            mFlag = false;
            return (BitmapDrawable) ContextCompat.getDrawable(mContext,R.drawable.ic_sim_card_small);
        }else {
            mFlag = true;
            return (BitmapDrawable) ContextCompat.getDrawable(mContext,R.drawable.bkav_ic_quick_new_message);
        }
    }


}
