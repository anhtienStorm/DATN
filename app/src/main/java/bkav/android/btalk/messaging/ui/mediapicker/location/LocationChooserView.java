package bkav.android.btalk.messaging.ui.mediapicker.location;

import android.app.FragmentManager;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.android.messaging.datamodel.data.DraftMessageData;
import com.android.messaging.datamodel.data.MessagePartData;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 14/05/2017.
 * class xu ly cac su kien cua location attachment
 */
public class LocationChooserView extends LinearLayout implements MapViewFragment.OnMapConnected{

    private FragmentManager mFragmentManager;
    private static final String FRAGMENT_TAG = "LOCATION_PICKER_TAG";
    private Context mContext;
    private MapViewFragment mMapViewFragment;
//    private boolean mIsConnected = false;
    private MenuItem mSendMenu;

    public interface LocationChooserViewHost extends DraftMessageData.DraftMessageSubscriptionDataProvider {
        void onSendLocationClick(MessagePartData messagePartData);
    }

    private LocationChooserViewHost mHost;

    public void setHost(LocationChooserViewHost host) {
        this.mHost = host;
    }

    public LocationChooserView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        if (mFragmentManager != null) {
            mMapViewFragment = new MapViewFragment();
            mMapViewFragment.setHost(this);
            mFragmentManager.beginTransaction().replace(R.id.location_picker_container, mMapViewFragment, FRAGMENT_TAG).commit();
        }
    }

    public void onCreateOptionMenu(final MenuInflater inflater, final Menu menu) {
        inflater.inflate(R.menu.btalk_location_chooser_menu, menu);
        mSendMenu = menu.findItem(R.id.menu_send_location);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send_location:
                if (mHost != null) {
                    final MessagePartData linkMap = new MessagePartData(mMapViewFragment.getLinkMap());
                    mHost.onSendLocationClick(linkMap);
                }
                return true;
        }
        return false;
    }

    @Override
    public void onReadyConnected(boolean isConnect) {
//        mIsConnected = isConnect;
        if (mSendMenu != null) {
            mSendMenu.setVisible(isConnect);
        }
    }
}
