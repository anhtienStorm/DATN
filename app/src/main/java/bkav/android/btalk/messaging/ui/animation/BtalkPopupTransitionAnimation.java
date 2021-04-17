package bkav.android.btalk.messaging.ui.animation;

import android.graphics.Rect;
import android.view.View;

import com.android.messaging.ui.animation.PopupTransitionAnimation;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 24/05/2017.
 */

public class BtalkPopupTransitionAnimation extends PopupTransitionAnimation {

    public BtalkPopupTransitionAnimation(Rect startRect, View viewToAnimate) {
        super(startRect, viewToAnimate);
    }

    @Override
    protected View getActionBarView(View viewToAnimate) {
        return viewToAnimate.getRootView().findViewById(R.id.toolbar);
    }

}
