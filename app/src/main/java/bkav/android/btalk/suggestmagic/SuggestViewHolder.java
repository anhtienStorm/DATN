package bkav.android.btalk.suggestmagic;

import android.Manifest;
import android.accounts.Account;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneNumberUtils;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.CallUtil;
import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.GeoUtil;
import com.android.contacts.common.compat.ContactsCompat;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.common.util.UriUtils;
import com.android.dialer.calllog.PhoneAccountUtils;
import com.android.dialer.compat.DialerCompatUtils;
import com.android.dialer.database.DialerDatabaseHelper;
import com.android.dialer.dialpad.SmartDialMatchPosition;
import com.android.dialer.dialpad.SmartDialNameMatcher;
import com.android.dialer.dialpad.SmartDialPrefix;
import com.android.dialer.util.DialerUtils;
import com.android.messaging.Factory;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.contacts.BtalkQuickContactActivity;
import bkav.android.btalk.esim.ISimProfile;
import bkav.android.btalk.esim.dialog_choose_sim.DialogChooseSimFragment;
import bkav.android.btalk.esim.provider.ESimDbController;
import bkav.android.btalk.fragments.BtalkPhoneFragment;
import bkav.android.btalk.mutilsim.SimUltil;
import bkav.android.btalk.utility.Clipboard;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by anhdt on 19/05/2017.
 * View hien thi so danh ba dau tien trong list search
 */

public class SuggestViewHolder {

    private View mContainerSuggest;

    private View mContainerExtraSuggest;

    private Context mContext;

    private SmartDialNameMatcher mMatcher;

    private ImageView mPhotoView;

    private TextView mNameView;

    private TextView mPhoneView;

    private TextView mNameExtraView;

    private TextView mPhoneExtraView;

    private BtalkPhoneFragment mFragment;

    private ImageView mShowMoreButton;

    private SpannableString mNameSpan;
    // Bkav HienDTk: lay sim mac dinh
    private int mSimDefault;
    // Bkav HienDTk: check xem co dang dung esim hay khong
    private boolean mIsCheckEsim;
    // Bkav HienDTk: lay list profile
    private List<ISimProfile> mListEsim;

    // Bkav HienDTk: item menu dialog chon sim
    private static int ITEM_MENU_DIALOG_CHOOSE_SIM = 0;
    // Bkav HienDTk: profile cua sim (ca sim thuong va esim)
    private static int PROFILE_SIM = 3;
    // Bkav HienDTk: tra ve so sim dang duoc lap tren khay sim
    private static int SUBSCRIPTION_PHONE_ACCOUNT = 2;


    private String mNumber;

    private List<PhoneAccountHandle> subscriptionAccountHandles;

