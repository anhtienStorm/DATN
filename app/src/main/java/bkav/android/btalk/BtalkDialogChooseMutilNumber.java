package bkav.android.btalk;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.contacts.ContactSaveService;
import com.android.dialer.TransactionSafeActivity;
import com.android.dialer.util.TelecomUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.esim.dialog_choose_sim.DialogChooseSimFragment;

/**
 * Created by anhdt on 19/07/2017.
 *
 */

public class BtalkDialogChooseMutilNumber extends DialogFragment implements Loader.OnLoadCompleteListener<Cursor> {

    private LinearLayout mViewGroupPhone;

    private Context mContext;

    private List<DataPhone> mPhoneDataItemsList;

    private List<View> mPhoneViewList = new ArrayList<>();

    private CheckBox mCheckBoxSetPrimary;

    private CursorLoader mLoader;

    private Uri mUri;

    private static final String URI_DATA = "uri_data";

    private static final String[] PHONE_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone._ID,                  // 0
            ContactsContract.CommonDataKinds.Phone.NUMBER,              // 1
            ContactsContract.CommonDataKinds.Phone.LABEL,               // 2
            ContactsContract.CommonDataKinds.Phone.TYPE                 // 3
    };

    private static final String PHONE_NUMBER_SELECTION =
            ContactsContract.Data.MIMETYPE + " IN ('"
                    + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "', "
                    + "'" + ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE + "') AND "
                    + ContactsContract.Data.DATA1 + " NOT NULL";

    public static String DIALOG_TAG = "dialog_chose_phone";

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static BtalkDialogChooseMutilNumber newInstance(int num) {
        BtalkDialogChooseMutilNumber f = new BtalkDialogChooseMutilNumber();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.btalk_dialog_chose_phone_to_call, container, false);
        mViewGroupPhone = (LinearLayout) v.findViewById(R.id.content_area_linear_layout_chose_phone);

        mCheckBoxSetPrimary = (CheckBox) v.findViewById(R.id.setPrimary);

        if (mPhoneDataItemsList != null) {
            inflateAllPhone(LayoutInflater.from(mContext));
            insertEntriesIntoViewGroupPhone();
        }
        if (savedInstanceState != null) {
            String uri = savedInstanceState.getString(URI_DATA);
            if (!TextUtils.isEmpty(uri)) {
                startInteraction(getActivity(), Uri.parse(uri));
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                }, 300);
            }
        }
        return v;
    }

    public void insertEntriesIntoViewGroupPhone() {
        mViewGroupPhone.removeAllViews();

        for (View view : getViewsToDisplay()) {
            mViewGroupPhone.addView(view);
        }
    }

    private List<View> getViewsToDisplay() {
        final List<View> viewsToDisplay = new ArrayList<>();
        for (int i = 0; i < mPhoneViewList.size(); i++) {
            viewsToDisplay.add(mPhoneViewList.get(i));
        }
        return viewsToDisplay;
    }

    public void inflateAllPhone(LayoutInflater layoutInflater) {
        int size = mPhoneDataItemsList.size();
        for (int i = 0; i < size; i++) {
            DataPhone phoneDataItem = mPhoneDataItemsList.get(i);
            mPhoneViewList.add(createPhoneView(layoutInflater, phoneDataItem));
            if (i < size - 1) {
                mPhoneViewList.add(createDividerView());
            }
        }
    }

    public View createDividerView() {
        View dividerView = new View(mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.btalk_section_divider_height));
        dividerView.setLayoutParams(lp);
        dividerView.setBackgroundColor(Color.BLACK);
        return dividerView;
    }

    public View createPhoneView(LayoutInflater layoutInflater, final DataPhone phoneDataItem) {
        final View view = layoutInflater.inflate(
                R.layout.phone_disambig_item, mViewGroupPhone, false);
        final TextView text = (TextView) view.findViewById(R.id.text1);
        if (phoneDataItem.mType != null) {
            CharSequence typeLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(mContext.getResources(), Integer.parseInt(phoneDataItem.mType), phoneDataItem.mLabel);
            if (!TextUtils.isEmpty(typeLabel)) {
                text.setText(typeLabel);
            } else {
                text.setVisibility(View.GONE);
            }
        } else {
            text.setVisibility(View.GONE);
        }

        final String num = phoneDataItem.mNumber;
        final TextView header = (TextView) view.findViewById(R.id.text2);
        if (!TextUtils.isEmpty(num)) {
            header.setText(num);
        } else {
            header.setVisibility(View.GONE);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action(phoneDataItem);
            }
        });
        ImageView callInternet = (ImageView) view.findViewById(R.id.bkav_call_internet);
        callInternet.setVisibility(View.GONE);
        return view;
    }

    public void action(DataPhone phoneDataItem) {
        // Bkav HienDTk: check xem co dang o che do hoi truoc khi goi kh
        PhoneAccountHandle accountDefault = TelecomUtil.getDefaultOutgoingPhoneAccount(mContext,
                PhoneAccount.SCHEME_TEL);
        if (mCheckBoxSetPrimary.isChecked()) {
            final Intent setIntent = ContactSaveService.createSetSuperPrimaryIntent(mContext,
                    phoneDataItem.mId);
            mContext.startService(setIntent);
        }
        if (mListener == null) {
            // Bkav HienDTk: neu dang o che do hoi truo khi goi thi hien thi dialog chon sim
            if(accountDefault == null){
                DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(phoneDataItem.mNumber);
                dialogChooseSimFragment.show(getFragmentManager(), "chooseSim");
            }else {
                Intent in = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneDataItem.mNumber));
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mContext.startActivity(in);
            }
        } else {
            mListener.onClick(phoneDataItem.mNumber, mActionId, (Activity)mContext);
        }
        this.dismiss();
    }

    public void startInteraction(Context context, Uri uri) {
        mContext = context;
        mUri = uri;
        if (mLoader != null) {
            mLoader.reset();
        }
        final Uri queryUri;
        final String inputUriAsString = uri.toString();
        if (inputUriAsString.startsWith(ContactsContract.Contacts.CONTENT_URI.toString())) {
            if (!inputUriAsString.endsWith(ContactsContract.Contacts.Data.CONTENT_DIRECTORY)) {
                queryUri = Uri.withAppendedPath(uri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
            } else {
                queryUri = uri;
            }
        } else if (inputUriAsString.startsWith(ContactsContract.Data.CONTENT_URI.toString())) {
            queryUri = uri;
        } else {
            throw new UnsupportedOperationException(
                    "Input Uri must be contact Uri or data Uri (input: \"" + uri + "\")");
        }

        mLoader = new CursorLoader(mContext,
                queryUri,
                PHONE_PROJECTION,
                PHONE_NUMBER_SELECTION,
                null,
                null);
        mLoader.registerListener(0, this);
        mLoader.startLoading();
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null) {
            return;
        }
        try {
            if (mPhoneDataItemsList == null) {
                mPhoneDataItemsList = new ArrayList<>();
            } else {
                mPhoneDataItemsList.clear();
            }
            if (!isSafeToCommitTransactions()) {
                return;
            }
            cursor.moveToFirst();
            do {
                DataPhone data = new DataPhone(cursor);
                mPhoneDataItemsList.add(data);
            } while (cursor.moveToNext());
        } finally {
            cursor.close();
        }
        if (mPhoneDataItemsList.isEmpty()) {
            onDestroy();
        } else {
            if (mViewGroupPhone != null) {
                inflateAllPhone(LayoutInflater.from(mContext));
                insertEntriesIntoViewGroupPhone();
            }
        }
        cursor.close();
    }

    public void setDataCursor(List<DataPhone> data, Context context) {
        mContext = context;
        mPhoneDataItemsList = data;
        if (mPhoneDataItemsList == null || mPhoneDataItemsList.isEmpty()) {
            onDestroy();
        } else {
            if (mViewGroupPhone != null) {
                inflateAllPhone(LayoutInflater.from(context));
                insertEntriesIntoViewGroupPhone();
            }
        }
    }

    private boolean isSafeToCommitTransactions() {
        return !(mContext instanceof TransactionSafeActivity) || ((TransactionSafeActivity) mContext).isSafeToCommitTransactions();
    }

    public static class DataPhone implements Serializable {
        String mNumber;

        String mType;

        String mLabel;

        long mId;

        public DataPhone(Cursor cursor) {
            mId = cursor.getLong(0);
            mNumber = cursor.getString(1);
            mLabel = cursor.getString(2);
            mType = cursor.getString(3);
        }
    }

    private ChooseNumberListener mListener;

    private int mActionId;

    public void setListener(ChooseNumberListener listener, int action) {
        mListener = listener;
        mActionId = action;
    }

    public interface ChooseNumberListener {
        void onClick(String number, int actionId, Activity activity);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(URI_DATA, mUri == null ? "" : mUri.toString());
        super.onSaveInstanceState(outState);
    }
}
