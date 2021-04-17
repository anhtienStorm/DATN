package bkav.android.btalk.contacts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import bkav.android.btalk.R;

/**
 * AnhNDd: view chưa header pinner với fadeedge ở bên dưới
 */
public class BtalkContactListPinnedHeaderViewWithFadeEdge extends LinearLayout {

    private BtalkContactListPinnedHeaderView mBtalkContactListPinnedHeaderView;

    //AnhNDd: view de fadeedge
    private View mViewFadeEdge;
    private int mHeightViewFadeEdge;

    public BtalkContactListPinnedHeaderViewWithFadeEdge(Context context, AttributeSet attrs, View parent) {
        super(context, attrs);

        mBtalkContactListPinnedHeaderView = new BtalkContactListPinnedHeaderView(context, attrs, parent);
        mHeightViewFadeEdge = (int) getResources().getDimension(R.dimen.fadeEdge_length);
        addView(mBtalkContactListPinnedHeaderView);
        getViewFadeEdge();
        setBackgroundColor(Color.TRANSPARENT);
        setPaddingRelative(0, 0, 0, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int specWidth = resolveSize(0, widthMeasureSpec);
        mBtalkContactListPinnedHeaderView.measure(
                MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mBtalkContactListPinnedHeaderView.getHeight(), MeasureSpec.EXACTLY));

        mViewFadeEdge.measure(
                MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mHeightViewFadeEdge, MeasureSpec.EXACTLY));

        int height;
        height = mBtalkContactListPinnedHeaderView.getMeasuredHeight() + mHeightViewFadeEdge;
        setMeasuredDimension(specWidth, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = right - left;
        mBtalkContactListPinnedHeaderView.layout(0,
                0,
                width,
                mBtalkContactListPinnedHeaderView.getMeasuredHeight());

        mViewFadeEdge.layout(0,
                mBtalkContactListPinnedHeaderView.getMeasuredHeight(),
                width,
                mBtalkContactListPinnedHeaderView.getMeasuredHeight() + mHeightViewFadeEdge);
    }

    public void getViewFadeEdge() {
        if (mViewFadeEdge == null) {
            mViewFadeEdge = new View(getContext());
            GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[]{Color.TRANSPARENT, /*getResources().getColor(R.color.btalk_listview_bg_color)*/Color.WHITE});
            mViewFadeEdge.setBackgroundDrawable(gradient);
            addView(mViewFadeEdge);
            mViewFadeEdge.setVisibility(VISIBLE);
        }
    }

    /**
     * Sets section header or makes it invisible if the title is null.
     */
    public void setSectionHeaderTitle(String title) {
        mBtalkContactListPinnedHeaderView.setSectionHeaderTitle(title);
    }
}
