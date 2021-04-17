package bkav.android.btalk.contacts;

import android.net.Uri;

import com.android.contacts.common.vcard.ExportRequest;


/**
 * AnhNDd: class chứa thông tin các contacts để export
 */
public class BtalkExportRequest extends ExportRequest {
    public final String selExport;

    public BtalkExportRequest(Uri destUri, String selExport) {
        super(destUri);
        this.selExport = selExport;
    }
}
