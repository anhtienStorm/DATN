package bkav.android.btalk.activities;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.util.Assert;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.OsUtil;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.QuickResponseListAdapter;
import bkav.android.btalk.messaging.datamodel.data.QuickResponseData;
import bkav.android.btalk.messaging.ui.conversation.BtalkConversationActivity;
import bkav.android.btalk.utility.BtalkUiUtils;
import bkav.android.btalk.text_shortcut.DeepShortcutsContainer;

/**
 * Created by quangnd on 15/12/2017.
 * Activity hien thi giao dien edit quick reponse
 */

public class BtalkQuickResponseEditActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, QuickResponseListAdapter.OnQuickResponseListener {

    private RecyclerView mRecyclerView;
    private QuickResponseListAdapter mAdapter;
    private static final int ID_LOAD_RESPONSE = 3;
    private View mParentView;
    // Bkav HienDTk: check luu tra loi nhanh neu list rong
    private boolean mIsCheckResponeContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.btalk_dialog_quick_response);
        bindView();
        if (OsUtil.isAtLeastL_MR1()) {
            findViewById(R.id.parent_view).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.actionbar_setting_color)));
        }else {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.actionbar_setting_color_lollipop)));
            BtalkUiUtils.setStatusbarColor(getWindow());
        }
        getLoaderManager().initLoader(ID_LOAD_RESPONSE, null, this);
        getSupportActionBar().setElevation(0f);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void bindView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.list_response);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new QuickResponseListAdapter(this, null, this);
        mRecyclerView.setAdapter(mAdapter);
        mParentView = findViewById(R.id.parent_view);
        final QuickResponseData quickResponseData = (QuickResponseData) getIntent().getSerializableExtra(QuickResponseData.KEY_TRANSFER);
        if (quickResponseData != null) {
            showDialogEditResponse(quickResponseData);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.btalk_quick_response_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                if (!isHasMaxQuickResponse()) {
                    showDialogAddQuickResponse();
                }else {
                    Toast.makeText(this, R.string.notify_max_support_quick_response, Toast.LENGTH_SHORT).show();
                }
                break;
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, MessagingContentProvider.QUICK_RESPONSE_URI, null, null, null, "_id desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Anhdts neu khong co thi hien luon dialog len
        if (data != null && data.getCount() == 0) {
            if(!mIsCheckResponeContent)
            showDialogAddQuickResponse();
        }
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null) {
            mAdapter.swapCursor(null);
        }
    }

    @Override
    public void onResponseClick(QuickResponseData data) {
        showDialogEditResponse(data);
    }

    /**
     * Bkav QuangNDb show dialog sua response
     */
    private void showDialogEditResponse(final QuickResponseData data) {
        final AlertDialog.Builder newResponseBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.btalk_dialog_add_or_edit_response, null);
        final EditText mContent = (EditText) view.findViewById(R.id.response);
        mContent.setText(data.getResponse());
        mContent.setSelection(0, mContent.length());
        showImeKeyboard(mContent);
        newResponseBuilder.setView(view).setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(mContent.getText())) {
                    mIsCheckResponeContent = true;
                    //HienDTk: neu nguoi dung xoa het text ma bam luu thi xoa luon noi dung tra loi nhanh
//                    Toast.makeText(BtalkQuickResponseEditActivity.this, R.string.notify_empty_response_content, Toast.LENGTH_SHORT).show();
                    data.deleteToDb(BtalkQuickResponseEditActivity.this);
                } else {
                    data.setResponse(mContent.getText().toString());
                    data.updateToDb(BtalkQuickResponseEditActivity.this);
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        newResponseBuilder.create().show();
    }

    /**Bkav QuangNDb show dialog add response*/
    private void showDialogAddQuickResponse() {
        AlertDialog.Builder newResponseBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.btalk_dialog_add_or_edit_response, null);
        final EditText mContent = (EditText) view.findViewById(R.id.response);
        showImeKeyboard(mContent);
        newResponseBuilder.setView(view).setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(mContent.getText())) {
                    Toast.makeText(BtalkQuickResponseEditActivity.this, R.string.notify_empty_response_content, Toast.LENGTH_SHORT).show();
                }else {
                    final QuickResponseData data = new QuickResponseData(mContent.getText().toString(), false);
                    data.insertToDb(BtalkQuickResponseEditActivity.this);
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        newResponseBuilder.create().show();
    }

    /**
     * Bkav QuangNDb show keyboard with input view
     */
    private void showImeKeyboard(final EditText view) {
        Assert.notNull(view);
        view.requestFocus();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ImeUtil.get().showImeKeyboard(BtalkQuickResponseEditActivity.this, view);
            }
        }, 100);
    }
    
    /**Bkav QuangNDb ham chech xem da max quick reponse chua*/
    private boolean isHasMaxQuickResponse() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(MessagingContentProvider.QUICK_RESPONSE_URI, null, null, null, null);
            if (cursor != null && cursor.getCount() == DeepShortcutsContainer.MAX_SHORTCUTS) {
                return true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

}
