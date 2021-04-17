package bkav.android.btalk.calllog;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageButton;
import android.widget.ImageView;

import bkav.android.btalk.R;
import bkav.android.btalk.contacts.BtalkContactListItemView;

/**
 * Created by anhdt on 04/12/2017.
 *
 */

public class BtalkContactSearchCallLogListItem extends BtalkContactListItemView {
    public BtalkContactSearchCallLogListItem(Context context) {
        super(context);
    }

    public BtalkContactSearchCallLogListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setClickable(boolean clickable) {
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
        getPhotoView().setClickable(true);
        getPhotoView().setLongClickable(true);
    }

    @Override
    public void setChecked(boolean bool) {
    }

    @Override
    public void setEnabled(boolean enabled) {
        getPhotoView().setEnabled(true);
        super.setEnabled(true);
    }

    @Override
    protected void setInvisibleButton(ImageButton imageButtonCall, ImageButton imageButtonMessage) {
        imageButtonCall.setVisibility(INVISIBLE);
        imageButtonMessage.setVisibility(INVISIBLE);
    }
}
