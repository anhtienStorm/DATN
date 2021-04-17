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

import android.app.FragmentManager;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ui.AsyncImageView.AsyncImageViewDelayLoader;
import com.android.messaging.ui.CursorRecyclerAdapter;
import com.android.messaging.ui.conversation.ConversationMessageView.ConversationMessageViewHost;
import com.android.messaging.util.Assert;

/**
 * Provides an interface to expose Conversation Message Cursor data to a UI widget like a
 * RecyclerView.
 */
public class ConversationMessageAdapter extends
    CursorRecyclerAdapter<ConversationMessageAdapter.ConversationMessageViewHolder> {

    private ConversationMessageViewHost mHost;
    private final AsyncImageViewDelayLoader mImageViewDelayLoader;
    private final View.OnClickListener mViewClickListener;
    private final View.OnLongClickListener mViewLongClickListener;
    private boolean mOneOnOne;
    private String mSelectedMessageId;


    public ConversationMessageAdapter(final Context context, final Cursor cursor,
                                      final ConversationMessageViewHost host,
                                      final AsyncImageViewDelayLoader imageViewDelayLoader,
                                      final View.OnClickListener viewClickListener,
                                      final View.OnLongClickListener longClickListener) {
        super(context, cursor, 0);
        mHost = host;
        mViewClickListener = viewClickListener;
        mViewLongClickListener = longClickListener;
        mImageViewDelayLoader = imageViewDelayLoader;
        setHasStableIds(true);
    }

    @Override
    public void bindViewHolder(final ConversationMessageViewHolder holder,
            final Context context, final Cursor cursor) {
        Assert.isTrue(holder.mView instanceof ConversationMessageView);
        final ConversationMessageView conversationMessageView =
                concatConversationMessageView(holder.mView);
        conversationMessageView.bind(cursor, mOneOnOne, mSelectedMessageId);
        setAnimation(holder.mView,cursor.getPosition(), conversationMessageView);
    }


    protected void setAnimation(View view, int position, ConversationMessageView conversationMessageView) {

    }

    @Override
    public ConversationMessageViewHolder createViewHolder(final Context context,
            final ViewGroup parent, final int viewType) {
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final ConversationMessageView conversationMessageView = initConversationMessageView(layoutInflater);
        conversationMessageView.setHost(mHost);
        conversationMessageView.setImageViewDelayLoader(mImageViewDelayLoader);
        return new ConversationMessageViewHolder(conversationMessageView,
                            mViewClickListener, mViewLongClickListener);
    }

    public void setSelectedMessage(final String messageId) {
        mSelectedMessageId = messageId;
        notifyDataSetChanged();
    }

    public void setOneOnOne(final boolean oneOnOne, final boolean invalidate) {
        if (mOneOnOne != oneOnOne) {
            mOneOnOne = oneOnOne;
            if (invalidate) {
                notifyDataSetChanged();
            }
        }
    }


    /**
    * ViewHolder that holds a ConversationMessageView.
    */
    public static class ConversationMessageViewHolder extends RecyclerView.ViewHolder {
        final View mView;

        /**
         * @param viewClickListener a View.OnClickListener that should define the interaction when
         *        an item in the RecyclerView is clicked.
         */
        public ConversationMessageViewHolder(final View itemView,
                final View.OnClickListener viewClickListener,
                final View.OnLongClickListener viewLongClickListener) {
            super(itemView);
            mView = itemView;

            mView.setOnClickListener(viewClickListener);
            mView.setOnLongClickListener(viewLongClickListener);
        }
    }

    /**
     * ----------------------------------------BKAV-------------------------------------
     * BKav QuangNDb tach code ra de custom lai o class con
     */
    protected ConversationMessageView initConversationMessageView(LayoutInflater layoutInflater) {
        return (ConversationMessageView)
                layoutInflater.inflate(R.layout.conversation_message_view, null);
    }

    /**
     * Bkav QuangNDb tach code doan concat ConversationMessageView de override o lop con
     */
    protected ConversationMessageView concatConversationMessageView(View view) {
        return (ConversationMessageView)view;
    }

    // BKav QuangNDb interface gui su kien thay end animation
    public interface OnMessageAnimationEndListener {
        void onMessageAnimationEnd();
    }

    // Bkav QuangNDb them bien listener end animation
    protected OnMessageAnimationEndListener mListener;

    // Ham setlistener end animation
    public void setListener(OnMessageAnimationEndListener listener) {
        this.mListener = listener;
    }

    //Bkav QuangNDb ham giai phong listener
    public void releaseListener() {
        mListener = null;
    }

    public void unbind() {
        mHost = null;
    }

}
