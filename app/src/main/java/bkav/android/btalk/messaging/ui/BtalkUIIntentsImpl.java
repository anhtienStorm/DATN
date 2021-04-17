package bkav.android.btalk.messaging.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;

import com.android.contacts.common.CallUtil;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.TelecomUtil;
import com.android.messaging.Factory;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.sms.MmsSmsUtils;
import com.android.messaging.ui.UIIntentsImpl;
import com.android.messaging.util.Assert;

import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.activities.BtalkQuickResponseEditActivity;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.esim.dialog_choose_sim.DialogChooseSimFragment;
import bkav.android.btalk.messaging.ui.appsettings.BtalkApplicationSettingsActivity;
import bkav.android.btalk.messaging.ui.appsettings.BtalkPerSubscriptionSettingsActivity;
import bkav.android.btalk.messaging.ui.appsettings.BtalkSettingsActivity;
import bkav.android.btalk.messaging.ui.conversation.BtalkConversationActivity;
import bkav.android.btalk.messaging.ui.conversationlist.BtalkArchivedConversationListActivity;
import bkav.android.btalk.messaging.ui.conversationlist.BtalkForwardMessageActivity;
import bkav.android.btalk.messaging.ui.conversationsettings.BtalkPeopleAndOptionsActivity;
import bkav.android.btalk.messaging.ui.photoviewer.BtalkBuglePhotoViewActivity;

/**
 * Created by quangnd on 27/03/2017.
 */

public class BtalkUIIntentsImpl extends UIIntentsImpl {
    private static final String GROUP_ID = "group_id";

    public static final String MESSAGE_ACTION = "message_action";

    // Bkav HuyNQN them co xu ly app ngoai goi toi tin nhan
    public static final String FLAG_SEND_TO = "message_send_to";

    // Bkav TienNAb: them hang so cho che do them lien he yeu thich
    public static final String ACTION = "action";
    public static final String ACTION_ADD_FAVORITE = "action_add_favorite";

    @Override
    protected Intent conversationIntent(Context context) {
        return new Intent(context, BtalkConversationActivity.class);
    }

