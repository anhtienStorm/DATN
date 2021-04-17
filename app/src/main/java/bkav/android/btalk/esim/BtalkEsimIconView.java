package bkav.android.btalk.esim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import bkav.android.btalk.R;

// Bkav HuyNQN 29/11/2019
public class BtalkEsimIconView extends ImageView {

    private Context mContext;


    public BtalkEsimIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    private BitmapDrawable getRegularSim() {
        return (BitmapDrawable) ContextCompat.getDrawable(mContext, R.drawable.ic_sim_card_esim);
    }


    public void setColorImageResource(int color) {
//        BitmapDrawable bitmapDrawable = getRegularSim();
//        Bitmap bitmap = bitmapDrawable.getBitmap();
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
//        paint.setAlpha(0xff);
//        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(newBitmap);
//        canvas.drawBitmap(bitmap, 0, 0, paint);
//        setImageBitmap(newBitmap);
        // Bkav HienDTk: fix loi hien thi thieu icon chon sim
        setImageResource(R.drawable.ic_sim_card_esim);
        setColorFilter(color);
    }

    private BitmapDrawable getRegularSimDialogChoose() {
        return (BitmapDrawable) ContextCompat.getDrawable(mContext, R.drawable.ic_sim_card_esim_popup);
    }
    public void setColorIconDialogChoose(int color) {
//        BitmapDrawable bitmapDrawable = getRegularSimDialogChoose();
//        Bitmap bitmap = bitmapDrawable.getBitmap();
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
//        paint.setAlpha(0xff);
//        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_4444);
//        Canvas canvas = new Canvas(newBitmap);
//        canvas.drawBitmap(bitmap, 0, 0, null);
//        setImageBitmap(newBitmap);
        // Bkav HienDTk: fix loi hien thi thieu icon chon sim
        setImageResource(R.drawable.ic_sim_card_esim_popup);
        setColorFilter(color);
    }
}
