package bkav.android.btalk.suggestmagic;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import com.android.contacts.common.list.ContactListFilter;
import com.android.contacts.common.list.ContactListFilterController;
import com.android.contacts.common.util.PermissionsUtil;
import com.android.contacts.common.util.StopWatch;
import com.android.dialer.database.DialerDatabaseHelper;
import com.android.dialer.dialpad.LatinSmartDialMap;
import com.android.dialer.dialpad.SmartDialNameMatcher;
import com.android.dialer.dialpad.SmartDialPrefix;
import com.android.ex.chips.RecipientEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bkav.android.btalk.activities.BtalkActivity;

/**
 * Created by anhdt on 31/10/2017.
 *
 */
class SmartSuggestLoader extends AsyncTaskLoader<DialerDatabaseHelper.ContactNumber[]> {

    private String TIME_SINCE_LAST_USED_MS = "( ?2 - " +
            DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.LAST_TIME_USED + ")";

    private long MAGIC_CHECK_TIME_RECENT_CALL = 60L * 60 * 1000;


    private final static String PREFIX_QUERY = !BtalkActivity.isAndroidQ() ?
            "SELECT " +
            DialerDatabaseHelper.SmartDialDbColumns.DATA_ID + ", " +
            DialerDatabaseHelper.SmartDialDbColumns.DISPLAY_NAME_PRIMARY + ", " +
            DialerDatabaseHelper.SmartDialDbColumns.PHOTO_ID + ", " +
            DialerDatabaseHelper.SmartDialDbColumns.NUMBER + ", " +
            DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.CONTACT_ID + ", " +
            DialerDatabaseHelper.SmartDialDbColumns.LOOKUP_KEY + ", " +
            DialerDatabaseHelper.SmartDialDbColumns.CARRIER_PRESENCE + ", " +
            DialerDatabaseHelper.SmartDialDbColumns.ACCOUNT_TYPE + ", " +
            DialerDatabaseHelper.SmartDialDbColumns.ACCOUNT_NAME + /*", " +*/
            " FROM " + DialerDatabaseHelper.Tables.SMARTDIAL_TABLE :
            "SELECT " +
            DialerDatabaseHelper.SmartDialDbColumns.DATA_ID + ", " +
            DialerDatabaseHelper.SmartDialDbColumns.DISPLAY_NAME_PRIMARY + ", " +
            DialerDatabaseHelper.SmartDialDbColumns.PHOTO_ID + ", " +
            DialerDatabaseHelper.SmartDialDbColumns.NUMBER + ", " +
            DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.CONTACT_ID + ", " +
            DialerDatabaseHelper.SmartDialDbColumns.LOOKUP_KEY + ", " +
            DialerDatabaseHelper.SmartDialDbColumns.CARRIER_PRESENCE + /*", " +
            DialerDatabaseHelper.SmartDialDbColumns.ACCOUNT_TYPE + ", " +
            DialerDatabaseHelper.SmartDialDbColumns.ACCOUNT_NAME +*/ /*", " +*/
            " FROM " + DialerDatabaseHelper.Tables.SMARTDIAL_TABLE;

    /**
     * Anhdts Bang tam de xu ly trong cau query
     */
    private static final String TEMP_TABLE = "temp_table";

    /**
     * Anhdts sap xep theo thoi quen tu search
     */
    private String SORT_BY_NAME_PRIORITY = "(CASE WHEN " + DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.PREFIX_PRIORITY + " LIKE " + "?1" +
            " THEN " + 0 + " ELSE 1 END)";

    /**
     * Anhdts
     * thoi gian goi nho hon 1h
     * nho hon 1h tra ve hieu cua khoang cach thoi gian da goi voi gia tri 1h, gia tri cang
     * lon thi cang gan nhat )
     */
    private String SORT_BY_IS_RECENT_CALL =
            "(CASE WHEN " + TIME_SINCE_LAST_USED_MS + " < " + MAGIC_CHECK_TIME_RECENT_CALL +
                    " THEN " + MAGIC_CHECK_TIME_RECENT_CALL + " - " + TIME_SINCE_LAST_USED_MS +
                    " ELSE 1 END) DESC";
    /**
     * Anhdts sap xep theo so lan su dung
     * neu so cuoc goi = 0 thi tong so cuoc goi bang 0
     */
    private String SORT_BY_TIMES_USED =
            "(CASE WHEN " + DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.TIMES_OUT_CALL + " > 0" +
                    " THEN " + DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.TIMES_USED +
                    " ELSE 0 END) DESC";

