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
 * Constants used in both exporter and importer code.
 */
public class VCardConstants {
    
    /** The Constant VERSION_V21. */
    public static final String VERSION_V21 = "2.1";
    
    /** The Constant VERSION_V30. */
    public static final String VERSION_V30 = "3.0";

    // The property names valid both in vCard 2.1 and 3.0.
    /** The Constant PROPERTY_BEGIN. */
    public static final String PROPERTY_BEGIN = "BEGIN";
    
    /** The Constant PROPERTY_VERSION. */
    public static final String PROPERTY_VERSION = "VERSION";
    
    /** The Constant PROPERTY_N. */
    public static final String PROPERTY_N = "N";
    
    /** The Constant PROPERTY_FN. */
    public static final String PROPERTY_FN = "FN";
    
    /** The Constant PROPERTY_ADR. */
    public static final String PROPERTY_ADR = "ADR";
    
    /** The Constant PROPERTY_EMAIL. */
    public static final String PROPERTY_EMAIL = "EMAIL";
    
    /** The Constant PROPERTY_NOTE. */
    public static final String PROPERTY_NOTE = "NOTE";
    
    /** The Constant PROPERTY_ORG. */
    public static final String PROPERTY_ORG = "ORG";
    
    /** The Constant PROPERTY_SOUND. */
    public static final String PROPERTY_SOUND = "SOUND";  // Not fully supported.
    
    /** The Constant PROPERTY_TEL. */
    public static final String PROPERTY_TEL = "TEL";
    
    /** The Constant PROPERTY_TITLE. */
    public static final String PROPERTY_TITLE = "TITLE";
    
    /** The Constant PROPERTY_ROLE. */
    public static final String PROPERTY_ROLE = "ROLE";
    
    /** The Constant PROPERTY_PHOTO. */
    public static final String PROPERTY_PHOTO = "PHOTO";
    
    /** The Constant PROPERTY_LOGO. */
    public static final String PROPERTY_LOGO = "LOGO";
    
    /** The Constant PROPERTY_URL. */
    public static final String PROPERTY_URL = "URL";
    
    /** The Constant PROPERTY_BDAY. */
    public static final String PROPERTY_BDAY = "BDAY";  // Birthday
    
    /** The Constant PROPERTY_END. */
    public static final String PROPERTY_END = "END";

    // Valid property names not supported (not appropriately handled) by our vCard importer now.
    /** The Constant PROPERTY_REV. */
    public static final String PROPERTY_REV = "REV";
    
    /** The Constant PROPERTY_AGENT. */
    public static final String PROPERTY_AGENT = "AGENT";

    // Available in vCard 3.0. Shoud not use when composing vCard 2.1 file.
    /** The Constant PROPERTY_NAME. */
    public static final String PROPERTY_NAME = "NAME";
    
    /** The Constant PROPERTY_NICKNAME. */
    public static final String PROPERTY_NICKNAME = "NICKNAME";
    
    /** The Constant PROPERTY_SORT_STRING. */
    public static final String PROPERTY_SORT_STRING = "SORT-STRING";
    
    // De-fact property values expressing phonetic names.
    /** The Constant PROPERTY_X_PHONETIC_FIRST_NAME. */
    public static final String PROPERTY_X_PHONETIC_FIRST_NAME = "X-PHONETIC-FIRST-NAME";
    
    /** The Constant PROPERTY_X_PHONETIC_MIDDLE_NAME. */
    public static final String PROPERTY_X_PHONETIC_MIDDLE_NAME = "X-PHONETIC-MIDDLE-NAME";
    
    /** The Constant PROPERTY_X_PHONETIC_LAST_NAME. */
    public static final String PROPERTY_X_PHONETIC_LAST_NAME = "X-PHONETIC-LAST-NAME";

    // Properties both ContactsStruct in Eclair and de-fact vCard extensions
    // shown in http://en.wikipedia.org/wiki/VCard support are defined here.
    /** The Constant PROPERTY_X_AIM. */
    public static final String PROPERTY_X_AIM = "X-AIM";
    
    /** The Constant PROPERTY_X_MSN. */
    public static final String PROPERTY_X_MSN = "X-MSN";
    
