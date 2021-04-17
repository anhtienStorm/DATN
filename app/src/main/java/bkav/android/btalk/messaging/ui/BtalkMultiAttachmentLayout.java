package bkav.android.btalk.messaging.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.ui.MultiAttachmentLayout;

/**
 * Created by quangnd on 24/05/2017.
 */

public class BtalkMultiAttachmentLayout extends MultiAttachmentLayout {

    public BtalkMultiAttachmentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void animateViewIn(MessagePartData attachment, MultiAttachmentLayout.ViewWrapper attachmentWrapper) {
        BtalkAttachmentPreview.tryAnimateViewIn(attachment, attachmentWrapper.view);
    }
}
