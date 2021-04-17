package bkav.android.btalk.messaging.ui.conversation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 13/09/2017.
 */

public class BtalkClickMailDialog extends DialogFragment implements View.OnClickListener {
    private View mContentView;
    private static final String MAIL_KEY = "mail_key";
    private View mText, mCopy;
    private TextView mTitle;
    private String mMailAddr;
    public interface OnOptionClickListener {
        void onSendMail(String mail);

        void onCopy(String mail);
    }
    private OnOptionClickListener mListener;
    public void setListener(OnOptionClickListener listener) {
        this.mListener = listener;
    }

    public static BtalkClickMailDialog newInstance(String mailAddr) {

        Bundle args = new Bundle();
        args.putString(MAIL_KEY, mailAddr);
        BtalkClickMailDialog fragment = new BtalkClickMailDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Bkav TienNAb: sua lai kich thuoc dialog
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.click_phone_number_and_click_mail_width), ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Bkav TienNAb: sua lai mau nen va vi tri hien thi dialog
        getDialog().getWindow().setGravity(Gravity.CENTER);
        getDialog().getWindow().setBackgroundDrawableResource(R.color.btalk_transparent_view);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMailAddr = getArguments().getString(MAIL_KEY);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mContentView = inflater.inflate(R.layout.btalk_dialog_click_mail, null);
        binView();
        handleCLick();
        builder.setView(mContentView);
        return builder.create();
    }
    private void binView() {
        mTitle = (TextView) mContentView.findViewById(R.id.lbl_title);
        mText = mContentView.findViewById(R.id.layout_message);
        mCopy = mContentView.findViewById(R.id.layout_copy);
        mTitle.setText(mMailAddr);
    }
    private void handleCLick() {
        mText.setOnClickListener(this);
        mCopy.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (mListener != null) {
            switch (v.getId()) {
                case R.id.layout_message:
                    mListener.onSendMail(mMailAddr);
                    break;
                case R.id.layout_copy:
                    mListener.onCopy(mMailAddr);
                    break;
            }
        }
    }
}
