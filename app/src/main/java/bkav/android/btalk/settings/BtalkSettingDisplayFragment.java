package bkav.android.btalk.settings;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

import bkav.android.btalk.R;

/**
 * Created by anhdt on 10/10/2017.
 *
 */

public class BtalkSettingDisplayFragment extends PreferenceFragment {

    public static final String OPTION_TWO_CALL_BUTTON = "two_call_button";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_display_options);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getPreferenceScreen().removePreference(findPreference(OPTION_TWO_CALL_BUTTON));
        }
    }
}
