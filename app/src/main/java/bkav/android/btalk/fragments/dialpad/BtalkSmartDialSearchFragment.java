package bkav.android.btalk.fragments.dialpad;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.android.contacts.common.CallUtil;
import com.android.contacts.common.list.ContactEntryListAdapter;
import com.android.contacts.common.list.PhoneNumberListAdapter;
import com.android.dialer.calllog.PhoneAccountUtils;
import com.android.dialer.list.SmartDialSearchFragment;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.TelecomUtil;
import com.android.messaging.util.PhoneUtils;

import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.esim.ISimProfile;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.esim.dialog_choose_sim.DialogChooseSimFragment;
import bkav.android.btalk.esim.provider.ESimDbController;
import bkav.android.btalk.mutilsim.SimUltil;
import bkav.android.btalk.suggestmagic.BtalkDialerDatabaseHelper;
import bkav.android.btalk.suggestmagic.SuggestViewHolder;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by trungth on 06/05/2017.
 */

public class BtalkSmartDialSearchFragment extends SmartDialSearchFragment {

    public BtalkSmartDialSearchFragment() {
        super();
        setQuickContactEnabled(true);
    }

    private int mSimDefault;

    private boolean mIsCheckEsim;

    private List<ISimProfile> mListEsim;

    private List<PhoneAccountHandle> subscriptionAccountHandles;
    // Bkav HienDTk: item menu dialog chon sim
    private static int ITEM_MENU_DIALOG_CHOOSE_SIM = 0;
    // Bkav HienDTk: profile cua sim (ca sim thuong va esim)
    private static int PROFILE_SIM = 3;
    // Bkav HienDTk: tra ve so sim dang duoc lap tren khay sim
    private static int SUBSCRIPTION_PHONE_ACCOUNT = 2;

    @Override
    protected int getIdContentLayout() {
        return R.layout.btalk_smartdial_contact_list;
    }

    @Override
    protected int getColorItemBackground() {
        return R.color.btalk_transparent_view;
    }

    /**
     * Anhdts
     * Updates the position and padding of the search fragment, depending on whether the dialpad is
     * shown. This can be optionally animated.
     * hieu ung luc keo cham vao list contact
     * khong dung nua
     *
     * @param animate
     */
    @Override
    public void updatePosition(boolean animate) {
//        // Use negative shadow height instead of 0 to account for the 9-patch's shadow.
//        int startTranslationValue = 0;
//        int endTranslationValue = 0;
//        // Prevents ListView from being translated down after a rotation when the ActionBar is up.
//        // If the dialpad will be shown, then this animation involves sliding the list up.
//        final boolean slideUp = mActivity.isDialpadShown();
//
//        if (animate || mActivity.isActionBarShowing()) {
//            endTranslationValue = slideUp ? 0 : mShadowHeight;
//        }
//        if (animate) {
//            Interpolator interpolator = slideUp ? AnimUtils.EASE_IN : AnimUtils.EASE_OUT;
//            int duration = slideUp ? mShowDialpadDuration : mHideDialpadDuration;
//            getView().setTranslationY(startTranslationValue);
//            getView().animate()
//                    .translationY(endTranslationValue)
//                    .setInterpolator(interpolator)
//                    .setDuration(duration)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationStart(Animator animation) {
//                            if (!slideUp) {
//                                resizeListView();
//                            }
//                        }
//
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            if (slideUp) {
//                                resizeListView();
//                            }
//                        }
//                    });
//
//        } else {
//            getView().setTranslationY(endTranslationValue);
//            resizeListView();
//            // There is padding which should only be applied when the dialpad is not shown.
//            int paddingTop = mActivity.isDialpadShown() ? 0 : mPaddingTop;
//            final ListView listView = getListView();
//            listView.setPaddingRelative(
//                    listView.getPaddingStart(),
//                    paddingTop,
//                    listView.getPaddingEnd(),
//                    listView.getPaddingBottom());
//        }
//        ListView listView = getListView();
//        listView.setPaddingRelative(
//                listView.getPaddingStart(),
//                0,
//                listView.getPaddingEnd(),
//                listView.getPaddingBottom());
        resizeListView();
        ListView listView = getListView();
        listView.setPaddingRelative(
                listView.getPaddingStart(),
                0,
                listView.getPaddingEnd(),
                listView.getPaddingBottom());
    }

    /**
     * Anhdts custom lai adapter
     */
    @Override
    protected ContactEntryListAdapter createListAdapter() {
        BtalkSmartNumberListAdapter adapter = new BtalkSmartNumberListAdapter(getActivity());
        adapter.setUseCallableUri(super.usesCallableUri());
        adapter.setQuickContactEnabled(true);
        // Set adapter's query string to restore previous instance state.
        adapter.setQueryString(getQueryString());
        adapter.setListener(this);
        return adapter;
    }

    /**
     * Anhdts
     */
    public void resizeList() {
        if (mListView != null) {
            resizeListView();
        }
    }

    /**
     * TrungTH check xem adapter co data hay chua
     */
    public boolean hasData() {
        return getAdapter() != null && !getAdapter().isEmpty();
    }

    /**
     * Anhdts gan lai HostInterface
     */
    public void resetActivity(Activity activity) {
        mActivity = (HostInterface) activity;
    }

    /**
     * Anhdts action chon contact de goi
     */
    @Override
    protected void callNumber(int position, boolean isVideoCall) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        if (cursor != null) {
            String name = cursor.getString(PhoneNumberListAdapter.PhoneQuery.DISPLAY_NAME);
            if (!TextUtils.isEmpty(name)) {
                BtalkDialerDatabaseHelper.getInstance(getActivity().getApplicationContext()).setRecentCall(name);
            }
        }
        super.callNumber(position, isVideoCall);
    }

