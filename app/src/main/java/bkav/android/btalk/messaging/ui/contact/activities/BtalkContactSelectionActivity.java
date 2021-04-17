package bkav.android.btalk.messaging.ui.contact.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.contacts.activities.ContactSelectionActivity;
import com.android.contacts.common.list.PhoneNumberPickerFragment;
import com.android.contacts.list.ContactsRequest;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.ui.contacts.common.list.BtalkContactPhoneNumberPickerFragment;

/**
 * Created by quangnd on 20/04/2017.
 */

public class BtalkContactSelectionActivity extends ContactSelectionActivity {

    @Override
    protected PhoneNumberPickerFragment initPhoneNumberFragment() {
        return new BtalkContactPhoneNumberPickerFragment();
    }

    @Override
    protected int getIdResMenu() {
        return R.menu.btalk_contact_selection_menu;
    }

    @Override
    protected int getIdResCustomActionBar() {
        return R.layout.btalk_contact_selection_custom_action_bar;
    }

    // Anhdts
    @Override
    protected int getTitleConvertPhone() {
        return R.string.convert_phone;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (mRequest.getActionCode() == ContactsRequest.ACTION_CONVERT_PHONE) {
            mListFragment.setModeConvertPhone();
        }
    }
}
