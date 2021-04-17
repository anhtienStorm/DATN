package bkav.android.btalk.contacts.editcontact;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.contacts.detail.PhotoSelectionHandler;

import java.io.FileNotFoundException;

import bkav.android.btalk.R;

public class BtalkDefaultPhotoPickerFragment extends Fragment implements BtalkPhotoPickerView.PhotoPickerListener {

    private PhotoSelectionHandler mPhotoHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.btalk_layout_default_photo_picker, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((BtalkPhotoPickerView) view.findViewById(R.id.picker_container)).setPickerListener(this);
    }

    public void setPhotoHandler(PhotoSelectionHandler photoHandler, Uri lastUri) {
        mPhotoHandler = photoHandler;
    }

    @Override
    public void pickerPhoto(Uri uri) {
        try {
            mPhotoHandler.getListener().onPhotoSelected(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void removePhoto() {
        mPhotoHandler.getListener().onRemovePhotoPicker();
    }
}
