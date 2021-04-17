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

package com.android.messaging.ui.conversation;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.text.BidiFormatter;
import android.support.v4.text.TextDirectionHeuristicsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.action.InsertNewMessageAction;
import com.android.messaging.datamodel.binding.Binding;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.binding.ImmutableBindingRef;
import com.android.messaging.datamodel.data.ConversationData;
import com.android.messaging.datamodel.data.ConversationData.ConversationDataListener;
import com.android.messaging.datamodel.data.ConversationMessageData;
import com.android.messaging.datamodel.data.ConversationParticipantsData;
import com.android.messaging.datamodel.data.DraftMessageData;
import com.android.messaging.datamodel.data.DraftMessageData.DraftMessageDataListener;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.datamodel.data.SubscriptionListData.SubscriptionListEntry;
import com.android.messaging.ui.AttachmentPreview;
import com.android.messaging.ui.BugleActionBarActivity;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.ui.SnackBar;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.animation.PopupTransitionAnimation;
import com.android.messaging.ui.contact.AddContactsConfirmationDialog;
import com.android.messaging.ui.conversation.ComposeMessageView.IComposeMessageViewHost;
import com.android.messaging.ui.conversation.ConversationInputManager.ConversationInputHost;
import com.android.messaging.ui.conversation.ConversationMessageView.ConversationMessageViewHost;
import com.android.messaging.ui.mediapicker.MediaPicker;
import com.android.messaging.util.AccessibilityUtil;
import com.android.messaging.util.Assert;
import com.android.messaging.util.AvatarUriUtil;
import com.android.messaging.util.ChangeDefaultSmsAppHelper;
import com.android.messaging.util.ContentType;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.SafeAsyncTask;
import com.android.messaging.util.TextUtil;
import com.android.messaging.util.UiUtils;
import com.android.messaging.util.UriUtil;
import com.google.common.annotations.VisibleForTesting;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows a list of messages/parts comprising a conversation.
 */
