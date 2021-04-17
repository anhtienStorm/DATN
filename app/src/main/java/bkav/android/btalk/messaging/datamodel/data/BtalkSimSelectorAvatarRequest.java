package bkav.android.btalk.messaging.datamodel.data;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;

import com.android.messaging.datamodel.media.AvatarRequestDescriptor;
import com.android.messaging.datamodel.media.SimSelectorAvatarRequest;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 27/09/2017.
 */

public class BtalkSimSelectorAvatarRequest extends SimSelectorAvatarRequest {
    protected int mShapeBig;

    public BtalkSimSelectorAvatarRequest(Context context, AvatarRequestDescriptor descriptor) {
        super(context, descriptor);
        mShapeBig = context.getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size);
    }

    @Override
    protected int getSimTitleRatio(int width) {
        if (width < mShapeBig) {
            return R.dimen.sim_identifier_to_tile_ratio_small;
        }else {
            return super.getSimTitleRatio(width);
        }
    }

    @Override
    protected BitmapDrawable getRegularSim(int width) {
        if (width < mShapeBig) {
            return (BitmapDrawable) mContext.getResources()
                    .getDrawable(R.drawable.ic_sim_card_default);
        }else {
            return (BitmapDrawable) mContext.getResources()
                    .getDrawable(R.drawable.ic_sim_card_small);
        }
    }

}
