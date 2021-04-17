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

import android.content.ContentProviderOperation;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * Utilities for VCard handling codes.
 */
public class VCardUtils {
    // Note that not all types are included in this map/set, since, for example, TYPE_HOME_FAX is
    // converted to two parameter Strings. These only contain some minor fields valid in both
    // vCard and current (as of 2009-08-07) Contacts structure. 
    /** The Constant sKnownPhoneTypesMap_ItoS. */
    private static final SparseArray<String> sKnownPhoneTypesMap_ItoS;
    
    /** The Constant sPhoneTypesUnknownToContactsSet. */
    private static final Set<String> sPhoneTypesUnknownToContactsSet;
    
    /** The Constant sKnownPhoneTypeMap_StoI. */
    private static final Map<String, Integer> sKnownPhoneTypeMap_StoI;
    
    /** The Constant sKnownImPropNameMap_ItoS. */
    private static final SparseArray<String> sKnownImPropNameMap_ItoS;
    static {
        sKnownPhoneTypesMap_ItoS = new SparseArray<String>();
        sKnownPhoneTypeMap_StoI = new HashMap<String, Integer>();

        sKnownPhoneTypesMap_ItoS.put(Phone.TYPE_CAR, VCardConstants.PARAM_TYPE_CAR);
        sKnownPhoneTypeMap_StoI.put(VCardConstants.PARAM_TYPE_CAR, Phone.TYPE_CAR);
        sKnownPhoneTypesMap_ItoS.put(Phone.TYPE_PAGER, VCardConstants.PARAM_TYPE_PAGER);
        sKnownPhoneTypeMap_StoI.put(VCardConstants.PARAM_TYPE_PAGER, Phone.TYPE_PAGER);
        sKnownPhoneTypesMap_ItoS.put(Phone.TYPE_ISDN, VCardConstants.PARAM_TYPE_ISDN);
        sKnownPhoneTypeMap_StoI.put(VCardConstants.PARAM_TYPE_ISDN, Phone.TYPE_ISDN);
        
        sKnownPhoneTypeMap_StoI.put(VCardConstants.PARAM_TYPE_HOME, Phone.TYPE_HOME);
        sKnownPhoneTypeMap_StoI.put(VCardConstants.PARAM_TYPE_WORK, Phone.TYPE_WORK);
        sKnownPhoneTypeMap_StoI.put(VCardConstants.PARAM_TYPE_CELL, Phone.TYPE_MOBILE);
                
        sKnownPhoneTypeMap_StoI.put(VCardConstants.PARAM_PHONE_EXTRA_TYPE_OTHER, Phone.TYPE_OTHER);
        sKnownPhoneTypeMap_StoI.put(VCardConstants.PARAM_PHONE_EXTRA_TYPE_CALLBACK,
                Phone.TYPE_CALLBACK);
        sKnownPhoneTypeMap_StoI.put(
                VCardConstants.PARAM_PHONE_EXTRA_TYPE_COMPANY_MAIN, Phone.TYPE_COMPANY_MAIN);
        sKnownPhoneTypeMap_StoI.put(VCardConstants.PARAM_PHONE_EXTRA_TYPE_RADIO, Phone.TYPE_RADIO);
        sKnownPhoneTypeMap_StoI.put(VCardConstants.PARAM_PHONE_EXTRA_TYPE_TTY_TDD,
                Phone.TYPE_TTY_TDD);
        sKnownPhoneTypeMap_StoI.put(VCardConstants.PARAM_PHONE_EXTRA_TYPE_ASSISTANT,
                Phone.TYPE_ASSISTANT);

        sPhoneTypesUnknownToContactsSet = new HashSet<String>();
        sPhoneTypesUnknownToContactsSet.add(VCardConstants.PARAM_TYPE_MODEM);
        sPhoneTypesUnknownToContactsSet.add(VCardConstants.PARAM_TYPE_MSG);
        sPhoneTypesUnknownToContactsSet.add(VCardConstants.PARAM_TYPE_BBS);
        sPhoneTypesUnknownToContactsSet.add(VCardConstants.PARAM_TYPE_VIDEO);

        sKnownImPropNameMap_ItoS = new SparseArray<String>();
        sKnownImPropNameMap_ItoS.put(Im.PROTOCOL_AIM, VCardConstants.PROPERTY_X_AIM);
        sKnownImPropNameMap_ItoS.put(Im.PROTOCOL_MSN, VCardConstants.PROPERTY_X_MSN);
        sKnownImPropNameMap_ItoS.put(Im.PROTOCOL_YAHOO, VCardConstants.PROPERTY_X_YAHOO);
        sKnownImPropNameMap_ItoS.put(Im.PROTOCOL_SKYPE, VCardConstants.PROPERTY_X_SKYPE_USERNAME);
        sKnownImPropNameMap_ItoS.put(Im.PROTOCOL_GOOGLE_TALK,
                VCardConstants.PROPERTY_X_GOOGLE_TALK);
        sKnownImPropNameMap_ItoS.put(Im.PROTOCOL_ICQ, VCardConstants.PROPERTY_X_ICQ);
        sKnownImPropNameMap_ItoS.put(Im.PROTOCOL_JABBER, VCardConstants.PROPERTY_X_JABBER);
        sKnownImPropNameMap_ItoS.put(Im.PROTOCOL_QQ, VCardConstants.PROPERTY_X_QQ);
        sKnownImPropNameMap_ItoS.put(Im.PROTOCOL_NETMEETING, VCardConstants.PROPERTY_X_NETMEETING);

        new HashSet<String>(Arrays.asList(
                "MOBILE", "\u643A\u5E2F\u96FB\u8A71", "\u643A\u5E2F", "\u30B1\u30A4\u30BF\u30A4",
                "\uFF79\uFF72\uFF80\uFF72"));
    }

