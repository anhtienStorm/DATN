package bkav.android.btalk.speeddial;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.dialer.database.DialerDatabaseHelper;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkSpeedDialActivity;
import bkav.android.btalk.calllog.BtalkCallLogSearchFragment;
import bkav.android.btalk.suggestmagic.SuggestPopup;
import bkav.android.btalk.utility.BtalkConst;
import bkav.android.btalk.view.EditTextKeyListener;

/**
 * Created by anhdt on 04/12/2017.
 * giao dien search speed
 */

public class BtalkSpeedDialSearchFragment extends BtalkCallLogSearchFragment implements SuggestPopup.ActionSmartSuggest, View.OnClickListener {

    private EditTextKeyListener mSearchView;

    private View mClearSearchView;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setRetainInstance(true);
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.btalk_search_speeddial_fragment, null);
        mSearchView = (EditTextKeyListener) view.findViewById(R.id.search_view);
        mSearchView.setHint(getString(R.string.hint_findContacts));
        mSearchView.addTextChangedListener(new SearchTextWatcher());
        view.findViewById(R.id.search_back_button).setOnClickListener(this);

        // Bkav HuyNQN them su kien onClick cua nut add
        view.findViewById(R.id.search_add_contact).setOnClickListener(this);

        mClearSearchView = view.findViewById(R.id.search_close_button);
        mClearSearchView.setVisibility(View.GONE);
        mClearSearchView.setOnClickListener(this);
        mSearchView.setOnBackPressListener(new EditTextKeyListener.OnBackPressListener() {
            @Override
            public boolean onBackState() {
                existSearchMode();
                return false;
            }
        });
        return view;
    }

    /**
     * Anhdts: lằng nghe sự kiện text thay đổi.
     */
    private class SearchTextWatcher implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence queryString, int start, int before, int count) {
            if (queryString.equals(mQueryString) || (TextUtils.isEmpty(queryString) && mQueryString == null)) {
                return;
            }
            setQueryString(String.valueOf(queryString));
            ((BtalkSpeedDialActivity) getActivity()).querySmartContact(String.valueOf(queryString), BtalkSpeedDialSearchFragment.this);
            mClearSearchView.setVisibility(
                    TextUtils.isEmpty(queryString) ? View.GONE : View.VISIBLE);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
    }

    @Override
    public void onClickMessage() {

    }

    @Override
    public void onClick(DialerDatabaseHelper.ContactNumber data) {
        clearTextSearch();
    }

    private void clearTextSearch() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSearchView.setText("");
                mSearchView.setCursorVisible(true);
                mSearchView.requestFocus();
                existSearchMode();
            }
        }, BtalkConst.DELAY_CLEAR_TEXT_SEARCH); // Them delay vi xoa luon tao cam giac giat view khi goi dien
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_close_button: {
                mSearchView.setText("");
                break;
            }
            case R.id.search_back_button: {
                existSearchMode();
                break;
            }

            // Bkav HuyNQN them su kien bam vao add contact
            case R.id.search_add_contact: {
                addNewContact();
                break;
            }
        }
    }

    public void existSearchMode() {
        mSearchView.setText("");
        InputMethodManager imm = (InputMethodManager) mSearchView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
        ((BtalkSpeedDialActivity) getActivity()).existSearchMode();
    }

    // Bkav HuyNQN xu ly xu kien add contact
    public void addNewContact() {
        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            intent.putExtras(extras);
        }
        try {
            ImplicitIntentsUtil.startActivityInApp(getActivity(), intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), R.string.missing_app,
                    Toast.LENGTH_SHORT).show();
        }
    }

}
