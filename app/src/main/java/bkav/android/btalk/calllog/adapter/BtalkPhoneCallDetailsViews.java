package bkav.android.btalk.calllog.adapter;

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

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.dialer.calllog.CallTypeIconsView;
import com.android.dialer.calllog.PhoneCallDetailsViews;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.BtalkCallTypeIconsView;

/**
 * Created by anhdt on 31/03/2017.
 * groupView chua cac thong tin chi tiet cuoc goi trong mot hang list view cua
 * giao dien call log. use in {@link BtalkCallLogAdapter}
 * init in {@link BtalkPhoneCallDetailsViews}
 * Encapsulates the views that are used to display the details of a phone call in the call log.
 */
// Bkav HuyNQN them tu khoa public cho lop con ke thua
public class BtalkPhoneCallDetailsViews extends PhoneCallDetailsViews {

    private BtalkPhoneCallDetailsViews(TextView nameView, View callTypeView,
                                       CallTypeIconsView callTypeIcons, TextView callLocationAndDate,
                                       TextView voicemailTranscriptionView, ImageView callAccountIcon,
                                       TextView callAccountLabel, TextView viewNumber, TextView dateIfSpamExist, TextView space) {
        super(nameView, callTypeView, callTypeIcons, callLocationAndDate,
                voicemailTranscriptionView, callAccountIcon, callAccountLabel);
        numberView = viewNumber;
        this.dateIfSpamExist = dateIfSpamExist;
        numberDateSpace = space;
    }

    /**
     * Create a new instance by extracting the elements from the given view.
     * <p>
     * The view should contain three text views with identifiers {@code R.id.name},
     * {@code R.id.date}, and {@code R.id.number}, and a linear layout with identifier
     * {@code R.id.call_types}.
     */
    public static BtalkPhoneCallDetailsViews fromView(View view) {
        return new BtalkPhoneCallDetailsViews((TextView) view.findViewById(R.id.name),
                view.findViewById(R.id.call_type),
                (BtalkCallTypeIconsView) view.findViewById(R.id.call_type_icons),
                (TextView) view.findViewById(R.id.call_location_and_date),
                (TextView) view.findViewById(R.id.voicemail_transcription),
                (ImageView) view.findViewById(R.id.call_account_icon),
                (TextView) view.findViewById(R.id.call_account_label),
                (TextView) view.findViewById(R.id.number),
                (TextView) view.findViewById(R.id.date_label_if_spam_exist),
                (TextView) view.findViewById(R.id.number_date_space)
        );
    }

}

