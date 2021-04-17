package bkav.android.btalk.utility;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.ActivityPermission;

/**
 * Created by anhdt on 17/11/2017.
 */

public class PermissionUtil {

    private static final String PREF_FIRST_RUN_CHECK_PRE = "PREF_FIRST_RUN_CHECK_PRE";
    // Bkav QuangLH: tuong tu nhu trong BuildConfig. Build bang mmm khong co file nay.
    // Chuyen thanh hang so o day.
    private static final String APPLICATION_ID = "bkav.android.btalk";

    private static final int DELTA_REQUEST_OPEN_SETTINGS = 101;

    private static volatile PermissionUtil sInstance;

    public static PermissionUtil get() {
        if (sInstance == null) {
            sInstance = new PermissionUtil();
        }
        return sInstance;
    }


    /**
     * Anhdts check quyen btalk
     */
    public boolean requestPermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) !=
                        PackageManager.PERMISSION_GRANTED ||
                // Bkav TienNAb: fix loi crash app khi khong cap quyen doc nhat ky cuoc goi
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            if (context instanceof Activity) {
                context.startActivity(new Intent(context, ActivityPermission.class));
                ((Activity)context).finish();
            } else {
                // Bkav HaiKH - Fix bug BOS-4009- Start
                // Check class ActivityPermission tồn tại thì mới tạo intent và start
                try  {
                    Class.forName("ActivityPermission");
                    Intent intent = new Intent(context, ActivityPermission.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }  catch (ClassNotFoundException e) {
                    return false;
                }
                // Bkav HaiKH - Fix bug BOS-4009- End
            }
            return false;
        } else {
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkPermission(String[] pers, int request_code, Activity context, CallbackCheckPermission callback) {
        boolean denyContact = false;
        ArrayList<String> permissions = new ArrayList<>();
        for (String permission : pers) {
            int value = ContextCompat.checkSelfPermission(context, permission);
            if (value != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                        permission) || isFirstTimeAskingPermission(context, permission)) {
                    permissions.add(permission);
                } else {
                    denyContact = true;
                }
            }
        }
        if (permissions.size() > 0) {
            if (denyContact) {
                callback.alwaysDeny(pers);
            } else {
                String[] arrPer = new String[permissions.size()];
                int i = 0;
                for (String permission : permissions) {
                    arrPer[i] = permission;
                    i++;
                }
                context.requestPermissions(arrPer, request_code);
            }
        } else {
            if (denyContact) {
                callback.alwaysDeny(pers);
            } else {
                callback.acceptPermission(pers);
            }
        }
    }

    /**
     * Anhdts callback lai cho activity la dang hien dialog xin cap quyen
     */
    public void showDialogOpenSetting(final Activity activity, final CallbackCheckPermission callback, final String[] pers, final int requestCode, int title, final int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        // Anhdts tao text CAI DAT (APP SETTING) trong values
        builder.setPositiveButton(R.string.positive_title_request_permission, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                activity.startActivityForResult(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + APPLICATION_ID)), requestCode + DELTA_REQUEST_OPEN_SETTINGS);
            }
        });
        // Anhdts tao text KHONG PHAI BAY GIO (NOT NOW) trong values
        builder.setNegativeButton(R.string.negative_title_request_permission, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                callback.denyPermission(pers);
            }
        });
        AlertDialog mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    public int getRequestOpenSetting(int permissionsRequest) {
        return permissionsRequest + DELTA_REQUEST_OPEN_SETTINGS;
    }

    public void onActivityPermissionResult(String[] permissions, int[] grantResults, CallbackCheckPermission callbackCheckPermission, Activity activity) {
        int i = 0;
        boolean isDenied = false;
        boolean isAlwaysDenied = false;
        for (String permission : permissions) {
            if (isFirstTimeAskingPermission(activity, permission)) {
                firstTimeAskingPermission(activity, permission, false);
            }

            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        permission)) {
                    isAlwaysDenied = true;
                }
                isDenied = true;
            }
            i++;
        }
        if (isDenied) {
            callbackCheckPermission.denyPermission(permissions);
        } else {
            callbackCheckPermission.acceptPermission(permissions);
        }
    }

    public interface CallbackCheckPermission {
        void denyPermission(String[] pers);

        void acceptPermission(String[] pers);

        void alwaysDeny(String[] pers);
    }

    //TODO Dang nhe can lam chuan hon bi deny quyen nao thi chi thong bao quyen day thoi, dang hoi chung 1 the
    private void firstTimeAskingPermission(Context context, String permission, boolean isFirstTime) {
        SharedPreferences sharedPreference = context.getSharedPreferences(PREF_FIRST_RUN_CHECK_PRE, Context.MODE_PRIVATE);
        sharedPreference.edit().putBoolean(permission, isFirstTime).apply();
    }

    private boolean isFirstTimeAskingPermission(Context context, String permission) {
        return context.getSharedPreferences(PREF_FIRST_RUN_CHECK_PRE, Context.MODE_PRIVATE).getBoolean(permission, true);
    }
}
