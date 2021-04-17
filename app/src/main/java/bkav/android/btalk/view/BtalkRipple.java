package bkav.android.btalk.view;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by anhdt on 27/02/2018.
 */

public class BtalkRipple extends RippleDrawable {
    /**
     * Creates a new ripple drawable with the specified ripple color and
     * optional content and mask drawables.
     *  @param color The ripple color
     * @param content The content drawable, may be {@code null}
     * @param mask The mask drawable, may be {@code null}
     */
    public BtalkRipple(@NonNull ColorStateList color, @Nullable Drawable content, @Nullable Drawable mask) {
        super(color, content, mask);
    }


}
