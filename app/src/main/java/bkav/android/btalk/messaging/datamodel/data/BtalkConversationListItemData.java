package bkav.android.btalk.messaging.datamodel.data;

import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.util.Dates;

/**
 * Created by quangnd on 04/04/2017.
 * class custom lai ConversationListItemData cua source goc
 */

public class BtalkConversationListItemData extends ConversationListItemData {

    @Override
    public String getFormattedTimestamp() {
        return Dates.getBtalkConversationTimeString(mTimestamp).toString();
    }
}
