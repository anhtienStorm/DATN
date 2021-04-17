package bkav.android.btalk.contacts;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import com.android.contacts.activities.PeopleActivity;
import com.android.incallui.Log;


public class BtalkContactsActivity extends PeopleActivity {
    public static boolean USE_BTALK = true;

    @Override
    protected void onCreate(Bundle savedState) {
        showLog("AnhNDd: onCreate");
        super.onCreate(savedState);
    }

    @Override
    public void findAllFragment(FragmentManager fragmentManager, String ALL_TAG) {
        if (USE_BTALK) {
            mAllFragment = (BtalkMultiSelectContactsListFragment)
                    fragmentManager.findFragmentByTag(ALL_TAG);
        } else {
            super.findAllFragment(fragmentManager, ALL_TAG);
        }
    }

    @Override
    public void createNewAllFragment() {
        if (USE_BTALK) {
            mAllFragment = new BtalkMultiSelectContactsListFragment();
        } else {
            super.createNewAllFragment();
        }
    }

    @Override
    public void startActivityContactsPreference() {
        if (USE_BTALK) {
            startActivity(new Intent(this, BtalkContactsPreferenceActivity.class));
        } else {
            super.startActivityContactsPreference();
        }
    }

    //AnhNDd: dung de show log
    public static boolean DEV_DEBUG = false;
    public static String TAG = "BtalkContacts";

    public static void showLog(String show) {
        if (DEV_DEBUG) {
            Log.i(TAG, show);
        }
    }
    //--------------------------


}

