package bkav.android.btalk.messaging.ui.mediapicker.emoticon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.ui.mediapicker.MediaChooser;
import com.android.messaging.ui.mediapicker.MediaPicker;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.ui.mediapicker.BtalkMediaPicker;

/**
 * Created by quangnd on 14/04/2017.
 * QuangNDb class hien thi giao dien attch emoticon
 */

public class EmoticonChooser extends MediaChooser implements EmoticonView.HostInterface{

    /**
     * Initializes a new instance of the Chooser class
     *
     * @param mediaPicker The media picker that the chooser is hosted in
     */
    public EmoticonChooser(MediaPicker mediaPicker) {
        super(mediaPicker);
    }

    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = getLayoutInflater();
        final EmoticonView view = (EmoticonView)inflater.inflate(R.layout.mediapicker_emoticon_chooser, container, false);
        view.setHost(this);
        return view;
    }

    @Override
    public int getSupportedMediaTypes() {
        return BtalkMediaPicker.MEDIA_TYPE_EMOTICON;
    }

    // Anhdts doi icon ic_btalk_emoticon_chooser =>>
    // Bkav TienNAb: Thay icon ic_btalk_emotion_chooser =>> btalk_ic_emoticon
    @Override
    protected int getIconResource() {
        return R.drawable.btalk_ic_emoticon;
    }

    @Override
    protected int getIconDescriptionResource() {
        return R.string.media_chooser_emoticon;
    }

    @Override
    public int getActionBarTitleResId() {
        return R.string.media_chooser_emoticon;
    }

    @Override
    public void onEmoticonClick(MessagePartData messagePartData) {
        mMediaPicker.dispatchItemsSelected(messagePartData, false);
    }
    @Override
    protected int getIdResBackIcon() {
        return R.drawable.ic_btalk_remove_small;
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
