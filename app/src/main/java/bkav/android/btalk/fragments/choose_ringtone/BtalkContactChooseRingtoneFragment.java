package bkav.android.btalk.fragments.choose_ringtone;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;

import com.android.contacts.common.list.ContactListFilter;
import com.android.contacts.list.ContactPickerFragment;
import com.android.contacts.list.HeaderEntryContactListAdapter;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by quangnd on 30/07/2017.
 */

public class BtalkContactChooseRingtoneFragment extends ContactPickerFragment {

    @Override
    protected void setFilter(HeaderEntryContactListAdapter adapter) {
        adapter.setFilter(ContactListFilter.createFilterWithType(
                configureFilter()));
    }

    @Override
    protected int configureFilter() {
        int mContactListFilter = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS :
                ContactListFilter.FILTER_TYPE_ALL_WITHOUT_SIM;

        Intent intent = getActivity().getIntent();
        Bundle extras = intent.getExtras();
        boolean canShowSimContacts =
                launchAddToContactDialog(extras);

        if (canShowSimContacts) {
            mContactListFilter = ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS;

            if (extras.containsKey(ContactsContract.Intents.Insert.EMAIL)) {
                mContactListFilter = ContactListFilter.FILTER_TYPE_CAN_SAVE_EMAIL;
            }
        }

        return mContactListFilter;
    }


    public boolean launchAddToContactDialog(Bundle extras) {
        if (extras == null) {
            return false;
        }

        // Copy extras because the set may be modified in the next step
        Set<String> intentExtraKeys = Sets.newHashSet();
        intentExtraKeys.addAll(extras.keySet());

        // Ignore name key because this is an existing contact.
        if (intentExtraKeys.contains(ContactsContract.Intents.Insert.NAME)) {
            intentExtraKeys.remove(ContactsContract.Intents.Insert.NAME);
        }

        int numIntentExtraKeys = intentExtraKeys.size();
        // We should limit extras strictly. if there only have Insert.PHONE or Insert.EMAIL
        // or Insert.DATA which size is only one and only includes phone or email type,
        // there can show the dialog.
        if (numIntentExtraKeys == 1 && intentExtraKeys.contains(ContactsContract.Intents.Insert.DATA)) {
            ArrayList<ContentValues> values = extras.getParcelableArrayList(ContactsContract.Intents.Insert.DATA);
            if (values.size() == 1) {
                ContentValues cv = values.get(0);
                if (ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(cv.getAsString(ContactsContract.Data.MIMETYPE))) {
                    extras.putString(ContactsContract.Intents.Insert.PHONE, cv.getAsString(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    extras.putInt(ContactsContract.Intents.Insert.PHONE_TYPE, cv.getAsInteger(ContactsContract.CommonDataKinds.Phone.TYPE) == 0 ?
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE : cv.getAsInteger(ContactsContract.CommonDataKinds.Phone.TYPE));
                } else if (ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(cv.getAsString(ContactsContract.Data.MIMETYPE))) {
                    extras.putString(ContactsContract.Intents.Insert.EMAIL, cv.getAsString(ContactsContract.CommonDataKinds.Email.DATA));
                    extras.putInt(ContactsContract.Intents.Insert.EMAIL_TYPE, cv.getAsInteger(ContactsContract.CommonDataKinds.Email.TYPE) == 0 ?
                            ContactsContract.CommonDataKinds.Email.TYPE_HOME : cv.getAsInteger(ContactsContract.CommonDataKinds.Email.TYPE));
                } else {
                    return false;
                }

                extras.remove(ContactsContract.Intents.Insert.DATA);
                return true;
            }
        }

        if (numIntentExtraKeys == 2) {
            boolean hasPhone = intentExtraKeys.contains(ContactsContract.Intents.Insert.PHONE) &&
                    intentExtraKeys.contains(ContactsContract.Intents.Insert.PHONE_TYPE);
            boolean hasEmail = intentExtraKeys.contains(ContactsContract.Intents.Insert.EMAIL) &&
                    intentExtraKeys.contains(ContactsContract.Intents.Insert.EMAIL_TYPE);
            return hasPhone || hasEmail;
        } else if (numIntentExtraKeys == 1) {
            return intentExtraKeys.contains(ContactsContract.Intents.Insert.PHONE) ||
                    intentExtraKeys.contains(ContactsContract.Intents.Insert.EMAIL);
        }
        // Having 0 or more than 2 intent extra keys means that we should launch
        // the full contact editor to properly handle the intent extras.
        return false;

    }
}
