package bkav.android.btalk.contacts.editcontact;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import com.android.contacts.common.model.ValuesDelta;
import com.android.contacts.common.util.MaterialColorMapUtils;
import com.android.contacts.editor.CompactPhotoEditorView;
import com.android.contacts.editor.EditorUiUtils;
import com.android.contacts.editor.PhotoSourceDialogFragment;

import bkav.android.btalk.R;

public class BtalkCompactPhotoEditorView extends CompactPhotoEditorView {

    private PhotoSourceDialogFragment.Listener mPhotoHandler;

    private Uri mLastUri;

    private ValuesDelta mValuesDelta;
    private MaterialColorMapUtils.MaterialPalette mMaterialPalette;

    public BtalkCompactPhotoEditorView(Context context) {
        super(context);
    }

    public BtalkCompactPhotoEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findViewById(R.id.take_photo_from_app).setOnClickListener(this);
        findViewById(R.id.take_photo_from_camera).setOnClickListener(this);
        findViewById(R.id.take_photo_from_gallery).setOnClickListener(this);
    }

    public void setListener(PhotoSourceDialogFragment.Listener photoHandler) {
        mPhotoHandler = photoHandler;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.take_photo_from_app:
                mPhotoHandler.onChooseFromExistImage();
                break;
            case R.id.take_photo_from_gallery:
                mPhotoHandler.onPickFromGalleryChosen();
                break;
            case R.id.take_photo_from_camera:
                mPhotoHandler.onTakePhotoChosen();
                break;
        }
    }

    /**
     * Tries to bind a full size photo or a bitmap loaded from the given ValuesDelta,
     * and falls back to the default avatar, tinted using the given MaterialPalette (if it's not
     * null);
     */
    @Override
    public void setPhoto(ValuesDelta valuesDelta, MaterialColorMapUtils.MaterialPalette materialPalette) {
        super.setPhoto(valuesDelta, materialPalette);
        final Long photoFileId = EditorUiUtils.getPhotoFileId(valuesDelta);
        if (photoFileId != null) {
            mValuesDelta = valuesDelta;
            mMaterialPalette = materialPalette;
        }
    }

    /**
     * Binds a full size photo loaded from the given Uri.G???i b???ng
     */
    @Override
    public void setFullSizedPhoto(Uri photoUri) {
        super.setFullSizedPhoto(photoUri);
        if (!photoUri.toString().contains("default_")) {
            mLastUri = photoUri;
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        mReadOnly = readOnly;
        mPhotoIconOverlay.setVisibility(View.GONE);
        mPhotoTouchInterceptOverlay.setOnClickListener(null);
    }

    public void removePhotoPicker() {
        if (mLastUri != null) {
            setFullSizedPhoto(mLastUri);
            adjustDimensions();
            //Bkav ToanNTe tr?????ng h???p b??? tick ???nh khi li??n h??? ???? c?? ???nh t??? tr?????c th?? truy???n Uri
            //c???a ???nh li??n h??? c?? t??? tr?????c ????? c???p nh???t l???i ???nh
            mSend.sendUri(mLastUri);
        } else {
            // Use the bitmap image from the values delta
            if (mValuesDelta != null) {
                setPhoto(mValuesDelta, mMaterialPalette);
            }
        }
    }

    public void setISend(ISendUri sendUri) {
        mSend = sendUri;
    }

    private ISendUri mSend;
    public interface ISendUri {
        void sendUri(Uri uri);
    }


}
