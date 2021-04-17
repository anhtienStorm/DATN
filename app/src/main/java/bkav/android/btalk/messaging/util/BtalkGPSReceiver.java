package bkav.android.btalk.messaging.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Observable;

/**
 * Created by quangnd on 14/05/2017.
 * class lang nghe trang thai turn on, off  location
 */

public class BtalkGPSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Doan xu ly khi su kien network chang state
        // Thong bao cho tat ca observer su kien da thay doi
        getObservable().connectionChanged();
    }
    public static class GPSObservable extends Observable {
        private static GPSObservable instance = null;

        private GPSObservable() {
            // Exist to defeat instantiation.
        }

        public void connectionChanged(){
            setChanged();
            notifyObservers();
        }

        public static GPSObservable getInstance(){
            if(instance == null){
                instance = new GPSObservable();
            }
            return instance;
        }
    }

    public static GPSObservable getObservable() {
        return GPSObservable.getInstance();
    }
}
