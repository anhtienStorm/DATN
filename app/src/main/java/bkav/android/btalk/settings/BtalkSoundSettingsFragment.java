package bkav.android.btalk.settings;

import android.media.RingtoneManager;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.RingtonePreference;
import android.util.Log;

import com.android.dialer.settings.DialerSettingsActivity;
import com.android.dialer.settings.SoundSettingsFragment;
import com.android.phone.common.util.SettingsUtil;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;

public class BtalkSoundSettingsFragment extends SoundSettingsFragment  {

    private Preference mRingtonePreferenceSim1;

    private Preference mRingtonePreferenceSim2;

    protected static final int MSG_UPDATE_RINGTONE_SUMMARY_SIM1 = 2;

    protected static final int MSG_UPDATE_RINGTONE_SUMMARY_SIM2 = 3;


    @Override
    public void addPreferencesFromResource(int preferencesResId) {
        if (preferencesResId == R.xml.sound_settings) {
            super.addPreferencesFromResource(R.xml.btalk_sound_settings);
        }
    }

    /**
     * Anhdts tim 2 view custom ringtone
     */
    @Override
    protected void findPref() {
        mRingtonePreferenceSim1 = findPreference(getString(R.string.ringtone_preference_key_sim1));
        mRingtonePreferenceSim2 = findPreference(getString(R.string.ringtone_preference_key_sim2));
        //Bkav QuangNDb khi co nhieu sim thi chi hien thi chon nhac chuong cho sim 1 va sim 2
        if (BtalkCallLogCache.getCallLogCache(getContext()).isHasSimOnAllSlot()) {
            getPreferenceScreen().removePreference(mRingtonePreference);
        }else {
            getPreferenceScreen().removePreference(mRingtonePreferenceSim1);
            getPreferenceScreen().removePreference(mRingtonePreferenceSim2);
        }

    }

    protected final Handler mRingtoneLookupSimComplete = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_RINGTONE_SUMMARY_SIM1:
                    mRingtonePreferenceSim1.setSummary((CharSequence) msg.obj);
                    break;
                case MSG_UPDATE_RINGTONE_SUMMARY_SIM2:
                    mRingtonePreferenceSim2.setSummary((CharSequence) msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void updateRingtonePreferenceSummary() {
        //HienDTk: fix bug context = null
        if(getActivity() != null){
            SettingsUtil.updateRingtoneName(
                    getActivity(),
                    mRingtoneLookupComplete,
                    RingtoneManager.TYPE_RINGTONE,
                    mRingtonePreference.getKey(),
                    MSG_UPDATE_RINGTONE_SUMMARY);
            SettingsUtil.updateRingtoneName(
                    getActivity(),
                    mRingtoneLookupSimComplete,
                    BtalkDefaultRingtonePreference.TYPE_SIM1,
                    mRingtonePreferenceSim1.getKey(),
                    MSG_UPDATE_RINGTONE_SUMMARY_SIM1);
            SettingsUtil.updateRingtoneName(
                    getActivity(),
                    mRingtoneLookupSimComplete,
                    BtalkDefaultRingtonePreference.TYPE_SIM2,
                    mRingtonePreferenceSim2.getKey(),
                    MSG_UPDATE_RINGTONE_SUMMARY_SIM2);
        }

        }
        }


