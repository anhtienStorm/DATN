package bkav.android.btalk.calllog;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.android.dialer.CallDetailActivity;
import com.android.messaging.ui.UIIntents;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.bmsblocked.BmsUtils;

/**
 * Created by anhdt on 14/07/2017.
 * 
 */

public class BtalkCallDetailActivity extends CallDetailActivity {

    /**
     * Anhdts su dung ham chinh sua so truoc khi goi
     * return true: sua dung ham cu, false ham moi
     */
    @Override
    protected boolean useDefaultMethod(String number) {
        Bundle data = new Bundle();
        data.putString(BtalkActivity.ARGUMENT_NUMBER, number);
        Intent intent = new Intent(BtalkActivity.ACTION_FIX_BEFORE_CALL);
        intent.putExtras(data);
        startActivity(intent);
        return false;
    }

    /**
     * Anhdts
     */
    @Override
    protected Drawable getDrawable() {
        Drawable iconDelete = ContextCompat.getDrawable(this, R.drawable.ic_delete_24dp).mutate();
        iconDelete.setColorFilter(ContextCompat.getColor(this, R.color.btalk_ab_text_and_icon_normal_color), PorterDuff.Mode.MULTIPLY);
        return iconDelete;
    }

    @Override
    protected void makeCallNumber() {
        if (TextUtils.isEmpty(mNumber)) {
            return;
        }
        // Bkav HuyNQN su dung logic chon sim moi
        UIIntents.get().makeACall(getApplicationContext(), getFragmentManager(), mNumber);
    }

    @Override
    protected void checkCallsBlocks() {
        if(BmsUtils.isHasNumberBlocks(getApplicationContext(), mNumber)){
            updateUnblocksTitle();
        }else {
            updateBlocksTitle();
        }

    }

    @Override
    protected void showDiaLogCallsBlocksWithBMS() {
        if(BmsUtils.isHasNumberBlocks(getApplicationContext(), mNumber)){
            BmsUtils.showDialogUnblocked(getApplicationContext(), mNumber);
        }else {
            BmsUtils.showDialogAddCallLogBlocked(getApplicationContext(), mNumber);
        }
    }
}