public class ConversationFragment extends Fragment implements ConversationDataListener,
        IComposeMessageViewHost, ConversationMessageViewHost, ConversationInputHost,
        DraftMessageDataListener {


    public interface ConversationFragmentHost extends ImeUtil.ImeStateHost {
        void onStartComposeMessage();

        void onConversationMetadataUpdated();

        boolean shouldResumeComposeMessage();

        void onFinishCurrentConversation();

        void invalidateActionBar();

        ActionMode startActionMode(ActionMode.Callback callback);

        void dismissActionMode();

        ActionMode getActionMode();

        void onConversationMessagesUpdated(int numberOfMessages);

        void onConversationParticipantDataLoaded(int numberOfParticipants);

        boolean isActiveAndFocused();

        // Bkav QuangNDb them ham change state UISTATE
        void changeState();
    }

    public static final String FRAGMENT_TAG = "conversation";

    static final int REQUEST_CHOOSE_ATTACHMENTS = 2;
    private static final int JUMP_SCROLL_THRESHOLD = 15;
    // We animate the message from draft to message list, if we the message doesn't show up in the
    // list within this time limit, then we just do a fade in animation instead
    public static final int MESSAGE_ANIMATION_MAX_WAIT = 500;

    protected ComposeMessageView mComposeMessageView;
    protected RecyclerView mRecyclerView;
    protected ConversationMessageAdapter mAdapter;
    private ConversationFastScroller mFastScroller;

    private View mConversationComposeDivider;
    private ChangeDefaultSmsAppHelper mChangeDefaultSmsAppHelper;

    protected String mConversationId;
    // If the fragment receives a draft as part of the invocation this is set
    private MessageData mIncomingDraft;

    // This binding keeps track of our associated ConversationData instance
    // A binding should have the lifetime of the owning component,
    //  don't recreate, unbind and bind if you need new data
    @VisibleForTesting
    protected final Binding<ConversationData> mBinding = BindingBase.createBinding(this);

    // Saved Instance State Data - only for temporal data which is nice to maintain but not
    // critical for correctness.
    private static final String SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY = "conversationViewState";
    private Parcelable mListState;

    protected ConversationFragmentHost mHost;

    protected List<Integer> mFilterResults;

    // The minimum scrolling distance between RecyclerView's scroll change event beyong which
    // a fling motion is considered fast, in which case we'll delay load image attachments for
    // perf optimization.
    private int mFastFlingThreshold;

    // ConversationMessageView that is currently selected
    private ConversationMessageView mSelectedMessage;

    // Attachment data for the attachment within the selected message that was long pressed
    protected MessagePartData mSelectedAttachment;

    protected ViewGroup mParentView;

//    protected LinearLayout mListEmpty;
    // Bkav HienDTk: Check xem co dang thuc hien hang dong search khong
    public boolean mActionSearch;

    // Bkav TienNAb: tao bien luu list MessageData
    protected List<ConversationMessageData> mConversationMessageDatas = new ArrayList<>();

    // Normally, as soon as draft message is loaded, we trust the UI state held in
    // ComposeMessageView to be the only source of truth (incl. the conversation self id). However,
    // there can be external events that forces the UI state to change, such as SIM state changes
    // or SIM auto-switching on receiving a message. This receiver is used to receive such
    // local broadcast messages and reflect the change in the UI.
    private final BroadcastReceiver mConversationSelfIdChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String conversationId =
                    intent.getStringExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID);
            final String selfId =
                    intent.getStringExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_SELF_ID);
            Assert.notNull(conversationId);
            Assert.notNull(selfId);
            if (TextUtils.equals(mBinding.getData().getConversationId(), conversationId)) {
                mComposeMessageView.updateConversationSelfIdOnExternalChange(selfId);
            }
        }
    };

    // Flag to prevent writing draft to DB on pause
    private boolean mSuppressWriteDraft;

    // Indicates whether local draft should be cleared due to external draft changes that must
    // be reloaded from db
    private boolean mClearLocalDraft;
    private ImmutableBindingRef<DraftMessageData> mDraftMessageDataModel;

    private boolean isScrolledToBottom() {
        if (mRecyclerView.getChildCount() == 0) {
            return true;
        }
        final View lastView = mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1);
        int lastVisibleItem = ((LinearLayoutManager) mRecyclerView
                .getLayoutManager()).findLastVisibleItemPosition();
        if (lastVisibleItem < 0) {
            // If the recyclerView height is 0, then the last visible item position is -1
            // Try to compute the position of the last item, even though it's not visible
            final long id = mRecyclerView.getChildItemId(lastView);
            final RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForItemId(id);
            if (holder != null) {
                lastVisibleItem = holder.getAdapterPosition();
            }
        }
        final int totalItemCount = mRecyclerView.getAdapter().getItemCount();
        final boolean isAtBottom = (lastVisibleItem + 1 == totalItemCount);
        return isAtBottom && lastView.getBottom() <= mRecyclerView.getHeight();
    }

    private void scrollToBottom(final boolean smoothScroll) {
        if (mAdapter.getItemCount() > 0) {
            scrollToPosition(mAdapter.getItemCount() - 1, smoothScroll);
        }
    }

    private int mScrollToDismissThreshold;
    private final RecyclerView.OnScrollListener mListScrollListener =
            new RecyclerView.OnScrollListener() {
                // Keeps track of cumulative scroll delta during a scroll event, which we may use to
                // hide the media picker & co.
                private int mCumulativeScrollDelta;
                private boolean mScrollToDismissHandled;
                private boolean mWasScrolledToBottom = true;
                private boolean mWasScrolledToTop = false;
                private int mScrollState = RecyclerView.SCROLL_STATE_IDLE;

                @Override
                public void onScrollStateChanged(final RecyclerView view, final int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        // Reset scroll states.
                        mCumulativeScrollDelta = 0;
                        mScrollToDismissHandled = false;
                    } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        mRecyclerView.getItemAnimator().endAnimations();
                    }
                    mScrollState = newState;
                }

                @Override
                public void onScrolled(final RecyclerView view, final int dx, final int dy) {
                    if (mScrollState == RecyclerView.SCROLL_STATE_DRAGGING &&
                            !mScrollToDismissHandled) {
                        mCumulativeScrollDelta += dy;
                        // Dismiss the keyboard only when the user scroll up (into the past).
                        if (mCumulativeScrollDelta < -mScrollToDismissThreshold) {
                            mComposeMessageView.hideAllComposeInputs(false /* animate */);
                            mScrollToDismissHandled = true;
                        }
                    }
                    if (mWasScrolledToBottom != isScrolledToBottom()) {
                        mConversationComposeDivider.animate().alpha(isScrolledToBottom() ? 0 : 1);
                        mWasScrolledToBottom = isScrolledToBottom();
                    }
                    // Bkav QuangNDb them doan xu ly khi recycler view scroll den top
                    if (mWasScrolledToTop != isScrolledToTop()) {
                        setAlphaShadowActionbar(isScrolledToTop());
                        mWasScrolledToTop = isScrolledToTop();
                    }
                }
            };

    private final ActionMode.Callback mMessageActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(final ActionMode actionMode, final Menu menu) {
            if (mSelectedMessage == null) {
                return false;
            }
            final ConversationMessageData data = mSelectedMessage.getData();
            final MenuInflater menuInflater = getActivity().getMenuInflater();
            menuInflater.inflate(R.menu.conversation_fragment_select_menu, menu);
            menu.findItem(R.id.action_download).setVisible(data.getShowDownloadMessage());
            menu.findItem(R.id.action_send).setVisible(data.getShowResendMessage());

            // ShareActionProvider does not work with ActionMode. So we use a normal menu item.
            menu.findItem(R.id.share_message_menu).setVisible(data.getCanForwardMessage());
            menu.findItem(R.id.save_attachment).setVisible(mSelectedAttachment != null);
            menu.findItem(R.id.forward_message_menu).setVisible(data.getCanForwardMessage());

            // TODO: We may want to support copying attachments in the future, but it's
            // unclear which attachment to pick when we make this context menu at the message level
            // instead of the part level
            menu.findItem(R.id.copy_text).setVisible(data.getCanCopyMessageToClipboard());

            return true;
        }

        @Override
        public boolean onPrepareActionMode(final ActionMode actionMode, final Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode actionMode, final MenuItem menuItem) {
            final ConversationMessageData data = mSelectedMessage.getData();
            final String messageId = data.getMessageId();
            int i = menuItem.getItemId();
            if (i == R.id.save_attachment) {
                if (OsUtil.hasStoragePermission()) {
                    final SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask(
                            getActivity());
                    for (final MessagePartData part : data.getAttachments()) {
                        saveAttachmentTask.addAttachmentToSave(part.getContentUri(),
                                part.getContentType());
                    }
                    if (saveAttachmentTask.getAttachmentCount() > 0) {
                        saveAttachmentTask.executeOnThreadPool();
                        mHost.dismissActionMode();
                    }
                } else {
                    getActivity().requestPermissions(
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
                return true;
            } else if (i == R.id.action_delete_message) {
                if (mSelectedMessage != null) {
                    deleteMessage(messageId);
                }
                return true;
            } else if (i == R.id.action_download) {
                if (mSelectedMessage != null) {
                    retryDownload(messageId);
                    mHost.dismissActionMode();
                }
                return true;
            } else if (i == R.id.action_send) {
                if (mSelectedMessage != null) {
                    retrySend(messageId);
                    mHost.dismissActionMode();
                }
                return true;
            } else if (i == R.id.copy_text) {
                Assert.isTrue(data.hasText());
                final ClipboardManager clipboard = (ClipboardManager) getActivity()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(
                        ClipData.newPlainText(null /* label */, data.getText()));
                mHost.dismissActionMode();
                return true;
            } else if (i == R.id.details_menu) {
                MessageDetailsDialog.show(
                        getActivity(), data, mBinding.getData().getParticipants(),
                        mBinding.getData().getSelfParticipantById(data.getSelfParticipantId()));
                mHost.dismissActionMode();
                return true;
            } else if (i == R.id.share_message_menu) {
                shareMessage(data);
                mHost.dismissActionMode();
                return true;
            } else if (i == R.id.forward_message_menu) {// TODO: Currently we are forwarding one part at a time, instead of
                // the entire message. Change this to forwarding the entire message when we
                // use message-based cursor in conversation.
                final MessageData message = mBinding.getData().createForwardedMessage(data);
                UIIntents.get().launchForwardMessageActivity(getActivity(), message);
                mHost.dismissActionMode();
                return true;
            }
            return false;
        }

        private void shareMessage(final ConversationMessageData data) {
            // Figure out what to share.
            MessagePartData attachmentToShare = mSelectedAttachment;
            // If the user long-pressed on the background, we will share the text (if any)
            // or the first attachment.
            if (mSelectedAttachment == null
                    && TextUtil.isAllWhitespace(data.getText())) {
                final List<MessagePartData> attachments = data.getAttachments();
                if (attachments.size() > 0) {
                    attachmentToShare = attachments.get(0);
                }
            }

            final Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            if (attachmentToShare == null) {
                shareIntent.putExtra(Intent.EXTRA_TEXT, data.getText());
                shareIntent.setType("text/plain");
            } else {
                shareIntent.putExtra(
                        Intent.EXTRA_STREAM, attachmentToShare.getContentUri());
                shareIntent.setType(attachmentToShare.getContentType());
            }
            final CharSequence title = getResources().getText(R.string.action_share);
            startActivity(Intent.createChooser(shareIntent, title));
        }

        @Override
        public void onDestroyActionMode(final ActionMode actionMode) {
            selectMessage(null);
        }
    };

    /**
     * {@inheritDoc} from Fragment
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: test unactive permission");



        mFastFlingThreshold = getResources().getDimensionPixelOffset(
                R.dimen.conversation_fast_fling_threshold);
        initAdapter();
    }


    /**
     * setConversationInfo() may be called before or after onCreate(). When a user initiate a
     * conversation from compose, the ConversationActivity creates this fragment and calls
     * setConversationInfo(), so it happens before onCreate(). However, when the activity is
     * restored from saved instance state, the ConversationFragment is created automatically by
     * the fragment, before ConversationActivity has a chance to call setConversationInfo(). Since
     * the ability to start loading data depends on both methods being called, we need to start
     * loading when onActivityCreated() is called, which is guaranteed to happen after both.
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Delay showing the message list until the participant list is loaded.
        mRecyclerView.setVisibility(View.INVISIBLE);
        mBinding.ensureBound();
        mBinding.getData().init(getLoaderManager(), mBinding);

        // Build the input manager with all its required dependencies and pass it along to the
        // compose message view.
        // Bkav QuangNDb tach code doan khoi tao input manager de override lai o class con
        inputManager = initInputManager(savedInstanceState);
        mComposeMessageView.setInputManager(inputManager);
        mComposeMessageView.setConversationDataModel(BindingBase.createBindingReference(mBinding));
        if (mSelfSimId != null) {
            mComposeMessageView.updateConversationSelfIdOnExternalChange(mSelfSimId);
        }
        mHost.invalidateActionBar();

        mDraftMessageDataModel =
                BindingBase.createBindingReference(mComposeMessageView.getDraftDataModel());
        mDraftMessageDataModel.getData().addListener(this);
    }

    // Bkav HuyNQN de protected dung lai o lop con
    protected ConversationInputManager inputManager;


    public void onAttachmentChoosen() {
        // Attachment has been choosen in the AttachmentChooserActivity, so clear local draft
        // and reload draft on resume.
        mClearLocalDraft = true;
    }

    private int getScrollToMessagePosition() {
        final Activity activity = getActivity();
        if (activity == null) {
            return -1;
        }

        final Intent intent = activity.getIntent();
        if (intent == null) {
            return -1;
        }

        return intent.getIntExtra(UIIntents.UI_INTENT_EXTRA_MESSAGE_POSITION, -1);
    }

    private void clearScrollToMessagePosition() {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        final Intent intent = activity.getIntent();
        if (intent == null) {
            return;
        }
        intent.putExtra(UIIntents.UI_INTENT_EXTRA_MESSAGE_POSITION, -1);
    }


    private final Handler mHandler = new Handler();

    /**
     * {@inheritDoc} from Fragment
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Test unactive permission");
        final View view = inflateView(inflater, container);
        mRecyclerView = (RecyclerView) view.findViewById(android.R.id.list);
        final LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setStackFromEnd(true);
        manager.setReverseLayout(false);
        initComposeView(view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setScrollBarSize(0);
//         Bkav QuangNDb tach code doan set animation cho 1 tin nhan moi den
        setRecyclerAnimator();
        mRecyclerView.setAdapter(mAdapter);

//        mListEmpty = (LinearLayout) view.findViewById(R.id.empty);

        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY);
        }

        mConversationComposeDivider = view.findViewById(R.id.conversation_compose_divider);
        mScrollToDismissThreshold = ViewConfiguration.get(getActivity()).getScaledTouchSlop();
        mRecyclerView.addOnScrollListener(mListScrollListener);
        initFastScroll();

        // Bind the compose message view to the DraftMessageData
        mComposeMessageView.bind(DataModel.get().createDraftMessageData(
                mBinding.getData().getConversationId()), this);
        // Bkav QuangNDb them ham init view shadow action bar
        initShadowActionbar(view);
        checkTopVisible();
        mRootView = view;
        return view;
    }


    private static final String TAG = "Btalk";


    protected ConversationMessageView getConversationMessageView(View itemView) {
        return (ConversationMessageView) itemView;
    }


    private void scrollToPosition(final int targetPosition, final boolean smoothScroll) {
        if (smoothScroll) {
            final int maxScrollDelta = JUMP_SCROLL_THRESHOLD;

            final LinearLayoutManager layoutManager =
                    (LinearLayoutManager) mRecyclerView.getLayoutManager();
            final int firstVisibleItemPosition =
                    layoutManager.findFirstVisibleItemPosition();
            final int delta = targetPosition - firstVisibleItemPosition;
            final int intermediatePosition;

            if (delta > maxScrollDelta) {
                intermediatePosition = Math.max(0, targetPosition - maxScrollDelta);
            } else if (delta < -maxScrollDelta) {
                final int count = layoutManager.getItemCount();
                intermediatePosition = Math.min(count - 1, targetPosition + maxScrollDelta);
            } else {
                intermediatePosition = -1;
            }
            if (intermediatePosition != -1) {
                mRecyclerView.scrollToPosition(intermediatePosition);
            }
            mRecyclerView.smoothScrollToPosition(targetPosition);
        } else {
            mRecyclerView.scrollToPosition(targetPosition);
        }
    }

    private int getScrollPositionFromBottom() {
        final LinearLayoutManager layoutManager =
                (LinearLayoutManager) mRecyclerView.getLayoutManager();
        final int lastVisibleItem =
                layoutManager.findLastVisibleItemPosition();
        return Math.max(mAdapter.getItemCount() - 1 - lastVisibleItem, 0);
    }

    /**
     * Display a photo using the Photoviewer component.
     */
    @Override
    public void displayPhoto(final Uri photoUri, final Rect imageBounds, final boolean isDraft) {
        displayPhoto(photoUri, imageBounds, isDraft, mConversationId, getActivity());
    }

    public static void displayPhoto(final Uri photoUri, final Rect imageBounds,
                                    final boolean isDraft, final String conversationId, final Activity activity) {
        final Uri imagesUri =
                isDraft ? MessagingContentProvider.buildDraftImagesUri(conversationId)
                        : MessagingContentProvider.buildConversationImagesUri(conversationId);
        UIIntents.get().launchFullScreenPhotoViewer(
                activity, photoUri, imageBounds, imagesUri);
    }

    protected void selectMessage(final ConversationMessageView messageView) {
        selectMessage(messageView, null /* attachment */);
    }

    protected void selectMessage(final ConversationMessageView messageView,
                                 final MessagePartData attachment) {
        mSelectedMessage = messageView;
        if (mSelectedMessage == null) {
            mAdapter.setSelectedMessage(null);
            mHost.dismissActionMode();
            mSelectedAttachment = null;
            return;
        }
        mSelectedAttachment = attachment;
        mAdapter.setSelectedMessage(messageView.getData().getMessageId());
        // Bkav QuangNDb tach code doan start action mode
        startActionMode();
    }


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mListState != null) {
            outState.putParcelable(SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY, mListState);
        }
        mComposeMessageView.saveInputState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIncomingDraft == null) {
            mComposeMessageView.requestDraftMessage(mClearLocalDraft);
        } else {
            mComposeMessageView.setDraftMessage(mIncomingDraft);
            mIncomingDraft = null;
        }
        mClearLocalDraft = false;

        // On resume, check if there's a pending request for resuming message compose. This
        // may happen when the user commits the contact selection for a group conversation and
        // goes from compose back to the conversation fragment.
        if (mHost.shouldResumeComposeMessage()) {
            mComposeMessageView.resumeComposeMessage();
        }

        setConversationFocus();

        // On resume, invalidate all message views to show the updated timestamp.
        mAdapter.notifyDataSetChanged();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mConversationSelfIdChangeReceiver,
                new IntentFilter(UIIntents.CONVERSATION_SELF_ID_CHANGE_BROADCAST_ACTION));
    }

    void setConversationFocus() {
        if (mHost.isActiveAndFocused()) {
            mBinding.getData().setFocus();
        }
    }


    @Override
    public boolean isAlwaysAskSim() {
        return false;
    }


    @Override
    public boolean isFinishLoadDraf() {
        return false;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if (mHost.getActionMode() != null) {
            return;
        }

        inflateMenu(menu, inflater);

        final ConversationData data = mBinding.getData();

        // Disable the "people & options" item if we haven't loaded participants yet.
        menu.findItem(R.id.action_people_and_options).setEnabled(data.getParticipantsLoaded());

        // See if we can show add contact action.
        final ParticipantData participant = data.getOtherParticipant();
        final boolean addContactActionVisible = (participant != null
                && TextUtils.isEmpty(participant.getLookupKey()));
        menu.findItem(R.id.action_add_contact).setVisible(addContactActionVisible);

        // See if we should show archive or unarchive.
        final boolean isArchived = data.getIsArchived();
        menu.findItem(R.id.action_archive).setVisible(!isArchived);
        menu.findItem(R.id.action_unarchive).setVisible(isArchived);

        // Conditionally enable the phone call button.
        final boolean supportCallAction = (PhoneUtils.getDefault().isVoiceCapable() &&
                data.getParticipantPhoneNumber() != null);

        menu.findItem(R.id.action_call).setVisible(supportCallAction);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_people_and_options) {
            Assert.isTrue(mBinding.getData().getParticipantsLoaded());
            UIIntents.get().launchPeopleAndOptionsActivity(getActivity(), mConversationId);
            return true;
        } else if (i == R.id.action_call) {
            // tach code doan xu ly goi dien cho participant
            makeACall(true); // Bkav HuyNQN luc nao cung hien danh sach lua chon sim
            return true;
        } else if (i == R.id.action_archive) {
            mBinding.getData().archiveConversation(mBinding);
            closeConversation(mConversationId);
            return true;
        } else if (i == R.id.action_unarchive) {
            mBinding.getData().unarchiveConversation(mBinding);
            return true;
        } else if (i == R.id.action_settings) {
            return true;
        } else if (i == R.id.action_add_contact) {
            final ParticipantData participant = mBinding.getData().getOtherParticipant();
            Assert.notNull(participant);
            final String destination = participant.getNormalizedDestination();
            final Uri avatarUri = AvatarUriUtil.createAvatarUri(participant);
            (new AddContactsConfirmationDialog(getActivity(), avatarUri, destination)).show();
            return true;
        } else if (i == R.id.action_delete) {
            if (isReadyForAction()) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getQuantityString(
                                R.plurals.delete_conversations_confirmation_dialog_title, 1))
                        .setPositiveButton(R.string.delete_conversation_confirmation_button,
                                new OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog,
                                                        final int button) {
                                        deleteConversation();
                                    }
                                })
                        .setNegativeButton(R.string.delete_conversation_decline_button, null)
                        .show();
            } else {
                warnOfMissingActionConditions(false /*sending*/,
                        null /*commandToRunAfterActionConditionResolved*/);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected boolean mIsEmptyMessage = false;//Bkav QuangNDb Bien check xem co phai cuoc hoi thoai rong hay khong

    /**
     * {@inheritDoc} from ConversationDataListener
     */
    @Override
    public void onConversationMessagesCursorUpdated(final ConversationData data,
                                                    final Cursor cursor, final ConversationMessageData newestMessage,
                                                    final boolean isSync) {
        mBinding.ensureBound(data);
        if (cursor != null) {
            mIsEmptyMessage = cursor.getCount() == 0;
        }
        // This needs to be determined before swapping cursor, which may change the scroll state.
        final boolean scrolledToBottom = isScrolledToBottom();
        final int positionFromBottom = getScrollPositionFromBottom();

        // If participants not loaded, assume 1:1 since that's the 99% case
        final boolean oneOnOne =
                !data.getParticipantsLoaded() || data.getOtherParticipant() != null;
        mAdapter.setOneOnOne(oneOnOne, false /* invalidate */);

        // Ensure that the action bar is updated with the current data.
        // Bkav QuangNDb tach code doan reset option menu
        resetOptionMenu();
//        mAdapter.changeCursor(cursor);
        // TODO TrungTH tra ve nhu cu, quangndb xem tiep
        final Cursor oldCursor = mAdapter.swapCursor(cursor);
//        final Cursor oldCursor;
//        if (cursor != null && cursor.getCount() > mAdapter.getItemCount()) {
//            oldCursor = mAdapter.changeCursor(cursor);
//        }else {
//            oldCursor = mAdapter.swapCursor(cursor);
//        }

        if (cursor != null && oldCursor == null) {
            if (mListState != null) {
                mRecyclerView.getLayoutManager().onRestoreInstanceState(mListState);
                // RecyclerView restores scroll states without triggering scroll change events, so
                // we need to manually ensure that they are correctly handled.
                mListScrollListener.onScrolled(mRecyclerView, 0, 0);
            }
        }

        if (isSync) {
            // This is a message sync. Syncing messages changes cursor item count, which would
            // implicitly change RV's scroll position. We'd like the RV to keep scrolled to the same
            // relative position from the bottom (because RV is stacked from bottom), so that it
            // stays relatively put as we sync.
            final int position = Math.max(mAdapter.getItemCount() - 1 - positionFromBottom, 0);
            scrollToPosition(position, false /* smoothScroll */);
        } else if (newestMessage != null) {
            // Show a snack bar notification if we are not scrolled to the bottom and the new
            // message is an incoming message.
            if (!scrolledToBottom && newestMessage.getIsIncoming()) {
                // If the conversation activity is started but not resumed (if another dialog
                // activity was in the foregrond), we will show a system notification instead of
                // the snack bar.
                if (mBinding.getData().isFocused()) {
                    UiUtils.showSnackBarWithCustomAction(getActivity(),
                            getView().getRootView(),
                            getString(R.string.in_conversation_notify_new_message_text),
                            SnackBar.Action.createCustomAction(new Runnable() {
                                                                   @Override
                                                                   public void run() {
                                                                       scrollToBottom(true /* smoothScroll */);
                                                                       mComposeMessageView.hideAllComposeInputs(false /* animate */);
                                                                   }
                                                               },
                                    getString(R.string.in_conversation_notify_new_message_action)),
                            null /* interactions */,
                            SnackBar.Placement.above(mComposeMessageView));
                }
            } else {
                // We are either already scrolled to the bottom or this is an outgoing message,
                // scroll to the bottom to reveal it.
                // Don't smooth scroll if we were already at the bottom; instead, we scroll
                // immediately so RecyclerView's view animation will take place.
                scrollToBottom(!scrolledToBottom);
            }
        }

        if (cursor != null) {
            // Bkav HienDTk: neu khong tim thay noi dung tin nhan thi hien thi list empty
            if(cursor.getCount() == 0 && mActionSearch && !TextUtils.isEmpty(getSearchString())){
                showEmptyView(true);
            } else {
                showEmptyView(false);
            }

            mHost.onConversationMessagesUpdated(cursor.getCount());

            // Are we coming from a widget click where we're told to scroll to a particular item?
            final int scrollToPos = getScrollToMessagePosition();
            if (scrollToPos >= 0) {
                if (LogUtil.isLoggable(LogUtil.BUGLE_TAG, LogUtil.VERBOSE)) {
                    LogUtil.v(LogUtil.BUGLE_TAG, "onConversationMessagesCursorUpdated " +
                            " scrollToPos: " + scrollToPos +
                            " cursorCount: " + cursor.getCount());
                }
                // Bkav QuangNDb doi smoothScroll tu true sang false de bo hieu ung scroll di ma hien thi ngay vi tri
                scrollToPosition(scrollToPos, false /*smoothScroll*/);
                clearScrollToMessagePosition();
            }
        }

        mHost.invalidateActionBar();

        // Bkav TienNAb: them du lieu cho list MessageData lay tu cursor
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                final ConversationMessageData conversationMessageData = new ConversationMessageData();
                conversationMessageData.bind(cursor);
                mConversationMessageDatas.add(conversationMessageData);
            } while (cursor.moveToNext());
            cursor.moveToFirst();
        }
    }

    public void showEmptyView(boolean isCheckSearch){
        // Bkav HienDTk: khong lam gi
    }

    public String getSearchString(){
        return null;
    }


    /**
     * {@inheritDoc} from ConversationDataListener
     */
    @Override
    public void onConversationMetadataUpdated(final ConversationData conversationData) {
        mBinding.ensureBound(conversationData);

        if (mSelectedMessage != null && mSelectedAttachment != null) {
            // We may have just sent a message and the temp attachment we selected is now gone.
            // and it was replaced with some new attachment.  Since we don't know which one it
            // is we shouldn't reselect it (unless there is just one) In the multi-attachment
            // case we would just deselect the message and allow the user to reselect, otherwise we
            // may act on old temp data and may crash.
            final List<MessagePartData> currentAttachments = mSelectedMessage.getData().getAttachments();
            if (currentAttachments.size() == 1) {
                mSelectedAttachment = currentAttachments.get(0);
            } else if (!currentAttachments.contains(mSelectedAttachment)) {
                selectMessage(null);
            }
        }
        // Ensure that the action bar is updated with the current data.
        // Bkav QuangNDb comment reset option menu
        resetOptionMenu();
        mHost.onConversationMetadataUpdated();
        mAdapter.notifyDataSetChanged();
    }

    public void setConversationInfo(final Context context, final String conversationId,
                                    final MessageData draftData) {
        // TODO: Eventually I would like the Factory to implement
        // Factory.get().bindConversationData(mBinding, getActivity(), this, conversationId));
        if (!mBinding.isBound()) {
            mConversationId = conversationId;
            mIncomingDraft = draftData;
            mBinding.bind(DataModel.get().createConversationData(context, this, conversationId));
        } else {
            Assert.isTrue(TextUtils.equals(mBinding.getData().getConversationId(), conversationId));
        }
    }

    private String mSelfSimId = null;

    /**
     * QuangNDb truyen du lieu sim sang
     */
    public void updateSelfSimId(String selfSimId) {
        mSelfSimId = selfSimId;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unbind all the views that we bound to data
        if (mComposeMessageView != null) {
            mComposeMessageView.unbind();
        }

        // And unbind this fragment from its data
        mBinding.unbind();
        mConversationId = null;
    }

    public void suppressWriteDraft() {
        mSuppressWriteDraft = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mComposeMessageView != null && !mSuppressWriteDraft) {
            mComposeMessageView.writeDraftMessage();
        }
        mSuppressWriteDraft = false;
        mBinding.getData().unsetFocus();
        mListState = mRecyclerView.getLayoutManager().onSaveInstanceState();

        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mConversationSelfIdChangeReceiver);
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mRecyclerView.getItemAnimator().endAnimations();
    }

    // TODO: Remove isBound and replace it with ensureBound after b/15704674.
    public boolean isBound() {
        return mBinding.isBound();
    }

    protected FragmentManager getFragmentManagerToUse() {
        return OsUtil.isAtLeastJB_MR1() ? getChildFragmentManager() : getFragmentManager();
    }

    public MediaPicker getMediaPicker() {
        return (MediaPicker) getFragmentManagerToUse().findFragmentByTag(
                MediaPicker.FRAGMENT_TAG);
    }

    @Override
    public void sendMessage(final MessageData message) {
        if (isReadyForAction()) {
            if (ensureKnownRecipients()) {
                // Merge the caption text from attachments into the text body of the messages
                message.consolidateText();

                mBinding.getData().sendMessage(mBinding, message);
                mComposeMessageView.resetMediaPickerState();
            } else {
                LogUtil.w(LogUtil.BUGLE_TAG, "Message can't be sent: conv participants not loaded");
            }
        } else {
            warnOfMissingActionConditions(true /*sending*/,
                    new Runnable() {
                        @Override
                        public void run() {
                            sendMessage(message);
                        }
                    });
        }
    }

    public void setHost(final ConversationFragmentHost host) {
        mHost = host;
    }

    public String getConversationName() {
        return mBinding.getData().getConversationName();
    }

    @Override
    public void onComposeEditTextFocused() {
        mHost.onStartComposeMessage();
    }

    @Override
    public void onAttachmentsCleared() {
        // When attachments are removed, reset transient media picker state such as image selection.
        mComposeMessageView.resetMediaPickerState();
    }

    /**
     * Called to check if all conditions are nominal and a "go" for some action, such as deleting
     * a message, that requires this app to be the default app. This is also a precondition
     * required for sending a draft.
     *
     * @return true if all conditions are nominal and we're ready to send a message
     */
    @Override
    public boolean isReadyForAction() {
        return UiUtils.isReadyForAction();
    }

    /**
     * When there's some condition that prevents an operation, such as sending a message,
     * call warnOfMissingActionConditions to put up a snackbar and allow the user to repair
     * that condition.
     *
     * @param sending                                  - true if we're called during a sending operation
     * @param commandToRunAfterActionConditionResolved - a runnable to run after the user responds
     *                                                 positively to the condition prompt and resolves the condition. If null,
     *                                                 the user will be shown a toast to tap the send button again.
     */
    @Override
    public void warnOfMissingActionConditions(final boolean sending,
                                              final Runnable commandToRunAfterActionConditionResolved) {
        if (mChangeDefaultSmsAppHelper == null) {
            mChangeDefaultSmsAppHelper = new ChangeDefaultSmsAppHelper();
        }
        mChangeDefaultSmsAppHelper.warnOfMissingActionConditions(sending,
                commandToRunAfterActionConditionResolved, mComposeMessageView,
                getView().getRootView(),
                getActivity(), this);
    }

    private boolean ensureKnownRecipients() {
        final ConversationData conversationData = mBinding.getData();

        if (!conversationData.getParticipantsLoaded()) {
            // We can't tell yet whether or not we have an unknown recipient
            return false;
        }

        final ConversationParticipantsData participants = conversationData.getParticipants();
        for (final ParticipantData participant : participants) {


            if (participant.isUnknownSender()) {
                UiUtils.showToast(R.string.unknown_sender);
                return false;
            }
        }

        return true;
    }

    public void retryDownload(final String messageId) {
        if (isReadyForAction()) {
            mBinding.getData().downloadMessage(mBinding, messageId);
        } else {
            warnOfMissingActionConditions(false /*sending*/,
                    null /*commandToRunAfterActionConditionResolved*/);
        }
    }

    public void retrySend(final String messageId) {
        if (isReadyForAction()) {
            if (ensureKnownRecipients()) {
                mBinding.getData().resendMessage(mBinding, messageId);
            }
        } else {
            warnOfMissingActionConditions(true /*sending*/,
                    new Runnable() {
                        @Override
                        public void run() {
                            retrySend(messageId);
                        }

                    });
        }
    }

    protected void deleteMessage(final String messageId) {
        if (isReadyForAction()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.delete_message_confirmation_dialog_title)
                    .setMessage(R.string.delete_message_confirmation_dialog_text)
                    .setPositiveButton(R.string.delete_message_confirmation_button,
                            new OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    mBinding.getData().deleteMessage(mBinding, messageId);
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, null);
            if (OsUtil.isAtLeastJB_MR1()) {
                builder.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(final DialogInterface dialog) {
                        mHost.dismissActionMode();
                    }
                });
            } else {
                builder.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        mHost.dismissActionMode();
                    }
                });
            }
            builder.create().show();
        } else {
            warnOfMissingActionConditions(false /*sending*/,
                    null /*commandToRunAfterActionConditionResolved*/);
            mHost.dismissActionMode();
        }
    }

    public void deleteConversation() {
        if (isReadyForAction()) {
            final Context context = getActivity();
            mBinding.getData().deleteConversation(mBinding);
            closeConversation(mConversationId);
        } else {
            warnOfMissingActionConditions(false /*sending*/,
                    null /*commandToRunAfterActionConditionResolved*/);
        }
    }

    @Override
    public void closeConversation(final String conversationId) {
        if (TextUtils.equals(conversationId, mConversationId)) {
            mHost.onFinishCurrentConversation();
            // TODO: Explicitly transition to ConversationList (or just go back)?
        }
    }


    @Override
    public void onConversationParticipantDataLoaded(final ConversationData data) {
        mBinding.ensureBound(data);
        if (mBinding.getData().getParticipantsLoaded()) {
            final boolean oneOnOne = mBinding.getData().getOtherParticipant() != null;
            mAdapter.setOneOnOne(oneOnOne, true /* invalidate */);

            // refresh the options menu which will enable the "people & options" item.
            // Bkav QuangNDb tach code doan reset option menu
            resetOptionMenu();

            mHost.invalidateActionBar();
//            if (oneOnOne) {
//                ParticipantRefresh.refreshParticipantsIfNeeded(mBinding.getData().getOtherParticipant().getId());
//            }

            mRecyclerView.setVisibility(View.VISIBLE);
            mHost.onConversationParticipantDataLoaded
                    (mBinding.getData().getNumberOfParticipantsExcludingSelf());
        }
    }

    @Override
    public void onSubscriptionListDataLoaded(final ConversationData data) {
        mBinding.ensureBound(data);
//        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void promptForSelfPhoneNumber() {
        if (mComposeMessageView != null) {
            // Avoid bug in system which puts soft keyboard over dialog after orientation change
            ImeUtil.hideSoftInput(getActivity(), mComposeMessageView);
        }

        final FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        final EnterSelfPhoneNumberDialog dialog = EnterSelfPhoneNumberDialog
                .newInstance(getConversationSelfSubId());
        dialog.setTargetFragment(this, 0/*requestCode*/);
        dialog.show(ft, null/*tag*/);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (mChangeDefaultSmsAppHelper == null) {
            mChangeDefaultSmsAppHelper = new ChangeDefaultSmsAppHelper();
        }
        mChangeDefaultSmsAppHelper.handleChangeDefaultSmsResult(requestCode, resultCode, null);
    }

    public boolean hasMessages() {
        return mAdapter != null && mAdapter.getItemCount() > 0;
    }

    public boolean onBackPressed() {
        //HienDTk: cap nhat lai giao dien khi back
        mAdapter.notifyDataSetChanged();

        if (mComposeMessageView.onBackPressed()) {
            return true;
        }
        return false;
    }


    public boolean onNavigationUpPressed() {
        return mComposeMessageView.onNavigationUpPressed();
    }

    @Override
    public boolean onAttachmentClick(final ConversationMessageView messageView,
                                     final MessagePartData attachment, final Rect imageBounds, final boolean longPress) {
        if (longPress) {
            selectMessage(messageView, attachment);
            return true;
        } else if (messageView.getData().getOneClickResendMessage()) {
            handleMessageClick(messageView);
            return true;
        }

        if (attachment.isImage()) {
            displayPhoto(attachment.getContentUri(), imageBounds, false /* isDraft */);
        }

        if (attachment.isVCard()) {
            UIIntents.get().launchVCardDetailActivity(getActivity(), attachment.getContentUri());
        }

        return false;
    }

    protected void handleMessageClick(final ConversationMessageView messageView) {
        if (messageView != mSelectedMessage) {
            final ConversationMessageData data = messageView.getData();
            final boolean isReadyToSend = isReadyForAction();
            if (data.getOneClickResendMessage()) {
                // Directly resend the message on tap if it's failed
                retrySend(data.getMessageId());
                selectMessage(null);
            } else if (data.getShowResendMessage() && isReadyToSend) {
                // Select the message to show the resend/download/delete options
                selectMessage(messageView);
            } else if (data.getShowDownloadMessage() && isReadyToSend) {
                // Directly download the message on tap
                retryDownload(data.getMessageId());
            } else {
                // Let the toast from warnOfMissingActionConditions show and skip
                // selecting
//                 Bkav QuangNdb bo xung them ham show or hide sub info message
                messageView.showOrHideSubInfoWhenClick();
                clickCompleteMessage();
            }
        } else {
            selectMessage(null);
        }
    }


    private static class AttachmentToSave {
        public final Uri uri;
        public final String contentType;
        public Uri persistedUri;

        AttachmentToSave(final Uri uri, final String contentType) {
            this.uri = uri;
            this.contentType = contentType;
        }
    }

    public static class SaveAttachmentTask extends SafeAsyncTask<Void, Void, Void> {
        private final Context mContext;
        private final List<AttachmentToSave> mAttachmentsToSave = new ArrayList<>();

        public SaveAttachmentTask(final Context context, final Uri contentUri,
                                  final String contentType) {
            mContext = context;
            addAttachmentToSave(contentUri, contentType);
        }

        public SaveAttachmentTask(final Context context) {
            mContext = context;
        }

        public void addAttachmentToSave(final Uri contentUri, final String contentType) {
            mAttachmentsToSave.add(new AttachmentToSave(contentUri, contentType));
        }

        public int getAttachmentCount() {
            return mAttachmentsToSave.size();
        }

        @Override
        protected Void doInBackgroundTimed(final Void... arg) {
            final File appDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES),
                    mContext.getResources().getString(R.string.app_name));
            final File downloadDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            for (final AttachmentToSave attachment : mAttachmentsToSave) {
                final boolean isImageOrVideo = ContentType.isImageType(attachment.contentType)
                        || ContentType.isVideoType(attachment.contentType);
                attachment.persistedUri = UriUtil.persistContent(attachment.uri,
                        isImageOrVideo ? appDir : downloadDir, attachment.contentType);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            int failCount = 0;
            int imageCount = 0;
            int videoCount = 0;
            int otherCount = 0;
            for (final AttachmentToSave attachment : mAttachmentsToSave) {
                if (attachment.persistedUri == null) {
                    failCount++;
                    continue;
                }

                // Inform MediaScanner about the new file
                final Intent scanFileIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                scanFileIntent.setData(attachment.persistedUri);
                mContext.sendBroadcast(scanFileIntent);

                if (ContentType.isImageType(attachment.contentType)) {
                    imageCount++;
                } else if (ContentType.isVideoType(attachment.contentType)) {
                    videoCount++;
                } else {
                    otherCount++;
                    // Inform DownloadManager of the file so it will show in the "downloads" app
                    final DownloadManager downloadManager =
                            (DownloadManager) mContext.getSystemService(
                                    Context.DOWNLOAD_SERVICE);
                    final String filePath = attachment.persistedUri.getPath();
                    final File file = new File(filePath);

                    if (file.exists()) {
                        downloadManager.addCompletedDownload(
                                file.getName() /* title */,
                                mContext.getString(
                                        R.string.attachment_file_description) /* description */,
                                true /* isMediaScannerScannable */,
                                attachment.contentType,
                                file.getAbsolutePath(),
                                file.length(),
                                false /* showNotification */);
                    }
                }
            }

            String message;
            if (failCount > 0) {
                message = mContext.getResources().getQuantityString(
                        R.plurals.attachment_save_error, failCount, failCount);
            } else {
                int messageId = R.plurals.attachments_saved;
                if (otherCount > 0) {
                    if (imageCount + videoCount == 0) {
                        messageId = R.plurals.attachments_saved_to_downloads;
                    }
                } else {
                    if (videoCount == 0) {
                        messageId = R.plurals.photos_saved_to_album;
                    } else if (imageCount == 0) {
                        messageId = R.plurals.videos_saved_to_album;
                    } else {
                        messageId = R.plurals.attachments_saved_to_album;
                    }
                }
                final String appName = mContext.getResources().getString(R.string.app_name);
                final int count = imageCount + videoCount + otherCount;
                message = mContext.getResources().getQuantityString(
                        messageId, count, count, appName);
            }
            UiUtils.showToastAtBottom(message);
        }
    }

    private void invalidateOptionsMenu() {
        final Activity activity = getActivity();
        // TODO: Add the supportInvalidateOptionsMenu call to the host activity.
        if (activity == null || !(activity instanceof BugleActionBarActivity)) {
            return;
        }
        ((BugleActionBarActivity) activity).supportInvalidateOptionsMenu();
    }

    @Override
    public void setOptionsMenuVisibility(final boolean visible) {
        setHasOptionsMenu(visible);
    }

    @Override
    public int getConversationSelfSubId() {
        final String selfParticipantId = mComposeMessageView.getConversationSelfId();
        final ParticipantData self = mBinding.getData().getSelfParticipantById(selfParticipantId);
        // If the self id or the self participant data hasn't been loaded yet, fallback to
        // the default setting.
        return self == null ? ParticipantData.DEFAULT_SELF_SUB_ID : self.getSubId();
    }

    @Override
    public void invalidateActionBar() {
        mHost.invalidateActionBar();
    }

    @Override
    public void dismissActionMode() {
        mHost.dismissActionMode();
    }

    @Override
    public void selectSim(final SubscriptionListEntry subscriptionData) {
        mComposeMessageView.selectSim(subscriptionData);
        mHost.onStartComposeMessage();
    }

    @Override
    public void showQuickResponse() {

    }

    @Override
    public boolean isReadySendMessage() {
        return true;
    }

    @Override
    public void onStartComposeMessage() {
        mHost.onStartComposeMessage();
    }

    @Override
    public SubscriptionListEntry getSubscriptionEntryForSelfParticipant(
            final String selfParticipantId, final boolean excludeDefault) {
    // TODO: ConversationMessageView is the only one using this. We should probably
        // inject this into the view during binding in the ConversationMessageAdapter.
        return mBinding.getData().getSubscriptionEntryForSelfParticipant(selfParticipantId,
                excludeDefault);
    }


    @Override
    public SimSelectorView getSimSelectorView() {
        return (SimSelectorView) getView().findViewById(R.id.sim_selector);
    }

    @Override
    public MediaPicker createMediaPicker() {
        return new MediaPicker(getActivity());
    }

    @Override
    public void notifyOfAttachmentLoadFailed() {
        UiUtils.showToastAtBottom(R.string.attachment_load_failed_dialog_message);
    }

    @Override
    public void warnOfExceedingMessageLimit(final boolean sending, final boolean tooManyVideos) {
        warnOfExceedingMessageLimit(sending, mComposeMessageView, mConversationId,
                getActivity(), tooManyVideos);
    }

    public static void warnOfExceedingMessageLimit(final boolean sending,
                                                   final ComposeMessageView composeMessageView, final String conversationId,
                                                   final Activity activity, final boolean tooManyVideos) {
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.mms_attachment_limit_reached);

        if (sending) {
            if (tooManyVideos) {
                builder.setMessage(R.string.video_attachment_limit_exceeded_when_sending);
            } else {
                builder.setMessage(R.string.attachment_limit_reached_dialog_message_when_sending)
                        .setNegativeButton(R.string.attachment_limit_reached_send_anyway,
                                new OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog,
                                                        final int which) {
                                        composeMessageView.sendMessageIgnoreMessageSizeLimit();
                                    }
                                });
            }
            builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    showAttachmentChooser(conversationId, activity);
                }
            });
        } else {
            builder.setMessage(R.string.attachment_limit_reached_dialog_message_when_composing)
                    .setPositiveButton(android.R.string.ok, null);
        }
        builder.show();
    }

    @Override
    public void showAttachmentChooser() {
        showAttachmentChooser(mConversationId, getActivity());
    }

    public static void showAttachmentChooser(final String conversationId,
                                             final Activity activity) {
        UIIntents.get().launchAttachmentChooserActivity(activity,
                conversationId, REQUEST_CHOOSE_ATTACHMENTS);
    }

    protected void updateActionAndStatusBarColor(final ActionBar actionBar) {
        final int themeColor = ConversationDrawables.get().getConversationThemeColor();
        actionBar.setBackgroundDrawable(new ColorDrawable(themeColor));
        UiUtils.setStatusBarColor(getActivity(), themeColor);
    }

    public void updateActionBar(final ActionBar actionBar) {
        // Bkav TienNAb: them dieu kien de update actionbar khi mediapicker khong o che do fullscreen
        if (mComposeMessageView == null || !mComposeMessageView.updateActionBar(actionBar) || !inputManager.getMediaInput().isFullScreen()) {
            // Bkav QuangNDb xoa do bong tren action bar
            removeShadowActionbar(actionBar);
            updateActionAndStatusBarColor(actionBar);
            // We update this regardless of whether or not the action bar is showing so that we
            // don't get a race when it reappears.
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(true);
            // Reset the back arrow to its default
            actionBar.setHomeAsUpIndicator(0);
            View customView = actionBar.getCustomView();
            if (customView == null || customView.getId() != R.id.conversation_title_container) {
                final LayoutInflater inflator = (LayoutInflater)
                        getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                customView = inflator.inflate(getIdResLayoutCustomActionBar(), null);
                customView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        onBackPressed();
                    }
                });
                actionBar.setCustomView(customView);
            }

            final TextView conversationNameView =
                    (TextView) customView.findViewById(R.id.conversation_title);
            // Bkav QUangNDb them su kien click vao text view tren action bar
            titleBarClick(conversationNameView);
            final String conversationName = getConversationName();
            if (!TextUtils.isEmpty(conversationName)) {
                // bkav QuangNDb set title bar cho action bar
                setTitleBar(conversationName, conversationNameView);
            } else {
                setDefaultName(conversationNameView);
            }

            // When conversation is showing and media picker is not showing, then hide the action
            // bar only when we are in landscape mode, with IME open.
            if (mHost.isImeOpen() && UiUtils.isLandscapeMode()) {
                actionBar.hide();
            } else {
                actionBar.show();
            }
        } else {
            exitActionMode();//Bkav QuangNDb them ham exit action mode khi action bar bi an
        }
    }


    @Override
    public boolean shouldShowSubjectEditor() {
        return true;
    }

    @Override
    public boolean shouldHideAttachmentsWhenSimSelectorShown() {
        return false;
    }

    @Override
    public void showHideSimSelector(final boolean show) {
        // no-op for now
    }

    @Override
    public int getSimSelectorItemLayoutId() {
        return R.layout.sim_selector_item_view;
    }

    @Override
    public Uri getSelfSendButtonIconUri() {
        return null;    // use default button icon uri
    }

    @Override
    public int overrideCounterColor() {
        return -1;      // don't override the color
    }

    @Override
    public void onAttachmentsChanged(final boolean haveAttachments) {
        // no-op for now
    }

    @Override
    public void onDraftChanged(final DraftMessageData data, final int changeFlags) {
        mDraftMessageDataModel.ensureBound(data);
        // We're specifically only interested in ATTACHMENTS_CHANGED from the widget. Ignore
        // other changes. When the widget changes an attachment, we need to reload the draft.
        if (changeFlags ==
                (DraftMessageData.WIDGET_CHANGED | DraftMessageData.ATTACHMENTS_CHANGED)) {
            mClearLocalDraft = true;        // force a reload of the draft in onResume
        }
    }

    @Override
    public void onDraftAttachmentLimitReached(final DraftMessageData data) {
        // no-op for now
    }

    @Override
    public void onDraftAttachmentLoadFailed() {
        // no-op for now
    }

    @Override
    public int getAttachmentsClearedFlags() {
        return DraftMessageData.ATTACHMENTS_CHANGED;
    }

    @Override
    public void onReadyMessage() {
        // Bkav QuangNDb khong lam gi
    }

    /**
     * ------------------------------------------------------------------BKAV------------------------------------------------
     * Bkav QuangNDb them bien de lay duoc contentview cua fragment ra ngoai
     */
    protected View mRootView;

    public View getRootView() {
        return mRootView;
    }

    /**
     * Bkav: QuangNDb tach ham de custom lai o lop con
     */
    protected void inflateMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.conversation_menu, menu);
    }

    /**
     * Bkav: QuangNDb tach ham de custom lai o lop con
     */
    protected View inflateView(final LayoutInflater inflater, final ViewGroup container) {
        return inflater.inflate(R.layout.conversation_fragment, container, false);
    }

    /**
     * Bkav: QuangNDb tach ham de custom lai o lop con
     */
    protected void initComposeView(View view) {
        mComposeMessageView = (ComposeMessageView)
                view.findViewById(R.id.message_compose_view_container);
    }

    /**
     * Bkav QuangNDb tach code doan khoi tao adapter de override lai o lop con
     */
    protected void initAdapter() {
        mAdapter = new ConversationMessageAdapter(getActivity(), null, this,
                null,
                // Sets the item click listener on the Recycler item views.
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        final ConversationMessageView messageView = (ConversationMessageView) v;
                        handleMessageClick(messageView);
                    }
                },
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View view) {
                        selectMessage((ConversationMessageView) view);
                        return true;
                    }
                }
        );
    }

    /**
     * Bkav QuangNDb: tach code doan start action mode de override lai o class con
     */
    protected void startActionMode() {
        mHost.startActionMode(mMessageActionModeCallback);
    }

    @Override
    public boolean isMessageSelected(String messageId) {
        return false;
    }

    @Override
    public boolean isShowActionMode() {
        return false;
    }

    @Override
    public boolean onMessageTextClick(ConversationMessageView view) {
        return false;
    }

    @Override
    public boolean isMessageHasLink(TextView messageText) {
        return false;
    }

    /**
     * Bkav QuangNDb them ham de override lai o class con
     */
    protected void selectMessage(final ConversationMessageView messageView,
                                 final MessagePartData attachment, boolean isLongClick) {
    }

    /**
     * Bkav QuangNDb tach code doan khoi tao input manager de override lai o class con
     */
    protected ConversationInputManager initInputManager(final Bundle savedInstanceState) {
        return new ConversationInputManager(
                getActivity(), this, mComposeMessageView, mHost, getFragmentManagerToUse(),
                mBinding, mComposeMessageView.getDraftDataModel(), savedInstanceState);
    }

    protected void titleBarClick(TextView conversationNameView) {
        // Bkav QuangNdb khong lam gi
    }

    public void setBtalkConversationFragmentHost(Object o) {
    }

    /**
     * Bkav QUangNDb tach code doan click vao 1 complete message
     */
    protected void clickCompleteMessage() {
        warnOfMissingActionConditions(false /*sending*/,
                null /*commandToRunAfterActionConditionResolved*/);
        selectMessage(null);
    }

    /**
     * Bkav QuangNDb xoa do bong default tren action bar
     */
    protected void removeShadowActionbar(ActionBar actionBar) {
        // Bkav QuangNDb khong lam gi override lai o class con
    }

    /**
     * Bkav QuangNDb them view do bong custom
     */
    protected void initShadowActionbar(View view) {
        // Bkav QuangNDb khong lam gi override lai o class con
    }

    /**
     * Bkav QuangNDb ham check recycler view scroll to top
     */
    protected boolean isScrolledToTop() {
        return false;
    }

    /**
     * Bkav QuangNDb ham xu li khi action ba keo nen top hoac khong
     */
    protected void setAlphaShadowActionbar(boolean scrolledToTop) {
        // Bkav QuangNDb khong lam gi custom lai o class cha
    }

    // Bkav QuangNDb check xem khi mo list tin nhan thi item dau da xuat hien chua
    protected void checkTopVisible() {
        // Bkav QuangNDb khong lam gi override lai o class con
    }

    /**
     * Bkav QuangNDb tach code doan set default name
     */
    protected void setDefaultName(TextView conversationNameView) {
        final String appName = getString(R.string.app_name);
        conversationNameView.setText(appName);
        getActivity().setTitle(appName);
    }

    /**
     * Bkav QuangNDb tach code doan get layout custom actionbar de override lai o class con
     */
    protected int getIdResLayoutCustomActionBar() {
        return R.layout.action_bar_conversation_name;
    }

    /**
     * Bkav QuangNDb tach code doan init fastScroll
     */
    protected void initFastScroll() {
        mFastScroller = ConversationFastScroller.addTo(mRecyclerView,
                UiUtils.isRtlMode() ? ConversationFastScroller.POSITION_LEFT_SIDE :
                        ConversationFastScroller.POSITION_RIGHT_SIDE);
    }

    /**
     * Bkav QuangNDb tach code doan title bar
     */
    protected void setTitleBar(String conversationName, TextView conversationNameView) {
        // RTL : To format conversation title if it happens to be phone numbers.
        final BidiFormatter bidiFormatter = BidiFormatter.getInstance();
        final String formattedName = bidiFormatter.unicodeWrap(
                UiUtils.commaEllipsize(
                        conversationName,
                        conversationNameView.getPaint(),
                        conversationNameView.getWidth(),
                        getString(R.string.plus_one),
                        getString(R.string.plus_n)).toString(),
                TextDirectionHeuristicsCompat.LTR);
        conversationNameView.setText(formattedName);
        // In case phone numbers are mixed in the conversation name, we need to vocalize it.
        final String vocalizedConversationName =
                AccessibilityUtil.getVocalizedPhoneNumber(getResources(), conversationName);
        conversationNameView.setContentDescription(vocalizedConversationName);
        getActivity().setTitle(conversationName);
    }

    /**
     * Bkav QuangNDb tach code doan make a call
     */
    protected void makeACall(boolean suggestShowOtherSim) {
        final String phoneNumber = mBinding.getData().getParticipantPhoneNumber();
        Assert.notNull(phoneNumber);
        final View targetView = getActivity().findViewById(R.id.action_call);
        Point centerPoint;
        if (targetView != null) {
            final int screenLocation[] = new int[2];
            targetView.getLocationOnScreen(screenLocation);
            final int centerX = screenLocation[0] + targetView.getWidth() / 2;
            final int centerY = screenLocation[1] + targetView.getHeight() / 2;
            centerPoint = new Point(centerX, centerY);
        } else {
            // In the overflow menu, just use the center of the screen.
            final Display display = getActivity().getWindowManager().getDefaultDisplay();
            centerPoint = new Point(display.getWidth() / 2, display.getHeight() / 2);
        }
        UIIntents.get().launchPhoneCallActivity(getActivity(), phoneNumber, centerPoint);
    }

    /**
     * Bkav QuangNDb: tach code doan getPopupAnimation
     */
    protected PopupTransitionAnimation getPopupTransitionAnimation(Rect startRect, ConversationMessageView view) {
        return new PopupTransitionAnimation(startRect, view);
    }

    /**
     * Bkav QuangNDb: tach code doan set item animation
     */
    protected RecyclerView.ItemAnimator setItemAnimation() {
        return new DefaultItemAnimator() {
            private final List<ViewHolder> mAddAnimations = new ArrayList<ViewHolder>();
            private PopupTransitionAnimation mPopupTransitionAnimation;

            @Override
            public boolean animateAdd(final ViewHolder holder) {
                final ConversationMessageView view = getConversationMessageView(holder.itemView);
                final ConversationMessageData data = view.getData();
                endAnimation(holder);
                final long timeSinceSend = System.currentTimeMillis() - data.getReceivedTimeStamp();
                if (data.getReceivedTimeStamp() == InsertNewMessageAction.getLastSentMessageTimestamp()
                        && !data.getIsIncoming()
                        && timeSinceSend < MESSAGE_ANIMATION_MAX_WAIT) {
                    final ConversationMessageBubbleView messageBubble =
                            (ConversationMessageBubbleView) view
                                    .findViewById(R.id.message_content);
                    final Rect startRect = UiUtils.getMeasuredBoundsOnScreen(mComposeMessageView);
                    final View composeBubbleView = mComposeMessageView.findViewById(
                            R.id.compose_message_text);
                    final Rect composeBubbleRect =
                            UiUtils.getMeasuredBoundsOnScreen(composeBubbleView);
                    final AttachmentPreview attachmentView =
                            (AttachmentPreview) mComposeMessageView.findViewById(
                                    R.id.attachment_draft_view);
                    final Rect attachmentRect = UiUtils.getMeasuredBoundsOnScreen(attachmentView);
                    if (attachmentView.getVisibility() == View.VISIBLE) {
                        startRect.top = attachmentRect.top;
                    } else {
                        startRect.top = composeBubbleRect.top;
                    }
                    startRect.top -= view.getPaddingTop();
                    startRect.bottom =
                            composeBubbleRect.bottom;
                    startRect.left += view.getPaddingRight();

                    view.setAlpha(0);
                    // todo Crash o doan nay do thay actionbar bang tool bar xem xet sua lai
                    mPopupTransitionAnimation = getPopupTransitionAnimation(startRect, view);
                    mPopupTransitionAnimation.setOnStartCallback(new Runnable() {
                        @Override
                        public void run() {
                            final int startWidth = composeBubbleRect.width();
                            attachmentView.onMessageAnimationStart();
                            messageBubble.kickOffMorphAnimation(startWidth,
                                    messageBubble.findViewById(R.id.message_text_and_info)
                                            .getMeasuredWidth());
                        }
                    });
                    mPopupTransitionAnimation.setOnStopCallback(new Runnable() {
                        @Override
                        public void run() {
                            view.setAlpha(1);
                        }
                    });
                    mPopupTransitionAnimation.startAfterLayoutComplete();
                    mAddAnimations.add(holder);
                    return true;
                } else {
                    return super.animateAdd(holder);
                }
            }

            @Override
            public void endAnimation(final ViewHolder holder) {
                if (mAddAnimations.remove(holder)) {
                    holder.itemView.clearAnimation();
                }
                super.endAnimation(holder);
            }

            @Override
            public void endAnimations() {
                for (final ViewHolder holder : mAddAnimations) {
                    holder.itemView.clearAnimation();
                }
                mAddAnimations.clear();
                if (mPopupTransitionAnimation != null) {
                    mPopupTransitionAnimation.cancel();
                }
                super.endAnimations();
            }
        };
    }

    protected void setRecyclerAnimator() {
        mRecyclerView.setItemAnimator(setItemAnimation());
    }

    /**
     * Bkav QuangNDB khoi tao cac bien view tren custom actionbar
     */
    protected void setUpToolBar(View customView) {
        // Bkav QuangNDb khong lam gi custom lai o class con
    }

    /**
     * Bkav QuangNDb reset option menu
     */
    protected void resetOptionMenu() {
        invalidateOptionsMenu();
    }

    /**
     * Bkav QuangNDb Thoat che do action mode
     */
    protected void exitActionMode() {

    }


    /**
     * Bkav QuangNDb luu lai trang thai thoi gian va conversation_id de khi bam home quay lai van du trang thai
     */
    public void saveStatus() {

    }

    /**
     * Bkav QuangNDb check xem co o trang thai deep shortcut show hay khong
     */
    public boolean isDeepShortcutTouch(MotionEvent ev) {
        return false;
    }

    // Bkav HuyNQN kiem tra xem danh sach esim co dang hien thi hay khong
    public boolean isListEsimTouch(MotionEvent event) {
        return false;
    }

    @Override
    public void sendQuickResponse(Object data) {

    }

    @Override
    public void keyboardShow() {

    }

    @Override
    public void emojiClick() {

    }


    @Override
    public void onMediaPickerStateChange(boolean isShow) {

    }

    /**
     * Bkav QuangNDb ham get context de override lai o lop chathead
     */
    public Context getContext() {
        return getActivity();
    }
    
}
