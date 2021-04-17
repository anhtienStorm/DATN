package bkav.android.btalk.calllog.recoder;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.android.dialer.calllog.calllogcache.CallLogCache;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.adapter.BtalkPhoneCallDetailsHelper;
import bkav.android.btalk.calllog.adapter.BtalkPhoneCallDetailsViews;

/**
 * HuyNQn de sua lai cac ham minh can khoi tao lai tuong ung voi cac lop
 */

public class BtalkPhoneCallRecoderDetailsHelper extends BtalkPhoneCallDetailsHelper {

    BtalkPhoneCallRecoderDetailsHelper(Context mContext, Resources resources, CallLogCache mCallLogCache) {
        super(mContext, resources, mCallLogCache);
    }

    // Bkav HuyNQN thay doi mau chu thanh cam
    @Override
    protected CharSequence getStyle(int start, int end, CharSequence nameText) {
        SpannableStringBuilder sb = new SpannableStringBuilder(nameText);
        ForegroundColorSpan span = new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.btalk_orange_color));
        sb.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return sb;
    }
}
