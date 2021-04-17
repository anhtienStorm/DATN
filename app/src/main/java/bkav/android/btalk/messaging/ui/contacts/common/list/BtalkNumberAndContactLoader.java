package bkav.android.btalk.messaging.ui.contacts.common.list;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

import com.android.contacts.common.compat.CompatUtils;
import com.google.common.collect.Lists;

import java.util.List;

import bkav.android.btalk.contacts.BtalkProfileAndContactsLoader;

/**
 * Created by quangnd on 22/04/2017.
 * Loader thuc hien viec load du lieu trong fragment pick contact(BtalkContactPhoneNumberFragment)
 */

public class BtalkNumberAndContactLoader extends BtalkProfileAndContactsLoader {

    private boolean mIsSearchMode = false;

    private static int mCountStarredContacts = 0;

    private static int mCountFrequentContacts = 0;

    private long mDirectoryId;
    private String mQuery = "";

    // Anhdts: gioi han so luong hien thi cac so thuong xuyen lien lac
    private static final int MAXIMUM_QUERY_COUNT = 10;

    private static final int MAXIMUM_SEARCH_COUNT = 3;

    private static final int MIN_TIMES_USED_FREQUENT = 3;

    public BtalkNumberAndContactLoader(Context context) {
        super(context);
    }

    private static final String[] CONTACT_STARED_PROJECTION_PRIMARY = new String[]{
            ContactsContract.CommonDataKinds.Phone._ID,                          // 0
            ContactsContract.CommonDataKinds.Phone.TYPE,                         // 1
            ContactsContract.CommonDataKinds.Phone.LABEL,                        // 2
            ContactsContract.CommonDataKinds.Phone.NUMBER,                       // 3
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,                   // 4
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,                   // 5
            ContactsContract.CommonDataKinds.Phone.PHOTO_ID,                     // 6
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,         // 7
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,          // 8
            ContactsContract.RawContacts.ACCOUNT_TYPE,           // 9
            ContactsContract.RawContacts.ACCOUNT_NAME,           // 10
    };

    private static final String[] PROJECTION_PRIMARY;

    static {
        final List<String> projectionList = Lists.newArrayList(CONTACT_STARED_PROJECTION_PRIMARY);
        if (CompatUtils.isMarshmallowCompatible()) {
            projectionList.add(ContactsContract.CommonDataKinds.Phone.CARRIER_PRESENCE); // 11
        }
        PROJECTION_PRIMARY = projectionList.toArray(new String[projectionList.size()]);
    }

    @Override
    public Cursor loadStarredContacts() {
        Cursor cursor;
        mCountStarredContacts = 0;
        if (mIsSearchMode) {
            Uri baseUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            Uri.Builder builder = baseUri.buildUpon().appendQueryParameter(
                    ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(ContactsContract.Directory.DEFAULT));
            String selection = ContactsContract.CommonDataKinds.Phone.STARRED + " = 1 AND (UPPER(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") LIKE UPPER('%" + mQuery + "%') " +
                    "OR UPPER(" + ContactsContract.CommonDataKinds.Phone.NUMBER + ") LIKE UPPER('%" + mQuery + "%'))";
            cursor = getContext().getContentResolver().query(builder.build(),
                    PROJECTION_PRIMARY,
                    selection, null, STARRED_ORDER);
        } else {
            cursor = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    PROJECTION_PRIMARY,
                    ContactsContract.CommonDataKinds.Phone.STARRED + "=?", new String[]{"1"}, STARRED_ORDER);
        }

        if (cursor != null) {
            mCountStarredContacts = cursor.getCount();
        }
        return cursor;
    }

    private static final String TAG = "BtalkNumberAndContact";

    @Override
    public Cursor loadFrequentContacts() {
        Cursor cursor;
        mCountFrequentContacts = 0;
        // TODO: 07/06/2017 Bkav QuangNDb dung search giong cua anhndd
        String sortOrder;
        String selection = null;
        String[] arg = null;
        Log.d(TAG, "loadFrequentContacts: " + getSortByRecentCall(String.valueOf(System.currentTimeMillis())));
        sortOrder = SORT_BY_LOOKUP_KEY + ", "
                + getSortByRecentCall(String.valueOf(System.currentTimeMillis())) + ", " // Sap xep theo thoi gian goi
                + ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED + " DESC, "  // Sap xep theo so lan lien lac
                + getSortByAbsoluteNameSame("'" + mQuery + "'") + ", "            //sap xep theo do trung ten
                + ContactsContract.CommonDataKinds.Phone.STARRED + " DESC, "          // Sap xep theo danh ba yeu thich
                + getSortByLevelName("'" + mQuery + "%" + "'") + ", "                // sap xep theo vi tri cua ten
                + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + ", "
                + BaseColumns._ID + " limit " + (mQuery.isEmpty() ? MAXIMUM_QUERY_COUNT : MAXIMUM_SEARCH_COUNT);
        String fromContentTablePhone = "view_data data LEFT OUTER JOIN (SELECT data_usage_stat.data_id as STAT_DATA_ID, SUM(data_usage_stat.times_used) as times_used, MAX(data_usage_stat.last_time_used) as last_time_used FROM data_usage_stat GROUP BY data_usage_stat.data_id) as data_usage_stat ON (STAT_DATA_ID=data._id)";
        if (!mQuery.isEmpty()) {
            selection = "(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " LIKE ? or " +
                    ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?" + ")" + "and " +
                    ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED + " > " + MIN_TIMES_USED_FREQUENT;
            arg = new String[]{"%" + mQuery + "%", "%" + mQuery + "%"};
        } else {
            selection = ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED + " > " + MIN_TIMES_USED_FREQUENT + selectionBkavConnect();//Bkav QuangNDb Them selection them so dt bkav connect;
        }
        selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                + " IN (select " + ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                + " from " + fromContentTablePhone
                + " where " + selection
                + " group by " + ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                + " order by " + sortOrder
                + ")";
