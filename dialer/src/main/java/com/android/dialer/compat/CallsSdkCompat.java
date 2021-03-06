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
 * limitations under the License.
 */

package com.android.dialer.compat;

import android.provider.CallLog;
import android.support.annotation.Nullable;

public class CallsSdkCompat {
    // Bkav TrungTH - thay gia tri null bang ten cot cho dung
    @Nullable public static final String POST_DIAL_DIGITS =  CallLog.Calls.POST_DIAL_DIGITS;
    @Nullable public static final String VIA_NUMBER = CallLog.Calls.VIA_NUMBER;
}
