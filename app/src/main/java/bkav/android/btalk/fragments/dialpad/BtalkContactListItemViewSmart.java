package bkav.android.btalk.fragments.dialpad;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.common.widget.CheckableQuickContactBadge;

import bkav.android.btalk.R;

/**
 * Created by anhdt on 18/05/2017.
 * custom lai listview contact tren giao dien smart search {@link bkav.android.btalk.fragments.BtalkPhoneFragment}
 */

public class BtalkContactListItemViewSmart extends ContactListItemView {
    public BtalkContactListItemViewSmart(Context context) {
        super(context);
    }

    public BtalkContactListItemViewSmart(Context context, AttributeSet attrs, boolean supportVideoCallIcon) {
        super(context, attrs, supportVideoCallIcon);
    }

    public BtalkContactListItemViewSmart(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPreferredHeight = context.getResources().getDimensionPixelSize(R.dimen.btalk_list_item_view_height);
        mDefaultPhotoViewSize = context.getResources().getDimensionPixelSize(R.dimen.btalk_list_item_photo_size);
    }

    @Override
    public void hookToSetPaddingRelative(int start, int top, int end, int bottom) {
        top = getContext().getResources().getDimensionPixelOffset(R.dimen.btalk_list_item_padding_top);
        bottom = getContext().getResources().getDimensionPixelOffset(R.dimen.btalk_list_item_padding_bottom);
        start = getContext().getResources().getDimensionPixelOffset(R.dimen.btalk_list_item_smart_padding_start);
        setPaddingRelative(start, top, end, bottom);
    }

    public void updateTextColor(boolean iskeyBoardShow) {
        if (mNameTextView != null) {
            mNameTextView.setTextColor(ContextCompat.getColor(getContext(), iskeyBoardShow ? R.color.btalk_ab_text_and_icon_normal_color : R.color.btalk_black_bg));
        }
    }

    /**
     * Anhdts custom font text
     */
    @Override
    public void setTypefaceView(TextView view) {
        view.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
    }

    /**
     * Anhdts custom text size
     */
    @Override
    public void setTextSizeView(TextView view) {
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_name_suggest));
    }

    @Override
    protected CheckableQuickContactBadge newCheckableQuickContactBadge(Context context) {
        return new BtalkCheckableQuickContactBadge(context);
    }

    @Override
    public void showDisplayName(Cursor cursor, int nameColumnIndex, int displayOrder) {
        CharSequence name = cursor.getString(nameColumnIndex);
        if (name.toString().startsWith("?DATE:")) {
            hideDisplayName();
            return;
        }
        setDisplayName(name);

        // Since the quick contact content description is derived from the display name and there is
        // no guarantee that when the quick contact is initialized the display name is already set,
        // do it here too.
        if (mQuickContact != null) {
            mQuickContact.setContentDescription(getContext().getString(
                    R.string.description_quick_contact_for, mNameTextView.getText()));
        }
    }
}
