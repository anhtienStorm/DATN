package bkav.android.btalk.calllog.recoder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dialer.calllog.CallLogAdapter;
import com.android.dialer.calllog.FastScroller;
import com.android.dialer.calllog.CallLogFragment;
import com.android.dialer.calllog.ContactInfoHelper;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;

import java.util.ArrayList;
import java.util.TreeSet;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.adapter.BtalkCallLogAdapter;
import bkav.android.btalk.utility.ShortcutUtils;

import static android.view.View.GONE;
import static com.android.dialer.widget.EmptyContentView.NO_LABEL;

/**
 * HuyNQn de sua lai cac ham minh can khoi tao lai tuong ung voi cac lop
 */

public class BtalkCallLogRecoderFragment extends CallLogFragment implements RecorderService.OnRecorderService, BtlkCallLogRecoderAdapter.RecorderAdapterListenner,
                BtalkCallLogRecorderCustomActionModeView.ActionModeListener{

    private SearchView mSearchView;
    private ImageView mImageSearch;
    private boolean mIsCheckSearch = false;
    private RecorderService mService;
    private Intent mPlayIntent;

    private ImageButton mPlayButtonDetail;
    private SeekBar mSeekBarDetail;
    private TextView mNameAudioDetail;
    private TextView mTimeDurationDetail;
    private TextView mTimeCurationDetail;

    private boolean mBoundService;
    private boolean mIsOnFinsh; // Bkav HuyNQN kiem tra xem video da phat xong chua
    private RelativeLayout mLayoutPlayDetail;
    private RecorderAudioAttachmentView mCustomViewIndex;
    private ImageButton mImbOverflow;
    public static final String ID_CREATE_SHORTCUT_CALL_LOG_RECORDER = "ID_CREATE_SHORTCUT_CALL_LOG_RECORDER";
    public static final int SEEKBAR_START = 0;

    private BtalkCallLogRecorderCustomActionModeView mActionModeView;

    private CallLogRecoderActivity mActivity;

    private BtlkCallLogRecoderAdapter mBtalCallLogAdapter;

    // Bkav HuyNQN co check da bat lay out detail
    private boolean mShowDetailLayout;

    public BtalkCallLogRecoderFragment() {
        // Required empty public constructor
    }
    // Bkav HuyNQN bugfix 3412 start
    // lay ra thuc hien xet lai service phat file ghi am khi chuyen sang che do multiscreen
    public RecorderService getService() {
        return mService;
    }

    public boolean isBoundService() {
        return mBoundService;
    }

    // Bkav HuyNQN Tao serviceConnection
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecorderService.RecorderBinder recorderBinder = (RecorderService.RecorderBinder) service;
            mService = recorderBinder.getService();
            mService.setListener(BtalkCallLogRecoderFragment.this);
            setupService();
            mBtalCallLogAdapter = ((BtlkCallLogRecoderAdapter) mAdapter);
            ((BtlkCallLogRecoderAdapter) mAdapter).setListenner(BtalkCallLogRecoderFragment.this);
            mBtalCallLogAdapter.setListenerCheckItem(mCheckListener);
            mBoundService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = false;
        }
    };

    public void setupService() {
        ((BtalkCallLogAdapter) mAdapter).setService(mService);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallLogQueryHandler.setIsCallLogRecorder(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof CallLogRecoderActivity) {
            mActivity = (CallLogRecoderActivity) getActivity();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Bkav HuyNQN bindService
        if (mPlayIntent == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mPlayIntent = new Intent(getContext(), RecorderService.class);
                mPlayIntent.setAction(RecorderService.ACTION_CONNECT);
                getContext().bindService(mPlayIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
                getContext().startService(mPlayIntent);
            }
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Bkav HuyNQN sau khi mo app se clearfocus khoi searchview de khong bi chan onBackPress
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mAdapter.getItemCount() == 0) {
                    mLayoutPlayDetail.setVisibility(View.GONE);
                }
            }
        }, 200);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Bkav HuyNQN refresh thanh play, neu nguoi dung xoa het du lieu se GONE di
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mAdapter.getItemCount() == 0) {
                    mLayoutPlayDetail.setVisibility(View.GONE);
                }
            }
        }, 350); // Bkav HuyNQN delay 350ms de khi truong hop nguoi dung home ra ngoai xoa het du lieu thi khi vao lai se cap nhat lai giao dien
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Bkav HuyNQN ckeck truoc khi unbind tranh bi out app
        if (mService != null) {
            getActivity().unbindService(mServiceConnection);
            mService.onStopSelf();
        }
    }


    @Override
    protected void updateTabUnreadCounts() {

    }

    @Override
    public int getLayoutCallLog() {
        return R.layout.btalk_call_log_recoder_fragment;
    }

    @Override
    public void fetchCalls() {
        super.fetchCalls();
    }

    @Override
    protected CallLogAdapter newCallLogAdapter(String currentCountryIso, VoicemailPlaybackPresenter voicemailPlaybackPresenter, int activityType) {
        return new BtlkCallLogRecoderAdapter(
                getActivity(),
                this,
                new ContactInfoHelper(getActivity(), currentCountryIso),
                voicemailPlaybackPresenter,
                activityType);
    }

    // Bkav HuyNQN cutom them searchview cho setupview
    @Override
    protected void setupView(View view, @Nullable VoicemailPlaybackPresenter voicemailPlaybackPresenter) {
        mSearchView = view.findViewById(R.id.search_call_log_recoder);
        mImageSearch = view.findViewById(R.id.search_recoder_image);
        mSearchView.setQueryHint(getString(R.string.search_menu_search));
        mSearchView.setFocusable(false); // Bkav HuyNQN khi bat len khong cho phep focus vao search view va bat ban phim
        View underLine = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        underLine.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
        EditText searchEditText = (EditText) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(ContextCompat.getColor(getActivity(), R.color.btalk_ab_text_and_icon_normal_color));
        searchEditText.setHintTextColor(ContextCompat.getColor(getActivity(), R.color.color_hint_searchview));
        searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_search_record_size));
