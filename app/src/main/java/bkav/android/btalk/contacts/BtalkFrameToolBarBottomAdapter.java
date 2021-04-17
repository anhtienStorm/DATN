package bkav.android.btalk.contacts;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.android.contacts.activities.PopupList;
import com.android.contacts.activities.SelectionMenu;

import java.lang.ref.WeakReference;

import bkav.android.btalk.R;
import bkav.android.btalk.utility.BtalkUiUtils;

/**
 * AnhNDd: class adapter chứa view giống action bar nắm ở phía dưới khi lựa chọn
 * 1 hoặc nhiều contact, xử lý các sự kiện trên menu đó.
 */
public class BtalkFrameToolBarBottomAdapter implements View.OnClickListener {


    //AnhNDd: listener cho viec select contact
    public interface SelectionContactListener {
        //AnhNDd: có một số action chưa sử dụng.
        abstract class Action {
            public static final int START_SELECTION_MODE = 2;

            public static final int STOP_SELECTION_MODE = 3;

            public static final int BEGIN_STOPPING_SEARCH_AND_SELECTION_MODE = 4;
        }

        void onActionSeclection(int action);

        void onPopupItemClick(boolean selectAll);

        //AnhNDd: thông báo xóa các số điện thoại đã chọn
        void deleteSelectedContacts();

        //AnhNDd: thông báo chia sẻ các số điện thoại
        void shareSelectedContacts();

        //AnhNDd: thông báo edit contact đã trọn.
        void editSelectedContact();

        //AnhNDd: Thông báo gửi tin nhắn
        void sendMessageSelectedContact();
    }

    private SelectionContactListener mSelectionContactListener;

    private Context mContext;

    //AnhNDd: frame chứa giao diện.
    private FrameLayout mFrameToolBarBottom;

    //AnhNDd: view mặc định khi lựa chọn 1 contact
    private View mSelectionContainer;

    private boolean mSelectionMode = false;

    // build action bar with a spinner
    private BtalkSelectionMenu mSelectionMenu;

    private PopupListListener mPopupListListener;

    private int mSelectionCount;

    //AnhNDd: cac imageview chuc nang
    private ImageButton mEditContact;

    private ImageButton mSendMessage;

    private ImageButton mShareContact;

    private ImageButton mDeleteContact;

    public BtalkFrameToolBarBottomAdapter(Context context, FrameLayout frameLayout, SelectionContactListener selectionContactListener) {
        mFrameToolBarBottom = frameLayout;
        mFrameToolBarBottom.setVisibility(View.GONE);
        mContext = context;
        setUpSelectionView();
        mSelectionContactListener = selectionContactListener;
    }

