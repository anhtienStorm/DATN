package bkav.android.btalk.contacts;

import android.content.Context;
import android.provider.ContactsContract;

import com.android.contacts.common.vcard.VCardService;
import com.android.vcard.VCardComposer;


/**
 * AnhNDd: class lay thong tin cua contacts khi export
 */
public class BtalkVCardComposer extends VCardComposer {

    private String mSelExport ;

    public BtalkVCardComposer(Context context) {
        super(context);
    }

    public BtalkVCardComposer(VCardService mService, int vcardType, boolean b, String selExport) {
        super(mService, vcardType, b);
        mSelExport = selExport;
    }

    @Override
    public StringBuilder genSelection() {
        StringBuilder selection = new StringBuilder();
        selection.append(ContactsContract.Data.CONTACT_ID).append(mSelExport);
        return selection;
    }
}
