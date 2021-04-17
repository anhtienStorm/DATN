package bkav.android.btalk.messaging.util;

import android.graphics.Typeface;

import com.android.messaging.Factory;

/**
 * Created by quangnd on 26/04/2017.
 * class chua cac type face cua btalk
 */

public class BtalkTypefaces {
    public static Typeface sRobotoRegularFont = Typeface.createFromAsset(Factory.get().getApplicationContext().getAssets(),
            "fonts/Roboto-Regular.ttf");
    public static Typeface sRobotoLightFont = Typeface.createFromAsset(Factory.get().getApplicationContext().getAssets(),
            "fonts/Roboto-Light.ttf");
    public static Typeface sRobotoBoldFont = Typeface.createFromAsset(Factory.get().getApplicationContext().getAssets(),
            "fonts/Roboto-Bold.ttf");
}
