package bkav.android.btalk.suggestmagic;

import android.accounts.Account;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.compat.ContactsCompat;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.dialer.database.DialerDatabaseHelper;
import com.android.dialer.dialpad.LatinSmartDialMap;
import com.android.dialer.dialpad.SmartDialMatchPosition;
import com.android.dialer.dialpad.SmartDialNameMatcher;
import com.android.dialer.dialpad.SmartDialPrefix;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.IntentUtil;
import com.android.dialer.util.TelecomUtil;
import com.android.messaging.Factory;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.PhoneUtils;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.BtalkDialogChooseMutilNumber;
import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.contacts.BtalkQuickContactActivity;
import bkav.android.btalk.esim.ISimProfile;
import bkav.android.btalk.esim.provider.ESimDbController;
import bkav.android.btalk.messaging.BtalkFactoryImpl;
import bkav.android.btalk.mutilsim.SimUltil;
import bkav.android.btalk.utility.ContactUtils;
import bkav.android.btalk.view.BtalkAutoResizeTextView;

/**
 * Created by anhdt on 31/10/2017.
 *
 */
public class SuggestPopup implements View.OnClickListener, BtalkDialogChooseMutilNumber.ChooseNumberListener, View.OnLongClickListener {

    private Activity mContext;

    private PopupWindow mPopup;

    private SmartDialNameMatcher mMatcher;

    private ImageView mPhotoView;

    private BtalkAutoResizeTextView mNameView;

    private TextView mPhoneView;

    private LatinSmartDialMap mMap;

    private DialerDatabaseHelper.ContactNumber mContactSuggest;

    private ImageView mViewShowMore;

    private boolean mShowNumberMode = false;

    private boolean mMutilNumber = false;

    private ImageCallButton mButtonCall;

    // Bkav TienNAb: Tao them icon cuoc goi cho sim 2
    private ImageCallButton mButtonCallSim2;

    private boolean mIsInteract = false;

    private static final String PHONE_NUMBER_SELECTION =
            ContactsContract.Data.MIMETYPE + " IN ('"
                    + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "', "
                    + "'" + ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE + "') AND "
                    + ContactsContract.Data.DATA1 + " NOT NULL";

    private boolean mIsMessageMode = false;

    private final static int TIME_COUNT = 1000;
    private final static int TIME_STICK = 50;
    private String mQueryOld = "";
    private String mDataIdContact = "";
    private String mNameContact = "";
    private String mPhoneContact = "";
    private long mIdContactSuggest = 0;

