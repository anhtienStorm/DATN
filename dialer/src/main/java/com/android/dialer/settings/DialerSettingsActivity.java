/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.dialer.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.os.BuildCompat;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.contacts.common.compat.CompatUtils;
import com.android.contacts.common.compat.TelephonyManagerCompat;
import com.android.dialer.R;

import com.android.dialer.compat.FilteredNumberCompat;
import com.android.dialer.compat.SettingsCompat;
import com.android.dialer.compat.UserManagerCompat;

import java.util.List;

public class DialerSettingsActivity extends AppCompatPreferenceActivity {
    protected SharedPreferences mPreferences;
    private boolean migrationStatusOnBuildHeaders;

    public int mIsMutilSim;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setElevationActionbar();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * The headers need to be recreated if the migration status changed between when the headers
         * were created and now.
         */
        if (migrationStatusOnBuildHeaders != FilteredNumberCompat.hasMigratedToNewBlocking()) {
            invalidateHeaders();
        }
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        if (showDisplayOptions(target)) {
            Header displayOptionsHeader = new Header();
            displayOptionsHeader.titleRes = R.string.display_options_title;
            displayOptionsHeader.fragment = DisplayOptionsSettingsFragment.class.getName();
            target.add(displayOptionsHeader);
        }

        Header soundSettingsHeader = new Header();
        soundSettingsHeader.titleRes = R.string.sounds_and_vibration_title;
        // Anhdts custom lai
        soundSettingsHeader.fragment = getSoundFragment();
        soundSettingsHeader.id = R.id.settings_header_sounds_and_vibration;
        target.add(soundSettingsHeader);

        if (CompatUtils.isMarshmallowCompatible()) {
            Header quickResponseSettingsHeader = new Header();
            Intent quickResponseSettingsIntent =
                    new Intent(TelecomManager.ACTION_SHOW_RESPOND_VIA_SMS_SETTINGS);
            quickResponseSettingsHeader.titleRes = R.string.respond_via_sms_setting_title;
            quickResponseSettingsHeader.intent = quickResponseSettingsIntent;
            target.add(quickResponseSettingsHeader);
        }

//        Header speedDialSettingsHeader = new Header();
//        Intent speedDialSettingsIntent = new Intent(this, SpeedDialListActivity.class);
//        speedDialSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//        speedDialSettingsHeader.titleRes = R.string.speed_dial_settings;
//        speedDialSettingsHeader.intent = speedDialSettingsIntent;
//        target.add(speedDialSettingsHeader);

        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        // "Call Settings" (full settings) is shown if the current user is primary user and there
        // is only one SIM. Before N, "Calling accounts" setting is shown if the current user is
        // primary user and there are multiple SIMs. In N+, "Calling accounts" is shown whenever
        // "Call Settings" is not shown.
        boolean isPrimaryUser = isPrimaryUser();
        if (isPrimaryUser
                && TelephonyManagerCompat.getPhoneCount(telephonyManager) <= 1) {
            Header callSettingsHeader = new Header();
            Intent callSettingsIntent = new Intent(TelecomManager.ACTION_SHOW_CALL_SETTINGS);
            callSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            callSettingsHeader.titleRes = R.string.call_settings_label;
            callSettingsHeader.intent = callSettingsIntent;
            target.add(callSettingsHeader);
        } else if (BuildCompat.isAtLeastN() || isPrimaryUser) {
            Header phoneAccountSettingsHeader = new Header();
            Intent phoneAccountSettingsIntent =
                    new Intent(TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS);
            phoneAccountSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            phoneAccountSettingsHeader.titleRes = R.string.phone_account_settings_label;
            phoneAccountSettingsHeader.intent = phoneAccountSettingsIntent;
            target.add(phoneAccountSettingsHeader);
        }

