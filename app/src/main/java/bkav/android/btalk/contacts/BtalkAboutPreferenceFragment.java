package bkav.android.btalk.contacts;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.preference.Preference;

import com.android.contacts.common.preference.AboutPreferenceFragment;

import bkav.android.btalk.R;

/**
 * AnhNDd: class kế thừa AboutPreferenceFragment để thực hiện việc điều chỉnh về giới thiệu danh bạ.
 */
public class BtalkAboutPreferenceFragment extends AboutPreferenceFragment {
    @Override
    public void startShowEulaDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (BtalkContactUtils.isConnectivityStatus(getActivity())) {
            //AnhNDd: Truong hop co ket noi internet
            // Create and show the dialog.
            BtalkDialogShowEULA btalkDialogShowEULA = BtalkDialogShowEULA.newInstance(0);
            btalkDialogShowEULA.setEulaUrl(getResources().getString(R.string.bkav_about_eula_url));
            btalkDialogShowEULA.show(ft, BtalkDialogShowEULA.DIALOG_TAG);
        } else {
            //AnhNDd: Truong hop khong co ket noi mang
            BtalkDialogCheckInternet btalkDialogCheckInternet = BtalkDialogCheckInternet.newInstance(0);
            btalkDialogCheckInternet.show(ft, BtalkDialogCheckInternet.DIALOG_TAG);
        }
    }

    @Override
    public void setIntentLicensePreference(Preference licensePreference) {
        licensePreference.setIntent(new Intent(getActivity(), BtalkLicenseActivity.class));
    }
}
