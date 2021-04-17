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

// TODO: Auto-generated Javadoc
/**
 * VCardException thrown when VCard is nested without VCardParser's being notified.
 */
@SuppressWarnings("serial")
public class VCardNestedException extends VCardNotSupportedException {
    
    /**
     * Instantiates a new v card nested exception.
     */
    public VCardNestedException() {
        super();
    }
    
    /**
     * Instantiates a new v card nested exception.
     *
     * @param message the message
     */
    public VCardNestedException(String message) {
        super(message);
    }
}
