package bkav.android.btalk.contacts;

import android.content.Context;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.model.RawContactDelta;
import com.android.contacts.common.model.RawContactDeltaList;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.common.util.AccountsListAdapter;
import com.android.contacts.editor.CompactKindSectionView;
import com.android.contacts.editor.CompactRawContactsEditorView;
import com.android.contacts.editor.ContactEditorUtils;
import com.android.contacts.util.UiClosables;

import bkav.android.btalk.R;
import bkav.android.btalk.utility.Config;

/**
 * Anhdts
 */

public class BtalkCompactRawContactsEditorView extends CompactRawContactsEditorView implements BtalkAccountsListAdapter.CheckDefaultListener{

    public BtalkCompactRawContactsEditorView(Context context) {
        super(context);
    }

    public BtalkCompactRawContactsEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public CompactKindSectionView hookToInflateKindSectionView(ViewGroup viewGroup) {
        final BtalkCompactKindSectionView kindSectionView = (BtalkCompactKindSectionView)
                mLayoutInflater.inflate(R.layout.btalk_compact_item_kind_section, viewGroup,
                        /* attachToRoot =*/ false);
        return kindSectionView;
    }

    @Override
    public void showAllKindSectionView(CompactKindSectionView kindSectionView) {
        BtalkCompactKindSectionView btalkCompactKindSectionView = (BtalkCompactKindSectionView) kindSectionView;
        btalkCompactKindSectionView.showAllStructEditorName();
    }

    // Anhdts them cac view
    protected void addBkavView(String mimeType, RawContactDeltaList rawContactDeltas) {
        if (ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                //Bkav QuangNDb neu khong phai luu tren sim moi hien thi len
                if (!isSaveOnSim(rawContactDeltas)) {
                    View viewRingTon = inflater.inflate(R.layout.btalk_layout_editor_view, null);
                    View viewRingMess = inflater.inflate(R.layout.btalk_layout_editor_view, null);
                    final View viewPriorSim = inflater.inflate(R.layout.btalk_layout_editor_view, null);
                    viewRingTon.setId(R.id.action_set_rington);
                    viewRingMess.setId(R.id.action_set_ringmess);
                    viewPriorSim.setId(R.id.action_set_default_sim);
                    inflateBkavView(R.string.label_ringtone, R.drawable.ic_ringtone,
                            R.string.default_info_title, viewRingTon);
                    inflateBkavView(R.string.label_ringtone_message, R.drawable.ic_mess_rington,
                            R.string.default_info_title, viewRingMess);
//                inflateBkavView(R.string.label_priority_sim, R.drawable.icon_sim,
//                        R.string.default_info_title, viewPriorSim);
                    // Bkav HienDTk: fix loi: Không mở được giao diện lựa chọn Quản lý file/Bộ nhớ phương tiện khi chạm vào text Nhạc chuông/Âm báo tin nhắn trên giao diện Thêm mới liên hệ => BOS-2595- Start
                    viewRingTon.findViewById(R.id.container_editor).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mListener.pickRingTon(true);
                        }
                    });

                    viewRingMess.findViewById(R.id.container_editor).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mListener.pickRingTon(false);
                        }
                    });
                }
                // Bkav HienDTk: fix loi: Không mở được giao diện lựa chọn Quản lý file/Bộ nhớ phương tiện khi chạm vào text Nhạc chuông/Âm báo tin nhắn trên giao diện Thêm mới liên hệ => BOS-2595- End