    public SuggestViewHolder(View parentView, final BtalkPhoneFragment fragment, View.OnClickListener listener) {
        mFragment = fragment;
        mContext = fragment.getActivity();
        mDetector = new GestureDetectorCompat(mContext, new GestureListener());
        mContainerSuggest = parentView.findViewById(R.id.container_suggest);
        mContainerSuggest.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetector.onTouchEvent(event);
                return false;
            }
        });

        mContainerSuggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFragment != null && mPhoneView.getVisibility() == View.VISIBLE && !TextUtils.isEmpty(mPhoneView.getText())) {
                    mFragment.actionCallSuggest();
                }
            }
        });

        mContainerSuggest.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mIsCheckEsim = ESimDbController.isEsimExist();
                mListEsim = ESimDbController.getAllSim();

                if (TextUtils.isEmpty(mFragment.getDialpadFragment().getDigitsWidget().getText())) {
                    String temp = Clipboard.get().getStringClipboard(mContext);
                    if (!TextUtils.isEmpty(temp)) {
                        mFragment.getDialpadFragment().getDigitsWidget().setCursorVisible(false);
                        mFragment.getDialpadFragment().setPasteFromClipboard(true);
                        mFragment.getDialpadFragment().getDigitsWidget().setText(temp);
                        mFragment.getDialpadFragment().getDigitsWidget().setSelection(temp.length());
                    }
                    return true;
                } else {
                    final String number = String.valueOf(mPhoneView.getText());
                    mNumber = number;
                    if (!TextUtils.isEmpty(number)) {
                        PopupMenu popup = new PopupMenu(mContext, v, Gravity.END);
                        popup.inflate(R.menu.menu_view_holder);
                        Menu menu = popup.getMenu();
                        mSimDefault = SimUltil.getDefaultSimCell(mContext);
                        subscriptionAccountHandles =
                                PhoneAccountUtils.getSubscriptionPhoneAccounts(mContext);
                        // Bkav HienDTk: truong hop lap 1 sim
                        if (subscriptionAccountHandles != null && subscriptionAccountHandles.size() <= 1 && !mIsCheckEsim) {
                            menu.removeItem(R.id.action_call_by_sim_other);
                        } else {

                            if (mSimDefault != -1) {
                                // Bkav HienDTk: truong hop lap sim co nhieu profile
                                if (mIsCheckEsim && subscriptionAccountHandles.size() == SUBSCRIPTION_PHONE_ACCOUNT && mListEsim.size() >= PROFILE_SIM) {
                                    menu.getItem(ITEM_MENU_DIALOG_CHOOSE_SIM).setTitle(mContext.getString(R.string.action_call_by_sim_other));
                                } else if (mIsCheckEsim && mListEsim.size() >= 2) { // Bkav HienDTk: truong hop lap esim co nhieu profile thi hien thi dialog chon sim
                                    menu.getItem(ITEM_MENU_DIALOG_CHOOSE_SIM).setTitle(mContext.getString(R.string.action_call_by_sim_other));
                                } else {
                                    menu.getItem(ITEM_MENU_DIALOG_CHOOSE_SIM).setTitle(mContext.getString(R.string.action_call_by_sim, String.valueOf(2 - mSimDefault),
                                            SimUltil.getNotDefaultSimName(mContext)));
                                }

                            } else {
                                menu.getItem(ITEM_MENU_DIALOG_CHOOSE_SIM).setTitle(mContext.getString(R.string.action_call_by_sim_other));
                            }
                        }
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getItemId() == R.id.action_copy) {
                                    ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText(String.valueOf(mNameView.getText()), number);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(mContext, R.string.copy_done_suggest, Toast.LENGTH_SHORT).show();
                                }
                                if (item.getItemId() == R.id.action_share) {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_SEND);
                                    intent.putExtra(Intent.EXTRA_TEXT, getContentForward(number, String.valueOf(mNameView.getText())));
                                    intent.setType("text/plain");
                                    Intent chooser = Intent.createChooser(
                                            intent, mContext.getString(R.string.action_share_contact_via));
                                    DialerUtils.startActivityWithErrorToast(mContext, chooser);
                                } else if (item.getItemId() == R.id.action_call_by_sim_other) { //HienDTk: them item chon sim
                                    if (mSimDefault != -1) {
                                        // Bkav HienDTk: truong hop lap ca sim thuong va e sim co nhieu profile
                                        if (mIsCheckEsim && subscriptionAccountHandles.size() == SUBSCRIPTION_PHONE_ACCOUNT && mListEsim.size() >= PROFILE_SIM) {
                                            mFragment.showDialogChooseSim(number);
                                        } else if (mIsCheckEsim && mListEsim.size() >= 2) { // Bkav HienDTk: Truong hop chi lap esim co nhieu profile
                                            mFragment.showDialogChooseSim(number);
                                        } else {
                                            makeCall();
                                        }
                                    } else {
                                        mFragment.showDialogChooseSim(number);
                                    }
                                }
                                return false;
                            }
                        });
                        popup.show();
                    }
                }
                return true;
            }
        });

        mContainerExtraSuggest = parentView.findViewById(R.id.extra_number_container);
        mContainerExtraSuggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = String.valueOf(mPhoneExtraView.getContentDescription());
                if (number != null && !number.isEmpty()) {
                    final Intent intent = CallUtil.getCallIntent(number);
                    DialerUtils.startActivityWithErrorToast(mContext, intent);
                    mFragment.getDialpadFragment().hideAndClearDialpad(false);
                }
            }
        });

        mNameExtraView = (TextView) mContainerExtraSuggest.findViewById(R.id.suggested_extra_contact_name);
        mPhoneExtraView = (TextView) mContainerExtraSuggest.findViewById(R.id.suggested_extra_contact_phone);

        mMatcher = new SmartDialNameMatcher("", SmartDialPrefix.getMap(), mContext);

        mPhotoView = (ImageView) mContainerSuggest.findViewById(R.id.suggested_contact_photo);
        mNameView = (TextView) mContainerSuggest.findViewById(R.id.suggested_contact_name);
        mPhoneView = (TextView) mContainerSuggest.findViewById(R.id.suggested_contact_phone);
        mShowMoreButton = (ImageView) mContainerSuggest.findViewById(R.id.show_more_contact);
        mShowMoreButton.setOnClickListener(listener);
    }

    public boolean hasSuggest() {
        return isVisibility() && mPhoneView.isShown();
    }

    public boolean hasShowMore() {
        return mShowMoreButton.isShown();
    }

    /**
     * Anhdts bind view contact dau tien
     */
    public void bindMainSuggestView(DialerDatabaseHelper.ContactNumber data, boolean isShowMore) {
        if (isShowMore) {
            mShowMoreButton.setVisibility(View.VISIBLE);
        } else {
            mShowMoreButton.setVisibility(View.INVISIBLE);
        }
        mNameView.setVisibility(View.VISIBLE);

        String name = data.displayName;

        mMatcher.setQuery(PhoneNumberUtils.normalizeNumber(String.valueOf(mFragment.getDialpadFragment().getDigitsWidget().getText())));

        if (name.startsWith("?DATE:")) {
            mNameView.setContentDescription(name);
            mNameView.setVisibility(View.GONE);
        } else {
            ArrayList<HighlightSequence> nameHighlights = new ArrayList<>();

            if (mMatcher.matches(name)) {
                final ArrayList<SmartDialMatchPosition> nameMatches = mMatcher.getMatchPositions();
                for (SmartDialMatchPosition match : nameMatches) {
                    nameHighlights.add(new HighlightSequence(match.start, match.end));
                }
            }

            CharSequence formattedName = name;
            // set name view
            if (nameHighlights.size() != 0) {
                final SpannableString spannableName = new SpannableString(formattedName);
                for (HighlightSequence highlightSequence : nameHighlights) {
                    applyMaskingHighlight(spannableName, highlightSequence.start,
                            highlightSequence.end);
                }
                formattedName = spannableName;
            }
            mNameSpan = new SpannableString(formattedName);
            setMarqueeText(mNameView, mNameSpan);
            mNameView.setContentDescription(data.displayName);
        }

        String number = data.phoneNumber;
        HighlightSequence numberHighlight = null;
        final SmartDialMatchPosition numberMatch = mMatcher.matchesNumber(number);
        if (numberMatch != null) {
            numberHighlight = new HighlightSequence(numberMatch.start, numberMatch.end);
        }

        // set phone view
        if (number.isEmpty()) {
            mPhoneView.setText("");
            mPhoneView.setVisibility(View.INVISIBLE);
            return;
        } else {
            mPhoneView.setVisibility(View.VISIBLE);
        }
        final SpannableString textToSet = new SpannableString(number);

        if (numberHighlight != null) {
            applyMaskingHighlightNumber(textToSet, numberHighlight.start,
                    numberHighlight.end);
        }
        setMarqueeText(mPhoneView, textToSet);
        mPhoneView.setContentDescription(number);

        bindPhotoView(data);

    }

