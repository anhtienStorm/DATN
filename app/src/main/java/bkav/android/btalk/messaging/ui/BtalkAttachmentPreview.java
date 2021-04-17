package bkav.android.btalk.messaging.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.android.messaging.datamodel.data.MediaPickerMessagePartData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.ui.AttachmentPreview;

import bkav.android.btalk.messaging.ui.animation.BtalkPopupTransitionAnimation;

/**
 * Created by quangnd on 24/05/2017.
 */

public class BtalkAttachmentPreview extends AttachmentPreview {

    public BtalkAttachmentPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void animateViewIn(MessagePartData attachment, View attachmentView) {
        tryBtalkAnimateViewIn(attachment, attachmentView);
    }

    static void tryBtalkAnimateViewIn(final MessagePartData attachmentData, final View view) {
        if (attachmentData instanceof MediaPickerMessagePartData) {
            final Rect startRect = ((MediaPickerMessagePartData) attachmentData).getStartRect();
            new BtalkPopupTransitionAnimation(startRect, view).startAfterLayoutComplete();
        }
    }
}
