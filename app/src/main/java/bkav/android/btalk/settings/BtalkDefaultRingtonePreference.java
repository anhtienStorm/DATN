package bkav.android.btalk.settings;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.android.dialer.compat.SettingsCompat;
import com.android.dialer.settings.DialerSettingsActivity;

import bkav.android.btalk.R;

/**
 * Anhdts cai dat nhac chuong cho tung sim
 */
public class BtalkDefaultRingtonePreference extends RingtonePreference {

    static final int TYPE_SIM1 = 8;
    static final int TYPE_SIM2 = 16;


    public BtalkDefaultRingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRingtoneType(getTypeSim());
    }

    @Override
    protected void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        super.onPrepareRingtonePickerIntent(ringtonePickerIntent);

        /*
         * Since this preference is for choosing the default ringtone, it
         * doesn't make sense to show a 'Default' item.
         */
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
    }

    @Override
    protected void onSaveRingtone(Uri ringtoneUri) {
        if (!SettingsCompat.System.canWrite(getContext())) {
            Toast.makeText(
                    getContext(),
                    getContext().getResources().getString(R.string.toast_cannot_write_system_settings),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        RingtoneManager.setActualDefaultRingtoneUri(getContext(), getRingtoneType(), ringtoneUri);
    }

    @Override
    protected Uri onRestoreRingtone() {
        return RingtoneManager.getActualDefaultRingtoneUri(getContext(), getRingtoneType());
    }

    private int getTypeSim() {
        if (getKey().equals(getContext().getString(R.string.ringtone_preference_key_sim1))) {
            return TYPE_SIM1;
        }
        return TYPE_SIM2;
    }

    @Override
    protected void onClick() {
        super.onClick();
    }
}
