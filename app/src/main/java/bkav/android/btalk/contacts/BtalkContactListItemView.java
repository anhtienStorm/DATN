package bkav.android.btalk.contacts;


import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v4.text.BidiFormatter;
import android.support.v4.text.TextDirectionHeuristicsCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.contacts.common.compat.CompatUtils;
import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.util.HelpUtils;
import com.android.messaging.Factory;

import java.util.StringTokenizer;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.BtalkFactoryImpl;

/**
 * AnhNDd :lớp kế thừa từ class giao diện từng view itemcontact để thực hiện tùy chỉnh lại view
 * hoặc thêm mới thuộc tính.
 */
public class BtalkContactListItemView extends ContactListItemView {

    //AnhNDd: View gạch ngang trên contact
    private View mViewDividerSection;

    private int mViewDividerSectionHeight = 1;

    private int mViewDividerSectionBackgroundColor = Color.BLACK;

    private int mContactViewDetailsPaddingRight;

    protected int mContactViewDetailsBackGround;

    //AnhNDd: padding trai phai cua item
    private int mDefaultPadding;

    //AnhNDd: chieu cao cua item khi khong header
    private int mItemHeightNoHeader;

    //AnhNDd: chieu cao cua item tinh theo chieu cao vat ly cua man hinh
    private int mItemHeightPhysics;

    private boolean mIsShowNumber = false;

    private Uri mContactUri;

    private String mPhoneNumber;

    private boolean mIsMultiNumber = false;

    // Bkav HuyNQN textsize khi setting density = small
    private int mTextSizeSmall = 40;

    // Bkav HuyNQN chieu cao cua item khi khong co header khi setting density = small
    private int mItemHeightNoHeaderSmall = 171;

    // Bkav HuyNQN them nut goi
    private ImageButton mImageButtonCall;
    private int mCallWidth = (int) getResources().getDimension(R.dimen.button_call_witdh);
    private int mCallHeight = (int) getResources().getDimension(R.dimen.button_call_height);

    // Bkav HuyNQN them nut message
    private ImageButton mImageButtonMessage;
    private int mMessageWidth = (int) getResources().getDimension(R.dimen.button_call_witdh);
    private int mMessageHeight = (int) getResources().getDimension(R.dimen.button_call_height);

    protected int mCallPaddingRight;
    protected int mMessagePaddingCall;
    protected int mMessagePaddingNameText;

    public IOnClickMessageButton mOnClickMessageButton;

    // Bkav HuyNQN lay ra so dien thoai khi nguoi dung cai dat khong hien thi so dien thoai
    public interface ContactListItemListener {

        void showDialog(String number, String action);

        void actionCall(String number);
    }

    public void setItemListener(ContactListItemListener mItemListener) {
        this.mItemListener = mItemListener;
    }

    private ContactListItemListener mItemListener;

    public static final String ACTION_SELECT_SEND = "send";
    public static final String ACTION_SELECT_CALL = "call";


    public BtalkContactListItemView(Context context) {
        super(context);
    }

    public BtalkContactListItemView(Context context, AttributeSet attrs, boolean supportVideoCallIcon) {
        this(context, attrs);

        mSupportVideoCallIcon = supportVideoCallIcon;
    }

    public BtalkContactListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //AnhNDd: Text color của tên liên hệ
        mNameTextViewTextColor = getResources().getColor(R.color.bkav_contact_name_text_color);

        mViewDividerSectionBackgroundColor = getResources().getColor(R.color.btalk_color_list_common_divider);

        mContactViewDetailsPaddingRight = (int) getResources().getDimension(R.dimen.btalk_paddingRight_image_details);

        mNameTextViewTextSize = (int) getResources().getDimension(R.dimen.btalk_list_item_contact_text_size);

        mDefaultPhotoViewSize = (int) getResources().getDimension(R.dimen.btalk_list_item_contact_photo_size);

        mDefaultPadding = (int) getResources().getDimension(R.dimen.btalk_list_item_contact_padding_left);

        mGapBetweenImageAndText = (int) getResources().getDimension(R.dimen.btalk_contact_browser_list_item_gap_between_image_and_text);

        // Bkav HuyNQN cai dat kich thuoc pasdding cac thanh phan trong contact_item
        mCallPaddingRight = 0; // Bkav HuyNQN khong padding Right nua
        mMessagePaddingCall = (int) getResources().getDimension(R.dimen.button_call_magin_buttom_message);
        mMessagePaddingNameText = (int) getResources().getDimension(R.dimen.buttom_message_magin_text_name);

