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
package com.android.messaging.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.ui.mediapicker.PausableChronometer;
import com.android.messaging.util.Assert;
import com.android.messaging.util.ContentType;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.MediaUtil;
import com.android.messaging.util.UiUtils;

/**
 * A reusable widget that hosts an audio player for audio attachment playback. This widget is used
 * by both the media picker and the conversation message view to show audio attachments.
 */
public class AudioAttachmentView extends LinearLayout{
    /** The normal layout mode where we have the play button, timer and progress bar */
    private static final int LAYOUT_MODE_NORMAL = 0;

    /** The compact layout mode with only the play button and the timer beneath it. Suitable
     *  for displaying in limited space such as multi-attachment layout */
    private static final int LAYOUT_MODE_COMPACT = 1;

    /** The sub-compact layout mode with only the play button. */
    private static final int LAYOUT_MODE_SUB_COMPACT = 2;

    private static final int PLAY_BUTTON = 0;
    private static final int PAUSE_BUTTON = 1;

    private AudioAttachmentPlayPauseButton mPlayPauseButton;
    protected PausableChronometer mChronometer;
    private AudioPlaybackProgressBar mProgressBar;
    protected MediaPlayer mMediaPlayer;

    private Uri mDataSourceUri;

    // The corner radius for drawing rounded corners. The default value is zero (no rounded corners)
    private final int mCornerRadius;
    private final Path mRoundedCornerClipPath;
    private int mClipPathWidth;
    private int mClipPathHeight;

    protected boolean mUseIncomingStyle;
    private int mThemeColor;

    protected boolean mStartPlayAfterPrepare;
    // should the MediaPlayer be prepared lazily when the user chooses to play the audio (as
    // opposed to preparing it early, on bind)
    private boolean mPrepareOnPlayback;
    protected boolean mPrepared;
    protected boolean mPlaybackFinished; // Was the audio played all the way to the end
    private final int mMode;
    private Context mContext;

    public AudioAttachmentView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final TypedArray typedAttributes =
                context.obtainStyledAttributes(attrs, R.styleable.AudioAttachmentView);
        mMode = typedAttributes.getInt(R.styleable.AudioAttachmentView_layoutMode,
                LAYOUT_MODE_NORMAL);
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        //Bkav QuangNDb tach code doan lay id layout
        inflater.inflate(getIdLayout(), this, true);
        typedAttributes.recycle();