//        searchEditText.setCursorVisible(false); // Bkav HuyNQN khi khoi dong len dong con tro trong serchview lai
        onClickSearchCallLogRecoder(searchEditText); // Bkav HuyNQN truyen them tham so de xu ly

        // Bkav HuyNQN thuc hien clearForcus khi bam close button
        ImageView closeButton = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.clearFocus();
                searchEditText.setText("");
            }
        });
        ImageView searchButton = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        searchButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.btalk_ic_search_light));
        closeButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_btalk_remove_small));
        ImageView hintIcon = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        hintIcon.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        mSearchView.setIconifiedByDefault(false);
        // Bkav HuyNQN cai dat cho phan customview detail
        mLayoutPlayDetail = view.findViewById(R.id.layout_play_detail);

        // Bkav HuyNQN neu bat phim dieu huong co ban se lui view len cao de khong bi de vao phim
        if(BtalkCallLogRecorderUtils.hasNavigationBar()){
            mLayoutPlayDetail.setPadding(0,0,0,BtalkCallLogRecorderUtils.getHeightOfNavigationBar(getContext()));
        }
        mLayoutPlayDetail.setVisibility(View.GONE);
        setTextNameAudioDetail(view);
        mTimeDurationDetail = view.findViewById(R.id.text_time_duration);
        mSeekBarDetail = view.findViewById(R.id.seek_bar);
        onClickSeckBar();
        mTimeCurationDetail = view.findViewById(R.id.text_time_curent);
        mPlayButtonDetail = view.findViewById(R.id.btn_pause_or_play);
        onClickPlayButtonDetail();

        // Bkav HuyNQN them nut overfloer
        mImbOverflow = view.findViewById(R.id.imb_over_flow);
        mImbOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickOverFollow();
            }
        });

        mActionModeView = (BtalkCallLogRecorderCustomActionModeView) view.findViewById(R.id.btalk_action_mode);
        mActionModeView.setListener(BtalkCallLogRecoderFragment.this);

        super.setupView(view, voicemailPlaybackPresenter);

    }

    @Override
    public void setFastScroller(FastScroller fastScroller) {
        // Bkav HuyNQN ko xu ly voi fastscroller
    }

    @Override
    public void showOrHideFastScroller(FastScroller fastScroller, int itemsShown) {
        // Bkav HuyNQN ko xu ly voi fastscroller
    }

    // Bkav HuyNQN truyen them tham so xu ly searchview
    private void onClickSearchCallLogRecoder(EditText searchEditText) {

        // Bkav HuyNQN Khi bam vao searchview thi thanh tim kiem se hien len
        searchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setCursorVisible(true);
            }
        });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    mLayoutPlayDetail.setVisibility(View.GONE); // Bkav HuyNQN an detailview khi dang search
                    mIsCheckSearch = true;
                    mCallLogQueryHandler.setIsSearch(true);
                    hideSearchImage(true);
                } else {
                    showSearchImage(true);
                    mCallLogQueryHandler.setIsSearch(false);
//                    mSearchView.clearFocus(); // Bkav HuyNQN Khi du lieu tren searchviw bi xoa het se dong lai ban phim
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter.getItemCount() > 0 && mShowDetailLayout) { // Bkav HuyNQN BOS-2745 end
                                mLayoutPlayDetail.setVisibility(View.VISIBLE); // Bkav HuyNQN mo lai detailview khi dong tim kiem
                            }
                        }
                    },200);

                }
                mCallLogQueryHandler.setTextQuery(newText);
                mAdapter.setQueryString(newText);
                dataSearch();
                return false;
            }
        });
    }

    // Bkav HuyNQN an image tim kiem
    private void hideSearchImage(boolean isHide) {
        mImageSearch.setVisibility(View.GONE);
    }

    // Bkav HuyNQN hien image tim kiem
    private void showSearchImage(boolean isShow) {
        mImageSearch.setVisibility(View.VISIBLE);
    }

    // Bkav HuyNQN tiem hanh cap nhat lai du lieu khi searchview co thay doi
    protected void dataSearch() {
        if (mIsCheckSearch) {
            mIsCheckSearch = false;
        }
        mAdapter.setLoading(true);
        mAdapter.invalidateCache();
        fetchCalls();
        mAdapter.notifyDataSetChanged();
    }

    // Bkav HuyNQN xu ly voi text_name
    private void setTextNameAudioDetail(View view) {
        mNameAudioDetail = view.findViewById(R.id.text_name);
        mNameAudioDetail.setSingleLine(true);
        mNameAudioDetail.setEllipsize(getTextEllipsis());
    }

    // Bkav HuyNQN xu ly tua am thanh khi keo seekbar
    private void onClickSeckBar() {
        mSeekBarDetail.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mService != null && mBoundService) {
                    mService.seekAudio(seekBar.getProgress());
                }
            }
        });
    }

    // Bkav HuyNQN xu ly nut bam play hoac pause trong detailview
    private void onClickPlayButtonDetail() {
        mPlayButtonDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShowDetailLayout = true;
                if (mService != null && mBoundService) {
                    if (mService.getPathAudio() == null || mService.getPathAudio().isEmpty()) {
                        // Bkav HuyNQN Thuc hien phat item dau tien cua danh sach
                        BtalkCallLogRecoderListItemViewHolder holder = (BtalkCallLogRecoderListItemViewHolder) mRecyclerView.findViewHolderForLayoutPosition(0);
                        mCustomViewIndex = holder.rootView.findViewById(R.id.recoder_audio_attachment_framelayout);
                        mCustomViewIndex.setupMediaplayer();
                        return;
                    } else if (mService.isPlaying()) {
                        mService.pauseMediaPlayer();
                    } else {
                        if (mIsOnFinsh) {
                            mService.playRecorder(mService.getIdAudio());
                        } else {
                            mService.startMediaPlayer();
                        }
                    }
                    mService.updateNotification();
                }
            }
        });
    }

    private TextUtils.TruncateAt getTextEllipsis() {
        return TextUtils.TruncateAt.END;
    }

    @Override
    public void onUpdateProgress(int cur) {
        int timeMin = (int) (cur / DateUtils.MINUTE_IN_MILLIS);
        int timeSec = (int) ((cur % DateUtils.MINUTE_IN_MILLIS) / DateUtils.SECOND_IN_MILLIS);
        String sTime = String.format("%02d:%02d", timeMin, timeSec);
        mTimeCurationDetail.setText(sTime);
        mSeekBarDetail.setProgress(cur);
    }

    @Override
    public void onTimeDurationAudio(int dur) {
        int timeMin = (int) (dur / DateUtils.MINUTE_IN_MILLIS);
        int timeSec = (int) ((dur % DateUtils.MINUTE_IN_MILLIS) / DateUtils.SECOND_IN_MILLIS);
        String sTime = String.format("%02d:%02d", timeMin, timeSec);
        mTimeDurationDetail.setText(sTime);
        mSeekBarDetail.setMax(dur);
    }

    @Override
    public void onPlayAudio() {
        mLayoutPlayDetail.setVisibility(View.VISIBLE); // Bkav HuyNQN khi thuc hien play se hien thi viewdetail
        mIsOnFinsh = false;
        String pathAudio = mService.getPathAudio();
        int start = pathAudio.lastIndexOf("/") + 1; // Bkav HuyNQN lay vi tri sau dau "/"
        String nameAudio = pathAudio.substring(start);
        mPlayButtonDetail.setVisibility(View.VISIBLE);
        mPlayButtonDetail.setImageResource(R.drawable.ic_pause_recoder);
        mNameAudioDetail.setText(nameAudio);
    }

    @Override
    public void onResumeAudio() {
        mPlayButtonDetail.setImageResource(R.drawable.ic_pause_recoder);
    }

    @Override
    public void onPauseAudio() {
        mPlayButtonDetail.setImageResource(R.drawable.ic_play_recoder);
    }

    @Override
    public void onFinishPlayAudio() {
        mIsOnFinsh = true;
        mPlayButtonDetail.setImageResource(R.drawable.ic_play_recoder);
        mSeekBarDetail.setProgress(SEEKBAR_START);
        mTimeCurationDetail.setText(R.string.time_seek_bar_start);
    }

    // Bkav HuyNQN thuc hien dong ung dung khi bam vao exit
    @Override
    public void exitWindows() {
        mService.onStopSelf(); // Bkav HuyNQN stopFroreground truoc khi exit
        // Bkav TienNAb: fix loi crash app btalk
        if (getActivity() != null){
            getActivity().moveTaskToBack(true);
            getActivity().finish();
            System.exit(0);
        }
    }

    // Bkav HuyNQN Khong thuc hien ve lai giao dien de update time nua.
    @Override
    protected void refeshRecyclerView() {

    }

    @Override
    public void onShowLayout(boolean isShow) {
        mShowDetailLayout = isShow;
    }

    @Override
    public void showActionModeView() {
        mActionModeView.setVisibility(View.VISIBLE);
        hideOrShowSearchBar(View.GONE);
        mActivity.setStatusbarOnActionModeMessage();
    }

    @Override
    public void goneActionModeView() {
        if(mActionModeView.getListPathSelect() != null){
            if(mActionModeView.getListPathSelect().size() == 0){
                mActionModeView.setVisibility(View.GONE);
                hideOrShowSearchBar(View.VISIBLE);
                mActivity.exitActionModeMessage();
            }
        }
    }

    private void hideOrShowSearchBar(int pos){
        mSearchView.setVisibility(pos);
        mImageSearch.setVisibility(pos);
    }

    // Bkav HuyNQN an Action call trong giao dien nay
    @Override
    protected void hideEmptyListViewAction() {
        mEmptyListView.setActionLabel(NO_LABEL);
    }

    @Override
    protected int getRecentCallsEmpty() {

        // Bkav HuyNQN khi danh sach trong thi khong hoen thi view play audio
        mLayoutPlayDetail.setVisibility(GONE);
        return R.string.call_log_recorder_all_empty;
    }

    private void clickOverFollow() {
        PopupMenu popupMenu = new PopupMenu(getActivity(), mImbOverflow, Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.btalk_menu_show_recorder_laucher
                , popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.show_icon_launcher:
                        ShortcutUtils.get().createOrRemoveShortCut(true);
                        Toast.makeText(getContext(), R.string.application_shortcut_has_been_created, Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    public void clickButton() {
        hideOrShowSearchBar(View.VISIBLE);
        mActionModeView.setVisibility(View.GONE);
        mActionModeView.getListPathSelect().removeAll(mActionModeView.getListPathSelect());

        refreshItemAfterSelection();
        mActivity.exitActionModeMessage();
    }

    @Override
    public void deleteAudioIsPlay(TreeSet<String> listPathSelect) {
        // Bkav HuyNQN neu xoa file dang phat thi stop service
        for (String path : listPathSelect) {
            if(path.equals(mService.getPathAudio())){
                mService.stopMediaPlayer();
                // Bkav HuyNQN BOS-1893 khi xoa file dang phat se an luon layout thông tin chơi nhạc
                mLayoutPlayDetail.setVisibility(GONE);
            }

        }
        clickButton();
    }

    // Bkav HuyNQN reset lai cac item sau khi bo check
    private void refreshItemAfterSelection() {
        //TrungTH mBtalCallLogAdapter duoc khoi tao sau khi service connect => can check null khi dung dam bao truong hop service bi sao do ko connect dc
        //TrungTH Chua ro y tuong hoi lai huynqn
        if(mBtalCallLogAdapter != null) {
            mBtalCallLogAdapter.setSelectAll(false);
        }
    }

    @Override
    public void selectAllItem() {
        mActionModeView.getListPathSelect().removeAll(mActionModeView.getListPathSelect());
        refreshItemAfterSelection();
        if(mBtalCallLogAdapter != null) {
            mBtalCallLogAdapter.setSelectAll(true);
        }
    }

    public interface CheckItemListener {
        void updateListPath(TreeSet<String> path);
    }

    BtalkCallLogRecoderFragment.CheckItemListener mCheckListener = new BtalkCallLogRecoderFragment.CheckItemListener() {
        @Override
        public void updateListPath(TreeSet<String> path) {
            mActionModeView.updateListPath(path); // Bkav HuyNQN update danh sach path file ghi am dang duoc chon
        }
    };
}
