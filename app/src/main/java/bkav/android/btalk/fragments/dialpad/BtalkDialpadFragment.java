package bkav.android.btalk.fragments.dialpad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.CallUtil;
import com.android.dialer.calllog.PhoneAccountUtils;
import com.android.dialer.dialpad.DialpadFragment;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.IntentUtil;
import com.android.dialer.util.TelecomUtil;
import com.android.ex.chips.ChipsUtil;
import com.android.incallui.Call;
import com.android.messaging.Factory;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.UiUtils;

import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.contacts.BtalkContactsPreferenceActivity;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.esim.dialog_choose_sim.DialogChooseSimFragment;
import bkav.android.btalk.fragments.BtalkPhoneFragment;
import bkav.android.btalk.messaging.BtalkFactoryImpl;
import bkav.android.btalk.messaging.ui.BtalkUIIntentsImpl;
import bkav.android.btalk.messaging.ui.appsettings.BtalkApplicationSettingsActivity;
import bkav.android.btalk.messaging.util.BtalkTypefaces;
import bkav.android.btalk.mutilsim.SimUltil;
import bkav.android.btalk.settings.BtalkDialerSettingsActivity;
import bkav.android.btalk.settings.BtalkSettingDisplayFragment;
import bkav.android.btalk.speeddial.BtalkSpeedDialListActivity;
import bkav.android.btalk.suggestmagic.BtalkDialerDatabaseHelper;
import bkav.android.btalk.suggestmagic.ImageCallButton;
import bkav.android.btalk.suggestmagic.SuggestPopup;
import bkav.android.btalk.suggestmagic.SuggestViewHolder;
import bkav.android.btalk.utility.Clipboard;

/**
 * Created by trungth on 22/03/2017.
 * Bkav TrungTH lop ke thua DialpadFragment sua lai de goi 1 so ham theo activty cua minh
 */

public class BtalkDialpadFragment extends DialpadFragment {

    private static final String TOOLTIP_REFERENCE = "tooltip_reference";

    private boolean mActionClickDigit = false;

    // Anhdts view action nhan tin
    private View mBackgroundActionMessage;

    private View mActionSendMessage;

    private View mBackgroundActionDeleteNumber;

    private View mActionDelete;

    private View mOverlayView;

    private boolean mIsOnCreateView;

    // Anhdts su kien sim thay doi
    private boolean mIsSimChange;

    private Context mContext;

    private int mCountShowTooltip;

    // khoang cach dich chuyen phan biet click va vuot
    private int mTouchSlop;

    // Bkav HuyNQN tao LPAController

//    private RecyclerView mRecyclerViewEsim;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mContext = getActivity().getApplicationContext();

        mCountShowTooltip = PreferenceManager.getDefaultSharedPreferences(mContext).
                getInt(TOOLTIP_REFERENCE, 0);

