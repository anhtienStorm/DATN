package bkav.android.btalk.messaging.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Observable;

/**
 * Created by quangnd on 14/05/2017.
 * class lang nghe trang thai net work thay doi
 */

public class BtalkNetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Doan xu ly khi su kien network chang state
        // Thong bao cho tat ca observer su kien da thay doi
        getObservable().connectionChanged();
    }

    public static class NetworkObservable extends Observable {
        private static NetworkObservable instance = null;

        private NetworkObservable() {
            // Exist to defeat instantiation.
        }

        void connectionChanged(){
            setChanged();
            notifyObservers();
        }

        public static NetworkObservable getInstance(){
            if(instance == null){
                instance = new NetworkObservable();
            }
            return instance;
        }
    }

    public static NetworkObservable getObservable() {
        return NetworkObservable.getInstance();
    }
}