    /**
     * Anhdts change sort follow company
     * sap xep theo so lan su dung
     * neu so cuoc goi = 0 so cuoc goi den bang nhau thi so sanh so cuoc goi den
     * duoc tinh bang hieu tong so lan su dung tru di cuoc goi di
     */
    private String SORT_BY_TIMES_USED_EXCEPTION =
            "(" + DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.TIMES_USED + " - "
                    + DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.TIMES_OUT_CALL +
                    ") DESC";

    /**
     * Anhdts
     * sap xep theo vi tri tu khoa o dau
     */
    private String SORT_BY_NAME_LIKE_LEVEL =
            "(CASE WHEN " + DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.PREFIX_FULL_NAME + " LIKE " + "?3" +
                    " THEN " + 0 +
                    " ELSE 1 END)";

    /** Anhdts sap xep theo cac tieu chi trong che do search text
     */
    private String BKAV_SEARCH_TEXT_SORT_ORDER = SORT_BY_NAME_PRIORITY + ", " // Anhdts dua vao tu khoa tim kiem
            + SORT_BY_IS_RECENT_CALL + ", " // cuoc goi trong vong 1h dua len dau
            + SORT_BY_TIMES_USED + ", " // sap xep theo tong so cuoc goi
            + DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.TIMES_OUT_CALL + " DESC, " //sap xep theo so cuoc goi di
            + SORT_BY_TIMES_USED_EXCEPTION + ", " // neu 2 ben tong cuoc goi deu bang 0 va cuoc goi di cung bang 0 so sanh cuoc goi den
            + DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.STARRED + " DESC, " // sap xep theo yeu thich
            + SORT_BY_NAME_LIKE_LEVEL + ", "// sap xep theo tu khoa
            + DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.IS_SUPER_PRIMARY + " DESC, " // sap xep theo yeu thich
            + DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.IN_VISIBLE_GROUP + " DESC, " // sap xep theo nhom
            + DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.DISPLAY_NAME_PRIMARY + ", " // sap xep theo ten hien thi
            + DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.IS_PRIMARY + " DESC"; // theo tieu chi google

    private static final boolean DEBUG = false;

    private static final String TAG = "Anhdts";

    private final Context mContext;

    private String mQuery;

    private ForceLoadContentObserver mObserver;

    private LatinSmartDialMap mMap;

    private DialerDatabaseHelper.ContactNumber[] mContactSuggest;

    private Set<BtalkContactMatch> mDuplicates = new HashSet<>();

    private SmartDialNameMatcher mNameMatcher;
    private ContactListFilterController mContactListFilterController;

    SmartSuggestLoader(Context context) {
        super(context);
        mContext = context;
        mMap = new LatinSmartDialMap();
    }

    void configureQuery(String query) {
        mQuery = query.replaceAll("'", "''");
    }


    void configureBlackList(List<RecipientEntry> recipientEntries) {
        mDuplicates.clear();
        for (RecipientEntry entry : recipientEntries) {
            mDuplicates.add(new BtalkContactMatch(entry.getLookupKey(), entry.getContactId(), entry.getDestination(), entry.getDisplayName()));
        }
    }

