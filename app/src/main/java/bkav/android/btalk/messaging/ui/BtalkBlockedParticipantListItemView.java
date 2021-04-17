package bkav.android.btalk.messaging.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.android.messaging.datamodel.data.ParticipantListItemData;
import com.android.messaging.ui.BlockedParticipantListItemView;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 26/05/2017.
 */

public class BtalkBlockedParticipantListItemView extends BlockedParticipantListItemView {

    private BtalkContactPhotoView mBtalkContactPhotoView;

    public BtalkBlockedParticipantListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initContactIconView() {
        mBtalkContactPhotoView = (BtalkContactPhotoView) findViewById(R.id.contact_icon);
    }

    @Override
    protected void setContactIconUri(ParticipantListItemData data) {
        mBtalkContactPhotoView.setImageResourceUriFromParticipant(data.getAvatarUri(), data.getContactId(),
                data.getLookupKey(), data.getNormalizedDestination(), data.getId(), data.getDisplayName());

    }
}
