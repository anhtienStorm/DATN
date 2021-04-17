package bkav.android.btalk.suggestmagic;

import android.Manifest;
import android.accounts.Account;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;

import com.android.contacts.common.list.ContactListFilter;
import com.android.contacts.common.list.ContactListFilterController;
import com.android.contacts.common.list.PhoneNumberPickerFragment;
import com.android.contacts.common.util.StopWatch;
import com.android.contacts.common.util.UriUtils;
import com.android.dialer.database.DialerDatabaseHelper;
import com.android.dialer.dialpad.LatinSmartDialMap;
import com.android.dialer.dialpad.SmartDialNameMatcher;
import com.android.dialer.dialpad.SmartDialPrefix;
import com.android.dialer.util.TelecomUtil;
import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.utility.TelephoneExchangeUtils;

/**
 * Created by anhdt on 20/04/2017.
 * custom search danh ba
 */

public class BtalkDialerDatabaseHelper extends DialerDatabaseHelper implements PhoneNumberPickerFragment.OnQueryCompleteListener {

    /**
     * Anhdts Bang tam de xu ly trong cau query
     */
    private static final String TEMP_TABLE = "temp_table";

    private final ContactListFilterController mContactListFilterController;

    private String mQueryString = "";

    private OnQueryPhoneSuccessListener mQuerySuccessListener;

    private boolean mIsShowMore = false;

    private ContactNumber mMainSuggest;

    private ContactNumber mSecondarySuggest;

    private boolean mHasSecondarySuggest;

    private LatinSmartDialMap mMap;

    private TelephoneExchangeUtils mExchangeUtils;

    private static final String IS_INITIALIZE_DATABASE = "initialize_db";

    private static final String SHARE_PREFERENCE_DIALER = "preference_dialer";

    private BtalkDialerDatabaseHelper(Context context, String databaseName) {
        super(context, databaseName);
        mIsFirstRunOrCleanDB = context.getSharedPreferences(SHARE_PREFERENCE_DIALER, Context.MODE_PRIVATE).getBoolean(IS_INITIALIZE_DATABASE, true);
        mListTempSuggest = new ArrayList<>();
        mMap = new LatinSmartDialMap();
        mExchangeUtils = new TelephoneExchangeUtils(context);
        if (context instanceof BtalkActivity) {
            ((BtalkActivity) context).setOnQueryCompleteListener(this);
        }
        mContactListFilterController = ContactListFilterController.getInstance(context);
        mContactListFilterController.checkFilterValidity(false);
    }

    public static synchronized BtalkDialerDatabaseHelper getInstance(Context context) {
        if (DEBUG) {
            Log.v(TAG, "Getting Instance");
        }
        if (sSingleton == null) {
            // Use application context instead of activity context because this is a singleton,
            // and we don't want to leak the activity if the activity is not running but the
            // dialer database helper is still doing work.
            sSingleton = new BtalkDialerDatabaseHelper(context.getApplicationContext(),
                    DATABASE_NAME);
        }
        return (BtalkDialerDatabaseHelper) sSingleton;
    }

    // Anhdts dung ham nay de check xem btalk co dang mo len khong
    public static boolean isReady() {
        return sSingleton != null;
    }

    @Override
    protected void initMultiLanguageSearch() {
    }

