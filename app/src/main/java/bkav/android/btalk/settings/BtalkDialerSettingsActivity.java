package bkav.android.btalk.settings;

import android.content.Intent;
import android.os.Build;

import bkav.android.btalk.bmsblocked.BlockCallsOnlyBmsActivity;
import com.android.dialer.settings.DialerSettingsActivity;
import com.android.messaging.util.PhoneUtils;

import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.bmsblocked.BmsUtils;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.speeddial.BtalkSpeedDialListActivity;

/**
 * Created by anhdt on 21/07/2017.
 *
 */

public class BtalkDialerSettingsActivity extends DialerSettingsActivity {

    private int mIsMutilSim;

    @Override
    protected void setElevationActionbar() {
        getSupportActionBar().setElevation(0f);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Bkav QuangNDB change color cua action thanh mau trang co alpha
//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(
//                getResources().getColor(R.color.actionbar_setting_color)));
    }

    @Override
    protected void addOptionOpenApp(List<Header> target) {
        Header header = new Header();
        header.titleRes = R.string.header_title_setting_open_app;
        header.fragment = BtalkSettingOpenAppFragment.class.getName();
        target.add(header);
    }

    // Anhdts bo cai dat hien thi cu, them cac tuy chon moi
    @Override
    protected boolean showDisplayOptions(List<Header> target) {
        Header displayOptionsHeader = new Header();
        mIsMutilSim = PhoneUtils.getDefault().getActiveSubscriptionCount();
        //HienDTk: check neu co 2 sim va khong co sim nao bi tat thi cho hien thi tuy chon
        if(mIsMutilSim >1)
        if(!ESimUtils.isSlotNotReady(0) && !ESimUtils.isSlotNotReady(1)){
            displayOptionsHeader.titleRes = R.string.display_options_title;
            displayOptionsHeader.fragment = BtalkSettingDisplayFragment.class.getName();
            target.add(displayOptionsHeader);
        }

        return false;
    }

    /**
     * Anhdts custom lai fragment sound
     */
    @Override
    protected String getSoundFragment() {
        return BtalkSoundSettingsFragment.class.getName();
    }

    // Bkav TienNAb: ham xu ly khi chon chuc nang cai dat quay so nhanh
    @Override
    protected void addOptionSpeedDial(List<Header> target) {
        Header header = new Header();
        header.titleRes = R.string.header_title_setting_speed_dial;
        header.intent = new Intent(getApplicationContext(), BtalkSpeedDialListActivity.class);
        target.add(header);
    }

    @Override
    protected void addCallBlockedBMS(List<Header> target) {
        // Bkav HuyNQN them logic chan sdt vao bms
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            Header header = new Header();
            header.titleRes = R.string.block_phone_number_only_bms_title;
            header.intent = BmsUtils.intentBlackListBms(getApplicationContext());
            target.add(header);
        }
    }
}
