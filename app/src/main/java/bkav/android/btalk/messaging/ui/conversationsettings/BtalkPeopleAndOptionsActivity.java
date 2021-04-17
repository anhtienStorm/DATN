package bkav.android.btalk.messaging.ui.conversationsettings;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.contacts.common.logging.ScreenEvent;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.messaging.Factory;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.conversationsettings.PeopleAndOptionsActivity;
import com.android.messaging.ui.conversationsettings.PeopleAndOptionsFragment;
import com.android.messaging.util.Assert;
import com.android.messaging.util.OsUtil;

import bkav.android.btalk.R;
import bkav.android.btalk.contacts.BtalkQuickContactActivity;
import bkav.android.btalk.messaging.ui.BtalkContactPhotoView;
import bkav.android.btalk.utility.BtalkUiUtils;

/**
 * Created by quangnd on 27/06/2017.
 */

public class BtalkPeopleAndOptionsActivity extends PeopleAndOptionsActivity implements PeopleAndOptionsFragment.onClickListener {

    @Override
    protected int getIdLayoutRes() {
        return R.layout.btalk_activity_people_and_options;
    }

    @Override
    protected void changeActionbarColor() {
        if (OsUtil.isAtLeastM()) {
            findViewById(R.id.people_and_options_fragment).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.actionbar_setting_color)));
        } else {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.actionbar_setting_color_lollipop)));
            BtalkUiUtils.setStatusbarColor(getWindow());
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof BtalkPeopleAndOptionsFragment) {
            final String conversationId =
                    getIntent().getStringExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID);
            Assert.notNull(conversationId);
            final BtalkPeopleAndOptionsFragment peopleAndOptionsFragment =
                    (BtalkPeopleAndOptionsFragment) fragment;
            peopleAndOptionsFragment.setConversationId(conversationId);
            peopleAndOptionsFragment.setListener(this);
        }
    }

    @Override
    public void onClick(String lookupKey, long id, String number) {
        // Bkav HienDTk: lay uri thong qua lookup key va contact id va mo giao dien quick contact
        Uri uri = ContactsContract.Contacts.getLookupUri(id, lookupKey);
        if(uri == null){
            uri = BtalkContactPhotoView.createTemporaryContactUri(number);
        }
        final Intent intentGo = ImplicitIntentsUtil.composeQuickContactIntent(
                uri, ContactsContract.QuickContact.MODE_LARGE);
        intentGo.putExtra(BtalkQuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE, ScreenEvent.ScreenType.UNKNOWN);
        intentGo.putExtra(BtalkQuickContactActivity.CONVERSATION_ID, id);
        ImplicitIntentsUtil.startActivityInApp(Factory.get().getApplicationContext(), intentGo);
    }
}
