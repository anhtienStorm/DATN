package bkav.android.btalk.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import bkav.android.btalk.R;

/**
 * Created by anhdt on 29/06/2017.
 */

public class BtalkViewAnchorNav extends View implements DrawerLayout.DrawerListener {

    public BtalkViewAnchorNav(Context context) {
        super(context);
    }

    public BtalkViewAnchorNav(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BtalkViewAnchorNav(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        if (slideOffset == 0) {
            onDrawerClosed(drawerView);
            return;
        } else if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }
        setAlpha(slideOffset);
        if (((DrawerLayout.LayoutParams) drawerView.getLayoutParams()).gravity == Gravity.START) {
            setTranslationX(slideOffset * getResources().getDimensionPixelOffset(R.dimen.btalk_width_navigation_view));
        } else {
            setTranslationX(-slideOffset * getResources().getDimensionPixelOffset(R.dimen.btalk_width_navigation_view));
        }
    }

    @Override
    public void onDrawerOpened(View drawerView) {
    }

    @Override
    public void onDrawerClosed(View drawerView) {

        setAlpha(0);
        setVisibility(View.GONE);
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }
}
