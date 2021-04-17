/*
 * Copyright (C) 2009 The Android Open Source Project
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
package bkav.android.btalk.backup.vcard;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;
import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * <P>.
 *
 * {@link VCardEntryHandler} implementation which commits the entry to ContentResolver.
 * </P>
 * <P>
 * Note:<BR />
 * Each vCard may contain big photo images encoded by BASE64,
 * If we store all vCard entries in memory, OutOfMemoryError may be thrown.
 * Thus, this class push each VCard entry into ContentResolver immediately.
 * </P>
 */
public class VCardEntryCommitter implements VCardEntryHandler {
    
    /** The log tag. */
    public static String LOG_TAG = "VCardEntryComitter";

    /** The m content resolver. */
    private final ContentResolver mContentResolver;
    
    /** The m time to commit. */
    private long mTimeToCommit;
    
    /** The m created uris. */
    private ArrayList<Uri> mCreatedUris = new ArrayList<Uri>();

    /**
     * Instantiates a new v card entry committer.
     *
     * @param resolver the resolver
     */
    public VCardEntryCommitter(ContentResolver resolver) {
        mContentResolver = resolver;
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardEntryHandler#onStart()
     */
    public void onStart() {
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardEntryHandler#onEnd()
     */
    public void onEnd() {
        if (VCardConfig.showPerformanceLog()) {
            Log.d(LOG_TAG, String.format("time to commit entries: %d ms", mTimeToCommit));
        }
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardEntryHandler#onEntryCreated(bms.backup.VCardEntry)
     */
    public void onEntryCreated(final VCardEntry contactStruct) {
        long start = System.currentTimeMillis();
		mCreatedUris.add(contactStruct.pushIntoContentResolver(mContentResolver));
		mTimeToCommit += System.currentTimeMillis() - start;
    }

    /**
     * Returns the list of created Uris. This list should not be modified by the caller as it is
     * not a clone.
     *
     * @return the created uris
     */
   public ArrayList<Uri> getCreatedUris() {
        return mCreatedUris;
    }
}