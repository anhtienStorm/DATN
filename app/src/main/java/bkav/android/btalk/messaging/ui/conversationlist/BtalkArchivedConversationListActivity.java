package bkav.android.btalk.messaging.ui.conversationlist;

import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.View;

import com.android.messaging.ui.SnackBarManager;
import com.android.messaging.ui.conversationlist.ArchivedConversationListActivity;
import com.android.messaging.ui.conversationlist.ConversationListFragment;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 28/03/2017.
 * Custom lai activity cua source goc de chinh mau status bar voi action bar
 */

public class BtalkArchivedConversationListActivity extends ArchivedConversationListActivity{

    @Override
    protected void changeColorActionBar(ActionBar actionBar) {
        actionBar.setElevation(0f);
        // Bkav QuangNDB change color cua action thanh mau trang co alpha
        actionBar.setBackgroundDrawable(new ColorDrawable(
                getResources().getColor(R.color.btalk_white_opacity_bg)));
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.btalk_white_opacity_bg));
        findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        //Bkav QuangNDb: khong lam gi vi khong set lai mau action bar nua, de mac dinh
    }

    @Override
    protected ConversationListFragment setUpFragment() {
        return BtalkConversationListFragment.createArchivedConversationListFragment();
    }

    @Override
    protected void initFragment(Fragment fragment) {
        if (fragment instanceof ConversationListFragment) {
            mConversationListFragment = (BtalkConversationListFragment) fragment;
            mConversationListFragment.setHost(this);
        }
    }

    @Override
    public void dismissActionMode() {
        super.dismissActionMode();
        setStatusBar(R.color.btalk_archive_status_bar_color);
        findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @Override
    protected void startMultiSelectActionMode() {
        super.startMultiSelectActionMode();
        setStatusBar(R.color.action_mode_message_color);
        findViewById(android.R.id.content).setSystemUiVisibility(0);//Bkav QuangNDb Tat che do light status bar
    }

    /**
     * Bkav QuangNDb set status bar color
     */
    private void setStatusBar(int colorId) {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, colorId));
    }

    // Bkav HaiKH - Fix bug BOS-3096- Start
    // gọi dismiss() để đóng snackBar khi backPress để tránh trường hợp backPress trước khi snackBar đóng.
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SnackBarManager.get().dismiss();
    }
    // Bkav HaiKH - Fix bug BOS-3096- End
}
