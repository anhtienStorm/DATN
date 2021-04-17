package bkav.android.btalk.contacts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.android.contacts.common.MoreContactUtils;
import com.android.contacts.common.SimContactsConstants;
import com.android.contacts.common.SimContactsOperation;
import com.android.contacts.common.compat.CompatUtils;
import com.android.contacts.common.interactions.ImportExportDialogFragment;
import com.android.contacts.common.util.PermissionsUtil;
import com.android.contacts.common.vcard.VCardCommonArguments;
import com.android.dialer.dialpad.LatinSmartDialMap;

import java.util.List;

import bkav.android.btalk.R;

/**
 * AnhNDd: class extends ImportExportDialogFragment để thay đổi
 * việc hiển thị các chức năng.
 */
public class BtalkImportExportDialogFragment extends ImportExportDialogFragment {

    @Override
    public Dialog createDialogFragment(boolean contactsAreAvailable, ArrayAdapter<AdapterEntry> adapter, DialogInterface.OnClickListener clickListener) {
        //AnhNDd: Thay đổi tên của dialog
        return new AlertDialog.Builder(getActivity())
                .setTitle(contactsAreAvailable
                        ? R.string.menu_export
                        : R.string.dialog_import)
                .setSingleChoiceItems(adapter, -1, clickListener)
                .create();
    }

    /**
     * AnhNDd: Thực hiện việc chỉ hiện thị chức năng tương ứng với việc nhập, xuất danh bạ.
     *
     * @param res
     * @param contactsAreAvailable
     * @param adapter
     */
    @Override
    public void addEntryAdapter(Resources res, boolean contactsAreAvailable, ArrayAdapter<ImportExportDialogFragment.AdapterEntry> adapter) {
        final TelephonyManager manager =
                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        if (res.getBoolean(R.bool.config_allow_import_from_vcf_file) && !contactsAreAvailable) {
            adapter.add(new AdapterEntry(getString(R.string.import_from_vcf_file),
                    R.string.import_from_vcf_file));
        }

        if (!CompatUtils.isBCY()) {
            if (CompatUtils.isMSIMCompatible()) {
                mSubscriptionManager = (SubscriptionManager) getContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                if (manager != null && res.getBoolean(R.bool.config_allow_sim_import)) {
                    List<SubscriptionInfo> subInfoRecords = null;
                    try {
                        subInfoRecords = mSubscriptionManager.getActiveSubscriptionInfoList();
                    } catch (SecurityException e) {

                    }
                    if (subInfoRecords != null && subInfoRecords.size() > 0) {
                        if (!contactsAreAvailable) {
                            adapter.add(new AdapterEntry(getString(R.string.import_from_sim),
                                    R.string.import_from_sim));
                        } else {
                            //Bkav ToanNTe tạm thời đóng chức năng xuất danh bạ sang SIM từ Android 10 vì chức năng này
                            //Google đã thay đổi trên từ Android 10, đợi phương án xử lí tiếp theo
                            if (!PermissionsUtil.isAndroidQ()) {
                                adapter.add(new AdapterEntry(getString(R.string.export_to_sim),
                                        R.string.export_to_sim));
                            }
                        }
                    }
                }
            } else {
                if (manager != null && manager.hasIccCard()
                        && res.getBoolean(R.bool.config_allow_sim_import)) {
                    if (!contactsAreAvailable) {
                        adapter.add(new AdapterEntry(getString(R.string.import_from_sim),
                                R.string.import_from_sim));
                    }
                    if (!PermissionsUtil.isAndroidQ()) {
                        adapter.add(new AdapterEntry(getString(R.string.export_to_sim),
                                R.string.export_to_sim));
                    }
                }
            }

        }

        if (res.getBoolean(R.bool.config_allow_export)) {
            if (contactsAreAvailable) {
                adapter.add(new AdapterEntry(getString(R.string.export_to_vcf_file),
                        R.string.export_to_vcf_file));
            }
        }

        return;
    }

    /**
     * Preferred way to show this dialog
     */
    public static void show(FragmentManager fragmentManager, boolean contactsAreAvailable,
                            Class callingActivity, int exportMode) {
        final BtalkImportExportDialogFragment fragment = new BtalkImportExportDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_CONTACTS_ARE_AVAILABLE, contactsAreAvailable);
        args.putString(VCardCommonArguments.ARG_CALLING_ACTIVITY, callingActivity.getName());
        fragment.setArguments(args);
        fragment.show(fragmentManager, ImportExportDialogFragment.TAG);
        mExportMode = exportMode;
    }

    private LatinSmartDialMap mMap;

    private int mMaxLengthName = 15;

    private static final int MIN_LENGTH = 8;

    /**
     * Anhdts: xu ly ten co dau va qua dai khong import vao sim duoc
     */
    protected String processName(String name) {
        if (mMap == null) {
            mMap = new LatinSmartDialMap();
        }
        StringBuilder normalizeQuery = new StringBuilder();
        for (char c : name.toCharArray()) {
            boolean isCharUpper = Character.isUpperCase(c);
            char normalize = mMap.normalizeCharacter(c);
            if (isCharUpper) {
                normalizeQuery.append(Character.toUpperCase(normalize));
            } else {
                normalizeQuery.append(normalize);
            }
        }
        if (normalizeQuery.length() > mMaxLengthName) {
            return normalizeQuery.substring(0, mMaxLengthName);
        }
        return normalizeQuery.toString();
    }

    @Override
    public Uri insertToCard(Context context, String name, String number, String emails,
                            String anrNumber, int subscription, boolean insertToPhone) {
        Uri result;
        ContentValues mValues = new ContentValues();
        mValues.clear();
        mValues.put(SimContactsConstants.STR_TAG, name);
        if (!TextUtils.isEmpty(number)) {
            number = PhoneNumberUtils.stripSeparators(number);
            mValues.put(SimContactsConstants.STR_NUMBER, number);
        }
        if (!TextUtils.isEmpty(emails)) {
            mValues.put(SimContactsConstants.STR_EMAILS, emails);
        }
        if (!TextUtils.isEmpty(anrNumber)) {
            anrNumber = anrNumber.replaceAll("[^0123456789PWN\\,\\;\\*\\#\\+\\:]", "");
            mValues.put(SimContactsConstants.STR_ANRS, anrNumber);
        }

        SimContactsOperation mSimContactsOperation = new SimContactsOperation(context);
        result = mSimContactsOperation.insert(mValues, subscription);


        while (result == null && mMaxLengthName > MIN_LENGTH) {
            name = processName(name);
            mValues.put(SimContactsConstants.STR_TAG, name);
            result = mSimContactsOperation.insert(mValues, subscription);
            if (result == null) {
                mMaxLengthName--;
            }
        }

        if (result != null) {
            if (insertToPhone) {
                // we should import the contact to the sim account at the same
                // time.
                String[] value = new String[]{name, number, emails, anrNumber};
                MoreContactUtils.insertToPhone(value, context, subscription);
            }
        } else {
            Log.e(TAG, "export contact: [" + name + ", " + number + ", " + emails + "] to slot "
                    + subscription + " failed");
        }
        return result;
    }
}
