package bkav.android.btalk.messaging.ui;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.android.messaging.ui.AudioAttachmentView;
import com.android.messaging.ui.AudioPlaybackProgressBar;
import com.android.messaging.ui.ConversationDrawables;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.ui.cutomview.BtalkPlaybackSeekbar;

/**
 * Created by quangnd on 12/04/2017.
 * class custom lai AudioAttachmentView cua source goc
 */

public class BtalkAudioAttachmentView extends AudioAttachmentView implements SeekBar.OnSeekBarChangeListener {

    private BtalkPlaybackSeekbar mSeekBar;

    private Handler mSeekbarHandler;

    private SeekRunnable mSeekbarRunnable;

    /**
    * Bkav QuangNDb Custom runnable update seekbar
    */
    private static class SeekRunnable implements Runnable {

        final WeakReference<BtalkAudioAttachmentView> mWeakReference;

        public SeekRunnable(BtalkAudioAttachmentView btalkAudioAttachmentView) {
            mWeakReference = new WeakReference<>(btalkAudioAttachmentView);
        }

        @Override
        public void run() {
            mWeakReference.get().updateSeekbar();
        }
    }

    @Override
    protected void onFinishInflate() {
        mSeekbarHandler = new Handler();
        mSeekbarRunnable = new SeekRunnable(this);
        super.onFinishInflate();
    }

    private static final int TIME_DELAY_UPDATE = 100;

    /**
     * Bkav QuangNDb Update pregress cua seekbar
     */
    private void updateSeekbar() {
        if (mMediaPlayer != null) {
            if (mPlaybackFinished) {
                mSeekBar.setProgress(0);
                mSeekbarHandler.removeCallbacksAndMessages(mSeekbarHandler);
            } else if (mPrepared) {
                mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
            }
            mSeekbarHandler.postDelayed(mSeekbarRunnable, TIME_DELAY_UPDATE);
        } else {
            mSeekbarHandler.removeCallbacksAndMessages(mSeekbarHandler);
        }
    }

    @Override
    protected void playAudio() {
        super.playAudio();
        updateSeekbar();
    }

    public BtalkAudioAttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected int getIdLayout() {
        return R.layout.btalk_audio_attchment_view;
    }

    @Override
    protected int getMessageIncomingTextColorId() {
        return R.color.btalk_message_text_color_incoming;
    }


    @Override
    protected int getMessageOutgoingTextColorId() {
        return R.color.btalk_message_text_color_outgoing;
    }

    @Override
    protected ConversationDrawables getConversationDrawables() {
        return BtalkConversationDrawables.get();
    }

    @Override
    public AudioPlaybackProgressBar getProgressBar() {
        return (BtalkAudioPlaybackProgressBar) findViewById(R.id.progress);
    }

    /**
     * Bkav QuangNDb ham khoi tao progressbar
     */
    protected void initProgressbar() {
        mSeekBar = (BtalkPlaybackSeekbar) findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    /**
     * Bkav QuangNDb Dung progressbar lai
     */
    protected void pauseProgressbar() {
    }

    /**
     * Bkav QuangNDb Restart progressbar
     */
    protected void restartProgressbar() {
    }

    /**
     * Bkav QuangNDb ham set visual style progress bar
     */
    protected void setVisualStyleProgressbar() {
        mSeekBar.setVisualStyle(mUseIncomingStyle);
    }

    /**
     * Bkav QuangNDb Set progressbar visible or gone, invisible
     */
    protected void setProgressbarVisible(int visible) {
        mSeekBar.setVisibility(visible);
    }

    /**
     * Bkav QuangNDb resume progress bar
     */
    protected void resumeProgressbar() {
    }

    /**
     * Bkav QuangNDb reset progressbar
     */
    protected void resetProgressbar() {
    }

    /**
     * Bkav QuangNDb set duration cho progressbar
     */
    protected void setDurationProgressbar() {
        mSeekBar.setMax(mMediaPlayer.getDuration());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mPlaybackFinished) {
            mChronometer.setText(formatTimer(mMediaPlayer.getDuration()));
        } else if (mMediaPlayer != null && mPrepared) {
            mChronometer.setText(formatTimer(progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            pauseAudio();
        }
        updatePlayPauseButtonState();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mMediaPlayer != null && mPrepared) {
            mMediaPlayer.seekTo(seekBar.getProgress());
            playAudio();
        } else {
            if (mStartPlayAfterPrepare) {
                // The user is (starting and) pausing before the MediaPlayer is prepared
                mStartPlayAfterPrepare = false;
            } else {
                mStartPlayAfterPrepare = true;
                setupMediaPlayer();
                mMediaPlayer.seekTo(seekBar.getProgress());
            }
        }
        updatePlayPauseButtonState();
    }

    //=======================================Timer==================================================

    @Override
    protected void pauseTimer() {
    }

    @Override
    protected void resetTimer() {
    }

    @Override
    protected void resumeTimer() {
    }

    @Override
    protected void restartTimer() {
    }

    @Override
    protected void setBaseTimer() {
        mChronometer.setText(formatTimer(mMediaPlayer.getDuration()));
    }

    private static final int ONE_HOUR = 3600000;

    /**
    * Bkav QuangNDb Format text time hien thi
     *
    */
    private String formatTimer(int progress) {
        String currentProgressTextView = String.format(Locale.getDefault(),
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(progress),
                TimeUnit.MILLISECONDS.toSeconds(progress)

                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                        .toMinutes(progress)));
        String currentProgressTextViewMoreOneHour = String.format(Locale.getDefault(),
                "%02d:%02d:02d",
                TimeUnit.MILLISECONDS.toHours(progress),
                TimeUnit.MILLISECONDS.toMinutes(progress)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                        .toHours(progress)),
                TimeUnit.MILLISECONDS.toSeconds(progress)

                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                        .toMinutes(progress)));
        if (progress < ONE_HOUR) {
            return currentProgressTextView;
        } else {
            return currentProgressTextViewMoreOneHour;
        }
    }
}
