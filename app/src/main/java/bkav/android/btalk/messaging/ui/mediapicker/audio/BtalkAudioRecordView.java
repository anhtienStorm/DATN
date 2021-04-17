package bkav.android.btalk.messaging.ui.mediapicker.audio;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.android.messaging.Factory;
import com.android.messaging.ui.mediapicker.AudioRecordView;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 06/05/2017.
 */

public class BtalkAudioRecordView extends AudioRecordView {

    public BtalkAudioRecordView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getColorIdle() {
        return Color.WHITE;
    }

    @Override
    protected int getColorRecording() {
        return ContextCompat.getColor(Factory.get().getApplicationContext(), R.color.btalk_ab_text_and_icon_selected_color);
    }
}
