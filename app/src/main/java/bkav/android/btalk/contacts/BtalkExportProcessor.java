package bkav.android.btalk.contacts;

import com.android.contacts.common.vcard.ExportProcessor;
import com.android.contacts.common.vcard.ExportRequest;
import com.android.contacts.common.vcard.VCardService;
import com.android.vcard.VCardComposer;


/**
 * AnhNDd: class xu ly export contacts
 */
public class BtalkExportProcessor extends ExportProcessor {
    public BtalkExportProcessor(VCardService service, ExportRequest exportRequest, int jobId, String callingActivity) {
        super(service, exportRequest, jobId, callingActivity);
    }

    @Override
    public VCardComposer createComposerVCard(int vcardType) {
        BtalkExportRequest request = (BtalkExportRequest) mExportRequest;
        return new BtalkVCardComposer(mService, vcardType, true, request.selExport);
    }
}
