package bkav.android.btalk.calllog.recoder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import bkav.android.btalk.BuildConfig;
import bkav.android.btalk.R;

public class BtalkCallLogRecorderCustomActionModeView extends RelativeLayout implements View.OnClickListener {

    private Context mContext;
    private ImageButton mDeleteButton;
    private ImageButton mShareButton;
    private ImageButton mCloseActionMode;
    private Button mSelectionMenuButton;
    private TreeSet<String> mListPathSelect;
    private View mActionModeView;

    public TreeSet<String> getListPathSelect() {
        return mListPathSelect;
    }

    public BtalkCallLogRecorderCustomActionModeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mListPathSelect = new TreeSet<>();
        LayoutInflater inflater = LayoutInflater.from(context);
        mActionModeView = inflater.inflate(R.layout.btalk_call_log_recorder_custom_action_mode_view, this, true);
        mDeleteButton = (ImageButton) mActionModeView.findViewById(R.id.action_delete);
        mShareButton = (ImageButton) mActionModeView.findViewById(R.id.action_share);
        mSelectionMenuButton = mActionModeView.findViewById(R.id.selection_menu);
        mCloseActionMode = mActionModeView.findViewById(R.id.action_home);
        handleClick();
    }

    private void handleClick() {
        mDeleteButton.setOnClickListener(this);
        mShareButton.setOnClickListener(this);
        mCloseActionMode.setOnClickListener(this);
        mSelectionMenuButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_delete:
                deleteAudioFile();
                if(mListener != null){
                    mListener.deleteAudioIsPlay(mListPathSelect);
                }
                break;
            case R.id.action_share:
                shareAudioFile();
                if(mListener != null){
                    mListener.clickButton();
                }
                break;
            case R.id.action_home:
                if(mListener != null){
                    mListener.clickButton();
                }
                break;
            case R.id.selection_menu:
                selectionAction();
                break;
        }
    }

    // Bkav HuyNQN thuc hien select file
    private void selectionAction(){
        PopupMenu popupMenu = new PopupMenu(mContext, mSelectionMenuButton, Gravity.NO_GRAVITY);
        popupMenu.getMenuInflater().inflate(R.menu.btalk_recorder_action_mode_menu
                , popupMenu.getMenu());
        MenuItem numberConversationItem = popupMenu.getMenu().findItem(R.id.action_show_num_item);
        numberConversationItem.setTitle(String.valueOf(mListPathSelect.size()));
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_select_all_item:
                        if(mListener != null){
                            mListener.selectAllItem();
//                            mSelectionMenuButton.setText(String.valueOf(mListPathSelect.size()));
                        }
                        break;
                    case R.id.action_unselect_all_item: // Bkav HuyNQN them option bo chon tat ca
                        if(mListener != null){
                            mListener.clickButton();
                        }
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    // Bkav HuyNQN thuc hien delete file
    private void deleteAudioFile() {
        int countFileDeleted = 0;
        boolean isDelete = false;
        for (String path : mListPathSelect) {
            // Bkav HuyNQN xoa duong dan khoi database callog
            DeleteCallRecordPathAction.deleteCallRecordPath(path);

            // Bkav HuyNQN thuc hien xoa file
            File fileDelete = new File(path);
            if (fileDelete.exists()) {
                if (fileDelete.delete()) {
                    isDelete = true;
                    countFileDeleted++;
                }
            }
        }

        //Bkav ToanNTe fix Btalk - BOS 8.7 - Lỗi: Thông báo "Xóa tệp tin thành công" khi thực hiện
        //xóa file ghi âm cuộc gọi hiển thị hơn 20 giây mới ẩn
        if (isDelete && mListPathSelect.size() > 1) {
                Toast.makeText(mContext, mContext.getString(R.string.delete_files_success, countFileDeleted), Toast.LENGTH_SHORT).show();
        } else if (isDelete && mListPathSelect.size() == 1) {
            Toast.makeText(mContext, R.string.delete_file_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, R.string.delete_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    // Bkav HuyNQN thuc hien chia se file
    private void shareAudioFile(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType(/*"audio/*"*/ "*/*"); // Bkav HuyNQN goi ra nhieu nhat cac app ho tro share
        ArrayList<Uri> files = new ArrayList<>();
        for(String path : mListPathSelect ) {
            File file = new File(path);
            Uri uri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider",file);
            files.add(uri);
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent intentChooser = Intent.createChooser(intent, mContext.getResources().getString(R.string.choose_app));
        mContext.startActivity(intentChooser);
    }

    public void updateListPath(TreeSet lsPath){
        mListPathSelect = lsPath;
        mSelectionMenuButton.setText(String.valueOf(mListPathSelect.size()));
    }

    public interface ActionModeListener{
        void clickButton();
        void selectAllItem();
        void deleteAudioIsPlay(TreeSet<String> listPathSelect);
    }

    private ActionModeListener mListener;

    public void setListener(ActionModeListener mListener) {
        this.mListener = mListener;
    }
    public void addPathToList(String path){
        mListPathSelect.add(path);
        mSelectionMenuButton.setText(String.valueOf(mListPathSelect.size()));
    }

    public void deletePathFromList(String path){
        mListPathSelect.remove(path);
        mSelectionMenuButton.setText(String.valueOf(mListPathSelect.size()));
    }
}
