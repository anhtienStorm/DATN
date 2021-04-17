package bkav.android.btalk.test;

import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;

import java.util.ArrayList;

/**
 * Created by anhdt on 05/08/2017.
 *
 */

public class TestReceiver extends BroadcastReceiver {

    private static final String ACTION_TEST = "bkav.android.TEST_CONTACT_BTALK";

    //    private static final String ACCOUNT_NAME = "PHONE";
//
//    private static final String ACCOUNT_TYPE = "com.android.localphone";
//
//    private static final String nameFile = "contacts_test.vcf";
//
//    private static final String URI_PATH = "content://com.android.externalstorage.documents/document/primary%3Abtalk%2Fcontacts_test.vcf";
//
//    private static final String LOCAL_TMP_FILE_NAME_EXTRA =
//            "com.android.contacts.common.vcard.LOCAL_TMP_FILE_NAME";
//
//    private static final String SOURCE_URI_DISPLAY_NAME =
//            "com.android.contacts.common.vcard.SOURCE_URI_DISPLAY_NAME";

    @Override
    public void onReceive(Context context, Intent intent) {
//        if (intent.getAction() != null && intent.getAction().equals(ACTION_TEST)) {
//            doImportFromVcfFile(context);
//        }
    }

    public void doImportFromVcfFile(Context context) {

        String[] names = new String[]{"Bphone", "Bkav", "Contact", "CSKH", "TEST"};

        String[] prefixPhone = new String[]{"0123", " 0976", "0965", "01652", "01669", "0957", "01634", "0969"};

        for (int i = 0; i < 5; i++) {
            long time = System.currentTimeMillis();
            String timeString = String.valueOf(time);
            addContact(context, names[i], prefixPhone[(int) (time % 8)] + timeString.substring(timeString.length() - 6, timeString.length()));
            for (int j = 1; j < 800; j++) {
                long timeTemp = System.currentTimeMillis();
                String timeStringTemp = String.valueOf(timeTemp);
                addContact(context, names[i] + " " + j, prefixPhone[(int) (timeTemp % 8)] + timeStringTemp.substring(timeStringTemp.length() - 6, timeStringTemp.length()));
            }
        }
    }

    private boolean addContact(Context context, String displayName, String phone) {
        ArrayList<ContentProviderOperation> cntProOper = new ArrayList<>();
        int contactIndex = cntProOper.size();//ContactSize

        //Newly Inserted contact
        // A raw contact will be inserted ContactsContract.RawContacts table in contacts database.
        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)//Step1
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        //Display name will be inserted in ContactsContract.Data table
        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)//Step2
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName) // Name of the contact
                .build());
        //Mobile number will be inserted in ContactsContract.Data table
        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)//Step 3
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone) // Number to be added
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()); //Type like HOME, MOBILE etc
        try {
            // We will do batch operation to insert all above data
            //Contains the output of the app of a ContentProviderOperation.
            //It is sure to have exactly one of uri or count set
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cntProOper); //apply above data insertion into contacts list
            return true;
        } catch (RemoteException exp) {
            return false;
        } catch (OperationApplicationException exp) {
            return false;
        }
    }
}
