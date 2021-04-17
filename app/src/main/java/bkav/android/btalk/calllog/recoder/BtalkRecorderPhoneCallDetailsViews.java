package bkav.android.btalk.calllog.recoder;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.dialer.calllog.CallTypeIconsView;
import com.android.dialer.calllog.PhoneCallDetailsViews;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.BtalkCallTypeIconsView;
import bkav.android.btalk.calllog.adapter.BtalkPhoneCallDetailsViews;
/*
* */
public class BtalkRecorderPhoneCallDetailsViews extends PhoneCallDetailsViews {
    private BtalkRecorderPhoneCallDetailsViews(TextView nameView, View callTypeView,
                                               CallTypeIconsView callTypeIcons, TextView callLocationAndDate,
                                               TextView voicemailTranscriptionView, ImageView callAccountIcon,
                                               TextView callAccountLabel, TextView viewNumber, RecorderAudioAttachmentView frameLayout, TextView dateIfSpamExist, TextView space) {
        super(nameView, callTypeView, callTypeIcons, callLocationAndDate,
                voicemailTranscriptionView, callAccountIcon, callAccountLabel);
        numberView = viewNumber;
        customRecorder = frameLayout;
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
    public static BtalkRecorderPhoneCallDetailsViews fromView(View view) {
        return new BtalkRecorderPhoneCallDetailsViews((TextView) view.findViewById(R.id.name),
                view.findViewById(R.id.call_type),
                (BtalkCallTypeIconsView) view.findViewById(R.id.call_type_icons),
                (TextView) view.findViewById(R.id.call_location_and_date),
                (TextView) view.findViewById(R.id.voicemail_transcription),
                (ImageView) view.findViewById(R.id.call_account_icon),
                (TextView) view.findViewById(R.id.call_account_label),
                (TextView) view.findViewById(R.id.number),
                (RecorderAudioAttachmentView) view.findViewById(R.id.recoder_audio_attachment_framelayout),
                (TextView) view.findViewById(R.id.date_label_if_spam_exist),
                (TextView) view.findViewById(R.id.number_date_space)
        );
    }

}