    @Override
    public void launchSettingsActivity(Context context) {
        final Intent intent = new Intent(context, BtalkSettingsActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void launchArchivedConversationsActivity(Context context) {
        final Intent intent = new Intent(context, BtalkArchivedConversationListActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void launchForwardMessageActivity(Context context, MessageData message) {
        final Intent forwardMessageIntent = new Intent(context, BtalkForwardMessageActivity.class)
                .putExtra(UI_INTENT_EXTRA_DRAFT_DATA, message);
        context.startActivity(forwardMessageIntent);
    }

    @Override
    public void launchApplicationSettingsActivity(Context context, boolean topLevel) {
        final Intent intent = new Intent(context, BtalkApplicationSettingsActivity.class);
        intent.putExtra(UI_INTENT_EXTRA_TOP_LEVEL_SETTINGS, topLevel);
        context.startActivity(intent);
    }

    @Override
    protected Intent getPerSubscriptionSettingsIntent(Context context, int subId, @Nullable String settingTitle) {
        return new Intent(context, BtalkPerSubscriptionSettingsActivity.class)
                .putExtra(UI_INTENT_EXTRA_SUB_ID, subId)
                .putExtra(UI_INTENT_EXTRA_PER_SUBSCRIPTION_SETTING_TITLE, settingTitle);
    }

    @Override
    public Intent getLaunchConversationActivityIntent(final Context context) {
        final Intent intent = new Intent(context, BtalkLaunchConversationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    @Override
    protected Class<? extends Activity> returnBuglePhotoViewActitivty() {
        return BtalkBuglePhotoViewActivity.class;
    }

    @Override
    public void launchCreateNewGroupConversationActivity(Context context, MessageData draft) {
        final Intent intent = getConversationActivityIntent(context, GROUP_ID, draft,
                false /* withCustomTransition */);
        context.startActivity(intent);
    }

    @Override
    public void launchBlockedParticipantsActivity(final Context context) {
        final Intent intent = new Intent(context, BtalkBlockedParticipantsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Intent getConversationListActivityIntent(Context context) {
        Intent intent = new Intent(context, BtalkActivity.class);
        intent.setAction(MESSAGE_ACTION);
        return intent;
    }

    /**
     * BKav QuangNDb mo activity conversation khi bam vao item khi search
     */
    @Override
    public void launchConversationActivitySearch(Context context, String conversationId, MessageData draft, Bundle activityOptions, boolean withCustomTransition, int messagePos) {
        Assert.isTrue(!withCustomTransition || activityOptions != null);
        final Intent intent = getConversationActivityIntent(context, conversationId, draft,
                withCustomTransition);
        intent.putExtra(UI_INTENT_EXTRA_MESSAGE_POSITION, messagePos);
        context.startActivity(intent, activityOptions);
    }

    @Override
    public void launchPeopleAndOptionsActivity(Activity activity, String conversationId) {
        final Intent intent = new Intent(activity, BtalkPeopleAndOptionsActivity.class);
        intent.putExtra(UI_INTENT_EXTRA_CONVERSATION_ID, conversationId);
        activity.startActivityForResult(intent, 0);
    }

    /**
     * Anhdts
     */
    @Override
    public void launchAddContactActivity(final Context context, final String destination) {
        final Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        final String destinationType = MmsSmsUtils.isEmailAddress(destination) ?
                ContactsContract.Intents.Insert.EMAIL : ContactsContract.Intents.Insert.PHONE;
        intent.setPackage(Factory.get().getApplicationContext().getPackageName());
        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        intent.putExtra(destinationType, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startExternalActivity(context, intent);
    }

    @Override
    public void launchAddFavoriteContactListActivity(Context context) {
        final Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.putExtra(ACTION, ACTION_ADD_FAVORITE);
        intent.setPackage(Factory.get().getApplicationContext().getPackageName());
        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);

        // Bkav TienNAb: fix loi khong start duoc AddFavoriteContactListActivity khi nang target version len 28
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startExternalActivity(context, intent);
    }

    @Override
    public void launchBtalkPermissionCheckActivity(Context context) {
        final Intent intent = new Intent(context, BtalkPermissionCheckActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void launchConversationActivityNewTask(Context context, String conversationId) {
        final Intent intent = getConversationActivityIntent(context, conversationId, null,
                false /* withCustomTransition */);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FLAG_SEND_TO, 1); // Bkav HuyNQN them co cho app ben ngoai goi toi tin nhan
        context.startActivity(intent);
    }

    //Bkav QuangNDb thuc hien 1 cuoc goi
    @Override
    public void makeACall(Context context, FragmentManager fragmentManager, String number) {
        PhoneAccountHandle handle = TelecomUtil.getDefaultOutgoingPhoneAccount(context,
                PhoneAccount.SCHEME_TEL);
        if (handle == null && BtalkCallLogCache.getCallLogCache(context).isHasSimOnAllSlot()) {
            DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(number);
            dialogChooseSimFragment.show(fragmentManager, "chooseSim");
        }else {
            final Intent intentCall = CallUtil.getCallIntent(number);
            intentCall.putExtra("Cdma_Supp", true);
            DialerUtils.sendBroadcastCount(context, DialerUtils.convertTabSeletedForCall(BtalkActivity.TAB_SELECT), 0);
            DialerUtils.startActivityWithErrorToast(context, intentCall);
        }
    }

    @Override
    public void showListEsimLongClick(Context context, FragmentManager fragmentManager, String number) {
        if(BtalkCallLogCache.getCallLogCache(context).isHasSimOnAllSlot() || ESimUtils.isMultiProfile()) {
            DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(number);
            dialogChooseSimFragment.show(fragmentManager, "chooseSim");
        }else { // Bkav HuyNQN neu 1 sim thuc hien goi luon
            final Intent intentCall = CallUtil.getCallIntent(number);
            intentCall.putExtra("Cdma_Supp", true);
            DialerUtils.startActivityWithErrorToast(context, intentCall);
        }
    }

    @Override
    public void makeACall(Context context, FragmentManager fragmentManager, Intent intent) {
        PhoneAccountHandle handle = TelecomUtil.getDefaultOutgoingPhoneAccount(context,
                PhoneAccount.SCHEME_TEL);
        if (handle == null && BtalkCallLogCache.getCallLogCache(context).isHasSimOnAllSlot()) {
            DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(intent);
            dialogChooseSimFragment.show(fragmentManager, "chooseSim");
        }else {
            DialerUtils.sendBroadcastCount(context, DialerUtils.convertTabSeletedForCall(BtalkActivity.TAB_SELECT), 0);
            DialerUtils.startActivityWithErrorToast(context, intent);
        }
    }

    @Override
    public void requestFakeCall(String number, Fragment fragment, int simIndex) {
        Intent intent = new Intent("bkav.android.action.INCALLFAKE");
        intent.setComponent(new ComponentName("com.android.dialer", "com.android.incallui.customizebkav.incall.IncallActivityFake"));
        intent.putExtra("number", number);
        intent.putExtra("index", simIndex);
        fragment.startActivityForResult(intent, 111);
    }

    @Override
    public void launchBtalkQuickResponseEditActivity(Context context) {
        final Intent intent = new Intent(context, BtalkQuickResponseEditActivity.class);
        context.startActivity(intent);
    }
}
