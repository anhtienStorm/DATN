package bkav.android.btalk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.CallLog;

import com.android.contacts.common.CallUtil;
import com.android.dialer.util.DialerUtils;

/**
 * Created by anhdt on 05/08/2017.
 *
 */

public class TouchLauncherReceiver extends BroadcastReceiver {

    private static final String ACTION_TOUCH_BTALK = "bkav.android.3d_touch_btalk";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(ACTION_TOUCH_BTALK)) {
            GetLastOutgoingCallArgs lastCallArgs =
                    new GetLastOutgoingCallArgs(
                            context,
                            new OnLastOutgoingCallComplete() {
                                @Override
                                public void lastOutgoingCall(String number) {
                                    // Anhdts goi dien
                                    if (!number.isEmpty()) {
                                        final Intent intent = CallUtil.getCallIntent(number);
                                        DialerUtils.startActivityWithErrorToast(context, intent);
                                    }
                                }
                            });
            new GetLastOutgoingCallTask(lastCallArgs.callback).execute(lastCallArgs);
        }
    }


    /**
     * Anhdts get number
     * AsyncTask to get the last outgoing call from the DB.
     */
    private class GetLastOutgoingCallTask extends AsyncTask<GetLastOutgoingCallArgs, Void, String> {
        // Happens on a background thread. We cannot run the callback
        // here because only the UI thread can modify the view
        // hierarchy (e.g enable/disable the dial button). The
        // callback is ran rom the post execute method.

        OnLastOutgoingCallComplete mCallBack;

        GetLastOutgoingCallTask(OnLastOutgoingCallComplete callBack) {
            mCallBack = callBack;
        }

        @Override
        protected String doInBackground(GetLastOutgoingCallArgs... list) {
            String number = "";
            for (GetLastOutgoingCallArgs args : list) {
                // May block. Select only the last one.
                number = CallLog.Calls.getLastOutgoingCall(args.context);
            }
            return number;  // passed to the onPostExecute method.
        }

        // Happens on the UI thread, it is safe to run the callback
        // that may do some work on the views.
        @Override
        protected void onPostExecute(String number) {
            mCallBack.lastOutgoingCall(number);
        }
    }

    class GetLastOutgoingCallArgs {
        GetLastOutgoingCallArgs(Context context,
                                OnLastOutgoingCallComplete callback) {
            this.context = context;
            this.callback = callback;
        }

        public final Context context;
        public final OnLastOutgoingCallComplete callback;
    }

    interface OnLastOutgoingCallComplete {
        void lastOutgoingCall(String number);
    }
}
