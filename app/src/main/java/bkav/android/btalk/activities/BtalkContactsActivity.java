package bkav.android.btalk.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.contacts.list.UiIntentActions;

import static com.android.messaging.datamodel.DatabaseHelper.PartColumns.ACTION_CALL_CONTACTS;
import static com.android.messaging.datamodel.DatabaseHelper.PartColumns.TEXT_SEARCH;

/**
 * Created by trungth on 02/06/2017.
 */

public class BtalkContactsActivity extends Activity {
    public static final String CONTACT_ACTION = "com.android.contacts.action.LIST_ALL_CONTACTS";

    //HienDTk: lay text search do ben khac gui den
    private String mTextSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intentTextSearch = getIntent();
        if(intentTextSearch != null && intentTextSearch.getAction().equals(ACTION_CALL_CONTACTS)){
            mTextSearch = intentTextSearch.getStringExtra(TEXT_SEARCH);
        }

        Intent intent = new Intent(this, BtalkActivity.class);
        intent.setAction(UiIntentActions.LIST_DEFAULT);
        intent.putExtra(TEXT_SEARCH, mTextSearch);
        startActivity(intent);
        finish();
    }
}
