package bkav.android.btalk.messaging.datamodel.data;

import android.content.Context;
import android.net.Uri;

import com.android.messaging.datamodel.media.AvatarRequest;
import com.android.messaging.datamodel.media.AvatarRequestDescriptor;
import com.android.messaging.datamodel.media.ImageResource;
import com.android.messaging.datamodel.media.MediaRequest;
import com.android.messaging.util.AvatarUriUtil;

/**
 * Created by quangnd on 27/09/2017.
 */

public class BtalkAvatarRequestDescriptor extends AvatarRequestDescriptor {

    public BtalkAvatarRequestDescriptor(Uri uri, int desiredWidth, int desiredHeight) {
        super(uri, desiredWidth, desiredHeight);
    }

    @Override
    public MediaRequest<ImageResource> buildSyncMediaRequest(final Context context) {
        final String avatarType = uri == null ? null : AvatarUriUtil.getAvatarType(uri);
        if (AvatarUriUtil.TYPE_SIM_SELECTOR_URI.equals(avatarType)) {
            return new BtalkSimSelectorAvatarRequest(context, this);
        } else {
            return new AvatarRequest(context, this);
        }
    }
}
