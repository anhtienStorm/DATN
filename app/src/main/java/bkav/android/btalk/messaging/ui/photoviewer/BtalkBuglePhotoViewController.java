package bkav.android.btalk.messaging.ui.photoviewer;

import com.android.messaging.ui.photoviewer.BuglePhotoViewController;

import bkav.android.btalk.R;

/**
 * Created by trungth on 06/05/2017.
 */

public class BtalkBuglePhotoViewController extends BuglePhotoViewController {
    public BtalkBuglePhotoViewController(ActivityInterface activity) {
        super(activity);
    }

    @Override
    protected int initMenuId() {
        return R.menu.btalk_photo_view_menu;
    }
}
