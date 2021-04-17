package bkav.android.btalk.calllog.recoder;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dialer.calllog.CallLogListItemHelper;
import com.android.dialer.calllog.CallLogListItemViewHolder;
import com.android.dialer.calllog.PhoneCallDetailsViews;
import com.android.dialer.calllog.calllogcache.CallLogCache;
import com.android.dialer.database.FilteredNumberAsyncQueryHandler;
import com.android.dialer.filterednumber.BlockNumberDialogFragment;
import com.android.dialer.service.ExtendedBlockingButtonRenderer;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.adapter.BtalkCallLogListItemViewHolderNew;
import bkav.android.btalk.calllog.adapter.BtalkPhoneCallDetailsViews;
import bkav.android.btalk.fragments.dialpad.BtalkCheckableQuickContactBadge;

/**
 * HuyNQn de sua lai cac ham minh can khoi tao lai tuong ung voi cac lop
 */

public class BtalkCallLogRecoderListItemViewHolder extends BtalkCallLogListItemViewHolderNew {

    public BtalkCallLogRecoderListItemViewHolder(Context context, ExtendedBlockingButtonRenderer.Listener eventListener, View.OnClickListener expandCollapseListener, CallLogCache callLogCache, CallLogListItemHelper callLogListItemHelper, VoicemailPlaybackPresenter voicemailPlaybackPresenter, FilteredNumberAsyncQueryHandler filteredNumberAsyncQueryHandler, BlockNumberDialogFragment.Callback filteredNumberDialogCallback, View rootView, QuickContactBadge quickContactView, View primaryActionView, PhoneCallDetailsViews phoneCallDetailsViews, View callLogEntryView, TextView dayGroupHeader, ImageView primaryActionButtonView, boolean isArchiveTab) {
        super(context, eventListener, expandCollapseListener, callLogCache, callLogListItemHelper, voicemailPlaybackPresenter, filteredNumberAsyncQueryHandler, filteredNumberDialogCallback, rootView, quickContactView, primaryActionView, phoneCallDetailsViews, callLogEntryView, dayGroupHeader, primaryActionButtonView, isArchiveTab);
    }

    // Bkav HuyNQN set image recoder
    @Override
    public int getImgResActionButton() {
        return 0; // Bkav HuyNQN return 0 de ko set image cho primaryActionButtonView
    }

    // Bkav HuyNQN xu lu xu kien play/pause file ghi am
    @Override
    public void onClick(View view) {
        // TODO: 06/07/2019  su ly phat file ghi am tai day
    }

    public static CallLogListItemViewHolder create(
            View view,
            Context context,
            ExtendedBlockingButtonRenderer.Listener eventListener,
            View.OnClickListener expandCollapseListener,
            CallLogCache callLogCache,
            CallLogListItemHelper callLogListItemHelper,
            VoicemailPlaybackPresenter voicemailPlaybackPresenter,
            FilteredNumberAsyncQueryHandler filteredNumberAsyncQueryHandler,
            BlockNumberDialogFragment.Callback filteredNumberDialogCallback,
            boolean isArchiveTab) {
        return new BtalkCallLogRecoderListItemViewHolder(
                context,
                eventListener,
                expandCollapseListener,
                callLogCache,
                callLogListItemHelper,
                voicemailPlaybackPresenter,
                filteredNumberAsyncQueryHandler,
                filteredNumberDialogCallback,
                view,
                (BtalkCheckableQuickContactBadge) view.findViewById(R.id.quick_contact_photo),
                view.findViewById(R.id.primary_action_view),
                BtalkRecorderPhoneCallDetailsViews.fromView(view),
                view.findViewById(R.id.call_log_row),
                (TextView) view.findViewById(R.id.call_log_day_group_label),
                (ImageView) view.findViewById(R.id.primary_action_button),
                isArchiveTab);
    }

    // Bkav HuyNQN Tai ItemView khi click se phat file ghi am
    @Override
    protected void initPrimaryView() {
        primaryActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                RecorderAudioAttachmentView view = v.findViewById(R.id.recoder_audio_attachment_framelayout);
//                view.setupMediaplayer();
            }
        });
    }
}
