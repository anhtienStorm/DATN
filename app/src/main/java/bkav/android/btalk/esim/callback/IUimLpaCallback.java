package bkav.android.btalk.esim.callback;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.qualcomm.qti.lpa.IUimLpaServiceCallback;
import com.qualcomm.qti.lpa.UimLpaDownloadProgress;
import com.qualcomm.qti.lpa.UimLpaProfile;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import bkav.android.btalk.esim.LPAController;
import bkav.android.btalk.esim.Utils.Utils;


public class IUimLpaCallback extends IUimLpaServiceCallback.Stub {

    private static final String TAG = IUimLpaCallback.class.getName();
    private Context mContext;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public IUimLpaCallback(Context context) {
        super();
        mContext = context;
    }

    @Override
    public void uimLpaAddProfileResponse(int slot, int token, int responseCode) throws RemoteException {
    }

    @Override
    public void uimLpaEnableProfileResponse(int slot, int token, int responseCode) throws RemoteException {
        LPAController.getInstance(mContext).setResponseCodeEnable(responseCode);
    }

    @Override
    public void uimLpaDisableProfileResponse(int slot, int token, int responseCode) throws RemoteException {
        if (responseCode == Utils.UIM_LPA_SUCCESS) {
            LPAController.getInstance(mContext).getIUimLpaService().uimLpaGetProfiles(slot, token);
        }
    }

    @Override
    public void uimLpaDeleteProfileResponse(int slot, int token, int responseCode) throws RemoteException {
        if (responseCode == Utils.UIM_LPA_SUCCESS) {
            LPAController.getInstance(mContext).getIUimLpaService().uimLpaGetProfiles(slot, token);
        }
    }

    @Override
    public void uimLpaUpdateNicknameResponse(int slot, int token, int responseCode) throws RemoteException {
        if (responseCode == Utils.UIM_LPA_SUCCESS) {
            LPAController.getInstance(mContext).getIUimLpaService().uimLpaGetProfiles(slot, token);
        }
    }

    @Override
    public void uimLpaeUICCMemoryResetResponse(int slot, int token, int responseCode) throws RemoteException {
    }

    @Override
    public void uimLpaGetProfilesResponse(int slot, int token, int responseCode, UimLpaProfile[] profiles) throws RemoteException {
        ArrayList<UimLpaProfile> arrayList = new ArrayList<UimLpaProfile>();
        if (responseCode == Utils.UIM_LPA_SUCCESS && profiles != null) {
            for (int i = 0; i < profiles.length; i++) {
                profiles[i].setSlot(slot);
                arrayList.add(profiles[i]);
            }
        }
        if (slot == 0) {
            LPAController.getInstance(mContext).setListProfileSlot_0(arrayList);
        } else if (slot == 1) {
            LPAController.getInstance(mContext).setListProfileSlot_1(arrayList);
        }
    }

    @Override
    public void uimLpaGetEidResponse(int slot, int token, int responseCode, byte[] eid) throws RemoteException {
        if (responseCode == Utils.UIM_LPA_SUCCESS) {
            String eID = null;
            try {
                eID = " EID : " + new String(eid, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            LPAController.getInstance(mContext).showDialog(eID);
        } else {
        }
    }

    @Override
    public void uimLpaGetSrvAddrResponse(int slot, int token, int responseCode, String smdp, String smds) throws RemoteException {
    }

    @Override
    public void uimLpaSetSrvAddrResponse(int slot, int token, int responseCode) throws RemoteException {
    }

    @Override
    public void uimLpaDownloadProgressIndication(int slot, int responseCode, UimLpaDownloadProgress progress) throws RemoteException {
    }

    @Override
    public void uimLpaRadioStateIndication(int slot, int state) throws RemoteException {
    }

}