    /**
     * AnhNDd: Khởi tạo view khi lựa chọn 1 hoặc nhiều contact.
     */
    private void setUpSelectionView() {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        //AnhNDd: tool gốc.
        mSelectionContainer = inflater.inflate(R.layout.btalk_contacts_selection_bar, mFrameToolBarBottom,
                /* attachToRoot = */ true);

        mSelectionContainer.findViewById(R.id.selection_close).setOnClickListener(this);
        //AnhNDd:  Setup popup window
        setupPopupWindow();
        // TrungTh khoi tao 2 view de set lai mau khi chon
        mExpandMoreView = (ImageView) mSelectionContainer.findViewById(R.id.expand_more_view);
        mSelectMenu = (Button) mSelectionContainer.findViewById(R.id.selection_menu);
        mEditContact = (ImageButton) mSelectionContainer.findViewById(R.id.action_edit);
        mEditContact.setOnClickListener(this);
        mSendMessage = (ImageButton) mSelectionContainer.findViewById(R.id.action_message);
        mSendMessage.setOnClickListener(this);
        mShareContact = (ImageButton) mSelectionContainer.findViewById(R.id.action_share);
        mShareContact.setOnClickListener(this);
        mDeleteContact = (ImageButton) mSelectionContainer.findViewById(R.id.action_delete);
        mDeleteContact.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.action_edit:
                //AnhNDd: chỉnh sử contact.
                mSelectionContactListener.editSelectedContact();
                break;
            case R.id.action_message:
                //AnhNDd: gửi tin nhắn cho 1 hoặc nhiều contact.
                mSelectionContactListener.sendMessageSelectedContact();
                break;
            case R.id.action_share:
                //AnhNDd: chia sẻ contacts.
                mSelectionContactListener.shareSelectedContacts();
                break;
            case R.id.action_delete:
                //AnhNDd: xóa contacts.
                mSelectionContactListener.deleteSelectedContacts();
                break;
            case R.id.selection_close:
                mSelectionContactListener.onActionSeclection(SelectionContactListener.Action.STOP_SELECTION_MODE);
                break;
        }
    }

    public void setSelectionMode(boolean bool) {
        mSelectionMode = bool;
        if (mSelectionMode) {
            BtalkUiUtils.visibilityViewWithAnimation(mFrameToolBarBottom);
            mPopupListListener.setHasSelectAll(false);
        } else {
            mFrameToolBarBottom.setVisibility(View.GONE);
        }
    }

    public boolean isSelectionMode() {
        return mSelectionMode;
    }

    //AnhNDd: cập nhật số contact đã chọn
    public void setSelectionCount(int selectionCount) {
        if (selectionCount > 1) {
            //AnhNDd: nếu nhiều hơn 1 contact được chọn thì ẩn icon chỉnh sửa đi.
            mEditContact.setVisibility(View.GONE);
        } else {
            mEditContact.setVisibility(View.VISIBLE);
        }
        mSelectionCount = selectionCount;
        mSelectMenu.setText(String.valueOf(mSelectionCount));
        mSelectionMenu.getPopupList().clearItems();
        mSelectionMenu.getPopupList().addItem(mSelectionMenu.SELECTED,
                String.valueOf(mSelectionCount));
        mSelectionMenu.getPopupList().addItem(mSelectionMenu.SELECT_OR_DESELECT,
                mContext.getString(mPopupListListener
                        .getHasSelectAll() ? R.string.menu_select_none
                        : R.string.menu_select_all));
    }

    private static class PopupListListener
            implements PopupList.OnPopupItemClickListener {
        private WeakReference<BtalkFrameToolBarBottomAdapter> mBtalkFrameToolBarBottomAdapterWeakReference;

        PopupListListener(BtalkFrameToolBarBottomAdapter btalkFrameToolBarBottomAdapter) {
            mBtalkFrameToolBarBottomAdapterWeakReference = new WeakReference<BtalkFrameToolBarBottomAdapter>(btalkFrameToolBarBottomAdapter);
        }

        private boolean mHasSelectAll = false;

        private boolean mPopupShowed = false;

        public void setPopupShowed(boolean popupShowed) {
            mPopupShowed = popupShowed;
        }

        public boolean getPopupShowed() {
            return mPopupShowed;
        }

        public void setHasSelectAll(boolean hasSelectAll) {
            mHasSelectAll = hasSelectAll;
        }

        public boolean getHasSelectAll() {
            return mHasSelectAll;
        }

        @Override
        public boolean onPopupItemClick(int itemId) {
            if (itemId == SelectionMenu.SELECT_OR_DESELECT) {
                mHasSelectAll = !mHasSelectAll;
                mBtalkFrameToolBarBottomAdapterWeakReference.get().mSelectionContactListener.onPopupItemClick(mHasSelectAll);
                if (mBtalkFrameToolBarBottomAdapterWeakReference.get().mSelectionMenu != null) {
                    mBtalkFrameToolBarBottomAdapterWeakReference.get().mSelectionMenu.updateSelectAllMode(mHasSelectAll);
                }
                return true;
            }
            return false;
        }
    }

    private void setupPopupWindow() {
        if (mPopupListListener == null) {
            mPopupListListener = new PopupListListener(this);
        }
        mSelectionMenu = new BtalkSelectionMenu(mContext,
                (Button) mSelectionContainer.findViewById(R.id.selection_menu),
                mPopupListListener);
        mSelectionMenu.getPopupList().addItem(mSelectionMenu.SELECTED,
                String.valueOf(mSelectionCount));
        mSelectionMenu.getPopupList().addItem(mSelectionMenu.SELECT_OR_DESELECT,
                mContext.getString(R.string.menu_select_all));
    }

    // Them set lai mau cho button select
    private Button mSelectMenu;
    private ImageView mExpandMoreView;

    public void setColorForSelectMenuButton(int color) {
        if (mSelectMenu != null && mExpandMoreView != null) {
            mSelectMenu.setTextColor(color);
            mExpandMoreView.setColorFilter(color);
        }
    }

    //Anhdts get view share de lay view anchor hien thi popup share
    public View getShareButton() {
        return mShareContact;
    }
}
