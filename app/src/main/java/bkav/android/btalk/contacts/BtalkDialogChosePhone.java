package bkav.android.btalk.contacts;


import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.contacts.ContactSaveService;
import com.android.contacts.common.model.dataitem.DataItem;
import com.android.contacts.common.model.dataitem.PhoneDataItem;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.R;

/**
 * AnhNDd: Dialog Thị khi lựa chọn contact có 2 số điện thoại trở lên, khi click trực tiếp vào contact để thực hiện cuộc gọi.
 */
public class BtalkDialogChosePhone extends DialogFragment {

    private LinearLayout mViewGroupPhone;
    private Context mContext;
    private List<DataItem> mPhoneDataItemsList;
    private List<View> mPhoneViewList = new ArrayList<>();
    private CheckBox mCheckBoxSetPrimary;

    public static String DIALOG_TAG = "dialog_chose_phone";

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static BtalkDialogChosePhone newInstance(int num) {
        BtalkDialogChosePhone f = new BtalkDialogChosePhone();

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

        if (mPhoneDataItemsList == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 200);
        } else {
            inflateAllPhone(LayoutInflater.from(getActivity()));
            insertEntriesIntoViewGroupPhone();
            //AnhNDd: check box cho việc đặt làm số điện thoại mặc định.
            mCheckBoxSetPrimary = (CheckBox) v.findViewById(R.id.setPrimary);
        }
        return v;
    }

    public void insertEntriesIntoViewGroupPhone() {
        mViewGroupPhone.removeAllViews();

        for (View view : getViewsToDisplay()) {
            mViewGroupPhone.addView(view);
        }
    }

    public void setData(Context context, List<DataItem> phoneDataItems) {
        mContext = context;
        mPhoneDataItemsList = phoneDataItems;
    }

    private List<View> getViewsToDisplay() {
        final List<View> viewsToDisplay = new ArrayList<View>();
        for (int i = 0; i < mPhoneViewList.size(); i++) {
            viewsToDisplay.add(mPhoneViewList.get(i));
        }
        return viewsToDisplay;
    }

    //AnhNDd: vẽ các view cho dialog.
    public void inflateAllPhone(LayoutInflater layoutInflater) {
        int size = mPhoneDataItemsList.size();
        for (int i = 0; i < size; i++) {
            //AnhNDd: dữ liệu lấy vào là PhoneDataItem .
            PhoneDataItem phoneDataItem = (PhoneDataItem) mPhoneDataItemsList.get(i);
            mPhoneViewList.add(createPhoneView(layoutInflater, phoneDataItem));
            // Bkav QuangNDb bo dai phan cach giua cac so dien thoai di
//            if (i < size - 1) {
//                mPhoneViewList.add(createDividerView());
//            }
        }
    }

    //AnhNDd: vẽ đường phân cách giữa các số điện thoại.
    public View createDividerView() {
        View dividerView = new View(mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.btalk_section_divider_height));
        dividerView.setLayoutParams(lp);
        dividerView.setBackgroundColor(Color.BLACK);
        return dividerView;
    }

    //AnhNDd: Hàm để khởi tạo view và đổ dữ liệu
    public View createPhoneView(LayoutInflater layoutInflater, final PhoneDataItem phoneDataItem) {
        final View view = (View) layoutInflater.inflate(
                R.layout.phone_disambig_item, mViewGroupPhone, false);
        final TextView text = (TextView) view.findViewById(R.id.text1);
        String type = phoneDataItem.getContentValues().getAsString(ContactsContract.CommonDataKinds.Phone.TYPE);
        // Anhdts fix loi type empty, k ro khi nao thi bi nua
        CharSequence typeLabel = "";
        if (!type.isEmpty()) {
            typeLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(mContext.getResources(), Integer.parseInt(type), phoneDataItem.getLabel());
        }
        if (!TextUtils.isEmpty(typeLabel)) {
            text.setText(typeLabel);
        } else {
            text.setVisibility(View.GONE);
        }

        //AnhNDd: hiển thị số điện thoại.
        final String num = phoneDataItem.getNumber();
        final TextView header = (TextView) view.findViewById(R.id.text2);
        if (!TextUtils.isEmpty(num)) {
            header.setText(num);
        } else {
            header.setVisibility(View.GONE);
        }

        //AnhNDd: sự kiện click vào một số điện thoại.
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callAction(phoneDataItem);
            }
        });
        ImageView callInternet = (ImageView) view.findViewById(R.id.bkav_call_internet);
        callInternet.setVisibility(View.GONE);
        return view;
    }

    //AnhNDd: Thực hiện cuộc gọi đến 1 số.
    public void callAction(PhoneDataItem phoneDataItem) {
        if (mCheckBoxSetPrimary.isChecked()) {
            //AnhNDd: Thực hiện việc set số điện thoại là mặc định.
            final Intent setIntent = ContactSaveService.createSetSuperPrimaryIntent(mContext,
                    phoneDataItem.getId());
            mContext.startService(setIntent);
        }
        Intent in = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneDataItem.getNumber()));
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
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(in);
        // TrungTH them vao
        if (mIDialogCallback != null) {
            mIDialogCallback.onCallActionFinish();
        }
        this.dismiss();
    }

    // Them doan callback cho dialog
    public interface IDialogCallback {
        public void onCallActionFinish();
    }

    private IDialogCallback mIDialogCallback;

    public void setDialogCallback(IDialogCallback dialogCallback) {
        mIDialogCallback = dialogCallback;
    }
}
