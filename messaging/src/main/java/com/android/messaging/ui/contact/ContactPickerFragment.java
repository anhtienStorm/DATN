/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.messaging.ui.contact;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.Explode;
import android.transition.Transition;
import android.transition.Transition.EpicenterCallback;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.RecipientEntry;
import com.android.messaging.R;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.action.ActionMonitor;
import com.android.messaging.datamodel.action.GetOrCreateConversationAction;
import com.android.messaging.datamodel.action.GetOrCreateConversationAction.GetOrCreateConversationActionListener;
import com.android.messaging.datamodel.action.GetOrCreateConversationAction.GetOrCreateConversationActionMonitor;
import com.android.messaging.datamodel.binding.Binding;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.data.ContactListItemData;
import com.android.messaging.datamodel.data.ContactPickerData;
import com.android.messaging.datamodel.data.ContactPickerData.ContactPickerDataListener;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.ui.CustomHeaderPagerViewHolder;
import com.android.messaging.ui.CustomHeaderViewPager;
import com.android.messaging.ui.animation.ViewGroupItemVerticalExplodeAnimation;
import com.android.messaging.ui.contact.ContactRecipientAutoCompleteView.ContactChipsChangeListener;
import com.android.messaging.util.Assert;
import com.android.messaging.util.Assert.RunsOnMainThread;
import com.android.messaging.util.ContactUtil;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.UiUtils;
import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Set;


/**
 * Shows lists of contacts to start conversations with.
 */
