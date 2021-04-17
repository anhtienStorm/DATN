package bkav.android.btalk.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import bkav.android.btalk.R;
import bkav.android.btalk.utility.PermissionUtil;

/**
 * Created by anhdt on 12/10/2017.
 *
 */

public class ActivityPermission extends Activity implements PermissionUtil.CallbackCheckPermission {

    private static final int PERMISSIONS_REQUEST = 201;
    //Bkav QuangNDb them cac quyen tren android P bat update
    private static final String[] PERMISSION = {Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_MMS,
            Manifest.permission.RECEIVE_SMS,
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtil.get().checkPermission(PERMISSION, PERMISSIONS_REQUEST, this, this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            PermissionUtil.get().onActivityPermissionResult(permissions, grantResults, this, this);
        } else {
            finish();
        }
    }

    /**
     * Anhdts result thi check lai xem co vua yeu cau vao setting cap quyen khong
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionUtil.get().getRequestOpenSetting(PERMISSIONS_REQUEST)) {
            PermissionUtil.get().checkPermission(PERMISSION, PERMISSIONS_REQUEST, this, this);
        }
    }

    /**
     * Anhdts tu choi khong cho cap quyen
     */
    @Override
    public void denyPermission(String[] pers) {
        finish();
    }

    /**
     * Anhdts chap nhan cap quyen
     */
    @Override
    public void acceptPermission(String[] pers) {
        Intent intent = new Intent(this, BtalkActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        startActivity(intent);
        finish();
    }

    /**
     * Anhdts luon tu choi 1 trong nhung permission nao do
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void alwaysDeny(String[] pers) {
        PermissionUtil.get().showDialogOpenSetting(this, this, pers, PERMISSIONS_REQUEST, R.string.title_request_permission, R.string.message_request_permission);
    }
}
