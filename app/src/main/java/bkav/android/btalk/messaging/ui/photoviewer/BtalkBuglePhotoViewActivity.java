package bkav.android.btalk.messaging.ui.photoviewer;

import com.android.ex.photo.PhotoViewController;
import com.android.messaging.ui.photoviewer.BuglePhotoViewActivity;

/**
 * Created by trungth on 06/05/2017.
 */

public class BtalkBuglePhotoViewActivity extends BuglePhotoViewActivity {
    @Override
    public PhotoViewController createController() {
        return new BtalkBuglePhotoViewController(this);
    }
}
