package bkav.android.btalk.esim;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.qualcomm.qti.lpa.IUimLpaService;
import com.qualcomm.qti.lpa.IUimLpaServiceCallback;
import com.qualcomm.qti.lpa.UimLpaProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LPAController {

    private Context mContext;
    private final LpaServiceConnection mLpaServiceConnection = new LpaServiceConnection();
    private IUimLpaServiceCallback mIUimLpaCallback ;
    private IUimLpaService mService = null;
    private static Object mLock = new Object();
    private ArrayList<UimLpaProfile> mListProfileSlot_0;
    private ArrayList<UimLpaProfile> mListProfileSlot_1;

    private int mSlot = 0;
    public static final int TOKEN = 0;


    private final Set<UpdateListProfile> mUpdateListProfile =
            Collections.newSetFromMap(new ConcurrentHashMap<UpdateListProfile, Boolean>(8, 0.9f, 1));

    private static LPAController sLpaController;

    public LPAController(Context context){
        mContext = context;
        mIUimLpaCallback = new bkav.android.btalk.esim.callback.IUimLpaCallback(context);
    }


    public static LPAController getInstance(Context context){
        synchronized (mLock){
            if (sLpaController == null){
                sLpaController = new LPAController(context);
            }
            return sLpaController;
        }
    }

    public IUimLpaService getIUimLpaService(){
        return mService;
    }

    public void setListProfileSlot_0(ArrayList<UimLpaProfile> listProfile){
        mListProfileSlot_0 = listProfile;
        // bao lai cap nhat listProfile de cac activity xu li
        for(UpdateListProfile list : mUpdateListProfile){
            list.updateListProfile();
        }

    }

    public ArrayList<UimLpaProfile> getListProfileSlot_0(){
        return mListProfileSlot_0;
    }

    public ArrayList<UimLpaProfile> getListProfileSlot_1() {
        return mListProfileSlot_1;
    }

    public void setListProfileSlot_1(ArrayList<UimLpaProfile> mListProfileSlot) {
        this.mListProfileSlot_1 = mListProfileSlot;
    }

    public void showDialog(String eID){
        // bao lai cap nhat listProfile de cac activity xu li
        for(UpdateListProfile list : mUpdateListProfile){
            list.showDialog(eID);
        }
    }

    public void checkConnectService(Context context) {
        if (mService == null && !mIsBound) {
            // thuc hien bind vao service
            Intent intent = new Intent();
            intent.setAction(IUimLpaService.class.getName());
            intent.setComponent(new ComponentName("com.qualcomm.qti.lpa", "com.qualcomm.qti.lpa.UimLpaService"));
            context.bindService(intent, mLpaServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unBindConnectService(Context context){
        if(mService != null && mIsBound){
            context.unbindService(mLpaServiceConnection);
            mService = null;
        }
    }



    public interface LPAListener{

        void onServiceConnected();
    }

    private LPAListener mLpaListener;
    private boolean mIsBound;

    public void setLpaListener(LPAListener lpaListener) {
        this.mLpaListener = lpaListener;
    }


    private class LpaServiceConnection implements ServiceConnection {

        public LpaServiceConnection(){

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IUimLpaService.Stub.asInterface(service);
            mIsBound = true;
            if(mService != null){
                //o day se dang ki lang nghe su kien call back
                try {
                    mService.registerCallback(mIUimLpaCallback);
                    if (mLpaListener != null) {
                        mLpaListener.onServiceConnected();
                    }
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBound = false;
            mService = null;
        }
    }

    public interface UpdateListProfile{
        void updateListProfile();
        void showDialog(String eid);
    }

    public void addUpdateListProfile(UpdateListProfile updateListProfile) {
        if(updateListProfile != null){
            mUpdateListProfile.add(updateListProfile);
        }
    }

    public void removeUpdateListProfile(UpdateListProfile updateListProfile) {
        if(mUpdateListProfile.contains(updateListProfile)){
            mUpdateListProfile.remove(updateListProfile);
        }
    }

    public int getSlot() {
        return mSlot;
    }

    public void setSlot(int mSlot) {
        this.mSlot = mSlot;
    }

    public int getResponseCodeEnable() {
        return mResponseCodeEnable;
    }

    public void setResponseCodeEnable(int mResponseCodeEnable) {
        this.mResponseCodeEnable = mResponseCodeEnable;
    }

    private int mResponseCodeEnable;


}
