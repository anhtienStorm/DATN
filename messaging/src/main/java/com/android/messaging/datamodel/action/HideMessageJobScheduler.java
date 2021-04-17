package com.android.messaging.datamodel.action;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.BugleNotifications;

import static com.android.messaging.datamodel.BugleNotifications.SENDER;

public class HideMessageJobScheduler extends JobService {
    private static final String NOTIFICATION_TAG = "notificationTag";
    private static final String TYPE = "type";
    private static final String HIDE_MESSAGE_CHANNEL_ID = "Hide_Message_Channel";

    public HideMessageJobScheduler() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        String notificationTag = params.getExtras().getString(NOTIFICATION_TAG);
        int type = params.getExtras().getInt(TYPE);
        String sender = params.getExtras().getString(SENDER);
        // FIXME: 19/03/2020 bit tam loi sNotifBuilder null, chua giai thich duoc
        if (BugleNotifications.sNotifBuilder == null) {
            return false;
        }
        NotificationCompat.Builder builder = BugleNotifications.sNotifBuilder;
        builder.setContentTitle(sender); // Bkav HuyNQN fix loi tin nhan hien sai ten nguoi gui
        builder.setContentText(getString(R.string.hide_message_text));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.hide_message_text)));

        NotificationManager notificationManager =
                (NotificationManager) (Factory.get().getApplicationContext().getSystemService(NOTIFICATION_SERVICE));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            BugleNotifications.createChannelNotification(notificationManager, HIDE_MESSAGE_CHANNEL_ID,
                    getApplicationContext().getResources().getString(R.string.message_notification_channel_name), NotificationManager.IMPORTANCE_LOW, false);
            builder.setChannelId(HIDE_MESSAGE_CHANNEL_ID);
        }

        Notification notification = builder.build();
        notificationManager.notify(notificationTag,type,notification);

        jobFinished(params,false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
