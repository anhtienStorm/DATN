package bkav.android.btalk.messaging.ui.contacts.common.list;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;

import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.common.list.PhoneNumberListAdapter;
import com.android.ex.chips.RecipientEntry;
import com.android.messaging.util.ContactUtil;

import java.util.ArrayList;

import bkav.android.btalk.R;
import bkav.android.btalk.contacts.BtalkContactListItemView;
import bkav.android.btalk.contacts.BtalkContactListPinnedHeaderView;
import bkav.android.btalk.contacts.BtalkContactsSectionIndexer;

/**
 * Created by quangnd on 20/04/2017.
 * adapter dung trong class BtalkContactPhoneNumberListFragment
 */

public class BtalkContactPhoneNumberListAdapter extends PhoneNumberListAdapter {

    public BtalkContactPhoneNumberListAdapter(Context context) {
        super(context);
        mListException = new ArrayList<Long>();
    }

    @Override
    public ContactListItemView getContactListItemView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        ContactListItemView view = new BtalkContactListItemView(context, null);
        view.setIsSectionHeaderEnabled(isSectionHeaderDisplayEnabled());
        view.setAdjustSelectionBoundsEnabled(isAdjustSelectionBoundsEnabled());
        return view;
    }

    @Override
    protected ContactListItemView getContactListItemView(View itemView) {
        return (BtalkContactListItemView) itemView;
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
    protected void addDividerSection(ContactListItemView view) {
        view.bindViewDividerSection();
    }

    @Override
    protected void invalidFrequentAndFavorite(CursorLoader loader, String query, long directoryId) {
        if (loader instanceof BtalkNumberAndContactLoader) {
            BtalkNumberAndContactLoader contactLoader = (BtalkNumberAndContactLoader) loader;
            contactLoader.setIsSearchMode(true);
            contactLoader.setQuery(query);
            contactLoader.setDirectoryId(directoryId);
        }
    }

    @Override
    public void changeCursor(int partitionIndex, Cursor cursor) {
        super.changeCursor(partitionIndex, cursor);
        //Bkav QuangNDb comment title frequent vs favorite lai
//        setStarredContactsExists(cursor);
//        setFrequentContactsExists(cursor);
    }

    /**Bkav QuangNDb Hien thi title frequent neu co*/
    private void setFrequentContactsExists(Cursor cursor) {
        if (cursor != null && BtalkNumberAndContactLoader.getCountFrequentContacts() > 0) {
            SectionIndexer indexer = getIndexer();
            if (indexer != null) {
                ((BtalkContactsSectionIndexer) indexer).setTitleToHeader(
                        getContext().getString(R.string.favoritesFrequentCalled),
                        BtalkNumberAndContactLoader.getCountFrequentContacts());
            }
        }
    }

    /**Bkav QuangNDb hien thi title favorite neu co */
    private void setStarredContactsExists(Cursor cursor) {
        if (cursor != null && BtalkNumberAndContactLoader.getCountStarredContacts() > 0) {
            SectionIndexer indexer = getIndexer();
            if (indexer != null) {
                ((BtalkContactsSectionIndexer) indexer).setTitleToHeader(
                        getContext().getString(R.string.btalk_contactsFavoritesLabel),
                        BtalkNumberAndContactLoader.getCountStarredContacts());
            }
        }
    }

    @Override
    public void customSetIndexer(String[] sections, int[] counts) {
        setIndexer(new BtalkContactsSectionIndexer(sections, counts));
    }

    /**
     * Bkav QuangNDb ham get Recipent cho auto completextView
     */
    public Object getRecipientEntry(int position) {
        final Cursor item = (Cursor) getItem(position);
        RecipientEntry entry = null;
        if (item != null) {
            int destinationType = item.getInt(PhoneQuery.PHONE_TYPE);
            String destinationLabel = item.getString(PhoneQuery.PHONE_LABEL);
            long contactId = item.getLong(PhoneQuery.CONTACT_ID);
            String photoThumbnailUri = item.getString(PhoneQuery.PHOTO_URI);
            boolean isFirstLevel = true;
            if (!item.isFirst() && item.moveToPrevious()) {
                final long contactIdPrevious = item.getLong(PhoneQuery.CONTACT_ID);
                if (contactId == contactIdPrevious) {
                    isFirstLevel = false;
                }
                item.moveToNext();
            }
            entry = ContactUtil.createRecipientEntry(getContactDisplayName(position),
                    ContactsContract.DisplayNameSources.STRUCTURED_NAME, getPhoneNumber(position), destinationType, destinationLabel,
                    contactId, getLookupKey(position), getItemId(position), photoThumbnailUri, isFirstLevel);
        }
        return entry;
    }

    @Override
    public void bindDividerSection(ContactListItemView view, int position) {
        final BtalkContactListItemView btalkContactListItemView = (BtalkContactListItemView) view;
        Placement placement = getItemPlacementInSection(position);
        btalkContactListItemView.getViewDividerSection().setVisibility(placement.sectionHeader != null ? View.VISIBLE : View.GONE);
    }

//    @Override
//    protected Uri getBaseSearchUri() {
//        return ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
//    }

//    @Override
//    protected void setUpSearchBuilder(Uri.Builder builder, String query, long directoryId, boolean isRemoteDirectoryQuery) {
//        super.setUpSearchBuilder(builder,query,directoryId,isRemoteDirectoryQuery);
//        builder.appendQueryParameter(
//                ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(ContactsContract.Directory.DEFAULT));
//        builder.appendQueryParameter(ContactsContract.CommonDataKinds.Phone.EXTRA_ADDRESS_BOOK_INDEX, "true");
//    }

//    @Override
//    protected String getSelectionIfSearchMode(String query) {
//        return " AND (UPPER(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+") LIKE UPPER('%"+query+"%') " +
//                "OR UPPER("+ ContactsContract.CommonDataKinds.Phone.NUMBER+") LIKE UPPER('%"+query+"%'))";
//    }

    @Override
    protected boolean getCheckFirstEntry(int position) {
        Placement placement = getItemPlacementInSection(position);
        return placement.firstInSection;
    }

    @Override
    protected void bindPhoto(final ContactListItemView view, int partitionIndex, Cursor cursor) {
        super.bindPhoto(view, partitionIndex, cursor);
        if (mIsConvertPhoneMode) {
            if (!mListException.contains(cursor.getLong(PhoneQuery.PHONE_ID))) {
                view.setChecked(true, false);
            } else {
                view.setChecked(false, false);
            }
        }
    }

    private boolean mIsConvertPhoneMode = false;

    void setConvertPhoneMode() {
        mIsConvertPhoneMode = true;
    }

    private ArrayList<Long> mListException;

    public void onItemClick(long pos) {
        if (mListException.contains(pos)) {
            mListException.remove(pos);
        } else {
            mListException.add(pos);
        }
    }

    /**
     * Anhdts
     */
    // Bkav HienDTk: them textSearch de boi cam sdt
    @Override
    protected void setPhoneNumber(ContactListItemView view, String text, long dataId, String textSearch) {
        if (mIsConvertPhoneMode) {
            String phoneConvert = convertPhoneNumber(text, dataId);
            view.setPhoneNumber(phoneConvert, phoneConvert.indexOf(")") + 1);
        } else {
            view.setPhoneNumber(text, null, textSearch);
        }
    }

    private String convertPhoneNumber(String text, long dataId) {
        int headNumber;
        String formatNumber = text.replaceAll("-", "");
        formatNumber = formatNumber.replaceAll(" ", "");

        String headText;
        String headConvert;
        String detailConvert;
        if (formatNumber.startsWith("+84")) {
            headNumber = Integer.parseInt(formatNumber.substring(1, 6));
            headText = formatNumber.substring(0, 6);
            headConvert = "+84";
        } else if (formatNumber.startsWith("84")) {
            headNumber = Integer.parseInt(formatNumber.substring(0, 5));
            headText = formatNumber.substring(0, 5);
            headConvert = "84";
        } else {
            headNumber = Integer.parseInt(formatNumber.substring(1, 4));
            headText = formatNumber.substring(0, 4);
            headConvert = "0";
        }
        int pos = 0;
        for (char c : headText.toCharArray()) {
            if (c != text.charAt(pos)) {
                pos++;
                while (text.charAt(pos) != c) {
                    pos++;
                }
                pos++;
            } else {
                pos++;
            }
        }
        detailConvert = text.substring(pos);
        headNumber = getHeader(headNumber);

        return "(" + headText + " -> " + headConvert + headNumber + ") " + detailConvert;
    }

    static int getHeader(int headNumber) {
        switch (headNumber) {
            case 162:
            case 84162:
                headNumber = 32;
                break;
            case 163:
            case 84163:
                headNumber = 33;
                break;
            case 164:
            case 84164:
                headNumber = 34;
                break;
            case 165:
            case 84165:
                headNumber = 35;
                break;
            case 166:
            case 84166:
                headNumber = 36;
                break;
            case 167:
            case 84167:
                headNumber = 37;
                break;
            case 168:
            case 84168:
                headNumber = 38;
                break;
            case 169:
            case 84169:
                headNumber = 39;
                break;
            case 120:
            case 84120:
                headNumber = 70;
                break;
            case 121:
            case 84121:
                headNumber = 79;
                break;
            case 122:
            case 84122:
                headNumber = 77;
                break;
            case 126:
            case 84126:
                headNumber = 76;
                break;
            case 128:
            case 84128:
                headNumber = 78;
                break;
            case 123:
            case 84123:
                headNumber = 83;
                break;
            case 124:
            case 84124:
                headNumber = 84;
                break;
            case 125:
            case 84125:
                headNumber = 85;
                break;
            case 127:
            case 84127:
                headNumber = 81;
                break;
            case 129:
            case 84129:
                headNumber = 82;
                break;
            case 186:
            case 84186:
                headNumber = 56;
                break;
            case 188:
            case 84188:
                headNumber = 58;
                break;
            case 199:
            case 84199:
                headNumber = 59;
                break;
        }
        return headNumber;
    }

    int getCountChoose() {
        return mListException.size();
    }

    boolean isExceptionContact(long i) {
        return mListException.contains(i);
    }

}
