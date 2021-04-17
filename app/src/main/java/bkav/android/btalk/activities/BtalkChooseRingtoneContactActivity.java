package bkav.android.btalk.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.contacts.ContactSaveService;
import com.android.contacts.editor.EditorUiUtils;
import com.android.contacts.list.ContactPickerFragment;
import com.android.contacts.list.OnContactPickerActionListener;

import bkav.android.btalk.R;
import bkav.android.btalk.fragments.choose_ringtone.BtalkContactChooseRingtoneFragment;

/**
 * Created by quangnd on 30/07/2017.
 * class choose Ringtone for contact
 */

public class BtalkChooseRingtoneContactActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener, OnContactPickerActionListener{
    private SearchView mSearchView;
    ContactPickerFragment mContactFragment;
    protected Uri mLookupUri;
    protected static final int REQUEST_CODE_PICK_RINGTONE = 2;
    private static final int CURRENT_API_VERSION = android.os.Build.VERSION.SDK_INT;
    private static final String KEY_CUSTOM_RINGTONE = "customRingtone";
    private static final String KEY_URI = "uri";
    private String mCustomRingtone;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_picker);
        if (savedInstanceState != null) {
            mCustomRingtone = savedInstanceState.getString(KEY_CUSTOM_RINGTONE);
            mLookupUri = savedInstanceState.getParcelable(KEY_URI);
        }
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContactFragment = new BtalkContactChooseRingtoneFragment();
        mContactFragment.setIncludeProfile(true);
        mContactFragment.setOnContactPickerActionListener(this);
        getFragmentManager().beginTransaction()
                .replace(R.id.list_container, mContactFragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putString(KEY_CUSTOM_RINGTONE, mCustomRingtone);
        outState.putParcelable(KEY_URI, mLookupUri);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.btalk_contact_chooser_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) menuItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        EditText searchEditText = (EditText)mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.btalk_ab_text_and_icon_normal_color));
        searchEditText.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
        searchEditText.setHint(R.string.contact_chooser_search_title);
        ImageView searchButton = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        searchButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_btalk_search));
        ImageView closeButton = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        closeButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_btalk_remove_small));
        View underLine = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        underLine.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        return super.onCreateOptionsMenu(menu);
    }

    private void doPickRingtone() {
        final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        // Allow user to pick 'Default'
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        // Show only ringtones
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        // Allow the user to pick a silent ringtone
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);

        final Uri ringtoneUri = EditorUiUtils.getRingtoneUriFromString(mCustomRingtone,
                CURRENT_API_VERSION);

        // Put checkmark next to the current ringtone for this contact
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri);

        // Launch!
        try {
            // QuangNDb them chooser de khong bi just one, always
            Intent intentChooser = Intent.createChooser(intent, getString(R.string.notify_chooser_app_set_ringtone));
            startActivityForResult(intentChooser, REQUEST_CODE_PICK_RINGTONE);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, com.android.contacts.R.string.missing_app, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PICK_RINGTONE: {
                if (data != null) {
                    final Uri pickedUri = data.getParcelableExtra(
                            RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    onRingtonePicked(pickedUri);
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onRingtonePicked(Uri pickedUri) {
        mCustomRingtone = EditorUiUtils.getRingtoneStringFromUri(pickedUri, CURRENT_API_VERSION);
        Intent intent = ContactSaveService.createSetRingtone(
                this, mLookupUri, mCustomRingtone, true);
        startService(intent);
        Toast.makeText(this, R.string.notify_complete_set_ringtone, Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mContactFragment.setQueryString(newText, true);
        return false;
    }

    @Override
    public void onPickContactAction(Uri contactUri) {
        mLookupUri = contactUri;
        doPickRingtone();
    }

    @Override
    public void onShortcutIntentCreated(Intent intent) {

    }

    @Override
    public void onCreateNewContactAction() {

    }

    @Override
    public void onEditContactAction(Uri contactLookupUri) {

    }
}
