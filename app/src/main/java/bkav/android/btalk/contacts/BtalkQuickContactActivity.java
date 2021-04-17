package bkav.android.btalk.contacts;

import android.accounts.Account;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.contacts.activities.ContactEditorActivity;
import com.android.contacts.common.model.Contact;
import com.android.contacts.common.model.ContactLoader;
import com.android.contacts.editor.ContactEditorBaseFragment;
import com.android.contacts.editor.ContactEditorFragment;
import com.android.contacts.quickcontact.DirectoryContactUtil;
import com.android.contacts.quickcontact.ExpandingEntryCardView;
import com.android.contacts.quickcontact.InvisibleContactUtil;
import com.android.contacts.quickcontact.QuickContactActivity;
import com.android.contacts.widget.MultiShrinkScroller;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.TelecomUtil;
import com.android.messaging.Factory;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.ui.UIIntents;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.recoder.BtalkCallLogRecorderUtils;
import bkav.android.btalk.calllog.recoder.RecorderService;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.esim.dialog_choose_sim.DialogChooseSimFragment;
import bkav.android.btalk.esim.provider.ESimDbController;
import bkav.android.btalk.messaging.BtalkFactoryImpl;
import bkav.android.btalk.messaging.ui.BtalkUIIntentsImpl;
import bkav.android.btalk.mutilsim.SimUltil;

/**
 * AnhNDd: class kế thừa QuickContactActivity để custom giao diện xem
 * khi click xem thông tin contact.
 */
public class BtalkQuickContactActivity extends QuickContactActivity {
    private RecorderService mService;
    private Intent mPlayIntent;
    ExpandingEntryCardView cardView;
    private boolean isShowListSim;
    private DialogChooseSimFragment mDialogChooseSimFragment;

    // Bkav TienNAb: hang so Zalo, Viber
    private static final String ZALO = "Zalo";
    private static final String VIBER = "Viber";

    private static final String CHECK_ACTION_SENTO = "smsto";

