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

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The class which just counts the number of vCard entries in the specified input.
 */
public class VCardEntryCounter implements VCardInterpreter {
    
    /** The m count. */
    private int mCount;

    /**
     * Gets the count.
     *
     * @return the count
     */
    public int getCount() {
        return mCount;
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#start()
     */
    public void start() {
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#end()
     */
    public void end() {
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#startEntry()
     */
    public void startEntry() {
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#endEntry()
     */
    public void endEntry() {
        mCount++;
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#startProperty()
     */
    public void startProperty() {
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#endProperty()
     */
    public void endProperty() {
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#propertyGroup(java.lang.String)
     */
    public void propertyGroup(String group) {
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#propertyName(java.lang.String)
     */
    public void propertyName(String name) {
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#propertyParamType(java.lang.String)
     */
    public void propertyParamType(String type) {
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#propertyParamValue(java.lang.String)
     */
    public void propertyParamValue(String value) {
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#propertyValues(java.util.List)
     */
    public void propertyValues(List<String> values) {
    }    
}
