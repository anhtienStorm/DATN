package bkav.android.btalk.calllog.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.ClipboardUtils;
import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.compat.CompatUtils;
import com.android.contacts.common.compat.ContactsCompat;
import com.android.contacts.common.compat.PhoneNumberUtilsCompat;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.common.util.UriUtils;
import com.android.contacts.common.widget.CheckableQuickContactBadge;
import com.android.dialer.calllog.CallLogAdapter;
import com.android.dialer.calllog.CallLogAsyncTaskUtil;
import com.android.dialer.calllog.CallLogListItemHelper;
import com.android.dialer.calllog.CallLogListItemViewHolder;
import com.android.dialer.calllog.IntentProvider;
import com.android.dialer.calllog.PhoneCallDetailsViews;
import com.android.dialer.calllog.calllogcache.CallLogCache;
import com.android.dialer.compat.FilteredNumberCompat;
import com.android.dialer.database.FilteredNumberAsyncQueryHandler;
import com.android.dialer.filterednumber.BlockNumberDialogFragment;
import com.android.dialer.filterednumber.FilteredNumbersUtil;
import com.android.dialer.logging.Logger;
import com.android.dialer.logging.ScreenEvent;
import com.android.dialer.service.ExtendedBlockingButtonRenderer;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.PhoneNumberUtil;
import com.android.dialer.util.TelecomUtil;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;
import com.android.ex.chips.ChipsUtil;
import com.android.messaging.Factory;

import java.util.ArrayList;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.bmsblocked.BmsUtils;
import bkav.android.btalk.calllog.BtalkAllCallDetailActivity;
import bkav.android.btalk.calllog.recoder.CallLogRecoderActivity;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.contacts.BtalkQuickContactActivity;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.esim.provider.ESimDbController;
import bkav.android.btalk.fragments.dialpad.BtalkCheckableQuickContactBadge;
import bkav.android.btalk.messaging.BtalkFactoryImpl;
import bkav.android.btalk.messaging.ui.BtalkUIIntentsImpl;
import bkav.android.btalk.mutilsim.SimUltil;
import bkav.android.btalk.suggestmagic.BtalkDialerDatabaseHelper;

import static com.android.dialer.DialerApplication.getContext;

/**
 * Created by anhdt on 31/03/2017.
 * viewHolder cho lop {@link BtalkCallLogAdapter}
 * custom {@link CallLogListItemViewHolder}
 */
// Bkav HuyNQN them public cho lop con the ke thua
public class BtalkCallLogListItemViewHolderNew extends CallLogListItemViewHolder implements View.OnLongClickListener {
    final View dividerView; //Bkav TrungTH Testthem vao de co the an di

    // Anhdts bo cardView, dung view khac de setTag cho view holder
    // Bkav HuyNQN thay doi thanh protected de cho lop con ke thua
    protected BtalkCallLogListItemViewHolderNew(
            Context context,
            ExtendedBlockingButtonRenderer.Listener eventListener,
            View.OnClickListener expandCollapseListener,
            CallLogCache callLogCache,
            CallLogListItemHelper callLogListItemHelper,
            VoicemailPlaybackPresenter voicemailPlaybackPresenter,
            FilteredNumberAsyncQueryHandler filteredNumberAsyncQueryHandler,
            BlockNumberDialogFragment.Callback filteredNumberDialogCallback,
            View rootView, QuickContactBadge quickContactView,
            View primaryActionView,
            PhoneCallDetailsViews phoneCallDetailsViews,
            View callLogEntryView,
            TextView dayGroupHeader,
            ImageView primaryActionButtonView,
            boolean isArchiveTab) {
        super(context, eventListener, expandCollapseListener, callLogCache, callLogListItemHelper, voicemailPlaybackPresenter, filteredNumberAsyncQueryHandler, filteredNumberDialogCallback, rootView, quickContactView, primaryActionView, phoneCallDetailsViews, null, dayGroupHeader, primaryActionButtonView, isArchiveTab);
        mBtalkCallLogEntryView = callLogEntryView;
        dividerView = rootView.findViewById(R.id.divider); // Bkav TrungTH Test:
    }

