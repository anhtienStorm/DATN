package bkav.android.btalk.calllog.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.CallLog;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.android.dialer.PhoneCallDetails;
import com.android.dialer.calllog.PhoneCallDetailsHelper;
import com.android.dialer.calllog.PhoneCallDetailsViews;
import com.android.dialer.calllog.calllogcache.CallLogCache;
import com.android.dialer.util.AppCompatConstants;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.messaging.util.BtalkTypefaces;
import bkav.android.btalk.utility.DateUtil;

/**
 * Created by anhdt on 31/03/2017.
 * bind du lieu cuoc goi cho view {@link PhoneCallDetails} trong lop {@link BtalkCallLogAdapter}
 * custom {@link PhoneCallDetailsHelper}
 */

// Bkav HuyNQN Them key puplic cho class
public class BtalkPhoneCallDetailsHelper extends PhoneCallDetailsHelper {
    protected BtalkPhoneCallDetailsHelper(Context mContext, Resources resources, CallLogCache mCallLogCache) {
        super(mContext, resources, mCallLogCache);
    }

    // Anhdts custom hien thi thoi gian goi - rom goc dang de hien thi so cuoc goi va ngay
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void setDetailText(PhoneCallDetailsViews views, Integer callCount,
                                 PhoneCallDetails details) {
        // Combine the count (if present) and the date.
        // Bkav TrungTh doi sang calender nay
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(details.date);

        CharSequence dateText = DateUtil.get().getCallTime(mContext, details.date, System.currentTimeMillis());
        if (details.callTypes[0] == CallLog.Calls.VOICEMAIL_TYPE && details.duration > 0) {
            views.callLocationAndDate.setText(mResources.getString(
                    com.android.dialer.R.string.voicemailCallLogDateTimeFormatWithDuration, dateText,
                    getVoicemailDuration(details)));
            //Bkav QuangNDb them view khi co spam thi moi hien len
            views.dateIfSpamExist.setText(mResources.getString(
                    com.android.dialer.R.string.voicemailCallLogDateTimeFormatWithDuration, dateText,
                    getVoicemailDuration(details)));
        } else {
            //Bkav QuangNDb them view khi co spam thi moi hien len
            views.callLocationAndDate.setText(dateText);
            views.dateIfSpamExist.setText(dateText);
        }

    }

    // Anhdts them truong so dien thoai
    //Bkav QuangNDb hien thi text spam khi bms insert vao va item khong phai la lien he duoc luu tron danh ba
    @Override
    protected void setNumberOrSpamView(PhoneCallDetailsViews view, PhoneCallDetails details) {
        if (details.spamContent != null && !details.spamContent.isEmpty()
                && TextUtils.isEmpty(details.getPreferredName())) {
            view.numberView.setText(details.spamContent);
            view.dateIfSpamExist.setVisibility(View.VISIBLE);
            view.callLocationAndDate.setVisibility(View.GONE);
            view.numberDateSpace.setVisibility(View.GONE);
        }else {
            view.numberView.setText(details.displayNumber);
            view.dateIfSpamExist.setVisibility(View.GONE);
            view.callLocationAndDate.setVisibility(View.VISIBLE);
            view.numberDateSpace.setVisibility(View.VISIBLE);
        }

    }

    // Anhdts add call count after name
    @Override
    protected void setCallCount(TextView nameView, Integer callCount) {
        if (callCount > 1) {
            nameView.append(" (" + callCount + ")");
        }
    }

    // Anhdts custom view so va thoi gian goi
    @Override
    protected boolean customViewSecondary(PhoneCallDetailsViews views, PhoneCallDetails details) {
        // Check la cuoc goi nho hoac bi tu choi thi duoc xep vao cuoc goi nho
        views.voicemailTranscriptionView.setTextColor(ContextCompat.getColor(mContext, (details.callTypes[0] == CallLog.Calls.MISSED_TYPE || details.callTypes[0] == AppCompatConstants.MISSED_IMS_TYPE)
                ? R.color.btalk_call_log_primary_color_miss_call : R.color.btalk_call_log_primary_color));

        if (details.callTypes[0] == CallLog.Calls.MISSED_TYPE
                || details.callTypes[0] == AppCompatConstants.MISSED_IMS_TYPE) {
            views.nameView.setTextColor(ContextCompat.getColor(mContext,
                    R.color.btalk_call_log_primary_color_miss_call));
            if (!details.isRead) {
                views.nameView.setTypeface(BtalkTypefaces.sRobotoBoldFont);
                if (mContext instanceof BtalkActivity) {
//                    ((BtalkActivity) mContext).setReadMissCall();
                }
            } else {
                views.nameView.setTypeface(Typeface.SANS_SERIF);
            }
        } else {
            views.nameView.setTextColor(ContextCompat.getColor(mContext,
                    R.color.btalk_call_log_primary_color));
            views.nameView.setTypeface(Typeface.SANS_SERIF);
        }
        return true;
    }

    @Override
    protected void setBkavTypeface(PhoneCallDetailsViews views) {
        // Bkav QuangNDb them set typeface cho text date
        // views.nameView.setTypeface(BtalkTypefaces.sRobotoLightFont);
        views.callLocationAndDate.setTypeface(BtalkTypefaces.sRobotoLightFont);
        //HienDTk: set kieu text cho thoi gian neu co spam de dong nhat mau date time
        views.dateIfSpamExist.setTypeface(BtalkTypefaces.sRobotoLightFont);
    }

    @Override
    protected CharSequence setTimeCallLog(PhoneCallDetails details) {
        return  "";
    }

    @Override
    protected void addCallDate(PhoneCallDetails details) {

    }
}
