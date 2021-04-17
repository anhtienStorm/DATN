package bkav.android.btalk.utility;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.messaging.Factory;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.UiUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import bkav.android.btalk.R;

import static com.android.common.util.DeviceVersionUtil.isBL01Device;
import static com.android.dialer.DialerApplication.getContext;
import static com.android.messaging.util.PhoneUtils.DEFAULT_SIM_SETTING_ALWAYS_ASK_BL01;
/**
 * Created by trungth on 04/05/2017.
 *
 */

public class BtalkUiUtils extends UiUtils {

    public static void setSystemUiVisibility(View rootView, int visibility) {
        if (OsUtil.isAtLeastL_MR1()) {
            int flags = rootView.getSystemUiVisibility();
            flags |= visibility;
            rootView.setSystemUiVisibility(flags);
        }
    }

    /**Bkav QuangNDb reset lai mau cho icon statusbar*/
    public static void resetSystemUiVisibility(View rootView) {
        if (OsUtil.isAtLeastL_MR1()) {
            rootView.setSystemUiVisibility(0);
        }
    }

    /**
     * Bkav TrungTH: animation for show view hien len mo mo
     *
     */
    public static void visibilityViewWithAnimation(View view) {
        view.setAlpha(0);
        view.animate().alpha(1);
        view.setVisibility(View.VISIBLE);
    }

    /**
     * Anhdts check xem co phai che do da cua so khong
     */
    public static boolean isModeMultiScreen(Activity context) {
        Configuration configuration = context.getBaseContext().getResources().getConfiguration();
        return (configuration.screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL;
    }

    /**Bkav QuangNDb doi mau status bar*/
    public static void setStatusbarColor(Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(Factory.get().getApplicationContext(), R.color.actionbar_setting_color_lollipop));
    }

    /**
     * HienDTk: check xem nguoi dung co dang de che do luon hoi khi nhan tin hay khong
     */
    public static boolean isAlwaysAskBeforeSendSms() {
        // Bkav HaiKH - Fix bug BOS-3244- Start
        // Check phiên bản thiết bị để trả về giá trị đúng (-2) khi bật chế độ hỏi khi gửi tin nhắn
        if(isBL01Device()){
            final SubscriptionManager subscriptionManager = SubscriptionManager.from(getContext());
            int subId = subscriptionManager.getDefaultSmsSubscriptionId();
            return subId == DEFAULT_SIM_SETTING_ALWAYS_ASK_BL01;
        }else{
            try {
                Method isSMSPromptEnabledMethod = SmsManager.class.getMethod("isSMSPromptEnabled");
                isSMSPromptEnabledMethod.setAccessible(true);
                return (boolean) isSMSPromptEnabledMethod.invoke(SmsManager.getDefault());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            // Bkav HaiKH - Fix bug BOS-3244- End
        }
        return false;
    }



}
