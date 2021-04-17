package bkav.android.btalk.calllog.dialer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.android.dialer.PhoneCallDetails;
import com.android.dialer.calllog.CallDetailHistoryAdapter;
import com.android.dialer.calllog.CallTypeHelper;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.recoder.RecorderAudioAttachmentView;
import bkav.android.btalk.calllog.recoder.RecorderService;
/*
*HuyNQn xu ly ham duoc tach o lop cha
* */
public class BtalkCallDetailHistoryAdapter extends CallDetailHistoryAdapter {
    protected RecorderService mService;

    public void setService(RecorderService Service) {
        this.mService = Service;
        notifyDataSetChanged();
    }
    public BtalkCallDetailHistoryAdapter(Context context, LayoutInflater layoutInflater, CallTypeHelper callTypeHelper, PhoneCallDetails[] phoneCallDetails) {
        super(context, layoutInflater, callTypeHelper, phoneCallDetails);
    }

    // Bkav HuyNQN xu ly lai ham duoc tach ra o lop cha
    @Override
    public void setRecorderAudioCustomView(PhoneCallDetails details, View result) {
        RecorderAudioAttachmentView recorderAudioAttachmentView=result.findViewById(R.id.recoder_audio_attachment_framelayout);
        if(details.isRecorder){
            recorderAudioAttachmentView.setVisibility(View.VISIBLE);
            recorderAudioAttachmentView.setDataResource(details.mPathRecorder);
            recorderAudioAttachmentView.setService(mService);
        }else {
            recorderAudioAttachmentView.setVisibility(View.GONE);
        }
    }
}
