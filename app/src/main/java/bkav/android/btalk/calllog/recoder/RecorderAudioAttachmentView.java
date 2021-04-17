package bkav.android.btalk.calllog.recoder;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;


import bkav.android.btalk.R;

import static bkav.android.btalk.calllog.recoder.RecorderService.STATE_IDLE;
import static bkav.android.btalk.calllog.recoder.RecorderService.STATE_PAUSE;
import static bkav.android.btalk.calllog.recoder.RecorderService.STATE_RESUME;

public class RecorderAudioAttachmentView extends FrameLayout implements RecorderService.OnRecorderService {

    private ImageButton mPlayOrPauseButton;

    private ProgressBar mRecoderProgressbar;

    private RecorderService mService;

    private boolean mIsItemSelect;

    public RecorderService getService() {
        return mService;
    }

    public void setService(RecorderService mService) {
        this.mService = mService;
        // Bkav HuyNQN loai bo try/catch
        if (mService != null) {
            if (mDataResource != null && mDataResource.equals(mService.getPathAudio())) {
                switch (mService.getState()) {
                    case STATE_IDLE:
                        resetView();
                        break;
                    case STATE_PAUSE:
                        mPlayOrPauseButton.setImageResource(R.drawable.ic_pause_recoder);
                        break;
                    case STATE_RESUME:
                        mPlayOrPauseButton.setImageResource(R.drawable.ic_play_recoder);
                        break;
                }
            } else {
                resetView();
            }
        }
    }

    private String mDataResource;

    public String getDataResource() {
        return mDataResource;
    }

    public void setDataResource(String mDataResource) {
        this.mDataResource = mDataResource;
    }


    public RecorderAudioAttachmentView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPlayOrPauseButton = findViewById(R.id.primary_action_play_audio);
        mPlayOrPauseButton.setImageResource(R.drawable.ic_play_recoder);
        mRecoderProgressbar = findViewById(R.id.recoder_progressbar);
        onClickPlayOrPause();
    }

    private void onClickPlayOrPause() {
        mPlayOrPauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setupMediaplayer();
            }
        });
    }

    // Bkav HuyNQN Cai dat mediaplayer, doi thanh protected
    protected void setupMediaplayer() {
        if (!mDataResource.equals(mService.getPathAudio())) {
            mService.completeMediaPlayer();
            mService.setState(STATE_IDLE);
        }
        if (mService.getState() == STATE_IDLE) {
            // Bkav HuyNQN fix loi khong phat duoc audio tren Android 10 do bi thay doi colum id
            String[] projection = new String[]{MediaStore.Audio.Media._ID};
            // Bkav HuyNQN fix thay cap dau '' bang "" de fix loi ten nguoi dung su dung dau '
            Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    MediaStore.Audio.Media.DATA + " LIKE \"" + getDataResource() + "\"",
                    null, null);
            if (cursor != null && cursor.moveToNext()) {
                int columnIndex = cursor.getColumnIndex(projection[0]);
                long idAudio = cursor.getLong(columnIndex);
                if (mService != null) {
                    mService.setListener(this);
                    mService.setPathAudio(getDataResource());
                    mService.playRecorder(idAudio);
                }
            }
            cursor.close();
            mService.setState(STATE_PAUSE);
        } else if (mService.getState() == STATE_PAUSE) {
            mService.pauseMediaPlayer();
            mService.setState(STATE_RESUME);
        } else {
            mService.startMediaPlayer();
            mService.setState(STATE_PAUSE);
        }
        mService.updateNotification(); // Bkav HuyNQN cap nhat lai trang thai notification khi pause/play
    }

    @Override
    public void onUpdateProgress(int cur) {
        if (mDataResource.equals(mService.getPathAudio())) {
            mRecoderProgressbar.setProgress(cur);
        } /*else {
            mRecoderProgressbar.setProgress(0);
        }*/
    }

    @Override
    public void onTimeDurationAudio(int dur) {
        mRecoderProgressbar.setMax(dur);
    }

    @Override
    public void onPauseAudio() {
        mPlayOrPauseButton.setImageResource(R.drawable.ic_play_recoder);
    }

    private boolean isCurrentItemFocus() {
        return mService != null && mDataResource.equals(mService.getPathAudio());
    }

    @Override
    public void onPlayAudio() {

        // Bkav HuyNQN fix loi nhan khong dung trang thai cua cac nut play
        if (mService.getPathAudio().equals(mDataResource)) {
            mPlayOrPauseButton.setImageResource(R.drawable.ic_pause_recoder);
        }
    }

    @Override
    public void onResumeAudio() {

        // Bkav HuyNQN fix loi nhan khong dung trang thai cua cac nut play
        if (mService.getPathAudio().equals(mDataResource)){
            mPlayOrPauseButton.setImageResource(R.drawable.ic_pause_recoder);
        }
    }

    @Override
    public void onFinishPlayAudio() {
        resetView();
    }

    private void resetView() {
        mRecoderProgressbar.setProgress(0);
        mPlayOrPauseButton.setImageResource(R.drawable.ic_play_recoder);
    }
}