    // Bkav HuyNQN Cai dat cho cardView
    @Override
    public void getRecentCardView(ExpandingEntryCardView view) {
        cardView = view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Anhdts fake navigation
//        if (mScroller != null && navigationBarIsVisible()) {
//            showFakeNavigationBackground();
//        }
        // Bkav HienDTk: check xem navigation bar co hien thi hay khong va thuc hien set p
        if (BtalkCallLogRecorderUtils.hasNavigationBar()) {
            findViewById(R.id.multiscroller).setPadding(0, 0, 0, navigationBarIsVisible() ?
                    getHeightOfNavigationBar() : 0);
        }


        if (BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot()) {
            if (ESimDbController.getAllSim().size() < 3) {
                isShowListSim = false;
            } else {
                PhoneAccountHandle handleDefault = TelecomUtil.getDefaultOutgoingPhoneAccount(getApplicationContext(), PhoneAccount.SCHEME_TEL);
                int slotDefault = SimUltil.getSlotSimByAccount(getApplicationContext(), handleDefault); // Bkav HuyNQN neu co 1 sim hoac 2 sim ma ko dat sim mac dinh thi gia tri tra ve = -1;
                isShowListSim = slotDefault != -1;
            }
        } else {
            isShowListSim = ESimUtils.isMultiProfile();
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecorderService.RecorderBinder recorderBinder = (RecorderService.RecorderBinder) service;
            mService = recorderBinder.getService();
            ((BtalkExpandingEntryCardView) cardView).setService(mService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    // Bkav HuyNQN bind service
    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPlayIntent = new Intent(getApplicationContext(), RecorderService.class);
            bindService(mPlayIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            startService(mPlayIntent);
        }
    }

    // Bkav HaiKH - Fix bug BOS-3732- Start
    // Hàm update âm báo tin nhắn khi thay đổi trong danh bạ
    private void updateRingtone(){
        Cursor cursorContact = null;
        String[] lookup = mLookupUri.toString().split("/");
        ContentValues values = new ContentValues();
        if (lookup.length > 2){
            String[] args = { lookup[lookup.length - 2] };
            String[] projectionContact = new String[] {
                    ContactsContract.Contacts.LOOKUP_KEY,
                    ContactLoader.CUSTOM_RINGTONE_SMS
            };
            try {
                cursorContact = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                        projectionContact,ContactsContract.Contacts.LOOKUP_KEY + " = ?",args,null);
                if (cursorContact != null && cursorContact.getCount() > 0) {
                    cursorContact.moveToFirst();
                    String uriRingtone = cursorContact.getString(cursorContact.getColumnIndex(ContactLoader.CUSTOM_RINGTONE_SMS));
                    values.put(DatabaseHelper.ConversationColumns.NOTIFICATION_SOUND_URI,uriRingtone);
                    getContentResolver().update(MessagingContentProvider.CONVERSATIONS_URI_ALL,values,DatabaseHelper.ConversationColumns.PARTICIPANT_LOOKUP_KEY + " = ?",args);
                }
            } finally {
                if (cursorContact != null) {
                    cursorContact.close();
                }
            }
        }
    }
    // Bkav HaiKH - Fix bug BOS-3732- End

    @Override
    protected void onResume() {
        super.onResume();
        // Bkav HaiKH - Fix bug BOS-3732- Start
        // Nếu giá trị nhận đươc khác "ringtone" thì mới update, ngược lại thì put 1 giá trị khác
        Intent intent = getIntent();
        if (!(BtalkMultiSelectContactsListFragment.UPDATE_RINGTONE_VALUE.equals(intent.getStringExtra(BtalkMultiSelectContactsListFragment.UPDATE_RINGTONE_KEY)))) {
            updateRingtone();
        } else {
            intent.putExtra(BtalkMultiSelectContactsListFragment.UPDATE_RINGTONE_KEY, BtalkMultiSelectContactsListFragment.NO_UPDATE_RINGTONE_VALUE);
        }
        // Bkav HaiKH - Fix bug BOS-3732- End
    }

    // Bkav HuyNQN unbindService
    @Override
    public void onStop() {
        super.onStop();

        // Bkav HuyNQN kiem tra neu service ton tai moi unbind
        if (mService != null) {
            mService.onStopSelf();
            unbindService(mServiceConnection);
        }
    }

    // Bkav QuangNDb them ham set packag name cho intent de app khac khong bat dc
    @Override
    protected void setPackageName(Intent intent) {
        intent.setPackage(this.getPackageName());
    }

    @Override
    public void contactCardInitialize(List<List<ExpandingEntryCardView.Entry>> contactCardEntries, boolean firstEntriesArePrioritizedMimeType) {
        //AnhNDd: Thực hiện việc hiển thị toàn bộ số điện thoại

        mContactCard.initialize(contactCardEntries,
                /* numInitialVisibleEntries = */ MIN_NUM_CONTACT_ENTRIES_SHOWN,
                /* isExpanded = */ true,
                /* isAlwaysExpanded = */ true,
                mExpandingEntryCardViewListener,
                mScroller,
                firstEntriesArePrioritizedMimeType);
    }

    /**
     * Anhdts
     */

    @Override
    protected void checkActionSendTo(Intent intent) {
        //HienDTk: bat intent dang ky su kien gui tin nhan de khi back lai
        // Bkav TienNAb: Fix loi thoat ve launcher khi click icon back trong giao dien soan tin nhan
        if (intent.getAction() != null && (intent.getAction().equals(Intent.ACTION_SENDTO) || intent.getDataString().contains(CHECK_ACTION_SENTO))) {
            ((BtalkFactoryImpl) Factory.get()).registerSendMessage();
        }
    }

    /**
     * Anhdts them menu add mot so moico
     */
    @Override
    protected void inflateMenuAddNew(Menu menu, Contact mContactData) {
        final MenuItem editMenuItem = menu.findItem(R.id.menu_add_new_contact);
        if (DirectoryContactUtil.isDirectoryContact(mContactData) || InvisibleContactUtil
                .isInvisibleAndAddable(mContactData, this)) {
            editMenuItem.setVisible(true);
            editMenuItem.setIcon(R.drawable.ic_add_new_contact);
            editMenuItem.setTitle(R.string.menu_new_contact_action_bar);
        } else {
            editMenuItem.setVisible(false);
        }
    }

    /**
     * Anhdts get icon them danh ba
     */
    @Override
    public int getIconAddContact() {
        return R.drawable.ic_add_contact;
    }

    /**
     * Anhdts su kien chon vao them moi danh ba
     */
    @Override
    protected void addNewContact(Contact contactData) {
        /*Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        // Anhdts goi toi giao dien cua btalk
        intent.setPackage(getPackageName());

        intent.putExtra(ContactEditorActivity.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

        // Bkav QuangNDb set package name cho intent de ung dung khac khong bat duoc
        setPackageName(intent);
        ArrayList<ContentValues> values = contactData.getContentValues();

        // Only pre-fill the name field if the provided display name is an nickname
        // or better (e.g. structured name, nickname)
        if (contactData.getDisplayNameSource() >= ContactsContract.DisplayNameSources.NICKNAME) {
            intent.putExtra(ContactsContract.Intents.Insert.NAME, contactData.getDisplayName());
        } else if (contactData.getDisplayNameSource()
                == ContactsContract.DisplayNameSources.ORGANIZATION) {
            // This is probably an organization. Instead of copying the organization
            // name into a name entry, copy it into the organization entry. This
            // way we will still consider the contact an organization.
            final ContentValues organization = new ContentValues();
            organization.put(ContactsContract.CommonDataKinds.Organization.COMPANY, contactData.getDisplayName());
            organization.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
            values.add(organization);
        }

        // Last time used and times used are aggregated values from the usage stat
        // table. They need to be removed from data values so the SQL table can insert
        // properly
        for (ContentValues value : values) {
            value.remove(ContactsContract.Data.LAST_TIME_USED);
            value.remove(ContactsContract.Data.TIMES_USED);
        }
        intent.putExtra(ContactsContract.Intents.Insert.DATA, values);

        // If the contact can only export to the same account, add it to the intent.
        // Otherwise the ContactEditorFragment will show a dialog for selecting an
        // account.
        if (contactData.getDirectoryExportSupport() ==
                ContactsContract.Directory.EXPORT_SUPPORT_SAME_ACCOUNT_ONLY) {
            intent.putExtra(ContactsContract.Intents.Insert.EXTRA_ACCOUNT,
                    new Account(contactData.getDirectoryAccountName(),
                            contactData.getDirectoryAccountType()));
            intent.putExtra(ContactsContract.Intents.Insert.EXTRA_DATA_SET,
                    contactData.getRawContacts().get(0).getDataSet());
        }

        // Add this flag to disable the delete menu option on directory contact joins
        // with local contacts. The delete option is ambiguous when joining contacts.
        intent.putExtra(ContactEditorFragment.INTENT_EXTRA_DISABLE_DELETE_MENU_OPTION,
                true);

        mHasIntentLaunched = true;
        mContactLoader.cacheResult();
        // Bkav TienNAb: bo mau back ground de dong bo voi background khi them tu danh ba
//        MaterialColorMapUtils.MaterialPalette material = mHasComputedThemeColor
//                ? new MaterialColorMapUtils.MaterialPalette(mColorFilterColor, mStatusBarColor) : null;
//        if (material != null) {
//            intent.putExtra(ContactEditorBaseFragment.INTENT_EXTRA_MATERIAL_PALETTE_PRIMARY_COLOR,
//                    material.mPrimaryColor);
//            intent.putExtra(ContactEditorBaseFragment.INTENT_EXTRA_MATERIAL_PALETTE_SECONDARY_COLOR,
//                    material.mSecondaryColor);
//        }
        if (mContactData.getPhotoId() >= 0) {
            intent.putExtra(ContactEditorBaseFragment.INTENT_EXTRA_PHOTO_ID,
                    mContactData.getPhotoId());
        }

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.missing_app,
                    Toast.LENGTH_SHORT).show();
        }*/

        // Bkav HuyNQN su dung logic add contact nay, khong dung logic tren nua tam thoi dong lai.
        BtalkUIIntentsImpl.get().launchAddContactActivity(getApplicationContext(), contactData.getDisplayName());
    }

    @Override
    protected void visibleMenu() {
        String bkavConnectLookup = getLookupKeyBkavConnect();
        if (mContactData != null && bkavConnectLookup != null && bkavConnectLookup.equals(mContactData.getLookupKey())) {
            if (mEditMenuItem != null)
                mEditMenuItem.setVisible(false);
            if (mDeleteMenuItem != null)
                mDeleteMenuItem.setVisible(false);
        }
    }

    /**
     * Bkav QuangNDb ham get lookup key cua bkav cskh
     */
    protected String getLookupKeyBkavConnect() {
        final String mobileNumber = "1900545499";
        return getLookupKey(mobileNumber);
    }

    /**
     * Bkav QuangNDb get lookup key
     */
    protected String getLookupKey(String number) {
        if (number != null) {
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            String[] projection = {ContactsContract.PhoneLookup.LOOKUP_KEY};
            Cursor cur = getApplicationContext().getContentResolver().query(lookupUri, projection, null, null, null);
            try {
                if (cur != null && cur.moveToFirst()) {
                    return cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.LOOKUP_KEY));
                }
            } finally {
                if (cur != null)
                    cur.close();
            }
            return null;
        } else {
            return null;
        }
    }

    @Override
    protected void setFlagNoLimit() {
        // TrungTH
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    @Override
    protected void bkavStartInteractionLoaders(final Cp2DataCardModel cp2DataCardModel) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cp2DataCardModel != null) {
                    startInteractionLoaders(cp2DataCardModel);
                }
            }
        }, 500);
    }

    @Override
    protected int initLayout() {
        return R.layout.btalk_quickcontact_activity;
    }

    @Override
    protected Drawable getDrawableCall(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.btalk_ic_btn_call);
    }

    @Override
    protected int getResourceId(boolean isCall) {
        return isCall ? R.drawable.btalk_ic_btn_call : R.drawable.bkav_ic_message_recent_call;
    }

    @Override
    protected Drawable getDrawableMessage(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.bkav_ic_message_recent_call);
    }

    @Override
    protected Drawable getDrawableZalo(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.ic_zalo);
    }

    @Override
    protected Drawable getDrawableMessageZalo(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.ic_message_zalo);
    }

    @Override
    protected Drawable getDrawableVideoCallZalo(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.ic_video_call_zalo);
    }

    @Override
    protected Drawable getDrawableViber(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.ic_viber);
    }

    @Override
    protected Drawable getDrawableMessageViber(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.ic_message_viber);
    }

    @Override
    protected Drawable getDrawableWhatsApp(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.ic_whatsapp);
    }

    @Override
    protected Drawable getDrawableMessageWhatsApp(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.ic_message_whatsapp);
    }

    @Override
    protected Drawable getDrawableVideoCallWhatsApp(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.ic_video_call_whatsapp);
    }

    @Override
    protected Drawable getBackgroundSuggestionLinkButton() {
        return getDrawable(R.drawable.background_suggestion_link_button);
    }

    /**
     * Anhdts custom ham goi, chuyen thanh goi luon sim default
     */
    @Override
    protected boolean actionCall(boolean isDefault, Intent intent) {
        if (!Intent.ACTION_CALL.equals(intent.getAction())) {
            return false;
        }
        // neu la intent goi video thi re nhanh
        if (intent.hasExtra(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE)) {
            return false;
        }
        //Bkav QuangNDb doi logic thuc hien cuoc goi moi
        if (isDefault) {
            UIIntents.get().makeACall(mContext, getFragmentManager(), intent);
        } else {
            if (isShowListSim) {
                PhoneAccountHandle handleDefault = TelecomUtil.getDefaultOutgoingPhoneAccount(getApplicationContext(), PhoneAccount.SCHEME_TEL);
                int slotDefault = SimUltil.getSlotSimByAccount(getApplicationContext(), handleDefault);
                mDialogChooseSimFragment = DialogChooseSimFragment.newInstance(intent);
                mDialogChooseSimFragment.setSlotDefault(slotDefault);
                mDialogChooseSimFragment.show(getFragmentManager(), "chooseSim");
            } else {
                intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", SimUltil.getHandleNotDefaultSim(mContext));
                DialerUtils.startActivityWithErrorToast(mContext, intent);
            }
        }
        return true;
    }

    /**
     * Anhdts them menu goi bang sim khac
     */
    protected void addMenuCall(ContextMenu menu) {
        if (isShowListSim) {
            addMenuCallEsim(menu);
        } else {
            if (BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot()) {
                PhoneAccountHandle handle;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    handle = TelecomUtil.getDefaultOutgoingPhoneAccount(mContext,
                            PhoneAccount.SCHEME_TEL);
                    if (handle != null) {
                        int simDefault;
                        simDefault = SimUltil.getDefaultSimCell(mContext);
                        if (simDefault != -1) {
                            menu.add(ContextMenu.NONE, ContextMenuIds.CALL_OTHER_SIM,
                                    ContextMenu.NONE,
                                    mContext.getString(R.string.action_call_by_sim, String.valueOf(2 - simDefault),
                                            SimUltil.getNotDefaultSimName(mContext)));
                        }
                    }
                }
            }
        }
    }

    private void addMenuCallEsim(ContextMenu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            menu.add(ContextMenu.NONE, ContextMenuIds.CALL_OTHER_SIM,
                    ContextMenu.NONE, getString(R.string.action_call_by_sim_other));
        }
    }

    private boolean navigationBarIsVisible() {
        Boolean bool = false;

        Object windowService = getSystemService("window");
        Class<?> statusbarManager = null;
        try {
            statusbarManager = Class.forName("android.view.WindowManagerImpl");
            Method showsb;
            showsb = statusbarManager.getMethod("navigationBarIsInvisible");
            bool = (Boolean) showsb.invoke(windowService);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return !bool.booleanValue();
    }

    private int getHeightOfNavigationBar() {
        int navigationBarHeight = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return navigationBarHeight;
    }

    private void showFakeNavigationBackground() {
        if (navigationBarIsVisible()) {
            mScroller.fakeNavigationBar(getHeightOfNavigationBar());
        }
    }

    /**
     * Anhdts share tat ca thong tin danh ba
     */
    @Override
    protected String buildDataContact() {
        StringBuilder builder = new StringBuilder();
        builder.append(mDisplayName);
        for (List<ExpandingEntryCardView.Entry> entries : mCachedCp2DataCardModel.aboutCardEntries) {
            for (ExpandingEntryCardView.Entry entry : entries) {
                if (!TextUtils.isEmpty(entry.getHeader())) {
                    builder.append("\n+ ");
                    builder.append(entry.getHeader());
                    if (!TextUtils.isEmpty(entry.getSubHeader())) {
                        builder.append(" - ");
                        builder.append(entry.getSubHeader());
                    } else if (!TextUtils.isEmpty(entry.getText())) {
                        builder.append(" - ");
                        builder.append(entry.getText());
                    }
                }
            }
        }

        for (List<ExpandingEntryCardView.Entry> entries : mCachedCp2DataCardModel.contactCardEntries) {
            for (ExpandingEntryCardView.Entry entry : entries) {
                // Bkav TienNAb: Them dieu kien de loc cac thong tin zalo, viber ra
                if (!TextUtils.isEmpty(entry.getHeader()) && (!entry.getHeader().contains(ZALO)) && (!entry.getHeader().contains(VIBER))) {
                    builder.append("\n+ ");
                    builder.append(entry.getHeader());
                    if (!TextUtils.isEmpty(entry.getSubHeader())) {
                        builder.append(" - ");
                        builder.append(entry.getSubHeader());
                    } else if (!TextUtils.isEmpty(entry.getText())) {
                        builder.append(" - ");
                        builder.append(entry.getText());
                    }
                }
            }
        }

        return builder.toString();
    }
}
