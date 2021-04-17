package bkav.android.btalk.contacts;


import android.content.Context;
import android.view.View;

import com.android.contacts.activities.PopupList;

/**
 * AnhNDd: dùng để custom lại view của popuplist.
 */
public class BtalkPopupList extends PopupList {
    public BtalkPopupList(Context context, View anchorView) {
        super(context, anchorView);
    }

    @Override
    protected void updatePopupLayoutParams() {
        super.updatePopupLayoutParams();

        //AnhNDd: điều chỉnh lại vị trí hiển thị nếu là nằm dưới cùng của toolbar, khi toolbar ở phía dưới.
        /*PopupWindow popup = mPopupWindow;

        Rect p = new Rect();
        popup.getBackground().getPadding(p);

        mPopupOffsetY = mAnchorView.getHeight() + p.top;*/
    }
}