    public static CallLogListItemViewHolder create(
            View view,
            Context context,
            ExtendedBlockingButtonRenderer.Listener eventListener,
            View.OnClickListener expandCollapseListener,
            CallLogCache callLogCache,
            CallLogListItemHelper callLogListItemHelper,
            VoicemailPlaybackPresenter voicemailPlaybackPresenter,
            FilteredNumberAsyncQueryHandler filteredNumberAsyncQueryHandler,
            BlockNumberDialogFragment.Callback filteredNumberDialogCallback,
            boolean isArchiveTab) {

        return new BtalkCallLogListItemViewHolderNew(
                context,
                eventListener,
                expandCollapseListener,
                callLogCache,
                callLogListItemHelper,
                voicemailPlaybackPresenter,
                filteredNumberAsyncQueryHandler,
                filteredNumberDialogCallback,
                view,
                (BtalkCheckableQuickContactBadge) view.findViewById(R.id.quick_contact_photo),
                view.findViewById(R.id.primary_action_view),
                BtalkPhoneCallDetailsViews.fromView(view),
                view.findViewById(R.id.call_log_row),
                (TextView) view.findViewById(R.id.call_log_day_group_label),
                (ImageView) view.findViewById(R.id.primary_action_button),
                isArchiveTab);
    }

    // Anhdts image from call to message
    @Override
    public int getImgResActionButton() {
        return R.drawable.bkav_ic_message_recent_call;
    }

    @Override
    public void onCreateContextMenu(
            final ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (TextUtils.isEmpty(number)) {
            return;
        }
        // Anhdts khoi tao gia tri khi onClickMenu
        mFinalDisplayNumber = displayNumber;
        mFinalInfo = info;
        mFinalNumber = number;
        mFinalPostDialDigits = postDialDigits;

        if (callType == CallLog.Calls.VOICEMAIL_TYPE) {
            menu.setHeaderTitle(mContext.getResources().getText(R.string.voicemail));
        } else {
            if (info.name == null || info.name.isEmpty()) {
                menu.setHeaderTitle(PhoneNumberUtilsCompat.createTtsSpannable(
                        BidiFormatter.getInstance().unicodeWrap(number, TextDirectionHeuristics.LTR)));
            } else {
                menu.setHeaderTitle(info.name);
            }
        }

        menu.add(ContextMenu.NONE, R.id.context_menu_copy_to_clipboard, ContextMenu.NONE,
                R.string.action_copy_number_text)
                .setOnMenuItemClickListener(this);

        menu.add(ContextMenu.NONE, R.id.context_menu_view_call_log_recoerder, ContextMenu.NONE,
                R.string.action_view_call_log_recorder)
                .setOnMenuItemClickListener(this);

        // The edit number before call does not show up if any of the conditions apply:
        // 1) Number cannot be called
        // 2) Number is the voicemail number
        // 3) Number is a SIP address
        menu.add(ContextMenu.NONE, R.id.context_menu_show_call_detail, ContextMenu.NONE,
                R.string.action_show_call_detail)
                .setOnMenuItemClickListener(this);

        if (PhoneNumberUtil.canPlaceCallsTo(number, numberPresentation)
                && !mCallLogCache.isVoicemailNumber(accountHandle, number)
                && !PhoneNumberUtil.isSipNumber(number)) {
            menu.add(ContextMenu.NONE, R.id.context_menu_share_contact_via, ContextMenu.NONE,
                    R.string.action_share_contact_via)
                    .setOnMenuItemClickListener(this);
        }

        if (PhoneNumberUtil.canPlaceCallsTo(number, numberPresentation)
                && !mCallLogCache.isVoicemailNumber(accountHandle, number)
                && !PhoneNumberUtil.isSipNumber(number)) {
            menu.add(ContextMenu.NONE, R.id.context_menu_edit_before_call, ContextMenu.NONE,
                    R.string.action_edit_number_before_call)
                    .setOnMenuItemClickListener(this);
        }

        if (callType == CallLog.Calls.VOICEMAIL_TYPE
                && phoneCallDetailsViews.voicemailTranscriptionView.length() > 0) {
            menu.add(ContextMenu.NONE, R.id.context_menu_copy_transcript_to_clipboard,
                    ContextMenu.NONE, R.string.copy_transcript_text)
                    .setOnMenuItemClickListener(this);
        }

        if (FilteredNumberCompat.canAttemptBlockOperations(mContext)
                && FilteredNumbersUtil.canBlockNumber(mContext, number, countryIso)) {
            mFilteredNumberAsyncQueryHandler.isBlockedNumber(
                    new FilteredNumberAsyncQueryHandler.OnCheckBlockedListener() {
                        @Override
                        public void onCheckComplete(Integer id) {
                            /*blockId = id;
                            int blockTitleId = blockId == null ? R.string.action_block_number
                                    : R.string.action_unblock_number;*/
                            // Bkav HuyNQN check voi logic chan so cua BMS
                            int blockTitleId = 0;
                            if(BmsUtils.isHasNumberBlocks(mContext, number)){
                                blockTitleId = R.string.action_unblock_number;
                            }else {
                                blockTitleId = R.string.action_block_number;
                            }
                            final MenuItem blockItem = menu.add(
                                    ContextMenu.NONE,
                                    R.id.context_menu_block_number,
                                    ContextMenu.NONE,
                                    blockTitleId);
                            blockItem.setOnMenuItemClickListener(
                                    BtalkCallLogListItemViewHolderNew.this);
                        }
                    }, number, countryIso);
        }

        menu.add(ContextMenu.NONE, R.id.context_menu_remove_in_call_log, ContextMenu.NONE,
                R.string.action_remove_in_call_log)
                .setOnMenuItemClickListener(this);

        menu.add(ContextMenu.NONE, R.id.context_menu_clear_in_call_log, ContextMenu.NONE,
                R.string.action_clear_in_call_log)
                .setOnMenuItemClickListener(this);

        Logger.logScreenView(ScreenEvent.CALL_LOG_CONTEXT_MENU, (Activity) mContext);
    }

