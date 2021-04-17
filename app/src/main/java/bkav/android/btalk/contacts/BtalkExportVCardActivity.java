package bkav.android.btalk.contacts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.android.contacts.common.vcard.ExportRequest;
import com.android.contacts.common.vcard.ExportVCardActivity;


/**
 * AnhNDd: activity xu ly thong tin cho service
 */
public class BtalkExportVCardActivity extends ExportVCardActivity {
    //AnhNDd: string lựa chọn các contact để export.
    private String mSelExport = "";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mSelExport = getIntent().getExtras().getString(BtalkContactUtils.SEL_EXPORT);
    }

    @Override
    public ExportRequest createExportRequest(Uri targetFileName) {
        final BtalkExportRequest request = new BtalkExportRequest(targetFileName,mSelExport);
        return request;
    }

    @Override
    public Intent createIntentService() {
        Intent intent = new Intent(this, BtalkVCardService.class);
        return intent;
    }
}
