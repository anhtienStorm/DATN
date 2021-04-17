package bkav.android.btalk.speeddial;

import android.content.Context;
import android.util.AttributeSet;

import com.android.dialer.list.PhoneFavoriteSquareTileView;

import bkav.android.btalk.R;

/**
 * Created by anhdt on 07/03/2018.
 */

public class BtalkPhoneFavoriteSquareTileView extends PhoneFavoriteSquareTileView {

    public BtalkPhoneFavoriteSquareTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);

        int height = (getResources().getDisplayMetrics().heightPixels -
                getResources().getDimensionPixelSize(R.dimen.very_small_margin)) / 5;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            );
        }
        setMeasuredDimension(width, height);
    }

}
