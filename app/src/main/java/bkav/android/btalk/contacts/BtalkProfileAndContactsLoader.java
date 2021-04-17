package bkav.android.btalk.contacts;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;

import com.android.contacts.common.compat.ContactsCompat;
import com.android.contacts.common.list.ProfileAndContactsLoader;

import java.util.List;

import bkav.android.btalk.activities.BtalkActivity;

/**
 * AnhNDd: class kế thừa ProfileAndContactsLoader để thực hiện query thêm các số điện thoại
 * thường xuyên liên hệ và số điện thoại yêu thích.
 */
public class BtalkProfileAndContactsLoader extends ProfileAndContactsLoader {

    //AnhNDd: string de tim kiem
    private String mQueryString = "";

    //AnhNDd: id cua directory em vẫn chưa hiểu cái này.
    private long mDirectoryId;

    /**
     * AnhNDd: boolean để kiểm tra việc có trong search mode hay không
     */
    private boolean mSearchMode;

    // Anhdts: gioi han so luong hien thi cac so thuong xuyen lien lac
    private static final int MAXIMUM_QUERY_COUNT = 10;

    private static final int MAXIMUM_SEARCH_COUNT = 3;

    private static final int MIN_TIMES_USED_FREQUENT = 3;

    //AnhNDd: do lúc query trong lúc search thì bên profilecontacts yêu cầu thêm trường dữ liệu mà trong bảng,
    //yêu thích và liên hệ thường xuyên thì không có, nên xử dụng mặc định sẽ lấy ra những trường này.
    private static final String[] CONTACT_PROJECTION_PRIMARY = !BtalkActivity.isAndroidQ() ?
            new String[]{
            Contacts._ID,                           // 0
            Contacts.DISPLAY_NAME_PRIMARY,          // 1
            Contacts.CONTACT_PRESENCE,              // 2
            Contacts.CONTACT_STATUS,                // 3
            Contacts.PHOTO_ID,                      // 4
            Contacts.PHOTO_THUMBNAIL_URI,           // 5
            Contacts.LOOKUP_KEY,                    // 6
            Contacts.IS_USER_PROFILE,               // 7
            Contacts.PHONETIC_NAME,                 // 8
            ContactsContract.RawContacts.ACCOUNT_TYPE,               // 9
            ContactsContract.RawContacts.ACCOUNT_NAME,               // 10
            // Anhdts them cac truong de sap xep
            Contacts.LAST_TIME_CONTACTED,           // 11
            Contacts.STARRED,                       // 12
            Contacts.TIMES_CONTACTED,                // 13
    } : new String[]{
            Contacts._ID,                           // 0
            Contacts.DISPLAY_NAME_PRIMARY,          // 1
            Contacts.CONTACT_PRESENCE,              // 2
            Contacts.CONTACT_STATUS,                // 3
            Contacts.PHOTO_ID,                      // 4
            Contacts.PHOTO_THUMBNAIL_URI,           // 5
            Contacts.LOOKUP_KEY,                    // 6
            Contacts.IS_USER_PROFILE,               // 7
            Contacts.PHONETIC_NAME,                 // 8
//            ContactsContract.RawContacts.ACCOUNT_TYPE,               // 9
//            ContactsContract.RawContacts.ACCOUNT_NAME,               // 10
            // Anhdts them cac truong de sap xep
            Contacts.LAST_TIME_CONTACTED,           // 11
            Contacts.STARRED,                       // 12
            Contacts.TIMES_CONTACTED,                // 13
    };


    /**
     * AnhNDd: tổng số điện thoại yêu thích.
     */
    private static int mCountStarredContacts = 0;

    /**
     * AnhNDd: Tổng số điện thoại thường xuyên liên hệ.
     */
    private static int mCountFrequentContacts = 0;

    protected static final String STARRED_ORDER = Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC";

    public BtalkProfileAndContactsLoader(Context context) {
        super(context);
    }

    private static final String TAG = "BtalkProfileAndContacts";

