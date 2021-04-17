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
package com.android.dialer.util;

import android.provider.CallLog.Calls;

public final class AppCompatConstants {

    public static final int CALLS_INCOMING_TYPE = Calls.INCOMING_TYPE;
    public static final int CALLS_OUTGOING_TYPE = Calls.OUTGOING_TYPE;
    public static final int CALLS_MISSED_TYPE = Calls.MISSED_TYPE;
    public static final int CALLS_VOICEMAIL_TYPE = Calls.VOICEMAIL_TYPE;
    // Added to android.provider.CallLog.Calls in N+.
    public static final int CALLS_REJECTED_TYPE = 5;
    // Added to android.provider.CallLog.Calls in N+.
    public static final int CALLS_BLOCKED_TYPE = 6;

    /**
     * Anhdts tren rom 8 thay doi lai may gia tri nay
     */
    /**
     * Call log type for incoming IMS calls.
     * @hide
     */
    public static final int INCOMING_IMS_TYPE = 1000;
    /**
     * Call log type for outgoing IMS calls.
     * @hide
     */
    public static final int OUTGOING_IMS_TYPE = 1001;
    /**
     * Call log type for missed IMS calls.
     * @hide
     */
    public static final int MISSED_IMS_TYPE = 1002;

    // Bkav TrungTh - thay vao
    public static final int INCOMING_WIFI_TYPE = 20/*Calls.INCOMING_WIFI_TYPE*/;
    public static final int OUTGOING_WIFI_TYPE = 21/*Calls.OUTGOING_WIFI_TYPE*/;
    public static final int MISSED_WIFI_TYPE = 22/*Calls.MISSED_WIFI_TYPE*/;
}
