package bkav.android.btalk.contacts;

import android.content.Context;
import android.view.View;
import android.widget.PopupMenu;

public class BtalkPopupMenu extends PopupMenu {
    public BtalkPopupMenu(Context context, View anchor) {
        super(context, anchor);
    }

    public BtalkPopupMenu(Context context, View anchor, int gravity) {
        super(context, anchor, gravity);
    }

    public BtalkPopupMenu(Context context, View anchor, int gravity, int popupStyleAttr, int popupStyleRes) {
        super(context, anchor, gravity, popupStyleAttr, popupStyleRes);
    }


}