public class ContactPickerFragment extends Fragment implements ContactPickerDataListener,
        ContactListItemView.HostInterface, ContactChipsChangeListener, OnMenuItemClickListener,
        GetOrCreateConversationActionListener, RecipientEditTextView.RecipientChipDeletedListener {
    public static final String FRAGMENT_TAG = "contactpicker";

    // Undefined contact picker mode. We should never be in this state after the host activity has
    // been created.
    public static final int MODE_UNDEFINED = 0;

    // The initial contact picker mode for starting a new conversation with one contact.
    public static final int MODE_PICK_INITIAL_CONTACT = 1;

    // The contact picker mode where one initial contact has been picked and we are showing
    // only the chips edit box.
    public static final int MODE_CHIPS_ONLY = 2;

    // The contact picker mode for picking more contacts after starting the initial 1-1.
    public static final int MODE_PICK_MORE_CONTACTS = 3;

    // The contact picker mode when max number of participants is reached.
    public static final int MODE_PICK_MAX_PARTICIPANTS = 4;

    // BKav QuangNDb them mode ceate group participant
    public static final int MODE_CREATE_GROUP_PARTICIPANTS = 5;

    // Bkav QuangNDb them bien check khi bam vao back press
    protected boolean mIsBackPress = false;

    protected boolean mIsRequestSend = false;

    protected ViewGroup mParentView;

    protected boolean mIsFist = true;//Bkav QuangNDb Bien check lan dau mo giao dien

    protected int mFistPickingMode = MODE_UNDEFINED;//Bkav QuangNDb Bien luu trang thai lan dau tien

    @Override
    public void onRecipientChipDeleted(RecipientEntry entry) {

    }


    public interface ContactPickerFragmentHost extends ImeUtil.ImeStateHost{
        void onGetOrCreateNewConversation(String conversationId);
        void onBackButtonPressed();
        void onInitiateAddMoreParticipants();
        void onParticipantCountChanged(boolean canAddMoreParticipants);
        void invalidateActionBar();
        // QuangNDB them
        void getDrafData(MessageData draf);
        void getSelfSimId(String selfSimId);
        void setUIState(int state);
        void changeUiState(int state);
        ActionMode startActionMode(ActionMode.Callback callback);
        void dismissActionMode();
        ActionMode getActionMode();
        void fixSizeToolbar();// fix size toolbar khi o che do picker
        void onFinishCurrentConversation();
        boolean isActiveAndFocused();
        void onConversationParticipantDataLoaded(int numberOfParticipants);
    }

    @VisibleForTesting
    final Binding<ContactPickerData> mBinding = BindingBase.createBinding(this);

    protected ContactPickerFragmentHost mHost;
    protected ContactRecipientAutoCompleteView mRecipientTextView;
    private CustomHeaderViewPager mCustomHeaderViewPager;
    private AllContactsListViewHolder mAllContactsListViewHolder;
    private FrequentContactsListViewHolder mFrequentContactsListViewHolder;
    protected View mRootView;
    private View mPendingExplodeView;
    private View mComposeDivider;
    protected Toolbar mToolbar;
    protected int mContactPickingMode = MODE_UNDEFINED;

    // Bkav TienNAb: them bien check focus RecipientTextView
    protected boolean mIsRecipientTextViewFocus = false;

    // Keeps track of the currently selected phone numbers in the chips view to enable fast lookup.
    protected Set<String> mSelectedPhoneNumbers = null;

    /**
     * {@inheritDoc} from Fragment
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Bkav QuangNDb tach code doan init loader
        initLoader();
        // Bkav QuangNDb init binding
        initBinding();
        Log.d("HienDTk", "onCreate: ");
    }

    protected void initBinding() {

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("HienDTk", "onStart: ");
    }

    /**
     * {@inheritDoc} from Fragment
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View view = inflater.inflate(getIdLayout(), container, false);
        //Bkav QuangNDb tach code doan khoi tao RecipientTextView
        initRecipientTextView(view,inflater);
        // BKav QuangNDb tach code doan init view pager
        initViewPager(view);
        //Bkav QuangNDb tach code doan setup toolbar
        setupToolbar(view);
        //Bkav QuangNDb init list message
        initListMessage(view);
        // QuangNDb bin parent view
        initParentView(view);
        // QuangNDb bin compose message view
        initComposeMessageView(view);
        mRootView = view;
        return view;
    }

    /**Bkav QuangNDb init parent view*/
    protected void initParentView(View view) {

    }

    /**Bkav QuangNDb khoi tao recipient TextView*/
    protected void initRecipientTextView(View view, LayoutInflater inflater) {
            mRecipientTextView = initRecipient(view);
            mRecipientTextView.setThreshold(0);
            mRecipientTextView.setDropDownAnchor(R.id.compose_contact_divider);
            mRecipientTextView.setContactChipsListener(this);
            mRecipientTextView.setDropdownChipLayouter(new ContactDropdownLayouter(inflater,
                    getActivity(), this));
            // Bkav QuangNDb tach code doan set adapter cua auto completeTextView
            setRecipientTextAdapter();
            mRecipientTextView.addTextChangedListener(mRecipientTextWatcher);
            mRecipientTextView.setRecipientChipDeletedListener(this);
//            mRecipientTextView.requestFocus();
        setBackgroundRecipientColor();
        // Bkav TienNAb: khi focus vao o nguoi nhan tin nhan thi an giao dien mediapicker di
        mRecipientTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mIsRecipientTextViewFocus = true;
//                    hideMediaPicker();
                } else {
                    mIsRecipientTextViewFocus = false;
                }
            }
        });
    }

    //Bkav QuangNDb set background color o nhap text
    protected void setBackgroundRecipientColor() {

    }

    /**Bkav QuangNDb setup toolbar*/
    protected void setupToolbar(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(getIdResBackIcon());
        mToolbar.setNavigationContentDescription(R.string.back);
        mToolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Bkav QuangNDb them chuyen trang thai isbackpress = true de xu ly o class con
                mIsBackPress = true;
                mHost.onBackButtonPressed();
            }
        });

        mToolbar.inflateMenu(getIdResMenu());
        mToolbar.setOnMenuItemClickListener(this);
    }

    /**Bkav QuangNDb khai bao view action bar*/
    protected void initActionBar(View view) {

    }

    /**Bkav QuangNDb Khoi tao compose messageView*/
    protected void initComposeMessageView(View view) {
    }

    // QuangNDb text watcher cua mRecipientText
    protected TextWatcher mRecipientTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Bkav QuangNdb them ham quey khi text change
            onRecipientTextChanged(s);
        }

        @Override
        public void afterTextChanged(Editable s) {
//            updateTextInputButtonsVisibility();
            updateTextInputTextChange();
//            mRecipientTextView.getRecipientParticipantDataForConversationCreation();
        }
    };



    protected Bundle mSaveInstanceState;//Bkav QuangNDb bien luu trang thai instance cua class
    /**
     * {@inheritDoc}
     *
     * Called when the host activity has been created. At this point, the host activity should
     * have set the contact picking mode for us so that we may update our visuals.
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Assert.isTrue(mContactPickingMode != MODE_UNDEFINED);
//        updateVisualsForContactPickingMode(false /* animate */);
//        mHost.invalidateActionBar();
        // QuangNDb them doan init composeMessageView
        setUpComposeMessageView(savedInstanceState);
        mSaveInstanceState = savedInstanceState;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // We could not have bound to the data if the permission was denied.
        if (mBinding.isBound()) {
            mBinding.unbind();
        }

        if (mMonitor != null) {
            mMonitor.unregister();
        }
        mMonitor = null;
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
        // QuangNDb unbin compose message view
        unbinComposeMessageView();
    }

    protected void unbinComposeMessageView() {

    }

    @Override
    public boolean onMenuItemClick(final MenuItem menuItem) {
        int i = menuItem.getItemId();
        if (i == R.id.action_ime_dialpad_toggle) {
            final int baseInputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE;
            if ((mRecipientTextView.getInputType() & InputType.TYPE_CLASS_PHONE) !=
                    InputType.TYPE_CLASS_PHONE) {
                mRecipientTextView.setInputType(baseInputType | InputType.TYPE_CLASS_PHONE);
                menuItem.setIcon(getIdResIconIme());
            } else {
                mRecipientTextView.setInputType(baseInputType | InputType.TYPE_CLASS_TEXT);
                menuItem.setIcon(getIdResDialPad());
            }
            ImeUtil.get().showImeKeyboard(getActivity(), mRecipientTextView);
            return true;
        } else if (i == R.id.action_add_more_participants) {
            mHost.onInitiateAddMoreParticipants();
            return true;
        } else if (i == R.id.action_confirm_participants) {
            //Bkav QuangNDb Tach code doan click confirm button
            clickConfirmButton();
            return true;
        } else if (i == R.id.action_delete_text) {
            // Bkav QuangNDb tach code doan bam vao icon menu remove
            deleteAllText();

            return true;
        }
        return false;
    }


    @Override // From ContactPickerDataListener
    public void onAllContactsCursorUpdated(final Cursor data) {
        mBinding.ensureBound();
        mAllContactsListViewHolder.onContactsCursorUpdated(data);
    }

    @Override // From ContactPickerDataListener
    public void onFrequentContactsCursorUpdated(final Cursor data) {
        mBinding.ensureBound();
        mFrequentContactsListViewHolder.onContactsCursorUpdated(data);
        if (data != null && data.getCount() == 0) {
            // Show the all contacts list when there's no frequents.
            mCustomHeaderViewPager.setCurrentItem(1);
        }
    }

    @Override // From ContactListItemView.HostInterface
    public void onContactListItemClicked(final ContactListItemData item,
            final ContactListItemView view) {
        if (!isContactSelected(item)) {
            if (mContactPickingMode == MODE_PICK_INITIAL_CONTACT) {
                mPendingExplodeView = view;
            }
            mRecipientTextView.appendRecipientEntry(item.getRecipientEntry());
        } else if (mContactPickingMode != MODE_PICK_INITIAL_CONTACT) {
            mRecipientTextView.removeRecipientEntry(item.getRecipientEntry());
        }
    }

    @Override // From ContactListItemView.HostInterface
    public boolean isContactSelected(final ContactListItemData item) {
        return mSelectedPhoneNumbers != null &&
                mSelectedPhoneNumbers.contains(PhoneUtils.getDefault().getCanonicalBySystemLocale(
                        item.getRecipientEntry().getDestination()));
    }

    /**
     * Call this immediately after attaching the fragment, or when there's a ui state change that
     * changes our host (i.e. restore from saved instance state).
     */
    public void setHost(final ContactPickerFragmentHost host) {
        mHost = host;
    }

    public void setContactPickingMode(final int mode, final boolean animate) {
        if (mContactPickingMode != mode) {
            // Guard against impossible transitions.
            Assert.isTrue(getConditionMode(mode));

            mContactPickingMode = mode;
            updateVisualsForContactPickingMode(animate);
            if (mIsFist) {//Bkav QuangNDb luu trang thai first pick de xu ly
                mFistPickingMode = mode;
                mIsFist = false;
            }
        }
    }

    protected boolean getConditionMode(int mode) {
        return (mContactPickingMode == MODE_UNDEFINED) ||
                (mContactPickingMode == MODE_PICK_INITIAL_CONTACT && mode == MODE_CHIPS_ONLY) ||
                (mContactPickingMode == MODE_CHIPS_ONLY && mode == MODE_PICK_MORE_CONTACTS) ||
                (mContactPickingMode == MODE_PICK_MORE_CONTACTS && mode == MODE_PICK_MAX_PARTICIPANTS) ||
                (mContactPickingMode == MODE_PICK_MAX_PARTICIPANTS && mode == MODE_PICK_MORE_CONTACTS);
    }


    protected void showImeKeyboard() {
        Assert.notNull(mRecipientTextView);
        mRecipientTextView.requestFocus();

        // showImeKeyboard() won't work until the layout is ready, so wait until layout is complete
        // before showing the soft keyboard.
        UiUtils.doOnceAfterLayoutChange(mRootView, new Runnable() {
            @Override
            public void run() {
                final Activity activity = getActivity();
                if (activity != null) {
                    ImeUtil.get().showImeKeyboard(activity, mRecipientTextView);
                    hideMediaPicker();
                }
            }
        });
        mRecipientTextView.invalidate();
    }


    protected Menu mMenu;//Bkav QuangNDb menu cua fragment
    protected MenuItem mConfirmParticipantsItem;
    protected void updateVisualsForContactPickingMode(final boolean animate) {
        // Don't update visuals if the visuals haven't been inflated yet.
        if (mRootView != null) {
            final Menu menu = getMenu();
            if (menu == null) {
                return;
            }
            final MenuItem addMoreParticipantsItem = menu.findItem(
                    R.id.action_add_more_participants);
            mConfirmParticipantsItem = menu.findItem(
                    R.id.action_confirm_participants);
            switch (mContactPickingMode) {
                case MODE_PICK_INITIAL_CONTACT:
                    addMoreParticipantsItem.setVisible(false);
                    mConfirmParticipantsItem.setVisible(false);
                    // BKAV quangNDb tach code set visible view pager
                    setViewPagerVisible(View.VISIBLE);
                    setComposeDividerVisible(View.INVISIBLE);
                    if (mRecipientTextView != null) {
                        mRecipientTextView.setEnabled(true);
                    }
                    resetConversation();//Bkav QuangNDb reset lai gia tri conversation id
//                    showImeKeyboard();// QuangB sua tam, xem lai sau
                    break;
                case MODE_CREATE_GROUP_PARTICIPANTS:
                    // Bkav QuangNDb them case nay khong lam gi va override lai o class con
                    showStateGroupMode(addMoreParticipantsItem, mConfirmParticipantsItem);
                    break;

                case MODE_CHIPS_ONLY:
                    if (animate) {
                        if (mPendingExplodeView == null) {
                            // The user didn't click on any contact item, so use the toolbar as
                            // the view to "explode."
                            mPendingExplodeView = mToolbar;
                        }
                        startExplodeTransitionForContactLists(false /* show */);

                        ViewGroupItemVerticalExplodeAnimation.startAnimationForView(
                                mCustomHeaderViewPager, mPendingExplodeView, mRootView,
                                true /* snapshotView */, UiUtils.COMPOSE_TRANSITION_DURATION);
                        showHideContactPagerWithAnimation(false /* show */);
                    } else {
                        setViewPagerVisible(View.GONE);
                    }

                    addMoreParticipantsItem.setVisible(true);
                    mConfirmParticipantsItem.setVisible(false);
                    setComposeDividerVisible(View.VISIBLE);
                    mRecipientTextView.setEnabled(true);
                    break;

                case MODE_PICK_MORE_CONTACTS:
                    if (animate) {
                        // Correctly set the start visibility state for the view pager and
                        // individual list items (hidden initially), so that the transition
                        // manager can properly track the visibility change for the explode.
                        setViewPagerVisible(View.VISIBLE);
                        toggleContactListItemsVisibilityForPendingTransition(false /* show */);
                        startExplodeTransitionForContactLists(true /* show */);
                    }
                    addMoreParticipantsItem.setVisible(false);
                    mConfirmParticipantsItem.setVisible(true);
                    setViewPagerVisible(View.VISIBLE);
                    setComposeDividerVisible(View.INVISIBLE);
                    mRecipientTextView.setEnabled(true);
                    showAddKeyboard();
                    break;

                case MODE_PICK_MAX_PARTICIPANTS:
                    addMoreParticipantsItem.setVisible(false);
                    mConfirmParticipantsItem.setVisible(true);
                    setViewPagerVisible(View.VISIBLE);
                    setComposeDividerVisible(View.INVISIBLE);
                    // TODO: Verify that this is okay for accessibility
                    mRecipientTextView.setEnabled(false);
                    break;

                default:
                    Assert.fail("Unsupported contact picker mode!");
                    break;
            }
            updateTextInputButtonsVisibility();
        }
    }



    protected void updateTextInputButtonsVisibility() {
        final Menu menu = getMenu();
        if (menu == null) {
            return;
        }
        final MenuItem keypadToggleItem = menu.findItem(R.id.action_ime_dialpad_toggle);
        final MenuItem deleteTextItem = menu.findItem(R.id.action_delete_text);
        if (getConditionShowKeyboardIcon()) {
            if (TextUtils.isEmpty(mRecipientTextView.getText())) {
                deleteTextItem.setVisible(false);
                keypadToggleItem.setVisible(true);
            } else {
                deleteTextItem.setVisible(true);
                keypadToggleItem.setVisible(false);
            }
        } else {
            deleteTextItem.setVisible(false);
            keypadToggleItem.setVisible(false);
        }
    }

    protected void updateTextInputTextChange() {
        final Menu menu = getMenu();
        if (menu == null) {
            return;
        }
        final MenuItem keypadToggleItem = menu.findItem(R.id.action_ime_dialpad_toggle);
        final MenuItem deleteTextItem = menu.findItem(R.id.action_delete_text);
        if (keypadToggleItem == null || deleteTextItem == null) {
            return;
        }
        if (getConditionShowKeyboardIcon()) {
            if (TextUtils.isEmpty(mRecipientTextView.getText())) {
                deleteTextItem.setVisible(false);
                keypadToggleItem.setVisible(true);
            } else {
                deleteTextItem.setVisible(true);
                keypadToggleItem.setVisible(false);
            }
        } else {
            deleteTextItem.setVisible(false);
            keypadToggleItem.setVisible(false);
        }
    }

    protected ProgressDialog mProgressDialog;
    public void maybeGetOrCreateConversation() {
        final ArrayList<ParticipantData> participants = getParticipantList();//Bkav QuangNDb tach code doan lay participant
        if (ContactPickerData.isTooManyParticipants(participants.size())) {
            UiUtils.showToast(R.string.too_many_participants);
        } else if (participants.size() > 0 && mMonitor == null) {
            if (participants.size() > 10) {
                mProgressDialog = new ProgressDialog(getContext());
                mProgressDialog.setTitle(R.string.notify_wait_conversation_init);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
            }
            mMonitor = GetOrCreateConversationAction.getOrCreateConversation(participants,
                    null, this);
        }
    }


