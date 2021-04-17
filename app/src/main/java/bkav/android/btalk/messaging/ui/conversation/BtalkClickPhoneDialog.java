package bkav.android.btalk.messaging.ui.conversation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 23/06/2017.
 * class show giao dien va xu ly su kien dialog khi click vao sdt co trong noi dung tin nhan
 */

public class BtalkClickPhoneDialog extends DialogFragment implements View.OnClickListener {

    private View mContentView;
    private TextView mTitle;
    private View mCall, mText, mAddContact, mCopy, mEdit;
    private static final String NAME_KEY = "name_key";
    private static final String PHONE_KEY = "phone_key";
    private String mPhoneNum;
    private String mName;

    public interface OnOptionClickListener {

        void onCall(String phone);

        void onSendMessage(String phone);

        void onAddContact(String phone);

        void onCopy(String phone);

        void onOpenDigitPad(String phone);
    }

    private OnOptionClickListener mListener;

    @Override
    public void onResume() {
        super.onResume();
        // Bkav TienNAb: sua lai kich thuoc dialog
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.click_phone_number_and_click_mail_width), ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setListener(OnOptionClickListener listener) {
        this.mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Bkav TienNAb: sua lai mau nen va vi tri hien thi dialog
        getDialog().getWindow().setGravity(Gravity.CENTER);
        getDialog().getWindow().setBackgroundDrawableResource(R.color.btalk_transparent_view);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public static BtalkClickPhoneDialog newInstance(String nameContact, String phoneNum) {
        Bundle args = new Bundle();
        args.putString(NAME_KEY, nameContact);
        args.putString(PHONE_KEY, phoneNum);
        BtalkClickPhoneDialog fragment = new BtalkClickPhoneDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhoneNum = getArguments().getString(PHONE_KEY);
        if (getArguments().getString(NAME_KEY) != null) {
            mName = getArguments().getString(NAME_KEY) + " - ";
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mContentView = inflater.inflate(R.layout.btalk_dialog_click_phone_number, null);
        binView();
        handleCLick();
        builder.setView(mContentView);
        return builder.create();
    }

    private void handleCLick() {
        mCall.setOnClickListener(this);
        mText.setOnClickListener(this);
        mAddContact.setOnClickListener(this);
        mCopy.setOnClickListener(this);
        mEdit.setOnClickListener(this);
    }

    private void binView() {
        mTitle = (TextView) mContentView.findViewById(R.id.lbl_title);
        mCall = mContentView.findViewById(R.id.layout_call);
        mText = mContentView.findViewById(R.id.layout_message);
        mAddContact = mContentView.findViewById(R.id.layout_add_contact);
        mCopy = mContentView.findViewById(R.id.layout_copy);
        mEdit = mContentView.findViewById(R.id.layout_edit);
        if (mName != null) {
            updateViewNameNotNull();
        }
        String title = mName != null ? mName + mPhoneNum : mPhoneNum;
        mTitle.setText(title);
	// Anhdts cho phep sua luon truoc khi thuc hien action
        if (mTitle instanceof EditText) {
            if (TextUtils.isEmpty(mName)) {
                ((EditText)mTitle).setSelection(mTitle.length());
                mTitle.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        mPhoneNum = s.toString();
                    }
                });
            } else {
                mTitle.setCursorVisible(false);
                mTitle.setFocusable(false);
            }
        }
    }

    /**
     * Bkav QuangNDB an hien cac option trong trg sdt do co trong danh ba
     */
    private void updateViewNameNotNull() {
        mAddContact.setVisibility(View.GONE);
        mEdit.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            switch (v.getId()) {
                case R.id.layout_call:
                    mListener.onCall(mPhoneNum);
                    break;
                case R.id.layout_message:
                    mListener.onSendMessage(mPhoneNum);
                    break;
                case R.id.layout_add_contact:
                    mListener.onAddContact(mPhoneNum);
                    break;
                case R.id.layout_copy:
                    mListener.onCopy(mPhoneNum);
                    break;
                case R.id.layout_edit:
                    mListener.onOpenDigitPad(mPhoneNum);
                    break;
            }
        }

    }
}
