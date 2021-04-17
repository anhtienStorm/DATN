package bkav.android.btalk.messaging.util;

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
import android.widget.ImageButton;

import com.android.messaging.util.AvatarUriUtil;

import bkav.android.btalk.R;

public class ImageButtonSendCustom extends ImageButton {
    private Context mContext;
    protected int mShapeBig;
    private String mIdSim;
    boolean isFlagIconEsim;
    public ImageButtonSendCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mShapeBig = context.getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size);
    }

    public void setImageResourceUriEsim(String pos, int withAndHeight) {
        mIdSim = pos;
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final int simColor = Color.BLUE;
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

    protected BitmapDrawable getRegularSim(int withAndHeight) {
        if (withAndHeight < mShapeBig) {
            return (BitmapDrawable) ContextCompat.getDrawable(mContext,R.drawable.ic_sim_card_small);
        }else {
            return (BitmapDrawable) ContextCompat.getDrawable(mContext,R.drawable.bkav_ic_quick_new_message);
        }
    }

}
