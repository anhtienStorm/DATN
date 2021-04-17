package bkav.android.btalk.calllog.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.support.v7.widget.RecyclerView;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.dialer.calllog.CallLogAdapter;
import com.android.dialer.calllog.CallLogAsyncTaskUtil;
import com.android.dialer.calllog.CallLogGroupBuilder;
import com.android.dialer.calllog.CallLogListItemViewHolder;
import com.android.dialer.calllog.CallLogQuery;
import com.android.dialer.calllog.ContactInfoHelper;
import com.android.dialer.calllog.IntentProvider;
import com.android.dialer.calllog.PhoneCallDetailsHelper;
import com.android.dialer.calllog.calllogcache.CallLogCache;
import com.android.dialer.filterednumber.BlockNumberDialogFragment;
import com.android.dialer.logging.InteractionEvent;
import com.android.dialer.logging.Logger;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.PhoneNumberUtil;
import com.android.dialer.util.TelecomUtil;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;
import com.android.messaging.Factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.BtalkCallLogFragment;
import bkav.android.btalk.calllog.recoder.RecorderAudioAttachmentView;
import bkav.android.btalk.calllog.recoder.RecorderService;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.messaging.BtalkFactoryImpl;
import bkav.android.btalk.suggestmagic.BtalkDialerDatabaseHelper;
import bkav.android.btalk.utility.DateUtil;


/**
 * Created by trungth on 24/03/2017.
 * Bkav TrungTH : de sua lai cac ham minh can khoi tao lai tuong ung voi cac lop
 */

public class BtalkCallLogAdapter extends CallLogAdapter implements BtalkCallLogListItemViewHolderNew.ItemCallLogListener {

    // Bkav HuyNQN them bien RecorderService
    protected RecorderService mService;

    public void setService(RecorderService mService) {
        this.mService = mService;
        notifyDataSetChanged();
    }

    public BtalkCallLogAdapter(Context context, CallFetcher callFetcher, ContactInfoHelper contactInfoHelper, VoicemailPlaybackPresenter voicemailPlaybackPresenter, int activityType) {
        super(context, callFetcher, contactInfoHelper, voicemailPlaybackPresenter, activityType);
    }

