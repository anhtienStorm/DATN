package bkav.android.btalk.text_shortcut;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.view.View;

/**
 * Created by quanglh on 01/08/2017.
 */
public class Utilities {
    public static final int BKAV_DURATION_ANIMATION_FACTOR = 1;
    public static final boolean ATLEAST_JB_MR1 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

    public static final boolean ATLEAST_LOLLIPOP_MR1 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl(Resources res) {
        return ATLEAST_JB_MR1 &&
                (res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }
}
