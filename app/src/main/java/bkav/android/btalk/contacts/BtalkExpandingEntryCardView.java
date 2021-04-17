package bkav.android.btalk.contacts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.android.contacts.quickcontact.ExpandingEntryCardView;
import com.android.dialer.util.TelecomUtil;

import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.recoder.RecorderAudioAttachmentView;
import bkav.android.btalk.calllog.recoder.RecorderService;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.esim.ISimProfile;
import bkav.android.btalk.esim.provider.ESimDbController;
import bkav.android.btalk.suggestmagic.ImageCallButton;
import bkav.android.btalk.suggestmagic.SuggestPopup;

public class BtalkExpandingEntryCardView extends ExpandingEntryCardView {

    RecorderService mService;

    public void setService(RecorderService mService) {
        this.mService = mService;
    }

    public BtalkExpandingEntryCardView(Context context) {
        super(context);
    }

    public BtalkExpandingEntryCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Anhdts doi lai layout de doi lai icon goi dien nhan tin
     */
    @Override
    public int getLayoutCardView() {
        return R.layout.btalk_expanding_entry_card_item;
    }

    @Override
    protected void updateSimIcon(ImageView icon, boolean mShouldApplyColor) {
        if (icon == null) {
            return;
        }
        if (icon instanceof ImageCallButton) {
            ImageCallButton mButtonCall = (ImageCallButton) icon;
            mButtonCall.setIsQuickContact();
            if (mShouldApplyColor) {
                mButtonCall.setShowSim(false, null);
            } else {
                List<ISimProfile> listEsim = ESimDbController.getAllSim();
                if (BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot() || listEsim.size() > 1) { /*xu ly ca truong hop lap 1 esim co nhieu profile*/
                    PhoneAccountHandle handle;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        handle = TelecomUtil.getDefaultOutgoingPhoneAccount(mContext,
                                PhoneAccount.SCHEME_TEL);
                        final Drawable profileDrawable = BtalkCallLogCache.
                                getCallLogCache(mContext).getAccountIcon(handle);
                        if (handle != null && profileDrawable != null) {
                            Bitmap iconSimFloat = SuggestPopup.convertDrawableToBitmap(profileDrawable, getResources().getDimensionPixelSize(R.dimen.size_icon_sim_quickcontact));
                            mButtonCall.setShowSim(true, iconSimFloat);
                        } else {
                            mButtonCall.setShowSim(false, null);
                        }
                    }
                } else {
                    mButtonCall.setShowSim(false, null);
                }
            }
        }
    }

    // Bkav HuyNQN Xu ly RecorderAudioAttachmentView
    @Override
    public void onSetUpPlayAudioView(EntryView view, String pathAudio) {
        RecorderAudioAttachmentView customViewPlayAudio = view.findViewById(R.id.recoder_audio_attachment_framelayout);
        if (pathAudio!=null && !pathAudio.equals("")){
            customViewPlayAudio.setVisibility(View.VISIBLE);
            customViewPlayAudio.setDataResource(pathAudio);
            customViewPlayAudio.setService(mService);
        }
    }
}
