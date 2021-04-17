package bkav.android.btalk.contacts;


import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;

import com.android.contacts.common.logging.ScreenEvent;
import com.android.contacts.common.model.Contact;
import com.android.contacts.common.model.ContactLoader;
import com.android.contacts.common.preference.DisplayOptionsPreferenceFragment;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.editor.ContactEditorFragment;
import com.android.contacts.editor.EditorIntents;
import com.android.contacts.quickcontact.QuickContactActivity;

import bkav.android.btalk.R;


public class BtalkContactDisplayOptionsPreferenceFragment extends DisplayOptionsPreferenceFragment {
    //AnhNDd: boolean xac minh viec edit contact tu option
    public static final String FROM_OPTION = "fromOption";
    //AnhNDd: string để tìm preference profile
    public static final String PREFERENCE_PROFILE = "profile";
    //AnhNDd: dùng để load contact.
    private ContactLoader mContactLoader;
    //AnhNDd: contact profile để chỉnh sửa.
    private Contact mContactProfileData;

    private int REQUEST_CODE_CONTACT_EDITOR_ACTIVITY_FROM_OPTION = 1;
    /**
     * Id for the background contact loader
     */
    private static final int LOADER_CONTACT_ID = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AnhNDd: Lắng nghe sự kiện thiết lập hồ sơ.
        final Preference profilePreference = findPreference(PREFERENCE_PROFILE);
        profilePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //AnhNDd: Thực hiện truy vấn profile
                getProfile();
                return true;
            }
        });
    }

    @Override
    public void needAddPreferencesFromResource(int preferencesResId) {
        if (BtalkContactsActivity.USE_BTALK) {
            addPreferencesFromResource(R.xml.btalk_contact_prference_display_options);
        } else {
            super.needAddPreferencesFromResource(preferencesResId);
        }
    }
    
    /**Bkav QuangNDb hide divider trong setting di*/
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View rootView = getView();
        ListView list = (ListView) rootView.findViewById(android.R.id.list);
        list.setDivider(null);
    }

    public void showContactEditorFragment() {

        if (mContactProfileData.isUserProfile()) {
            //AnhNDd: Trường hợp có liên hệ người dùng
            mContactLoader.cacheResult();
            Intent intent = getEditContactIntent();
            intent.putExtra(FROM_OPTION, true);
            startActivityForResult(intent, REQUEST_CODE_CONTACT_EDITOR_ACTIVITY_FROM_OPTION);

            /*mContactLoader.cacheResult();
            final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(
                    mContactProfileData.getLookupUri(), BtalkQuickContactActivity.MODE_FULLY_EXPANDED);
            intent.putExtra(BtalkQuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE,
                    ScreenEvent.ScreenType.EDITOR);
            startActivity(intent);*/
        } else {
            //AnhNDd: Trường hợp chưa có liên hệ người dùng
            Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
            intent.putExtra(ContactEditorFragment.INTENT_EXTRA_NEW_LOCAL_PROFILE, true);
            ImplicitIntentsUtil.startActivityInApp(getActivity(), intent);
        }
        /*Uri uri = Uri.parse(mStringUri);
        getContext().getContentResolver().delete(uri,null,null);*/
    }

    /**
     * AnhNDd: thực hiện query lấy contact profile.
     */
    public Contact getProfile() {
        mContactLoader = (ContactLoader) getLoaderManager().initLoader(
                LOADER_CONTACT_ID, null, mLoaderContactCallbacks);
        return mContactProfileData;
    }

    private final LoaderManager.LoaderCallbacks<Contact> mLoaderContactCallbacks = new LoaderManager.LoaderCallbacks<Contact>() {
        @Override
        public Loader<Contact> onCreateLoader(int id, Bundle args) {
            return new ContactLoader(getContext(), ContactsContract.Profile.CONTENT_URI,
                    true /*loadGroupMetaData*/, false /*loadInvitableAccountTypes*/,
                    true /*postViewNotification*/, true /*computeFormattedPhoneNumber*/);
        }

        @Override
        public void onLoadFinished(Loader<Contact> loader, Contact data) {
            //AnhNDd: show edit sau khi load dữ liệu xong.
            mContactProfileData = data;
            showContactEditorFragment();
            getLoaderManager().destroyLoader(LOADER_CONTACT_ID);
        }

        @Override
        public void onLoaderReset(Loader<Contact> loader) {

        }
    };

    //AnhNDd: Thực hiện thiết lập intent editor để chỉnh sửa.
    private Intent getEditContactIntent() {
        return EditorIntents.createCompactEditContactIntent(
                mContactProfileData.getLookupUri(),
                null,
                mContactProfileData.getPhotoId());
    }
}