    /**
     * Gets the phone type string.
     *
     * @param type the type
     * @return the phone type string
     */
    public static String getPhoneTypeString(Integer type) {
        return sKnownPhoneTypesMap_ItoS.get(type);
    }

    /**
     * Returns Interger when the given types can be parsed as known type. Returns String object
     * when not, which should be set to label.
     *
     * @param types the types
     * @param number the number
     * @return the phone type from strings
     */
    public static Object getPhoneTypeFromStrings(Collection<String> types,
            String number) {
        if (number == null) {
            number = "";
        }
        int type = -1;
        String label = null;
        boolean isFax = false;
        boolean hasPref = false;
        
        if (types != null) {
            for (String typeString : types) {
                if (typeString == null) {
                    continue;
                }
                typeString = typeString.toUpperCase(Locale.getDefault());
                if (typeString.equals(VCardConstants.PARAM_TYPE_PREF)) {
                    hasPref = true;
                } else if (typeString.equals(VCardConstants.PARAM_TYPE_FAX)) {
                    isFax = true;
                } else {
                    if (typeString.startsWith("X-") && type < 0) {
                        typeString = typeString.substring(2);
                    }
                    if (typeString.length() == 0) {
                        continue;
                    }
                    final Integer tmp = sKnownPhoneTypeMap_StoI.get(typeString);
                    if (tmp != null) {
                        final int typeCandidate = tmp;
                        // TYPE_PAGER is prefered when the number contains @ surronded by
                        // a pager number and a domain name.
                        // e.g.
                        // o 1111@domain.com
                        // x @domain.com
                        // x 1111@
                        final int indexOfAt = number.indexOf("@");
                        if ((typeCandidate == Phone.TYPE_PAGER
                                && 0 < indexOfAt && indexOfAt < number.length() - 1)
                                || type < 0
                                || type == Phone.TYPE_CUSTOM) {
                            type = tmp;
                        }
                    } else if (type < 0) {
                        type = Phone.TYPE_CUSTOM;
                        label = typeString;
                    }
                }
            }
        }
        if (type < 0) {
            if (hasPref) {
                type = Phone.TYPE_MAIN;
            } else {
                // default to TYPE_HOME
                type = Phone.TYPE_HOME;
            }
        }
        if (isFax) {
            if (type == Phone.TYPE_HOME) {
                type = Phone.TYPE_FAX_HOME;
            } else if (type == Phone.TYPE_WORK) {
                type = Phone.TYPE_FAX_WORK;
            } else if (type == Phone.TYPE_OTHER) {
                type = Phone.TYPE_OTHER_FAX;
            }
        }
        if (type == Phone.TYPE_CUSTOM) {
            return label;
        } else {
            return type;
        }
    }

    /**
     * Checks if is valid in v21 but unknown to contacts phote type.
     *
     * @param label the label
     * @return true, if is valid in v21 but unknown to contacts phote type
     */
    public static boolean isValidInV21ButUnknownToContactsPhoteType(final String label) {
        return sPhoneTypesUnknownToContactsSet.contains(label);
    }

    /**
     * Gets the property name for im.
     *
     * @param protocol the protocol
     * @return the property name for im
     */
    public static String getPropertyNameForIm(final int protocol) {
        return sKnownImPropNameMap_ItoS.get(protocol);
    }

