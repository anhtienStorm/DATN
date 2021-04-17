package bkav.android.btalk.messaging.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.android.messaging.util.OsUtil;

import bkav.android.btalk.R;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_SMS;

/**
 * Created by quangnd on 29/03/2017.
 * class de kiem tra, cho phep,.. viec cap permission cho app Btalk
 */

public class BtalkPermissionUtil {

    public static final String PHONE = Manifest.permission.READ_PHONE_STATE;
    public static final String AUDIO = Manifest.permission.RECORD_AUDIO;
    public static final String STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final int REQUEST_READ_SMS = 111;

    private static String[] sRequiredPermissions = new String[]{
            // Required to read existing SMS threads
            PHONE,
            // Required for knowing the phone number, number of SIMs, etc.
            AUDIO,
            // This is not strictly required, but simplifies the contact picker scenarios
            STORAGE,
    };

    /**
     * Does the app have a the specified STORAGE
     */
    public static boolean hasStoragePermissions(Context context) {
        return hasPermission(context, STORAGE);
    }

    /**
     * Does the app have all the specified permissions
     */
    public static boolean hasPermissions(Context context) {
        for (final String permission : sRequiredPermissions) {
            if (!hasPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Does the app have a the specified permissions
     */
    private static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Bkav QuangNDb Truong hop mo setting ung dung len trong TH nguoi dung click vao nerver ask again
     */
    public static void openSettingIfCheckNerverAskAgain(final Activity activity, final int requestCode, View view) {
//            final AlertDialog.Builder builder = new AlertDialog.Builder(activity).
//                    setMessage(R.string.notify_not_permission)
//                    .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
//                            intent.setData(uri);
//                            activity.startActivityForResult(intent, requestCode);
//                        }
//                    });
//            builder.create().show();
    }

    /**Bkav QuangNDb thong bao chua duoc cap quyen truy cap bo nho*/
    public static void notifyStoragePermissionDenied(Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity).
                    setMessage(R.string.notify_not_permission)
                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            builder.create().show();
    }

    /**
     * Bkav QuangNDb Request permission
     */
    public static void requestPermission(Activity activity, int requestCode) {
        if (OsUtil.isAtLeastM()) {
            activity.requestPermissions(sRequiredPermissions, requestCode);
        }
    }

    /**
     * Bkav QuangNDb Request permission storage
     */
    public static void requestStoragePermission(Activity activity, int requestCode) {
        if (OsUtil.isAtLeastM()) {
            activity.requestPermissions(new String[]{STORAGE}, requestCode);
        }
    }

    // Bkav HuyNQN kiem tra xem da duoc cap quyen read SMS hay chua
    public static  boolean hasPermissionReadSMS(Context context){
        return ( ContextCompat.checkSelfPermission(context, READ_SMS ) == PackageManager.PERMISSION_GRANTED);
    }
}