    /** The Constant PROPERTY_X_YAHOO. */
    public static final String PROPERTY_X_YAHOO = "X-YAHOO";
    
    /** The Constant PROPERTY_X_ICQ. */
    public static final String PROPERTY_X_ICQ = "X-ICQ";
    
    /** The Constant PROPERTY_X_JABBER. */
    public static final String PROPERTY_X_JABBER = "X-JABBER";
    
    /** The Constant PROPERTY_X_GOOGLE_TALK. */
    public static final String PROPERTY_X_GOOGLE_TALK = "X-GOOGLE-TALK";
    
    /** The Constant PROPERTY_X_SKYPE_USERNAME. */
    public static final String PROPERTY_X_SKYPE_USERNAME = "X-SKYPE-USERNAME";
    // Properties only ContactsStruct has. We alse use this.
    /** The Constant PROPERTY_X_QQ. */
    public static final String PROPERTY_X_QQ = "X-QQ";
    
    /** The Constant PROPERTY_X_NETMEETING. */
    public static final String PROPERTY_X_NETMEETING = "X-NETMEETING";

    // Phone number for Skype, available as usual phone.
    /** The Constant PROPERTY_X_SKYPE_PSTNNUMBER. */
    public static final String PROPERTY_X_SKYPE_PSTNNUMBER = "X-SKYPE-PSTNNUMBER";

    // Property for Android-specific fields.
    /** The Constant PROPERTY_X_ANDROID_CUSTOM. */
    public static final String PROPERTY_X_ANDROID_CUSTOM = "X-ANDROID-CUSTOM";

    // Properties for DoCoMo vCard.
    /** The Constant PROPERTY_X_CLASS. */
    public static final String PROPERTY_X_CLASS = "X-CLASS";
    
    /** The Constant PROPERTY_X_REDUCTION. */
    public static final String PROPERTY_X_REDUCTION = "X-REDUCTION";
    
    /** The Constant PROPERTY_X_NO. */
    public static final String PROPERTY_X_NO = "X-NO";
    
    /** The Constant PROPERTY_X_DCM_HMN_MODE. */
    public static final String PROPERTY_X_DCM_HMN_MODE = "X-DCM-HMN-MODE";

    /** The Constant PARAM_TYPE. */
    public static final String PARAM_TYPE = "TYPE";

    /** The Constant PARAM_TYPE_HOME. */
    public static final String PARAM_TYPE_HOME = "HOME";
    
    /** The Constant PARAM_TYPE_WORK. */
    public static final String PARAM_TYPE_WORK = "WORK";
    
    /** The Constant PARAM_TYPE_FAX. */
    public static final String PARAM_TYPE_FAX = "FAX";
    
    /** The Constant PARAM_TYPE_CELL. */
    public static final String PARAM_TYPE_CELL = "CELL";
    
    /** The Constant PARAM_TYPE_VOICE. */
    public static final String PARAM_TYPE_VOICE = "VOICE";
    
    /** The Constant PARAM_TYPE_INTERNET. */
    public static final String PARAM_TYPE_INTERNET = "INTERNET";

    // Abbreviation of "prefered" according to vCard 2.1 specification.
    // We interpret this value as "primary" property during import/export.
    //
    // Note: Both vCard specs does not mention anything about the requirement for this parameter,
    //       but there may be some vCard importer which will get confused with more than
    //       one "PREF"s in one property name, while Android accepts them.
    /** The Constant PARAM_TYPE_PREF. */
    public static final String PARAM_TYPE_PREF = "PREF";

    // Phone type parameters valid in vCard and known to ContactsContract, but not so common.
    /** The Constant PARAM_TYPE_CAR. */
    public static final String PARAM_TYPE_CAR = "CAR";
    
    /** The Constant PARAM_TYPE_ISDN. */
    public static final String PARAM_TYPE_ISDN = "ISDN";
    
    /** The Constant PARAM_TYPE_PAGER. */
    public static final String PARAM_TYPE_PAGER = "PAGER";
    
    /** The Constant PARAM_TYPE_TLX. */
    public static final String PARAM_TYPE_TLX = "TLX";  // Telex

    // Phone types existing in vCard 2.1 but not known to ContactsContract.
    /** The Constant PARAM_TYPE_MODEM. */
    public static final String PARAM_TYPE_MODEM = "MODEM";
    
