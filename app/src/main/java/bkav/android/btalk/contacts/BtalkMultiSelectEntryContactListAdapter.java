package bkav.android.btalk.contacts;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;

import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.common.widget.CheckableImageView;
import com.android.contacts.list.MultiSelectEntryContactListAdapter;

import bkav.android.btalk.R;


/**
 * AnhNDd: kế thừa lại MultiSelectEntryContactListAdapter để thực hiện custom view cho từng
 * item theo ý mình.
 */
public class BtalkMultiSelectEntryContactListAdapter extends MultiSelectEntryContactListAdapter implements BtalkContactListItemView.ContactListItemListener {

    public interface OnBtalkContactListItemViewListener {
        void showContactAction(Uri contactLookupUri, boolean isEnterpriseContact);

        // Bkav HuyNQN them truong hop hien thi dialog cho contact co nhieu so dien thoai
        default void showDialog(String number, String action) {

        }
        //Bkav QuangNDb action call chuyen ra fragment de xu ly cho de
        default void actionCall(String number) {

        }
    }

    private OnBtalkContactListItemViewListener mListener;

    public OnBtalkContactListItemViewListener getListener() {
        return mListener;
    }

    public void setListener(OnBtalkContactListItemViewListener mListener) {
        this.mListener = mListener;
    }

    public interface IOnClickMessageButton{
        void onClickMessageButton();
    }

    public IOnClickMessageButton mOnClickMessageButton;
    /**
     * HienDTk:su kien bam vao icon_message cho an view search ben tab danh ba
     */
    public void setOnclickMessageButton(IOnClickMessageButton onclickMessageButton){
        mOnClickMessageButton = onclickMessageButton;
    }
    /**
     * AnhNDd: vị trí hiển thị của contact user
     */
    private int mPositionUserProfile = -1;

    private boolean mIsShowPhoneNumber;

    public BtalkMultiSelectEntryContactListAdapter(Context context) {
        super(context);
    }

    // Anhdts cai dat co hien so dien thoai trong danh ba khong
    public void setShowPhoneNumber(boolean isShowPhoneNumber) {
        if (mIsShowPhoneNumber != isShowPhoneNumber) {
            mIsShowPhoneNumber = isShowPhoneNumber;
        }
    }