    /**
     * Anhdts
     * phan mem dialer su dung contextmenu trong viewHolder
     * nhung khi scroll recycler thi viewHolder duoc tai su dung lai nen cac bien da bi thay doi
     * => xu ly sai khi click nen can luu lai cac gia tri tai thoi diem click vao
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    // Anhdts chia se contact
    private void shareContact() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        if (DialerUtils.isConferenceURICallLog(mFinalNumber, mFinalPostDialDigits)) {
            intent.putExtra("org.codeaurora.extra.DIAL_CONFERENCE_URI", true);
        }
        intent.putExtra(Intent.EXTRA_TEXT, getContentForward(mFinalDisplayNumber, mFinalInfo.name));
        intent.setType("text/plain");
        Intent chooser = Intent.createChooser(
                intent, mContext.getString(R.string.action_share_contact_via));
        DialerUtils.startActivityWithErrorToast(mContext, chooser);
    }

    @Override
    protected Uri[] getCallLogEntryUris() {
        final Uri[] uris = new Uri[callIds.length];
        for (int index = 0; index < callIds.length; ++index) {
            uris[index] = ContentUris.withAppendedId(
                    TelecomUtil.getCallLogUri(mContext), callIds[index]);
        }
        return uris;
    }

    // Anhdts action sua truoc khi goi
    @Override
    protected void fixBeforeCallAction() {
        if (mContext instanceof BtalkActivity) {
//            ((BtalkActivity) mContext).setCurrentTab(BtalkActivity.TAB_PHONE_INDEX);
//            ((BtalkActivity) mContext).setActionFixBeforeCall(mFinalNumber);
        } else {
            Bundle data = new Bundle();
            data.putString(BtalkActivity.ARGUMENT_NUMBER, number);
            Intent intent = new Intent(BtalkActivity.ACTION_FIX_BEFORE_CALL);
            intent.putExtras(data);
            mContext.startActivity(intent);
        }
    }

    // Anhdts xoa tat ca call log theo so dien thoai
    private void deleteAllCallLogByName() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // Bkav HienDTk: cursor de dem xem bao nhieu so duoc chon de xoa
        Cursor cursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                CallLog.Calls.NUMBER + "= ? ", new String[]{mFinalNumber}, null);
        mContext.getContentResolver().delete(
                CallLog.Calls.CONTENT_URI,
                CallLog.Calls.NUMBER + "= ? ", new String[]{mFinalNumber}
        );
        BtalkDialerDatabaseHelper.getInstance(mContext).removeCallLog(mFinalNumber);
        // Bkav HienDTk: them text xoa lien he trong callog.
        Toast.makeText(mContext,  mContext.getString(R.string.delete_callog, cursor.getCount()), Toast.LENGTH_LONG).show();
    }

    // Anhdts action cho image button cua item recycle
    @Override
    protected IntentProvider getIntentPrimaryButton() {
        return IntentProvider.getSendSmsIntentProvider(number);
    }

    // Anhdts noi dung chuyen danh ba
    private static String getContentForward(String number, String name) {

        if (TextUtils.isEmpty(number)) {
            return "";
        }

        if (TextUtils.isEmpty(name)) {
            return number;
        }

        StringBuilder phoneNumber = new StringBuilder(name);
        if (!number.equals(name)) {
            phoneNumber.append(" <");
            phoneNumber.append(number);
            phoneNumber.append(">");
        }
        return phoneNumber.toString();
    }

    // Anhdts dang ki gui tin nhan trong app
    // Neu trong che do xoa hang loat thi la xoa call log
    @Override
    protected void registerFactoryActionSend(Intent intent) {
        ((BtalkFactoryImpl) Factory.get()).registerSendMessage();
        DialerUtils.startActivityWithErrorToast(mContext, intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onLongClick(View v) {
        if (BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot()) {
            if (ESimDbController.getAllSim().size() < 3) {
                isShowListSim = false;
            } else {
                PhoneAccountHandle handleDefault = TelecomUtil.getDefaultOutgoingPhoneAccount(mContext, PhoneAccount.SCHEME_TEL);
                int slotDefault = SimUltil.getSlotSimByAccount(mContext, handleDefault); // Bkav HuyNQN neu co 1 sim hoac 2 sim ma ko dat sim mac dinh thi gia tri tra ve = -1;
                isShowListSim = slotDefault != -1;
            }
        } else {
            isShowListSim = ESimUtils.isMultiProfile();
        }

        // Anhdts trong che do delete thi khong cho hien dialog len
        if (TextUtils.isEmpty(number) || mIsModeDelete) {
            return false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        final ArrayAdapter<String> menu = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1);
        final ArrayList<Integer> mapId = new ArrayList<>();

        // Anhdts khoi tao gia tri khi onClickMenu
        mFinalDisplayNumber = displayNumber;
        mFinalInfo = info;
        mFinalNumber = number;
        mFinalPostDialDigits = postDialDigits;
        mFinalRowId = rowId;
        mFinalCallId = callIds;

        View header = LayoutInflater.from(mContext).inflate(R.layout.btalk_header_context_menu_calllog, null);

        TextView title = (TextView) header.findViewById(R.id.header_menu_contact_name);

        QuickContactBadge icon = (QuickContactBadge) header.findViewById(R.id.header_menu_contact_photo);
        icon.setContentDescription("Show detail contact " + mFinalNumber);

        if (callType == CallLog.Calls.VOICEMAIL_TYPE) {
            title.setText(mContext.getResources().getText(R.string.voicemail));
        } else {
            if (info.name == null || info.name.isEmpty()) {
                title.setText(PhoneNumberUtilsCompat.createTtsSpannable(
                        BidiFormatter.getInstance().unicodeWrap(number, TextDirectionHeuristics.LTR)));
            } else {
                title.setText(info.name);
            }
        }

        icon.setOverlay(null);
        if (CompatUtils.hasPrioritizedMimeType()) {
            if (ChipsUtil.isRunningMOrLater()) {
                icon.setPrioritizedMimeType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            }
        }

        bindPhoto(icon);

        builder.setCustomTitle(header);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int simDefault;
            simDefault = SimUltil.getDefaultSimCell(mContext);
            if (simDefault != 3) { // Bkav HuyNQN du co 1 sim cung phai kiem tra xem no co chua listSim hay ko
//                menu.add(mContext.getString(R.string.action_call_by_sim, String.valueOf(2 - simDefault),
//                        SimUltil.getNotDefaultSimName(mContext)));
                addMenuCall(menu);
                if (isAddMapId) {
                    mapId.add(R.id.context_menu_call_by_other_sim);
                }
            }
        }

        if (info.name == null || info.name.isEmpty()) {
            menu.add(mContext.getString(R.string.action_add_contact));
            mapId.add(R.id.context_menu_add_contact);
        }

        menu.add(mContext.getString(R.string.action_copy_number_text));
        mapId.add(R.id.context_menu_copy_to_clipboard);

        // Bkav HuyNQN them xu ly chuen sang giao dien CallLogRecorder
        if (isShowTextViewCallLogRecorder()) {
            menu.add(mContext.getString(R.string.action_view_call_log_recorder));
            mapId.add(R.id.context_menu_view_call_log_recoerder);
        }

        // The edit number before call does not show up if any of the conditions apply:
        // 1) Number cannot be called
        // 2) Number is the voicemail number
        // 3) Number is a SIP address
        // <Anhdts add>
        menu.add(mContext.getString(R.string.action_show_call_detail));
        mapId.add(R.id.context_menu_show_call_detail);

        menu.add(mContext.getString(R.string.action_show_all_call_detail));
        mapId.add(R.id.context_menu_show_all_call_detail);

        if (PhoneNumberUtil.canPlaceCallsTo(number, numberPresentation)
                && !mCallLogCache.isVoicemailNumber(accountHandle, number)
                && !PhoneNumberUtil.isSipNumber(number)) {
            menu.add(mContext.getString(R.string.action_share_contact_via));
            mapId.add(R.id.context_menu_share_contact_via);
        } // </Anhdts add>

        if (PhoneNumberUtil.canPlaceCallsTo(number, numberPresentation)
                && !mCallLogCache.isVoicemailNumber(accountHandle, number)
                && !PhoneNumberUtil.isSipNumber(number)) {
            menu.add(mContext.getString(R.string.action_edit_number_before_call));
            mapId.add(R.id.context_menu_edit_before_call);
        }

        if (callType == CallLog.Calls.VOICEMAIL_TYPE
                && phoneCallDetailsViews.voicemailTranscriptionView.length() > 0) {
            menu.add(mContext.getString(R.string.copy_transcript_text));
            mapId.add(R.id.context_menu_copy_transcript_to_clipboard);
        }

        if (FilteredNumberCompat.canAttemptBlockOperations(mContext)
                && FilteredNumbersUtil.canBlockNumber(mContext, number, countryIso)) {
            mFilteredNumberAsyncQueryHandler.isBlockedNumber(
                    new FilteredNumberAsyncQueryHandler.OnCheckBlockedListener() {
                        @Override
                        public void onCheckComplete(Integer id) {
                            /*blockId = id;
                            int blockTitleId = blockId == null ? R.string.action_block_number
                                    : R.string.action_unblock_number;*/
                            // Bkav HuyNQN check voi logic chan so cua BMS
                            int blockTitleId = 0;
                            if(BmsUtils.isHasNumberBlocks(mContext, number)){
                                blockTitleId = R.string.action_unblock_number;
                            }else {
                                blockTitleId = R.string.action_block_number;
                            }

