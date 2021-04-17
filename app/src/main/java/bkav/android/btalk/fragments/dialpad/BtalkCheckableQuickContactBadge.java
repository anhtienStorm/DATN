package bkav.android.btalk.fragments.dialpad;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.view.View;

import com.android.contacts.common.compat.ContactsCompat;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.common.widget.CheckableQuickContactBadge;

import bkav.android.btalk.contacts.BtalkQuickContactActivity;

/**
 * Created by anhdt on 05/08/2017.
 * Lop nay dung de custom cho item lop search contact tab phone
 */

public class BtalkCheckableQuickContactBadge extends CheckableQuickContactBadge implements View.OnClickListener {


    public BtalkCheckableQuickContactBadge(Context context) {
        this(context, null);
    }

    public BtalkCheckableQuickContactBadge(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BtalkCheckableQuickContactBadge(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BtalkCheckableQuickContactBadge(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    @Override
    public void onClick(View v) {
        if (ContactsCompat.isEnterpriseContactId(mId)) {
            // No implicit intent as user may have a different contacts app in work profile.
            ContactsContract.QuickContact.showQuickContact(getContext(), new Rect(), mLookupUri,
                    BtalkQuickContactActivity.MODE_FULLY_EXPANDED, null);
        } else {
            final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(
                    mLookupUri, BtalkQuickContactActivity.MODE_FULLY_EXPANDED);
            intent.setPackage(getContext().getPackageName());
            intent.putExtra(BtalkQuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE,
                    com.android.contacts.common.logging.ScreenEvent.ScreenType.ALL_CONTACTS);
            try {
                ImplicitIntentsUtil.startActivityInApp(getContext(), intent);
            } catch (Exception ignored) {
            }
        }
    }

    private long mId;

    private Uri mLookupUri;

    public void setData(long id, String lookupKey) {
        mId = id;
        mLookupUri = ContactsContract.Contacts.getLookupUri(mId, lookupKey);
    }

    public void setData(long id, Uri lookupUri) {
        mId = id;
        mLookupUri = lookupUri;
    }
}