//    private static final String[] PHONE_PROJECTION = new String[]{
//            ContactsContract.CommonDataKinds.Phone.NUMBER,              // 0
//            ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,          // 1
//    };

    public void bindSecondarySuggestView(DialerDatabaseHelper.ContactNumber data) {
//        String numberExtra = "";
//        final Cursor cursor = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONE_PROJECTION,
//                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + data.id, null, null);
//        if (cursor.moveToFirst()) {
//            do {
//                String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                Integer isPrimary = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)));
//                if (isPrimary > 0 && !phone.equals(numberFirst)) {
//                    numberExtra = phone;
//                    break;
//                }
//                if (TextUtils.isEmpty(numberExtra) && !phone.equals(numberFirst)) {
//                    numberExtra = phone;
//                }
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        if (TextUtils.isEmpty(numberExtra)) {
//            if (mContainerExtraSuggest.isShown()) {
//                mContainerExtraSuggest.setVisibility(View.GONE);
//            }
//            return;
//        }
        mContainerExtraSuggest.setVisibility(View.VISIBLE);

        setMarqueeText(mNameExtraView, mNameSpan);

        // phone
        final SmartDialMatchPosition numberMatch = mMatcher.matchesNumber(data.phoneNumber);
        HighlightSequence numberHighlight = null;
        if (numberMatch != null) {
            numberHighlight = new HighlightSequence(numberMatch.start, numberMatch.end);
        }
        // set phone view
        mPhoneView.setVisibility(View.VISIBLE);
        final SpannableString textToSet = new SpannableString(data.phoneNumber);

        if (numberHighlight != null) {
            applyMaskingHighlightNumber(textToSet, numberHighlight.start,
                    numberHighlight.end);
        }
        setMarqueeText(mPhoneExtraView, textToSet);
        mPhoneExtraView.setContentDescription(data.phoneNumber);

    }

    /**
     * Anhdts
     *
     * @param data cursor
     *             bind anh len
     */
    private void bindPhotoView(final DialerDatabaseHelper.ContactNumber data) {
        mPhotoView.setVisibility(View.VISIBLE);
        long photoId = data.photoId;
        Account account = null;
        if (!BtalkActivity.isAndroidQ()) {
            if (data.accountType != null
                    && data.accountName != null) {
                final String accountType = data.accountType;
                final String accountName = data.accountName;
                account = new Account(accountName, accountType);
            }
        }
        if (photoId != 0) {
            ContactPhotoManager.getInstance(mContext).loadThumbnail(mPhotoView, photoId, account, false,
                    true, null);
        } else {
            Uri URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI.buildUpon().
                    appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY,
                            String.valueOf(ContactsContract.Directory.DEFAULT)).
                    build();
            Cursor cursor = mContext.getContentResolver().query(URI, new String[]{ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI}
                    , ContactsContract.CommonDataKinds.Phone.PHOTO_ID + " = " + photoId, null, null);
            String photoUriString = null;
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    photoUriString = cursor.getString(0);
                }
                cursor.close();
            }
            final Uri photoUri = photoUriString == null ? null : Uri.parse(photoUriString);
            ContactPhotoManager.DefaultImageRequest request = null;
            if (photoUri == null) {
                final String displayName = data.displayName;
                final String lookupKey = data.lookupKey;
                request = new ContactPhotoManager.DefaultImageRequest(displayName, lookupKey, true);
            }
            ContactPhotoManager.getInstance(mContext).loadDirectoryPhoto(mPhotoView, photoUri, account, false,
                    true, request);
        }
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContactsCompat.isEnterpriseContactId(data.id)) {
                    String lookupKey = data.lookupUri.getLastPathSegment();
                    if (data.phoneNumber.equals(lookupKey)) {
                        String numberE164 = DialerCompatUtils.formatNumberE164(data.phoneNumber, GeoUtil.getCurrentCountryIso(Factory.get().getApplicationContext()));
                        Cursor callCursor;
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        callCursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                                new String[]{CallLog.Calls.NUMBER,
                                        CallLog.Calls.CACHED_NORMALIZED_NUMBER,
                                        CallLog.Calls.CACHED_LOOKUP_URI},
                                CallLog.Calls.CACHED_NORMALIZED_NUMBER + " LIKE ?",
                                new String[]{numberE164},
                                CallLog.Calls.DEFAULT_SORT_ORDER);
                        if (callCursor == null) {
                            return;
                        }
                        if (callCursor.getCount() > 0) {
                            callCursor.moveToFirst();
                            Uri lookupUri = UriUtils.parseUriOrNull(callCursor.getString(2));
                            if (lookupUri != null) {
                                BtalkDialerDatabaseHelper.getInstance(mContext).updateLookupUri(lookupKey, String.valueOf(lookupUri));
                                final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(lookupUri
                                        , BtalkQuickContactActivity.MODE_FULLY_EXPANDED);
                                intent.setPackage(mContext.getPackageName());
                                intent.putExtra(BtalkQuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE,
                                        com.android.contacts.common.logging.ScreenEvent.ScreenType.ALL_CONTACTS);
                                try {
                                    ImplicitIntentsUtil.startActivityInApp(mContext, intent);
                                } catch (Exception ignored) {
                                }
                            }
                        }
                        callCursor.close();
                    } else {
                        // No implicit intent as user may have a different contacts app in work profile.
                        // No implicit intent as user may have a different contacts app in work profile.
                        ContactsContract.QuickContact.showQuickContact(mContext, new Rect(), data.lookupUri,
                                BtalkQuickContactActivity.MODE_FULLY_EXPANDED, null);
                    }
                } else {
                    Uri lookupUri;
                    if (data.lookupUri == null) {
                        lookupUri = ContactsContract.Contacts.getLookupUri(data.id, data.lookupKey);
                    } else {
                        lookupUri = data.lookupUri;
                    }
                    final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(lookupUri
                            , BtalkQuickContactActivity.MODE_FULLY_EXPANDED);
                    intent.setPackage(mContext.getPackageName());
                    intent.putExtra(BtalkQuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE,
                            com.android.contacts.common.logging.ScreenEvent.ScreenType.ALL_CONTACTS);
                    try {
                        ImplicitIntentsUtil.startActivityInApp(mContext, intent);
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    public CharSequence getContentDescription() {
        TextView viewNumber = (TextView) mContainerSuggest.findViewById(R.id.suggested_contact_phone);
        return viewNumber.getContentDescription();
    }

    public void setButtonShowMoreDown(boolean buttonShowMoreDown) {
        mShowMoreButton.setImageDrawable(ContextCompat.getDrawable(mContext, buttonShowMoreDown ?
                R.drawable.ic_show_list_contact : R.drawable.ic_hide_list_contact));
    }

    public String getDisplayName() {
        return (String) mNameView.getContentDescription();
    }

    public boolean isNumberCallLog() {
        return !mNameView.isShown();
    }

    private static class HighlightSequence {
        private final int start;

        private final int end;

        HighlightSequence(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private void setMarqueeText(TextView textView, SpannableString spannable) {
        if (spannable == null) {
            return;
        }
        spannable.setSpan(TextUtils.TruncateAt.MARQUEE, 0, spannable.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannable);
    }

    public void cleanSuggestView() {
        if (mFragment.getDialpadFragment().getDigitsWidget() == null) {
            return;
        }
        String newText = String.valueOf(mFragment.getDialpadFragment().getDigitsWidget().getText());
        mShowMoreButton.setVisibility(View.INVISIBLE);
        mContainerExtraSuggest.setVisibility(View.GONE);
        if (newText.isEmpty()) {
            mPhoneView.setText("");
            mNameView.setText("");
            mPhoneView.setVisibility(View.INVISIBLE);
            mPhotoView.setVisibility(View.INVISIBLE);
        } else {
            mPhoneView.setText("");
            mNameView.setText("");
            mPhoneView.setVisibility(View.INVISIBLE);
            mNameView.setVisibility(View.INVISIBLE);
            mPhotoView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Anhdts xoa view goi y thu 2
     */
    public void cleanSecondarySuggestView() {
        mContainerExtraSuggest.setVisibility(View.GONE);
    }

    /**
     * Anhdts doi mau highlight
     * Applies highlight span to the text.
     *
     * @param text  Text sequence to be highlighted.
     * @param start Start position of the highlight sequence.
     * @param end   End position of the highlight sequence.
     */

    private void applyMaskingHighlight(SpannableString text, int start, int end) {
        text.setSpan(new ForegroundColorSpan(0xFFFF7E38), start, end, 0);
//        text.setSpan(new ForegroundColorSpan(0xFF7E7E7E), text.length() - 1, text.length(), 0);
    }

    /**
     * Anhdts
     */
    private void applyMaskingHighlightNumber(SpannableString text, int start, int end) {
        text.setSpan(new ForegroundColorSpan(0xFFFF7E38), start, end, 0);
    }

    // TrungTH them xu ly vuot de show danh sach
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (distanceY < 0) {
                //TrungTH neu vuot xuong thi show list goi y ra, => truyen state touch scrool de ben kia so sanh ok luon
                if (mFragment != null) {
                    mFragment.onListFragmentScrollStateChange(AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    private GestureDetectorCompat mDetector;

    /**
     * TrungTH them ham check view phone hien hay khong
     */
    public boolean isVisibility() {
        return mPhoneView != null && mPhoneView.getVisibility() == View.VISIBLE;
    }

    /**
     * Anhdts noi dung chuyen danh ba
     */
    public static String getContentForward(String number, String name) {

        if (TextUtils.isEmpty(number)) {
            return "";
        }

        if (TextUtils.isEmpty(name)) {
            return number;
        }

        StringBuilder phoneNumber = new StringBuilder(name);
        if (!number.equals(name)) {
            phoneNumber.append(" <");
            phoneNumber.append(number);
            phoneNumber.append(">");
        }
        return phoneNumber.toString();
    }
    // Bkav HienDTk: lay slot sim mac dinh
    private static final int SLOT_INDEX_SIM_1 = 0;
    private static final int SLOT_INDEX_SIM_2 = 1;

    // Bkav HienDTk: thuc hien cuoc goi
    public void makeCall() {
        String callId = "";
        if (BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot()) {
            if (mSimDefault == SLOT_INDEX_SIM_1) {
                // Bkav HienDTk: neu mac dinh la sim 1 thi lay id sim 2 de thuc hien cuoc goi
                callId = subscriptionAccountHandles.get(SLOT_INDEX_SIM_2).getId();
            } else if (mSimDefault == SLOT_INDEX_SIM_2) {
                // Bkav HienDTk: neu mac dinh la sim 2 thi lay id sim 1 de thuc hien cuoc goi
                callId = subscriptionAccountHandles.get(SLOT_INDEX_SIM_1).getId();
            }

        } else {
            callId = subscriptionAccountHandles.get(SLOT_INDEX_SIM_1).getId();
        }

        SimUltil.callWithSlotSim(mContext, callId, mNumber);
    }
}
