package bkav.android.btalk.calllog.recoder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.R;

import static android.os.Build.VERSION_CODES.M;

public class RecorderService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer mMediaPlayer;
    /*
     * HuyNQn cac trang thai cua mediaplayer khi service duoc goi
     * */
    public static final int STATE_IDLE = 0;
    public static final int STATE_RESUME = 1;
    public static final int STATE_PAUSE = 2;

    /*
     * Bkav HuyNQN them bien xu ly foreground service
     * */
    public static final String ACTION_CONNECT = "ACTION_CONNECT";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_EXIT = "ACTION_EXIT";
    public static final int NOTIFICATION_ID = 101;
    public static final String ACTION_CALL_RECORD = "ACTION_CALL_RECORD";
    private PendingIntent mPendingIntent;
    private PendingIntent mPendindPlayIntent;
    private PendingIntent mPendingPauseIntent;
    private PendingIntent mPendingExitIntent;
    // Bkav HienDTk: bien check xem service da duoc bound hay chua
    private boolean mIsBoundService = false;

    private int mState = STATE_IDLE;

    public void setState(int mState) {
        this.mState = mState;
    }

    // Bkav HuyNQN path cua audio dang duoc play hoac pause
    private String mPathAudio;

    public String getPathAudio() {
        return mPathAudio;
    }

    public void setPathAudio(String path) {
        mPathAudio = path;
    }

    private final IBinder mRecorderBind = new RecorderBinder();

    public void pauseMediaPlayer() {

        // Bkav HuyNQN xua lai theo logic obsever
        for (OnRecorderService onRecorderService : mCallbackList) {
            onRecorderService.onPauseAudio();
        }
        mMediaPlayer.pause();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void startMediaPlayer() {
        mMediaPlayer.start();

        // Bkav HuyNQN xua lai theo logic obsever
        for (OnRecorderService onRecorderService :
                mCallbackList) {
            onRecorderService.onResumeAudio();
        }
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public int getCurentPos() {
        return mMediaPlayer.getCurrentPosition();
    }

    public void completeMediaPlayer() {
        onCompletion(mMediaPlayer);
    }

    public int getState() {
        return mState;
    }

    // Bkav HuyNQN thuc hien tua am thanh khi keo seekbar
    public void seekAudio(int time) {
        mMediaPlayer.seekTo(time);
    }

    private long mIdAudio;

    public long getIdAudio() {
        return mIdAudio;
    }

    public interface OnRecorderService {

        void onUpdateProgress(int cur);

        void onTimeDurationAudio(int dur);

        void onPlayAudio();

        void onResumeAudio();

        void onPauseAudio();

        void onFinishPlayAudio();

        default void exitWindows() {

        }
    }

    private OnRecorderService mListener;
    public List<OnRecorderService> mCallbackList = new ArrayList<>();

    // Bkav HuyNQN xua lai logic tuong tu nhu obsever
    public void setListener(OnRecorderService listener) {
//        this.mListener = listener;
        mCallbackList.add(listener);
    }

    private Handler mHandler = new Handler();

    // Bkav HuyNQN thuc hien gui gia tri thoi gian dang phat audio de progressbar cap nhat
    private Runnable mRunable = new Runnable() {
        @Override
        public void run() {

            // Bkav HuyNQN xua lai theo logic obsever
            for (OnRecorderService onRecorderService : mCallbackList) {
                onRecorderService.onUpdateProgress(mMediaPlayer.getCurrentPosition());
            }
            mHandler.postDelayed(this, 100);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mRecorderBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerCallReceiver();
        mMediaPlayer = new MediaPlayer();
        setUpMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mIsBoundService = true;

        // Bkav HuyNQN cac truong hop xu ly khi chay foreground
        if (intent!=null&&intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_PLAY)) {
                mMediaPlayer.start();
                updateNotification();
                for (OnRecorderService onRecorderService :
                        mCallbackList) {
                    onRecorderService.onResumeAudio();
                }
            } else if (intent.getAction().equals(ACTION_PAUSE)) {
                mMediaPlayer.pause();
                updateNotification();
                for (OnRecorderService onRecorderService :
                        mCallbackList) {
                    onRecorderService.onPauseAudio();
                }
            } else if (intent.getAction().equals(ACTION_EXIT)) {
                for (OnRecorderService onRecorderService : mCallbackList) {
                    onRecorderService.exitWindows();
                }
                stopForeground(true);
                stopSelf();
            }
            return START_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // Bkav HienDTk: fix loi Service not registered => BOS-2519 - Start
    public boolean checkServiceBound(){
        return mIsBoundService;
    }
    // Bkav HienDTk: fix loi Service not registered => BOS-2519 - End

    @Override
    public void onCompletion(MediaPlayer mp) {
        mState = STATE_IDLE;

        // Bkav HuyNQN xua lai theo logic obsever
        for (OnRecorderService onRecorderService : mCallbackList) {
            onRecorderService.onFinishPlayAudio();
        }
        mHandler.removeCallbacks(mRunable);
        if (mMediaPlayer.getCurrentPosition() != 0) {
            mp.reset();
        }
        stopForeground(true); // Bkav HuyNQN stop foreground khi phat xong file ghi am
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

        // Bkav HuyNQN cai dat cac pendingIntent khi chay foreground
        Intent notificationIntent = new Intent(this, CallLogRecoderActivity.class);
        notificationIntent.setAction(ACTION_CALL_RECORD);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent(this, RecorderService.class);
        playIntent.setAction(ACTION_PLAY);
        mPendindPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        Intent pauseIntent = new Intent(this, RecorderService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        mPendingPauseIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

        Intent exitIntent = new Intent(this, RecorderService.class);
        exitIntent.setAction(ACTION_EXIT);
        mPendingExitIntent = PendingIntent.getService(this, 0, exitIntent, 0);

        createNotification(mPendingIntent,mPendindPlayIntent,mPendingPauseIntent,mPendingExitIntent);

        // Bkav HuyNQN xua lai theo logic obsever
        for (OnRecorderService onRecorderService : mCallbackList) {
            onRecorderService.onTimeDurationAudio(mMediaPlayer.getDuration());
            onRecorderService.onPlayAudio();
        }
        mHandler.post(mRunable);
    }

    // Bkav HuyNQN thu hien setDataResource và chuyen mediaplayer sang onPrepare;
    public void playRecorder(long id) {
        mMediaPlayer.reset();
        mIdAudio = id;
        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), trackUri);
            mMediaPlayer.prepare(); // Bkav HuyNQN fix loi phat ghi am bi crash
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Bkav HuyNQN Cai dat mediaplayer
    public void setUpMediaPlayer() {
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    // Bkav HuyNQN Thuc hien stop mediaplayer
    public void stopMediaPlayer() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        setState(STATE_IDLE);
    }

    // Bkav HuyNQN bind service
    public class RecorderBinder extends Binder {

        public RecorderService getService() {
            return RecorderService.this;
        }
    }

    // Bkav HuyNQN lay ten file ghi am
    private String getNameAudio() {
        if (mPathAudio != null) {
            String pathAudio = getPathAudio();
            int start = pathAudio.lastIndexOf("/") + 1; // Bkav HuyNQN lay vi tri sau dau "/"
            String nameAudio = pathAudio.substring(start);
            return nameAudio;
        } else {
            return getResources().getString(R.string.call_recorder);
        }
    }

    // Bkav HuyNQN fix loi sau khi nang version sdk 28
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotification(PendingIntent contentIntent, PendingIntent play, PendingIntent pause, PendingIntent exit){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = getString(R.string.app_name);
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setDescription(channelId);
        notificationChannel.setSound(null, null);

        notificationManager.createNotificationChannel(notificationChannel);
        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle(getNameAudio())
                .setContentText(getResources().getString(R.string.play_call_recorder))
                .setSmallIcon(R.drawable.ic_call_recoder_laucher)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .addAction(R.drawable.ic_play_recoder, mMediaPlayer.isPlaying() ? getResources().getString(R.string.button_pause) : getResources().getString(R.string.button_play),
                        mMediaPlayer.isPlaying() ? pause : play )
                .addAction(R.drawable.ic_pause_recoder, getResources().getString(R.string.button_exit), exit)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    // Bkav HuyNQN thuc hien cap nhan lai trang thai cua notification khi pause hoac play
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateNotification(){
        createNotification(mPendingIntent,mPendindPlayIntent,mPendingPauseIntent,mPendingExitIntent);
    }

    // Bkav HuyNQN stop foreground
    public void onStopSelf(){
            stopMediaPlayer();
            mHandler.removeCallbacks(mRunable);
            stopForeground(true);
//            stopSelf();
    }

    private BroadcastReceiver mCallReceiver;
    private void registerCallReceiver() {
        mCallReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.PHONE_STATE".equals(action)) {
                    try {
                        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                        //Incoming call, ringing
                        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING) || state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                            // Bkav HuyNQN khi co cuoc goi thi pause lai thoi
                            pauseMediaPlayer();
                            mState = STATE_RESUME;
                            // call end
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(mCallReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mCallReceiver);
    }
}