        //AnhNDd: lấy ra background hình tròn mờ khi click vào item .
        int[] a = new int[]{R.attr.selectableItemBackgroundBorderless};
        TypedArray typedArray = context.obtainStyledAttributes(a);
        mContactViewDetailsBackGround = typedArray.getResourceId(0, 0);
        typedArray.recycle();

        //this.setBackgroundResource(mContactViewDetailsBackGround);

        //AnhNDd: Lấy chiều cao vật lý của màn hình trừ đi chiều cao actionbar và statusbar
        int statusbarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusbarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int actionbarHeight = (int) getResources().getDimension(R.dimen.bkav_actionbar_height);
        //AnhNDd: tren man hinh chi hien thi 7 item
        mItemHeightPhysics = (screenHeight - 2 * actionbarHeight - statusbarHeight) / 7;
    }

    // Bkav HuyNQN cai dat cho nut call
    public ImageView setImageButtonCall(final Uri mPhoneUri) {
        if (mImageButtonCall == null) {
            mImageButtonCall = new ImageButton(getContext());

            // Bkav HuyNQN Tach code xu ly o lop con
            setResourcesImageCall(mImageButtonCall);
            addView(mImageButtonCall);
            mImageButtonCall.setVisibility(VISIBLE);

            // Bkav HuyNQN thuc hien an nut goi neu contact khong co so dien thoai
            if (mPhoneUri == null) {
                mImageButtonCall.setVisibility(INVISIBLE);
            }

            // Bkav HuyNQN them background khi click
            TypedValue outValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            mImageButtonCall.setBackgroundResource(outValue.resourceId);
        }

        mImageButtonCall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Bkav HuyNQN hien thi dialog khi contact co nhiwu so dien thoai
                String numberPhone = getPhoneNumber();
                StringTokenizer stringTokenizer = new StringTokenizer(numberPhone, ";", false);
                if (stringTokenizer.countTokens() > 1) {
                    if (mItemListener != null) {
                        mItemListener.showDialog(numberPhone, ACTION_SELECT_CALL);
                    }
                } else if (!numberPhone.isEmpty()) {
//                    Intent callIntent = new Intent(Intent.ACTION_CALL);
//                    callIntent.setData(Uri.parse("tel:" + numberPhone));
//                    getContext().startActivity(callIntent);
                    if (mItemListener != null) {
                        mItemListener.actionCall(numberPhone);
                    }
                }
            }
        });
        return mImageButtonCall;
    }

    // Bkav HuyNQN cai dat cho nut sms
    public ImageView setImageButtonMessenge(final Uri mPhoneUri) {
        if (mImageButtonMessage == null) {
            mImageButtonMessage = new ImageButton(getContext());
            // Bkav HuyNQN Tach code xu ly o lop con
            setResourcesImageMessage(mImageButtonMessage);
            addView(mImageButtonMessage);
            mImageButtonMessage.setVisibility(VISIBLE);

            // Bkav HuyNQN thuc hien an nut messages neu contact khong co so dien thoai
            if (mPhoneUri == null) {
                mImageButtonMessage.setVisibility(INVISIBLE);
            }

            // Bkav HuyNQN them background khi click
            TypedValue outValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            mImageButtonMessage.setBackgroundResource(outValue.resourceId);
        }
        mImageButtonMessage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Bkav HuyNQN hien thi dialog khi contact co nhiwu so dien thoai
                String numberPhone = getPhoneNumber();
                StringTokenizer stringTokenizer = new StringTokenizer(numberPhone, ";", false);
                if (stringTokenizer.countTokens() > 1) {
                    if (mItemListener != null) {
                        mItemListener.showDialog(numberPhone, ACTION_SELECT_SEND);
                    }
                } else if (!numberPhone.isEmpty()) {
                    //HienDTk: bam vao icon_message o tab danh ba thi cho an search
                    mOnClickMessageButton.onClick();
                    Uri uri = Uri.parse("smsto:" + numberPhone);
                    Intent messageIntent = new Intent(Intent.ACTION_SENDTO, uri);
                    // Bkav HuyNQN dang ki gui tin nhan trong app de khi back ra thi van o lai trong contact
                    registerFactoryActionSend(messageIntent);
                    getContext().startActivity(messageIntent);
                }
            }
        });
        return mImageButtonMessage;
    }
    /**
     * HienDTk: bat su kien bam vao icon_message cho an view search ben tab danh ba
     */
    public void setOnClickMessageButton(IOnClickMessageButton onClickMessageButton){
        this.mOnClickMessageButton = onClickMessageButton;
    }

    public interface IOnClickMessageButton{
        void onClick();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //AnhNDd: Set measure cho divider
        final int specWidth = resolveSize(0, widthMeasureSpec);
        getViewDividerSection().measure(
                MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mViewDividerSectionHeight, MeasureSpec.EXACTLY));

        // Bkav HuyNQN measure cho nut goi, cai dat co dinh width va height
        setImageButtonCall(mContactUri).measure(
                MeasureSpec.makeMeasureSpec(mCallWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mCallHeight, MeasureSpec.EXACTLY));

        // Bkav HuyNQN measure cho nut nhan tin, cai dat co dinh width va height
        setImageButtonMessenge(mContactUri).measure(
                MeasureSpec.makeMeasureSpec(mMessageWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mMessageHeight, MeasureSpec.EXACTLY));

        // Bkav HuyNQN thuc hien an di 2 button o lop con
        setInvisibleButton(mImageButtonCall, mImageButtonMessage);
    }

    /**
     * AnhNDd: add view đường gạch ngang trong contact
     */
    public View getViewDividerSection() {
        if (mViewDividerSection == null) {
            mViewDividerSection = new View(getContext());
            mViewDividerSection.setBackgroundColor(mViewDividerSectionBackgroundColor);
            addView(mViewDividerSection);
            mViewDividerSection.setVisibility(VISIBLE);
        }
        return mViewDividerSection;
    }


    @Override
    public void hookToSetTextAppearance() {
        mHeaderTextView.setTextAppearance(getContext(), R.style.BtalkSectionHeaderStyle);
    }


    @Override
    public int hookToSetHeaderTopBound(int current) {
        return 0;
    }

    @Override
    public int hookToSetHeaderWidth(int current) {
        return 0;
    }

    @Override
    public int hookToSetGapBetweenImageAndText(int current) {
        return 0;
    }

    @Override
    public void hookToSetPaddingRelative(int start, int top, int end, int bottom) {
        //setPaddingRelative(start, 0, end / 2, 0);
        int padding = (int) getResources().getDimension(R.dimen.btalk_list_item_contact_padding_left);
        setPaddingRelative(padding, 0, padding, 0);
    }

    @Override
    public int hookToIncreaseHeightItemView(int currentHeight, int increase) {
        return super.hookToIncreaseHeightItemView(currentHeight, increase) + (int) mViewDividerSectionHeight;
    }

    @Override
    public int hookToChangeBottomBound(int current) {
        if (mIsSectionHeaderEnabled) {
            if (mHeaderTextView != null) {
                return current + mHeaderTextView.getMeasuredHeight();
            }
        }
        return current;
    }

    @Override
    public int hookToChangeCheckboxTop(int current) {
        if (mIsSectionHeaderEnabled) {
            if (mHeaderTextView != null) {
                return current + mHeaderTextView.getHeight() / 2;
            }
        }
        return current;
    }

    @Override
    public void setMeasureHeaderView() {
        mHeaderTextView.measure(
                MeasureSpec.makeMeasureSpec(mHeaderWidth, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        //AnhNDd: Tính lại độ rộng
        mHeaderWidth = mHeaderTextView.getMeasuredWidth();
    }

    /**
     * AnhNDd: Khi item được chọn thì thay đổi màu nền.
     */
    public void setChecked(boolean bool) {
        if (bool) {
            setBackgroundColor(Color.LTGRAY);
        } else {
            setBackgroundColor(ContextCompat.getColor(getContext(), R.color.btalk_transparent_view));
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        getPhotoView().setEnabled(enabled);

        super.setEnabled(enabled);
    }

    @Override
    public void setClickable(boolean clickable) {
        getPhotoView().setClickable(clickable);
        getPhotoView().setLongClickable(clickable
        );
    }

    @Override
    public int calculateWidthWithSectionHeader() {
        return 0; // Bkav TrungTH, vi gio HEADer o ben tren roi nen ko lien quan den chieu rong
    }

    @Override
    public TextView getNameTextView() {
        mNameTextView = super.getNameTextView();
        mNameTextView.setTextColor(Color.BLACK);
        mNameTextView.setTypeface(Typeface.SANS_SERIF);
        return mNameTextView;
    }

    @Override
    public void hookToSetMeasureDimension(int specWidth, int heightMeasureSpec, int height) {
        int heightIncree = mItemHeightPhysics - mItemHeightNoHeader;
        int finalHeightIncree = heightIncree > 0 ? heightIncree : 0;

        // Bkav HuyNQN thay doi lai chieu cao cua item khi density = small, check NameTextView khac null
        if (mNameTextView != null) {
            if (mNameTextView.getTextSize() == mTextSizeSmall) {
                setMeasuredDimension(specWidth, height + finalHeightIncree);
            } else {
                setMeasuredDimension(specWidth, height + finalHeightIncree);
            }
        } else {
            setMeasuredDimension(specWidth, height + finalHeightIncree);
        }
    }

    @Override
    public void setItemHeightNoHeader(int height) {

        // Bkav HuyNQN thay doi lai chieu cao cua item khi density = small, check NameTextView khac null
        if (mNameTextView != null) {
            if (mNameTextView.getTextSize() == mTextSizeSmall) {
                mItemHeightNoHeader = mItemHeightNoHeaderSmall;
            } else {
                mItemHeightNoHeader = height;
            }
        }
    }

    @Override
    protected int getNotFirstEntryHeight(int height) {
        return mSnippetTextViewHeight + mStatusTextViewHeight + mLabelAndDataViewMaxHeight + mPhoneticNameTextViewHeight;
    }

    private static final String PHONE_NUMBER_SELECTION =
            ContactsContract.Data.MIMETYPE + " IN ('"
                    + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "', "
                    + "'" + ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE + "') AND "
                    + ContactsContract.Data.DATA1 + " NOT NULL";

    // Bkav HienDTk: fix bug - BOS-3363
    // Bkav HienDTk: them cursor de lay text search
    public void setLookupUri(Uri contactUri, boolean showNumber, Cursor cursor) {
        mIsShowNumber = showNumber;
        mContactUri = contactUri;
        mPhoneNumber = null;
        mIsMultiNumber = false;
        if (contactUri == null || !mIsShowNumber) {
            if (mDataView != null) {
                mDataView.setVisibility(GONE);
            }

            invalidate();
            return;
        }
        // Bkav HienDTk: fix bug - BOS-3363 - Start
        // Bkav HienDTk: lay text search de boi vang sdt khi search
        final Bundle extras = cursor.getExtras();
        final String query = extras.getString(ContactsContract.DEFERRED_SNIPPETING_QUERY);
        mPhoneNumber = getPhoneNumber();

        setPhoneNumber(mPhoneNumber, null, query);
        // Bkav HienDTk: fix bug - BOS-3363 - End
        if (mDataView != null && !TextUtils.isEmpty(mPhoneNumber)) {
            mDataView.setVisibility(VISIBLE);
            invalidate();
        }
    }

    private String getPhoneNumber() {
        final Uri queryUri;
        final String inputUriAsString = mContactUri.toString();
        if (inputUriAsString.startsWith(ContactsContract.Contacts.CONTENT_URI.toString())) {
            if (!inputUriAsString.endsWith(ContactsContract.Contacts.Data.CONTENT_DIRECTORY)) {
                queryUri = Uri.withAppendedPath(mContactUri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
            } else {
                queryUri = mContactUri;
            }
        } else if (inputUriAsString.startsWith(ContactsContract.Data.CONTENT_URI.toString())) {
            queryUri = mContactUri;
        } else {
            throw new UnsupportedOperationException(
                    "Input Uri must be contact Uri or data Uri (input: \"" + mContactUri + "\")");
        }
        String[] project = CompatUtils.isOCompatible() ? new String[]{
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                "default_sim"
        } : new String[]{
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        Cursor cursor = getContext().getApplicationContext().getContentResolver().query(
                queryUri,
                project,
                PHONE_NUMBER_SELECTION,
                null,
                null
        );

        // Bkav HuyNQN format lai so dien thoai hien theo mot kieu nhat dinh
        final BidiFormatter bidiFormatter = BidiFormatter.getInstance();
        String bidiFormaterNumber;

        StringBuilder number = new StringBuilder();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.isLast()) {
                    number.append(HelpUtils.formatNumber(cursor.getString(2))); // Bkav HuyNQN format so dien thoai
                } else {
                    number.append(HelpUtils.formatNumber(cursor.getString(2))); // Bkav HuyNQN format so dien thoai
                    number.append("; ");
                }
            }
            cursor.close();
        }
        bidiFormaterNumber = bidiFormatter.unicodeWrap(number.toString(), TextDirectionHeuristicsCompat.LTR);

        return bidiFormaterNumber;
    }

    // Bkav HuyNQN overrite cai dat resource imagecall va imageMessage
    @Override
    protected void setResourcesImageCall(ImageButton imageButton) {
        imageButton.setImageResource(R.drawable.ic_contact_call);
    }

    @Override
    protected void setResourcesImageMessage(ImageButton imageButtonMessage) {
        imageButtonMessage.setImageResource(R.drawable.bkav_ic_message_recent_call);
    }

    protected void setInvisibleButton(ImageButton mImageButtonCall, ImageButton mImageButtonMessage) {
        // Bkav HuyNQN thuc hien an nut tai lop con
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        //AnhNDd: set vị trí của section divider.
        int headerHeight = 0;
        if (mIsSectionHeaderEnabled) {
            if (mHeaderTextView != null) {
                headerHeight = mHeaderTextView.getHeight();
            }
        }
        mViewDividerSection.layout(getPaddingLeft(), headerHeight, mViewDividerSection.getMeasuredWidth(), headerHeight + mViewDividerSectionHeight);

        mBoundsWithoutHeader.set(0, 0, right, bottom - top);

    }

    @Override
    protected void setMesuaDataView(int dataWidth) {
        mDataView.measure(MeasureSpec.makeMeasureSpec(dataWidth - mMessagePaddingNameText - mMessagePaddingCall - mCallWidth - mMessageWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    }

    @Override
    protected void setMesuaNameTextView(int nameTextWidth) {
        mNameTextView.measure(
                // Bkav HuyNQN sua lai khoang cach khi them 2 button call va sms
                MeasureSpec.makeMeasureSpec(nameTextWidth - mMessagePaddingNameText - mMessagePaddingCall - mCallWidth - mMessageWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    }

    @Override
    protected void setOnLayoutButtonCallAndMessage(int rightBound, int bottomBound) {
        int callTop = ((int) bottomBound / 2) - ((int) mCallHeight / 2);
        mImageButtonCall.layout(rightBound - mCallWidth, callTop,
                rightBound,
                callTop + mCallHeight);

        mImageButtonMessage.layout(rightBound - mCallWidth - mMessageWidth - mMessagePaddingCall, callTop,
                rightBound - mCallWidth - mMessagePaddingCall, callTop + mMessageHeight);
    }

    @Override
    protected void setNameTextViewVisible(int leftBound, int textTopBound, int rightBound, int distanceFromEnd) {
        if (mPhotoPosition == PhotoPosition.LEFT) {
            mNameTextView.layout(leftBound,
                    textTopBound,
                    rightBound - distanceFromEnd - mMessagePaddingNameText - mMessagePaddingCall - mCallWidth - mMessageWidth, // Bkav HuyNQN sua lai khoang cach khi them 2 button call va sms
                    textTopBound + mNameTextViewHeight);
        } else {
            mNameTextView.layout(leftBound + distanceFromEnd,
                    textTopBound,
                    rightBound - mMessagePaddingNameText - mMessagePaddingCall - mCallWidth - mMessageWidth, // Bkav HuyNQN sua lai khoang cach khi them 2 button call va sms
                    textTopBound + mNameTextViewHeight);
        }
    }

    @Override
    protected void setDataViewVisible(boolean isLayoutRtl, int dataLeftBound, int textTopBound, int rightBound, int DataViewHeight) {
        if (!isLayoutRtl) {
            mDataView.layout(dataLeftBound,
                    textTopBound + mLabelAndDataViewMaxHeight - DataViewHeight,
                    rightBound - mMessagePaddingCall - mCallWidth - mMessageWidth, // Bkav HuyNQN sua lai khoang cach khi them 2 button call va sms
                    textTopBound + mLabelAndDataViewMaxHeight);
        } else {
            mDataView.layout(rightBound - mDataView.getMeasuredWidth(),
                    textTopBound + mLabelAndDataViewMaxHeight - DataViewHeight,
                    rightBound - mMessagePaddingCall - mCallWidth - mMessageWidth, // Bkav HuyNQN sua lai khoang cach khi them 2 button call va sms
                    textTopBound + mLabelAndDataViewMaxHeight);
        }
    }

    // Bkav HuyNQN dang ki gui tin nhan trong app
    protected void registerFactoryActionSend(Intent intent) {
        ((BtalkFactoryImpl) Factory.get()).registerSendMessage();
    }
}
