package bkav.android.btalk.messaging.ui.mediapicker.audio;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.messaging.ui.mediapicker.AudioMediaChooser;
import com.android.messaging.ui.mediapicker.AudioRecordView;
import com.android.messaging.ui.mediapicker.MediaPicker;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 06/05/2017.
 */

public class BtalkAudioMediaChooser extends AudioMediaChooser {

    public BtalkAudioMediaChooser(MediaPicker mediaPicker) {
        super(mediaPicker);
    }

    @Override
    protected int getIdResBackIcon() {
        return R.drawable.ic_btalk_remove_small;
    }

    @Override
    public int getIconResource() {
        return R.drawable.ic_btalk_audio_light;
    }
    private Button mAppSetting;

    @Override
    protected void initAppSettingView(AudioRecordView view) {
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
    protected AudioRecordView getAudioRecordView(LayoutInflater inflater, ViewGroup container) {
        return (BtalkAudioRecordView) inflater.inflate(
                R.layout.btalk_mediapicker_audio_chooser,
                container /* root */,
                false /* attachToRoot */);
    }



    @Override
    protected void setAlphaTab(boolean selected) {
        //Bkav QuangNDB khong lam gi
    }



    @Override
    public boolean canShowIme() {
        return false;
    }
}
