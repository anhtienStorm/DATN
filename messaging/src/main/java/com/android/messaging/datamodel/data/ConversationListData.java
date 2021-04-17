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

package com.android.messaging.datamodel.data;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.android.messaging.datamodel.BoundCursorLoader;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseHelper.ParticipantColumns;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.SyncManager;
import com.android.messaging.datamodel.binding.BindableData;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.data.ConversationListItemData.ConversationListViewColumns;
import com.android.messaging.receiver.SmsReceiver;
import com.android.messaging.util.Assert;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.OsUtil;

import java.util.HashSet;

public class ConversationListData extends BindableData
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = LogUtil.BUGLE_DATAMODEL_TAG;
    private static final String BINDING_ID = "bindingId";
    public static final String SORT_ORDER =
            ConversationListViewColumns.SORT_TIMESTAMP + " DESC";

    private static final String WHERE_ARCHIVED =
            "(" + ConversationListViewColumns.ARCHIVE_STATUS + " = 1)";
    public static final String WHERE_NOT_ARCHIVED =
            "(" + ConversationListViewColumns.ARCHIVE_STATUS + " = 0)";

    // Bkav QuangNDb bien search de nhan biet co dang o che do search hay k
    private String mSearchString = null;

    private final SubscriptionListData mSubscriptionListData;//Bkav QuangNDb bien luu du lieu ve list sim

    private final SelfParticipantsData mSelfParticipantsData;//Bkav QuangNDb bien luu du lieu ve sim

    public void setSearchString(String mSearchString) {
        this.mSearchString = mSearchString;
    }

    public interface ConversationListDataListener {
        public void onConversationListCursorUpdated(ConversationListData data, Cursor cursor);
        public void setBlockedParticipantsAvailable(boolean blockedAvailable);
        void onSubscriptionListDataLoaded(ConversationListData data);
    }

    private ConversationListDataListener mListener;
    private final Context mContext;
    private final boolean mArchivedMode;
    private LoaderManager mLoaderManager;

    public ConversationListData(final Context context, final ConversationListDataListener listener,
            final boolean archivedMode) {
        mListener = listener;
        mContext = context;
        mArchivedMode = archivedMode;
        //Bkav QuangNDb khoi tao bien du lieu de lay thong tin sim
        mSelfParticipantsData = new SelfParticipantsData();
        mSubscriptionListData = new SubscriptionListData(context);
    }

    private static final int CONVERSATION_LIST_LOADER = 1;
    private static final int BLOCKED_PARTICIPANTS_AVAILABLE_LOADER = 2;
    private static final int SELF_PARTICIPANT_LOADER = 3;//Bkav QuangNDb them id loader load thong tin sim

    private static final String[] BLOCKED_PARTICIPANTS_PROJECTION = new String[] {
            ParticipantColumns._ID,
            ParticipantColumns.NORMALIZED_DESTINATION,
    };
    private static final int INDEX_BLOCKED_PARTICIPANTS_ID = 0;
    private static final int INDEX_BLOCKED_PARTICIPANTS_NORMALIZED_DESTINATION = 1;

    // all blocked participants
    private final HashSet<String> mBlockedParticipants = new HashSet<String>();

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final String bindingId = args.getString(BINDING_ID);
        Loader<Cursor> loader = null;
        // Check if data still bound to the requesting ui element
        if (isBound(bindingId)) {
            switch (id) {
                case BLOCKED_PARTICIPANTS_AVAILABLE_LOADER:
                    loader = new BoundCursorLoader(bindingId, mContext,
                            MessagingContentProvider.PARTICIPANTS_URI,
                            BLOCKED_PARTICIPANTS_PROJECTION,
                            ParticipantColumns.BLOCKED + "=1", null, null);
                    break;
                case CONVERSATION_LIST_LOADER:
                    String where = "";
                    if (mSearchString == null) {
                        where = mArchivedMode ? WHERE_ARCHIVED : WHERE_NOT_ARCHIVED;
                    }else {
                        where = ConversationListViewColumns.ARCHIVE_STATUS + " = 0 and "
                                + ConversationListViewColumns._ID + " IN (SELECT docid"
                                + " FROM " + DatabaseHelper.FTS_CONVERSATIONS_TABLE
                                + " WHERE " + DatabaseHelper.FTS_CONVERSATIONS_TABLE + " MATCH '*" + mSearchString+"*')";
                    }
                    loader = new BoundCursorLoader(bindingId, mContext,
                            MessagingContentProvider.CONVERSATIONS_URI,
                            ConversationListItemData.PROJECTION,
                            where,
                            null,       // selection args
                            SORT_ORDER);
                    break;
                case SELF_PARTICIPANT_LOADER:
                    loader = new BoundCursorLoader(bindingId, mContext,
                            MessagingContentProvider.PARTICIPANTS_URI,
                            ParticipantData.ParticipantsQuery.PROJECTION,
                            ParticipantColumns.SUB_ID + " <> ?",
                            new String[] { String.valueOf(ParticipantData.OTHER_THAN_SELF_SUB_ID) },
                            null);
                    break;
                default:
                    Assert.fail("Unknown loader id");
                    break;
            }
        } else {
            LogUtil.w(TAG, "Creating loader after unbinding list");
        }
        return loader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(final Loader<Cursor> generic, final Cursor data) {
        final BoundCursorLoader loader = (BoundCursorLoader) generic;
        if (isBound(loader.getBindingId())) {
            switch (loader.getId()) {
                case BLOCKED_PARTICIPANTS_AVAILABLE_LOADER:
                    mBlockedParticipants.clear();
                    for (int i = 0; i < data.getCount(); i++) {
                        data.moveToPosition(i);
                        mBlockedParticipants.add(data.getString(
                                INDEX_BLOCKED_PARTICIPANTS_NORMALIZED_DESTINATION));
                    }
                    mListener.setBlockedParticipantsAvailable(data != null && data.getCount() > 0);
                    break;
                case CONVERSATION_LIST_LOADER:
                    mListener.onConversationListCursorUpdated(this, data);
                    break;
                case SELF_PARTICIPANT_LOADER:
                    mSelfParticipantsData.bind(data);
                    mSubscriptionListData.bind(mSelfParticipantsData.getSelfParticipants(true));
                    mListener.onSubscriptionListDataLoaded(ConversationListData.this);
                    break;
                default:
                    Assert.fail("Unknown loader id");
                    break;
            }
        } else {
            LogUtil.w(TAG, "Loader finished after unbinding list");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(final Loader<Cursor> generic) {
        final BoundCursorLoader loader = (BoundCursorLoader) generic;
        if (isBound(loader.getBindingId())) {
            switch (loader.getId()) {
                case BLOCKED_PARTICIPANTS_AVAILABLE_LOADER:
                    mListener.setBlockedParticipantsAvailable(false);
                    break;
                case CONVERSATION_LIST_LOADER:
                    mListener.onConversationListCursorUpdated(this, null);
                    break;
                case SELF_PARTICIPANT_LOADER:
                    mListener.onSubscriptionListDataLoaded(this);
                    break;
                default:
                    Assert.fail("Unknown loader id");
                    break;
            }
        } else {
            LogUtil.w(TAG, "Loader reset after unbinding list");
        }
    }

    private Bundle mArgs;

    public void init(final LoaderManager loaderManager,
            final BindingBase<ConversationListData> binding) {
        mArgs = new Bundle();
        mArgs.putString(BINDING_ID, binding.getBindingId());
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(CONVERSATION_LIST_LOADER, mArgs, this);
        mLoaderManager.initLoader(BLOCKED_PARTICIPANTS_AVAILABLE_LOADER, mArgs, this);
        mLoaderManager.initLoader(SELF_PARTICIPANT_LOADER, mArgs, this);
    }

    /**Bkav QuangNDb yeu cau load lai du lieu*/
    public void restart(final LoaderManager loaderManager,
                     final BindingBase<ConversationListData> binding) {
        mArgs = new Bundle();
        mArgs.putString(BINDING_ID, binding.getBindingId());
        mLoaderManager = loaderManager;
        mLoaderManager.restartLoader(CONVERSATION_LIST_LOADER, mArgs, this);
        mLoaderManager.restartLoader(BLOCKED_PARTICIPANTS_AVAILABLE_LOADER, mArgs, this);
        mLoaderManager.restartLoader(SELF_PARTICIPANT_LOADER, mArgs, this);
    }


    public void handleMessagesSeen() {
        BugleNotifications.markAllMessagesAsSeen();

        SmsReceiver.cancelSecondaryUserNotification();
    }

    @Override
    protected void unregisterListeners() {
        mListener = null;

        // This could be null if we bind but the caller doesn't init the BindableData
        if (mLoaderManager != null) {
            mLoaderManager.destroyLoader(CONVERSATION_LIST_LOADER);
            mLoaderManager.destroyLoader(BLOCKED_PARTICIPANTS_AVAILABLE_LOADER);
            mLoaderManager.destroyLoader(SELF_PARTICIPANT_LOADER);
            mLoaderManager = null;
        }
    }

    public boolean getHasFirstSyncCompleted() {
        final SyncManager syncManager = DataModel.get().getSyncManager();
        return syncManager.getHasFirstSyncCompleted();
    }

    public void setScrolledToNewestConversation(boolean scrolledToNewestConversation) {
        DataModel.get().setConversationListScrolledToNewestConversation(
                scrolledToNewestConversation);
        if (scrolledToNewestConversation) {
            handleMessagesSeen();
        }
    }

    public HashSet<String> getBlockedParticipants() {
        return mBlockedParticipants;
    }

//    ======================================================================BKAV=========================================================
    /**Bkav QuangNDb lay du lieu sim theo selfparticipant id */
    public SubscriptionListData.SubscriptionListEntry getSubscriptionEntryForSelfParticipant(
            final String selfParticipantId, final boolean excludeDefault) {
        return getSubscriptionEntryForSelfParticipant(selfParticipantId, excludeDefault,
                mSubscriptionListData, mSelfParticipantsData);
    }
    /**Bkav QuangNDb lay du lieu sim theo selfparticipant id */
    public static SubscriptionListData.SubscriptionListEntry getSubscriptionEntryForSelfParticipant(
            final String selfParticipantId, final boolean excludeDefault,
            final SubscriptionListData subscriptionListData,
            final SelfParticipantsData selfParticipantsData) {
        // SIM indicators are shown in the UI only if:
        // 1. Framework has MSIM support AND
        // 2. The device has had multiple *active* subscriptions. AND
        // 3. The message's subscription is active.
        if (OsUtil.isAtLeastL_MR1() &&
                selfParticipantsData.getSelfParticipantsCountExcludingDefault(true) > 1) {
            return subscriptionListData.getActiveSubscriptionEntryBySelfId(selfParticipantId,
                    excludeDefault);
        }
        return null;
    }
}