    /**
     * Anhdts custom tu lop cha
     */
    @Override
    protected RecyclerView.ViewHolder createCallLogEntryViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.btalk_call_log_list_item, parent, false);
        CallLogListItemViewHolder viewHolder = createCallLogViewHolder(view);
        viewHolder.mBtalkCallLogEntryView.setTag(viewHolder);
        viewHolder.mBtalkCallLogEntryView.setAccessibilityDelegate(mAccessibilityDelegate);

        viewHolder.primaryActionView.setTag(viewHolder);
        return viewHolder;
    }

    /**
     * Anhdts createCallLogViewHolder su dung BtalkCallLogListItemViewHolder
     */
    private CallLogListItemViewHolder createCallLogViewHolder(View view) {
        return BtalkCallLogListItemViewHolderNew.create(
                view,
                mContext,
                this,
                mExpandCollapseListener,
                mCallLogCache,
                mCallLogListItemHelper,
                mVoicemailPlaybackPresenter,
                mFilteredNumberAsyncQueryHandler,
                new BlockNumberDialogFragment.Callback() {
                    @Override
                    public void onFilterNumberSuccess() {
                        Logger.logInteraction(
                                InteractionEvent.BLOCK_NUMBER_CALL_LOG);
                    }

                    @Override
                    public void onUnfilterNumberSuccess() {
                        Logger.logInteraction(
                                InteractionEvent.UNBLOCK_NUMBER_CALL_LOG);
                    }

                    @Override
                    public void onChangeFilteredNumberUndo() {
                    }
                }, mActivityType == ACTIVITY_TYPE_ARCHIVE);
    }

    /**
     * Anhdts custom lai ham, nhom theo cac ngay
     */
    @Override
    protected boolean checkSetGroupHeader(Cursor c, CallLogListItemViewHolder views, long date) {
        String currentGroup = DateUtil.get().formatDateRecentCall(mContext, date);
        long previousGroup = getPreviousDay(c);
        return previousGroup == 0 || !currentGroup.equals(DateUtil.get().formatDateRecentCall(mContext, previousGroup));
    }

    @Override
    protected String getDayGroupForCall(long callId, long date) {
        return DateUtil.get().formatDateRecentCall(mContext, date);
    }

    /**
     * Anhdts su dung BtalkPhoneCallDetailsHelper thay {@link CallLogGroupBuilder}
     */
    @Override
    protected PhoneCallDetailsHelper createPhoneCallDetailsHelper(Resources resources) {
        return new BtalkPhoneCallDetailsHelper(mContext, resources, mCallLogCache);
    }

    /**
     * Anhdts get thoi gian cho cuoc goi phia truoc
     */
    private long getPreviousDay(Cursor cursor) {
        // We want to restore the position in the cursor at the end.
        int startingPosition = cursor.getPosition();
        long dateTime = 0;
        if (cursor.moveToPrevious()) {
            int previousViewPosition = mShowVoicemailPromoCard ? startingPosition :
                    startingPosition - 1;
            if (previousViewPosition != mHiddenPosition || cursor.moveToPrevious()) {
                dateTime = cursor.getLong(CallLogQuery.DATE);
            }
        }
        cursor.moveToPosition(startingPosition);

        return dateTime;
    }

    // Anhdts custom action touch vao item recycle
    @Override
    protected boolean getActionCallBkav(CallLogListItemViewHolder viewHolder) {
        PhoneAccountHandle handle = TelecomUtil.getDefaultOutgoingPhoneAccount(mContext,
                PhoneAccount.SCHEME_TEL);
        //Bkav QuangNDb neu khong co default sim va multi sim thi hien thi dialog chon sim
        if (handle == null && BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot()) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClickWithNotDefaultSim(viewHolder.number);
            }
        }else {
            IntentProvider intentProvider = IntentProvider.getReturnCallIntentProvider(viewHolder.number + viewHolder.postDialDigits);
            final Intent intent = intentProvider.getIntent(mContext);
            // See IntentProvider.getCallDetailIntentProvider() for why this may be null.
            if (DialerUtils.isConferenceURICallLog(viewHolder.number, viewHolder.postDialDigits)) {
                intent.putExtra("org.codeaurora.extra.DIAL_CONFERENCE_URI", true);
            }
            if (intent != null) {
                DialerUtils.startActivityWithErrorToast(mContext, intent);
            }
        }
        return true;
    }

    @Override
    protected void setDividerVisibility(CallLogListItemViewHolder views, int value) {
        // Bkav TrungTH Test: Them vao de xu ly view divider
        if (views instanceof BtalkCallLogListItemViewHolderNew) {
            ((BtalkCallLogListItemViewHolderNew) views).dividerView.setVisibility(value);
            ((BtalkCallLogListItemViewHolderNew) views).setListener(BtalkCallLogAdapter.this);
        }
    }

    // Anhdts custom calllog
    @Override
    protected CallLogCache getCallLogCache(Context context) {
        return BtalkCallLogCache.getCallLogCache(context);
    }

    public void changeModeDelete(boolean isModeDelete) {
        mIsModeDelete = isModeDelete;
        if (!mIsModeDelete) {
            mListCallDelete = null;
            mListVoiceDelete = null;
        }
    }

    /**
     * Anhdts xoa danh sach da chon
     */
    public void deleteSelectedItem() {
        int numbers = 0;
        if (mListVoiceDelete != null) {
            numbers += mListVoiceDelete.size();
            for (String callId : mListVoiceDelete) {
                CallLogAsyncTaskUtil.deleteVoicemail(
                        mContext, Uri.parse(callId), null);
            }
        }

        if (mListCallDelete != null) {
            numbers += mListCallDelete.size();
            final StringBuilder callIds = new StringBuilder();
            for (long callId : mListCallDelete) {
                if (callIds.length() != 0) {
                    callIds.append(",");
                }
                callIds.append(callId);
            }
            // Anhdts cap nhat du lieu search
            BtalkDialerDatabaseHelper.getInstance(mContext).removeCallLog(callIds);
            CallLogAsyncTaskUtil.deleteCalls(
                    mContext, callIds.toString(), null, mListCallDelete.size());
        }
        // Bkav HienDTk: fix bug hien thi thong bao 2 lan khi xoa nhat ky cuoc goi => BOS-2702 - Start
//        Toast.makeText(mContext, numbers + " " + mContext.getString(R.string.toast_delete_calls_complete), Toast.LENGTH_SHORT).show();
        // Bkav HienDTk: fix bug hien thi thong bao 2 lan khi xoa nhat ky cuoc goi => BOS-2702 - End
    }


    /**
     * Anhdts thay doi trang thai co duoc check hay khong
     */
    @Override
    public boolean changeStateUri(long[] callIds) {
        boolean notExist = false;
        if (mListCallDelete == null) {
            mListCallDelete = new TreeSet<>();
        }
        for (long callId : callIds) {
            if (mListCallDelete.contains(callId)) {
                mListCallDelete.remove(callId);
            } else {
                mListCallDelete.add(callId);
                notExist = true;
            }
        }
        mListenerCheckItem.updateSelectionCount((mListVoiceDelete != null ? mListVoiceDelete.size() : 0)
                + (mListCallDelete != null ? mListCallDelete.size() : 0));
        return notExist;
    }

    /**
     * Anhdts thay doi trang thai co duoc check hay khong - dung co voice call
     */
    @Override
    public boolean changeStateUriVoice(String voiceuri) {
        boolean notExist = false;
        if (mListVoiceDelete == null) {
            mListVoiceDelete = new TreeSet<>();
        }
        if (mListVoiceDelete.contains(voiceuri)) {
            mListVoiceDelete.remove(voiceuri);
        } else {
            mListVoiceDelete.add(voiceuri);
            notExist = true;
        }
        mListenerCheckItem.updateSelectionCount((mListVoiceDelete != null ? mListVoiceDelete.size() : 0)
                + (mListCallDelete != null ? mListCallDelete.size() : 0));
        return notExist;
    }

    private BtalkCallLogFragment.CheckItemListener mListenerCheckItem;

    public void setCheckItemChangeListener(BtalkCallLogFragment.CheckItemListener checkItemChangeListener) {
        mListenerCheckItem = checkItemChangeListener;
    }

    public void setSelectAll(boolean selectAll) {
        if (selectAll) {
            if (mListVoiceDelete == null) {
                mListVoiceDelete = new TreeSet<>();
            } else {
                mListVoiceDelete.clear();
            }
            if (mListCallDelete == null) {
                mListCallDelete = new TreeSet<>();
            } else {
                mListCallDelete.clear();
            }

            Cursor c = (Cursor) getItem(0);
            if (c != null && c.getCount() > 0) {
                int currentPos = c.getPosition();
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    if (c.getInt(CallLogQuery.CALL_TYPE) == CallLog.Calls.VOICEMAIL_TYPE) {
                        mListVoiceDelete.add(c.getString(CallLogQuery.VOICEMAIL_URI));
                    } else {
                        mListCallDelete.add(c.getLong(CallLogQuery.ID));
                    }
                }
                c.moveToPosition(currentPos);
            }
            mListenerCheckItem.updateSelectionCount((mListVoiceDelete != null ? mListVoiceDelete.size() : 0)
                    + (mListCallDelete != null ? mListCallDelete.size() : 0));
        } else {
            mListenerCheckItem.updateSelectionCount(0);
            if (mListVoiceDelete != null) {
                mListVoiceDelete.clear();
            }
            if (mListCallDelete != null) {
                mListCallDelete.clear();
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Anhdts copy gia tri cac so dien thoai duoc chon
     */
    public void actionCopy() {
        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", getNumberByUri(mListCallDelete, true));
        clipboard.setPrimaryClip(clip);
        Toast.makeText(mContext, R.string.toast_copy_contact, Toast.LENGTH_SHORT).show();
        // Bkav HienDTk: thuc hien copy xong moi bo chon
        setSelectAll(false);
    }

    /**
     * Anhdts action nhan tin toi cac so dien thoai duoc chon
     */
    public void actionSendMessage() {
        ((BtalkFactoryImpl) Factory.get()).registerSendMessage();
        String phoneToSend = getNumberByUri(mListCallDelete, false);
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneToSend));
        smsIntent.setPackage(mContext.getPackageName());
        mContext.startActivity(smsIntent);
    }

    /**
     * Anhdts action chia se cac so dien thoai duoc chon
     */
    public void actionShare() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, getNumberByUri(mListCallDelete, true));
        intent.setType("text/plain");
        Intent chooser = Intent.createChooser(
                intent, mContext.getString(R.string.action_share_contact_via));
        DialerUtils.startActivityWithErrorToast(mContext, chooser);
    }

    private static String[] SELECTION =
            {CallLog.Calls.NUMBER,
                    CallLog.Calls.NUMBER_PRESENTATION,
                    CallLog.Calls.CACHED_FORMATTED_NUMBER,
                    CallLog.Calls.CACHED_NAME};

    /**
     * Anhdts lay gia tri so dien thoai tu uri
     */
    private String getNumberByUri(TreeSet<Long> callIds, boolean shareOrCopy) {
        final StringBuilder selectionIds = new StringBuilder();
        for (long callId : callIds) {
            if (selectionIds.length() != 0) {
                selectionIds.append(",");
            }
            selectionIds.append(callId);
        }

        Cursor cursor = mContext.getContentResolver().query(TelecomUtil.getCallLogUri(mContext), SELECTION,
                CallLog.Calls._ID + " IN (" + selectionIds.toString() + ")", null, null);
        ArrayList<String> listNumber = new ArrayList<>();
        HashMap<String, String> mapName = new HashMap<>();
        StringBuilder value = new StringBuilder();
        if (cursor != null) {
            cursor.moveToPosition(-1);
            if (shareOrCopy) {
                while (cursor.moveToNext()) {
                    String number = getDisplayNumber(mContext,
                            cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)),
                            cursor.getInt(cursor.getColumnIndex(CallLog.Calls.NUMBER_PRESENTATION)),
                            cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_FORMATTED_NUMBER)),
                            false);
                    if (!listNumber.contains(number)) {
                        String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                        if (!TextUtils.isEmpty(name)) {
                            mapName.put(number, name);
                        }
                        listNumber.add(number);
                    }
                }
                for (String number : listNumber) {
                    if (value.length() != 0) {
                        value.append("; ");
                    }
                    if (mapName.containsKey(number)) {
                        value.append(mapName.get(number));
                        value.append(" <");
                        value.append(number);
                        value.append(">");
                    } else {
                        value.append(number);
                    }
                }
            } else {
                while (cursor.moveToNext()) {
                    String number = "";
                    if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_FORMATTED_NUMBER)))) {
                        number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_FORMATTED_NUMBER));
                    } else if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)))) {
                        number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                    }
                    if (!listNumber.contains(number)) {
                        listNumber.add(number);
                    }
                }
                for (String temp : listNumber) {
                    value.append(temp);
                    value.append("; ");
                }
            }
            cursor.close();
        }
        return value.toString();
    }

    /**
     * Anhdts: lay gia tri so dien thoai hien thi
     */
    private String getDisplayNumber(
            Context context,
            String number,
            int presentation,
            String formattedNumber,
            boolean isVoicemail) {
        final String displayName = getDisplayName(context, number, presentation, isVoicemail);
        if (!TextUtils.isEmpty(displayName)) {
            return displayName;
        }
        if (!TextUtils.isEmpty(formattedNumber)) {
            return formattedNumber;
        } else if (!TextUtils.isEmpty(number)) {
            return number;
        } else {
            return context.getResources().getString(R.string.unknown);
        }
    }

    private String getDisplayName(
            Context context,
            String number,
            int presentation,
            boolean isVoicemail) {
        if (presentation == CallLog.Calls.PRESENTATION_UNKNOWN) {
            return context.getResources().getString(R.string.unknown);
        }
        if (presentation == CallLog.Calls.PRESENTATION_RESTRICTED) {
            return context.getResources().getString(R.string.private_num);
        }
        if (presentation == CallLog.Calls.PRESENTATION_PAYPHONE) {
            return context.getResources().getString(R.string.payphone);
        }
        if (isVoicemail) {
            return context.getResources().getString(R.string.voicemail);
        }
        if (PhoneNumberUtil.isLegacyUnknownNumbers(number)) {
            return context.getResources().getString(R.string.unknown);
        }
        return "";
    }

    /**
     * Anhdts get lai mang id calllog
     */
    @Override
    protected long[] getCallIds(final Cursor cursor, final int groupSize) {
        // We want to restore the position in the cursor at the end.
        long[] ids = new long[groupSize];
        // Copy the ids of the rows in the group.
        ArrayList<Long> list = mCallLogGroupBuilder.getSortList();
        int pos = list.indexOf(cursor.getLong(CallLogQuery.ID));
        for (int i = pos; i < pos + groupSize; i++) {
            ids[i - pos] = list.get(i);
        }
        return ids;
    }

    // Bkav HuyNQN xu ly RecorderAudioAttachmentView
    @Override
    public void setPathRecorder(CallLogListItemViewHolder view, String path) {
        RecorderAudioAttachmentView customView = view.primaryActionView.findViewById(R.id.recoder_audio_attachment_framelayout);
        if (path == null || path.equals("")) {
            view.setIsShowTextViewCallLogRecorder(false);
            customView.setVisibility(View.GONE);
        } else {
            view.setIsShowTextViewCallLogRecorder(true);
            customView.setVisibility(View.VISIBLE);
            customView.setDataResource(path);
            customView.setService(mService);
        }
    }

    private ItemClickListener mItemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    @Override
    public void actionCall(boolean isShow, String number) {
        if (mItemClickListener != null) {
            mItemClickListener.onItemCallWithOtherSim(isShow, number);
        }
    }

    //Bkav QuangNDb them interface click item call log
    public interface ItemClickListener{
        void onItemClickWithNotDefaultSim(String number);
        void onItemCallWithOtherSim(boolean isShow, String number); // Bkav HuyNQN thuc hien goi voi sim khac
    }

}
