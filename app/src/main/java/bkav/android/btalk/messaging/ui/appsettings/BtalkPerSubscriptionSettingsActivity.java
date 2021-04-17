package bkav.android.btalk.messaging.ui.appsettings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.messaging.ui.appsettings.PerSubscriptionSettingsActivity;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 19/04/2017.
 * class hien thi giao dien advance setting hoac sim setting custom lai source goc
 */

public class BtalkPerSubscriptionSettingsActivity extends PerSubscriptionSettingsActivity {

    @Override
    protected PerSubscriptionSettingsFragment getFragment(String title) {
        return BtalkPerSubscriptionSettingsFragment.newInstance(title);
    }

    @Override
    protected void setElevationActionbar() {
        getSupportActionBar().setElevation(0f);
        // Bkav QuangNDB change color cua action thanh mau trang co alpha
//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(
//                getResources().getColor(R.color.btalk_white_opacity_bg)));
    }

    @Override
    protected void homeButtonClick() {
        onBackPressed();
    }

    public static class BtalkPerSubscriptionSettingsFragment extends PerSubscriptionSettingsFragment {

        private static final String TITLE = "title";

        // Bkav HaiKH - Fix bug BOS-3248- Start
        // Một số trường trong cài đặt tin nhắn
        private PreferenceCategory advancedCategory; // Nâng cao
        private Preference deliveryReportsPreference; // Yêu cầu báo cáo gửi cho mỗi SMS bạn gửi
        private PreferenceScreen rootCategory; // Preference root
        private Preference wirelessAlertPref; // Cảnh báo không dây
        private Preference apnsPref; // Tên điểm truy cập
        // Bkav HaiKH - Fix bug BOS-3248- End

        public static BtalkPerSubscriptionSettingsFragment newInstance(String title) {
            Bundle args = new Bundle();
            args.putString(TITLE, title);
            BtalkPerSubscriptionSettingsFragment fragment = new BtalkPerSubscriptionSettingsFragment();
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
            String title = getArguments().getString(TITLE);
            if (title == null || TextUtils.isEmpty(title)) {
                advancedCategory = (PreferenceCategory) findPreference(getString(R.string.advanced_category_pref_key));
                deliveryReportsPreference = findPreference(getString(R.string.delivery_reports_pref_key));

                // Bkav HaiKH - Fix bug BOS-3248- Start
                // Remove advancedCategory khi không có trường nào trong Advance
                rootCategory = (PreferenceScreen)
                        findPreference(getString(R.string.root_preference_screen_pref_key));
                wirelessAlertPref = findPreference(getString(
                        R.string.wireless_alerts_key));
                apnsPref = findPreference(getString(R.string.sms_apns_key));
                advancedCategory.removePreference(deliveryReportsPreference);
                if (wirelessAlertPref == null && apnsPref == null) {
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rootCategory.removePreference(advancedCategory);
                        }
                    }, 100);
                }
                // Bkav HaiKH - Fix bug BOS-3248- End

            }
        }

        @Override
        protected int getIdResMMSGroupMessageDefault() {
            return R.bool.btalk_group_mms_pref_default;
        }

        @Override
        protected void showDialogOptionMMSGroup(int mSubId) {
            BtalkGroupMmsSettingDialog.showDialog(getActivity(), mSubId);
        }
    }
}