        setWillNotDraw(mMode != LAYOUT_MODE_SUB_COMPACT);
        mRoundedCornerClipPath = new Path();
        mCornerRadius = context.getResources().getDimensionPixelSize(
                R.dimen.conversation_list_image_preview_corner_radius);
        setContentDescription(context.getString(R.string.audio_attachment_content_description));
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPlayPauseButton = (AudioAttachmentPlayPauseButton) findViewById(R.id.play_pause_button);
        initTextTime();
        // Bkav QuangNDb tach code doan bin progress bar
        initProgressbar();
        mPlayPauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Has the MediaPlayer already been prepared?
                if (mMediaPlayer != null && mPrepared) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                        pauseTimer();
                        pauseProgressbar();
                    } else {
                        playAudio();
                    }
                } else {
                    // Either eager preparation is still going on (the user must have clicked
                    // the Play button immediately after the view is bound) or this is lazy
                    // preparation.
                    if (mStartPlayAfterPrepare) {
                        // The user is (starting and) pausing before the MediaPlayer is prepared
                        mStartPlayAfterPrepare = false;
                    } else {
                        mStartPlayAfterPrepare = true;
                        setupMediaPlayer();
                    }
                }
                updatePlayPauseButtonState();
            }
        });
        updatePlayPauseButtonState();
        initializeViewsForMode();
    }



    /**
     * Bind the audio attachment view with a MessagePartData.
     * @param incoming indicates whether the attachment view is to be styled as a part of an
     *        incoming message.
     */
    public void bindMessagePartData(final MessagePartData messagePartData,
            final boolean incoming, final boolean showAsSelected) {
        Assert.isTrue(messagePartData == null ||
                ContentType.isAudioType(messagePartData.getContentType()));
        final Uri contentUri = (messagePartData == null) ? null : messagePartData.getContentUri();
        bind(contentUri, incoming, showAsSelected);
    }

    public void bind(
            final Uri dataSourceUri, final boolean incoming, final boolean showAsSelected) {
        final String currentUriString = (mDataSourceUri == null) ? "" : mDataSourceUri.toString();
        final String newUriString = (dataSourceUri == null) ? "" : dataSourceUri.toString();

        // Bkav QuangNdb tach code doan init ConversationDrawables
        final int themeColor = getConversationDrawables().getConversationThemeColor();
        final boolean useIncomingStyle = incoming || showAsSelected;
        final boolean visualStyleChanged = mThemeColor != themeColor ||
                mUseIncomingStyle != useIncomingStyle;

        mUseIncomingStyle = useIncomingStyle;
        mThemeColor = themeColor;
        mPrepareOnPlayback = incoming && !MediaUtil.canAutoAccessIncomingMedia();

        if (!TextUtils.equals(currentUriString, newUriString)) {
            mDataSourceUri = dataSourceUri;
            resetToZeroState();
        } else if (visualStyleChanged) {
            updateVisualStyle();
        }
    }

    protected void playAudio() {
        Assert.notNull(mMediaPlayer);
        if (mPlaybackFinished) {
            mMediaPlayer.seekTo(0);
            restartTimer();
            restartProgressbar();
            mPlaybackFinished = false;
        } else {
            resumeTimer();
            resumeProgressbar();
        }
        mMediaPlayer.start();
        stopAudioPlayback();
    }



    private void onAudioReplayError(final int what, final int extra, final Exception exception) {
        if (exception == null) {
            LogUtil.e(LogUtil.BUGLE_TAG, "audio replay failed, what=" + what +
                    ", extra=" + extra);
        } else {
            LogUtil.e(LogUtil.BUGLE_TAG, "audio replay failed, exception=" + exception);
        }
        UiUtils.showToastAtBottom(R.string.audio_recording_replay_failed);
        releaseMediaPlayer();
    }

    /**
     * Prepare the MediaPlayer, and if mPrepareOnPlayback, start playing the audio
     */
    protected void setupMediaPlayer() {
        Assert.notNull(mDataSourceUri);
//        mAudioManager.abandonAudioFocus(this);
        if (mMediaPlayer == null) {
            Assert.isTrue(!mPrepared);
            mMediaPlayer = new MediaPlayer();

            try {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(Factory.get().getApplicationContext(), mDataSourceUri);
                mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(final MediaPlayer mp) {
                        updatePlayPauseButtonState();
                        resetTimer();
                        setBaseTimer();
                        updateChronometerVisibility(false /* playing */);
                        resetProgressbar();

                        mPlaybackFinished = true;
                    }
                });

                mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                    @Override
                    public void onPrepared(final MediaPlayer mp) {
                        // Set base on the chronometer so we can show the full length of the audio.
                        setBaseTimer();
                        setDurationProgressbar();
                        mMediaPlayer.seekTo(0);
                        mPrepared = true;

                        if (mStartPlayAfterPrepare) {
                            mStartPlayAfterPrepare = false;
                            playAudio();
                            updatePlayPauseButtonState();
                        }
                    }
                });

                mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                    @Override
                    public boolean onError(final MediaPlayer mp, final int what, final int extra) {
                        mStartPlayAfterPrepare = false;
                        onAudioReplayError(what, extra, null);
                        return true;
                    }
                });

                mMediaPlayer.prepareAsync();
            } catch (final Exception exception) {
                onAudioReplayError(0, 0, exception);
                releaseMediaPlayer();
            }
        }
    }



    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mPrepared = false;
            mStartPlayAfterPrepare = false;
            mPlaybackFinished = false;
            resetTimer();
            resetProgressbar();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // The view must have scrolled off. Stop playback.
        releaseMediaPlayer();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        if (mMode != LAYOUT_MODE_SUB_COMPACT) {
            return;
        }

        final int currentWidth = this.getWidth();
        final int currentHeight = this.getHeight();
        if (mClipPathWidth != currentWidth || mClipPathHeight != currentHeight) {
            final RectF rect = new RectF(0, 0, currentWidth, currentHeight);
            mRoundedCornerClipPath.reset();
            mRoundedCornerClipPath.addRoundRect(rect, mCornerRadius, mCornerRadius,
                    Path.Direction.CW);
            mClipPathWidth = currentWidth;
            mClipPathHeight = currentHeight;
        }

        canvas.clipPath(mRoundedCornerClipPath);
        super.onDraw(canvas);
    }

    protected void updatePlayPauseButtonState() {
        final boolean playing = mMediaPlayer != null && mMediaPlayer.isPlaying();
        updateChronometerVisibility(playing);
        if (mStartPlayAfterPrepare || playing) {
            mPlayPauseButton.setDisplayedChild(PAUSE_BUTTON);
        } else {
            mPlayPauseButton.setDisplayedChild(PLAY_BUTTON);
        }
    }

    private void resetToZeroState() {
        // Release the media player so it may be set up with the new audio source.
        releaseMediaPlayer();
        updateVisualStyle();
        updateChronometerVisibility(false /* playing */);

        if (mDataSourceUri != null && !mPrepareOnPlayback) {
            // Prepare the media player, so we can read the duration of the audio.
            setupMediaPlayer();
        }
    }

    private void updateVisualStyle() {
        if (mMode == LAYOUT_MODE_SUB_COMPACT) {
            // Sub-compact mode has static visual appearance already set up during initialization.
            return;
        }

        // Bkav QuangNDb tach code doan get Id color cua mChronometer
        if (mUseIncomingStyle) {
            mChronometer.setTextColor(getResources().getColor(getMessageIncomingTextColorId()));
        } else {
            mChronometer.setTextColor(getResources().getColor(getMessageOutgoingTextColorId()));
        }
        setVisualStyleProgressbar();
        mPlayPauseButton.setVisualStyle(mUseIncomingStyle);
        updatePlayPauseButtonState();
    }


    private void initializeViewsForMode() {
        switch (mMode) {
            case LAYOUT_MODE_NORMAL:
                setOrientation(HORIZONTAL);
                setProgressbarVisible(VISIBLE);
                break;

            case LAYOUT_MODE_COMPACT:
                setOrientation(VERTICAL);
                setProgressbarVisible(GONE);
                ((MarginLayoutParams) mPlayPauseButton.getLayoutParams()).setMargins(0, 0, 0, 0);
                ((MarginLayoutParams) mChronometer.getLayoutParams()).setMargins(0, 0, 0, 0);
                break;

            case LAYOUT_MODE_SUB_COMPACT:
                setOrientation(VERTICAL);
                setProgressbarVisible(GONE);
                mChronometer.setVisibility(GONE);
                ((MarginLayoutParams) mPlayPauseButton.getLayoutParams()).setMargins(0, 0, 0, 0);
                final ImageView playButton = (ImageView) findViewById(R.id.play_button);
                playButton.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_preview_play));
                final ImageView pauseButton = (ImageView) findViewById(R.id.pause_button);
                pauseButton.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_preview_pause));
                break;

            default:
                Assert.fail("Unsupported mode for AudioAttachmentView!");
                break;
        }
    }



    /**
     * ---------------------------------------BKAV---------------------------------------
     * Bkav QuangNDb tach code doan get id layout de custom lai o class con
     */
    protected int getIdLayout() {
        return R.layout.audio_attachment_view;
    }

    /**
     * Bkav QuangNDb tach code doan lay id color outgoing
     */
    protected int getMessageOutgoingTextColorId() {
        return R.color.message_text_color_outgoing;
    }

    /**
     * Bkav QuangNdb tach code doan lay id color incoming
     */
    protected int getMessageIncomingTextColorId() {
        return R.color.message_text_color_incoming;
    }

    /**
     * BKav QuangNDb tach code doan int drawable
     */
    protected ConversationDrawables getConversationDrawables() {
        return ConversationDrawables.get();
    }

    /**
     * Tach code doan bin progressbar de override lai o class con
     */
    public AudioPlaybackProgressBar getProgressBar() {
        return (AudioPlaybackProgressBar) findViewById(R.id.progress);
    }

    /**
    * Bkav QuangNDb Pause audio
    */
    public void pauseAudio() {
        if (mMediaPlayer != null && mPrepared) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                pauseTimer();
                pauseProgressbar();
            }
        }
        updatePlayPauseButtonState();
        initializeViewsForMode();
    }

    /**
     * dung lai khi change focus
     */
    private void stopAudioPlayback() {
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(mAudioFocusListener,
                AudioManager.STREAM_RING, AudioManager.AUDIOFOCUS_GAIN);
    }


    /**
     * Listener lang nghe su kien dung lai khi change focus
     */
    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    mRecorderHandler.obtainMessage(FOCUSCHANGE, focusChange, 0)
                            .sendToTarget();
                }
            };

    /**
    * Bkav QuangNDb Handler xu ly message change focus
    */
    static final int FOCUSCHANGE = 0;
    private Handler mRecorderHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FOCUSCHANGE:
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_LOSS:
                            pauseAudio();
                            break;

                        default:
                            break;
                    }
                    break;

                default:
                    break;
            }
        }
    };

    //====================================================================Doan can chuyen sang seekbar===================================
    /**
     * Bkav QuangNDb ham khoi tao progressbar
     */
    protected void initProgressbar() {
        mProgressBar = getProgressBar();
    }

    /**
    * Bkav QuangNDb Dung progressbar lai
    */
    protected void pauseProgressbar() {
        mProgressBar.pause();
    }

    /**
     * Bkav QuangNDb Restart progressbar
     */
    protected void restartProgressbar() {
        mProgressBar.restart();
    }

    /**
     * Bkav QuangNDb ham set visual style progress bar
     */
    protected void setVisualStyleProgressbar() {
        mProgressBar.setVisualStyle(mUseIncomingStyle);
    }

    /**
    * Bkav QuangNDb Set progressbar visible or gone, invisible
    */
    protected void setProgressbarVisible(int visible) {
        mProgressBar.setVisibility(visible);
    }
    /**
     * Bkav QuangNDb resume progress bar
     */
    protected void resumeProgressbar() {
        mProgressBar.resume();
    }

    /**
     * Bkav QuangNDb reset progressbar
     */
    protected void resetProgressbar() {
        mProgressBar.reset();
    }
    /**
     * Bkav QuangNDb set duration cho progressbar
     */
    protected void setDurationProgressbar() {
        mProgressBar.setDuration(mMediaPlayer.getDuration());
    }

    //===============================================================Sua lai doan update text time======================================
    /**
     * Bkav QuangNDb Khoi tao text time
     */
    protected void initTextTime() {
        mChronometer = (PausableChronometer) findViewById(R.id.timer);
    }

    /**
     * Bkav QuangNDb Pause text time
     */
    protected void pauseTimer() {
        mChronometer.pause();
    }
    /**
     * Bkav QuangNDb update visible timer view
     */
    protected void updateChronometerVisibility(final boolean playing) {
        if (mChronometer.getVisibility() == View.GONE) {
            // The chronometer is always GONE for LAYOUT_MODE_SUB_COMPACT
            Assert.equals(LAYOUT_MODE_SUB_COMPACT, mMode);
            return;
        }

        if (mPrepareOnPlayback) {
            // For lazy preparation, the chronometer will only be shown during playback
            mChronometer.setVisibility(playing ? View.VISIBLE : View.INVISIBLE);
        } else {
            mChronometer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Bkav QuangNDb reset timeer
     */
    protected void restartTimer() {
        mChronometer.restart();
    }

    /**
     * Bkav QuangNDb Resume timer
     */
    protected void resumeTimer() {
        mChronometer.resume();
    }

    /**
     * Bkav QuangNDb reeset timer
     */
    protected void resetTimer() {
        mChronometer.reset();
    }

    /**
     * Bkav QuangNDb Set base timer
     */
    protected void setBaseTimer() {
        mChronometer.setBase(SystemClock.elapsedRealtime() -
                mMediaPlayer.getDuration());
    }

}