    @Override
    public DialerDatabaseHelper.ContactNumber[] loadInBackground() {
        if (!PermissionsUtil.hasContactsPermissions(mContext) || mQuery == null || mQuery.equals("")) {
            return null;
        }

        final StopWatch stopWatch = DEBUG ? StopWatch.start(":load magic") : null;

        boolean isNormalizeCharacter = true;

        StringBuilder dialpadString = new StringBuilder();

        for (char c : mQuery.toCharArray()) {
            char normalize = mMap.normalizeCharacter(c);
            dialpadString.append(mMap.getDialpadIndex(normalize));
            if (normalize != c) {
                isNormalizeCharacter = false;
            }
        }

        boolean isNumeric = mQuery.matches("-?\\d+(\\.\\d+)?");

        final SQLiteDatabase db = BtalkDialerDatabaseHelper.getInstance(mContext).getReadableDatabase();

        long currentTimeStamp = System.currentTimeMillis();
        Cursor cursor;

        String looseQueryNumber = BtalkDialerDatabaseHelper.ENCRYPT_PREFIX_NUMBER + "%" + mQuery + "%" +
                BtalkDialerDatabaseHelper.ENCRYPT_PREFIX_NUMBER;

        String queryFilterContactNoName = "";
        String queryAccount = "";
        mContactListFilterController = ContactListFilterController.getInstance(mContext);
        if (mContactListFilterController != null && mContactListFilterController.getFilter() != null) {
            ContactListFilter filter = mContactListFilterController.getFilter();
            if (filter.filterType == ContactListFilter.FILTER_TYPE_ACCOUNT || !isNumeric) {
                // Bkav TienNAb: sua lai logic hien thi suggest view khi search danh ba
                // We should exclude the invisiable contacts.
//                queryFilterContactNoName = " and (" + DialerDatabaseHelper.SmartDialDbColumns.ACCOUNT_TYPE + " = '" +
//                        filter.accountType + "' and " +
//                        DialerDatabaseHelper.SmartDialDbColumns.ACCOUNT_NAME + " = '" + filter.accountName + "') or " +
//                        DialerDatabaseHelper.SmartDialDbColumns.ACCOUNT_TYPE + " = '(No type)'";

//                    queryFilterContactNoName = " and (" + DialerDatabaseHelper.SmartDialDbColumns.ACCOUNT_TYPE + " != '(No type)' ) ";
                queryFilterContactNoName = " and (" + DialerDatabaseHelper.SmartDialDbColumns.PREFIX_FULL_NAME + " != " + DialerDatabaseHelper.SmartDialDbColumns.NORMALIZE_NAME + " ) ";
                // Bkav TienNAb: them dieu kien de chi goi y cac danh ba dang hien thi
                if (filter.accountName != null){
                    queryAccount = " and (" + DialerDatabaseHelper.SmartDialDbColumns.ACCOUNT_NAME + " = '" + filter.accountName + "' ) ";
                }
            }
        }

        if (isNumeric) {
            cursor = db.rawQuery(PREFIX_QUERY + ", " +
                            " (SELECT " + DialerDatabaseHelper.Tables.PREFIX_TABLE + "." +
                            DialerDatabaseHelper.PrefixColumns.CONTACT_ID + ", " +
                            DialerDatabaseHelper.PrefixColumns.PREFIX +
                            " FROM " + DialerDatabaseHelper.Tables.PREFIX_TABLE +
                            " WHERE " + DialerDatabaseHelper.PrefixColumns.PREFIX +
                            " LIKE '" + looseQueryNumber + "'" +
                            " GROUP BY " + DialerDatabaseHelper.PrefixColumns.CONTACT_ID +
                            ") " + TEMP_TABLE +
                            " WHERE " +
                            DialerDatabaseHelper.Tables.SMARTDIAL_TABLE + "." + DialerDatabaseHelper.SmartDialDbColumns.CONTACT_ID +
                            " = " + TEMP_TABLE + "." + DialerDatabaseHelper.PrefixColumns.CONTACT_ID +
                            queryFilterContactNoName + queryAccount +
                            " ORDER BY " + BKAV_SEARCH_TEXT_SORT_ORDER +
                            " limit 10",
                    new String[]{"%" + BtalkDialerDatabaseHelper.ENCRYPT_FILTER_SUFFIX + dialpadString.toString() + "%",
                            String.valueOf(currentTimeStamp),
                            dialpadString.toString()});
        } else {
            if (isNormalizeCharacter) {
                // Bkav TienNAb: Fix loi suggest popup hien thi cac so dien thoai khong luu trong danh ba
                final String sql = PREFIX_QUERY +
                        " WHERE (" + DialerDatabaseHelper.SmartDialDbColumns.DISPLAY_NAME_PRIMARY +
                        " LIKE '" + mQuery + "%" + "' OR " +
                        DialerDatabaseHelper.SmartDialDbColumns.DISPLAY_NAME_PRIMARY +
                        " LIKE '" + "% " + mQuery + "%" + "' OR " +
                        DialerDatabaseHelper.SmartDialDbColumns.NORMALIZE_NAME +
                        " LIKE '" + mQuery + "%" + "' OR " +
                        DialerDatabaseHelper.SmartDialDbColumns.NORMALIZE_NAME +
                        " LIKE '" + "% " + mQuery + "%" + "')" +
                        queryFilterContactNoName + queryAccount +
                        " ORDER BY " + BKAV_SEARCH_TEXT_SORT_ORDER +
                        " limit 10";
                cursor = db.rawQuery(sql,
                        new String[]{"%" + BtalkDialerDatabaseHelper.ENCRYPT_FILTER_SUFFIX + dialpadString.toString() + "%",
                                String.valueOf(currentTimeStamp),
                                dialpadString.toString()});
            } else {
                // Bkav TienNAb: fix lỗi gợi ý cả những danh bạ đang không hiển thị trong tab contact
                cursor = db.rawQuery(PREFIX_QUERY +
                                " WHERE (" + DialerDatabaseHelper.SmartDialDbColumns.DISPLAY_NAME_PRIMARY +
                                " LIKE '" + mQuery + "%" + "' OR " +
                                DialerDatabaseHelper.SmartDialDbColumns.DISPLAY_NAME_PRIMARY +
                                " LIKE '" + "% " + mQuery + "%" + "')" +
                                queryFilterContactNoName + queryAccount +
                                " ORDER BY " + BKAV_SEARCH_TEXT_SORT_ORDER +
                                " limit 10",
                        new String[]{"%" + BtalkDialerDatabaseHelper.ENCRYPT_FILTER_SUFFIX + dialpadString.toString() + "%",
                                String.valueOf(currentTimeStamp),
                                dialpadString.toString()});
            }
        }
        if (!isNormalizeCharacter && cursor != null && cursor.getCount() == 0) {
            cursor.close();
            StringBuilder normalizeQuery = new StringBuilder();
            for (char c : mQuery.toCharArray()) {
                char normalize = mMap.normalizeCharacter(c);
                normalizeQuery.append(normalize);
            }
            // Bkav TienNAb: fix lỗi gợi ý cả những danh bạ đang không hiển thị trong tab contact
            cursor = db.rawQuery(PREFIX_QUERY +
                            " WHERE (" + DialerDatabaseHelper.SmartDialDbColumns.DISPLAY_NAME_PRIMARY +
                            " LIKE '" + normalizeQuery + "%" + "' OR " +
                            DialerDatabaseHelper.SmartDialDbColumns.DISPLAY_NAME_PRIMARY +
                            " LIKE '" + "% " + normalizeQuery + "%" + "')" +
                            queryFilterContactNoName + queryAccount +
                            " ORDER BY " + BKAV_SEARCH_TEXT_SORT_ORDER +
                            " limit 10",
                    new String[]{"%" + BtalkDialerDatabaseHelper.ENCRYPT_FILTER_SUFFIX + dialpadString.toString() + "%",
                            String.valueOf(currentTimeStamp),
                            dialpadString.toString()});
        }
        return loadCursor(cursor, stopWatch);
    }