    public void updateLookupUri(String lookupKey, String lookupKeyNew) {
        if (TextUtils.isEmpty(lookupKey) || TextUtils.isEmpty(lookupKeyNew)) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + Tables.SMARTDIAL_TABLE + " SET " + SmartDialDbColumns.LOOKUP_KEY +
                "= " + "'" + lookupKeyNew + "'" + " WHERE " + SmartDialDbColumns.LOOKUP_KEY + "=" + lookupKey);
    }

    interface BtalkSmartDialSortingOrder extends SmartDialSortingOrder {
        long MAGIC_CHECK_TIME_RECENT_CALL = 60L * 60 * 1000;

        /**
         * Anhdts change sort follow company
         * thoi gian goi nho hon 1h
         * nho hon 1h tra ve hieu cua khoang cach thoi gian da goi voi gia tri 1h, gia tri cang
         * lon thi cang gan nhat )
         */
        String SORT_BY_IS_RECENT_CALL =
                "(CASE WHEN " + TIME_SINCE_LAST_USED_MS + " < " + MAGIC_CHECK_TIME_RECENT_CALL +
                        " THEN " + MAGIC_CHECK_TIME_RECENT_CALL + " - " + TIME_SINCE_LAST_USED_MS +
                        " ELSE 1 END) DESC";
        /**
         * Anhdts sap xep theo so lan su dung
         * neu so cuoc goi = 0 thi tong so cuoc goi bang 0
         */
        String SORT_BY_TIMES_USED =
                "(CASE WHEN " + Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.TIMES_OUT_CALL + " > 0" +
                        " THEN " + Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.TIMES_USED +
                        " ELSE 0 END) DESC";

        /**
         * Anhdts change sort follow company
         * sap xep theo so lan su dung
         * neu so cuoc goi = 0 so cuoc goi den bang nhau thi so sanh so cuoc goi den
         * duoc tinh bang hieu tong so lan su dung tru di cuoc goi di
         */
        String SORT_BY_TIMES_USED_EXCEPTION =
                "(" + Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.TIMES_USED + " - "
                        + Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.TIMES_OUT_CALL +
                        ") DESC";

        /**
         * Anhdts
         * neu trung khit voi tu khoa tim kiem thi dua len tren
         */
        String SORT_BY_ABSOLUTE_NAME_SAME =
                "CASE WHEN " + Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.PREFIX_FULL_NAME + " LIKE " + "?2" +
                        " THEN " + 0 +
                        " WHEN " + Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.PREFIX_FULL_NAME + " LIKE " + "?3" +
                        " THEN " + 1 +
                        " ELSE 2 END";

        /**
         * Anhdts sap xep theo thoi quen tu search
         */
        String SORT_BY_NAME_PRIORITY = "(CASE WHEN " + Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.PREFIX_PRIORITY + " LIKE " + "?4" +
                " THEN " + 0 + " ELSE 1 END)";

        /**
         * Anhdts sap xep theo cac tieu chi
         */
        //TODO TrungTH bo doan sap xep theo thoi quen tu search di, can build noi bo test trai nhiem truoc da
        String BKAV_SORT_ORDER = SORT_BY_NAME_PRIORITY + ", " // Anhdts dua vao tu khoa tim kiem
                + SORT_BY_IS_RECENT_CALL + ", " // cuoc goi trong vong 1h dua len dau
                + SORT_BY_TIMES_USED + ", " // sap xep theo tong so cuoc goi
                // + Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.TIMES_USED + " DESC, "
                + Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.TIMES_OUT_CALL + " DESC, " //sap xep theo so cuoc goi di
                + SORT_BY_TIMES_USED_EXCEPTION + ", " // neu 2 ben tong cuoc goi deu bang 0 va cuoc goi di cung bang 0 so sanh cuoc goi den
                + SORT_BY_ABSOLUTE_NAME_SAME + ", " // trung khop voi tu khoa
                + Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.STARRED + " DESC, " // sap xep theo yeu thich
                + Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.IS_SUPER_PRIMARY + " DESC, " // sap xep theo yeu thich
                + Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.IN_VISIBLE_GROUP + " DESC, " // sap xep theo nhom
                + Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.DISPLAY_NAME_PRIMARY + ", " // sap xep theo ten hien thi
                + Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.IS_PRIMARY + " DESC"; // theo tieu chi google

    }

    @Override
    protected boolean isConfigBtalk() {
        return true;
    }

    /**
     * Anhdts cau lenh de tim kiem danh ba
     *
     * @param db               database
     * @param currentTimeStamp thoi gian hien tai
     * @param query            so nhap vao
     * @return cursor
     * search theo 3 truong hop
     * TH1: trung khop voi tu nhap
     * TH2: trung khop voi phan dau
     * TH3: neu tu khoa nhap vao dai hon 3 ki tu thi tim theo so dien thoai
     */
    @Override
    protected Cursor getBtalkCursor(SQLiteDatabase db, String currentTimeStamp, String query) {
        String[] loosesQuery = new String[3];
        loosesQuery[0] = query;
        loosesQuery[1] = query + "%";
        loosesQuery[2] = "%" + ENCRYPT_FILTER_SUFFIX + query + "%";
        String queryAccount = "";
        if (mContactListFilterController != null && mContactListFilterController.getFilter() != null) {
            ContactListFilter filter = mContactListFilterController.getFilter();
            if (filter.filterType == ContactListFilter.FILTER_TYPE_ACCOUNT) {
                // We should exclude the invisiable contacts.
                //Bkav QuangNDb sua lai cau query cua anhdts bi loi, lam chon 1 account bi search cham hon
                queryAccount = " and (" + SmartDialDbColumns.ACCOUNT_TYPE + " = '" +
                        filter.accountType + "' and " +
                        SmartDialDbColumns.ACCOUNT_NAME + " = '" + filter.accountName + "')";
            }
        }
        if (query.length() < 3) {
            String sql = "SELECT " +
                    SmartDialDbColumns.DATA_ID + ", " +
                    SmartDialDbColumns.DISPLAY_NAME_PRIMARY + ", " +
                    SmartDialDbColumns.PHOTO_ID + ", " +
                    SmartDialDbColumns.NUMBER + ", " +
                    Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.CONTACT_ID + ", " +
                    SmartDialDbColumns.LOOKUP_KEY + ", " +
                    SmartDialDbColumns.CARRIER_PRESENCE + ", " +
                    SmartDialDbColumns.ACCOUNT_TYPE + ", " +
                    SmartDialDbColumns.ACCOUNT_NAME + /*", " +*/
//                        SmartDialDbColumns.LAST_TIME_USED + ", " + // Query ra chua dung den, bo di de query nhanh hon
//                        SmartDialDbColumns.TIMES_OUT_CALL + ", " + // Anhdts so cuoc goi di
//                        TEMP_TABLE + "." + PrefixColumns.PREFIX +
                    " FROM " + Tables.SMARTDIAL_TABLE + ", " +
                    " (SELECT " + Tables.PREFIX_TABLE + "." +
                    PrefixColumns.CONTACT_ID + ", " +
                    PrefixColumns.PREFIX +
                    " FROM " + Tables.PREFIX_TABLE +
                    " WHERE " + PrefixColumns.PREFIX +
                    " LIKE '" + loosesQuery[1] + "'" +
                    " GROUP BY " + PrefixColumns.CONTACT_ID +
                    ") " + TEMP_TABLE +
                    " WHERE " +
                    Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.CONTACT_ID +
                    " = " + TEMP_TABLE + "." + PrefixColumns.CONTACT_ID +
                    queryAccount +
                    " ORDER BY " + BtalkSmartDialSortingOrder.BKAV_SORT_ORDER;
            return db.rawQuery(sql,
                    new String[]{currentTimeStamp,
                            loosesQuery[0],
                            loosesQuery[1],
                            loosesQuery[2]});
        } else {
            // Anhdts search theo so dien thoai
            // so dien thoai hien dang ma hoa dang so voi duoi co ki tu *
            String looseQueryNumberHeading = ENCRYPT_PREFIX_NUMBER + "%" + query + "%" + ENCRYPT_PREFIX_NUMBER;

            return db.rawQuery("SELECT " +
                            SmartDialDbColumns.DATA_ID + ", " +
                            SmartDialDbColumns.DISPLAY_NAME_PRIMARY + ", " +
                            SmartDialDbColumns.PHOTO_ID + ", " +
                            SmartDialDbColumns.NUMBER + ", " +
                            Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.CONTACT_ID + ", " +
                            SmartDialDbColumns.LOOKUP_KEY + ", " +
                            SmartDialDbColumns.CARRIER_PRESENCE + ", " +
                            SmartDialDbColumns.ACCOUNT_TYPE + ", " +
                            SmartDialDbColumns.ACCOUNT_NAME +
                            " FROM " + Tables.SMARTDIAL_TABLE + ", " +
                            " (SELECT " + Tables.PREFIX_TABLE + "." +
                            PrefixColumns.CONTACT_ID + ", " +
                            PrefixColumns.PREFIX +
                            " FROM " + Tables.PREFIX_TABLE +
                            " WHERE " + PrefixColumns.PREFIX +
                            " LIKE '" + loosesQuery[1] + "' OR " +
                            PrefixColumns.PREFIX +
                            " LIKE '" + looseQueryNumberHeading + "'" +
                            " GROUP BY " + PrefixColumns.CONTACT_ID +
                            ") " + TEMP_TABLE +
                            " WHERE " +
                            Tables.SMARTDIAL_TABLE + "." + SmartDialDbColumns.CONTACT_ID +
                            " = " + TEMP_TABLE + "." + PrefixColumns.CONTACT_ID +
                            queryAccount +
                            " ORDER BY " + BtalkSmartDialSortingOrder.BKAV_SORT_ORDER,
                    new String[]{currentTimeStamp,
                            loosesQuery[0],
                            loosesQuery[1],
                            loosesQuery[2]});
        }
    }

    /**
     * Anhdts
     * Su dung de update truong tong so cuoc goi va cuoc voi di
     *
     * @param db                   database
     * @param updatedContactCursor cursor chua nhung contact moi duoc update
     * @param insert               bo gia tri update vao smartDialer
     * @param number               so duoc update
     * @param lastUpdateMillis     thoi gian lan cuoi cung update
     */
    @Override
    protected boolean getOutCallTimes(SQLiteDatabase db, Cursor updatedContactCursor, SQLiteStatement insert, String number, String lastUpdateMillis) {
        Cursor cursor = null;
        boolean hasChange = false;
        try {
            // Uri.Builder builder = CallLog.Calls.CONTENT_URI.buildUpon().appendQueryParameter(CallLog.Calls.LIMIT_PARAM_KEY, "1");
            String normalized_number = updatedContactCursor.getString(PhoneQuery.PHONE_NORMALIZED_NUMBER);
            long contactId = updatedContactCursor.getLong(PhoneQuery.PHONE_CONTACT_ID);
            // neu contact chua duoc khoi tao thi query tu dau
            boolean contactIsDefined = !mIsFirstRunOrCleanDB && (mContactsRemove != null && mContactsRemove.get(contactId) != null);
            if (!contactIsDefined) {
                if (normalized_number == null) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return false;
                    }
                    String formatNumber = number;
                    if (number.contains("+") && number.length() >= 10) {
                        formatNumber = "%" + number.substring(3);
                    }
                    formatNumber = formatNumber.replaceAll("-", "");
                    formatNumber = formatNumber.replaceAll(" ", "");

                    cursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                            new String[]{
                                    CallLog.Calls.TYPE,
                            },
                            CallLog.Calls.NUMBER + " like ?",
                            new String[]{formatNumber}, null);
                } else {
                    cursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                            new String[]{
                                    CallLog.Calls.TYPE,
                            },
                            CallLog.Calls.CACHED_NORMALIZED_NUMBER + " like ?",
                            new String[]{normalized_number}, null);
                }
            } else {
                if (normalized_number == null) {
                    String formatNumber = number;
                    if (number.contains("+") && number.length() >= 10) {
                        formatNumber = "%" + number.substring(3);
                    }
                    formatNumber = formatNumber.replaceAll("-", "");
                    formatNumber = formatNumber.replaceAll(" ", "");

                    cursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                            new String[]{
                                    CallLog.Calls.TYPE
                            },
                            CallLog.Calls.DATE + " > ?" + " and " +
                                    CallLog.Calls.NUMBER + " like ?",
                            new String[]{lastUpdateMillis, formatNumber}, null);
                } else {
                    cursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                            new String[]{
                                    CallLog.Calls.TYPE
                            },
                            CallLog.Calls.DATE + " > ?" + " and " +
                                    CallLog.Calls.CACHED_NORMALIZED_NUMBER + " like ?",
                            new String[]{lastUpdateMillis, normalized_number}, null);
                }
            }

            int outTimeUsed = 0;
            int timeUsed = 0;
            if (cursor != null) {
                timeUsed = cursor.getCount();
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    int type = cursor.getInt(0);
                    if (type == CallLog.Calls.OUTGOING_TYPE) {
                        outTimeUsed++;
                    }
                }
            }

            if (timeUsed > 0) {
                hasChange = true;
            }

            if (contactIsDefined) {
                long dataIdPriority = mContactsRemove.get(contactId).getDataIdPriority();
                if (dataIdPriority < 100000 && dataIdPriority
                        != updatedContactCursor.getLong(PhoneQuery.PHONE_ID)) {
                    if (outTimeUsed == 0) {
                        outTimeUsed += mContactsRemove.get(contactId).getCallOut() - 1;
                        timeUsed += mContactsRemove.get(contactId).getCallUsed() - 1;
                    } else {
                        outTimeUsed += mContactsRemove.get(contactId).getCallOut();
                        timeUsed += mContactsRemove.get(contactId).getCallUsed();
                    }
                } else {
                    outTimeUsed += mContactsRemove.get(contactId).getCallOut();
                    timeUsed += mContactsRemove.get(contactId).getCallUsed();
                }
            }
            insert.bindLong(8, timeUsed);
            insert.bindLong(17, outTimeUsed);
        } catch (Exception e) {
            insert.bindLong(8, updatedContactCursor.getInt(PhoneQuery.PHONE_TIMES_USED));
            insert.bindLong(17, 0);
            Log.v(TAG, "exception " + e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return hasChange;
    }

    @Override
    protected void setFirstUpdateComplete() {
        if (mIsFirstRunOrCleanDB) {
            mIsFirstRunOrCleanDB = false;
            mContext.getSharedPreferences(SHARE_PREFERENCE_DIALER, Context.MODE_PRIVATE).edit().putBoolean(IS_INITIALIZE_DATABASE, false).apply();
        }
    }

    @Override
    protected void restoreFirstUpdate() {
        mContext.getSharedPreferences(SHARE_PREFERENCE_DIALER, Context.MODE_PRIVATE).edit().putBoolean(IS_INITIALIZE_DATABASE, true).apply();
    }

    /**
     * Anhdts loc cac ket qua ma hoa ten
     */
    @Override
    protected ArrayList<String> filterNamePrefixes(ArrayList<String> namePrefixes) {
        ArrayList<String> values = new ArrayList<>();
        while (!namePrefixes.isEmpty()) {
            String value = namePrefixes.get(0);
            namePrefixes.remove(0);
            if (values.contains(value)) {
                continue;
            }
            boolean found = false;
            for (String compareValue : namePrefixes) {
                if (compareValue.startsWith(value)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                values.add(value);
            }
        }
        return values;
    }


    /**
     * Anhdts Xu ly du lieu truoc
     * Buoc 1: loc cac so trung nhau, luu bang ma id va mLookupUri
     * Buoc 2: sap xep danh sach theo ma id va mLookupUri
     */
    @Override
    protected boolean processPrefixBtalk(Cursor cursor, StopWatch stopWatch,
                                         SmartDialNameMatcher nameMatcher, String query, ArrayList<ContactNumber> result) {
        mQueryString = query;
        ArrayList<ContactNumber> temp = new ArrayList<>();
        String prefixDateFormat = mContext.getString(R.string.display_name_date_format);
        try {
            if (DEBUG) {
                stopWatch.lap("Prefix query completed");
            }

            // Gets the column ID from the cur
            // sor.
            final int columnDataId = 0;
            final int columnDisplayNamePrimary = 1;
            final int columnPhotoId = 2;
            final int columnNumber = 3;
            final int columnId = 4;
            final int columnLookupKey = 5;
            final int columnCarrierPresence = 6;
            final int columnAccountType = 7;
            final int columnAccountName = 8;
            if (DEBUG) {
                stopWatch.lap("Found column IDs");
            }

            final Set<BtalkContactMatch> duplicates = new HashSet<>();
            int counter = 0;
            if (DEBUG) {
                stopWatch.lap("Moved cursor to start");
            }
            // Iterates the cursor to find top contact suggestions without duplication.
            while ((cursor.moveToNext()) && (counter < MAX_ENTRIES)) {
                // Anhdts dang callLog
                boolean isCallLog = false;
                final long dataID = cursor.getLong(columnDataId);
                String displayName = cursor.getString(columnDisplayNamePrimary);

                // Anhdts dang date thi doi lai ten hien thi
                if (displayName.startsWith(prefixDateFormat)) {
                    isCallLog = true;
                    // displayName = DateUtil.formatDateRecentCall(mContext, Long.parseLong(displayName.replace(prefixDateFormat, "")));
                }

                final String phoneNumber = cursor.getString(columnNumber);
                final long id = cursor.getLong(columnId);
                final long photoId = cursor.getLong(columnPhotoId);
                final String lookupKey = cursor.getString(columnLookupKey);
                final int carrierPresence = cursor.getInt(columnCarrierPresence);
                final String accountType = cursor.getString(columnAccountType);
                final String accountName = cursor.getString(columnAccountName);

                // Neu mot contact trung ten va so dien thoai thi bo qua
                final BtalkContactMatch contactMatch = new BtalkContactMatch(lookupKey, id, phoneNumber, displayName);
                if (duplicates.contains(contactMatch)) {
                    // Log.v("Anhdts", "aaaa " + displayName + " number " + phoneNumber);
                    continue;
                }

                // If the contact has either the name or number that matches the query, add to the
                // result.
                final boolean nameMatches = nameMatcher.matches(displayName);
                final boolean numberMatches =
                        (nameMatcher.matchesNumber(phoneNumber, query) != null);
                if (nameMatches || numberMatches) {
                    // If a contact has not been added, add it to the result and the hash set.*/
                    duplicates.add(contactMatch);
                    if (isCallLog) {
                        temp.add(new ContactNumber(id, dataID, displayName, phoneNumber, lookupKey, UriUtils.parseUriOrNull(lookupKey),
                                photoId, carrierPresence, accountType, accountName));
                    } else {
                        temp.add(new ContactNumber(id, dataID, displayName, phoneNumber, lookupKey,
                                photoId, carrierPresence, accountType, accountName));
                    }
                    counter++;
                    if (DEBUG) {
                        stopWatch.lap("Added one result: Name: " + displayName);
                    }
                } else {
                    final SmartDialPrefix.PhoneNumberTokens phoneNumberTokens = SmartDialPrefix.parsePhoneNumber(phoneNumber);

                    // Anhdts chuyen dang format E164 sang dang chung
                    if (phoneNumberTokens.countryCodeOffset != 0) {
                        String phoneFormat = "0" + SmartDialNameMatcher.normalizeNumber(phoneNumber,
                                phoneNumberTokens.countryCodeOffset, mMap);
                        if (nameMatcher.matchesNumber(phoneFormat, query) != null) {
                            // If a contact has not been added, add it to the result and the hash set.*/
                            duplicates.add(contactMatch);
                            if (isCallLog) {
                                temp.add(new ContactNumber(id, dataID, displayName, phoneFormat, lookupKey, UriUtils.parseUriOrNull(lookupKey),
                                        photoId, carrierPresence, accountType, accountName));
                            } else {
                                temp.add(new ContactNumber(id, dataID, displayName, phoneFormat, lookupKey,
                                        photoId, carrierPresence, accountType, accountName));
                            }
                            counter++;
                        }
                    }
                }
            }

            if (DEBUG) {
                stopWatch.stopAndLog(TAG + "Finished loading cursor", 0);
            }
        } finally {
            cursor.close();
        }

        // mang luu lai list cac lookupKey va id
        ArrayList<ContactMatch> listContactMatch = new ArrayList<>();
        ArrayList<ContactNumber> listSortFollowSameName = new ArrayList<>();
        // sap xep lai
        if (!temp.isEmpty()) {
            while (!temp.isEmpty()) {
                if (listSortFollowSameName.isEmpty()) {
                    ContactNumber contactNumber = temp.get(0);
                    listSortFollowSameName.add(contactNumber);
                    temp.remove(0);
                    listContactMatch.add(new ContactMatch(contactNumber.lookupKey, contactNumber.id));
                } else {
                    String lookupKey = listSortFollowSameName.get(listSortFollowSameName.size() - 1).lookupKey;
                    long id = listSortFollowSameName.get(listSortFollowSameName.size() - 1).id;

                    int i = 0;
                    while (i < temp.size()) {
                        ContactNumber contactNumber = temp.get(i);
                        if (contactNumber.lookupKey.equals(lookupKey) && contactNumber.id == id) {
                            listSortFollowSameName.add(contactNumber);
                            temp.remove(contactNumber);
                            continue;
                        }
                        i++;
                    }
                    if (!temp.isEmpty()) {
                        ContactNumber contactNumber = temp.get(0);
                        listSortFollowSameName.add(contactNumber);
                        temp.remove(0);
                        listContactMatch.add(new ContactMatch(contactNumber.lookupKey, contactNumber.id));
                    }
                }
            }
        }

        // Log.v("Anhdts", "loc ten xong  " + listSortFollowSameName.size());
        if (query.length() > 2) {
            ContactNumber exchangeContact = mExchangeUtils.getTelephoneExchange(query, mContext);
            if (exchangeContact != null) {
                filterExchangeTelephone(query, listContactMatch, listSortFollowSameName, exchangeContact);
            }
        }

        processViewSuggest(listContactMatch, listSortFollowSameName, result);

        return true;
    }

    /**
     * Anhdts xu ly du lieu suggest
     */
    private void processViewSuggest(ArrayList<ContactMatch> data, ArrayList<ContactNumber> contactNumbersSort, ArrayList<ContactNumber> results) {
        if (mQuerySuccessListener == null) {
            return;
        }
        clearSuggestValue();
        if (data != null && data.size() > 0) {
            long timeCurrent = System.currentTimeMillis();
            getPosSuggest(data, timeCurrent);

            ContactMatch contactMatchSuggest;
            if (data.size() > mPosTakeSuggest) {
                contactMatchSuggest = data.get(mPosTakeSuggest);
            } else {
                contactMatchSuggest = data.get(data.size() - 1);
            }
            String lookupKey = contactMatchSuggest.lookupKey;
            long id = contactMatchSuggest.id;

            // lay cac danh ba goi y 1 va 2
            for (ContactNumber tmp : contactNumbersSort) {
                if (tmp.lookupKey.equals(lookupKey) && tmp.id == id) {
                    if (mMainSuggest == null) {
                        mMainSuggest = tmp;
                    } else if (!mHasSecondarySuggest) {
                        mHasSecondarySuggest = true;
                        mSecondarySuggest = tmp;
                    } else {
                        results.add(tmp);
                    }
                }
            }

            // lay cac so con lai
            for (ContactNumber tmp : contactNumbersSort) {
                if (!lookupKey.equals(tmp.lookupKey) || tmp.id != id) {
                    results.add(tmp);
                }
            }

            mLastTimeCheckSuggest = timeCurrent;
            mLastTextQuery = mQueryString;
            mIsShowMore = results.size() > 0;
        } else {
            mLastTimeCheckSuggest = 0;
            mLastTextQuery = "";
            mListTempSuggest.clear();
            mPosTakeSuggest = 0;
        }
    }

    private int mPosTakeSuggest = 0;

    /**
     * Anhdts
     * get lai view
     */
    private void getPosSuggest(ArrayList<ContactMatch> listMatch, long timeCurrent) {
        if (mLastTextQuery.equals(mQueryString)) {
            mPosTakeSuggest = 0;
            for (SuggestContactDetail tmp : mListTempSuggest) {
                if (tmp.mTextQuery.equals(mQueryString)) {
                    tmp.mPosTakeSuggest = 0;
                    break;
                }
            }
            return;
        } else {
            // Vi tri suggest set o diem cuoi cung
            mPosTakeSuggest = 0;
            // TH1 vua moi khoi tao hoac nhap so dau tien thi vi tri bang 0 luon
            if (mLastTimeCheckSuggest == 0 || TextUtils.isEmpty(mQueryString) || (mLastTextQuery.isEmpty() && mQueryString.length() == 1)) {
                mListTempSuggest.clear();
                SuggestContactDetail currentContact = new SuggestContactDetail(listMatch.get(0));
                currentContact.setMakeSuggest(mQueryString, 0);
                mListTempSuggest.add(currentContact);
                return;
            }
            // TH2 thoi gian nhap phim lon hon thoi gian MAGIC
            else if ((timeCurrent - mLastTimeCheckSuggest) > MAX_TIME_CHECK_DELAY) {
                // Neu text cu ngan hon text moi, bat dau tim kiem
                if (mLastTextQuery.length() > mQueryString.length()) {
                    // Neu text cu dai hon text moi, tim trong mang, neu khong co thi search binh thuong
                    int i = mListTempSuggest.size();
                    while (i > 0 && !mListTempSuggest.get(i - 1).mTextQuery.equals(mQueryString)) {
                        i--;
                    }
                    if (i > 0) {
                        int total = mListTempSuggest.size();
                        for (int j = i; j < total; j++) {
                            mListTempSuggest.remove(mListTempSuggest.size() - 1);
                        }
                        mPosTakeSuggest = mListTempSuggest.get(i - 1).mPosTakeSuggest;
                        return;
                    }
                }
            } else {
                SuggestContactDetail currentContact = new SuggestContactDetail(listMatch.get(0));
                currentContact.setMakeSuggest(mQueryString, 0);
                mListTempSuggest.add(currentContact);
                return;
            }
        }

        int i = 0;
        do {
            SuggestContactDetail currentContact = new SuggestContactDetail(listMatch.get(i));
            boolean found = false;
            for (SuggestContactDetail tmp : mListTempSuggest) {
                if (currentContact.equals(tmp)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                mPosTakeSuggest = i;
                currentContact.setMakeSuggest(mQueryString, mPosTakeSuggest);
                mListTempSuggest.add(currentContact);
                return;
            }
            i++;
        } while (i < listMatch.size());
        mPosTakeSuggest = 0;
    }

    public void setOnQueryPhoneSuccessListener(OnQueryPhoneSuccessListener listener) {
        mQuerySuccessListener = listener;
    }

    /**
     * Anhdts interface bind phan tu dau tien len view suggest cua dialpad
     */
    public interface OnQueryPhoneSuccessListener {
        void cleanSuggestView();

        void bindSuggestViewMain(ContactNumber data, boolean showMore);

        void bindSecondarySuggestView(ContactNumber mainSuggest);

        void clearSecondaryIfNeed();

        boolean isStringQueryNotNull();
    }

    /**
     * Anhdts load du lieu suggest
     */
    @Override
    public void bindSuggestViewDialpad() {
        if (mQuerySuccessListener != null) {
            if (mQuerySuccessListener.isStringQueryNotNull() && mMainSuggest != null) {
                mQuerySuccessListener.bindSuggestViewMain(mMainSuggest, mIsShowMore);
                if (mHasSecondarySuggest) {
                    mQuerySuccessListener.bindSecondarySuggestView(mSecondarySuggest);
                } else {
                    mQuerySuccessListener.clearSecondaryIfNeed();
                }
            } else {
                clearSuggestValue();
                mQuerySuccessListener.cleanSuggestView();
                mLastTimeCheckSuggest = 0;
                mLastTextQuery = "";
                mListTempSuggest.clear();
                mPosTakeSuggest = 0;
            }
        }
    }

    @Override
    protected void clearSuggestValue() {
        mMainSuggest = null;
        mSecondarySuggest = null;
        mHasSecondarySuggest = false;
        mIsShowMore = false;
    }

    /**
     * Anhdts upgrade database them cot convert ten sang dang magic pad
     */
    @Override
    protected void createDatabaseWithPrefix(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL("CREATE TABLE " + Tables.SMARTDIAL_TABLE + " ("
                    + SmartDialDbColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + SmartDialDbColumns.DATA_ID + " INTEGER, "
                    + SmartDialDbColumns.NUMBER + " TEXT,"
                    + SmartDialDbColumns.CONTACT_ID + " INTEGER,"
                    + SmartDialDbColumns.LOOKUP_KEY + " TEXT,"
                    + SmartDialDbColumns.DISPLAY_NAME_PRIMARY + " TEXT, "
                    + SmartDialDbColumns.PHOTO_ID + " INTEGER, "
                    + SmartDialDbColumns.LAST_SMARTDIAL_UPDATE_TIME + " LONG, "
                    + SmartDialDbColumns.LAST_TIME_USED + " LONG, "
                    + SmartDialDbColumns.TIMES_USED + " INTEGER, "
                    + SmartDialDbColumns.STARRED + " INTEGER, "
                    + SmartDialDbColumns.IS_SUPER_PRIMARY + " INTEGER, "
                    + SmartDialDbColumns.IN_VISIBLE_GROUP + " INTEGER, "
                    + SmartDialDbColumns.IS_PRIMARY + " INTEGER, "
                    + SmartDialDbColumns.CARRIER_PRESENCE + " INTEGER NOT NULL DEFAULT 0,"
                    + SmartDialDbColumns.ACCOUNT_TYPE + " TEXT, "
                    + SmartDialDbColumns.ACCOUNT_NAME + " TEXT, "
                    + SmartDialDbColumns.TIMES_OUT_CALL + " INTEGER, " // Anhdts them cot thoi so cuoc goi di
                    + SmartDialDbColumns.PREFIX_FULL_NAME + " TEXT, " // Anhdts them cot ma hoa ten thanh so
                    + SmartDialDbColumns.PREFIX_PRIORITY + " TEXT, " // Anhdts them cot ma hoa lich su nhap tu de search
                    + SmartDialDbColumns.NORMALIZE_NAME + " TEXT " // Anhdts them cot ma hoa ten ve dang khong dau
                    + ");");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Anhdts them cot prefix hay search
     */
    @Override
    protected void addColumnNormalizeName(SQLiteDatabase db, boolean overMuch) {
        db.beginTransaction();
        try {
            if (!overMuch) {
                // Sua loi duplicate khi nhay tu ban 11 sang 13
                db.execSQL("ALTER TABLE " + Tables.SMARTDIAL_TABLE +
                        " ADD COLUMN " + SmartDialDbColumns.NORMALIZE_NAME + " TEXT ");
            }
            Cursor cursor = db.rawQuery("SELECT " +
                    SmartDialDbColumns._ID + ", " +
                    SmartDialDbColumns.DISPLAY_NAME_PRIMARY + " FROM " + Tables.SMARTDIAL_TABLE, null);
            if (cursor != null) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    String displayName = cursor.getString(1);
                    StringBuilder stringNormalize = new StringBuilder();

                    for (char c : displayName.toCharArray()) {
                        char normalize = mMap.normalizeCharacter(c);
                        if (normalize != '\'' && normalize != '"' && normalize != '\\') {
                            stringNormalize.append(normalize);
                        }
                    }
                    String value = stringNormalize.toString().contains(" ") ? stringNormalize + " + " +
                            stringNormalize.toString().replaceAll(" ", "") : stringNormalize.toString();
                    db.execSQL("UPDATE " + Tables.SMARTDIAL_TABLE + " SET " + SmartDialDbColumns.NORMALIZE_NAME +
                            "= " + "'" + value + "'" + " WHERE " + SmartDialDbColumns._ID + "=" + cursor.getLong(0));
                }
                cursor.close();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Anhdts lay du lieu cot ten ma hoa sang dang so
     * Iterates through the whole name string. If the current character is a valid character,
     * append it to the current token. If the current character is not a valid character, for
     * example space " ", mark the current token as complete and add it to the list of tokens.
     */
    @Override
    protected void getPrefixFullName(SQLiteStatement insert, String displayName) {
        if (displayName == null) {
            insert.bindString(SmartUpdate.PREFIX_FULL_NAME_COLUMN, "");
        } else {
            final int length = displayName.length();
            char c;
            final StringBuilder currentIndexToken = new StringBuilder();

            for (int i = 0; i < length; i++) {
                c = mMap.normalizeCharacter(displayName.charAt(i));
                if (mMap.isValidDialpadCharacter(c)) {
                    // Converts a character into the number on dialpad that represents the character.*/
                    currentIndexToken.append(mMap.getDialpadIndex(c));
                }
            }
            insert.bindString(SmartUpdate.PREFIX_FULL_NAME_COLUMN, String.valueOf(currentIndexToken));
        }
    }

    private String mRecentDisplayName;

    private String mRecentQuery;

    public void setRecentCall(String display) {
        mRecentDisplayName = display;
        mRecentQuery = mLastTextQuery;
    }

    /**
     * Anhdts ma hoa du lieu nhap nguoi dung
     * co che ma hoa. Voi moi gia tri Prefix da duoc tim kiem (A la so lan dung, B la prefix)
     * A-B dang nay khi search se khong duoc uu tien
     * A#B dang nay khi search duoc uu tien (khi vua nhap vao hoac so lan su dung lon hon 5)
     * khi search thi su dung dang %#QUERY% de tim trong chuoi nay, neu true thi duoc uu tien len
     */
    @Override
    protected void getPrefixPriority(SQLiteStatement insert, String displayName, long contactId) {
        if (TextUtils.isEmpty(mRecentQuery) || TextUtils.isEmpty(mRecentDisplayName)) {
            if (mContactsRemove != null &&
                    mContactsRemove.get(contactId) != null) {
                insert.bindString(SmartUpdate.PREFIX_PRIORITY_COLUMN, String.valueOf(mContactsRemove.get(
                        contactId).getPrefix()));
                return;
            }
            return;
        }

        if (displayName.equals(mRecentDisplayName)) {
            ArrayList<String> namePrefixes = SmartDialPrefix
                    .generateNamePrefixes(displayName);
            namePrefixes = filterNamePrefixes(namePrefixes);

            if (namePrefixes.size() == 1) {
                insert.bindString(SmartUpdate.PREFIX_PRIORITY_COLUMN, ENCRYPT_FILTER_SUFFIX + namePrefixes.get(0));
                return;
            }

            ArrayList<String> tempPrefixes = new ArrayList<>(namePrefixes);

            String priority = "";

            if (mContactsRemove != null &&
                    mContactsRemove.get(contactId) != null) {
                priority = mContactsRemove.get(
                        contactId).getPrefix();
            }

            int i = 0;
            while (i < tempPrefixes.size()) {
                if (tempPrefixes.get(i).startsWith(mRecentQuery)) {
                    i++;
                } else {
                    tempPrefixes.remove(i);
                }
            }
            try {
                priority = analysisPriority(priority, namePrefixes, tempPrefixes);
            } catch (Exception e) {
                Log.v(TAG, e.toString());
            }

            if (!TextUtils.isEmpty(priority)) {
                insert.bindString(SmartUpdate.PREFIX_PRIORITY_COLUMN, priority);
            }
        }
    }

    /**
     * Anhdts xoa du lieu nhap search,
     */
    @Override
    protected void clearRecentQuery() {
        mRecentQuery = null;
        mRecentDisplayName = null;
    }

    /**
     * Anhdts lay du lieu ten da duoc bo dau
     */
    @Override
    protected void getNormalizeName(SQLiteStatement insert, String displayName) {
        if (displayName == null) {
            insert.bindString(SmartUpdate.NORMALIZE_NAME_COLUMN, "");
        } else {
            StringBuilder stringNormalize = new StringBuilder();

            for (char c : displayName.toCharArray()) {
                char normalize = mMap.normalizeCharacter(c);
                if (normalize != '\'' && normalize != '"' && normalize != '\\') {
                    stringNormalize.append(normalize);
                }
            }
            String value = stringNormalize.toString().contains(" ") ? stringNormalize + " + " +
                    stringNormalize.toString().replaceAll(" ", "") : stringNormalize.toString();
            insert.bindString(SmartUpdate.NORMALIZE_NAME_COLUMN, value);
        }
    }

    /**
     * Anhdts Xu ly thong ke lai
     * voi chuoi vua nhap vao thi tang so lan su dung len 1, chuyen prefix do ve dang uu tien
     * update lai cac prefix khac
     *
     * @param tempValues   chuoi ma hoa luc truoc
     * @param tempPrefixes mang cac Prefix khop voi tu khoa tim kiem
     * @return chuoi ma hoa moi
     */
    private String analysisPriority(String tempValues, ArrayList<String> namePrefixes, ArrayList<String> tempPrefixes) {
        StringBuilder encryption = new StringBuilder();
        if (TextUtils.isEmpty(tempValues)) {
            for (String namePrefix : tempPrefixes) {
                encryption.append(String.valueOf("1" + ENCRYPT_FILTER_SUFFIX + namePrefix + ENCRYPT_PREFIX));
            }
            return encryption.toString();
        }
        String[] values = tempValues.split("\\*");
        for (String value : values) {
            int i = 0;
            boolean found = false;
            while (i < tempPrefixes.size()) {
                String namePrefix = tempPrefixes.get(i);
                if (value.endsWith(ENCRYPT_FILTER_SUFFIX + namePrefix) ||
                        value.endsWith(ENCRYPT_NO_FILTER_SUFFIX + namePrefix)) {
                    encryption.append((new PriorityValues(value)).parseValues());
                    tempPrefixes.remove(i);
                    found = true;
                    break;
                }
                i++;
            }
            if (!found) {
                encryption.append((new PriorityValues(value)).updateValues(namePrefixes));
            }
        }
        if (tempPrefixes.size() > 0) {
            for (String namePrefix : tempPrefixes) {
                encryption.append(String.valueOf("1" + ENCRYPT_FILTER_SUFFIX + namePrefix + ENCRYPT_PREFIX));
            }
        }
        return encryption.toString();
    }

    private static final String ENCRYPT_PREFIX = "*";

    private static final String ENCRYPT_NO_FILTER_SUFFIX = "-";

    static final Character ENCRYPT_FILTER_SUFFIX = '#';

    private static final int VALUE_PRIORITY = 5;

    private class PriorityValues {

        String prefix = "";

        int number = 0;

        PriorityValues(String encryption) {
            int index;
            if (encryption.indexOf(ENCRYPT_FILTER_SUFFIX) > 0) {
                index = encryption.indexOf(ENCRYPT_FILTER_SUFFIX);
            } else {
                index = encryption.indexOf(ENCRYPT_NO_FILTER_SUFFIX);
            }
            if (index <= 0) {
                return;
            }
            try {
                number = Integer.valueOf(encryption.substring(0, index));
                prefix = encryption.substring(index + 1);
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
        }

        String parseValues() {
            number++;
            return "" + number + ENCRYPT_FILTER_SUFFIX + prefix + ENCRYPT_PREFIX;
        }

        String updateValues(ArrayList<String> namePrefixes) {
            if (namePrefixes.contains(prefix)) {
                return "" + number + (number > VALUE_PRIORITY ? ENCRYPT_FILTER_SUFFIX : ENCRYPT_NO_FILTER_SUFFIX) + prefix + ENCRYPT_PREFIX;
            } else {
                return "";
            }
        }
    }

    private ArrayList<SuggestContactDetail> mListTempSuggest;

    private String mLastTextQuery = "";

    private long mLastTimeCheckSuggest = 0;

    private static final long MAX_TIME_CHECK_DELAY = 1000;

    //************************ Phan xu ly search CALL LOG***************************/

    /**
     * Anhdts search call log de lay lookup uri neu co
     */
    private void filterExchangeTelephone(String keyWord, ArrayList<ContactMatch> listContactMatch, ArrayList<ContactNumber> listSortFollowSameName, ContactNumber exchange) {
        Cursor callCursor;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        callCursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                new String[]{CallLog.Calls.NUMBER,
                        CallLog.Calls.CACHED_NORMALIZED_NUMBER,
                        CallLog.Calls.CACHED_LOOKUP_URI},
                CallLog.Calls.NUMBER + " LIKE ?",
                new String[]{keyWord},
                CallLog.Calls.DEFAULT_SORT_ORDER);
        if (callCursor == null) {
            return;
        }
        if (callCursor.getCount() > 0) {
            callCursor.moveToFirst();
            String number = callCursor.getString(0);
            Uri lookupUri = UriUtils.parseUriOrNull(callCursor.getString(2));
            if (lookupUri == null) {
                lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            }
            String lookupKey = UriUtils.getLookupKeyFromUri(lookupUri);
            if (TextUtils.isEmpty(lookupKey)) {
                exchange = new ContactNumber(exchange.displayName, number, lookupUri.toString(), lookupUri);
            }
        }
        listContactMatch.add(0, new ContactMatch(exchange.lookupKey, ContactNumber.ID_DEFAULT_EXCHANGE_CONTACT));
        listSortFollowSameName.add(0, exchange);
        callCursor.close();
    }

    /**
     * Xoa 1 contact
     */
    @VisibleForTesting
    @Override
    protected void removeDeletedContacts(SQLiteDatabase db, Cursor deletedContactCursor, Long currentMillis) {
        if (deletedContactCursor == null) {
            return;
        }
        ArrayList<CallLogData> callLogs = new ArrayList<>();
        db.beginTransaction();
        try {
            while (deletedContactCursor.moveToNext()) {
                final Long deleteContactId =
                        deletedContactCursor.getLong(DeleteContactQuery.DELETED_CONTACT_ID);

                // Anhdts luu lai gia tri vua xoa
                Cursor cursorAcc = null;
                try {
                    cursorAcc = db.rawQuery("SELECT " +
                            SmartDialDbColumns.TIMES_OUT_CALL + ", " +  // 0
                            SmartDialDbColumns.TIMES_USED + ", " +      // 1
                            SmartDialDbColumns.LAST_TIME_USED + ", " +  // 2
                            SmartDialDbColumns.NUMBER +                 // 3
                            " FROM " + Tables.SMARTDIAL_TABLE +
                            " WHERE " + SmartDialDbColumns.CONTACT_ID +
                            "=" + deleteContactId, null);
                    if (cursorAcc != null && cursorAcc.getCount() > 0) {
                        cursorAcc.moveToPosition(-1);
                        int countUsed = 0;
                        int countOutCall = 0;
                        long usedTime;
                        while (cursorAcc.moveToNext()) {
                            if (cursorAcc.getInt(0) > countOutCall) {
                                countOutCall = cursorAcc.getInt(0);
                            }
                            countUsed = cursorAcc.getInt(1) > countUsed ? cursorAcc.getInt(1) : countUsed;
                            usedTime = cursorAcc.getInt(2);
                            String number = cursorAcc.getString(3);
                            String numberFormat = number.replace("+84", "0").replaceAll("-", "").replace(" ", "");
                            long numberId = parseNumberToId(numberFormat);
                            if (numberId != -1) {
                                callLogs.add(new CallLogData(numberFormat, numberId, usedTime, countOutCall, countUsed));
                            }
                        }
                    }
                } finally {
                    if (cursorAcc != null) {
                        cursorAcc.close();
                    }
                }

                db.delete(Tables.SMARTDIAL_TABLE,
                        SmartDialDbColumns.CONTACT_ID + "=" + deleteContactId, null);
                db.delete(Tables.PREFIX_TABLE,
                        PrefixColumns.CONTACT_ID + "=" + deleteContactId, null);
            }
            for (CallLogData callLogData : callLogs) {
                if (callLogData.mCountUsed > 0) {
                    insertCallLogNumber(db, callLogData.mLookupUri, callLogData.mNumber, callLogData.mNumberId, currentMillis, callLogData.mDate,
                            callLogData.mCountOutCall, callLogData.mCountUsed, true);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            deletedContactCursor.close();
            db.endTransaction();
        }
    }

    @Override
    protected long parseNumberToId(String number) {
        try {
            if (number.length() > 9) {
                return 100000000 + Long.parseLong(number.substring(number.length() - 9));
            } else {
                return 100000000 + Long.parseLong(number);
            }
        } catch (Exception e) {
            return -1;
        }
    }

    private void insertCallLogNumber(SQLiteDatabase db, Uri lookupUri, String number, long numberId, long currentTime, long lastUsedTime, int countOutCall, long countUsed, boolean isRemoveContact) {
        db.beginTransaction();
        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            final String sqlInsert = "INSERT INTO " + Tables.SMARTDIAL_TABLE + " (" +
                    SmartDialDbColumns.DATA_ID + ", " +
                    SmartDialDbColumns.NUMBER + ", " +
                    SmartDialDbColumns.CONTACT_ID + ", " +
                    SmartDialDbColumns.LOOKUP_KEY + ", " +
                    SmartDialDbColumns.DISPLAY_NAME_PRIMARY + ", " +
                    SmartDialDbColumns.PHOTO_ID + ", " +
                    SmartDialDbColumns.LAST_TIME_USED + ", " +
                    SmartDialDbColumns.TIMES_USED + ", " +
                    SmartDialDbColumns.STARRED + ", " +
                    SmartDialDbColumns.IS_SUPER_PRIMARY + ", " +
                    SmartDialDbColumns.IN_VISIBLE_GROUP + ", " +
                    SmartDialDbColumns.IS_PRIMARY + ", " +
                    SmartDialDbColumns.CARRIER_PRESENCE + ", " +
                    SmartDialDbColumns.LAST_SMARTDIAL_UPDATE_TIME + ", " +
                    SmartDialDbColumns.ACCOUNT_TYPE + ", " +
                    SmartDialDbColumns.ACCOUNT_NAME + ", " +
                    SmartDialDbColumns.TIMES_OUT_CALL + ", " + // Anhdts so cuoc goi di
                    SmartDialDbColumns.PREFIX_FULL_NAME + ", " +
                    SmartDialDbColumns.PREFIX_PRIORITY + ", " +
                    SmartDialDbColumns.NORMALIZE_NAME + ") " + // Anhdts ten da ma hoa
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            final SQLiteStatement insert = db.compileStatement(sqlInsert);

            final String numberSqlInsert = "INSERT INTO " + Tables.PREFIX_TABLE + " (" +
                    PrefixColumns.CONTACT_ID + ", " +
                    PrefixColumns.PREFIX + ") " +
                    " VALUES (?, ?)";
            final SQLiteStatement numberInsert = db.compileStatement(numberSqlInsert);

            String displayName;

            // Query trong calllog, neu so vua xoa query tu dau do khong dung duoc last time query,
            // dung lasttime tra ve null khong lay duoc lookup_uri
            Cursor cursor;
            if (isRemoveContact) {
                cursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                        new String[]{
                                CallLog.Calls.CACHED_LOOKUP_URI
                        }, CallLog.Calls.NUMBER + " like ?",
                        new String[]{"%" + numberId}, CallLog.Calls.DATE + " DESC");
                if (cursor == null) {
                    return;
                }
                cursor.moveToFirst();

                // Neu da duoc luu lai cac gia tri thi lay lai

                if (cursor.getCount() > 0) {
                    lookupUri = UriUtils.parseUriOrNull(cursor.getString(0));
                }
                cursor.close();
            }

            displayName = getCallLocationAndDate(lastUsedTime);
            if (lookupUri == null) {
                lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            }

            String lookupKey = lookupUri.toString();

            // Handle string columns which can possibly be null first. In the case of certain
            // null columns (due to malformed rows possibly inserted by third-party apps
            // or sync adapters), skip the phone number row.
            insert.bindString(SmartUpdate.NUMBER_COLUMN, number);

            insert.bindString(SmartUpdate.LOOKUP_KEY_COLUMN, lookupKey);

            insert.bindString(SmartUpdate.DISPLAY_NAME_PRIMARY_COLUMN, displayName);
            insert.bindLong(SmartUpdate.DATA_ID_COLUMN, numberId);
            insert.bindLong(SmartUpdate.CONTACT_ID_COLUMN, numberId);
            insert.bindLong(SmartUpdate.PHOTO_ID_COLUMN, 0);
            insert.bindLong(SmartUpdate.LAST_TIME_USED_COLUMN, lastUsedTime);
            insert.bindLong(SmartUpdate.COUNT_USED_COLUMN, countUsed);
            insert.bindLong(SmartUpdate.STARRED_COLUMN, 0);
            insert.bindLong(SmartUpdate.IS_SUPER_PRIMARY_COLUMN, 0);
            insert.bindLong(SmartUpdate.IN_VISIBLE_GROUP_COLUMN, 1);
            insert.bindLong(SmartUpdate.IS_PRIMARY_COLUMN, 0);
            insert.bindLong(SmartUpdate.CARRIER_PRESENCE_COLUMN, 0);
            insert.bindLong(SmartUpdate.LAST_SMARTDIAL_UPDATE_TIME_COLUMN, currentTime);
            insert.bindLong(SmartUpdate.COUNT_OUT_CALL_COLUMN, countOutCall);


            insert.bindString(SmartUpdate.ACCOUNT_TYPE_COLUMN, mContext.getResources().getString(
                    R.string.missing_account_type));

            insert.bindString(SmartUpdate.ACCOUNT_NAME_COLUMN, mContext.getResources().getString(
                    R.string.missing_account_name));

            insert.bindString(SmartUpdate.PREFIX_FULL_NAME_COLUMN, number);

            getNormalizeName(insert, number);

            insert.executeInsert();

            // bind prefix
            numberInsert.bindLong(1, numberId);
            // Anhdts: voi so dien thoai thi ma hoa dang *NUMBER*
            numberInsert.bindString(2, ENCRYPT_PREFIX_NUMBER + number + ENCRYPT_PREFIX_NUMBER);
            numberInsert.executeInsert();
            numberInsert.clearBindings();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

    }

    private static final String PHONE_NUMBER_SELECTION =
            ContactsContract.Data.MIMETYPE + " IN ('"
                    + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "', "
                    + "'" + ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE + "') AND "
                    + ContactsContract.Data.DATA1 + " NOT NULL";

    @VisibleForTesting
    @Override
    protected void removeUpdatedContacts(SQLiteDatabase db, Cursor updatedContactCursor) {
        db.beginTransaction();
        try {
            updatedContactCursor.moveToPosition(-1);
            // Anhdts khoi tao
            if (mContactsRemove == null) {
                mContactsRemove = new LongSparseArray<>();
            } else {
                mContactsRemove.clear();
            }

            while (updatedContactCursor.moveToNext()) {
                final Long contactId =
                        updatedContactCursor.getLong(UpdatedContactQuery.UPDATED_CONTACT_ID);
                String lookupKey = updatedContactCursor.getString(UpdatedContactQuery.LOOKUP_KEY);
                Uri lookupUri = ContentUris.withAppendedId(
                        Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey), contactId);

                if (lookupUri.toString().startsWith(ContactsContract.Contacts.CONTENT_URI.toString())) {
                    if (!lookupUri.toString().endsWith(ContactsContract.Contacts.Data.CONTENT_DIRECTORY)) {
                        lookupUri = Uri.withAppendedPath(lookupUri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
                    }
                }
                StringBuilder numberIds = new StringBuilder("");

                // Anhdts luu lai gia tri vua xoa
                Cursor cursorAcc = null;
                try {
                    cursorAcc = db.rawQuery("SELECT " +
                            SmartDialDbColumns.TIMES_OUT_CALL + ", " +
                            SmartDialDbColumns.TIMES_USED + ", " +
                            SmartDialDbColumns.PREFIX_PRIORITY + ", " +
                            SmartDialDbColumns.DATA_ID + ", " +
                            SmartDialDbColumns.LAST_TIME_USED +
                            " FROM " + Tables.SMARTDIAL_TABLE +
                            " WHERE " + SmartDialDbColumns.CONTACT_ID +
                            "=" + contactId, null);
                    if (cursorAcc != null) {
                        if (cursorAcc.getCount() == 0) {
                            cursorAcc.close();
                            cursorAcc = null;
                            Cursor cursorNumber = mContext.getContentResolver().
                                    query(lookupUri, new String[]{
                                            ContactsContract.CommonDataKinds.Phone._ID,
                                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    }, PHONE_NUMBER_SELECTION, null, null);
                            if (cursorNumber != null) {
                                cursorNumber.moveToPosition(-1);
                                while (cursorNumber.moveToNext()) {
                                    String numberFormat = cursorNumber.getString(1).
                                            replace("+84", "0").replaceAll("-", "").replace(" ", "");
                                    if (numberFormat.length() >= 9) {
                                        long numberId = parseNumberToId(numberFormat);
                                        if (!TextUtils.isEmpty(numberIds)) {
                                            numberIds.append(",");
                                        }
                                        numberIds.append(numberId);
                                    }
                                }
                                cursorNumber.close();
                            }
                            cursorAcc = db.rawQuery("SELECT " +
                                    SmartDialDbColumns.TIMES_OUT_CALL + ", " +
                                    SmartDialDbColumns.TIMES_USED + ", " +
                                    SmartDialDbColumns.PREFIX_PRIORITY + ", " +
                                    SmartDialDbColumns.DATA_ID + ", " +
                                    SmartDialDbColumns.LAST_TIME_USED +
                                    " FROM " + Tables.SMARTDIAL_TABLE +
                                    " WHERE " + SmartDialDbColumns.CONTACT_ID +
                                    " in (" + numberIds.toString() + ")", null);

                            if (cursorAcc != null && cursorAcc.getCount() > 0) {
                                cursorAcc.moveToPosition(-1);
                                int callUsed = 0;
                                int timeOutCall = 0;
                                long dataIdPriority = 0;
                                String prefix = "";
                                long usedTime = 0;
                                while (cursorAcc.moveToNext()) {
                                    prefix = TextUtils.isEmpty(prefix) ? cursorAcc.getString(2) : prefix;
                                    if (cursorAcc.getInt(0) >= timeOutCall) {
                                        timeOutCall = cursorAcc.getInt(0);
                                        dataIdPriority = cursorAcc.getLong(3);
                                    }
                                    callUsed = cursorAcc.getInt(1) > callUsed ? cursorAcc.getInt(1) : callUsed;
                                    usedTime = cursorAcc.getLong(4);
                                }
                                mContactsRemove.put(contactId, new ContactRemove(
                                        timeOutCall, callUsed, prefix, dataIdPriority, usedTime));
                            }
                        }

                    }
                } finally {
                    if (cursorAcc != null) {
                        cursorAcc.close();
                    }
                }
                if (TextUtils.isEmpty(numberIds)) {
                    db.delete(Tables.SMARTDIAL_TABLE, SmartDialDbColumns.CONTACT_ID + "=" +
                            contactId, null);
                    db.delete(Tables.PREFIX_TABLE, PrefixColumns.CONTACT_ID + "=" +
                            contactId, null);
                } else {
                    db.delete(Tables.SMARTDIAL_TABLE, SmartDialDbColumns.CONTACT_ID + "=" +
                            contactId + " or " + SmartDialDbColumns.CONTACT_ID + " IN (" + numberIds.toString() + ")", null);
                    db.delete(Tables.PREFIX_TABLE, PrefixColumns.CONTACT_ID + "=" +
                            contactId + " or " + PrefixColumns.CONTACT_ID + " IN (" + numberIds.toString() + ")", null);
                }
            }
            db.setTransactionSuccessful();
        } catch (
                Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * * Anhdts update so trong callLog
     */
    @Override
    protected void updateSmartCallLog(String lastUpdateMillis, SQLiteDatabase db,
                                      long currentTime) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Cursor cursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                new String[]{
                        CallLog.Calls.NUMBER,           // 0
                        CallLog.Calls.TYPE,             // 1
                        CallLog.Calls.DATE,             // 2
                        CallLog.Calls.CACHED_LOOKUP_URI,// 3
                        CallLog.Calls.DURATION          // 4
                },
                CallLog.Calls.DATE + " > ?" + " and " +
                        CallLog.Calls.CACHED_NAME + " IS NULL",
                new String[]{lastUpdateMillis}, null);
        if (cursor != null) {
            LongSparseArray<CallLogData> callLogs = new LongSparseArray<>();
            StringBuilder numberIds = new StringBuilder("");
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                String number = cursor.getString(0);
                if (number.length() < 10) {
                    continue;
                }
                long duration = cursor.getLong(4);
                // Neu thoi gian bang 0; gia su truong hop goi nhung chua nghe may thi check 10 so thi van duoc suggest
                // neu lon hon 10 so thi bo khong luu (hien uu tien viet nam). vi do dai cac so nuoc ngoai lon hon 10 so
                if (duration == 0) {
                    String phoneFormat = PhoneNumberUtils.stripSeparators(number);
                    if (phoneFormat.startsWith("84")) {
                        phoneFormat = phoneFormat.replaceFirst("84", "0");
                    }
                    if (phoneFormat.length() > 10) {
                        continue;
                    }
                }
                String numberFormat = number.replace("+84", "0").replaceAll("-", "").replace(" ", "");
                long numberId = parseNumberToId(numberFormat);
                if (numberId == -1) {
                    continue;
                }
                if (callLogs.get(numberId) == null) {
                    if (!TextUtils.isEmpty(numberIds.toString())) {
                        numberIds.append(",");
                    }
                    numberIds.append(numberId);
                    Uri lookupUri = UriUtils.parseUriOrNull(cursor.getString(3));
                    if (lookupUri == null) {
                        Cursor contactLookupCursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                                new String[]{
                                        CallLog.Calls.CACHED_LOOKUP_URI
                                }, CallLog.Calls.NUMBER + " like ? and " + CallLog.Calls.CACHED_LOOKUP_URI +
                                        " is not null",
                                new String[]{number}, CallLog.Calls.DATE + " limit 2");
                        if (contactLookupCursor != null) {
                            if (contactLookupCursor.getCount() > 0) {
                                contactLookupCursor.moveToFirst();
                                lookupUri = UriUtils.parseUriOrNull(contactLookupCursor.getString(0));
                                contactLookupCursor.close();
                            }
                        }
                        if (lookupUri == null) {
                            lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
                        }
                    }
                    int typeCall = cursor.getInt(1);
                    long date = cursor.getLong(2);
                    CallLogData callLogData = new CallLogData(numberFormat, numberId,
                            typeCall == CallLog.Calls.OUTGOING_TYPE, lookupUri, date);
                    callLogs.put(numberId, callLogData);
                } else {
                    CallLogData data = callLogs.get(numberId);
                    int typeCall = cursor.getInt(1);
                    long date = cursor.getLong(2);
                    data.updateDateAndCall(date, typeCall == CallLog.Calls.OUTGOING_TYPE);
                }
            }
            cursor.close();

            if (TextUtils.isEmpty(numberIds.toString())) {
                return;
            }
            Cursor cursorAcc;
            db.beginTransaction();
            try {
                cursorAcc = db.rawQuery("SELECT " +
                        SmartDialDbColumns.CONTACT_ID + ", " +
                        SmartDialDbColumns.TIMES_USED + ", " +
                        SmartDialDbColumns.TIMES_OUT_CALL +
                        " FROM " + Tables.SMARTDIAL_TABLE +
                        " WHERE " + SmartDialDbColumns.CONTACT_ID +
                        " IN (" + numberIds.toString() + ")", null);
                if (cursorAcc != null) {
                    cursorAcc.moveToPosition(-1);
                    while (cursorAcc.moveToNext()) {
                        long contactId = cursorAcc.getLong(0);
                        CallLogData callLogData = callLogs.get(contactId);
                        if (callLogData == null) {
                            continue;
                        }
                        final String lookupKey = callLogData.mLookupUri != null
                                ? UriUtils.getLookupKeyFromUri(callLogData.mLookupUri) : null;
                        if (TextUtils.isEmpty(lookupKey)) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(SmartDialDbColumns.TIMES_USED, cursorAcc.getLong(1) + callLogData.mCountUsed);
                            contentValues.put(SmartDialDbColumns.TIMES_OUT_CALL, cursorAcc.getLong(2) + callLogData.mCountOutCall);
                            contentValues.put(SmartDialDbColumns.LAST_TIME_USED, callLogData.mDate);
                            contentValues.put(SmartDialDbColumns.DISPLAY_NAME_PRIMARY, getCallLocationAndDate(callLogData.mDate));
                            contentValues.put(SmartDialDbColumns.LAST_SMARTDIAL_UPDATE_TIME, currentTime);
                            contentValues.put(SmartDialDbColumns.LOOKUP_KEY, callLogData.mLookupUri.toString());
                            if (!TextUtils.isEmpty(mRecentQuery) || !TextUtils.isEmpty(mRecentDisplayName)) {
                                if (mRecentDisplayName.startsWith("?DATE:")) {
                                    contentValues.put(SmartDialDbColumns.PREFIX_PRIORITY, ENCRYPT_FILTER_SUFFIX + mRecentQuery);
                                }
                            }
                            db.update(Tables.SMARTDIAL_TABLE, contentValues, SmartDialDbColumns.CONTACT_ID + "=" + contactId, null);
                        } else {
                            db.delete(Tables.SMARTDIAL_TABLE, SmartDialDbColumns.CONTACT_ID + "=" + contactId, null);
                        }
                        callLogs.remove(contactId);
                    }
                    cursorAcc.close();
                    for (int i = 0; i < callLogs.size(); i++) {
                        CallLogData callLogData = callLogs.valueAt(i);
                        final String lookupKey = callLogData.mLookupUri != null
                                ? UriUtils.getLookupKeyFromUri(callLogData.mLookupUri) : null;
                        if (TextUtils.isEmpty(lookupKey)) {
                            insertCallLogNumber(db, callLogData.mLookupUri, callLogData.mNumber, callLogData.mNumberId, currentTime, callLogData.mDate,
                                    callLogData.mCountOutCall, callLogData.mCountUsed, false);
                        }
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

    }


    /**
     * Anhdts du lieu call log
     */
    private class CallLogData {

        String mNumber;

        long mNumberId;

        int mCountUsed;

        int mCountOutCall;

        Uri mLookupUri;

        long mDate;

        CallLogData(String number, long numberId, boolean isOutCall, Uri lookupUri, long date) {
            this.mNumber = number;
            this.mNumberId = numberId;
            mCountOutCall = isOutCall ? 1 : 0;
            this.mLookupUri = lookupUri;
            this.mDate = date;
            this.mCountUsed = 1;
        }

        CallLogData(String number, long numberId,
                    long usedTime, int countOutCall, int countUsed) {
            this.mNumber = number;
            this.mNumberId = numberId;
            this.mLookupUri = null;
            this.mDate = usedTime;
            this.mCountUsed = countUsed;
            this.mCountOutCall = countOutCall;
        }

        void updateDateAndCall(long date, boolean isOutCall) {
            if (date > mDate) {
                mDate = date;
            }
            if (isOutCall) {
                mCountOutCall++;
            }
            mCountUsed++;
        }

    }

    private String getCallLocationAndDate(long lastUsedTime) {
        return mContext.getString(R.string.display_name_date_format) + lastUsedTime;
    }

    /**
     * Anhdts xoa so trong nhat ki thi xoa luon trong du lieu search
     */
    public void removeCallLog(String mFinalNumber) {
        if (mFinalNumber.length() < 9) {
            return;
        }
        String numberFormat = mFinalNumber.replace("+84", "0").replaceAll("-", "").replace(" ", "");
        long numberId = parseNumberToId(numberFormat);

        final SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(Tables.SMARTDIAL_TABLE, SmartDialDbColumns.CONTACT_ID + "=" +
                    numberId, null);
            db.delete(Tables.PREFIX_TABLE, PrefixColumns.CONTACT_ID + "=" +
                    numberId, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Anhdts xoa toan bo nhat ki thi xoa toan bo du lieu search so khong co trong danh ba
     */
    public void removeCallLog() {
        final SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery("Select " + SmartDialDbColumns.CONTACT_ID + " from " + Tables.SMARTDIAL_TABLE + " where " +
                    SmartDialDbColumns.DISPLAY_NAME_PRIMARY + " like \"?DATE:%\"", null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    String contactIds = "";
                    cursor.moveToFirst();
                    do {
                        if (TextUtils.isEmpty(contactIds)) {
                            contactIds = "(";
                        } else {
                            contactIds += ", ";
                        }
                        contactIds += String.valueOf(cursor.getInt(0));
                    } while (cursor.moveToNext());
                    contactIds += ")";
                    db.delete(Tables.SMARTDIAL_TABLE, SmartDialDbColumns.CONTACT_ID + " in " +
                            contactIds, null);
                    db.delete(Tables.PREFIX_TABLE, PrefixColumns.CONTACT_ID + " in " +
                            contactIds, null);
                }
                cursor.close();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static final String[] CALL_LOG_PROJECTION = {CallLog.Calls.NUMBER, CallLog.Calls._ID, CallLog.Calls.TYPE, CallLog.Calls.DATE};

    /**
     * Anhdts xoa list nhat ki, cap nhat lai du lieu search
     */
    public void removeCallLog(StringBuilder callIdsBuilder) {
        final SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            LongSparseArray<String> countRemove = new LongSparseArray<>();
            Cursor cursor = mContext.getContentResolver().query(TelecomUtil.getCallLogUri(mContext), CALL_LOG_PROJECTION,
                    CallLog.Calls._ID + " IN (" + callIdsBuilder.toString() + ") AND " + CallLog.Calls.TYPE + " != " + CallLog.Calls.VOICEMAIL_TYPE +
                            " AND " + CallLog.Calls.CACHED_NAME + " IS NULL",
                    null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        String number = cursor.getString(0);
                        if (number.length() < 9) {
                            continue;
                        }
                        String numberFormat = number.replace("+84", "0").replaceAll("-", "").replace(" ", "");
                        long numberId = parseNumberToId(numberFormat);
                        if (numberId == -1) {
                            continue;
                        }
                        if (countRemove.get(numberId) == null) {
                            countRemove.put(numberId, String.valueOf(cursor.getLong(1)));
                        } else {
                            String value = countRemove.get(numberId);
                            countRemove.append(numberId, value + "," + cursor.getLong(1));
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();

                for (int i = 0; i < countRemove.size(); i++) {
                    long contactId = countRemove.keyAt(i);
                    Cursor cursorSmart = mContext.getContentResolver().query(TelecomUtil.getCallLogUri(mContext), CALL_LOG_PROJECTION,
                            CallLog.Calls.NUMBER + " LIKE ? AND " + CallLog.Calls.TYPE + " != " + CallLog.Calls.VOICEMAIL_TYPE +
                                    " AND " + CallLog.Calls.CACHED_NAME +
                                    " IS NULL AND " + CallLog.Calls._ID + " NOT IN (" + countRemove.get(contactId) + ")",
                            new String[]{"%" + contactId}, CallLog.Calls.DATE + " DESC");
                    if (cursorSmart != null) {
                        if (cursorSmart.getCount() > 0) {
                            long date = 0;
                            cursorSmart.moveToFirst();
                            int callOut = 0;
                            do {
                                if (date == 0) {
                                    date = cursorSmart.getLong(3);
                                }
                                if (cursorSmart.getInt(2) == CallLog.Calls.OUTGOING_TYPE) {
                                    callOut++;
                                }
                            } while (cursorSmart.moveToNext());

                            ContentValues values = new ContentValues();
                            values.put(SmartDialDbColumns.TIMES_USED, countRemove.size());
                            values.put(SmartDialDbColumns.TIMES_OUT_CALL, callOut);
                            values.put(SmartDialDbColumns.LAST_TIME_USED, date);

                            db.update(Tables.SMARTDIAL_TABLE, values, SmartDialDbColumns.CONTACT_ID + "=" +
                                    contactId, null);
                        } else {
                            db.delete(Tables.SMARTDIAL_TABLE, SmartDialDbColumns.CONTACT_ID + "=" +
                                    contactId, null);
                            db.delete(Tables.PREFIX_TABLE, PrefixColumns.CONTACT_ID + "=" +
                                    contactId, null);
                        }
                        cursorSmart.close();
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

}
