package bkav.android.btalk.messaging.ui.appsettings;

import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;

import com.android.messaging.ui.appsettings.SettingsActivity;
import com.android.messaging.util.OsUtil;

import bkav.android.btalk.R;
import bkav.android.btalk.utility.BtalkUiUtils;

/**
 * Created by quangnd on 28/03/2017.
 * Custom lai SettingsActivity de chinh lai mau cua status bar va cac logic
 */

public class BtalkSettingsActivity extends SettingsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.settings_activity_title));
        getSupportActionBar().setElevation(0f);

        if (OsUtil.isAtLeastM()) {
            // Bkav QuangNDB change color cua action thanh mau trang co alpha
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(
                    getResources().getColor(R.color.actionbar_setting_color)));
        }else {
            // Bkav QuangNDB change color cua action thanh mau trang co alpha
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(
                    getResources().getColor(R.color.actionbar_setting_color_lollipop)));
            BtalkUiUtils.setStatusbarColor(getWindow());
        }
    }

    @Override
    protected void clickHomeButton() {
        onBackPressed();
    }

    public static class BtalkSettingsFragment extends SettingsFragment{

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            View rootView = getView();
            ListView list = (ListView) rootView.findViewById(android.R.id.list);
            list.setDivider(null);
        }
    }

    @Override
    protected Fragment getSettingFragment() {
        return new BtalkSettingsFragment();
    }
}
