/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.android.contacts.common;

import com.android.contacts.common.util.PermissionsUtil;
import com.google.common.annotations.VisibleForTesting;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;

/**
 * Used to create {@link CursorLoader}s to load different groups of
 * {@link com.android.contacts.list.ContactTileView}.
 */
public final class ContactTileLoaderFactory {

    public final static int CONTACT_ID = 0;
    public final static int DISPLAY_NAME = 1;
    public final static int STARRED = 2;
    public final static int PHOTO_URI = 3;
    public final static int LOOKUP_KEY = 4;
    public final static int ACCOUNT_TYPE = 5;
    public final static int ACCOUNT_NAME = 6;

    public final static int CONTACT_PRESENCE = !PermissionsUtil.isAndroidQ() ? 7 : 5;
    public final static int CONTACT_STATUS = !PermissionsUtil.isAndroidQ() ? 8 : 6;
    //HienDTk: tren android 10 khong co 2 cot RawContacts.ACCOUNT_TYPE va RawContacts.ACCOUNT_NAME
    // Only used for StrequentPhoneOnlyLoader
    public final static int PHONE_NUMBER = !PermissionsUtil.isAndroidQ() ? 7 : 5;
    public final static int PHONE_NUMBER_TYPE = !PermissionsUtil.isAndroidQ() ? 8 : 6;
    public final static int PHONE_NUMBER_LABEL = !PermissionsUtil.isAndroidQ() ? 9 : 7;
    public final static int IS_DEFAULT_NUMBER = !PermissionsUtil.isAndroidQ() ? 10 : 8;
    public final static int PINNED = !PermissionsUtil.isAndroidQ() ? 11 : 9;
    // The _ID field returned for strequent items actually contains data._id instead of
    // contacts._id because the query is performed on the data table. In order to obtain the
    // contact id for strequent items, we thus have to use Phone.contact_id instead.
    public final static int CONTACT_ID_FOR_DATA = !PermissionsUtil.isAndroidQ() ? 12 : 10;
    public final static int DISPLAY_NAME_ALTERNATIVE = !PermissionsUtil.isAndroidQ() ? 13 : 11;

    private static final String[] COLUMNS = !PermissionsUtil.isAndroidQ() ?
            new String[]{
                    Contacts._ID, // ..........................................0
                    Contacts.DISPLAY_NAME, // .................................1
                    Contacts.STARRED, // ......................................2
                    Contacts.PHOTO_URI, // ....................................3
                    Contacts.LOOKUP_KEY, // ...................................4
                    RawContacts.ACCOUNT_TYPE, //                               5
                    RawContacts.ACCOUNT_NAME, //                               6
                    Contacts.CONTACT_PRESENCE, // .............................7
                    Contacts.CONTACT_STATUS, // ...............................8
            } : new String[]{
            Contacts._ID, // ..........................................0
            Contacts.DISPLAY_NAME, // .................................1
            Contacts.STARRED, // ......................................2
            Contacts.PHOTO_URI, // ....................................3
            Contacts.LOOKUP_KEY, // ...................................4
//      RawContacts.ACCOUNT_TYPE, //                               5
//      RawContacts.ACCOUNT_NAME, //                               6
            Contacts.CONTACT_PRESENCE, // .............................7
            Contacts.CONTACT_STATUS, // ...............................8
    };

    /**
     * Projection used for the {@link Contacts#CONTENT_STREQUENT_URI}
     * query when {@link ContactsContract#STREQUENT_PHONE_ONLY} flag
     * is set to true. The main difference is the lack of presence
     * and status data and the addition of phone number and label.
     */
    @VisibleForTesting
    public static final String[] COLUMNS_PHONE_ONLY = !PermissionsUtil.isAndroidQ() ?
            new String[]{
                    Contacts._ID, // ..........................................0
                    Contacts.DISPLAY_NAME_PRIMARY, // .........................1
                    Contacts.STARRED, // ......................................2
                    Contacts.PHOTO_URI, // ....................................3
                    Contacts.LOOKUP_KEY, // ...................................4
                    RawContacts.ACCOUNT_TYPE, //                               5
                    RawContacts.ACCOUNT_NAME, //                               6
                    Phone.NUMBER, // ..........................................7
                    Phone.TYPE, // ............................................8
                    Phone.LABEL, // ...........................................9
                    Phone.IS_SUPER_PRIMARY, //.................................10
                    Contacts.PINNED, // .......................................11
                    Phone.CONTACT_ID, //.......................................12
                    Contacts.DISPLAY_NAME_ALTERNATIVE, // .....................13
            } : new String[]{
            Contacts._ID, // ..........................................0
            Contacts.DISPLAY_NAME_PRIMARY, // .........................1
            Contacts.STARRED, // ......................................2
            Contacts.PHOTO_URI, // ....................................3
            Contacts.LOOKUP_KEY, // ...................................4
//        RawContacts.ACCOUNT_TYPE, //                               5
//        RawContacts.ACCOUNT_NAME, //                               6
            Phone.NUMBER, // ..........................................7
            Phone.TYPE, // ............................................8
            Phone.LABEL, // ...........................................9
            Phone.IS_SUPER_PRIMARY, //.................................10
            Contacts.PINNED, // .......................................11
            Phone.CONTACT_ID, //.......................................12
            Contacts.DISPLAY_NAME_ALTERNATIVE, // .....................13
    };

    private static final String STARRED_ORDER = Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC";

    public static CursorLoader createStrequentLoader(Context context) {
        return new CursorLoader(context, Contacts.CONTENT_STREQUENT_URI, COLUMNS, null, null,
                STARRED_ORDER);
    }

    public static CursorLoader createStrequentPhoneOnlyLoader(Context context) {
        Uri uri = Contacts.CONTENT_STREQUENT_URI.buildUpon()
                .appendQueryParameter(ContactsContract.STREQUENT_PHONE_ONLY, "true").build();

        return new CursorLoader(context, uri, COLUMNS_PHONE_ONLY, null, null, null);
    }

    public static CursorLoader createStarredLoader(Context context) {
        return new CursorLoader(context, Contacts.CONTENT_URI, COLUMNS, Contacts.STARRED + "=?",
                new String[]{"1"}, STARRED_ORDER);
    }

    public static CursorLoader createFrequentLoader(Context context) {
        return new CursorLoader(context, Contacts.CONTENT_FREQUENT_URI, COLUMNS,
                Contacts.STARRED + "=?", new String[]{"0"}, null);
    }


}
