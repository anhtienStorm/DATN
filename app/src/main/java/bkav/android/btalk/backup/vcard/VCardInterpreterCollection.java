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

import java.util.Collection;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The {@link VCardInterpreter} implementation which aggregates more than one.
 *
 * {@link VCardInterpreter} objects and make a user object treat them as one
 * {@link VCardInterpreter} object.
 */
public class VCardInterpreterCollection implements VCardInterpreter {
    
    /** The m interpreter collection. */
    private final Collection<VCardInterpreter> mInterpreterCollection;
    
    /**
     * Instantiates a new v card interpreter collection.
     *
     * @param interpreterCollection the interpreter collection
     */
    public VCardInterpreterCollection(Collection<VCardInterpreter> interpreterCollection) {
        mInterpreterCollection = interpreterCollection;
    }

    /**
     * Gets the collection.
     *
     * @return the collection
     */
    public Collection<VCardInterpreter> getCollection() {
        return mInterpreterCollection;
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#start()
     */
    public void start() {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.start();
        }
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#end()
     */
    public void end() {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.end();
        }
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#startEntry()
     */
    public void startEntry() {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.startEntry();
        }
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#endEntry()
     */
    public void endEntry() {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.endEntry();
        }
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#startProperty()
     */
    public void startProperty() {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.startProperty();
        }
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#endProperty()
     */
    public void endProperty() {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.endProperty();
        }
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#propertyGroup(java.lang.String)
     */
    public void propertyGroup(String group) {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.propertyGroup(group);
        }
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#propertyName(java.lang.String)
     */
    public void propertyName(String name) {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.propertyName(name);
        }
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#propertyParamType(java.lang.String)
     */
    public void propertyParamType(String type) {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.propertyParamType(type);
        }
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#propertyParamValue(java.lang.String)
     */
    public void propertyParamValue(String value) {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.propertyParamValue(value);
        }
    }

    /* (non-Javadoc)
     * @see bms.backup.VCardInterpreter#propertyValues(java.util.List)
     */
    public void propertyValues(List<String> values) {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.propertyValues(values);
        }
    }
}