                            menu.add(mContext.getString(blockTitleId));
                            mapId.add(R.id.context_menu_block_number);
                        }
                    }, number, countryIso);
        }

        // <Anhdts add>
        menu.add(mContext.getString(R.string.action_remove_in_call_log));
        mapId.add(R.id.context_menu_remove_in_call_log);

        menu.add(mContext.getString(R.string.action_clear_in_call_log));
        mapId.add(R.id.context_menu_clear_in_call_log);

        // Bkav HuyNQN them bao cao spam cho bms
        menu.add(mContext.getString(R.string.action_report_spam_to_bms));
        mapId.add(R.id.report_spam_to_bms);

        // </Anhdts add>

        Logger.logScreenView(ScreenEvent.CALL_LOG_CONTEXT_MENU, (Activity) mContext);
        builder.setAdapter(menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int id = mapId.get(which);
                onClickItemMenu(id);
            }
        });
        builder.show();
        return true;
    }


    /**
     * Anhdts
     * phan mem dialer su dung contextmenu trong viewHolder
     * nhung khi scroll recycler thi viewHolder duoc tai su dung lai nen cac bien da bi thay doi
     * => xu ly sai khi click nen can luu lai cac gia tri tai thoi diem click vao
     */
    private boolean onClickItemMenu(int resId) {
        if (resId == R.id.context_menu_block_number) {
            // Bkav HuyNQN su dung chan so dien thoai bang bms logic cu khong dung comment lai
            /*FilteredNumberCompat
                    .showBlockNumberDialogFlow(mContext.getContentResolver(), blockId, mFinalNumber,
                            countryIso, mFinalDisplayNumber, R.id.floating_action_button_container,
                            ((Activity) mContext).getFragmentManager(),
                            mFilteredNumberDialogCallback);*/
            if(BmsUtils.isHasNumberBlocks(mContext, mFinalNumber)){
                BmsUtils.showDialogUnblocked(mContext, mFinalNumber);
            }else {
                BmsUtils.showDialogAddCallLogBlocked(mContext, mFinalNumber);
            }
            return true;
        } else if (resId == R.id.context_menu_copy_to_clipboard) {
            ClipboardUtils.copyText(mContext, null, mFinalNumber, false);
            // Bkav TienNAb: sua lai thong bao toast tren man hinh khi copy so dien thoai
            Toast.makeText(mContext, R.string.string_copy_number_contact, Toast.LENGTH_SHORT).show();
            return true;

        } else if (resId == R.id.context_menu_view_call_log_recoerder) {
            Intent intent = new Intent(getContext(), CallLogRecoderActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            return true;
        } else if (resId == R.id.context_menu_copy_transcript_to_clipboard) {
            ClipboardUtils.copyText(mContext, null,
                    phoneCallDetailsViews.voicemailTranscriptionView.getText(), true);
            return true;
        } else if (resId == R.id.context_menu_edit_before_call) {
            fixBeforeCallAction();
            return true;
        } else if (resId == R.id.context_menu_show_all_call_detail) {
            showAllContactCallLog();
            return true;
        }
        // Anhdts doi intent
        if (resId == R.id.context_menu_show_call_detail) {
            IntentProvider intentProvider = getCallDetailIntentProvider(mFinalRowId, mFinalCallId, null);
            final Intent intent = intentProvider.getIntent(mContext);
            // See IntentProvider.getCallDetailIntentProvider() for why this may be null.
            if (DialerUtils.isConferenceURICallLog(mFinalNumber, mFinalPostDialDigits)) {
                intent.putExtra("org.codeaurora.extra.DIAL_CONFERENCE_URI", true);
            }
            if (intent != null) {
                DialerUtils.startActivityWithErrorToast(mContext, intent);
            }
        } else if (resId == R.id.context_menu_share_contact_via) {
            shareContact();
        } else if (resId == R.id.context_menu_remove_in_call_log) {
            if (callType == CallLog.Calls.VOICEMAIL_TYPE) {
                CallLogAsyncTaskUtil.deleteVoicemail(
                        mContext, Uri.parse(voicemailUri), null);
            } else {
                final StringBuilder callIds = new StringBuilder();
                for (Uri callUri : getCallLogEntryUris()) {
                    if (callIds.length() != 0) {
                        callIds.append(",");
                    }
                    callIds.append(ContentUris.parseId(callUri));
                }
                // Anhdts cap nhat du lieu search
                BtalkDialerDatabaseHelper.getInstance(mContext).removeCallLog(callIds);
                CallLogAsyncTaskUtil.deleteCalls(
                        mContext, callIds.toString(), null, getCallLogEntryUris().length);
            }
        } else if (resId == R.id.context_menu_clear_in_call_log) {
            deleteAllCallLogByName();
        } else if (resId == R.id.context_menu_call_by_other_sim) {
//            SimUltil.callWithSimMode(mContext, false, number);
            if (mListener != null && isShowListSim) {
                mListener.actionCall(isShowListSim, number);
            } else if (!isShowListSim) { // Bkav HuyNQN fix loi 2 sim thuong co cai dat sim mac dinh, khi goi bang sim khac lai ra sim mac dinh
                SimUltil.callWithSimMode(mContext, false, number);
            }
        } else if (resId == R.id.context_menu_add_contact) {
            BtalkUIIntentsImpl.get().launchAddContactActivity(mContext, number);
        }else if(resId == R.id.report_spam_to_bms){
            Toast.makeText(mContext, mContext.getString(R.string.text_notification_report_spam), Toast.LENGTH_SHORT).show();
            BmsUtils.insertNumberSpam(mContext, number);
        }
        return false;
    }

    /**
     * Anhdts hien thi tat ca nhat ki cuoc goi cua so do
     */
    private void showAllContactCallLog() {
        Intent intent = new Intent(mContext, BtalkAllCallDetailActivity.class);
        // Check if the first item is a voicemail.
        if (voicemailUri != null) {
            intent.putExtra(BtalkAllCallDetailActivity.EXTRA_VOICEMAIL_URI,
                    Uri.parse(voicemailUri));
        }
        intent.putExtra(BtalkAllCallDetailActivity.EXTRA_TYPE, BtalkAllCallDetailActivity.EXTRA_TYPE_SHOW_ALL);
        intent.putExtra(BtalkAllCallDetailActivity.EXTRA_NUMBER, mFinalNumber);
        if (DialerUtils.isConferenceURICallLog(mFinalNumber, mFinalPostDialDigits)) {
            intent.putExtra("org.codeaurora.extra.DIAL_CONFERENCE_URI", true);
        }
        DialerUtils.startActivityWithErrorToast(mContext, intent);
    }

    // Anhdts
    protected void initPrimaryView() {
        primaryActionButtonView.setOnClickListener(this);
        primaryActionView.setOnClickListener(mExpandCollapseListener);
        primaryActionView.setOnLongClickListener(this);
    }

    /**
     * Anhdts load photo len context menu
     */
    private void bindPhoto(QuickContactBadge view) {
        view.assignContactUri(info.lookupUri);

        final boolean isVoicemail = mCallLogCache.isVoicemailNumber(accountHandle, number);
        int contactType = ContactPhotoManager.TYPE_DEFAULT;
        if (isVoicemail) {
            contactType = ContactPhotoManager.TYPE_VOICEMAIL;
        } else if (isBusiness) {
            contactType = ContactPhotoManager.TYPE_BUSINESS;
        }

        final String lookupKey = info.lookupUri != null
                ? UriUtils.getLookupKeyFromUri(info.lookupUri) : null;
        final String displayName = TextUtils.isEmpty(info.name) ? displayNumber : info.name;
        final ContactPhotoManager.DefaultImageRequest request = new ContactPhotoManager.DefaultImageRequest(
                displayName, lookupKey, contactType, true /* isCircular */);

        if (info.photoId == 0 && info.photoUri != null) {
            ContactPhotoManager.getInstance(mContext).loadPhoto(view, info.photoUri,
                    mPhotoSize, false /* darkTheme */, true /* isCircular */, request);
        } else {
            ContactPhotoManager.getInstance(mContext).loadThumbnail(view, info.photoId,
                    false /* darkTheme */, true /* isCircular */, request);
        }

        if (mExtendedBlockingButtonRenderer != null) {
            mExtendedBlockingButtonRenderer.updatePhotoAndLabelIfNecessary(
                    number,
                    countryIso,
                    view,
                    phoneCallDetailsViews.callLocationAndDate);
        }

        view.setOnClickListener(mOnClickPhotoBadge);
    }

    private View.OnClickListener mOnClickPhotoBadge = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onClickQuickContact();
        }
    };

    /**
     * Anhdts custom lai su kien click icon anh contact, chi mo den giao dien cua btalk
     */
    private void onClickQuickContact() {
        if (ContactsCompat.isEnterpriseContactId(rowId)) {
            // No implicit intent as user may have a different contacts app in work profile.
            ContactsContract.QuickContact.showQuickContact(mContext, new Rect(), info.lookupUri,
                    BtalkQuickContactActivity.MODE_FULLY_EXPANDED, null);
        } else {
            final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(
                    info.lookupUri, BtalkQuickContactActivity.MODE_FULLY_EXPANDED);
            intent.setPackage(mContext.getPackageName());
            intent.putExtra(BtalkQuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE,
                    com.android.contacts.common.logging.ScreenEvent.ScreenType.ALL_CONTACTS);
            ImplicitIntentsUtil.startActivityInApp(mContext, intent);
        }
    }

    @Override
    public void setModeDelete(boolean modeDelete) {
        mIsModeDelete = modeDelete;
    }

    @Override
    public void setChooseDelete(CallLogAdapter callLogAdapter) {
        if (callType == CallLog.Calls.VOICEMAIL_TYPE) {
            boolean checked = callLogAdapter.changeStateUriVoice(voicemailUri);
            setChecked(checked);
            ((BtalkCheckableQuickContactBadge) quickContactView).setChecked(
                    checked, true);
        } else {
            boolean checked = callLogAdapter.changeStateUri(callIds);
            setChecked(checked);
            ((BtalkCheckableQuickContactBadge) quickContactView).setChecked(
                    checked, true);
        }
    }

    /**
     * Anhdts set du lieu cho image
     */
    @Override
    protected void setDataImage() {
        ((BtalkCheckableQuickContactBadge) quickContactView).setData(rowId, info.lookupUri);
    }

    @Override
    protected void updatePhoto(CallLogAdapter callLogAdapter) {
        // Anhdts neu trong che do delete thi check xem da duoc check chua
        if (mIsModeDelete && callLogAdapter.isCheckDelete(callIds, voicemailUri)) {
            super.updatePhoto();
            setChecked(true);
            ((CheckableQuickContactBadge) quickContactView).setChecked(true, false);
            return;
        } else if (((CheckableQuickContactBadge) quickContactView).isChecked()) {
            setChecked(false);
            ((CheckableQuickContactBadge) quickContactView).setChecked(false, false);
        }
        super.updatePhoto();
    }

    private void setChecked(boolean bool) {
        if (bool) {
            rootView.setBackgroundColor(Color.LTGRAY);
        } else {
            rootView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.btalk_transparent_view));
        }
    }

    private boolean isShowListSim;

    protected void addMenuCall(ArrayAdapter<String> menu) {
        isAddMapId = false;
        if (isShowListSim) {
            addMenuCallEsim(menu);
            isAddMapId = true;
        } else {
            if (BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot()) {
                PhoneAccountHandle handle;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    handle = TelecomUtil.getDefaultOutgoingPhoneAccount(mContext,
                            PhoneAccount.SCHEME_TEL);
                    int slotDefault = SimUltil.getSlotSimByAccount(mContext, handle);
                    // Bkav TienNAb: them dieu kien check truong hop lap 2 sim nhung huy kich hoat mot sim
                    if (handle != null && slotDefault != -1) {
                        isAddMapId = true;
                        int simDefault;
                        simDefault = SimUltil.getDefaultSimCell(mContext);
                        if (simDefault != -1) {
                            menu.add(mContext.getString(R.string.action_call_by_sim, String.valueOf(2 - simDefault),
                                    SimUltil.getNotDefaultSimName(mContext)));
                        }
                    }
                }
            }
        }
    }

    private void addMenuCallEsim(ArrayAdapter<String> menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            menu.add(mContext.getString(R.string.action_call_by_sim_other));
        }
    }

    public interface ItemCallLogListener {
        void actionCall(boolean isShow, String number);
    }

    private ItemCallLogListener mListener;

    public void setListener(ItemCallLogListener listener) {
        this.mListener = listener;
    }

    private boolean isAddMapId; // Bkav HuyNQN dung de kiem tra xem co add id goi bang sim khac hay khong

    /**
     * Retrieves the call details intent provider for an entry in the call log.
     *
     * @param id           The call ID of the first call in the call group.
     * @param extraIds     The call ID of the other calls grouped together with the call.
     * @param voicemailUri If call log entry is for a voicemail, the voicemail URI.
     * @return The call details intent provider.
     */
    private IntentProvider getCallDetailIntentProvider(
            final long id, final long[] extraIds, final String voicemailUri) {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                Intent intent = new Intent(context, BtalkAllCallDetailActivity.class);
                intent.putExtra(BtalkAllCallDetailActivity.EXTRA_TYPE, BtalkAllCallDetailActivity.EXTRA_TYPE_SHOW_LIST);
                // Check if the first item is a voicemail.
                if (voicemailUri != null) {
                    intent.putExtra(BtalkAllCallDetailActivity.EXTRA_VOICEMAIL_URI,
                            Uri.parse(voicemailUri));
                }

                if (extraIds != null && extraIds.length > 0) {
                    intent.putExtra(BtalkAllCallDetailActivity.EXTRA_CALL_LOG_IDS, extraIds);
                } else {
                    // If there is a single item, use the direct URI for it.
                    intent.setData(ContentUris.withAppendedId(TelecomUtil.getCallLogUri(context),
                            id));
                }
                return intent;
            }
        };
    }
}
