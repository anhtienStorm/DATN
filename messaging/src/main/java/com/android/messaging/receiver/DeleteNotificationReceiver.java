package com.android.messaging.receiver;

import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeleteNotificationReceiver extends BroadcastReceiver {

    private static final String JOB_ID = "Job_Id";

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(JOB_ID,-1);
        if (id != -1){
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.cancel(id);
        }
    }
}
