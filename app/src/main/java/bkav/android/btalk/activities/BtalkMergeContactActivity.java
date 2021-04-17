package bkav.android.btalk.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.contacts.activities.MergeContactActivity;
import com.android.contacts.activities.MergeContactAdapter;
import com.android.contacts.util.DuplicatesUtils;

import java.util.ArrayList;

import bkav.android.btalk.R;

/**
 * Created by anhdt on 03/10/2017.
 * Lop activity merge contact
 */

public class BtalkMergeContactActivity extends MergeContactActivity {

    int mCurrentIndex = 0;

    private static final int SPINNER_NAME_DUPLICATE = 0;

    private static final int SPINNER_PHONE_DUPLICATE = 1;

    private static final int SPINNER_MAIL_DUPLICATE = 2;

    private ArrayList<Integer> mHeaderIndexList;

    private Spinner mSpinner;

    FrameLayout mHeaderView;

    protected int getLayoutId() {
        return R.layout.btalk_merge_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void inflaterMenuItem(MenuInflater inflater, Menu menu) {
        inflater.inflate(R.menu.btalk_merge_options, menu);
    }

    /**
     * Anhdts tao header, neu co 2 muc tro len thi dung spinner, con neu khong thi dung textView
     */
    public void setupViewHeader() {
        mHeaderIndexList = new ArrayList<>();
        ArrayList<String> headerTitleList = new ArrayList<>();
        if (DuplicatesUtils.getMergeRawContacts() != null && !DuplicatesUtils.getMergeRawContacts().isEmpty()) {
            mHeaderIndexList.add(SPINNER_NAME_DUPLICATE);
            headerTitleList.add(getString(R.string.title_merge_by_name));
        }
        if (DuplicatesUtils.getMergePhoneContacts() != null && !DuplicatesUtils.getMergePhoneContacts().isEmpty()) {
            mHeaderIndexList.add(SPINNER_PHONE_DUPLICATE);
            headerTitleList.add(getString(R.string.title_merge_by_phone));
        }
        if (DuplicatesUtils.getMergeMailContacts() != null && !DuplicatesUtils.getMergeMailContacts().isEmpty()) {
            mHeaderIndexList.add(SPINNER_MAIL_DUPLICATE);
            headerTitleList.add(getString(R.string.title_merge_by_mail));
        }
        if (mHeaderIndexList.size() > 1) {
            if (mSpinner == null) {
                mSpinner = new Spinner(this);
                mSpinner.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (mCurrentIndex != position) {
                            mCurrentIndex = position;
                            getData(mHeaderIndexList.get(position));
                            updateData();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                mHeaderView.addView(mSpinner);
            }
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, headerTitleList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapter);
        } else if (mHeaderIndexList.size() > 0) {
            TextView textView = new TextView(this);
            textView.setText(headerTitleList.get(0));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_spinner));
            textView.setTextColor(Color.BLACK);
            mHeaderView.removeAllViews();
            mHeaderView.addView(textView);
        } else {
            finish();
            return;
        }
        mCurrentIndex = 0;
        getData(mHeaderIndexList.get(0));
    }

    private void getData(int pos) {
        switch (pos) {
            case SPINNER_NAME_DUPLICATE:
                mMergeList = DuplicatesUtils.getMergeRawContacts();
                break;
            case SPINNER_MAIL_DUPLICATE:
                mMergeList = DuplicatesUtils.getMergeMailContacts();
                break;
            case SPINNER_PHONE_DUPLICATE:
                mMergeList = DuplicatesUtils.getMergePhoneContacts();
                break;
        }
    }

    @Override
    protected void initData() {
        mHeaderView = (FrameLayout) findViewById(R.id.header_merge_contact);
        setupViewHeader();
        if (mHeaderIndexList == null || mHeaderIndexList.isEmpty()) {
            return;
        }
        if (adapter == null) {
            adapter = new MergeContactAdapter(BtalkMergeContactActivity.this);
            adapter.setData(mMergeList);
            getListView().setAdapter(adapter);
        } else {
            adapter.setData(mMergeList);
            adapter.notifyDataSetChanged();
        }
        mSelectCount = mMergeList.size();
    }

    private void updateData() {
        adapter.setData(mMergeList);
        adapter.notifyDataSetChanged();
    }

    /**
     * Anhdts hoan thanh merge
     */
    @Override
    protected void completeMerge() {
        if (mHeaderIndexList.get(mCurrentIndex) == SPINNER_NAME_DUPLICATE) {
            DuplicatesUtils.clearMergeRawContacts();
        } else if (mHeaderIndexList.get(mCurrentIndex) == SPINNER_PHONE_DUPLICATE) {
            DuplicatesUtils.clearMergePhoneContacts();
        } else if (mHeaderIndexList.get(mCurrentIndex) == SPINNER_MAIL_DUPLICATE) {
            DuplicatesUtils.clearMergeMailContacts();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        });
    }
}
