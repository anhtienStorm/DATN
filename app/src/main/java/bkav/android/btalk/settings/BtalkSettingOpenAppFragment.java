package bkav.android.btalk.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import bkav.android.btalk.R;

/**
 * Created by anhdt on 10/10/2017.
 */

public class BtalkSettingOpenAppFragment extends PreferenceFragment {

    public static final String OPTION_KEEP_STATE = "keep_state_app";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_open_app);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View rootView = getView();
        ListView list = (ListView) rootView.findViewById(android.R.id.list);
        list.setDivider(null);
    }
}
