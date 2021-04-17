package bkav.android.btalk.messaging.ui.cutomview;

import android.content.Context;
import android.util.AttributeSet;

import com.android.messaging.datamodel.data.SubscriptionListData;

/**
 * Created by quangnd on 06/09/2017.
 */

public class BtalkPickerComposeMessageView extends BtalkComposeMessageView {

    public BtalkPickerComposeMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected SubscriptionListData.SubscriptionListEntry getSelfSubscriptionListEntry() {
        String selfId = mBinding.getData().getSelfId();
        return (selfId == null || (selfId.equals("-1"))) ?
                mConversationDataModel.getData().getSubscriptionEntryForSelfParticipant()
                : super.getSelfSubscriptionListEntry();
    }

    @Override
    protected boolean getConditionOldSelfId(String oldSelfId) {
        return false;
    }


}
