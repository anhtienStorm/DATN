package bkav.android.btalk.calllog;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.list.MultiSelectEntryContactListAdapter;

import bkav.android.btalk.contacts.BtalkContactListPinnedHeaderView;
import bkav.android.btalk.contacts.BtalkContactsSectionIndexer;

/**
 * Created by anhdt on 04/12/2017.
 *
 */

class BtalkCallLogSearchContactListAdapter extends MultiSelectEntryContactListAdapter {

    BtalkCallLogSearchContactListAdapter(Context context) {
        super(context);
    }

    interface OnSearchCallLogItemListener {
        void showContactAction(Uri contactLookupUri, boolean isEnterpriseContact);

        void sendMessageAction(Uri contactLookupUri, boolean isEnterpriseContact);
    }

    private OnSearchCallLogItemListener mListener;

    public OnSearchCallLogItemListener getListener() {
        return mListener;
    }

    public void setListener(OnSearchCallLogItemListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public ContactListItemView createNewView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        final BtalkContactSearchCallLogListItem view = new BtalkContactSearchCallLogListItem(context, null);
        view.setIsSectionHeaderEnabled(isSectionHeaderDisplayEnabled());
        view.setAdjustSelectionBoundsEnabled(isAdjustSelectionBoundsEnabled());
        return view;
    }

//    @Override
//    protected void bindView(View itemView, int partition, Cursor cursor, final int position) {
//        final BtalkContactSearchCallLogListItem BtalkContactSearchCallLogListItem = (BtalkContactSearchCallLogListItem) itemView;
//        BtalkContactSearchCallLogListItem.getContactViewDetails().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mListener.sendMessageAction(getContactUri(position), isEnterpriseContact(position));
//            }
//        });
//
//        BtalkContactSearchCallLogListItem.getContactViewDetails().setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                mListener.sendMessageAction(getContactUri(position), isEnterpriseContact(position));
//                return true;
//            }
//        });
//        super.bindView(itemView, partition, cursor, position);
//    }

    @Override
    public void hookToSetOnClickPhotoView(View itemView, final int position) {
        final BtalkContactSearchCallLogListItem btalkContactSearchCallLogListItem = (BtalkContactSearchCallLogListItem) itemView;
        btalkContactSearchCallLogListItem.getPhotoView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showContactAction(getContactUri(position), isEnterpriseContact(position));
            }
        });

        btalkContactSearchCallLogListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showContactAction(getContactUri(position), isEnterpriseContact(position));
            }
        });

        btalkContactSearchCallLogListItem.getPhotoView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.showContactAction(getContactUri(position), isEnterpriseContact(position));
                return true;
            }
        });
    }

    @Override
    public void bindDividerSection(ContactListItemView view, int position) {
        final BtalkContactSearchCallLogListItem BtalkContactSearchCallLogListItem = (BtalkContactSearchCallLogListItem) view;
        BtalkContactSearchCallLogListItem.getViewDividerSection().setVisibility(View.GONE);
    }

    @Override
    protected View createPinnedSectionHeaderView(Context context, ViewGroup parent) {
        return new BtalkContactListPinnedHeaderView(context, null, parent);
    }

    @Override
    protected void setPinnedSectionTitle(View pinnedHeaderView, String title) {
        BtalkContactListPinnedHeaderView view =
                (BtalkContactListPinnedHeaderView) pinnedHeaderView;
        view.setSectionHeaderTitle(title);
    }

    @Override
    public void customSetIndexer(String[] sections, int[] counts) {
        setIndexer(new BtalkContactsSectionIndexer(sections, counts));
    }

    @Override
    public Uri appendUriBuildSectionIndexer(Uri uri) {
        return buildSectionIndexerUri(uri);
    }

    @Override
    public void hideCheckableImage(ContactListItemView view) {
        BtalkContactSearchCallLogListItem itemView = (BtalkContactSearchCallLogListItem) view;
        itemView.setClickable(!mDisplayCheckBoxes);
    }
}
