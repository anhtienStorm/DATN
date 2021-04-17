package bkav.android.btalk.contacts;


import android.content.Context;
import android.widget.Button;

import com.android.contacts.activities.PopupList;
import com.android.contacts.activities.SelectionMenu;

/**
 * AnhNDd: class dung de custom lai selection menu o bottom bar
 */
public class BtalkSelectionMenu extends SelectionMenu {

    private final BtalkPopupList mBtalkPopupList;

    public BtalkSelectionMenu(Context context, Button button, PopupList.OnPopupItemClickListener listener) {
        super(context, button, listener);

        //AnhNDd: thay doi cach tao popuplist
        mBtalkPopupList = new BtalkPopupList(context, mButton);
        mBtalkPopupList.setOnPopupItemClickListener(listener);
        mPopupList = mBtalkPopupList;
    }


}