    @Override
    public void deliverResult(DialerDatabaseHelper.ContactNumber[] contact) {
        if (DEBUG) {
            Log.v(TAG, "deliverResult " + (contact == null ? "null" : contact.length));
        }
        if (isReset()) {
            releaseResources();
            return;
        }
        DialerDatabaseHelper.ContactNumber[] oldContact = mContactSuggest;
        mContactSuggest = contact;

        //Bkav QuangNDb doi cach lay uri phu hop voi policy > android O
        if (mObserver == null) {
            mObserver = new ForceLoadContentObserver();
            mContext.getContentResolver().registerContentObserver(
                    DialerDatabaseHelper.getSmartDialUpdatedUri(), true, mObserver);
        }

        if (isStarted()) {
            super.deliverResult(contact);
        }

        if (oldContact != null && oldContact != contact) {
            releaseResources();
        }
    }

    @Override
    protected void onStartLoading() {
        if (DEBUG) {
            Log.v(TAG, "onStartLoading");
        }
        if (mContactSuggest != null) {
            /** Deliver any previously loaded data immediately. */
            deliverResult(mContactSuggest);
        }
        if (mContactSuggest == null) {
            /** Force loads every time as our results change with queries. */
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        if (DEBUG) {
            Log.v(TAG, "onStopLoading");
        }
        /** The Loader is in a stopped state, so we should attempt to cancel the current load. */
        cancelLoad();
    }

    @Override
    protected void onReset() {
        if (DEBUG) {
            Log.v(TAG, "onReset");
        }
        /** Ensure the loader has been stopped. */
        onStopLoading();
        if (mObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }

        /** Release all previously saved query results. */
        if (mContactSuggest != null) {
            releaseResources();
            mContactSuggest = null;
        }
    }

    @Override
    public void onCanceled(DialerDatabaseHelper.ContactNumber[] contacts) {
        if (DEBUG) {
            Log.v(TAG, "onCanceled");
        }
        super.onCanceled(contacts);
        if (mObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }

        /** The load has been canceled, so we should release the resources associated with 'data'.*/
        releaseResources();

    }

    private void releaseResources() {
        mContactSuggest = null;
    }

    private DialerDatabaseHelper.ContactNumber[] loadCursor(Cursor cursor, StopWatch stopWatch) {
        ArrayList<DialerDatabaseHelper.ContactNumber> temp = new ArrayList<>();
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

//            final Set<BtalkContactMatch> duplicates = new HashSet<>(); quangndb da tao bien global ben tren
            if (DEBUG) {
                stopWatch.lap("Moved cursor to start");
            }
            // Iterates the cursor to find top contact suggestions without duplication.
            while ((cursor.moveToNext())) {
                final long dataID = cursor.getLong(columnDataId);
                final String displayName = cursor.getString(columnDisplayNamePrimary);
                String phoneNumber = cursor.getString(columnNumber);
                final long id = cursor.getLong(columnId);
                final long photoId = cursor.getLong(columnPhotoId);
                final String lookupKey = cursor.getString(columnLookupKey);
                final int carrierPresence = cursor.getInt(columnCarrierPresence);
                String accountType = null;
                String accountName = null;
                if (!BtalkActivity.isAndroidQ()){
                    accountType = cursor.getString(columnAccountType);
                    accountName = cursor.getString(columnAccountName);
                }

                // Neu mot contact trung ten va so dien thoai thi bo qua
                final BtalkContactMatch contactMatch = new BtalkContactMatch(lookupKey, id, phoneNumber, displayName);
                if (mDuplicates.contains(contactMatch)) {
                    continue;
                }

                // If the contact has either the name or number that matches the query, add to the
                // result.
                // If a contact has not been added, add it to the result and the hash set.*/

                boolean isNumeric = mQuery.matches("-?\\d+(\\.\\d+)?");
                if (isNumeric) {
                    mNameMatcher = new SmartDialNameMatcher(mQuery, SmartDialPrefix.getMap(), mContext);
                    final boolean nameMatches = mNameMatcher.matches(displayName);
                    final boolean numberMatches =
                            (mNameMatcher.matchesNumber(phoneNumber, mQuery) != null);
                    if (!nameMatches && !numberMatches) {
                        final SmartDialPrefix.PhoneNumberTokens phoneNumberTokens = SmartDialPrefix.parsePhoneNumber(phoneNumber);

                        // Anhdts chuyen dang format E164 sang dang chung
                        if (phoneNumberTokens.countryCodeOffset != 0) {
                            String phoneFormat = "0" + SmartDialNameMatcher.normalizeNumber(phoneNumber,
                                    phoneNumberTokens.countryCodeOffset, mMap);
                            if (mNameMatcher.matchesNumber(phoneFormat, mQuery) == null) {
                                // If a contact has not been added, add it to the result and the hash set.*/
                                continue;
                            }
                            phoneNumber = phoneFormat;
                        }
                    }
                }

                mDuplicates.add(contactMatch);
                if (!BtalkActivity.isAndroidQ()){
                    temp.add(new DialerDatabaseHelper.ContactNumber(id, dataID, displayName, phoneNumber, lookupKey,
                            photoId, carrierPresence, accountType, accountName));
                } else {
                    temp.add(new DialerDatabaseHelper.ContactNumber(id, dataID, displayName, phoneNumber, lookupKey,
                            photoId, carrierPresence, null, null));
                }
                if (DEBUG) {
                    stopWatch.lap("Added one result: Name: " + displayName);
                }
            }

            if (DEBUG) {
                stopWatch.stopAndLog(TAG + "Finished loading cursor", 0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return temp.toArray(new DialerDatabaseHelper.ContactNumber[temp.size()]);
    }

}