//                if (!BtalkCallLogCache.getCallLogCache(getContext().getApplicationContext()).getIsMutilSim()) {
//                    viewPriorSim.setVisibility(GONE);
//                } else {
//                    viewPriorSim.findViewById(R.id.container_action).setOnClickListener(new OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            PopupWindow popupWindow = new PopupWindow(getContext());
//                            LinearLayout contentView = new LinearLayout(getContext());
//                            FrameLayout.LayoutParams params = new
//                                    FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
////                            params.width = getResources().getDimensionPixelOffset(R.dimen.width_row_sim_choose_default);
//                            contentView.setLayoutParams(params);
//                            contentView.setOrientation(LinearLayout.VERTICAL);
//                            List<PhoneAccountHandle> subscriptionAccountHandles =
//                                    PhoneAccountUtils.getSubscriptionPhoneAccounts(getContext());
//                            if (subscriptionAccountHandles != null) {
//                                for (PhoneAccountHandle handle : subscriptionAccountHandles) {
//                                    View sim = LayoutInflater.
//                                            from(getContext()).inflate(R.layout.layout_choose_sim_default, null);
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                        ((TextView) sim.findViewById(R.id.sim_name)).setText(SimUltil.getSimName(getContext(), handle));
//                                        ((ImageView) sim.findViewById(R.id.sim_icon)).setImageDrawable(BtalkCallLogCache.
//                                                getCallLogCache(getContext()).getAccountIcon(handle));
//                                        sim.setContentDescription(handle.getId());
//                                        sim.setOnClickListener(simListener);
//                                    }
//                                    contentView.addView(sim);
//                                }
//                            }
//                            popupWindow.setOutsideTouchable(true);
//                            popupWindow.setFocusable(true);
//                            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                            popupWindow.setContentView(contentView);
//                            int height = getResources().getDisplayMetrics().heightPixels;
//
//                            Rect rect = new Rect();
//                            viewPriorSim.findViewById(R.id.container_action).getGlobalVisibleRect(rect);
//                            if (rect.top < height * 2 / 3) {
//                                popupWindow.showAsDropDown(viewPriorSim.findViewById(R.id.container_action)
//                                        , getResources().getDimensionPixelOffset(R.dimen.width_row_sim_choose_default), -50);
//                            } else {
//                                popupWindow.showAsDropDown(viewPriorSim.findViewById(R.id.container_action)
//                                        , getResources().getDimensionPixelOffset(R.dimen.width_row_sim_choose_default),
//                                        - 3 * getResources().getDimensionPixelOffset(R.dimen.height_sim_icon));
//                            }
//                        }
//                    });
//                }
            }
        }
    }

    private OnClickListener simListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String idSim = String.valueOf(v.getContentDescription());
            Toast.makeText(getContext(), "choose Sim " + idSim, Toast.LENGTH_LONG).show();
        }
    };

    private void inflateBkavView(int label, int idResource, int value, View view) {
        ((TextView) view.findViewById(R.id.label_editor_view)).
                setText(label);
        ((ImageView) view.findViewById(R.id.kind_icon)).setImageResource(idResource);
        mKindSectionViews.addView(view);
    }

    @Override
    protected void showAllFields() {
        // Stop hiding empty editors and allow the user to enter values for all kinds now
        for (int i = 0; i < mKindSectionViews.getChildCount(); i++) {
            if (mKindSectionViews.getChildAt(i) instanceof CompactKindSectionView) {
                final CompactKindSectionView kindSectionView =
                        (CompactKindSectionView) mKindSectionViews.getChildAt(i);
                kindSectionView.setHideWhenEmpty(false);
                kindSectionView.updateEmptyEditors(/* shouldAnimate =*/ true);

                showAllKindSectionView(kindSectionView);
            }
        }
        mIsExpanded = true;

        // Hide the more fields button
        mMoreFields.setVisibility(View.GONE);
    }

    protected boolean mIsSetDefault = false;

    @Override
    protected void addAccountSelector(Pair<String,String> accountInfo,
                                      final RawContactDelta rawContactDelta) {
        super.addAccountSelector(accountInfo, rawContactDelta);
        mAccountSelectorContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ListPopupWindow popup = new ListPopupWindow(getContext(), null);
                final BtalkAccountsListAdapter adapter =
                        new BtalkAccountsListAdapter(getContext(),
                                AccountsListAdapter.AccountListFilter.ACCOUNTS_CONTACT_WRITABLE,
                                mPrimaryAccount, BtalkCompactRawContactsEditorView.this, mIsSetDefault);
                popup.setWidth(mAccountSelectorContainer.getWidth());
                popup.setAnchorView(mAccountSelectorContainer);
                popup.setAdapter(adapter);
                popup.setModal(true);
                popup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
                popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id) {
                        UiClosables.closeQuietly(popup);
                        AccountWithDataSet newAccount = adapter.getItem(position);
                        // Anhdts luu account mac dinh
                        if (adapter.isCheck()) {
                            ContactEditorUtils.getInstance(getContext()).saveDefaultAndAllAccounts(newAccount);
                        }
                        if (mListener != null && !mPrimaryAccount.equals(newAccount)) {
                            // TrungTH
                            newAccount = Config.changeAccountIfLocal(getContext(),newAccount);
                            mListener.onRebindEditorsForNewContact(
                                    rawContactDelta,
                                    mPrimaryAccount,
                                    newAccount); // TrungTH them vao
                        }
                    }
                });
                popup.show();
            }
        });
    }


    @Override
    public void setCheckDefault(boolean isCheck) {
        mIsSetDefault = isCheck;
    }

    public boolean isSetDefaultAccount() {
        return mIsSetDefault;
    }

    @Override
    protected void bkavAddAccountSelector(Pair<String, String> accountInfo, RawContactDelta rawContactDelta) {
        super.bkavAddAccountSelector(accountInfo, rawContactDelta);
        // Bkav HuyNQN tren ca ban VN va My deu xu ly cho phep luu vao bo nho may neu nhu khong co acc google
        if(Config.isMyanmar() || !TextUtils.isEmpty(accountInfo.second)&& TextUtils.isEmpty(rawContactDelta.getAccountType())){
            mAccountSelectorName.setVisibility(View.VISIBLE);
            mAccountSelectorName.setText(accountInfo.second);
            mPrimaryAccount = new AccountWithDataSet(getContext().getString(R.string.keep_local) , null, null);
        }else {
            mAccountSelectorName.setVisibility(View.GONE);
        }
    }
}