//         Bkav QuangNDB bo sung sort oder
        sortOrder = getSortByRecentCall(String.valueOf(System.currentTimeMillis())) + ", " // Sap xep theo thoi gian goi
                + ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED + " DESC, "  // Sap xep theo so lan lien lac
                + getSortByAbsoluteNameSame("'" + mQuery + "'") + ", "               // sap xep theo do trung ten
                + ContactsContract.CommonDataKinds.Phone.STARRED + " DESC, "          // Sap xep theo danh ba yeu thich
                + getSortByLevelName("'" + mQuery + "%" + "'") + ", "                // sap xep theo vi tri cua ten
                + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY;
        cursor = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION_PRIMARY,
                selection, arg, sortOrder);
        if (cursor != null) {
            mCountFrequentContacts = cursor.getCount();
        }
        return cursor;
    }

    @Override
    public void addCursorFrequentAndCursorStarred(List<Cursor> cursors) {
        //quangndb bo cursor load frequent va favorite di
    }

    public void setIsSearchMode(boolean isSearchMode) {
        this.mIsSearchMode = isSearchMode;
    }

    public void setQuery(String query) {
        this.mQuery = query.replace("'", "''");
    }

    public void setDirectoryId(long directoryId) {
        this.mDirectoryId = directoryId;
    }

    public static int getCountStarredContacts() {
        return mCountStarredContacts;
    }

    public static int getCountFrequentContacts() {
        return mCountFrequentContacts;
    }

    private interface SearchSmart {

        long MAGIC_CHECK_TIME_RECENT_CALL = 60L * 60 * 1000;

        String TIME_SINCE_LAST_USED_MS = "( %s - " +
                ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED + ")";
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
         * Anhdts
         * neu trung khi voi tu khoa tim kiem thi dua len tren
         */
        String SORT_BY_ABSOLUTE_NAME_SAME =
                "CASE WHEN " + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " LIKE " + "%s" +
                        " THEN " + 0 +
                        " ELSE 1 END";

        /**
         * Anhdts change sort follow company
         * sap xep theo vi tri tu khoa o dau
         */
        String SORT_BY_NAME_LIKE_LEVEL =
                "(CASE WHEN " + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " LIKE " + "%s" +
                        " THEN " + 0 +
                        " WHEN " + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " LIKE " + "%s" +
                        " THEN " + 1 +
                        " ELSE 2 END)";
    }

    /**
     * Anhdts sap xep theo thoi gian goi
     */
    private String getSortByRecentCall(String timeCurrent) {
        return String.format(BtalkNumberAndContactLoader.SearchSmart.SORT_BY_IS_RECENT_CALL, timeCurrent, timeCurrent);
    }

    /**
     * Anhdts sap xep theo dung thanh phan ten
     */
    private String getSortByAbsoluteNameSame(String key) {
        return String.format(BtalkNumberAndContactLoader.SearchSmart.SORT_BY_ABSOLUTE_NAME_SAME, key);
    }

    protected String[] getContactFrequentlyProjection() {
        return PROJECTION_PRIMARY;
    }

    /**
     * Anhdts sap xep theo vi tri tu khoa trong ten
     */
    private String getSortByLevelName(String key) {
        return String.format(BtalkNumberAndContactLoader.SearchSmart.SORT_BY_NAME_LIKE_LEVEL, key, " " + key);
    }
}