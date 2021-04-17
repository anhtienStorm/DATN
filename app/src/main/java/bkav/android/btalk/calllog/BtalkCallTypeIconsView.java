package bkav.android.btalk.calllog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;

import com.android.contacts.common.util.BitmapUtil;
import com.android.dialer.calllog.CallTypeIconsView;
import com.android.dialer.util.AppCompatConstants;

import bkav.android.btalk.R;

/*
 * Copyright (C) 2011 The Android Open Source Project
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


/**
 *
 * Created by anhdt on 01/04/2017.
 * View that draws one or more symbols for different types of calls (missed calls, outgoing etc).
 * The symbols are set up horizontally. As this view doesn't create subviews, it is better suited
 * for ListView-recycling that a regular LinearLayout using ImageViews.
 */

public class BtalkCallTypeIconsView extends CallTypeIconsView {

    protected static BtalkResources sResources;

    public BtalkCallTypeIconsView(Context context) {
        super(context);
    }

    public BtalkCallTypeIconsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initResource(Context context) {
        if (sResources == null) {
            sResources = new BtalkResources(context);
        }
    }

    @Override
    public void add(int callType) {
        mCallTypes.add(callType);

        // Anhdts chi hien thi 1 icon
        if (mCallTypes.size() == 1) {
            final Drawable drawable = getCallTypeDrawable(callType);
            mWidth += drawable.getIntrinsicWidth() + sResources.iconMargin;
            mHeight = Math.max(mHeight, drawable.getIntrinsicHeight());
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int left = 0;
        if (!mCallTypes.isEmpty()) {
            final Drawable drawable = getCallTypeDrawable(mCallTypes.get(0));
            final int right = left + drawable.getIntrinsicWidth();
            drawable.setBounds(left, 0, right, drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            left = right + sResources.iconMargin;
        }

        // If showing the video call icon, draw it scaled appropriately.
        if (mShowVideo) {
            final Drawable drawable = sResources.videoCall;
            final int right = left + drawable.getIntrinsicWidth();
            drawable.setBounds(left, 0, right, drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            left = right + sResources.iconMargin;
        }

        if (!mCallTypes.isEmpty()) {
            final Drawable drawableIms = getImsOrWifiDrawable(mCallTypes.get(0));
            if (drawableIms != null) {
                final int right = left + drawableIms.getIntrinsicWidth();
                drawableIms.setBounds(left, 0, right, drawableIms.getIntrinsicHeight());
                drawableIms.draw(canvas);
            }
        }
    }

    /**
     *  Anhdts call child to use child's sResources
     */
    @Override
    protected Drawable getCallTypeDrawable(int callType) {
        switch (callType) {
            case AppCompatConstants.CALLS_INCOMING_TYPE:
            case AppCompatConstants.INCOMING_IMS_TYPE:
            case AppCompatConstants.INCOMING_WIFI_TYPE:
                return mShowVideo ? sResources.incomingVideo : sResources.incoming;
            case AppCompatConstants.CALLS_OUTGOING_TYPE:
            case AppCompatConstants.OUTGOING_IMS_TYPE:
            case AppCompatConstants.OUTGOING_WIFI_TYPE:
                return mShowVideo ? sResources.outgoingVideo : sResources.outgoing;
            case AppCompatConstants.CALLS_MISSED_TYPE:
            case AppCompatConstants.MISSED_IMS_TYPE:
            case AppCompatConstants.MISSED_WIFI_TYPE:
                return mShowVideo ? sResources.missedVideo : sResources.missed;
            case AppCompatConstants.CALLS_VOICEMAIL_TYPE:
                return sResources.voicemail;
            case AppCompatConstants.CALLS_BLOCKED_TYPE:
                return sResources.blocked;
                // Ahhdts them icon rejected call
            case AppCompatConstants.CALLS_REJECTED_TYPE:
                return mShowVideo ? sResources.rejectedVideo : sResources.rejected;
            default:
                // It is possible for users to end up with calls with unknown call types in their
                // call history, possibly due to 3rd party call log implementations (e.g. to
                // distinguish between rejected and missed calls). Instead of crashing, just
                // assume that all unknown call types are missed calls.
                return mShowVideo ? sResources.missedVideo : sResources.missed;
        }
    }

    /**
     *  Anhdts get icon cho cuoc goi internet
     */
    @Override
    protected Drawable getImsOrWifiDrawable(int callType) {
        switch(callType) {
            case AppCompatConstants.INCOMING_IMS_TYPE:
            case AppCompatConstants.OUTGOING_IMS_TYPE:
            case AppCompatConstants.MISSED_IMS_TYPE:
                return sResources.imsCall;
            case AppCompatConstants.INCOMING_WIFI_TYPE:
            case AppCompatConstants.OUTGOING_WIFI_TYPE:
            case AppCompatConstants.MISSED_WIFI_TYPE:
                return sResources.wifiCall;
            default:
                return null;
        }
    }

    /**
     *    Anhdts make new class follow {@link com.android.dialer.calllog.CallTypeIconsView.Resources}
     */
    protected static class BtalkResources {

        // Drawable representing an incoming answered call.
        public final Drawable incoming;

        // Drawable respresenting an outgoing call.
        public final Drawable outgoing;

        // Drawable representing an incoming missed call.
        public final Drawable missed;

        // Anhdts them icon reject
        public final Drawable rejected;

        // Drawable representing a voicemail.
        public final Drawable voicemail;

        // Drawable representing a blocked call.
        public final Drawable blocked;

        //  Drawable repesenting a video call.
        public final Drawable videoCall;

        // Anhdts them icon cho video
        // Drawable representing an incoming answered call.
        public final Drawable incomingVideo;

        // Drawable respresenting an outgoing call.
        public final Drawable outgoingVideo;

        // Drawable representing an incoming missed call.
        public final Drawable missedVideo;

        public Drawable rejectedVideo;

        /**
         * The margin to use for icons.
         */
        public final int iconMargin;

        /**
         * Drawable repesenting a wifi call.
         */
        public final Drawable wifiCall;

        /**
         * Drawable repesenting a IMS call.
         */
        public final Drawable imsCall;

        /**
         * Configures the call icon drawables.
         * A single white call arrow which points down and left is used as a basis for all of the
         * call arrow icons, applying rotation and colors as needed.
         *
         * @param context The current context.
         */
        public BtalkResources(Context context) {
            final android.content.res.Resources r = context.getResources();

            incoming = ContextCompat.getDrawable(context, R.drawable.bkav_ic_incomming_call).mutate();;
            // incoming.setColorFilter(r.getColor(R.color.answered_call), PorterDuff.Mode.MULTIPLY);

            // Create a rotated instance of the call arrow for outgoing calls.
            outgoing = ContextCompat.getDrawable(context, R.drawable.bkav_ic_outgoing_call).mutate();;
            // outgoing.setColorFilter(r.getColor(R.color.answered_call), PorterDuff.Mode.MULTIPLY);

            // Need to make a copy of the arrow drawable, otherwise the same instance colored
            // above will be recolored here.
            missed = ContextCompat.getDrawable(context, R.drawable.bkav_ic_miss_call).mutate();
            // missed.setColorFilter(r.getColor(R.color.missed_call), PorterDuff.Mode.MULTIPLY);

            // Anhdts them icon rejected
            rejected = ContextCompat.getDrawable(context, R.drawable.bkav_icon_reject);

            voicemail = ContextCompat.getDrawable(context, R.drawable.ic_call_voicemail_holo_dark);

            blocked = getScaledBitmap(context, R.drawable.ic_block_24dp);
            blocked.setColorFilter(r.getColor(R.color.blocked_call), PorterDuff.Mode.MULTIPLY);

            if (mIsCarrierOneSupported) {
                videoCall = ContextCompat.getDrawable(context, R.drawable.volte_video).mutate();
            } else {
                // Get the video call icon, scaled to match the height of the call arrows.
                // We want the video call icon to be the same height as the call arrows, while keeping
                // the same width aspect ratio.
                videoCall = getScaledBitmap(context, R.drawable.ic_videocam_24dp);
            }
            videoCall.setColorFilter(r.getColor(R.color.dialtacts_secondary_text_color),
                    PorterDuff.Mode.MULTIPLY);

            iconMargin = r.getDimensionPixelSize(R.dimen.call_log_icon_margin);

            wifiCall = ContextCompat.getDrawable(context, R.drawable.wifi_calling).mutate();
            wifiCall.setColorFilter(r.getColor(R.color.dialtacts_secondary_text_color),
                    PorterDuff.Mode.MULTIPLY);

            imsCall = ContextCompat.getDrawable(context, R.drawable.volte_voice).mutate();
            imsCall.setColorFilter(r.getColor(R.color.dialtacts_secondary_text_color),
                    PorterDuff.Mode.MULTIPLY);

            // Anhdts icon cho video
            incomingVideo = r.getDrawable(R.drawable.bkav_ic_incomming_call_video).mutate();;

            // Create a rotated instance of the call arrow for outgoing calls.
            outgoingVideo = r.getDrawable(R.drawable.bkav_ic_outgoing_call_video).mutate();;

            // Need to make a copy of the arrow drawable, otherwise the same instance colored
            // above will be recolored here.
            missedVideo = r.getDrawable(R.drawable.bkav_ic_miss_call_video).mutate();;

            rejectedVideo = r.getDrawable(R.drawable.bkav_icon_reject_video).mutate();;

        }

        // Gets the icon, scaled to the height of the call type icons. This helps display all the
        // icons to be the same height, while preserving their width aspect ratio.
        private Drawable getScaledBitmap(Context context, int resourceId) {
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), resourceId);
            int scaledHeight =
                    context.getResources().getDimensionPixelSize(R.dimen.call_type_icon_size);
            int scaledWidth = (int) ((float) icon.getWidth()
                    * ((float) scaledHeight / (float) icon.getHeight()));
            Bitmap scaledIcon = Bitmap.createScaledBitmap(icon, scaledWidth, scaledHeight, false);
            return new BitmapDrawable(context.getResources(), scaledIcon);
        }
    }

    @Override
    public void setShowVideo(boolean showVideo) {
        mShowVideo = showVideo;
        if (mIsCarrierOneSupported) {
            // Don't show video icon in call log item. For CarrierOne, show more precise icon
            // based on call type in call detail history.
            return;
        }

        if (showVideo) {
            mWidth += sResources.videoCall.getIntrinsicWidth();
            mHeight = Math.max(mHeight, sResources.videoCall.getIntrinsicHeight());
            invalidate();
        }
    }

    @Override
    public void addImsOrVideoIcon(int callType, boolean showVideo) {
        mShowVideo = showVideo;
        if (showVideo) {
            mWidth += sResources.videoCall.getIntrinsicWidth();
            mHeight = Math.max(mHeight, sResources.videoCall.getIntrinsicHeight());
            invalidate();
        } else {
            final Drawable drawable = getImsOrWifiDrawable(callType);
            if (drawable != null) {
                // calculating drawable's width and adding it to total width for correct position
                // of icon.
                // calculating height by max of drawable height and other icons' height.
                mWidth += drawable.getIntrinsicWidth();
                mHeight = Math.max(mHeight, drawable.getIntrinsicHeight());
                invalidate();
            }
        }
    }

    @Override
    public void showVideo(boolean isShow) {
        mShowVideo = isShow;
    }
}
