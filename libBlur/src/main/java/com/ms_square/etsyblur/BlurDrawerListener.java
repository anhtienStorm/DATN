package com.ms_square.etsyblur;

import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;

/**
 * BlurDrawerListener.java
 *
 */
class BlurDrawerListener implements DrawerLayout.DrawerListener {

    private final BlurringView blurringView;

    public BlurDrawerListener(BlurringView blurringView) {
        this.blurringView = blurringView;
        this.blurringView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        // Anhdts neu chua mo thi khong goi onDrawerClosed, nen cac check bang 0 thi setVisible
        if (slideOffset == 0) {
            onDrawerClosed(drawerView);
            return;
        } else if (blurringView.getVisibility() != View.VISIBLE) {
            blurringView.startBlur();
            blurringView.setVisibility(View.VISIBLE);
        }
        blurringView.setAlpha(slideOffset);
    }

    @Override
    public void onDrawerOpened(View drawerView) {
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        blurringView.setVisibility(View.GONE);
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }
}
