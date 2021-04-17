package bkav.android.btalk.contacts;

import com.android.contacts.ContactSaveService;
import com.android.messaging.datamodel.ParticipantRefresh;

/**
 * Created by quangnd on 29/11/2017.
 * custom lai service save contact
 */

public class BtalkContactSaveService extends ContactSaveService {

    @Override
    protected void refreshParticipant(long contactId) {
        ParticipantRefresh.refreshParticipantsIfNeeded(String.valueOf(contactId));
    }
}
