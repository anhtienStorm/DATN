package bkav.android.btalk.contacts;

import com.android.contacts.common.vcard.ExportRequest;
import com.android.contacts.common.vcard.VCardService;


/**
 * AnhNDd: service de export contacts
 */
public class BtalkVCardService extends VCardService {
    @Override
    public boolean tryExecuteHandleExportRequest(ExportRequest request) {
        return tryExecute(new BtalkExportProcessor(this, request, mCurrentJobId, mCallingActivity));
    }
}
