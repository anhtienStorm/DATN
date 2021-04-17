/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License.
 */
package com.android.messaging.datamodel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.android.messaging.Factory;
import com.android.messaging.util.Assert;
import com.android.messaging.util.LogUtil;

public class DatabaseUpgradeHelper {
    private static final String TAG = LogUtil.BUGLE_DATABASE_TAG;

    public void doOnUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Assert.isTrue(newVersion >= oldVersion);
        if (oldVersion == newVersion) {
            return;
        }

        LogUtil.i(TAG, "Database upgrade started from version " + oldVersion + " to " + newVersion);
        try {
            doUpgradeWithExceptions(db, oldVersion, newVersion);
            LogUtil.i(TAG, "Finished database upgrade");
        } catch (final Exception ex) {
            LogUtil.e(TAG, "Failed to perform db upgrade from version " +
                    oldVersion + " to version " + newVersion, ex);
            DatabaseHelper.rebuildTables(db);
        }
    }

    public void doUpgradeWithExceptions(final SQLiteDatabase db, final int oldVersion,
            final int newVersion) throws Exception {
        int currentVersion = oldVersion;
        final Context context = Factory.get().getApplicationContext();
        if (currentVersion < 2) {
            currentVersion = upgradeToVersion2(db);
            // Rebuild all the views
            DatabaseHelper.dropAllViews(db);
            DatabaseHelper.rebuildAllViews(new DatabaseWrapper(context, db));
        }
        // Bkav QuangNDb updatedatabase nen version 3
        if (currentVersion < 3) {
            currentVersion = upgradeToVersion3(db);
        }
        // Bkav QuangNDb update database nen version 4 vi them cot slot id de nhan biet sim1, sim2 cho tung tin nhan
        if (currentVersion < 4) {
            currentVersion = upgradeToVersion4(db);
            DatabaseHelper.dropAllViews(db);
            DatabaseHelper.rebuildAllViews(new DatabaseWrapper(context, db));
        }
        // Bkav QuangNDb update database nen version 5 vi them bang call record
        if (currentVersion < 5) {
            currentVersion = upgradeToVersion5(db);
        }
        // Bkav QuangNDb update database nen version 6 vi them bang quick_reponse
        if (currentVersion < 6) {
            currentVersion = upgradeToVersion6(db);
        }
        // Bkav QuangNDb update database nen version 6 vi them bang quick_reponse
        if (currentVersion < 7) {
            currentVersion = upgradeToVersion7(db);
        }

        // Bkav QuangNDb update database them cot iccid vao participant
        if (currentVersion < 8) {
            currentVersion = upgradeToVersion8(db);
        }
        // Finally, check if we have arrived at the final version.
        checkAndUpdateVersionAtReleaseEnd(currentVersion, Integer.MAX_VALUE, newVersion);
    }

    private int upgradeToVersion2(final SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + DatabaseHelper.CONVERSATIONS_TABLE + " ADD COLUMN " +
                DatabaseHelper.ConversationColumns.IS_ENTERPRISE + " INT DEFAULT(0)");
        LogUtil.i(TAG, "Ugraded database to version 2");
        return 2;
    }


    /**
     * Checks db version correctness at the end of each milestone release. If target database
     * version lies beyond the version range that the current release may handle, we snap the
     * current version to the end of the release, so that we may go on to the next release' upgrade
     * path. Otherwise, if target version is within reach of the current release, but we are not
     * at the target version, then throw an exception to force a table rebuild.
     */
    private int checkAndUpdateVersionAtReleaseEnd(final int currentVersion,
            final int maxVersionForRelease, final int targetVersion) throws Exception {
        if (maxVersionForRelease < targetVersion) {
            // Target version is beyond the current release. Snap to max version for the
            // current release so we can go on to the upgrade path for the next release.
            return maxVersionForRelease;
        }

        // If we are here, this means the current release' upgrade handler should upgrade to
        // target version...
        if (currentVersion != targetVersion) {
            // No more upgrade handlers. So we can't possibly upgrade to the final version.
            throw new Exception("Missing upgrade handler from version " +
                    currentVersion + " to version " + targetVersion);
        }
        // Upgrade succeeded.
        return targetVersion;
    }

    public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        DatabaseHelper.rebuildTables(db);
        LogUtil.e(TAG, "Database downgrade requested for version " +
                oldVersion + " version " + newVersion + ", forcing db rebuild!");
    }

    //--------------------------------------------------------------------------------BKAV-----------------------------------------------
    /**
     * Bkav QuangNDb update database nen version 3 do them column text_search
     */
    private int upgradeToVersion3(final SQLiteDatabase db) {
        // TODO: 03/07/2017  Huong giai quyet la drop toan bo table di de rebuild lai cac bang
        DatabaseHelper.rebuildTables(db);
        LogUtil.i(TAG, "Ugraded database to version 3");
        return 3;
    }
    /**
     * Bkav QuangNDb update database nen version 4 do them column incoming_slot_id
     */
    private int upgradeToVersion4(final SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + DatabaseHelper.MESSAGES_TABLE + " ADD COLUMN " +
                DatabaseHelper.MessageColumns.INCOMING_SLOT_ID + " INT DEFAULT(-1)");
        LogUtil.i(TAG, "Ugraded database to version 2");
        return 4;
    }

    /**
     * Bkav QuangNDb update database them bang call record
     */
    private int upgradeToVersion5(final SQLiteDatabase db) {
        DatabaseHelper.rebuildTables(db);
        LogUtil.i(TAG, "Ugraded database to version 5");
        return 5;
    }

    /**
     * Bkav QuangNDb update database them bang quick response
     */
    private int upgradeToVersion6(final SQLiteDatabase db) {
        DatabaseHelper.rebuildTables(db);
        LogUtil.i(TAG, "Ugraded database to version 5");
        return 6;
    }
    /**
     * Bkav QuangNDb update database them bang fts conversation va them cot name_unsign trong bang conversations
     */
    private int upgradeToVersion7(final SQLiteDatabase db) {
        DatabaseHelper.rebuildTables(db);
        LogUtil.i(TAG, "Ugraded database to version 5");
        return 7;
    }

    /**
     * Bkav QuangNDb update database them bang fts conversation va them cot name_unsign trong bang conversations
     */
    private int upgradeToVersion8(final SQLiteDatabase db) {
        DatabaseHelper.rebuildTables(db);
        return 8;
    }
}