    /** The Constant PARAM_TYPE_MSG. */
    public static final String PARAM_TYPE_MSG = "MSG";
    
    /** The Constant PARAM_TYPE_BBS. */
    public static final String PARAM_TYPE_BBS = "BBS";
    
    /** The Constant PARAM_TYPE_VIDEO. */
    public static final String PARAM_TYPE_VIDEO = "VIDEO";

    // TYPE parameters for Phones, which are not formally valid in vCard (at least 2.1).
    // These types are basically encoded to "X-" parameters when composing vCard.
    // Parser passes these when "X-" is added to the parameter or not.
    /** The Constant PARAM_PHONE_EXTRA_TYPE_CALLBACK. */
    public static final String PARAM_PHONE_EXTRA_TYPE_CALLBACK = "CALLBACK";
    
    /** The Constant PARAM_PHONE_EXTRA_TYPE_RADIO. */
    public static final String PARAM_PHONE_EXTRA_TYPE_RADIO = "RADIO";
    
    /** The Constant PARAM_PHONE_EXTRA_TYPE_TTY_TDD. */
    public static final String PARAM_PHONE_EXTRA_TYPE_TTY_TDD = "TTY-TDD";
    
    /** The Constant PARAM_PHONE_EXTRA_TYPE_ASSISTANT. */
    public static final String PARAM_PHONE_EXTRA_TYPE_ASSISTANT = "ASSISTANT";
    // vCard composer translates this type to "WORK" + "PREF". Just for parsing.
    /** The Constant PARAM_PHONE_EXTRA_TYPE_COMPANY_MAIN. */
    public static final String PARAM_PHONE_EXTRA_TYPE_COMPANY_MAIN = "COMPANY-MAIN";
    // vCard composer translates this type to "VOICE" Just for parsing.
    /** The Constant PARAM_PHONE_EXTRA_TYPE_OTHER. */
    public static final String PARAM_PHONE_EXTRA_TYPE_OTHER = "OTHER";

    // TYPE parameters for postal addresses.
    /** The Constant PARAM_ADR_TYPE_PARCEL. */
    public static final String PARAM_ADR_TYPE_PARCEL = "PARCEL";
    
    /** The Constant PARAM_ADR_TYPE_DOM. */
    public static final String PARAM_ADR_TYPE_DOM = "DOM";
    
    /** The Constant PARAM_ADR_TYPE_INTL. */
    public static final String PARAM_ADR_TYPE_INTL = "INTL";

    // TYPE parameters not officially valid but used in some vCard exporter.
    // Do not use in composer side.
    /** The Constant PARAM_EXTRA_TYPE_COMPANY. */
    public static final String PARAM_EXTRA_TYPE_COMPANY = "COMPANY";

    // DoCoMo specific type parameter. Used with "SOUND" property, which is alternate of SORT-STRING in
    // vCard 3.0.
    /** The Constant PARAM_TYPE_X_IRMC_N. */
    public static final String PARAM_TYPE_X_IRMC_N = "X-IRMC-N";

    /**
     * The Interface ImportOnly.
     */
    public interface ImportOnly {
        
        /** The Constant PROPERTY_X_NICKNAME. */
        public static final String PROPERTY_X_NICKNAME = "X-NICKNAME";
        // Some device emits this "X-" parameter for expressing Google Talk,
        // which is specifically invalid but should be always properly accepted, and emitted
        // in some special case (for that device/application).
        /** The Constant PROPERTY_X_GOOGLE_TALK_WITH_SPACE. */
        public static final String PROPERTY_X_GOOGLE_TALK_WITH_SPACE = "X-GOOGLE TALK";
    }

    /* package */ /** The Constant MAX_DATA_COLUMN. */
    static final int MAX_DATA_COLUMN = 15;

    /* package */ /** The Constant MAX_CHARACTER_NUMS_QP. */
    static final int MAX_CHARACTER_NUMS_QP = 76;
    
    /** The Constant MAX_CHARACTER_NUMS_BASE64_V30. */
    static final int MAX_CHARACTER_NUMS_BASE64_V30 = 75;

    /**
     * Instantiates a new v card constants.
     */
    private VCardConstants() {
    }
}