//    private boolean mIsFirst = true;
    /**
     * Watches changes in contact chips to determine possible state transitions (e.g. creating
     * the initial conversation, adding more participants or finish the current conversation)
     */
    @Override
    public void onContactChipsChanged(final int oldCount, final int newCount) {
        Assert.isTrue(oldCount != newCount);
        if (getConditionChangeToAddMoreParticipantState()) {
            // Initial picking mode. Start a conversation once a recipient has been picked.
            maybeGetOrCreateConversation();
        } else if (mContactPickingMode == MODE_CHIPS_ONLY) {
            // oldCount == 0 means we are restoring from savedInstanceState to add the existing
            // chips, don't switch to "add more participants" mode in this case.
            if (oldCount > 0 && mRecipientTextView.isFocused()) {
                // Chips only mode. The user may have picked an additional contact or deleted the
                // only existing contact. Either way, switch to picking more participants mode.
                mHost.onInitiateAddMoreParticipants();
            }
        } else if (mContactPickingMode == MODE_CREATE_GROUP_PARTICIPANTS) {
            //Bkav QuangNDb change state khi o group
//            mHost.changeUiState(ConversationActivityUiState.STATE_CONTACT_PICKER_ONLY_ADD_MORE_CONTACTS);
            setContactPickingMode(MODE_PICK_MORE_CONTACTS, false);
            handlePaste();
        }
        mHost.onParticipantCountChanged(ContactPickerData.getCanAddMoreParticipants(newCount));

        // Refresh our local copy of the selected chips set to keep it up-to-date.
        mSelectedPhoneNumbers =  mRecipientTextView.getSelectedDestinations();
        invalidateContactLists();
    }

    /**Bkav QuangNDb xu ly viec paste*/
    protected void handlePaste() {

    }


    /**
     * Listens for notification that invalid contacts have been removed during resolving them.
     * These contacts were not local contacts, valid email, or valid phone numbers
     */
    @Override
    public void onInvalidContactChipsPruned(final int prunedCount) {
        Assert.isTrue(prunedCount > 0);
        // Bkav QuangNDb tach code doan invalid toast contact
        showInvalidContactToast(prunedCount);
    }


    /**
     * Listens for notification that the user has pressed enter/done on the keyboard with all
     * contacts in place and we should create or go to the existing conversation now
     */
    @Override
    public void onEntryComplete() {
        if (getConditionComplete()) {
            // Avoid multiple calls to create in race cases (hit done right after selecting contact)
            onComplete();
        }
    }


    @Override
    public boolean onPasteParticipant(String participants) {
        return false;
    }

    @Override
    public void onResumeRequest() {

    }


    protected void invalidateContactLists() {
        mAllContactsListViewHolder.invalidateList();
        mFrequentContactsListViewHolder.invalidateList();
    }

    /**
     * Kicks off a scene transition that animates visibility changes of individual contact list
     * items via explode animation.
     * @param show whether the contact lists are to be shown or hidden.
     */
    protected void startExplodeTransitionForContactLists(final boolean show) {
        if (!OsUtil.isAtLeastL()) {
            // Explode animation is not supported pre-L.
            return;
        }
        final Explode transition = new Explode();
        final Rect epicenter = mPendingExplodeView == null ? null :
            UiUtils.getMeasuredBoundsOnScreen(mPendingExplodeView);
        transition.setDuration(UiUtils.COMPOSE_TRANSITION_DURATION);
        transition.setInterpolator(UiUtils.EASE_IN_INTERPOLATOR);
        transition.setEpicenterCallback(new EpicenterCallback() {
            @Override
            public Rect onGetEpicenter(final Transition transition) {
                return epicenter;
            }
        });

        // Kick off the delayed scene explode transition. Anything happens after this line in this
        // method before the next frame will be tracked by the transition manager for visibility
        // changes and animated accordingly.
        TransitionManager.beginDelayedTransition(mCustomHeaderViewPager,
                transition);

        toggleContactListItemsVisibilityForPendingTransition(show);
    }

    /**
     * Toggle the visibility of contact list items in the contact lists for them to be tracked by
     * the transition manager for pending explode transition.
     */
    protected void toggleContactListItemsVisibilityForPendingTransition(final boolean show) {
        if (!OsUtil.isAtLeastL()) {
            // Explode animation is not supported pre-L.
            return;
        }
        mAllContactsListViewHolder.toggleVisibilityForPendingTransition(show, mPendingExplodeView);
        mFrequentContactsListViewHolder.toggleVisibilityForPendingTransition(show,
                mPendingExplodeView);
    }

    protected void showHideContactPagerWithAnimation(final boolean show) {
        final boolean isPagerVisible = (mCustomHeaderViewPager.getVisibility() == View.VISIBLE);
        if (show == isPagerVisible) {
            return;
        }

        mCustomHeaderViewPager.animate().alpha(show ? 1F : 0F)
            .setStartDelay(!show ? UiUtils.COMPOSE_TRANSITION_DURATION : 0)
            .withStartAction(new Runnable() {
                @Override
                public void run() {
                    mCustomHeaderViewPager.setVisibility(View.VISIBLE);
                    mCustomHeaderViewPager.setAlpha(show ? 0F : 1F);
                }
            })
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    mCustomHeaderViewPager.setVisibility(show ? View.VISIBLE : View.GONE);
                    mCustomHeaderViewPager.setAlpha(1F);
                }
            });
    }

    @Override
    public void onContactCustomColorLoaded(final ContactPickerData data) {
        mBinding.ensureBound(data);
        invalidateContactLists();
    }

    public void updateActionBar(final ActionBar actionBar) {
        // Hide the action bar for contact picker mode. The custom ToolBar containing chips UI
        // etc. will take the spot of the action bar.
        actionBar.hide();
        updateColorStatusBar();
    }


    protected GetOrCreateConversationActionMonitor mMonitor;

    @Override
    @RunsOnMainThread
    public void onGetOrCreateConversationSucceeded(final ActionMonitor monitor,
            final Object data, final String conversationId) {
        Assert.isTrue(monitor == mMonitor);
        Assert.isTrue(conversationId != null);

//        mRecipientTextView.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE |
//                InputType.TYPE_CLASS_TEXT);
        mHost.onGetOrCreateNewConversation(conversationId);

        mMonitor = null;
    }

    @Override
    @RunsOnMainThread
    public void onGetOrCreateConversationFailed(final ActionMonitor monitor,
            final Object data) {
        Assert.isTrue(monitor == mMonitor);
        LogUtil.e(LogUtil.BUGLE_TAG, "onGetOrCreateConversationFailed");
        mMonitor = null;
    }

    /**
     * -----------------------------------------------------BKAV------------------------------------------------------
     * Bkav QuangNDb tach code doan tao conversation de override lai class con
     */
    protected void onComplete() {
        maybeGetOrCreateConversation();
    }

    /**
     * Bkav QuangNDb tach code doan  lay id resource de override lai
     */
    protected int getIdLayout() {
        return R.layout.contact_picker_fragment;
    }

    /**
     * Bkav QuangNDb tach code doan init view pager
     */
    protected void initViewPager(View view) {
        final CustomHeaderPagerViewHolder[] viewHolders = {
                mFrequentContactsListViewHolder,
                mAllContactsListViewHolder };

        mCustomHeaderViewPager = (CustomHeaderViewPager) view.findViewById(R.id.contact_pager);
        mCustomHeaderViewPager.setViewHolders(viewHolders);
        mCustomHeaderViewPager.setViewPagerTabHeight(CustomHeaderViewPager.DEFAULT_TAB_STRIP_SIZE);
        mCustomHeaderViewPager.setBackgroundColor(getResources()
                .getColor(R.color.contact_picker_background));

        // The view pager defaults to the frequent contacts page.
        mCustomHeaderViewPager.setCurrentItem(0);
        mComposeDivider = view.findViewById(R.id.compose_contact_divider);
    }

    /**
     * Bkav QuangNDb tach code doan set visible view pager
     */
    protected void setViewPagerVisible(int visible) {
        mCustomHeaderViewPager.setVisibility(visible);
    }
    /**
     * Bkav QuangNDb tach code doan set visible view pager
     */
    protected void setComposeDividerVisible(int visible) {
        mComposeDivider.setVisibility(visible);
    }

    /**
     * Bkav QuangNDb tach code doan init loader
     */
    protected void initLoader() {
        mAllContactsListViewHolder = new AllContactsListViewHolder(getActivity(), this);
        mFrequentContactsListViewHolder = new FrequentContactsListViewHolder(getActivity(), this);

        if (ContactUtil.hasReadContactsPermission()) {
            mBinding.bind(DataModel.get().createContactPickerData(getActivity(), this));
            mBinding.getData().init(getLoaderManager(), mBinding);
        }
    }

    /**
     * Bkav QuangNDb tach code doan set adapter de override lai
     */
    protected void setRecipientTextAdapter() {
        mRecipientTextView.setAdapter(new ContactRecipientAdapter(getActivity(), this));
    }

    /**
     * BKav QUangNDB them ham check text change de quey list
     */
    protected void onRecipientTextChanged(CharSequence s) {
        // Bkav QuangNDb khong lam gi
    }

    /**
     * Bkav QuangNDb tach code doan khoi tao ContactRecipientAutoCompleteView
     */
    protected ContactRecipientAutoCompleteView initRecipient(View view) {
        return (ContactRecipientAutoCompleteView)
                view.findViewById(R.id.recipient_text_view);
    }

    /**
     * Bkav QuangNDb tach code doan get Id res icon ime
     */
    protected int getIdResIconIme() {
        return R.drawable.ic_ime_light;
    }
    /**
     * Bkav QuangNDb tach code doan get Id res menu
     */
    protected int getIdResMenu() {
        return R.menu.compose_menu;
    }
    /**
     * Bkav QUangNDb tach code doan update color status bar
     */
    protected void updateColorStatusBar() {
        UiUtils.setStatusBarColor(getActivity(),
                getResources().getColor(R.color.compose_notification_bar_background));
    }

    /**
     * Bkav QuangNDb tach code doan get id ref dial pad
     */
    protected int getIdResDialPad() {
        return R.drawable.ic_numeric_dialpad;
    }
    /**
     * tach code doan get id res cua nut back
     */
    protected int getIdResBackIcon() {
        return R.drawable.ic_arrow_back_light;
    }
    protected void showAddKeyboard() {
        showImeKeyboard();
    }

    protected void showStateGroupMode(MenuItem addMoreParticipantsItem, MenuItem confirmParticipantsItem) {
        // Bkav QuangNDb khong lam gi
    }

    protected boolean getConditionShowKeyboardIcon() {
        return mContactPickingMode == MODE_PICK_INITIAL_CONTACT;
    }

    protected boolean getConditionChangeToAddMoreParticipantState() {
        return mContactPickingMode == MODE_PICK_INITIAL_CONTACT;
    }

    protected boolean getConditionComplete() {
        return mContactPickingMode == MODE_PICK_INITIAL_CONTACT ||
                mContactPickingMode == MODE_PICK_MORE_CONTACTS ||
                mContactPickingMode == MODE_PICK_MAX_PARTICIPANTS;
    }

    /**
     * BKav QUangNDb tach code doan invalid toast contact
     */
    protected void showInvalidContactToast(int prunedCount) {
        UiUtils.showToast(R.plurals.add_invalid_contact_error, prunedCount);
    }

    /**
     * BKav QuangNDb tach code doan bam vao icon menu remove
     */
    protected void deleteAllText() {
        Assert.equals(MODE_PICK_INITIAL_CONTACT, mContactPickingMode);
        mRecipientTextView.setText("");
    }

    /**
     * QuangNDb khoi tao compose message view
     * @param savedInstanceState
     */
    protected void setUpComposeMessageView(Bundle savedInstanceState) {

    }

    /**
     * Bkav QuangNDb Tach code doan confirm button
     */
    protected void clickConfirmButton() {
        mRecipientTextView.clearFocus();
        maybeGetOrCreateConversation();
    }

    /**Bkav QuangNDb khoi tao view chua list tin nhan
     * @param view*/
    protected void initListMessage(View view) {
        //custom o class con
    }

    /**Bkav QuangNDb get menu*/
    protected Menu getMenu() {
        return mToolbar.getMenu();
    }

    /**Bkav QuangNDb set host cho picker*/
    public void setBtalkPickerFragmentHost(Object o) {
    }

    /**Bkav QuangNDb set focus cho conversation*/
    public void setConversationFocus() {
    }

    /**Bkav QuangNDb luu lai trang thai thoi gian va conversation_id de khi bam home quay lai van du trang thai*/
    public void saveStatus() {

    }

    /**Bkav QuangNDb reset lai converstion id khi ng dung chuyen lai ve che do init*/
    protected void resetConversation() {

    }

    /**Bkav QuangNDb check xem co o trang thai deep shortcut show hay khong*/
    public boolean isDeepShortcutTouch(MotionEvent ev) {
        return false;
    }

    @Override
    public void onInsertEntry(RecipientEntry recipientEntry) {

    }

    @Override
    public void onRemoveEntry(RecipientEntry recipientEntry) {

    }

    /**Bkav QuangNDb set incoming draf*/
    public void setIncomingDraft(final MessageData draftData) {
    }

    /**Bkav QuangNDb tach code doan lay list participant de custom lai*/
    protected ArrayList<ParticipantData> getParticipantList() {
        return mRecipientTextView.getRecipientParticipantDataForConversationCreation();
    }

    // Bkav HuyNQN kiem tra xem danh sach esim co dang hien thi hay khong
    public boolean isListEsimTouch(MotionEvent event){
        return false;
    }

    public void hideMediaPicker(){

    }
}
