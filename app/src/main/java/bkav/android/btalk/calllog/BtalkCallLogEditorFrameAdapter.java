package bkav.android.btalk.calllog;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.android.contacts.activities.PopupList;
import com.android.contacts.activities.SelectionMenu;

import java.lang.ref.WeakReference;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.adapter.BtalkCallLogAdapter;
import bkav.android.btalk.contacts.BtalkSelectionMenu;

/**
 * Created by anhdt on 23/10/2017.
 *
 */

class BtalkCallLogEditorFrameAdapter implements View.OnClickListener {

    private static final int SELECTED = 0;

    private static final int SELECT_OR_DESELECT = 1;

    private View mFrameEditor;

    private FrameLayout mFrameContainer;

    private BtalkCallLogFragment mFragment;

    private BtalkSelectionMenu mSelectionMenu;

    private PopupListListener mPopupListListener;

    private int mSelectionCount;

    private BtalkCallLogAdapter mAdapter;

    private View mBarView;

    BtalkCallLogEditorFrameAdapter(BtalkCallLogFragment fragment, FrameLayout frame, BtalkCallLogAdapter adapter, View barView) {
        mFragment = fragment;
        mFrameContainer = frame;
        mAdapter = adapter;
        mBarView = barView;
        setupView();
    }

    private void setupView() {
        LayoutInflater inflater = (LayoutInflater) mFragment.getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        mFrameEditor = inflater.inflate(R.layout.btalk_frame_editor_call_log, mFrameContainer,
                true);

        mFrameEditor.findViewById(R.id.selection_close).setOnClickListener(this);
        setupPopupWindow();
        mFrameEditor.findViewById(R.id.action_copy).setOnClickListener(this);
        mFrameEditor.findViewById(R.id.action_message).setOnClickListener(this);
        mFrameEditor.findViewById(R.id.action_share).setOnClickListener(this);
        mFrameEditor.findViewById(R.id.action_delete).setOnClickListener(this);
    }

    void setSelectionCount(int selectionCount) {
        // Bkav TienNAb: fix loi khong co chuc nang con tat ca khi dang chon mot lien he
        if (selectionCount == 0) {
//            mPopupListListener.setHasSelectAll(false);
            mFrameEditor.findViewById(R.id.action_copy).setVisibility(View.INVISIBLE);
            mFrameEditor.findViewById(R.id.action_message).setVisibility(View.INVISIBLE);
            mFrameEditor.findViewById(R.id.action_share).setVisibility(View.INVISIBLE);
            mFrameEditor.findViewById(R.id.action_delete).setVisibility(View.INVISIBLE);
        } else {
//            mPopupListListener.setHasSelectAll(true);
            mFrameEditor.findViewById(R.id.action_copy).setVisibility(View.VISIBLE);
            mFrameEditor.findViewById(R.id.action_message).setVisibility(View.VISIBLE);
            mFrameEditor.findViewById(R.id.action_share).setVisibility(View.VISIBLE);
            mFrameEditor.findViewById(R.id.action_delete).setVisibility(View.VISIBLE);
        }

        mSelectionCount = selectionCount;
        Button selectMenu = (Button) mFrameEditor.findViewById(R.id.selection_menu);
        selectMenu.setText(String.valueOf(mSelectionCount));
        mSelectionMenu.getPopupList().clearItems();
        mSelectionMenu.getPopupList().addItem(SELECTED,
                String.valueOf(mSelectionCount));
        mSelectionMenu.getPopupList().addItem(SELECT_OR_DESELECT,
                mFragment.getString(mPopupListListener
                        .getHasSelectAll() ? R.string.menu_select_none
                        : R.string.menu_select_all));
    }
    private static class PopupListListener
            implements PopupList.OnPopupItemClickListener {
        private WeakReference<BtalkCallLogEditorFrameAdapter> mBtalkEditorFrameAdapterWeakReference;

        PopupListListener(BtalkCallLogEditorFrameAdapter btalkCallLogEditorFrameAdapter) {
            mBtalkEditorFrameAdapterWeakReference = new WeakReference<>(btalkCallLogEditorFrameAdapter);
        }

        private boolean mHasSelectAll = false;

        void setHasSelectAll(boolean hasSelectAll) {
            mHasSelectAll = hasSelectAll;
        }

        boolean getHasSelectAll() {
            return mHasSelectAll;
        }

        @Override
        public boolean onPopupItemClick(int itemId) {
            if (itemId == SelectionMenu.SELECT_OR_DESELECT) {
                mHasSelectAll = !mHasSelectAll;
                mBtalkEditorFrameAdapterWeakReference.get().mAdapter.setSelectAll(mHasSelectAll);

                if (mBtalkEditorFrameAdapterWeakReference.get().mSelectionMenu != null) {
                    mBtalkEditorFrameAdapterWeakReference.get().mSelectionMenu.updateSelectAllMode(mHasSelectAll);
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.action_copy:
                mAdapter.actionCopy();
                break;
            case R.id.action_message:
                mAdapter.actionSendMessage();
                break;
            case R.id.action_share:
                mAdapter.actionShare();
                break;
            case R.id.action_delete:
                mAdapter.deleteSelectedItem();
                mFragment.changeModeEditor(false);
                break;
            // Bkav TienNAb: sua lai logic thoat selection mode, chi thoat selection mode khi click vao icon quay lai
            case R.id.selection_close:
                mFragment.changeModeEditor(false);
                break;
        }
    }

    private void setupPopupWindow() {
        if (mPopupListListener == null) {
            mPopupListListener = new PopupListListener(this);
        }
        mSelectionMenu = new BtalkSelectionMenu(mFragment.getActivity(),
                (Button) mFrameEditor.findViewById(R.id.selection_menu),
                mPopupListListener);
        mSelectionMenu.getPopupList().addItem(SELECTED,
                String.valueOf(mSelectionCount));
        mSelectionMenu.getPopupList().addItem(SELECT_OR_DESELECT,
                mFragment.getString(R.string.menu_select_all));
    }

    void changeModeSelection(boolean isSelection) {
        mSelectionCount = 0;
        setSelectionCount(0);
        mBarView.setVisibility(isSelection ? View.GONE : View.VISIBLE);
        mFrameEditor.setVisibility(isSelection ? View.VISIBLE : View.GONE);
    }
}