    /**
     * Sort name elements.
     *
     * @param vcardType the vcard type
     * @param familyName the family name
     * @param middleName the middle name
     * @param givenName the given name
     * @return the string[]
     */
    public static String[] sortNameElements(final int vcardType,
            final String familyName, final String middleName, final String givenName) {
        final String[] list = new String[3];
        final int nameOrderType = VCardConfig.getNameOrderType(vcardType);
        switch (nameOrderType) {
            case VCardConfig.NAME_ORDER_JAPANESE: {
                if (containsOnlyPrintableAscii(familyName) &&
                        containsOnlyPrintableAscii(givenName)) {
                    list[0] = givenName;
                    list[1] = middleName;
                    list[2] = familyName;
                } else {
                    list[0] = familyName;
                    list[1] = middleName;
                    list[2] = givenName;
                }
                break;
            }
            case VCardConfig.NAME_ORDER_EUROPE: {
                list[0] = middleName;
                list[1] = givenName;
                list[2] = familyName;
                break;
            }
            default: {
                list[0] = givenName;
                list[1] = middleName;
                list[2] = familyName;
                break;
            }
        }
        return list;
    }

    /**
     * Gets the phone number format.
     *
     * @param vcardType the vcard type
     * @return the phone number format
     */
    public static int getPhoneNumberFormat(final int vcardType) {
        if (VCardConfig.isJapaneseDevice(vcardType)) {
            return PhoneNumberUtils.FORMAT_JAPAN;
        } else {
            return PhoneNumberUtils.FORMAT_NANP;
        }
    }

    /**
     * Inserts postal data into the builder object.
     * 
     * Note that the data structure of ContactsContract is different from that defined in vCard.
     * So some conversion may be performed in this method.
     *
     * @param vcardType the vcard type
     * @param builder the builder
     * @param postalData the postal data
     */
    public static void insertStructuredPostalDataUsingContactsStruct(int vcardType,
            final ContentProviderOperation.Builder builder,
            final VCardEntry.PostalData postalData) {
        builder.withValueBackReference(StructuredPostal.RAW_CONTACT_ID, 0);
        builder.withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE);

        builder.withValue(StructuredPostal.TYPE, postalData.type);
        if (postalData.type == StructuredPostal.TYPE_CUSTOM) {
            builder.withValue(StructuredPostal.LABEL, postalData.label);
        }

        final String streetString;
        if (TextUtils.isEmpty(postalData.street)) {
            if (TextUtils.isEmpty(postalData.extendedAddress)) {
                streetString = null;
            } else {
                streetString = postalData.extendedAddress;
            }
        } else {
            if (TextUtils.isEmpty(postalData.extendedAddress)) {
                streetString = postalData.street;
            } else {
                streetString = postalData.street + " " + postalData.extendedAddress;
            }
        }
        builder.withValue(StructuredPostal.POBOX, postalData.pobox);
        builder.withValue(StructuredPostal.STREET, streetString);
        builder.withValue(StructuredPostal.CITY, postalData.localty);
        builder.withValue(StructuredPostal.REGION, postalData.region);
        builder.withValue(StructuredPostal.POSTCODE, postalData.postalCode);
        builder.withValue(StructuredPostal.COUNTRY, postalData.country);

