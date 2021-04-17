package bkav.android.btalk.messaging.ui.mediapicker.gallery;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

import com.android.messaging.ui.mediapicker.GalleryGridView;
import com.android.messaging.ui.mediapicker.GalleryMediaChooser;
import com.android.messaging.ui.mediapicker.MediaPicker;
import com.android.messaging.util.OsUtil;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 06/05/2017.
 */

public class BtalkGalleryMediaChooser extends GalleryMediaChooser {

    public BtalkGalleryMediaChooser(MediaPicker mediaPicker) {
        super(mediaPicker);
    }

    @Override
    protected int getIdResBackIcon() {
        return R.drawable.ic_btalk_remove_small;
    }

    @Override
    protected int getIdLayoutRes() {
        return R.layout.btalk_mediapicker_image_chooser;
    }

    @Override
    protected GalleryGridView getGalleryView(View view) {
        return (BtalkGalleryGridView) view.findViewById(R.id.gallery_grid_view);
    }

    @Override
    public int getIconResource() {
        return R.drawable.ic_btalk_image_light;
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
    protected void initAppSettingButton(View view) {
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
        if (mGalleryGridView != null && mMissingPermissionView != null) {
            mGalleryGridView.setVisibility(OsUtil.hasStoragePermission() ? View.VISIBLE : View.GONE);
            mMissingPermissionView.setVisibility(OsUtil.hasStoragePermission() ? View.GONE : View.VISIBLE);
        }
    }
}
