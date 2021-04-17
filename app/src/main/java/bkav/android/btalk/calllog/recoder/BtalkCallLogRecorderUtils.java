package bkav.android.btalk.calllog.recoder;

import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*
 * BKAV HuyNQn create 23/05/2020
 * */
public class BtalkCallLogRecorderUtils {
    // Bkav HuyNQN ham check hien thi phim dieu huong co ban
    public static boolean navigationBarIsVisible(Context context) {
        Display d = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth > displayWidth) || (realHeight > displayHeight);
    }

    // Bkav HuyNQN do chieu co cua view dieu huong co ban
    public static int getHeightOfNavigationBar(Context context) {
        int navigationBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return navigationBarHeight;
    }

    // Bkav HuyNQN su dung check NavigationBar
    public static boolean hasNavigationBar() {
        try {
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            IBinder serviceBinder = (IBinder) serviceManager.getMethod("getService", String.class).invoke(serviceManager, "window");
            Class<?> stub = Class.forName("android.view.IWindowManager$Stub");
            Object windowManagerService = stub.getMethod("asInterface", IBinder.class).invoke(stub, serviceBinder);
            Method hasNavigationBar;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                hasNavigationBar = windowManagerService.getClass().getMethod("hasNavigationBar", int.class);
                return (boolean) hasNavigationBar.invoke(windowManagerService, 0);
            }else{
                hasNavigationBar = windowManagerService.getClass().getMethod("hasNavigationBar");
                return (boolean) hasNavigationBar.invoke(windowManagerService);
            }
        } catch (ClassNotFoundException | ClassCastException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return true;
        }
    }
}