    /**
     * Anhdts custom lai do so trong callog khong co id
     */
    @Override
    protected void onItemClick(int position, long id) {
        if (id > 100000) {
            Cursor cursor = (Cursor) mAdapter.getItem(position);
            String number = cursor.getString(PhoneNumberListAdapter.PhoneQuery.PHONE_NUMBER);
            final Intent intent = CallUtil.getCallIntent(number);
            BtalkDialerDatabaseHelper.getInstance(getActivity().getApplicationContext()).
                    setRecentCall(cursor.getString(PhoneNumberListAdapter.PhoneQuery.DISPLAY_NAME));
            DialerUtils.startActivityWithErrorToast(getActivity(), intent);
        } else {
//            super.onItemClick(position, id);
            // Bkav HuyNQN fix loi BOS-2706 start
            // Kiem tra neu may lap mot sim thi se thuc hien goi luon
            if(PhoneUtils.getDefault().getActiveSubscriptionCount() == 1){
                String number = getPhoneNumber(position);
                final Intent intent = CallUtil.getCallIntent(number);
                BtalkDialerDatabaseHelper.getInstance(mContext);
                DialerUtils.sendBroadcastCount(mContext, DialerUtils.convertTabSeletedForCall(BtalkActivity.TAB_SELECT), 0);
                DialerUtils.startActivityWithErrorToast(getActivity(), intent);
                // Bkav HuyNQN fix loi BOS-2706 end
            }else if ((BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot() || ESimUtils.isMultiProfile()) && getSimDefault() == -1) {
                // Bkav HuyNQN Dung logic goi theo esim
                String number = getPhoneNumber(position);
                DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(number);
                dialogChooseSimFragment.show(getFragmentManager(), "chooseSim");

            } else {
                super.onItemClick(position, id);

            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private int getSimDefault() {
        PhoneAccountHandle handleDefault = TelecomUtil.getDefaultOutgoingPhoneAccount(getContext(), PhoneAccount.SCHEME_TEL);
        return SimUltil.getSlotSimByAccount(getContext(), handleDefault);
    }

    @Override
    protected String getPhoneNumber(int position) {
        return super.getPhoneNumber(position);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        int adjPosition = position - mListView.getHeaderViewsCount();

        if (adjPosition >= 0) {
            if (view instanceof BtalkContactListItemViewSmart) {
                showPopupMenu((BtalkContactListItemViewSmart) view);
            }
            return true;
        }
        return false;
    }

    private PopupMenu mPopupMenu;

    private PopupMenu.OnMenuItemClickListener mMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(mNumber);
            if (item.getItemId() == R.id.menu_call_sim1) {
                if (mSimDefault != -1) {
                    // Bkav HienDTk: truong hop lap
                    if (mIsCheckEsim && subscriptionAccountHandles.size() == SUBSCRIPTION_PHONE_ACCOUNT && mListEsim.size() >= PROFILE_SIM) {
                        dialogChooseSimFragment.setSlotDefault(getSimDefault());
                        dialogChooseSimFragment.show(getFragmentManager(), "chooseSim");
                    } else if (mIsCheckEsim && mListEsim.size() >= 2) { // Bkav HienDTk: truong hop lap esim co nhieu profile thi hien thi dialog choose sim
                        dialogChooseSimFragment.setSlotDefault(getSimDefault());
                        dialogChooseSimFragment.show(getFragmentManager(), "chooseSim");
                    } else {
                        makeCall();
                    }
                } else {
                    dialogChooseSimFragment.setSlotDefault(getSimDefault());
                    dialogChooseSimFragment.show(getFragmentManager(), "chooseSim");
                }
            }
            if (item.getItemId() == R.id.action_copy) { //HienDTk: item copy sdt

                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(String.valueOf(PhoneNumberListAdapter.PhoneQuery.DISPLAY_NAME), mNumber);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mContext, R.string.copy_done_suggest, Toast.LENGTH_SHORT).show();
                /*else {

                SimUltil.callWithSlotSim(getActivity(), subscriptionAccountHandles.get(1).getId(), mNumber);

            }*/
            } else if (item.getItemId() == R.id.action_share) { //HienDTk: item share sdt
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, SuggestViewHolder.getContentForward(mNumber, String.valueOf(PhoneNumberListAdapter.PhoneQuery.DISPLAY_NAME)));
                intent.setType("text/plain");
                Intent chooser = Intent.createChooser(
                        intent, mContext.getString(R.string.action_share_contact_via));
                DialerUtils.startActivityWithErrorToast(mContext, chooser);

            }
            mPopupMenu.dismiss();
            return false;
        }
    };

    private String mNumber;

    void showPopupMenu(BtalkContactListItemViewSmart view) {
        mNumber = view.getDataView().getText().toString();
        // Bkav HienDTk: lay ra toan bo profile sim trong may gom ca sim thuong va eSim
        mListEsim = ESimDbController.getAllSim();
        mIsCheckEsim = ESimDbController.isEsimExist();
        if (TextUtils.isEmpty(mNumber)) {
            return;
        }
        subscriptionAccountHandles =
                PhoneAccountUtils.getSubscriptionPhoneAccounts(mContext);

        mPopupMenu = new PopupMenu(mContext, view, Gravity.CENTER_HORIZONTAL);
        mPopupMenu.inflate(R.menu.menu_long_click_smart);
        Menu menu = mPopupMenu.getMenu();
        mSimDefault = SimUltil.getDefaultSimCell(mContext);
        // Bkav HienDTk: truong hop co 1 sim
        if (subscriptionAccountHandles != null && subscriptionAccountHandles.size() <= 1 && !mIsCheckEsim) {

            menu.removeItem(R.id.menu_call_sim1);
        } else {
            if (mSimDefault != -1) {
                // Bkav HienDTk: truong hop lap esim co nhieu profile
                if (mIsCheckEsim && subscriptionAccountHandles.size() == SUBSCRIPTION_PHONE_ACCOUNT && mListEsim.size() >= PROFILE_SIM) {
                    menu.getItem(ITEM_MENU_DIALOG_CHOOSE_SIM).setTitle(mContext.getString(R.string.action_call_by_sim_other));
                } else if (mIsCheckEsim && mListEsim.size() >= 2) { // Bkav HienDTk: truong hop lap esim co nhieu profile thi hien thi dialog chon sim
                    menu.getItem(ITEM_MENU_DIALOG_CHOOSE_SIM).setTitle(mContext.getString(R.string.action_call_by_sim_other));
                } else {
                    menu.getItem(ITEM_MENU_DIALOG_CHOOSE_SIM).setTitle(mContext.getString(R.string.action_call_by_sim, String.valueOf(2 - mSimDefault),
                            SimUltil.getNotDefaultSimName(mContext)));
                }

            } else {
                menu.getItem(ITEM_MENU_DIALOG_CHOOSE_SIM).setTitle(mContext.getString(R.string.action_call_by_sim_other));
            }
        }
        mPopupMenu.setOnMenuItemClickListener(mMenuItemClickListener);
        mPopupMenu.setGravity(Gravity.END);
        mPopupMenu.show();

    }

    // Bkav HienDTk: lay slot sim mac dinh
    private static final int SLOT_INDEX_SIM_1 = 0;
    private static final int SLOT_INDEX_SIM_2 = 1;

    // Bkav HienDTk: thuc hien cuoc goi
    private void makeCall() {
        String callId = "";
        if (BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot()) {
            if (mSimDefault == SLOT_INDEX_SIM_1) {
                // Bkav HienDTk: neu mac dinh la sim 1 thi lay id sim 2 de thuc hien cuoc goi
                callId = subscriptionAccountHandles.get(SLOT_INDEX_SIM_2).getId();
            } else if (mSimDefault == SLOT_INDEX_SIM_2) {
                // Bkav HienDTk: neu mac dinh la sim 2 thi lay id sim 1 de thuc hien cuoc goi
                callId = subscriptionAccountHandles.get(SLOT_INDEX_SIM_1).getId();
            }
        } else {
            callId = subscriptionAccountHandles.get(SLOT_INDEX_SIM_1).getId();
        }

        SimUltil.callWithSlotSim(mContext, callId, mNumber);
    }
}