        builder.withValue(StructuredPostal.FORMATTED_ADDRESS,
                postalData.getFormattedAddress(vcardType));
        if (postalData.isPrimary) {
            builder.withValue(Data.IS_PRIMARY, 1);
        }
    }

    /**
     * Construct name from elements.
     *
     * @param vcardType the vcard type
     * @param familyName the family name
     * @param middleName the middle name
     * @param givenName the given name
     * @return the string
     */
    public static String constructNameFromElements(final int vcardType,
            final String familyName, final String middleName, final String givenName) {
        return constructNameFromElements(vcardType, familyName, middleName, givenName,
                null, null);
    }

    /**
     * Construct name from elements.
     *
     * @param vcardType the vcard type
     * @param familyName the family name
     * @param middleName the middle name
     * @param givenName the given name
     * @param prefix the prefix
     * @param suffix the suffix
     * @return the string
     */
    public static String constructNameFromElements(final int vcardType,
            final String familyName, final String middleName, final String givenName,
            final String prefix, final String suffix) {
        final StringBuilder builder = new StringBuilder();
        final String[] nameList = sortNameElements(vcardType, familyName, middleName, givenName);
        boolean first = true;
        if (!TextUtils.isEmpty(prefix)) {
            first = false;
            builder.append(prefix);
        }
        for (final String namePart : nameList) {
            if (!TextUtils.isEmpty(namePart)) {
                if (first) {
                    first = false;
                } else {
                    builder.append(' ');
                }
                builder.append(namePart);
            }
        }
        if (!TextUtils.isEmpty(suffix)) {
            if (!first) {
                builder.append(' ');
            }
            builder.append(suffix);
        }
        return builder.toString();
    }

    /**
     * Construct list from value.
     *
     * @param value the value
     * @param isV30 the is v30
     * @return the list
     */
    public static List<String> constructListFromValue(final String value,
            final boolean isV30) {
        final List<String> list = new ArrayList<String>();
        StringBuilder builder = new StringBuilder();
        int length = value.length();
        for (int i = 0; i < length; i++) {
            char ch = value.charAt(i);
            if (ch == '\\' && i < length - 1) {
                char nextCh = value.charAt(i + 1);
                final String unescapedString =
                    (isV30 ? unescapeCharacterv30(nextCh) :
                        unescapeCharacterv21(nextCh));
                if (unescapedString != null) {
                    builder.append(unescapedString);
                    i++;
                } else {
                    builder.append(ch);
                }
            } else if (ch == ';') {
                list.add(builder.toString());
                builder = new StringBuilder();
            } else {
                builder.append(ch);
            }
        }
        list.add(builder.toString());
        return list;
    }

    /**
     * Contains only printable ascii.
     *
     * @param values the values
     * @return true, if successful
     */
    public static boolean containsOnlyPrintableAscii(final String...values) {
        if (values == null) {
            return true;
        }
        return containsOnlyPrintableAscii(Arrays.asList(values));
    }

    /**
     * Contains only printable ascii.
     *
     * @param values the values
     * @return true, if successful
     */
    public static boolean containsOnlyPrintableAscii(final Collection<String> values) {
        if (values == null) {
            return true;
        }
        for (final String value : values) {
            if (TextUtils.isEmpty(value)) {
                continue;
            }
//            if (!TextUtils.isPrintableAsciiOnly(value)) {
//                return false;
//            }
        }
        return true;
    }

    /**
     * This is useful when checking the string should be encoded into quoted-printable
     * or not, which is required by vCard 2.1.
     * See the definition of "7bit" in vCard 2.1 spec for more information.
     *
     * @param values the values
     * @return true, if successful
     */
    public static boolean containsOnlyNonCrLfPrintableAscii(final String...values) {
        if (values == null) {
            return true;
        }
        return containsOnlyNonCrLfPrintableAscii(Arrays.asList(values));
    }

    /**
     * Contains only non cr lf printable ascii.
     *
     * @param values the values
     * @return true, if successful
     */
    public static boolean containsOnlyNonCrLfPrintableAscii(final Collection<String> values) {
        if (values == null) {
            return true;
        }
        final int asciiFirst = 0x20;
        final int asciiLast = 0x7E;  // included
        for (final String value : values) {
            if (TextUtils.isEmpty(value)) {
                continue;
            }
            final int length = value.length();
            for (int i = 0; i < length; i = value.offsetByCodePoints(i, 1)) {
                final int c = value.codePointAt(i);
                if (!(asciiFirst <= c && c <= asciiLast)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** The Constant sUnAcceptableAsciiInV21WordSet. */
    private static final Set<Character> sUnAcceptableAsciiInV21WordSet =
        new HashSet<Character>(Arrays.asList('[', ']', '=', ':', '.', ',', ' '));

    /**
     * This is useful since vCard 3.0 often requires the ("X-") properties and groups
     * should contain only alphabets, digits, and hyphen.
     * 
     * Note: It is already known some devices (wrongly) outputs properties with characters
     * which should not be in the field. One example is "X-GOOGLE TALK". We accept
     * such kind of input but must never output it unless the target is very specific
     * to the device which is able to parse the malformed input.
     *
     * @param values the values
     * @return true, if successful
     */
    public static boolean containsOnlyAlphaDigitHyphen(final String...values) {
        if (values == null) {
            return true;
        }
        return containsOnlyAlphaDigitHyphen(Arrays.asList(values));
    }

    /**
     * Contains only alpha digit hyphen.
     *
     * @param values the values
     * @return true, if successful
     */
    public static boolean containsOnlyAlphaDigitHyphen(final Collection<String> values) {
        if (values == null) {
            return true;
        }
        final int upperAlphabetFirst = 0x41;  // A
        final int upperAlphabetAfterLast = 0x5b;  // [
        final int lowerAlphabetFirst = 0x61;  // a
        final int lowerAlphabetAfterLast = 0x7b;  // {
        final int digitFirst = 0x30;  // 0
        final int digitAfterLast = 0x3A;  // :
        final int hyphen = '-';
        for (final String str : values) {
            if (TextUtils.isEmpty(str)) {
                continue;
            }
            final int length = str.length();
            for (int i = 0; i < length; i = str.offsetByCodePoints(i, 1)) {
                int codepoint = str.codePointAt(i);
                if (!((lowerAlphabetFirst <= codepoint && codepoint < lowerAlphabetAfterLast) ||
                    (upperAlphabetFirst <= codepoint && codepoint < upperAlphabetAfterLast) ||
                    (digitFirst <= codepoint && codepoint < digitAfterLast) ||
                    (codepoint == hyphen))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * <P>
     * Returns true when the given String is categorized as "word" specified in vCard spec 2.1.
     * </P>
     * <P>
     * vCard 2.1 specifies:<BR />
     * word = &lt;any printable 7bit us-ascii except []=:., &gt;
     * </P>
     *
     * @param value the value
     * @return true, if is v21 word
     */
    public static boolean isV21Word(final String value) {
        if (TextUtils.isEmpty(value)) {
            return true;
        }
        final int asciiFirst = 0x20;
        final int asciiLast = 0x7E;  // included
        final int length = value.length();
        for (int i = 0; i < length; i = value.offsetByCodePoints(i, 1)) {
            final int c = value.codePointAt(i);
            if (!(asciiFirst <= c && c <= asciiLast) ||
                    sUnAcceptableAsciiInV21WordSet.contains((char)c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * To half width string.
     *
     * @param orgString the org string
     * @return the string
     */
    public static String toHalfWidthString(final String orgString) {
        if (TextUtils.isEmpty(orgString)) {
            return null;
        }
        final StringBuilder builder = new StringBuilder();
        final int length = orgString.length();
        for (int i = 0; i < length; i = orgString.offsetByCodePoints(i, 1)) {
            // All Japanese character is able to be expressed by char.
            // Do not need to use String#codepPointAt().
            final char ch = orgString.charAt(i);
            final String halfWidthText = JapaneseUtils.tryGetHalfWidthText(ch);
            if (halfWidthText != null) {
                builder.append(halfWidthText);
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    /**
     * Guesses the format of input image. Currently just the first few bytes are used.
     * The type "GIF", "PNG", or "JPEG" is returned when possible. Returns null when
     * the guess failed.
     * @param input Image as byte array.
     * @return The image type or null when the type cannot be determined.
     */
    public static String guessImageType(final byte[] input) {
        if (input == null) {
            return null;
        }
        if (input.length >= 3 && input[0] == 'G' && input[1] == 'I' && input[2] == 'F') {
            return "GIF";
        } else if (input.length >= 4 && input[0] == (byte) 0x89
                && input[1] == 'P' && input[2] == 'N' && input[3] == 'G') {
            // Note: vCard 2.1 officially does not support PNG, but we may have it and
            //       using X- word like "X-PNG" may not let importers know it is PNG.
            //       So we use the String "PNG" as is...
            return "PNG";
        } else if (input.length >= 2 && input[0] == (byte) 0xff
                && input[1] == (byte) 0xd8) {
            return "JPEG";
        } else {
            return null;
        }
    }

    /**
     * Are all empty.
     *
     * @param values the values
     * @return True when all the given values are null or empty Strings.
     */
    public static boolean areAllEmpty(final String...values) {
        if (values == null) {
            return true;
        }

        for (final String value : values) {
            if (!TextUtils.isEmpty(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Instantiates a new v card utils.
     */
    private VCardUtils() {
    }

	/**
	 * Checks if is mobile phone label.
	 *
	 * @param label the label
	 * @return true, if is mobile phone label
	 */
	public static boolean isMobilePhoneLabel(String label) {
		// TODO Auto-generated method stub
		return false;
	}
	
    /**
     * Unescape characterv21.
     *
     * @param ch the ch
     * @return the string
     */
    public static String unescapeCharacterv21(final char ch) {
        // Original vCard 2.1 specification does not allow transformation
        // "\:" -> ":", "\," -> ",", and "\\" -> "\", but previous implementation of
        // this class allowed them, so keep it as is.
        if (ch == '\\' || ch == ';' || ch == ':' || ch == ',') {
            return String.valueOf(ch);
        } else {
            return null;
        }
    }

    /**
     * Unescape characterv30.
     *
     * @param ch the ch
     * @return the string
     */
    public static String unescapeCharacterv30(char ch) {
        if (ch == 'n' || ch == 'N') {
            return "\n";
        } else {
            return String.valueOf(ch);
        }        
    }
}
