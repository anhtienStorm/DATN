package bkav.android.btalk.calllog.recoder;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.dialer.calllog.CallLogListItemViewHolder;
import com.android.dialer.calllog.CallLogQueryHandler;
import com.android.dialer.calllog.ContactInfoHelper;
import com.android.dialer.calllog.PhoneCallDetailsHelper;
import com.android.dialer.filterednumber.BlockNumberDialogFragment;
import com.android.dialer.logging.InteractionEvent;
import com.android.dialer.logging.Logger;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;
import com.android.messaging.Factory;

import java.util.TreeSet;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.adapter.BtalkCallLogAdapter;

/**
 * HuyNQn cai dat lai cho BtlkCallLogRecoderAdapter
 */

public class BtlkCallLogRecoderAdapter extends BtalkCallLogAdapter {

    private RecorderAudioAttachmentView mAudioAttachmentView;

    private boolean mIsItemCheck;

    public void setIsItemCheck(boolean mIsItemCheck) {
        this.mIsItemCheck = mIsItemCheck;
    }

    public BtlkCallLogRecoderAdapter(Context context, CallFetcher callFetcher, ContactInfoHelper contactInfoHelper, VoicemailPlaybackPresenter voicemailPlaybackPresenter, int activityType) {
        super(context, callFetcher, contactInfoHelper, voicemailPlaybackPresenter, activityType);
        mCallLogGroupBuilder.setRecoder(true);
    }

    /**
     * HuyNQn custom tu lop cha
     */