        if (ChipsUtil.isRunningNOrLater()) {
            setRetainInstance(true);
        }
        mTouchSlop = 5 * ViewConfiguration.get(getActivity().getApplicationContext()).getScaledEdgeSlop();


    }

    /**
     * Anhdts get lai gia tri touchSlop
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mTouchSlop = ViewConfiguration.get(getActivity().getApplicationContext()).getScaledEdgeSlop();
    }

    // Anhdts get view digit
    public View getContainerViewDigit(View fragmentView) {
        if (getParentFragment() == null) {
            BtalkPhoneFragment.newInstance();
        }
        return ((BtalkPhoneFragment) getParentFragment()).getContainerDigit();
    }

    @Override
    protected OnDialpadQueryChangedListener getDialpadQueryChangedListener() {
        return (OnDialpadQueryChangedListener) getParentFragment();
    }


    @Override
    public void hideAndClearDialpad(boolean animate) {
        ((BtalkActivity) getActivity()).hideDialpadFragment(animate, true);
        if (mListener != null) {
            mListener.goneRecycleView();
        }
    }

    @Override
    protected void onDialpadShown() {
        ((BtalkActivity) getActivity()).onDialpadShown();
    }

    @Override
    protected int getLayoutDialpadFragment() {
        return R.layout.btalk_dialpad_fragment;
    }

    /**
     * Anhdts setup view add contact and send message
     */
    @Override
    protected void setupActionView(View fragmentView) {
        mKeyViewContainer = (RelativeLayout) fragmentView.findViewById(R.id.key_view_container);
        if (mViewSimContainer != null && mViewSimContainer.getParent() != mKeyViewContainer) {
            ((ViewGroup) mViewSimContainer.getParent()).removeView(mViewSimContainer);
            mKeyViewContainer.addView(mViewSimContainer);
        }
        mActionSendMessage = fragmentView.findViewById(R.id.send_message_action);
        // Bkav TrungTH bo xung icon add contact tren ban phim
        mActionDelete = fragmentView.findViewById(R.id.delete_number_button);

        mBackgroundActionMessage = fragmentView.findViewById(R.id.background_action_message);
        mBackgroundActionDeleteNumber = fragmentView.findViewById(R.id.background_action_addContact);

        mActionSendMessage.setOnClickListener(this);
        mActionDelete.setOnClickListener(this);
        mActionDelete.setOnLongClickListener(this);

//        final RippleDrawable rippleBackground = (RippleDrawable)
//                mDialpadView.getDrawableCompat(getContext(), R.drawable.btalk_ripple_dialpad);
//        if (mDialpadView.mRippleColor != null) {như
//            rippleBackground.setColor(mDialpadView.mRippleColor);
//        }
//        mActionSendMessage.setBackground(rippleBackground);
//        mActionAddContact.setBackground(rippleBackground);

    }

    // Anhdts su kien click
    @Override
    protected boolean checkActionClick(View view, int resId) {
        if (resId == R.id.send_message_action) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            handleSendMessage();
            return true;
        } else if (resId == R.id.delete_number_button) {

            // Bkav HuyNQN xu ly xoa du lieu tren editText
            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
            mDigits.onKeyDown(KeyEvent.KEYCODE_DEL, event);
            return true;
        }
        return false;
    }

    // Bkav HuyNQN xu ly xoa toan bo so tren editText khi longClick
    @Override
    protected void deleteAllNumber(View view) {
        final Editable digits = mDigits.getText();
        final int id = view.getId();
        if (id == R.id.delete_number_button) {
            digits.clear();
        }
    }

    // Bkav HuyNQN override lai de xu ly chon sim the logic moi
    @Override
    public void callVoicemail() {
        // Bkav TienNAb: sua lai logic goi thu thoai
        PhoneAccountHandle handle = TelecomUtil.getDefaultOutgoingPhoneAccount(getContext(),
                PhoneAccount.SCHEME_TEL);
        if (handle == null && BtalkCallLogCache.getCallLogCache(getContext()).isHasSimOnAllSlot()) {
            Intent intent = new IntentUtil.CallIntentBuilder(CallUtil.getVoicemailUri()).setCallInitiationType(Call.LogState.INITIATION_DIALPAD).build();
            DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(intent);
            dialogChooseSimFragment.show(getFragmentManager(), "chooseSim");
            hideAndClearDialpad(false);
        } else {
            super.callVoicemail();
        }
    }

    // Bkav HuyNQN xu ly them moi contact
    @Override
    protected void addNewContact(View view) {
        super.addNewContact(view);
        if (mDigits != null) {
            String number = mDigits.getText().toString();
            if (!TextUtils.isEmpty(number)) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                BtalkUIIntentsImpl.get().launchAddContactActivity(getContext(), number);
            }
        }
    }

    private void handleSendMessage() {
        if (isDigitsEmpty() && (mRecipients == null || !mRecipients.isShown())) {
            // No number entered.
            handleSendMessageWithEmptyDigits();
        } else {
            boolean isDigitsShown = mDigits.isShown();
            final String number;
            // Anhdts nhan tin toi so goi y
            SuggestViewHolder suggestViewHolder = getSuggestHolder();
            if (suggestViewHolder != null && suggestViewHolder.hasSuggest()) {
                number = String.valueOf(suggestViewHolder.getContentDescription());
            } else {
                number = mDigits.getText().toString();
            }
            if (isDigitsShown && isDigitsEmpty()) {
                handleSendMessageWithEmptyDigits();
            } else if (mAddParticipant && isDigitsEmpty()
                    && mRecipients.isShown() && isRecipientEmpty()) {
                // mRecipients must be empty
                // TODO add support for conference URI in last number dialed
                // use ErrorDialogFragment instead? also see
                // android.app.AlertDialog
                Toast.makeText(mContext,
                        R.string.toast_warn_cannot_send_message,
                        Toast.LENGTH_SHORT).show();
            } else {
                // "persist.radio.otaspdial" is a temporary hack needed for one carrier's automated
                // test equipment.
                // TODO: clean it up.
                if (!TextUtils.isEmpty(mProhibitedPhoneNumberRegexp) && number.matches(mProhibitedPhoneNumberRegexp)) {
                    Log.i(TAG, "The phone number is prohibited explicitly by a rule.");
                    if (mContext != null) {
                        DialogFragment dialogFragment = ErrorDialogFragment.newInstance(
                                R.string.dialog_phone_send_prohibited_message);
                        dialogFragment.show(getFragmentManager(), "phone_prohibited_dialog");
                    }
                    // Clear the digits just in case.
                    clearDialpad();
                } else {
                    mIsMakeAction = true;
                    final Intent intent = IntentUtil.getSendSmsIntent(number);
                    ((BtalkFactoryImpl) Factory.get()).registerSendMessage();
                    DialerUtils.startActivityWithErrorToast(mContext, intent);
                }
            }
        }
    }

    private void handleSendMessageWithEmptyDigits() {
        mIsMakeAction = true;
        if (phoneIsCdma() && isPhoneInUse()) {
            // TODO: Move this logic into services/Telephony
            //
            // This is really CDMA specific. On GSM is it possible
            // to be off hook and wanted to add a 3rd party using
            // the redial feature.
            startActivity(newFlashIntent());
        } else {
            if (!TextUtils.isEmpty(mLastNumberDialed)) {
                // Recall the last number dialed.
                mDigits.setText(mLastNumberDialed);

                // ...and move the cursor to the end of the digits string,
                // so you'll be able to delete digits using the Delete
                // button (just as if you had typed the number manually.)
                //
                // Note we use mDigits.getResponse().length() here, not
                // mLastNumberDialed.length(), since the EditText widget now
                // contains a *formatted* version of mLastNumberDialed (due to
                // mTextWatcher) and its length may have changed.
                mDigits.setSelection(mDigits.getText().length());
            } else {
                // There's no "last number dialed" or the
                // background query is still running. There's
                // nothing useful for the Dial button to do in
                // this case.  Note: with a soft dial button, this
                // can never happens since the dial button is
                // disabled under these conditons.
                playTone(ToneGenerator.TONE_PROP_NACK);
            }
        }
    }

    /**
     * Anhdts neu co so goi y thi se goi toi so duoc goi y khi an icon goi dien
     * neu la action sua truoc khi goi thi bo che do sua truoc khi goi
     */
    @Override
    public boolean processPrefixCallAction(boolean simChoose) {
        mIsMakeAction = true;
        if (mActionClickDigit) {
            mActionClickDigit = false;
            return false;
        }
        SuggestViewHolder suggestViewHolder = getSuggestHolder();
        if (suggestViewHolder != null && suggestViewHolder.hasSuggest()) {
            String number = String.valueOf(suggestViewHolder.getContentDescription());
            BtalkDialerDatabaseHelper.getInstance(getActivity())
                    .setRecentCall(suggestViewHolder.getDisplayName());
            if (number != null && !number.isEmpty()) {
                if (!simChoose) {
                    if (mButtonCall.isShowingSim()) {
                        SimUltil.callWithSimMode(getActivity(), true, number);
                        hideAndClearDialpad(false);
                        return true;
                    }
                    //Bkav QuangNDb show dialog luon o Btalk neu co nhieu sim chu khong de den luc hien giao dien dialer moi show
                    if (BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot()) {
                        DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(number);
                        dialogChooseSimFragment.show(getFragmentManager(), "chooseSim");
                    } else {
                        final Intent intent = CallUtil.getCallIntent(number);
                        intent.putExtra(ADD_PARTICIPANT_KEY, mAddParticipant && isPhoneInUse());
                        intent.putExtra("Cdma_Supp", true);
                        DialerUtils.sendBroadcastCount(mContext, DialerUtils.convertTabSeletedForCall(BtalkActivity.TAB_SELECT), 0);
                        DialerUtils.startActivityWithErrorToast(mContext, intent);
                        hideAndClearDialpad(false);
                    }

                    return true;
                } else {
                    //Bkav QuangNDb sua thanh activity moi goi duoc
                    SimUltil.callWithSlotSim(getActivity(), mIdSimChooseCall, number);
                    hideAndClearDialpad(false);
//                    SimUltil.callWithSimMode(getActivity(), mViewFocusPosition == getSimDefault(), number);
//                    hideAndClearDialpad(false);
                    return true;
                }
            }
        }
        return false;
    }

    // Anhdts
    public void setOverlayView(View overlayView) {
        mOverlayView = overlayView;
    }

    boolean mIsAnimRunning = false;

    // Anhdts an lop view ben duoi dialpad de dialpad transparent nhin xuong duoc background
    public void hideOverlay() {
        if (mIsAnimRunning || mOverlayView.getVisibility() == View.INVISIBLE) {
            return;
        }
        mIsAnimRunning = true;
        AlphaAnimation anim = new AlphaAnimation(1f, 0.2f);
        anim.setDuration(250);
        anim.setFillAfter(true);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override

            public void onAnimationEnd(Animation animation) {
                mOverlayView.clearAnimation();
                mOverlayView.setAlpha(1);
                mOverlayView.setVisibility(View.INVISIBLE);
                mIsAnimRunning = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mOverlayView.startAnimation(anim);
    }

    @Override
    public void onResume() {
        if (getActivity() != null && getActivity() instanceof BtalkActivity) {
            // Bkav TrungTh goi ham thong bao da vao onResume de bao da load xong fragment nay
            mContext = getActivity().getApplicationContext();
            ((BtalkActivity) getActivity()).justFinishLoadingTab();
        }
        super.onResume();
        updateDeleteButton(TextUtils.isEmpty(mDigits.getText()));
    }

    @Override
    public void updateDeleteButton(boolean emptyText) {
        if (mBackgroundActionMessage == null) {
            return;
        }
        // Anhdts cho hien nut mo navigation
        if (emptyText) {
            mBackgroundActionMessage.setVisibility(View.VISIBLE);
            mActionSendMessage.setVisibility(View.INVISIBLE);
            mOverflowMenuButton.setVisibility(View.VISIBLE);
            mActionDelete.setVisibility(View.INVISIBLE);
            mBackgroundActionDeleteNumber.setVisibility(View.VISIBLE);
        } else {
            mOverflowMenuButton.setVisibility(View.INVISIBLE);
            if (!mAddContact.isShown()) {
                mBackgroundActionMessage.setVisibility(View.INVISIBLE);
                mActionSendMessage.setVisibility(View.VISIBLE);
                mActionDelete.setVisibility(View.VISIBLE);
                mBackgroundActionDeleteNumber.setVisibility(View.INVISIBLE);
            }
        }
    }

    // Bkav HuyNQN xu ly nut addContact
    public void showActionAddContact(boolean visible) {
        // Bkav TrungTH bo xung icon add contact tren ban phim
        // Bkav TienNAb: Check bien mAddContact khac null thi lam
        if (mAddContact != null) {
            mAddContact.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }

    }


    @Override
    protected void initDigits(View container) {
        mIsOnCreateView = true;
        mDigits = (EditText) container.findViewById(R.id.digits);
        mAddContact = container.findViewById(R.id.add_new_contact);
        mOverflowMenuButton = container.findViewById(R.id.dialpad_overflow);
        // Anhdts show navigation
        mOverflowMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (getActivity() != null) {
//                    ((BtalkActivity) getActivity()).showNavigation();
//                }
                showSettingMenu();
            }
        });
        AccessibilityManager accessibilityManager = (AccessibilityManager)
                getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            // The text view must be selected to send accessibility events.
            mDigits.setSelected(true);
        }
        mDigits.setTypeface(BtalkTypefaces.sRobotoLightFont);

        mDigits.setClickable(true);
        mDigits.setLongClickable(true);
        mDigits.setFocusableInTouchMode(true);
        mDigits.setCursorVisible(false);

        mDigits.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SuggestViewHolder suggestViewHolder = getSuggestHolder();
                if (suggestViewHolder != null && suggestViewHolder.hasSuggest()) {
                    mActionClickDigit = true;
                    handleDialButtonPressed();
                    return true;
                } else {
                    if (TextUtils.isEmpty(mDigits.getText())) {
                        String temp = Clipboard.get().getStringClipboard(mContext);
                        if (!TextUtils.isEmpty(temp)) {
                            setPasteFromClipboard(true);
                            mDigits.setCursorVisible(false);
                            mDigits.setText(temp);
                            mDigits.setSelection(temp.length());
                        }
                        return true;
                    }
                }
                return false;
            }
        });

    }

    /**
     * Anhdts xoa digit neu can thiet
     * trong truong hop vua moi goi thi xoa di
     */
    protected void removeDigitIfNeed() {
        // Anhdts neu action fix so truoc khi goi thi k remove
        if (getActivity() instanceof BtalkActivity) {
            BtalkActivity activity = (BtalkActivity) getActivity();
            if (activity.getIntent() != null && activity.getIntent().getAction() != null &&
                    activity.getIntent().getAction().equals(BtalkActivity.ACTION_FIX_BEFORE_CALL)) {
                return;
            }
        }
        if (isMakeAction()) {
            mDigits.setText("");
        }
    }

    public SuggestViewHolder getSuggestHolder() {
        if (getParentFragment() instanceof BtalkPhoneFragment) {
            return ((BtalkPhoneFragment) getParentFragment()).getSuggestView();
        }
        return null;
    }

    /**
     * Anhdts
     * animation dialpad dang chay
     */
    public boolean isShowDialpad() {
        return mIsAnimRunning;
    }

    @Override
    protected boolean isInstanceofBtalk(Activity parent) {
        return parent instanceof BtalkActivity;
    }

    /**
     * Chuyen xu ly len ham tren  de goi duoc ca ham ben tren
     */
    public View getDialpadView() {
        return mDialpadView;
    }

    private LinearLayout mViewSimContainer;

    private RelativeLayout mKeyViewContainer;

    private static final int SIM_LINE_FIRST = 0;

    private static final int SIM_LINE_SECOND = 1;

    private static final int SIM_LINE_TRIANGLE = 2;

    private static final int NO_FOCUS_VIEW = -1;

    private boolean mIsShowSimContainer = false;

    private int mViewFocusPosition = -1;

    private float mPosDown;

    /**
     * Anhdts su kien chon sim
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void setActionLongClickFloatingAction(final View actionLongClickFloatingAction, final View containerFloat, final View fragmentView) {
        actionLongClickFloatingAction.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (TelecomUtil.getDefaultOutgoingPhoneAccount(mContext,
                        PhoneAccount.SCHEME_TEL) == null) {
                    return false;
                }
                if (BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot()) {
                    if (event.getAction() == MotionEvent.ACTION_MOVE && !TextUtils.isEmpty(mDigits.getText())) {
                        if (!mIsShowSimContainer && (mPosDown - event.getY()) > mTouchSlop) {
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    mLineSeparate = 0;
//                                    checkShowViewChooseSim(containerFloat);
                                }
                            });
                        } else {
                            actionTouchChooseSim(event);
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_UP
                            || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        if (mIsShowSimContainer) {
                            mFocusContainerSim = false;
                            Rect rect = new Rect();
                            mViewSimContainer.getGlobalVisibleRect(rect);
                            clearFocusView();
                            if (mViewFocusPosition != NO_FOCUS_VIEW && rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                                actionChooseSimCall(mViewFocusPosition == SIM_LINE_FIRST);
                            } else {
                                mViewFocusPosition = NO_FOCUS_VIEW;
                            }
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mPosDown = event.getY();
                    }
                }
                return false;
            }
        });

        actionLongClickFloatingAction.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mListener != null) {
                    mListener.longClick(v);
                    return true;
                }
                if (TelecomUtil.getDefaultOutgoingPhoneAccount(mContext,
                        PhoneAccount.SCHEME_TEL) == null) {
                    return true;
                }
                if (/*BtalkCallLogCache.getCallLogCache(mContext).getIsMultiSim() && */!TextUtils.isEmpty(mDigits.getText())) { // Bkav HuyNQN  1 sim hay multi de long click dc
                    if (!mIsShowSimContainer) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                // Bkav HuyNQN ko dung den view nay nua
                                /*checkShowViewChooseSim(containerFloat);*/
                            }
                        });
                        return true;
                    }
                }
                return false;
            }
        });

        mButtonCall = (ImageCallButton) mFloatingActionButtonController.getContainerView().findViewById(R.id.dialpad_floating_action_button);
        mButtonCall.setImageResource(R.drawable.btalk_icon_btn_call);
        updateFloatButtonSim();
    }


    private void checkShowViewChooseSim(View containerFloat) {
        mViewSimContainer = SimUltil.showSimChooseView(containerFloat, mKeyViewContainer,
                mViewSimContainer, BtalkDialpadFragment.this);
        if (mViewTooltip != null && mViewTooltip.isShown()) {
            mViewTooltip.setVisibility(View.GONE);
            mCountShowTooltip = -1;
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().
                    putInt(TOOLTIP_REFERENCE, -1).apply();
        }
        mFocusContainerSim = true;
        mIsShowSimContainer = true;
    }

    private View mViewTooltip;

    public void showTooltip() {
        if (mCountShowTooltip == -1 || mIsTwoButton == null || mIsTwoButton
                || !BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot()
                || TelecomUtil.getDefaultOutgoingPhoneAccount(mContext,
                PhoneAccount.SCHEME_TEL) == null) {
            return;
        }
        if (mViewTooltip == null) {
            mViewTooltip = SimUltil.showTooltipSim(mKeyViewContainer);
        } else if (mViewTooltip.isShown()) {
            return;
        }
        mViewTooltip.setVisibility(View.VISIBLE);
        mCountShowTooltip++;
        if (mCountShowTooltip == 5) {
            mCountShowTooltip = -1;
        }
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().
                putInt(TOOLTIP_REFERENCE, mCountShowTooltip).apply();
    }

    public void hideTooltip() {
        if (mViewTooltip != null && mViewTooltip.isShown()) {
            mViewTooltip.setVisibility(View.GONE);
        }
    }


    /**
     * Anhdts clear all focus view
     */
    private void clearFocusView() {
        if (mViewFocusPosition == SIM_LINE_FIRST) {
            changeBackgroundView(SIM_LINE_FIRST, false);
        } else {
            changeBackgroundView(SIM_LINE_SECOND, false);
            changeBackgroundViewTriangle(SIM_LINE_TRIANGLE, false);
        }
    }

    /**
     * Anhdts check su kien di tay, neu trong vung hien thi cua dong nao thi focus vao dong do
     */
    private void actionTouchChooseSim(MotionEvent event) {
        if (mIsShowSimContainer) {
            if (mLineSeparate == SIM_LINE_FIRST) {
                Rect rect = new Rect();
                View view = mViewSimContainer.getChildAt(0);
                view.getGlobalVisibleRect(rect);
                mLineSeparate = rect.bottom;
            }

            if ((int) event.getRawY() < mLineSeparate) {
                changeBackgroundView(SIM_LINE_FIRST, true);
                if (mViewFocusPosition == SIM_LINE_SECOND) {
                    changeBackgroundView(SIM_LINE_SECOND, false);
                    changeBackgroundViewTriangle(SIM_LINE_TRIANGLE, false);
                }
                mViewFocusPosition = SIM_LINE_FIRST;
            } else {
                changeBackgroundView(SIM_LINE_SECOND, true);
                changeBackgroundViewTriangle(SIM_LINE_TRIANGLE, true);
                if (mViewFocusPosition == SIM_LINE_FIRST) {
                    changeBackgroundView(SIM_LINE_FIRST, false);
                }
                mViewFocusPosition = SIM_LINE_SECOND;
            }
        }
    }

    private void changeBackgroundViewTriangle(int pos, boolean focus) {
        View viewTriangle = mViewSimContainer.getChildAt(pos);
        viewTriangle.getBackground().setTint(ContextCompat.getColor(mContext, focus ? R.color.background_focus_sim_row : R.color.background_sim_row));
    }

    private void changeBackgroundView(int pos, boolean focus) {
        View view = mViewSimContainer.getChildAt(pos);
        view.getBackground().setTint(ContextCompat.getColor(mContext, focus ? R.color.background_focus_sim_row : R.color.background_sim_row));
    }

    private int mLineSeparate = 0;

    private boolean mFocusContainerSim = true;

    /**
     * Anhdts voi cac action goi thi se goi vao ham nay, neu enabled bang true tung la su dung
     * action cuoc goi, se disable chuc nang goi lai
     */
    @Override
    protected void showDialpadChooser(boolean disable) {
    }

    public boolean isShowContainerSim() {
        return mIsShowSimContainer && mViewSimContainer != null && mViewSimContainer.getVisibility() == View.VISIBLE;
    }

    /**
     * Anhdts check co focus vao view container sim khong
     */
    public boolean isFocusContainerSim(MotionEvent event) {
        if (mFocusContainerSim) {
            return true;
        } else {
            Rect rect = new Rect();
            mViewSimContainer.getGlobalVisibleRect(rect);
            return rect.contains((int) event.getRawX(), (int) event.getRawY());
        }
    }

    /**
     * Anhdts dong container sim
     */
    public void closeSimContainer() {
        mIsShowSimContainer = false;
        SimUltil.animateClose(mContext, mViewSimContainer);
        mFocusContainerSim = false;
    }

    /**
     * Anhdts click vao view chon sim hoac tha tay vao do
     */
    public void actionChooseSimCall(boolean isSimDefault) {
        String number = mDigits.getText().toString();
//        clearFocusView();
        if (!TextUtils.isEmpty(number) && !number.matches(mProhibitedPhoneNumberRegexp)) {
            mViewFocusPosition = isSimDefault ? SIM_LINE_FIRST : SIM_LINE_SECOND;
            if (!processPrefixCallAction(true)) {
                SimUltil.callWithSimMode(mContext, isSimDefault, number);
                hideAndClearDialpad(false);
            }
        }
//        closeSimContainer();
    }

    // TODO TrungTH them ham xu ly bam vao slot sim
    // Dang chua ro sao view bi nuot su kien click phai thuc hien tu ham ontouch
    // Se check lai sau
    public boolean actionChooseSimCall(MotionEvent event) {
        if (!isShowContainerSim() || mFocusContainerSim) {
            return false;
        }
        Rect rect = new Rect();
        mViewSimContainer.getGlobalVisibleRect(rect);
        boolean containt = rect.contains((int) event.getRawX(), (int) event.getRawY());
        if (containt) {
            actionTouchChooseSim(event);
            actionChooseSimCall(mViewFocusPosition == SIM_LINE_FIRST);
        }

        return containt;
    }

    /**
     * Anhdts animation hien thi nut goi
     */
    public void setVisibleFabButton(int visibleFabButton) {
        if (mIsTwoButton == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkConfigTwoButton();
                return;
            } else {
                mIsTwoButton = false;
            }
        }
        if (mIsTwoButton) {
            UiUtils.revealOrHideViewWithAnimationBtalk(mContainerTwoButton, visibleFabButton, null);
        } else {
            if (mFloatingActionButtonController != null) {
                UiUtils.revealOrHideViewWithAnimationBtalk(mFloatingActionButtonController.getContainerView(), visibleFabButton, null);
            }
        }
    }

    private boolean mIsJustPasteClipBoard = false;

    public void setPasteFromClipboard(boolean justPaste) {
        mIsJustPasteClipBoard = justPaste;
    }

    public boolean isPasteFromClipboard() {
        return mIsJustPasteClipBoard;
    }

    public View getContainerTwoButton() {
        return mContainerTwoButton;
    }

    private View mContainerTwoButton;

    /**
     * Anhdts them tuy chon 1 hay 2 nut goi
     */
    @Override
    protected void configTwoButtonCall(View fragmentView) {
        mContainerTwoButton = fragmentView.findViewById(R.id.dialpad_float_two_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkConfigTwoButton();
        } else {
            if (mIsOnCreateView) {
                mIsOnCreateView = false;
                mFloatingActionButtonController.getContainerView().setVisibility(View.VISIBLE);
            } else {
                setVisibleFabButton(View.VISIBLE);
            }
        }
    }

    public void setIdSimChooseCall(String mIdSimChooseCall) {
        this.mIdSimChooseCall = mIdSimChooseCall;
    }

    private String mIdSimChooseCall;

    private String mIdSimLeft;

    private String mIdSimRight;

    private Boolean mIsTwoButton = null;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkConfigTwoButton() {

        boolean isTwoButton = BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot() && PreferenceManager.getDefaultSharedPreferences(mContext).
                getBoolean(BtalkSettingDisplayFragment.OPTION_TWO_CALL_BUTTON, false);

        if (mIsTwoButton != null && mIsTwoButton == isTwoButton &&
                mContainerTwoButton != null && mContainerTwoButton.isShown()) {
            return;
        }
        //HienDTk: neu co 1 trong 2 sim bi tat => fix loi crash hien thi 2 nut goi cho 2 sim
        if (ESimUtils.isSlotNotReady(0) || ESimUtils.isSlotNotReady(1)) {
            isTwoButton = false;
        }
        mIsTwoButton = isTwoButton;
        if (isTwoButton) {
            if (mContainerTwoButton != null && !mContainerTwoButton.isShown()) {
                if (mFloatingActionButtonController != null) {
                    mFloatingActionButtonController.getContainerView().setVisibility(View.GONE);
                }
                mContainerTwoButton.setVisibility(View.VISIBLE);
            }
            if (mIsSimChange || TextUtils.isEmpty(mIdSimLeft)) {
            List<PhoneAccountHandle> subscriptionAccountHandles =
                    PhoneAccountUtils.getSubscriptionPhoneAccounts(mContext);
                mIdSimLeft = subscriptionAccountHandles.get(0).getId();
                mIdSimRight = subscriptionAccountHandles.get(1).getId();

                View buttonFirst = mContainerTwoButton.findViewById(R.id.dialpad_floating_button_first);
                ((TextView) buttonFirst.findViewById(R.id.label_sim_first)).setText(
                        SimUltil.getSimName(mContext, subscriptionAccountHandles.get(0)));
                buttonFirst.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mIdSimChooseCall = mIdSimLeft;
                        actionClickTwoButton();
                    }
                });
                buttonFirst.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (mListener != null)
                            mListener.longClickSlot0(v);
                            return true;
                        }

                    });
                    View buttonSecond = mContainerTwoButton.findViewById(R.id.dialpad_floating_button_second);
                    ((TextView) buttonSecond.findViewById(R.id.label_sim_second)).setText(
                            SimUltil.getSimName(mContext, subscriptionAccountHandles.get(1)));
                    buttonSecond.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mIdSimChooseCall = mIdSimRight;
                            actionClickTwoButton();
                        }
                    });
                    buttonSecond.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (mListener != null) {
                                mListener.longClickSlot1(v);
                                return true;
                            }
                            return false;
                        }
                    });
                }
