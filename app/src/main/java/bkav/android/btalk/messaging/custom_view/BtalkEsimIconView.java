package bkav.android.btalk.messaging.custom_view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import bkav.android.btalk.R;

public class BtalkEsimIconView extends ImageView {

    private Context mContext;

    public BtalkEsimIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

// Bkav HienDTk: khong dung den nua - fix loi: BOS-2657
//    public void setCustomImageResource(int color, int possitionEsim) {
//        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
//        paint.setAlpha(0xff);
//        BitmapDrawable bitmapDrawable = getRegularSim();
//        Bitmap bitmap = bitmapDrawable.getBitmap();
//        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(newBitmap);
//        canvas.drawBitmap(bitmap, 0, 0, paint);
//        setImageBitmap(newBitmap);
//
//
//        if (!TextUtils.isEmpty(String.valueOf(possitionEsim))) {
//            paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
//            paint.setColor(color);
//            paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
//
//            final String firstCharString = String.valueOf(possitionEsim + 1);
//            final Rect textBound = new Rect();
//            paint.getTextBounds(firstCharString, 0, 1, textBound);
//            paint.setTextSize(25);
//            final float xOffset = bitmap.getWidth() - (bitmap.getWidth() / 4 - bitmap.getWidth() / 20) - textBound.centerX();
//            final float yOffset = bitmap.getHeight() - (bitmap.getHeight() / 3 - bitmap.getHeight() / 20) - textBound.centerY();
//
//            canvas.drawText(firstCharString, xOffset, yOffset, paint);
//        }
//        setImageBitmap(newBitmap);
//    }

    // Bkav HienDTk: fix loi Tin nhắn - BOS 8.7 - Lỗi: Nút Gửi trên giao diện lựa chọn Sim gửi tin nhắn bị lẹm khi máy để kích thước phông chữ và kích thước hiển thị max => BOS-2657 - Start
    public void setImageResource(int pos, int withAndHeight, int color) {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final int simColor = color;
        final BitmapDrawable regularSim = getRegularSim();

        final Bitmap bitmap = regularSim.getBitmap();
        final Bitmap bitmapTest = Bitmap.createBitmap(withAndHeight, withAndHeight, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmapTest);

        final float halfWidth = withAndHeight / 2;
        final float halfHeight = withAndHeight / 2;
        paint.setColorFilter(new PorterDuffColorFilter(simColor, PorterDuff.Mode.SRC_ATOP));
        paint.setAlpha(0xff);
        canvas.drawBitmap(bitmap, halfWidth - bitmap.getWidth() / 2,
                halfHeight - bitmap.getHeight() / 2, paint);

        if (!TextUtils.isEmpty(String.valueOf(pos))) {
            paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            paint.setColor(simColor);
            paint.setColorFilter(new PorterDuffColorFilter(simColor, PorterDuff.Mode.SRC_ATOP));
            paint.setTextSize(30);

            final String firstCharString = String.valueOf(pos + 1);
            final Rect textBound = new Rect();
            paint.getTextBounds(firstCharString, 0, 1, textBound);

            final float xOffset = withAndHeight - (withAndHeight / 4 - withAndHeight / 40) - textBound.centerX();
            final float yOffset = withAndHeight - (withAndHeight / 3) - textBound.centerY();

            canvas.drawText(firstCharString, xOffset, yOffset, paint);
        }
        setImageBitmap(bitmapTest);
    }
    // Bkav HienDTk: fix loi Tin nhắn - BOS 8.7 - Lỗi: Nút Gửi trên giao diện lựa chọn Sim gửi tin nhắn bị lẹm khi máy để kích thước phông chữ và kích thước hiển thị max => BOS-2657 - End

    protected BitmapDrawable getRegularSim() {
        return (BitmapDrawable) ContextCompat.getDrawable(mContext, R.drawable.bkav_ic_quick_new_message);
    }

}
