package bkav.android.btalk.messaging.ui.contact;

import android.content.Context;
import android.widget.Filter;

import com.android.messaging.ui.contact.ContactListItemView;
import com.android.messaging.ui.contact.ContactRecipientAdapter;

/**
 * Created by quangnd on 26/04/2017.
 */

public class BtalkContactRecipientAdapter extends ContactRecipientAdapter {

    public BtalkContactRecipientAdapter(Context context, ContactListItemView.HostInterface clivHost) {
        super(context, clivHost);
    }

    @Override
    public Filter getFilter() {
        return null;
    }
}