    @Override
    public ContactListItemView createNewView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        if (BtalkContactsActivity.USE_BTALK) {
            final BtalkContactListItemView view = new BtalkContactListItemView(context, null);
            view.setIsSectionHeaderEnabled(isSectionHeaderDisplayEnabled());
            view.setAdjustSelectionBoundsEnabled(isAdjustSelectionBoundsEnabled());
            view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.btalk_white_bg)); // Bkav TrungTH doi viec xet nen len luc khoi tao
            return view;
        } else {
            return super.createNewView(context, partition, cursor, position, parent);
        }
    }

    @Override
    protected void bindView(View itemView, int partition, Cursor cursor, final int position) {
        //AnhNDd: thực hiện bind thêm những view custom.
        final BtalkContactListItemView btalkContactListItemView = (BtalkContactListItemView) itemView;
        //AnhNDd: sự kiện bấm vào phím mũi tên để xem thông tin contact.
        btalkContactListItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showContactAction(getContactUri(position), isEnterpriseContact(position));
            }
        });
        //HienDTk: su kien bam vao icon message de hien thi cuoc hoi thoai
       ((BtalkContactListItemView)itemView).setOnClickMessageButton(new BtalkContactListItemView.IOnClickMessageButton() {
           @Override
           public void onClick() {
                mOnClickMessageButton.onClickMessageButton();
           }
       });

        super.bindView(itemView, partition, cursor, position);

        btalkContactListItemView.setLookupUri(getContactUri(position), mIsShowPhoneNumber, cursor);
    }

    @Override
    protected void buttonClickWithMultiNumber(View itemView) {
        final BtalkContactListItemView btalkContactListItemView = (BtalkContactListItemView) itemView;
        btalkContactListItemView.setItemListener(this);
    }

    @Override
    public void hookToSetOnClickPhotoView(View itemView, final int position) {
        final BtalkContactListItemView btalkContactListItemView = (BtalkContactListItemView) itemView;
        //AnhNDd: sự kiện bấm vào hinh anh để xem thông tin contact.
        btalkContactListItemView.getPhotoView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showContactAction(getContactUri(position), isEnterpriseContact(position));
            }
        });

        btalkContactListItemView.getPhotoView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.showContactAction(getContactUri(position), isEnterpriseContact(position));
                return true;
            }
        });
    }

    @Override
    public void bindDividerSection(ContactListItemView view, int position) {
        final BtalkContactListItemView btalkContactListItemView = (BtalkContactListItemView) view;
        btalkContactListItemView.getViewDividerSection().setVisibility(/*placement.sectionHeader != null ? View.VISIBLE : View.GONE*/View.GONE);
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
    public void customSetProfileExists(Cursor cursor) {
        if (mSearchMode) {
            // Bkav TienNAb: Khi dang trong che do search thi khong set vi tri cho user profile
            mPositionUserProfile = -1;
            return;
        }
        if (cursor != null && cursor.moveToFirst()) {
            if (BtalkProfileAndContactsLoader.getCountStarredContacts() > 0 || BtalkProfileAndContactsLoader.getCountFrequentContacts() > 0) {
                //AnhNDd: Thay đổi lại ví trí con trỏ vì có query thêm các số điện thoại khác.
                cursor.move(BtalkProfileAndContactsLoader.getCountStarredContacts() + BtalkProfileAndContactsLoader.getCountFrequentContacts());
            }
            if (cursor.getInt(ContactQuery.CONTACT_IS_USER_PROFILE) == 1) {
                //set vi tri neu co user profile
                mPositionUserProfile = BtalkProfileAndContactsLoader.getCountStarredContacts() + BtalkProfileAndContactsLoader.getCountFrequentContacts();
            } else {
                mPositionUserProfile = -1;
            }
            setProfileExists(cursor.getInt(ContactQuery.CONTACT_IS_USER_PROFILE) == 1);
        }
    }

    @Override
    public void customSetIndexer(String[] sections, int[] counts) {
        setIndexer(new BtalkContactsSectionIndexer(sections, counts));
    }

    @Override
    public void setStarredContactsExists(Cursor cursor) {
        if (mSearchMode) {
            return;
        }
        //AnhNDd: Thực hiện kiểm tra xem có số điện thoại yêu thích hay không.
        if (cursor != null && BtalkProfileAndContactsLoader.getCountStarredContacts() > 0) {
            SectionIndexer indexer = getIndexer();
            if (indexer != null) {
                ((BtalkContactsSectionIndexer) indexer).setTitleToHeader(
                        getContext().getString(R.string.btalk_contactsFavoritesLabel), BtalkProfileAndContactsLoader.getCountStarredContacts());
            }
        }
    }

    @Override
    public void setFrequentContactsExists(Cursor cursor) {
        // Anhdts bo thuong xuyen lien he
//        //AnhNDd: Thực hiện kiểm tra xem có số điện thoại thường xuyên liên hệ hay không.
//        if (cursor != null && BtalkProfileAndContactsLoader.getCountFrequentContacts() > 0) {
//            SectionIndexer indexer = getIndexer();
//            if (indexer != null) {
//                ((BtalkContactsSectionIndexer) indexer).setTitleToHeader(
//                        getContext().getString(R.string.favoritesFrequentCalled), BtalkProfileAndContactsLoader.getCountFrequentContacts());
//            }
//        }
    }

    @Override
    public void bindCheckBoxORCheckImage(ContactListItemView view, long contactId) {

        if (!mDisplayCheckBoxes) {
            return;
        }
        //AnhNDd: Thực hiện ẩn checkbox và nếu là checkbox được click thì thay đổi ảnh và làm mờ item đó.
        if (view.getPhotoView() instanceof CheckableImageView) {
            CheckableImageView checkableImageView = (CheckableImageView) view.getPhotoView();
            checkableImageView.setChecked(mSelectedContactIds.contains(contactId), false);
            checkableImageView.setTag(contactId);
            //AnhNDd: xử lại của checkbox do cơ chế là giôngs nhau.
            //checkableImageView.setOnClickListener(mCheckableImageListener);

            //AnhNDd: Thay đổi màu nền background itemview.
            BtalkContactListItemView itemview = (BtalkContactListItemView) view;
            itemview.setChecked(mSelectedContactIds.contains(contactId));
            itemview.setClickable(false);
        }
    }


    /**
     * AnhNDd: Lằng nghe sự kiện thay đổi khi selected item.
     */
    /*private final View.OnClickListener mCheckableImageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final CheckableImageView checkableImageView = (CheckableImageView) v;
            final Long contactId = (Long) checkableImageView.getTag();
            if (checkableImageView.isChecked()) {
                mSelectedContactIds.add(contactId);
            } else {
                mSelectedContactIds.remove(contactId);
            }
            if (mSelectedContactsListener != null) {
                mSelectedContactsListener.onSelectedContactsChangedViaCheckBox();
            }
        }
    };*/
    @Override
    public void hideCheckableImage(ContactListItemView view) {
        BtalkContactListItemView itemview = (BtalkContactListItemView) view;
        itemview.setClickable(!mDisplayCheckBoxes);
        itemview.setChecked(false);

        CheckableImageView checkableImageView = (CheckableImageView) view.getPhotoView();
        checkableImageView.setChecked(false, false);
        checkableImageView.setTag(null);
        //checkableImageView.setOnClickListener(null);
    }

    @Override
    public Uri appendUriBuildSectionIndexer(Uri uri) {
        //AnhNDd: thực hiện thêm opption cho uri.
        return buildSectionIndexerUri(uri);
    }

    @Override
    public void setUpToQuerySTREQUENT(CursorLoader loader, String query, long directoryId) {
        if (loader instanceof BtalkProfileAndContactsLoader) {
            BtalkProfileAndContactsLoader btalkProfileAndContactsLoader = (BtalkProfileAndContactsLoader) loader;
            btalkProfileAndContactsLoader.setUpToSearch(query, directoryId, mSearchMode);
        }
    }

    public int getPositionUserContact() {
        return mPositionUserProfile;
    }

    @Override
    public boolean isPositionProfile(int position) {
        return position == mPositionUserProfile;
    }

    @Override
    public void showDialog(String number, String action) {
        if(mListener != null){
            mListener.showDialog(number,action);
        }
    }

    @Override
    public void actionCall(String number) {
        if(mListener != null){
            mListener.actionCall(number);
        }
    }
}