    @Override
    protected RecyclerView.ViewHolder createCallLogEntryViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.btalk_call_log_recoder_list_item, parent, false);
        CallLogListItemViewHolder viewHolder = createCallLogViewHolder(view);

        viewHolder.mBtalkCallLogEntryView.setTag(viewHolder);
        viewHolder.mBtalkCallLogEntryView.setAccessibilityDelegate(mAccessibilityDelegate);

        viewHolder.primaryActionView.setTag(viewHolder);

        RecorderAudioAttachmentView audioAttachmentView = viewHolder.primaryActionView.findViewById(R.id.recoder_audio_attachment_framelayout);

        viewHolder.primaryActionView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mIsItemCheck = true;
                if(mListCallRecorderCheck != null){
                    boolean flag = false;
                    for(String path : mListCallRecorderCheck){
                        if(audioAttachmentView.getDataResource().equals(path)){
                            setUnSelectItem(v, audioAttachmentView, viewHolder);
                            mListCallRecorderCheck.remove(path);
                            flag =true;
                            break;
                        }
                    }

                    if(!flag){
                        setSelectItem(v, viewHolder);
                        mListCallRecorderCheck.add(audioAttachmentView.getDataResource());
                    }
                }else {
                    mListCallRecorderCheck = new TreeSet<>();
                    setSelectItem(v, viewHolder);
                    mListCallRecorderCheck.add(audioAttachmentView.getDataResource());
                }
                mListenerCheckItem.updateListPath(mListCallRecorderCheck != null ? mListCallRecorderCheck : new TreeSet<>());
                return true;
            }
        });

        viewHolder.primaryActionView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Bkav HuyNQN BOS-2745 start
                // lang nghe viec hien thi detail layout qua viec bam phat audio
                if (mListenner != null) {
                    mListenner.onShowLayout(true);
                }
                if(mIsItemCheck && mListCallRecorderCheck != null){
                    boolean flag = false;
                    for(String path : mListCallRecorderCheck){
                        if(audioAttachmentView.getDataResource().equals(path)){
                            mListCallRecorderCheck.remove(path);
                            setUnSelectItem(v, audioAttachmentView, viewHolder);
                            if(mListCallRecorderCheck.size() == 0){
                                mIsItemCheck = false;
                                mListCallRecorderCheck = null;
                            }
                            flag = true;
                            break;
                        }
                    }

                    if(!flag){
                        setSelectItem(v, viewHolder);
                        mListCallRecorderCheck.add(audioAttachmentView.getDataResource());
                    }
                    mListenerCheckItem.updateListPath(mListCallRecorderCheck != null ? mListCallRecorderCheck : new TreeSet<>());
                }else {
                    audioAttachmentView.setupMediaplayer();
                }
            }
        });

        return viewHolder;
    }


    private void setSelectItem(View v, CallLogListItemViewHolder viewHolder) {
        RecorderAudioAttachmentView view = v.findViewById(R.id.recoder_audio_attachment_framelayout);
        String path = view.getDataResource();
        ImageView checkMark = v.findViewById(R.id.call_log_checkmark);
        checkMark.setVisibility(View.VISIBLE);
        if(mListenner != null){
            mListenner.showActionModeView();
        }
    }
    private void setUnSelectItem(View view, RecorderAudioAttachmentView audioAttachmentView, CallLogListItemViewHolder viewHolder){
        ImageView checkMark = view.findViewById(R.id.call_log_checkmark);
        checkMark.setVisibility(View.GONE);
        if(mListenner != null){
            mListenner.goneActionModeView();
        }
    }

    private CallLogListItemViewHolder createCallLogViewHolder(View view) {
        return BtalkCallLogRecoderListItemViewHolder.create(
                view,
                mContext,
                this,
                mExpandCollapseListener,
                mCallLogCache,
                mCallLogListItemHelper,
                mVoicemailPlaybackPresenter,
                mFilteredNumberAsyncQueryHandler,
                new BlockNumberDialogFragment.Callback() {
                    @Override
                    public void onFilterNumberSuccess() {
                        Logger.logInteraction(
                                InteractionEvent.BLOCK_NUMBER_CALL_LOG);
                    }

                    @Override
                    public void onUnfilterNumberSuccess() {
                        Logger.logInteraction(
                                InteractionEvent.UNBLOCK_NUMBER_CALL_LOG);
                    }

                    @Override
                    public void onChangeFilteredNumberUndo() {
                    }
                }, mActivityType == ACTIVITY_TYPE_ARCHIVE);
    }

    // Bkav HuyNQN tao ra doi tuong BtalkPhoneCallRecoderDetailsHelper moi
    @Override
    protected PhoneCallDetailsHelper createPhoneCallDetailsHelper(Resources resources) {
        return new BtalkPhoneCallRecoderDetailsHelper(mContext, resources, mCallLogCache);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
    }

    // Bkav HuyNQN Thuc hien set pathAudio cho cac item tuong ung va service cua CustomView
    @Override
    public void setPathRecorder(CallLogListItemViewHolder view, String path) {
        mAudioAttachmentView = view.primaryActionView.findViewById(R.id.recoder_audio_attachment_framelayout);
        mAudioAttachmentView.setDataResource(path);
        if (path != null && !path.isEmpty()) {
            //Bkav QuangNDb kiem tra xem path file da bi xoa thi xoa trong call log luon
            // Bkav HuyNQN fix thay cap dau '' bang "" de fix loi ten nguoi dung su dung dau '
            final Cursor cursor = Factory.get().getApplicationContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null,
                    MediaStore.Audio.Media.DATA + " LIKE \"" + path + "\"",
                    null, null);
            if (cursor ==null || cursor.getCount() == 0) {
                DeleteCallRecordPathAction.deleteCallRecordPath(path);
            }
            if (cursor != null) {
                cursor.close();
            }
        }

        mAudioAttachmentView.setService(mService);

    }

    // Bkav HuyNQN thuc hien set check item
    @Override
    public void setItemCheck(CallLogListItemViewHolder views, long callId, String path) {
        if(mListCallRecorderCheck != null){
            if(mListCallRecorderCheck.size() == 0){
                ImageView checkView = views.primaryActionView.findViewById(R.id.call_log_checkmark);
                if(checkView.getVisibility() == View.VISIBLE){
                    checkView.setVisibility(View.GONE);
                }
            }else {
                for (String pathRecorder : mListCallRecorderCheck) {
                    if (pathRecorder.equals(path)) {
                        ImageView checkView = views.primaryActionView.findViewById(R.id.call_log_checkmark);
                        checkView.setVisibility(View.VISIBLE);
                    }
                }
            }

        }
    }

    public interface RecorderAdapterListenner {
        void onShowLayout(boolean isShow);

        void showActionModeView();

        void goneActionModeView();
    }

    public void setListenner(RecorderAdapterListenner mListenner) {
        this.mListenner = mListenner;
    }

    private RecorderAdapterListenner mListenner;

    public void setListenerCheckItem(BtalkCallLogRecoderFragment.CheckItemListener mListenerCheckItem) {
        this.mListenerCheckItem = mListenerCheckItem;
    }

    private TreeSet<String> mListCallRecorderCheck; // Bkav HuyNQN luu lai danh sach path file dang duoc danh dau
    private BtalkCallLogRecoderFragment.CheckItemListener mListenerCheckItem;

    @Override
    public void setSelectAll(boolean selectAll) {
        mIsItemCheck = selectAll;
        if (selectAll) {
            if (mListCallRecorderCheck == null) {
                mListCallRecorderCheck = new TreeSet<>();
            } else {
                mListCallRecorderCheck.clear();
            }

            Cursor c = (Cursor) getItem(0);
            if (c != null && c.getCount() > 0) {
                int currentPos = c.getPosition();
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    mListCallRecorderCheck.add(c.getString(c.getColumnIndex(CallLogQueryHandler.RECORD_CALL_DATA)));
                }
                c.moveToPosition(currentPos);
            }
        } else {
            if (mListCallRecorderCheck != null) {
                mListCallRecorderCheck.clear();
            }
        }
        mListenerCheckItem.updateListPath(mListCallRecorderCheck != null ? mListCallRecorderCheck : new TreeSet<>());
        notifyDataSetChanged();
    }
}
