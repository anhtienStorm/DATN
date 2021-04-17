package bkav.android.btalk.contacts;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;

import static com.android.common.util.DeviceVersionUtil.isBL01Device;

public class ContactHeaderBehaviour extends CoordinatorLayout.Behavior<ContactHeaderView> {

    private Context mContext;

    private int mStartMarginRight;
    private int mEndMarginRight;
    private int mStartMarginBottom;
    private boolean isHide;

    private ImageView mImageBackgroundExpandLayout;
    private ImageButton mFloatingActionButton;
    private ImageButton mFloatingActionButtonSmall;

    public ContactHeaderBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public ContactHeaderBehaviour(Context context, AttributeSet attrs, Context mContext) {
        super(context, attrs);
        this.mContext = mContext;
    }

    public int getToolbarHeight(Context context) {
        int result = 0;
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            result = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return result;
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull ContactHeaderView child, @NonNull View dependency) {
        return dependency instanceof AppBarLayout;
    }

    // Bkav TienNAb: update header view khi scroll appbarlayout
    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull ContactHeaderView child, @NonNull View dependency) {
        shouldInitProperties();
        initView(parent,child);

        int maxScroll = ((AppBarLayout) dependency).getTotalScrollRange();
        float percentage = Math.abs(dependency.getY()) / (float) maxScroll;
        float childPosition = dependency.getHeight()
                + dependency.getY()
                - child.getHeight()
                - (getToolbarHeight(mContext) - child.getHeight()) * percentage / 2;

        childPosition = childPosition - mStartMarginBottom * (1f - percentage);

        float imageAlpha = 1 - (Math.abs(dependency.getY()) / (float) maxScroll);

        // Bkav TienNAb: hieu ung fly cua icon them lien he moi va icon yeu thich
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.rightMargin = (int) (mEndMarginRight + imageAlpha * (mStartMarginRight - mEndMarginRight));

        // Bkav TienNAb: hieu ung an/hien cua icon contact lon
        mImageBackgroundExpandLayout.setAlpha(imageAlpha);

        // Bkav TienNAb: hieu ung an/hien cua button search o phia duoi
        mFloatingActionButton.setAlpha(1 - imageAlpha);
        mFloatingActionButton.setScaleX(1 - imageAlpha);
        mFloatingActionButton.setScaleY(1 - imageAlpha);

        // Bkav TienNAb: hieu ung fly cua button search o phia tren
        RelativeLayout.LayoutParams floatingActionButtonSmallParams = (RelativeLayout.LayoutParams) mFloatingActionButtonSmall.getLayoutParams();
        int bottomMarginfloatingActionButtonSmall;

        // Bkav HaiKH - Fix bug BOS-3201- Start
        // Sửa vị trí hiển thị của floatting button ở máy BL01
        if(isBL01Device()){
            if (BtalkActivity.hasNavigationBar()){
                bottomMarginfloatingActionButtonSmall = (int) (mContext.getResources().getDimensionPixelOffset(R.dimen.floating_action_button_small_margin_bottom_end_has_navigation_bar_bl01)
                        - imageAlpha * (mContext.getResources().getDimensionPixelOffset(R.dimen.floating_action_button_small_margin_bottom_end_has_navigation_bar_bl01)
                        - mContext.getResources().getDimensionPixelOffset(R.dimen.floating_action_button_small_margin_bottom_start_has_navigation_bar_bl01)));
            } else {
                bottomMarginfloatingActionButtonSmall = (int) (mContext.getResources().getDimensionPixelOffset(R.dimen.floating_action_button_small_margin_bottom_end_bl01)
                        - imageAlpha * (mContext.getResources().getDimensionPixelOffset(R.dimen.floating_action_button_small_margin_bottom_end_bl01)
                        - mContext.getResources().getDimensionPixelOffset(R.dimen.floating_action_button_small_margin_bottom_start_bl01)));
            }
            // Bkav HaiKH - Fix bug BOS-3201- End
        }else {
            // Bkav TienNAb: sua lai vi tri hien thi cua floating button trong cac truong hop
            if (BtalkActivity.hasNavigationBar()){
                bottomMarginfloatingActionButtonSmall = (int) (mContext.getResources().getDimensionPixelOffset(R.dimen.floating_action_button_small_margin_bottom_end_has_navigation_bar)
                        - imageAlpha * (mContext.getResources().getDimensionPixelOffset(R.dimen.floating_action_button_small_margin_bottom_end_has_navigation_bar)
                        - mContext.getResources().getDimensionPixelOffset(R.dimen.floating_action_button_small_margin_bottom_start_has_navigation_bar)));
            } else {
                bottomMarginfloatingActionButtonSmall = (int) (mContext.getResources().getDimensionPixelOffset(R.dimen.floating_action_button_small_margin_bottom_end)
                        - imageAlpha * (mContext.getResources().getDimensionPixelOffset(R.dimen.floating_action_button_small_margin_bottom_end)
                        - mContext.getResources().getDimensionPixelOffset(R.dimen.floating_action_button_small_margin_bottom_start)));
            }
        }

        floatingActionButtonSmallParams.bottomMargin = bottomMarginfloatingActionButtonSmall;
        mFloatingActionButtonSmall.setLayoutParams(floatingActionButtonSmallParams);
        mFloatingActionButtonSmall.setAlpha(imageAlpha);
        mFloatingActionButtonSmall.setScaleX(imageAlpha);
        mFloatingActionButtonSmall.setScaleY(imageAlpha);


        child.setLayoutParams(lp);
        child.setY(childPosition);

        if (isHide && percentage < 1) {
            child.setVisibility(View.VISIBLE);
            isHide = false;
        } else if (!isHide && percentage == 1) {
            child.setVisibility(View.GONE);
            isHide = true;
        }
        return true;
    }

    private void initView(@NonNull CoordinatorLayout parent, @NonNull ContactHeaderView child){
        mImageBackgroundExpandLayout = parent.findViewById(R.id.img_background_expand_layout);
        mFloatingActionButton = parent.findViewById(R.id.floating_action_button);
        mFloatingActionButtonSmall = parent.findViewById(R.id.floating_action_button_small);
    }

    // Bkav TienNAb: gan cac gia tri cho layout header view
    private void shouldInitProperties() {
        if (mStartMarginBottom == 0) {
            mStartMarginBottom = mContext.getResources().getDimensionPixelOffset(R.dimen.contact_header_view_start_margin_bottom);
        }

        if (mStartMarginRight == 0) {
            mStartMarginRight = mContext.getResources().getDimensionPixelOffset(R.dimen.contact_header_view_start_margin_right);
        }

        if (mEndMarginRight == 0){
            mEndMarginRight = mContext.getResources().getDimensionPixelOffset(R.dimen.contact_header_view_end_margin_right);
        }
    }


}
