package bkav.android.btalk.messaging.ui.mediapicker.contact;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 22/04/2017.
 */

public class TestFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.btalk_picker_fragment, container, false);
    }
}
