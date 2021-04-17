package bkav.android.btalk.messaging.ui.appsettings;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.ui.appsettings.ApplicationSettingsActivity;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.OsUtil;

import bkav.android.btalk.R;
import bkav.android.btalk.utility.BtalkUiUtils;

/**
 * Created by quangnd on 19/04/2017.
 * class hien thi giao dien setting custom lai cua source goc
 */

public class BtalkApplicationSettingsActivity extends ApplicationSettingsActivity {

    private int mSubId = 0;

    private boolean mTopLevel = false;

    @Override
    protected void initSubId() {
        mSubId = ParticipantData.DEFAULT_SELF_SUB_ID;
        mTopLevel = true;
    }

    @Override
    protected void setElevationActionbar() {
        getSupportActionBar().setElevation(0f);
        // Bkav QuangNDB change color cua action thanh mau trang co alpha
//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(
//                getResources().getColor(R.color.actionbar_setting_color)));
        if (!OsUtil.isAtLeastM()) {
            // Bkav QuangNDB change color cua action thanh mau trang co alpha
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(
                    getResources().getColor(R.color.actionbar_setting_color_lollipop)));
            BtalkUiUtils.setStatusbarColor(getWindow());
        }
    }

    @Override
    protected Fragment getFragment() {
        return BtalkApplicationSettingsFragment.newInstance(mSubId, mTopLevel);
    }

    public static class BtalkApplicationSettingsFragment extends ApplicationSettingsFragment {

        private static final String TOP_LEVEL_KEY = "top_level";

        private static final String SUB_ID = "sub_id";

        private SwitchPreference mDeliveryReportsPreference, mShowAvatar;

        private int mSubId = 0;

        private boolean mTopLevel = false;

        protected View mContentView;

        public static BtalkApplicationSettingsFragment newInstance(int subId, boolean topLevel) {
            Bundle args = new Bundle();
            args.putBoolean(TOP_LEVEL_KEY, topLevel);
            args.putInt(SUB_ID, subId);
            BtalkApplicationSettingsFragment fragment = new BtalkApplicationSettingsFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mContentView = super.onCreateView(inflater, container, savedInstanceState);
            return mContentView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            View rootView = getView();
            ListView list = (ListView) rootView.findViewById(android.R.id.list);
            list.setDivider(null);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mDeliveryReportsPreference = (SwitchPreference) findPreference(getString(R.string.delivery_reports_pref_key));
            mShowAvatar = (SwitchPreference) findPreference(getString(R.string.option_show_avatar_key));
            mShowAvatar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean switched = (Boolean) newValue;
                    saveBooleanInDefaultPre(getString(R.string.option_show_avatar_key),switched);
                    return true;
                }
            });
            mSubId = getArguments().getInt(SUB_ID, 0);
            mTopLevel = getArguments().getBoolean(TOP_LEVEL_KEY, false);
            if (!mTopLevel || mSubId == 0) {
                getPreferenceScreen().removePreference(mDeliveryReportsPreference);
            } else {
                mDeliveryReportsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean switched = (Boolean) newValue;
                        final BuglePrefs subPrefs = Factory.get().getSubscriptionPrefs(mSubId);
                        SharedPreferences pre = getActivity().getSharedPreferences(subPrefs.getSharedPreferencesName(), MODE_PRIVATE);
                        SharedPreferences.Editor edit = pre.edit();
                        edit.putBoolean(getString(R.string.delivery_reports_pref_key), switched);
                        edit.apply();
                        return true;
                    }
                });
            }
        }

        @Override
        protected void setEnableDeliveryReport(boolean isSmsEnabledCurrentState) {
            mDeliveryReportsPreference.setEnabled(isSmsEnabledCurrentState);
        }

        @Override
        public void onResume() {
            super.onResume();
            initDeliveryReport();
            initShowAvatar();
        }

        private void initShowAvatar() {
            SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean isShowAvatar = pre.getBoolean(getString(R.string.option_show_avatar_key), false);
            mShowAvatar.setChecked(isShowAvatar);

        }

        private void saveBooleanInDefaultPre(String key, boolean value) {
            SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor edit = pre.edit();
            edit.putBoolean(key, value);
            edit.apply();
        }

        /**
         * BKav QuangNDb khoi tao gia tri cua Delivery Report pref
         */
        private void initDeliveryReport() {
            final BuglePrefs subPrefs = Factory.get().getSubscriptionPrefs(mSubId);
            SharedPreferences pre = getActivity().getSharedPreferences(subPrefs.getSharedPreferencesName(), MODE_PRIVATE);
            boolean isReport = pre.getBoolean(getString(R.string.delivery_reports_pref_key), true);
            mDeliveryReportsPreference.setChecked(isReport);
        }

        @Override
        protected int getPreferenceId() {
            return R.xml.btalk_preferences_application;
        }
    }

    @Override
    protected void homeButtonPress() {
        onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
