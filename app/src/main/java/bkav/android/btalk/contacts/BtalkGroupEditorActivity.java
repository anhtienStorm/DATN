package bkav.android.btalk.contacts;


import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.android.contacts.activities.GroupEditorActivity;
import com.android.contacts.group.GroupEditorFragment;

import bkav.android.btalk.R;

/**
 * AnhNDd: class edit thong tin cua groups
 */
public class BtalkGroupEditorActivity extends GroupEditorActivity {

    @Override
    public void hookToSetContentView() {
        setContentView(R.layout.btalk_group_editor_activity);
    }

    @Override
    public void getGroupEditorFragment() {
        mFragment = (BtalkGroupEditorFragment) getFragmentManager().findFragmentById(
                R.id.group_editor_fragment);
    }

    @Override
    public void customViewActionbar(ActionBar actionBar) {
        // Inflate a custom action bar that contains the "done" button for saving changes
        // to the group
        LayoutInflater inflater = (LayoutInflater) getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View customActionBarView = inflater.inflate(R.layout.btalk_editor_custom_action_bar,
                null);
        View saveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
        saveMenuItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragment.onDoneClicked();
            }
        });
        // Show the custom action bar but hide the home icon and title
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |
                        ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView);
    }

    @Override
    public void setFragmentListener() {
        mFragment.setListener(mFragmentListener);
    }

    private final GroupEditorFragment.Listener mFragmentListener =
            new GroupEditorFragment.Listener() {
                @Override
                public void onGroupNotFound() {
                    finish();
                }

                @Override
                public void onReverted() {
                    finish();
                }

                @Override
                public void onAccountsNotFound() {
                    finish();
                }

                @Override
                public void onSaveFinished(int resultCode, Intent resultIntent) {
                    if (resultIntent != null) {
                        Intent intent = new Intent(BtalkGroupEditorActivity.this, BtalkGroupDetailActivity.class);
                        intent.setData(resultIntent.getData());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
            };
}
