package bkav.android.btalk.contacts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.contacts.common.util.ViewUtil;

import bkav.android.btalk.R;

/**
 * AnhNDd: View pinned trong contact: neo lại chữ cái khi scroll.
 * view bao gồm text để hiển thị và đường gạch ngang phần cách.
 */
public class BtalkContactListPinnedHeaderView extends LinearLayout {

    //AnhNDd: Text view để hiển thị chữ pinned
    private TextView mTextViewContactListPinnedHeader;
    private int mHeaderWidth;
    private int mPaddingLeft;
    private int mPaddingRight;
    //AnhNDd: View gạch chân dưới text pinned.
    private View mViewDividerSection;
    private int mViewDividerSectionHeight = 1;
    private int mViewDividerSectionBackgroundColor = Color.BLACK;


    public BtalkContactListPinnedHeaderView(Context context, AttributeSet attrs, View parent) {
        super(context, attrs);

        if (R.styleable.ContactListItemView == null) {
            return;
        }
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ContactListItemView);
        /*mPaddingRight = a.getDimensionPixelSize(
                R.styleable.ContactListItemView_list_item_padding_right, 0);
        mPaddingLeft = a.getDimensionPixelSize(
                R.styleable.ContactListItemView_list_item_padding_left, 0);*/
        mPaddingLeft = (int) getResources().getDimension(R.dimen.btalk_list_item_contact_padding_left);
        mPaddingRight = mPaddingLeft;
        int backgroundColor = a.getColor(
                R.styleable.ContactListItemView_list_item_background_color, Color.WHITE);
        a.recycle();
        mHeaderWidth =
                getResources().getDimensionPixelSize(R.dimen.contact_list_section_header_width);

        mViewDividerSectionBackgroundColor = getResources().getColor(R.color.btalk_color_list_common_divider);
        setViewDividerSection();
        setTextViewContactListPinnedHeader();
        //AnhNDd: Không dùng màu mặc định.
        setBackgroundColor(ContextCompat.getColor(getContext(),R.color.btalk_actionbar_and_tabbar_bg_color));
//        setBackgroundColor(ContextCompat.getColor(getContext(),R.color.btalk_white_opacity_bg));
//        R.color.btalk_transparent_view
        setBackgroundColor(Color.TRANSPARENT); // TrungTH tam thoi an view nay di
        //AnhNDd: Vì trước khoảng cách quá rộng, nên giảm đi một nửa.
        //mPaddingRight /= 2;
        setPaddingRelative(mPaddingLeft, 0, mPaddingRight, 0);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int specWidth = resolveSize(0, widthMeasureSpec);
        int height;
        mTextViewContactListPinnedHeader.measure(
                MeasureSpec.makeMeasureSpec(mHeaderWidth, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        mViewDividerSection.measure(
                MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mViewDividerSectionHeight, MeasureSpec.EXACTLY));
        height = 0/*mTextViewContactListPinnedHeader.getMeasuredHeight() + mViewDividerSection.getMeasuredHeight()*/; // TODO TRUNGTH tam thoi set ve 0

        setMeasuredDimension(specWidth, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = right - left;

        // Determine the vertical bounds by laying out the header first.
        int topBound = 0;
        int leftBound = mPaddingLeft;

        int headerHeight = mTextViewContactListPinnedHeader.getMeasuredHeight();
        mTextViewContactListPinnedHeader.layout(leftBound,
                topBound,
                leftBound + mTextViewContactListPinnedHeader.getMeasuredWidth(),
                headerHeight);

        mViewDividerSection.layout(leftBound,
                headerHeight,
                width - mPaddingRight,
                headerHeight + mViewDividerSectionHeight);

    }

    /**
     * AnhNDd: add textview pinned trong contact.
     */
    public void setTextViewContactListPinnedHeader() {
        if (mTextViewContactListPinnedHeader == null) {
            mTextViewContactListPinnedHeader = new TextView(getContext());
            mTextViewContactListPinnedHeader.setTextAppearance(getContext(), R.style.BtalkSectionHeaderStyle);
            mTextViewContactListPinnedHeader.setGravity(
                    ViewUtil.isViewLayoutRtl(this) ? Gravity.RIGHT : Gravity.LEFT);
            addView(mTextViewContactListPinnedHeader);
        }
    }

    /**
     * AnhNDd: add view đường gạch ngang trong contact
     */
    public void setViewDividerSection() {
        if (mViewDividerSection == null) {
            mViewDividerSection = new View(getContext());
            mViewDividerSection.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.btalk_line_divider_color));
            addView(mViewDividerSection);
            mViewDividerSection.setVisibility(VISIBLE);
        }
        mViewDividerSection.setVisibility(GONE);
    }

    /**
     * Sets section header or makes it invisible if the title is null.
     */
    public void setSectionHeaderTitle(String title) {
//        if (!TextUtils.isEmpty(title)) {
//            mTextViewContactListPinnedHeader.setResponse(title);
//            setVisibility(View.VISIBLE);
//        } else {
//            setVisibility(View.GONE);
//        }
        // TrungTh tam thoi de gone do anh hoang chot
        setVisibility(View.GONE);
    }
}