//            }
        } else {
            // TrungTH bo khong dung hieu ung khi vao lan dau
            if (mFloatingActionButtonController != null) {
                UiUtils.revealOrHideViewWithAnimationBtalk(mContainerTwoButton, View.GONE, null);
                if (mIsOnCreateView) {
                    mIsOnCreateView = false;
                    mFloatingActionButtonController.getContainerView().setVisibility(View.VISIBLE);
                } else {
                    setVisibleFabButton(View.VISIBLE);
                }
            }
        }
    }

    /**
     * Anhdts goi bang id handle
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void actionClickTwoButton() {
        if (isDigitsEmpty() && (mRecipients == null || !mRecipients.isShown())) {
            // No number entered.
            handleDialButtonClickWithEmptyDigits();
        } else {
            String number = mDigits.getText().toString();
            if (!TextUtils.isEmpty(number) && !number.matches(mProhibitedPhoneNumberRegexp)) {
                if (!processPrefixCallAction(true)) {
                    SimUltil.callWithSlotSim(getActivity(), mIdSimChooseCall, number);
                    hideAndClearDialpad(false);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void actionCallWithIdSim(String number) {
        if (isDigitsEmpty() && (mRecipients == null || !mRecipients.isShown())) {
            // No number entered.
            handleDialButtonClickWithEmptyDigits();
        } else {
            if (number == null || number.isEmpty()) {
                number = getSuggestNumber();
            }
            if (!TextUtils.isEmpty(number) && !number.matches(mProhibitedPhoneNumberRegexp)) {
                if (!processPrefixCallAction(true)) {
                    SimUltil.callWithSlotSim(getActivity(), mIdSimChooseCall, number);
                    hideAndClearDialpad(false);
                }
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        final Activity activity = getActivity();
        if (activity == null) return;
        if (!hidden && !isDialpadChooserVisible()) {
            if (mAnimate) {
                mDialpadView.animateShow();
            }
            onDialpadShown();
            // Bkav HuyNQN fix loi dong ban phim khi search danh ba khi mo app danh ba lan dau
            Fragment fragment = getFragmentManager().findFragmentById(R.layout.btalk_dialpad_fragment);
            if(fragment != null && fragment.isVisible()){
                mDigits.requestFocus();
            }
        }
    }

//    @Override
//    protected void configureKeypadListeners(View fragmentView) {
//        if (fragmentView.findViewById(R.id.one) instanceof OverlayButton) {
//            final int[] buttonIds = new int[]{R.id.one, R.id.two, R.id.three, R.id.four, R.id.five,
//                    R.id.six, R.id.seven, R.id.eight, R.id.nine, R.id.star, R.id.zero, R.id.pound};
//
//            OverlayButton dialpadKey;
//
//            for (int i = 0; i < buttonIds.length; i++) {
//                dialpadKey = (OverlayButton) fragmentView.findViewById(buttonIds[i]);
//                dialpadKey.setOnPressedListener(this);
//                // Long-pressing button from two to nine will set up speed key dial.
//                if (i > 0 && i < buttonIds.length) {
//                    dialpadKey.setOnLongClickListener(this);
//                }
//            }
//        } else {
//            super.configureKeypadListeners(fragmentView);
//        }
//    }

    @Override
    protected void showNoSpeedNumberDialog(final int number) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.speed_dial_unassigned_dialog_title)
                .setMessage(getString(R.string.speed_dial_unassigned_dialog_message, number))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // go to speed dial setting screen to set speed dial number.
                        Intent intent = new Intent(mContext, BtalkSpeedDialListActivity.class);
                        intent.putExtra(BtalkSpeedDialListActivity.KEY_ADD, String.valueOf(number));
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    protected void floatingScaleIn() {
        // Khong lam gi
    }

    @Override
    protected void floatingScaleOut() {
        // Khong lam gi
    }

    /**
     * Anhdts
     */
    public void setSimChange() {
        mIsSimChange = true;
        updateFloatButtonSim(); // show 2 sim co can cai nay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot()) {
                if (mIsTwoButton) {
                    if (mContainerTwoButton != null && !mContainerTwoButton.isShown()) {
                        if (mFloatingActionButtonController != null) {
                            mFloatingActionButtonController.getContainerView().setVisibility(View.GONE);
                        }
                        mContainerTwoButton.setVisibility(View.VISIBLE);
                    }
                    List<PhoneAccountHandle> subscriptionAccountHandles =
                            PhoneAccountUtils.getSubscriptionPhoneAccounts(mContext);
                    // Bkav TienNAb: fix loi crash app khi lap 2 sim ma disable mot sim
                    if ((mIsSimChange || TextUtils.isEmpty(mIdSimLeft)) && subscriptionAccountHandles.size() > 1) {
                        mIdSimLeft = subscriptionAccountHandles.get(0).getId();
                        mIdSimRight = subscriptionAccountHandles.get(1).getId();

                        View buttonFirst = mContainerTwoButton.findViewById(R.id.dialpad_floating_button_first);
                        ((TextView) buttonFirst.findViewById(R.id.label_sim_first)).setText(
                                SimUltil.getSimName(mContext, subscriptionAccountHandles.get(0)));
                        buttonFirst.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mIdSimChooseCall = mIdSimLeft;
                                actionClickTwoButton();
                            }
                        });
                        View buttonSecond = mContainerTwoButton.findViewById(R.id.dialpad_floating_button_second);
                        ((TextView) buttonSecond.findViewById(R.id.label_sim_second)).setText(
                                SimUltil.getSimName(mContext, subscriptionAccountHandles.get(1)));
                        buttonSecond.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mIdSimChooseCall = mIdSimRight;
                                actionClickTwoButton();
                            }
                        });
                    }
                } else {
                    if (mViewSimContainer != null) {
                        mViewSimContainer.removeAllViews();
                        mViewSimContainer = null;
                    }
                }
            }
        }
        mIsSimChange = false;
    }

    private ImageCallButton mButtonCall;

    @Override
    public void updateFloatButtonSim() {
        if (BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot() || ESimUtils.isMultiProfile()) { /*xu ly ca truong hop lap 1 esim co nhieu profile*/
            PhoneAccountHandle handle;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                handle = TelecomUtil.getDefaultOutgoingPhoneAccount(mContext,
                        PhoneAccount.SCHEME_TEL);
                final Drawable defaultProfileDrawable = BtalkCallLogCache.
                        getCallLogCache(mContext).getAccountIcon(handle);
                if (handle != null && defaultProfileDrawable != null && (mDigits != null && !TextUtils.isEmpty(mDigits.getText()))) {
                    final Bitmap iconSimFloat = SuggestPopup.convertDrawableToBitmap(defaultProfileDrawable, getResources().getDimensionPixelSize(R.dimen.size_icon_sim_dial));
                    mButtonCall.setShowSim(true, iconSimFloat);
                } else {
                    mButtonCall.setShowSim(false, null);
                }
            }
        } else {
            mButtonCall.setShowSim(false, null);
        }
    }

    /**
     * Anhdts xoa contact duoc goi y khi nhap so goi tu 1 app khac
     */
    @Override
    protected void cleanSuggestView() {
        if (getParentFragment() == null) {
            BtalkPhoneFragment.newInstance();
        }
        ((BtalkPhoneFragment) getParentFragment()).cleanSuggestView();
    }

    /**
     * Anhdts check cham vao button goi
     */
    public boolean isTouchButtonCall() {
        return mPosDown != 0;
    }

    public void resetStateButtonDown() {
        mPosDown = 0;
    }

    public interface LongClickCallBackListener {

        void longClick(View view);

        void longClickSlot0(View view);

        void longClickSlot1(View view);

        void goneRecycleView();
    }

    public void setListener(LongClickCallBackListener Listener) {
        this.mListener = Listener;
    }

    private LongClickCallBackListener mListener;

    public static int getSimLineFirst() {
        return SIM_LINE_FIRST;
    }

    public static int getSimLineSecond() {
        return SIM_LINE_SECOND;
    }

    public int getViewFocusPosition() {
        return mViewFocusPosition;
    }

    public int getSimDefault() {
        return mSimDefault;
    }

    public void setSimDefault(int mSimDefault) {
        this.mSimDefault = mSimDefault;
    }

    private int mSimDefault;

    @Override
    protected void callWithoutSuggest(boolean isDigitsShown, String number) {
        PhoneAccountHandle handle = TelecomUtil.getDefaultOutgoingPhoneAccount(getContext(),
                PhoneAccount.SCHEME_TEL);
        if (handle == null && BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot()) {
            DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(number);
            dialogChooseSimFragment.show(getFragmentManager(), "chooseSim");
        } else {
            super.callWithoutSuggest(isDigitsShown, number);
        }

    }

    @Override
    protected void makeACall(String phoneNumber) {
        UIIntents.get().makeACall(mContext, getFragmentManager(), phoneNumber);
    }

    // Bkav TienNAb: hien thi menu setting
    private void showSettingMenu() {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.setting_menu_layout, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setElevation(10);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_setting_menu));
        popupView.findViewById(R.id.phone_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BtalkDialerSettingsActivity.class));
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.message_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BtalkApplicationSettingsActivity.class));
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.contact_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BtalkContactsPreferenceActivity.class));
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(getView(), Gravity.TOP | Gravity.RIGHT, 0, getStatusBarHeight());
    }

    // Bkav TienNAb: ham lay chieu cao statusbar
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    //Bkav QuangNDb lay suggest number neu khong co thi tra ve text dang go
    public String getSuggestNumber() {
        SuggestViewHolder suggestViewHolder = getSuggestHolder();
        if (suggestViewHolder != null && suggestViewHolder.hasSuggest()) {
            String number = String.valueOf(suggestViewHolder.getContentDescription());
            if (number != null && !number.isEmpty()) {
                return number;
            }
        }
        return mDigits.getText().toString();
    }
}
