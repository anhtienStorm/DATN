package bkav.android.btalk.contacts;


import android.os.Bundle;

import com.android.contacts.common.preference.ContactsPreferenceActivity;

import bkav.android.btalk.R;

/**
 * AnhNDd: class kế thừa từ ContactsPreferenceActivity để thực hiện chỉnh sửa việc setting theo
 * ý mình.
 */
public class BtalkContactsPreferenceActivity extends ContactsPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getListView().setBackgroundColor(getResources().getColor(R.color.btalk_white_bg));
    }

    @Override
    public void createDisplayOptionsPreferenceFragment() {
        if (BtalkContactsActivity.USE_BTALK) {
            //AnhNDd: tao fragment option btalk
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new BtalkContactDisplayOptionsPreferenceFragment())
                    .commit();
        } else {
            super.createDisplayOptionsPreferenceFragment();
        }
    }

    @Override
    public void createAboutPreferenceFragment() {
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new BtalkAboutPreferenceFragment(), TAG_ABOUT_CONTACTS)
                .addToBackStack(null)
                .commit();
    }
}
