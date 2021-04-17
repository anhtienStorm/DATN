package bkav.android.btalk.contacts;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.android.contacts.common.list.ContactTileAdapter;
import com.android.contacts.common.list.ContactTileView;
import com.android.contacts.common.logging.ScreenEvent;
import com.android.contacts.common.util.ImplicitIntentsUtil;

import bkav.android.btalk.R;

/**
 * AnhNDd: class kế thừa để xửa lại giao diện contact có ảnh và nametext
 */
class BtalkContactTileAdapter extends ContactTileAdapter {
    BtalkContactTileAdapter(Context context, ContactTileView.Listener listener, int numCols, DisplayType displayType) {
        super(context, listener, numCols, displayType);

        mWhitespaceStartEnd = mContext.getResources()
                .getDimensionPixelSize(R.dimen.btalk_list_item_contact_padding_left);
    }

    @Override
    public int getLayoutViewTypesFREQUENT() {
        return R.layout.btalk_contact_tile_frequent;
    }

    @Override
    protected TextView getDivider() {
        TextView divider = super.getDivider();
        divider.setTextAppearance(mContext, R.style.BtalkSectionHeaderStyle);
        return divider;
    }

    /**
     * Anhdts action hien thi danh ba
     */
    @Override
    protected void setActionShowDetail(final ContactTileView contactTile) {
        View viewShowDetail = contactTile.findViewById(R.id.contact_tile_show_detail);
        if (viewShowDetail != null) {
            viewShowDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDetailContact(contactTile);
                }
            });
        }
        contactTile.findViewById(R.id.contact_tile_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetailContact(contactTile);
            }
        });
    }

    private void showDetailContact(ContactTileView contactTile) {
        final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(contactTile.getLookupUri(),
                BtalkQuickContactActivity.MODE_FULLY_EXPANDED);
        intent.putExtra(BtalkQuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE, ScreenEvent.ScreenType.FAVORITES);
        ImplicitIntentsUtil.startActivityInApp(mContext, intent);
    }
}
