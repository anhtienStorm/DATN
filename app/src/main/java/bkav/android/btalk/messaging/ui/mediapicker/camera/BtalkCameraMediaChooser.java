package bkav.android.btalk.messaging.ui.mediapicker.camera;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.messaging.ui.mediapicker.CameraMediaChooser;
import com.android.messaging.ui.mediapicker.CameraMediaChooserView;
import com.android.messaging.ui.mediapicker.MediaPicker;
import com.android.messaging.util.OsUtil;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 06/05/2017.
 */

public class BtalkCameraMediaChooser extends CameraMediaChooser{

    public BtalkCameraMediaChooser(MediaPicker mediaPicker) {
        super(mediaPicker);
    }

    @Override
    public int getIconResource() {
        return R.drawable.ic_btalk_camera_light;
    }
    @Override
    protected void setAlphaTab(boolean selected) {
        //Bkav QuangNDB khong lam gi
    }

    @Override
    public boolean canShowIme() {
        return false;
    }

    private Button mAppSetting;

    @Override
    protected void initAppSettingView(CameraMediaChooserView view) {
        mAppSetting = (Button) view.findViewById(R.id.btn_app_setting_view);
        mAppSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:bkav.android.btalk")));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mEnabledView != null && mMissingPermissionView != null) {
            mEnabledView.setVisibility(OsUtil.hasPermission(Manifest.permission.CAMERA) ? View.VISIBLE : View.GONE);
            mMissingPermissionView.setVisibility(OsUtil.hasPermission(Manifest.permission.CAMERA) ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected CameraMediaChooserView getCameraMediaChooserView(LayoutInflater inflater, ViewGroup container) {
        return (BtalkCameraMediaChooserView) inflater.inflate(
                R.layout.btalk_mediapicker_camera_choooser,
                container /* root */,
                false /* attachToRoot */);
    }
}