    /*
    * sau 1 giay khi nguoi dung ko query nua moi thuc hien kiem tra va gui log
    * */
    private CountDownTimer mCountDownTimer = new CountDownTimer(TIME_COUNT,TIME_STICK) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            // Bkav HuyNQN kiem tra contact tren suggest co trong danh ba hay khong, thuc hien tren ban Alpha
            if(mListener != null && ContactUtils.checkAlphaOTAChannel(mContext)){
                mListener.checkDataContact(mDataIdContact, mIdContactSuggest, mNameContact, mPhoneContact);
            }
        }
    };

    private Handler mQueryHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String query = msg.obj.toString();
            if (!mQueryOld.equals(query)) {
                mCountDownTimer.cancel();
                mQueryOld = query;
                mCountDownTimer.start();
            }
            return false;
        }
    });

    // Bkav TienNAb: danh sach sim co thuc hien cuoc goi
    ArrayList<String> mPhoneAccountIdList = new ArrayList<>();

    SuggestPopup(Activity context) {
        mContext = context;
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.suggest_contact, null);
        mPopup = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mPopup.setFocusable(false);
        mPopup.setOutsideTouchable(false);
        mPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NEEDED);
        mPopup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mPhotoView = (ImageView) popupView.findViewById(R.id.suggested_contact_photo);
        mNameView = (BtalkAutoResizeTextView) popupView.findViewById(R.id.suggested_contact_name);

        mPhoneView = (TextView) popupView.findViewById(R.id.suggested_contact_phone);
        mViewShowMore = (ImageView) popupView.findViewById(R.id.suggested_show_more_number);
        mButtonCall = (ImageCallButton) popupView.findViewById(R.id.action_call);
        mButtonCallSim2 = (ImageCallButton) popupView.findViewById(R.id.action_call_sim2);
        mButtonCallSim2.setIsPopup(true);
        mButtonCall.setIsPopup(true);

        popupView.findViewById(R.id.action_send_message).setOnClickListener(this);
        mButtonCall.setOnClickListener(this);
        mButtonCallSim2.setOnClickListener(this);
        mButtonCall.setOnLongClickListener(this);
        mButtonCallSim2.setOnLongClickListener(this);

        updateSim();
        mPopup.getContentView().findViewById(R.id.content_view).setOnClickListener(this);
        //Bkav QuangNDb phan vung touchable popup window
        mPopup.setTouchInterceptor(new PopupTouchInterceptor());
        mMatcher = new SmartDialNameMatcher("", SmartDialPrefix.getMap(), mContext);
        mMap = new LatinSmartDialMap();
    }


    //Bkav QuangNDb phan vung touchable o popup window
    private class PopupTouchInterceptor implements View.OnTouchListener {

        private boolean mIsMoveAction;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float locationY = event.getY();
            int contentHeight = mPopup.getContentView().findViewById(R.id.content_view).getHeight();
            int expandedViewHeight = 0;


            if (mContext != null) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mIsInteract = locationY < expandedViewHeight
                            || locationY > contentHeight + expandedViewHeight;

                    mIsMoveAction = false;
                    if (mIsInteract) {
                        mPopup.getContentView().setVisibility(View.INVISIBLE);
                    }

                }
                if (mIsInteract) {
                    Rect rect1 = new Rect();
                    Rect rect2 = new Rect();
                    mPopup.getContentView().getWindowVisibleDisplayFrame(rect1);
                    mPopup.getContentView().getLocalVisibleRect(rect2);
                    event.setLocation(event.getX(), rect1.bottom - rect2.bottom + event.getY());

                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (!mIsMoveAction) {
                            MotionEvent downEvent = MotionEvent.obtain(event);
                            downEvent.setAction(MotionEvent.ACTION_DOWN);
                            mContext.dispatchTouchEvent(downEvent);
                        }
                        hide();
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        mIsMoveAction = true;
                    }
                    mContext.dispatchTouchEvent(event);
                    event.setLocation(event.getX(), locationY);
                }
            }
            return false;
        }
    }

    public static Bitmap convertDrawableToBitmap(Drawable drawable, float sizeIcon) {
        Bitmap bitmap;

        Drawable daDrawableMutate = drawable.getConstantState().newDrawable();

        if (drawable.getIntrinsicWidth() <= 0 || daDrawableMutate.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap((int) sizeIcon,
                    (int) (sizeIcon * daDrawableMutate.getIntrinsicHeight() / daDrawableMutate.getIntrinsicWidth()),
                    Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        daDrawableMutate.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        daDrawableMutate.draw(canvas);
        return bitmap;
    }

    void setMessageMode(int paddingBottom) {
        mIsMessageMode = true;
        mPopup.getContentView().findViewById(R.id.action_send_message).setVisibility(View.GONE);
        mPopup.getContentView().findViewById(R.id.action_call).setVisibility(View.GONE);
        mPopup.getContentView().setPadding(0, 0, 0, paddingBottom);
    }

    @Override
    public boolean onLongClick(View v) {
        String number = String.valueOf(mContactSuggest.phoneNumber);
        PhoneAccountHandle handle = TelecomUtil.getDefaultOutgoingPhoneAccount(mContext, PhoneAccount.SCHEME_TEL);

        if(BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot() && handle != null) {
            switch (v.getId()) {
                case R.id.action_call: // Bkav HuyNQN day la goi bang sim mac dinh
                    makeACallLongClick(number);
                    break;
                case R.id.action_call_sim2: // Bkav HuyNQN goi bang sim khong mac dinh
                    makeACallLongClick(number);
                    break;
            }
        }else {
            if (v.getId() == R.id.action_call) {
                makeACallLongClick(number);
            }
        }

        if (mListener != null) {
            mListener.onClick(mContactSuggest);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        String number = String.valueOf(mContactSuggest.phoneNumber);
        switch (v.getId()) {
            case R.id.action_send_message:
                //Bkav ToanNTe fix Danh bạ - BOS 8.7 - Lỗi: Không tự động bật bàn phím chữ khi mở giao diện Soạn tin nhắn từ liên hệ đề xuất trong Danh bạ
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mListener.onClickMessage();
                }
                final Intent intent = IntentUtil.getSendSmsIntent(mContactSuggest.phoneNumber);
                ((BtalkFactoryImpl) Factory.get()).registerSendMessage();
                DialerUtils.startActivityWithErrorToast(mContext, intent);

                break;
            case R.id.action_call:
                makeACall(number);
                break;
                // Bkav TienNAb: xu ly su kien khi click vao icon cuoc goi sim 2
            case R.id.action_call_sim2:
                if (number != null && !number.isEmpty()) {
                    SimUltil.callWithSimMode(mContext, false, number);
                }
                break;
            case R.id.content_view:
                if (!mIsMessageMode) {
                    if (mMutilNumber) {
                        FragmentTransaction ft = mContext.getFragmentManager().beginTransaction();
                        BtalkDialogChooseMutilNumber dialogChooseMutilNumber = new BtalkDialogChooseMutilNumber();
                        dialogChooseMutilNumber.setListener(this, v.getId());
                        dialogChooseMutilNumber.startInteraction(mContext, mContactSuggest.lookupUri);
                        dialogChooseMutilNumber.show(ft, BtalkDialogChooseMutilNumber.DIALOG_TAG);
                    } else {
                        makeACall(number);
                    }
                }
                break;
        }
        if (mListener != null) {
            mListener.onClick(mContactSuggest);
        }
    }

    @Override
    public void onClick(String number, int mActionId, Activity activity) {
        switch (mActionId) {
            case R.id.action_send_message:
                final Intent intent = IntentUtil.getSendSmsIntent(number);
                ((BtalkFactoryImpl) Factory.get()).registerSendMessage();
                DialerUtils.startActivityWithErrorToast(mContext, intent);
                break;
            case R.id.action_call:
            case R.id.content_view:
                makeACall(number);
                break;
        }
    }
    //Bkav QuangNDb tao ham tranh lap code
    private void makeACall(String number) {
        if (number != null && !number.isEmpty()) {
            UIIntents.get().makeACall(mContext, mContext.getFragmentManager(), number);
        }
    }

    // Bkav HuyNQN
    private void makeACallLongClick(String number){
        if (number != null && !number.isEmpty()) {
            UIIntents.get().showListEsimLongClick(mContext, mContext.getFragmentManager(), number);
        }
    }

    void showAsDropDown(View anchor) {
        mPopup.getContentView().setVisibility(View.VISIBLE);
        mPopup.setAnimationStyle(R.anim.animation_popup_suggest);
        mPopup.showAtLocation(anchor, Gravity.BOTTOM, 0, 0);
    }

    public void hide() {
        mIsInteract = false;
        mPopup.dismiss();
    }

    // Bkav TienNAb: lay ra danh sach cac sim co thuc hien cuoc goi
    private void getListAccountHandleMakeCall(Context context){
        mPhoneAccountIdList.clear();
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                null, null, null);
        int phoneAccountIdColumn = cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID);
        while (cursor.moveToNext()){
            String id = cursor.getString(phoneAccountIdColumn);
            if (!mPhoneAccountIdList.contains(id)){
                mPhoneAccountIdList.add(id);
            }
        }
    }

    public void setData(DialerDatabaseHelper.ContactNumber data, String query) {
        mContactSuggest = data;

        getListAccountHandleMakeCall(mContext);
        // Bkav TienNAb: lay ra tai khoan sim khong phai la sim mac dinh
        PhoneAccountHandle handleNotDefault = SimUltil.getHandleNotDefaultSim(mContext);
        // Bkav TienNAb: neu khong phai sim mac dinh ma khong thuc hien cuoc goi thi khong hien thi nut goi nua
        if (handleNotDefault != null){
            if (mPhoneAccountIdList.contains(handleNotDefault.getId())){
                mButtonCallSim2.setVisibility(View.VISIBLE);
            } else {
                mButtonCallSim2.setVisibility(View.GONE);
            }
        }

        String name = data.displayName;
        mNameView.setVisibility(View.VISIBLE);
        mMatcher.setQuery(query);

        if (name.startsWith("?DATE:")) {
            mNameView.setContentDescription(name);
            mNameView.setVisibility(View.GONE);
        } else {
            ArrayList<HighlightSequence> nameHighlights = new ArrayList<>();

            ArrayList<MatchPair> matches = getMatchName(name, query);
            if (matches.size() > 0) {
                for (MatchPair match : matches) {
                    nameHighlights.add(new HighlightSequence(match.start, match.end));
                }
            }

            CharSequence formattedName = name;
            mNameView.setVisibility(View.VISIBLE);
            // set name view
            if (nameHighlights.size() != 0) {
                final SpannableString spannableName = new SpannableString(formattedName);
                for (HighlightSequence highlightSequence : nameHighlights) {
                    applyMaskingHighlight(spannableName, highlightSequence.start,
                            highlightSequence.end);
                }
                formattedName = spannableName;
            }
            SpannableString nameSpan = new SpannableString(formattedName);
            setMarqueeText(mNameView, nameSpan);
            mNameView.setContentDescription(data.displayName);
        }

        mMutilNumber = false;

        boolean isNumeric = query.matches("-?\\d+(\\.\\d+)?");
        if (isNumeric) {
            if (!data.displayName.contains(query)) {
                mShowNumberMode = true;
            }
        }

        if (!mShowNumberMode) {
            if (!data.lookupKey.startsWith("content")) {
                Uri lookupUri = ContentUris.withAppendedId(
                        Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, data.lookupKey), data.id);
                data.lookupUri = lookupUri;
                final Uri queryUri;
                final String inputUriAsString = lookupUri.toString();
                if (inputUriAsString.startsWith(ContactsContract.Contacts.CONTENT_URI.toString())) {
                    if (!inputUriAsString.endsWith(ContactsContract.Contacts.Data.CONTENT_DIRECTORY)) {
                        queryUri = Uri.withAppendedPath(lookupUri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
                    } else {
                        queryUri = lookupUri;
                    }
                } else if (inputUriAsString.startsWith(ContactsContract.Data.CONTENT_URI.toString())) {
                    queryUri = lookupUri;
                } else {
                    throw new UnsupportedOperationException(
                            "Input Uri must be contact Uri or data Uri (input: \"" + lookupUri + "\")");
                }

                Cursor cursor = mContext.getContentResolver().query(
                        queryUri,
                        new String[]{
                                ContactsContract.CommonDataKinds.Phone._ID,
                                ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
                        },
                        PHONE_NUMBER_SELECTION,
                        null,
                        null
                );

                if (cursor != null) {
                    if (cursor.getCount() > 1) {
                        mMutilNumber = true;
                        cursor.moveToPosition(-1);
                        while (cursor.moveToNext()) {
                            if (cursor.getInt(0) == 1) {
                                mMutilNumber = false;
                                break;
                            }
                        }
                    }
                    cursor.close();
                }
            }
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
            mPhoneView.setVisibility(View.GONE);
            return;
        } else {
            mPhoneView.setVisibility(View.VISIBLE);
        }

        Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.LABEL,
                        ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
                },
                ContactsContract.CommonDataKinds.Phone._ID + " = " + data.dataId,
                null,
                null
        );
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                int destinationType = cursor.getInt(0);
                String destinationLabel = cursor.getString(1);
                String photoThumbnailUri = cursor.getString(2);
                data.setPhoneData(destinationType, destinationLabel, photoThumbnailUri);
            }
            cursor.close();
        }

        final SpannableString textToSet = new SpannableString(number);
        int offset = 0;

        if (numberHighlight != null) {
            applyMaskingHighlightNumber(textToSet, numberHighlight.start + offset,
                    numberHighlight.end + offset);
        }
        setMarqueeText(mPhoneView, textToSet);
        mPhoneView.setContentDescription(number);

        mNameContact = name;
        mPhoneContact = number;
        mDataIdContact = String.valueOf(data.dataId);
        mIdContactSuggest = data.id;
        Message message = new Message();
        message.obj = query;
        mQueryHandler.sendMessage(message);

        mViewShowMore.setVisibility(mMutilNumber ? View.VISIBLE : View.GONE);
        bindPhotoView(data);
    }

    void setShowNumberMode(boolean showNumber) {
        mShowNumberMode = showNumber;
    }

    public void setListener(ActionSmartSuggest listener) {
        this.mListener = listener;
    }

    boolean isShowing() {
        return mPopup.isShowing();
    }

    public void updateSim() {
        PhoneAccountHandle handle = null;
        // Bkav TienNAb: Tao handle cua sim khong phai sim mac dinh
        PhoneAccountHandle handleNotDefault = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            handle = TelecomUtil.getDefaultOutgoingPhoneAccount(mContext,
                    PhoneAccount.SCHEME_TEL);
            handleNotDefault = SimUltil.getHandleNotDefaultSim(mContext);
        }

        if (handle != null) {
            if (PhoneUtils.getDefault().getActiveSubscriptionCount() > 1) {
                mButtonCallSim2.setVisibility(View.VISIBLE);
                final Drawable defaultProfileDrawable = BtalkCallLogCache.
                        getCallLogCache(mContext).getAccountIcon(handle);
                if (defaultProfileDrawable != null) {
                    final Bitmap iconBitmap = convertDrawableToBitmap(defaultProfileDrawable,
                            mContext.getResources().getDimensionPixelSize(R.dimen.size_icon_sim_popup));
                    mButtonCall.setShowSim(true, iconBitmap);
                }else {
                    mButtonCall.setShowSim(false, null);
                }
                final Drawable notDefaultProfileDrawable = BtalkCallLogCache.
                        getCallLogCache(mContext).getAccountIcon(handleNotDefault);
                if (notDefaultProfileDrawable != null) {
                    // Bkav TienNAb: Tao Bitmap cua sim khong phai sim mac dinh
                    final Bitmap iconBitmapNotDefaultSim = convertDrawableToBitmap(BtalkCallLogCache.
                                    getCallLogCache(mContext.getApplicationContext()).getAccountIcon(handleNotDefault),
                            mContext.getResources().getDimensionPixelSize(R.dimen.size_icon_sim_popup));
                    mButtonCallSim2.setShowSim(true, iconBitmapNotDefaultSim);
                }else {
                    mButtonCallSim2.setShowSim(false, null);
                }
                return;
            } else {
                // Bkav TienNAb: an icon cuoc goi sim 2 khi su dung 1 sim
                mButtonCallSim2.setVisibility(View.GONE);
                // Bkav HuyNQN doi voi truong hop 1 esim co nhieu prof
                List<ISimProfile> listEsim = ESimDbController.getAllSim();
                if(listEsim.size() > 1){
                    final Drawable defaultProfileDrawable = BtalkCallLogCache.
                            getCallLogCache(mContext).getAccountIcon(handle);
                    if (defaultProfileDrawable != null) {
                        final Bitmap iconBitmap = convertDrawableToBitmap(defaultProfileDrawable,
                                mContext.getResources().getDimensionPixelSize(R.dimen.size_icon_sim_popup));
                        mButtonCall.setShowSim(true, iconBitmap);
                    }else {
                        mButtonCall.setShowSim(false, null);
                    }
                    return;
                }
            }
        } else {
            // Bkav TienNAb: an icon cuoc goi sim 2 khi chua chon sim mac dinh khi goi
            mButtonCallSim2.setVisibility(View.GONE);
        }
        mButtonCall.setShowSim(false, null);
        mButtonCallSim2.setShowSim(false, null);
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
        spannable.setSpan(TextUtils.TruncateAt.MARQUEE, 0, spannable.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannable);
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
        if (!BtalkActivity.isAndroidQ()){
            if (data.accountType != null
                    && data.accountName != null) {
                final String accountType = data.accountType;
                final String accountName = data.accountName;
                account = new Account(accountName, accountType);
            }
        }
        if (photoId != 0) {
            ContactPhotoManager.getInstance(mContext.getApplicationContext()).loadThumbnail(mPhotoView, photoId, account, false,
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
            ContactPhotoManager.getInstance(mContext.getApplicationContext()).loadDirectoryPhoto(mPhotoView, photoUri, account, false,
                    true, request);
        }
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToanNTe fix Danh bạ - BOS 8.7 - Lỗi: Không thu lại bàn phím chữ khi mở giao diện Chi tiết liên hệ từ liên hệ đề xuất trong Danh bạ
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mListener.onClickMessage();
                }
                if (ContactsCompat.isEnterpriseContactId(data.id)) {
                    // No implicit intent as user may have a different contacts app in work profile.
                    ContactsContract.QuickContact.showQuickContact(mContext.getApplicationContext(), new Rect(), ContactsContract.Contacts.getLookupUri(data.id, data.lookupKey),
                            BtalkQuickContactActivity.MODE_FULLY_EXPANDED, null);
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


    void clearView() {
        mPhoneView.setText("");
        mNameView.setText("");
        mPhoneView.setVisibility(View.GONE);
        mPhotoView.setVisibility(View.INVISIBLE);
    }

    private void applyMaskingHighlight(SpannableString text, int start, int end) {
        text.setSpan(new ForegroundColorSpan(0xFFFF7E38), start, end, 0);
    }

    private void applyMaskingHighlightNumber(SpannableString text, int start, int end) {
        text.setSpan(new ForegroundColorSpan(0xFFFF7E38), start, end, 0);
    }

    private ArrayList<MatchPair> getMatchName(String displayName, String query) {
        ArrayList<MatchPair> matches = new ArrayList<>();

        boolean isNormalizeQuery = false;

        for (char c : query.toCharArray()) {
            char normalize = mMap.normalizeCharacter(c);
            if (c != normalize) {
                isNormalizeQuery = true;
            }
        }

        String nameFormat = displayName.toLowerCase();
        String queryFormat = query.toLowerCase();

        int cursor = 0;
        while (nameFormat.contains(queryFormat)) {
            int posFirst = nameFormat.indexOf(queryFormat);
            if (posFirst == 0 || nameFormat.charAt(posFirst - 1) == ' ') {
                matches.add(new MatchPair(posFirst + cursor, posFirst + queryFormat.length() + cursor));
            }
            cursor += (posFirst + 1);
            nameFormat = nameFormat.substring(posFirst + 1, nameFormat.length());
        }

        if (matches.isEmpty()) {
            queryFormat = query.toLowerCase();
            StringBuilder stringNormalize = new StringBuilder();

            for (char c : nameFormat.toCharArray()) {
                char normalize = mMap.normalizeCharacter(c);
                stringNormalize.append(normalize);
            }
            int cursorFormat = 0;
            nameFormat = stringNormalize.toString();
            if (!isNormalizeQuery) {
                while (nameFormat.contains(queryFormat)) {
                    int posFirst = nameFormat.indexOf(queryFormat);
                    if (posFirst == 0 || nameFormat.charAt(posFirst - 1) == ' ') {
                        matches.add(new MatchPair(posFirst + cursorFormat, posFirst + queryFormat.length() + cursorFormat));
                    }
                    cursorFormat += (posFirst + 1);
                    nameFormat = nameFormat.substring(posFirst + 1, nameFormat.length());
                }
            } else {
                StringBuilder normalizeQuery = new StringBuilder();
                for (char c : queryFormat.toCharArray()) {
                    char normalize = mMap.normalizeCharacter(c);
                    normalizeQuery.append(normalize);
                }
                queryFormat = normalizeQuery.toString();
                while (nameFormat.contains(queryFormat)) {
                    int posFirst = nameFormat.indexOf(queryFormat);
                    if (posFirst == 0 || nameFormat.charAt(posFirst - 1) == ' ') {
                        matches.add(new MatchPair(posFirst + cursorFormat, posFirst + queryFormat.length() + cursorFormat));
                    }
                    cursorFormat += (posFirst + 1);
                    nameFormat = nameFormat.substring(posFirst + 1, nameFormat.length());
                }
            }
            if (matches.isEmpty()) {
                nameFormat = nameFormat.replaceAll(" ", "");
                nameFormat = nameFormat.replaceAll("'", "");

                while (nameFormat.contains(queryFormat)) {
                    int posFirst = nameFormat.indexOf(queryFormat);
                    if (posFirst == 0 || nameFormat.charAt(posFirst - 1) == ' ') {
                        posFirst = posFirst + cursorFormat;
                        String nameSmooth = displayName.substring(posFirst, posFirst + queryFormat.length());
                        int length = nameSmooth.length();
                        nameSmooth = nameSmooth.replaceAll(" ", "");
                        nameSmooth = nameSmooth.replaceAll("'", "");

                        int posAdd = length - nameSmooth.length();

                        nameSmooth = displayName.substring(posFirst, posFirst + queryFormat.length() + posAdd);
                        length = nameSmooth.length();

                        nameSmooth = nameSmooth.replaceAll(" ", "");
                        nameSmooth = nameSmooth.replaceAll("'", "");

                        posAdd = length - nameSmooth.length();

                        matches.add(new MatchPair(posFirst, posFirst + queryFormat.length() + posAdd));
                    }
                    cursorFormat += (posFirst + 1);
                    nameFormat = nameFormat.substring(posFirst + 1, nameFormat.length());
                }
            }
        }
        return matches;
    }

    private class MatchPair {
        final int start;

        final int end;

        MatchPair(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private ActionSmartSuggest mListener;

    public interface ActionSmartSuggest {
        void onClickMessage();
        void onClick(DialerDatabaseHelper.ContactNumber data);
        default void checkDataContact(String dataId, long idContactSuggest, String name, String phone){}// Bkav HuyNQN lay ra dataId cua contact tren suggest popup
    }

    public boolean isInteract() {
        return mIsInteract;
    }
}
