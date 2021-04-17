/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.dialer.compat;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.BlockedNumberContract;
import android.provider.BlockedNumberContract.BlockedNumbers;

public class BlockedNumbersSdkCompat {

    public static final Uri CONTENT_URI = isAndroidN() ?
            BlockedNumbers.CONTENT_URI : null;

    public static final String _ID = isAndroidN() ? BlockedNumbers.COLUMN_ID : null;

    public static final String COLUMN_ORIGINAL_NUMBER = isAndroidN() ?
            BlockedNumbers.COLUMN_ORIGINAL_NUMBER : null;

    public static final String E164_NUMBER = isAndroidN() ? BlockedNumbers.COLUMN_E164_NUMBER : null;

    public static boolean canCurrentUserBlockNumbers(Context context) {
        return isAndroidN() ? BlockedNumberContract.canCurrentUserBlockNumbers(context) : null;
    }

    private static boolean isAndroidN() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }
}