    @Override
    public Cursor loadFrequentContacts() {
        /*final Uri.Builder builder = Contacts.CONTENT_STREQUENT_FILTER_URI.buildUpon();
        builder.appendPath("c"); // Builder will encode the query
        builder.appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY,
                String.valueOf(0));
        builder.appendQueryParameter(ContactsContract.SearchSnippets.DEFERRED_SNIPPETING_KEY, "1");
        Cursor cursor = getContext().getContentResolver().query(builder.build(), CONTACT_PROJECTION_PRIMARY,
                null, null, FREQUENT_ORDER);*/
        // Anhdts: neu khong search thi load toi da 10 contact
        // neu search thi load toi da 3 contact
        // Neu nhap Qu thi chi search nhung contact co Qu o dau moi tu vi du Quang hoac Anh Quyet
        String sortOrder;
        String selection;
        String query = mQueryString.replaceAll("'", "''");
        sortOrder = SORT_BY_LOOKUP_KEY + ", "
                + getSortByRecentCall(String.valueOf(System.currentTimeMillis())) + ", " // Sap xep theo thoi gian goi
                + Contacts.TIMES_CONTACTED + " DESC, "                                      // Sap xep theo so lan lien lac
                + getSortByAbsoluteNameSame("'" + query + "'") + ", "               // sap xep theo do trung ten
                + Contacts.STARRED + " DESC, "                                             // Sap xep theo danh ba yeu thich
                + getSortByLevelName("'" + query + "%" + "'") + ", "                // sap xep theo vi tri cua ten
                + Contacts.DISPLAY_NAME_PRIMARY + ", "
                + BaseColumns._ID + " limit " + (mQueryString.isEmpty() ? MAXIMUM_QUERY_COUNT : MAXIMUM_SEARCH_COUNT);
        if (!mQueryString.isEmpty()) {
            selection = Contacts.TIMES_CONTACTED + " > " + MIN_TIMES_USED_FREQUENT;
        } else {
            selection = Contacts.TIMES_CONTACTED + " > " + MIN_TIMES_USED_FREQUENT + selectionBkavConnect();//Bkav QuangNDb Them selection them so dt bkav connect
        }
        // Anhdts ???? cai nay o dau ra, ai them vao????
//        sortOrder = getSortByRecentCall(String.valueOf(System.currentTimeMillis())) + ", " // Sap xep theo thoi gian goi
//                + Contacts.TIMES_CONTACTED + " DESC, "                                      // Sap xep theo so lan lien lac
//                + getSortByAbsoluteNameSame("'" + mQueryString + "'") + ", "               // sap xep theo do trung ten
//                + Contacts.STARRED + " DESC, "                                             // Sap xep theo danh ba yeu thich
//                + getSortByLevelName("'" + mQueryString + "%" + "'") + ", "                // sap xep theo vi tri cua ten
//                + Contacts.DISPLAY_NAME_PRIMARY + ", "
//                + sortOrder;
        try {
            Cursor cursor;
            if (mQueryString.isEmpty()) {
                cursor = getContext().getContentResolver().query(Contacts.CONTENT_URI, CONTACT_PROJECTION_PRIMARY,
                        selection, null, sortOrder);
            } else {
                final Uri.Builder builder = ContactsCompat.getContentUri().buildUpon();
                builder.appendPath(mQueryString); // Builder will encode the query
                builder.appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY,
                        String.valueOf(mDirectoryId));
                builder.appendQueryParameter(ContactsContract.SearchSnippets.DEFERRED_SNIPPETING_KEY, "1");
                cursor = getContext().getContentResolver().query(builder.build(), CONTACT_PROJECTION_PRIMARY,
                        selection, null, sortOrder);
            }
            if (cursor != null) {
                mCountFrequentContacts = cursor.getCount();
            }
            return cursor;
        } catch (Exception e) {
            mCountFrequentContacts = 0;
            return null;
        }
    }

    @Override
    public Cursor loadStarredContacts() {
        mCountStarredContacts = 0;
        Cursor cursor;
        try {
            if (mQueryString.isEmpty()) {
                //AnhNDd: Lấy ra tất cả các số điện thoại yêu thích.
                cursor = getContext().getContentResolver().query(Contacts.CONTENT_URI, getContactFrequentlyProjection(),
                        Contacts.STARRED + "=?", new String[]{"1"}, STARRED_ORDER);
            } else {
                //AnhNDd: tìm kiếm số điện thoại yêu thích.
                final Uri.Builder builder = ContactsCompat.getContentUri().buildUpon();
                builder.appendPath(mQueryString); // Builder will encode the query
                builder.appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY,
                        String.valueOf(mDirectoryId));
                builder.appendQueryParameter(ContactsContract.SearchSnippets.DEFERRED_SNIPPETING_KEY, "1");
                cursor = getContext().getContentResolver().query(builder.build(), getContactFrequentlyProjection(),
                        Contacts.STARRED + "=?", new String[]{"1"}, STARRED_ORDER);
            }
            // Anhdts check null
            if (cursor != null) {
                mCountStarredContacts = cursor.getCount();
            }
            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void addCursorFrequentAndCursorStarred(List<Cursor> cursors) {
        if (!mSearchMode) {
            super.addCursorFrequentAndCursorStarred(cursors);
        }
    }

    public static int getCountStarredContacts() {
        return mCountStarredContacts;
    }

    public static int getCountFrequentContacts() {
        return mCountFrequentContacts;
    }

    void setUpToSearch(String query, long directoryId, boolean searchMode) {
        mQueryString = query;
        mDirectoryId = directoryId;
        mSearchMode = searchMode;
    }

    private interface SearchSmart {

        long MAGIC_CHECK_TIME_RECENT_CALL = 60L * 60 * 1000;

        String TIME_SINCE_LAST_USED_MS = "( %s - " +
                Contacts.LAST_TIME_CONTACTED + ")";
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
                "CASE WHEN " + Contacts.DISPLAY_NAME_PRIMARY + " LIKE " + "%s" +
                        " THEN " + 0 +
                        " ELSE 1 END";

        /**
         * Anhdts change sort follow company
         * sap xep theo vi tri tu khoa o dau
         */
        String SORT_BY_NAME_LIKE_LEVEL =
                "(CASE WHEN " + Contacts.DISPLAY_NAME_PRIMARY + " LIKE " + "%s" +
                        " THEN " + 0 +
                        " WHEN " + Contacts.DISPLAY_NAME_PRIMARY + " LIKE " + "%s" +
                        " THEN " + 1 +
                        " ELSE 2 END)";
    }

    /**
     * Anhdts sap xep theo thoi gian goi
     */
    private String getSortByRecentCall(String timeCurrent) {
        return String.format(SearchSmart.SORT_BY_IS_RECENT_CALL, timeCurrent, timeCurrent);
    }

    /**
     * Anhdts sap xep theo dung thanh phan ten
     */
    private String getSortByAbsoluteNameSame(String key) {
        return String.format(SearchSmart.SORT_BY_ABSOLUTE_NAME_SAME, key);
    }

    protected String[] getContactFrequentlyProjection() {
        return CONTACT_PROJECTION_PRIMARY;
    }

    /**
     * Anhdts sap xep theo vi tri tu khoa trong ten
     */
    private String getSortByLevelName(String key) {
        return String.format(SearchSmart.SORT_BY_NAME_LIKE_LEVEL, key, " " + key);
    }

    /**
     * Bkav QuangNDb Check contact exist
     */
    protected String getLookupKey(String number) {
        if (number != null) {
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            String[] projection = {ContactsContract.PhoneLookup.LOOKUP_KEY};
            Cursor cur = getContext().getContentResolver().query(lookupUri, projection, null, null, null);
            try {
                if (cur != null && cur.moveToFirst()) {
                    return cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.LOOKUP_KEY));
                }
            } finally {
                if (cur != null)
                    cur.close();
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * Bkav QuangNDb them selection bkav connect
     */
    protected String selectionBkavConnect() {
        String lookupKey = getLookupKeyBkavConnect();
        return lookupKey == null ? "" : " OR " + Contacts.LOOKUP_KEY + " like '" + lookupKey + "'";
    }

    /**
     * Bkav QuangNDb ham get lookup key cua bkav cskh
     */
    protected String getLookupKeyBkavConnect() {
        final String mobileNumber = "1900545499";
        return getLookupKey(mobileNumber);
    }

    //Bkav QuangNDb Sort de luon cho thang bkav contact len dau
    protected String SORT_BY_LOOKUP_KEY =
            "CASE WHEN " + Contacts.LOOKUP_KEY + " LIKE '" + getLookupKeyBkavConnect() + "'" +
                    " THEN " + 0 +
                    " ELSE 1 END";

}