        // Bkav HuyNQN khong dung logic goc nay nua ma thay bang logic cua bms
        /*// Anhdts khong hien tren may 5
        if (FilteredNumberCompat.canCurrentUserOpenBlockSettings(this)
                && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            Header blockedCallsHeader = new Header();
            blockedCallsHeader.titleRes = R.string.manage_blocked_numbers_label;
            blockedCallsHeader.intent = FilteredNumberCompat.createManageBlockedNumbersIntent(this);
            target.add(blockedCallsHeader);
            migrationStatusOnBuildHeaders = FilteredNumberCompat.hasMigratedToNewBlocking();
        }*/

        if (isPrimaryUser
                && (TelephonyManagerCompat.isTtyModeSupported(telephonyManager)
                || TelephonyManagerCompat.isHearingAidCompatibilitySupported(telephonyManager))) {
            Header accessibilitySettingsHeader = new Header();
            Intent accessibilitySettingsIntent =
                    new Intent(TelecomManager.ACTION_SHOW_CALL_ACCESSIBILITY_SETTINGS);
            accessibilitySettingsHeader.titleRes = R.string.accessibility_settings_title;
            accessibilitySettingsHeader.intent = accessibilitySettingsIntent;
            target.add(accessibilitySettingsHeader);
        }
        //video calling
        boolean enablePresence = this.getResources().getBoolean(
                R.bool.config_regional_presence_enable);
        if(enablePresence){
            Header videocallingHeader = new Header();
            videocallingHeader.titleRes = R.string.video_call;
            videocallingHeader.fragment = VideoCallingSettingsFragment.class.getName();
            target.add(videocallingHeader);
        }

        boolean usageEnable = getResources().getBoolean(
                R.bool.config_regional_call_data_usage_enable);
        if (usageEnable) {
            final Header historyInfoHeader = new Header();
            historyInfoHeader.titleRes = R.string.call_data_info_label;
            historyInfoHeader.summaryRes = R.string.call_data_info_description;
            historyInfoHeader.intent = new Intent(Intent.ACTION_MAIN);
            historyInfoHeader.intent
                    .setAction("android.intent.action.SHOW_TIMERINFO");
            target.add(historyInfoHeader);
        }

        addOptionOpenApp(target);

        // Bkav TienNAb: them tuy chon cai dat quay so nhanh
        addOptionSpeedDial(target);

        // Bkav HuyNQN them logic chan sdt vao bms
        addCallBlockedBMS(target);
    }



    /**
    * Returns {@code true} or {@code false} based on whether the display options setting should be
    * shown. For languages such as Chinese, Japanese, or Korean, display options aren't useful
    * since contacts are sorted and displayed family name first by default.f
    *
    * @return {@code true} if the display options should be shown, {@code false} otherwise.
     * @param target
    */
    protected boolean showDisplayOptions(List<Header> target) {
        return getResources().getBoolean(R.bool.config_display_order_user_changeable)
                && getResources().getBoolean(R.bool.config_sort_order_user_changeable);
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        if (header.id == R.id.settings_header_sounds_and_vibration) {
            // If we don't have the permission to write to system settings, go to system sound
            // settings instead. Otherwise, perform the super implementation (which launches our
            // own preference fragment.
            if (!SettingsCompat.System.canWrite(this)) {
                Toast.makeText(
                        this,
                        getResources().getString(R.string.toast_cannot_write_system_settings),
                        Toast.LENGTH_SHORT).show();
                // Anhdts mo quyen viet vao setting
                openAndroidPermissionsMenu();
                // startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS));
                return;
            }
        }

        super.onHeaderClick(header, position);
    }

    private void openAndroidPermissionsMenu() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!isSafeToCommitTransactions()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    /**
     * @return Whether the current user is the primary user.
     */
    private boolean isPrimaryUser() {
        return UserManagerCompat.isSystemUser((UserManager) getSystemService(Context.USER_SERVICE));
    }

    protected void setElevationActionbar() {
    }

    protected void addOptionOpenApp(List<Header> target) {

    }

    protected void addOptionSpeedDial(List<Header> target) {

    }

    /**
     * Anhdts custom lai fragment sound
     */
    protected String getSoundFragment() {
        return SoundSettingsFragment.class.getName();
    }

    protected void addCallBlockedBMS(List<Header> target) {

    }
}
