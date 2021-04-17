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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.ParticipantRefresh;
import com.android.messaging.datamodel.action.BugleActionToasts;
import com.android.messaging.datamodel.action.UpdateConversationOptionsAction;
import com.android.messaging.datamodel.action.UpdateDestinationBlockedAction;
import com.android.messaging.util.AvatarUriUtil;

/**
 * Helps visualize a ParticipantData in a PersonItemView
 */
public class ParticipantListItemData extends PersonItemData {
    private final Uri mAvatarUri;
    private final String mDisplayName;
    private final String mDetails;
    private final long mContactId;
    private final String mLookupKey;
    private final String mNormalizedDestination;
    // Bkav QuangNDb them truong id de lay them du lieu do khong the override lai
    private final String mId;

    /**
     * Constructor. Takes necessary info from the incoming ParticipantData.
     */
    public ParticipantListItemData(final ParticipantData participant) {
        mAvatarUri = AvatarUriUtil.createAvatarUri(participant);
        mContactId = participant.getContactId();
        mLookupKey = participant.getLookupKey();
        mNormalizedDestination = participant.getNormalizedDestination();
        mId = participant.getId();
        if (TextUtils.isEmpty(participant.getFullName())) {
            mDisplayName = participant.getSendDestination();
            mDetails = null;
        } else {
            mDisplayName = participant.getFullName();
            mDetails = (participant.isUnknownSender()) ? null : participant.getSendDestination();
        }
    }

    @Override
    public Uri getAvatarUri() {
        return mAvatarUri;
    }

    @Override
    public String getDisplayName() {
        return mDisplayName;
    }

    @Override
    public String getDetails() {
        return mDetails;
    }

    @Override
    public Intent getClickIntent() {
        return null;
    }

    @Override
    public long getContactId() {
        return mContactId;
    }

    @Override
    public String getLookupKey() {
        return mLookupKey;
    }

    public String getId() {
        return mId;
    }

    @Override
    public String getNormalizedDestination() {
        return mNormalizedDestination;
    }

    // Bkav HienDTk: them id participant de lay id conversation => BOS-2335
    public void unblock(final Context context, String participantID) {
        updateNotification(participantID, context);
        UpdateDestinationBlockedAction.updateDestinationBlocked(
                mNormalizedDestination, false, null,
                BugleActionToasts.makeUpdateDestinationBlockedActionListener(context));
    }
    /**
     * HienDTk: update notification khi bo chan tin nhan => BOS-2335
     */
    public void updateNotification(String participantID, Context context){
        Cursor cursor = context.getContentResolver().query(MessagingContentProvider.PARTICIPANTS_CONVERSATIONS_URI, ParticipantRefresh.ConversationParticipantsQuery.PROJECTION, DatabaseHelper.ConversationParticipantsColumns.PARTICIPANT_ID + "=?", new String[]{participantID}, null);
        if(cursor != null){
            while (cursor.moveToNext()){
                String conversationId = cursor.getString(1);
                UpdateConversationOptionsAction.enableConversationNotifications(
                        conversationId, true);
            }
        }
    }
}
