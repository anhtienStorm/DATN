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

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import bkav.android.btalk.backup.BackupRestoreUtils;

// TODO: Auto-generated Javadoc
/**
 * This class bridges between data structure of Contact app and VCard data.
 */
public class VCardEntry {
	
	/** The Constant LOG_TAG. */
	private static final String LOG_TAG = "VCardEntry";

	/** The Constant DEFAULT_ORGANIZATION_TYPE. */
	private final static int DEFAULT_ORGANIZATION_TYPE = Organization.TYPE_WORK;

	/** The Constant ACCOUNT_TYPE_GOOGLE. */
	private static final String ACCOUNT_TYPE_GOOGLE = "com.google";
	
	/** The Constant GOOGLE_MY_CONTACTS_GROUP. */
	private static final String GOOGLE_MY_CONTACTS_GROUP = "System Group: My Contacts";
	
	/** The contact exist. */
	private static boolean contactExist = false;
	
	/** The Constant sImMap. */
	private static final Map<String, Integer> sImMap = new HashMap<String, Integer>();

	static {
		sImMap.put(VCardConstants.PROPERTY_X_AIM, Im.PROTOCOL_AIM);
		sImMap.put(VCardConstants.PROPERTY_X_MSN, Im.PROTOCOL_MSN);
		sImMap.put(VCardConstants.PROPERTY_X_YAHOO, Im.PROTOCOL_YAHOO);
		sImMap.put(VCardConstants.PROPERTY_X_ICQ, Im.PROTOCOL_ICQ);
		sImMap.put(VCardConstants.PROPERTY_X_JABBER, Im.PROTOCOL_JABBER);
		sImMap.put(VCardConstants.PROPERTY_X_SKYPE_USERNAME, Im.PROTOCOL_SKYPE);
		sImMap.put(VCardConstants.PROPERTY_X_GOOGLE_TALK, Im.PROTOCOL_GOOGLE_TALK);
		sImMap.put(VCardConstants.ImportOnly.PROPERTY_X_GOOGLE_TALK_WITH_SPACE,
				Im.PROTOCOL_GOOGLE_TALK);
	}

	/**
	 * The Class PhoneData.
	 */
	static public class PhoneData {
		
		/** The type. */
		public final int type;
		
		/** The data. */
		public final String data;
		
		/** The label. */
		public final String label;
		// isPrimary is changable only when there's no appropriate one existing in
		// the original VCard.
		/** The is primary. */
		public boolean isPrimary;
		
		/**
		 * Instantiates a new phone data.
		 *
		 * @param type the type
		 * @param data the data
		 * @param label the label
		 * @param isPrimary the is primary
		 */
		public PhoneData(int type, String data, String label, boolean isPrimary) {
			this.type = type;
			this.data = data;
			this.label = label;
			this.isPrimary = isPrimary;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof PhoneData)) {
				return false;
			}
			PhoneData phoneData = (PhoneData)obj;
			return (type == phoneData.type && data.equals(phoneData.data) &&
					label.equals(phoneData.label) && isPrimary == phoneData.isPrimary);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format(Locale.getDefault(),"type: %d, data: %s, label: %s, isPrimary: %s",
					type, data, label, isPrimary);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			assert false : "hashCode not designed";
		return 42; // any arbitrary constant will do
		}

	}

	/**
	 * The Class EmailData.
	 */
	static public class EmailData {
		
		/** The type. */
		public final int type;
		
		/** The data. */
		public final String data;
		// Used only when TYPE is TYPE_CUSTOM.
		/** The label. */
		public final String label;
		// isPrimary is changable only when there's no appropriate one existing in
		// the original VCard.
		/** The m_is primary. */
		public boolean m_isPrimary;
		
		/**
		 * Instantiates a new email data.
		 *
		 * @param type the type
		 * @param data the data
		 * @param label the label
		 * @param isPrimary the is primary
		 */
		public EmailData(int type, String data, String label, boolean isPrimary) {
			this.type = type;
			this.data = data;
			this.label = label;
			this.m_isPrimary = isPrimary;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof EmailData)) {
				return false;
			}
			EmailData emailData = (EmailData)obj;
			return (type == emailData.type && data.equals(emailData.data) &&
					label.equals(emailData.label) && m_isPrimary == emailData.m_isPrimary);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format(Locale.getDefault(),"type: %d, data: %s, label: %s, isPrimary: %s",
					type, data, label, m_isPrimary);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			assert false : "hashCode not designed";
		return 43; // any arbitrary constant will do
		}
	}

	/**
	 * The Class PostalData.
	 */
	static public class PostalData {
		// Determined by vCard spec.
		// PO Box, Extended Addr, Street, Locality, Region, Postal Code, Country Name
		/** The Constant ADDR_MAX_DATA_SIZE. */
		public static final int ADDR_MAX_DATA_SIZE = 7;
		
		/** The data array. */
		private final String[] dataArray;
		
		/** The pobox. */
		public final String pobox;
		
		/** The extended address. */
		public final String extendedAddress;
		
		/** The street. */
		public final String street;
		
		/** The localty. */
		public final String localty;
		
		/** The region. */
		public final String region;
		
		/** The postal code. */
		public final String postalCode;
		
		/** The country. */
		public final String country;
		
		/** The type. */
		public final int type;
		
		/** The label. */
		public final String label;
		
		/** The is primary. */
		public boolean isPrimary;

		/**
		 * Instantiates a new postal data.
		 *
		 * @param type the type
		 * @param propValueList the prop value list
		 * @param label the label
		 * @param isPrimary the is primary
		 */
		public PostalData(final int type, final List<String> propValueList,
				final String label, boolean isPrimary) {
			this.type = type;
			dataArray = new String[ADDR_MAX_DATA_SIZE];

			int size = propValueList.size();
			if (size > ADDR_MAX_DATA_SIZE) {
				size = ADDR_MAX_DATA_SIZE;
			}

			// adr-value = 0*6(text-value ";") text-value
					//           ; PO Box, Extended Address, Street, Locality, Region, Postal
			//           ; Code, Country Name
			//
			// Use Iterator assuming List may be LinkedList, though actually it is
			// always ArrayList in the current implementation.
			int i = 0;
			for (String addressElement : propValueList) {
				dataArray[i] = addressElement;
				if (++i >= size) {
					break;
				}
			}
			while (i < ADDR_MAX_DATA_SIZE) {
				dataArray[i++] = null;
			}

			this.pobox = dataArray[0];
			this.extendedAddress = dataArray[1];
			this.street = dataArray[2];
			this.localty = dataArray[3];
			this.region = dataArray[4];
			this.postalCode = dataArray[5];
			this.country = dataArray[6];
			this.label = label;
			this.isPrimary = isPrimary;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof PostalData)) {
				return false;
			}
			final PostalData postalData = (PostalData)obj;
			return (Arrays.equals(dataArray, postalData.dataArray) &&
					(type == postalData.type &&
					(type == StructuredPostal.TYPE_CUSTOM ?
							(label == postalData.label) : true)) &&
							(isPrimary == postalData.isPrimary));
		}

		/**
		 * Gets the formatted address.
		 *
		 * @param vcardType the vcard type
		 * @return the formatted address
		 */
		public String getFormattedAddress(final int vcardType) {
			StringBuilder builder = new StringBuilder();
			boolean empty = true;
			if (VCardConfig.isJapaneseDevice(vcardType)) {
				// In Japan, the order is reversed.
				for (int i = ADDR_MAX_DATA_SIZE - 1; i >= 0; i--) {
					String addressPart = dataArray[i];
					if (!TextUtils.isEmpty(addressPart)) {
						if (!empty) {
							builder.append(' ');
						} else {
							empty = false;
						}
						builder.append(addressPart);
					}
				}
			} else {
				for (int i = 0; i < ADDR_MAX_DATA_SIZE; i++) {
					String addressPart = dataArray[i];
					if (!TextUtils.isEmpty(addressPart)) {
						if (!empty) {
							builder.append(' ');
						} else {
							empty = false;
						}
						builder.append(addressPart);
					}
				}
			}

			return builder.toString().trim();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format(Locale.getDefault(),"type: %d, label: %s, isPrimary: %s",
					type, label, isPrimary);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			assert false : "hashCode not designed";
		return 44; // any arbitrary constant will do
		}
	}

	/**
	 * The Class OrganizationData.
	 */
	static public class OrganizationData {
		
		/** The type. */
		public final int type;
		// non-final is Intentional: we may change the values since this info is separated into
		// two parts in vCard: "ORG" + "TITLE".
		/** The company name. */
		public String companyName;
		
		/** The department name. */
		public String departmentName;
		
		/** The title name. */
		public String titleName;
		
		/** The is primary. */
		public boolean isPrimary;

		/**
		 * Instantiates a new organization data.
		 *
		 * @param type the type
		 * @param companyName the company name
		 * @param departmentName the department name
		 * @param titleName the title name
		 * @param isPrimary the is primary
		 */
		public OrganizationData(int type,
				String companyName,
				String departmentName,
				String titleName,
				boolean isPrimary) {
			this.type = type;
			this.companyName = companyName;
			this.departmentName = departmentName;
			this.titleName = titleName;
			this.isPrimary = isPrimary;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof OrganizationData)) {
				return false;
			}
			OrganizationData organization = (OrganizationData)obj;
			return (type == organization.type &&
					TextUtils.equals(companyName, organization.companyName) &&
					TextUtils.equals(departmentName, organization.departmentName) &&
					TextUtils.equals(titleName, organization.titleName) &&
					isPrimary == organization.isPrimary);
		}

		/**
		 * Gets the formatted string.
		 *
		 * @return the formatted string
		 */
		public String getFormattedString() {
			final StringBuilder builder = new StringBuilder();
			if (!TextUtils.isEmpty(companyName)) {
				builder.append(companyName);
			}

			if (!TextUtils.isEmpty(departmentName)) {
				if (builder.length() > 0) {
					builder.append(", ");
				}
				builder.append(departmentName);
			}

			if (!TextUtils.isEmpty(titleName)) {
				if (builder.length() > 0) {
					builder.append(", ");
				}
				builder.append(titleName);
			}

			return builder.toString();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format(Locale.getDefault(),
					"type: %d, company: %s, department: %s, title: %s, isPrimary: %s",
					type, companyName, departmentName, titleName, isPrimary);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			assert false : "hashCode not designed";
		return 45; // any arbitrary constant will do
		}
	}

	/**
	 * The Class ImData.
	 */
	static public class ImData {
		
		/** The protocol. */
		public final int protocol;
		
		/** The custom protocol. */
		public final String customProtocol;
		
		/** The type. */
		public final int type;
		
		/** The data. */
		public final String data;
		
		/** The is primary. */
		public final boolean isPrimary;

		/**
		 * Instantiates a new im data.
		 *
		 * @param protocol the protocol
		 * @param customProtocol the custom protocol
		 * @param type the type
		 * @param data the data
		 * @param isPrimary the is primary
		 */
		public ImData(final int protocol, final String customProtocol, final int type,
				final String data, final boolean isPrimary) {
			this.protocol = protocol;
			this.customProtocol = customProtocol;
			this.type = type;
			this.data = data;
			this.isPrimary = isPrimary;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ImData)) {
				return false;
			}
			ImData imData = (ImData)obj;
			return (type == imData.type && protocol == imData.protocol
					&& (customProtocol != null ? customProtocol.equals(imData.customProtocol) :
						(imData.customProtocol == null))
						&& (data != null ? data.equals(imData.data) : (imData.data == null))
						&& isPrimary == imData.isPrimary);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format(Locale.getDefault(),
					"type: %d, protocol: %d, custom_protcol: %s, data: %s, isPrimary: %s",
					type, protocol, customProtocol, data, isPrimary);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			assert false : "hashCode not designed";
		return 46; // any arbitrary constant will do
		}
	}

	/**
	 * The Class PhotoData.
	 */
	public static class PhotoData {
		
		/** The Constant FORMAT_FLASH. */
		public static final String FORMAT_FLASH = "SWF";
		
		/** The type. */
		public final int type;
		
		/** The format name. */
		public final String formatName;  // used when type is not defined in ContactsContract.
		
		/** The photo bytes. */
		public final byte[] photoBytes;
		
		/** The is primary. */
		public final boolean isPrimary;

		/**
		 * Instantiates a new photo data.
		 *
		 * @param type the type
		 * @param formatName the format name
		 * @param photoBytes the photo bytes
		 * @param isPrimary the is primary
		 */
		public PhotoData(int type, String formatName, byte[] photoBytes, boolean isPrimary) {
			this.type = type;
			this.formatName = formatName;
			this.photoBytes = photoBytes;
			this.isPrimary = isPrimary;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof PhotoData)) {
				return false;
			}
			PhotoData photoData = (PhotoData)obj;
			return (type == photoData.type &&
					(formatName == null ? (photoData.formatName == null) :
						formatName.equals(photoData.formatName)) &&
						(Arrays.equals(photoBytes, photoData.photoBytes)) &&
						(isPrimary == photoData.isPrimary));
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format(Locale.getDefault(),"type: %d, format: %s: size: %d, isPrimary: %s",
					type, formatName, photoBytes.length, isPrimary);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			assert false : "hashCode not designed";
		return 47; // any arbitrary constant will do
		}
	}

	/* package */ /**
	 * The Class Property.
	 */
	static class Property {
		
		/** The m property name. */
		private String mPropertyName;
		
		/** The m parameter map. */
		private Map<String, Collection<String>> mParameterMap =
				new HashMap<String, Collection<String>>();
		
		/** The m property value list. */
		private List<String> mPropertyValueList = new ArrayList<String>();
		
		/** The m property bytes. */
		private byte[] mPropertyBytes;

		/**
		 * Sets the property name.
		 *
		 * @param propertyName the new property name
		 */
		public void setPropertyName(final String propertyName) {
			mPropertyName = propertyName;
		}

		/**
		 * Adds the parameter.
		 *
		 * @param paramName the param name
		 * @param paramValue the param value
		 */
		public void addParameter(final String paramName, final String paramValue) {
			Collection<String> values;
			if (!mParameterMap.containsKey(paramName)) {
				if (paramName.equals("TYPE")) {
					values = new HashSet<String>();
				} else {
					values = new ArrayList<String>();
				}
				mParameterMap.put(paramName, values);
			} else {
				values = mParameterMap.get(paramName);
			}
			values.add(paramValue);
		}

		/**
		 * Adds the to property value list.
		 *
		 * @param propertyValue the property value
		 */
		public void addToPropertyValueList(final String propertyValue) {
			mPropertyValueList.add(propertyValue);
		}

		/**
		 * Sets the property bytes.
		 *
		 * @param propertyBytes the new property bytes
		 */
		public void setPropertyBytes(final byte[] propertyBytes) {
			mPropertyBytes = propertyBytes;
		}

		/**
		 * Gets the parameters.
		 *
		 * @param type the type
		 * @return the parameters
		 */
		public final Collection<String> getParameters(String type) {
			return mParameterMap.get(type);
		}

		/**
		 * Gets the property value list.
		 *
		 * @return the property value list
		 */
		public final List<String> getPropertyValueList() {
			return mPropertyValueList;
		}

		/**
		 * Clear.
		 */
		public void clear() {
			mPropertyName = null;
			mParameterMap.clear();
			mPropertyValueList.clear();
			mPropertyBytes = null;
		}
	}

	/** The m family name. */
	private String mFamilyName;
	
	/** The m given name. */
	private String mGivenName;
	
	/** The m middle name. */
	private String mMiddleName;
	
	/** The m prefix. */
	private String mPrefix;
	
	/** The m suffix. */
	private String mSuffix;

	// Used only when no family nor given name is found.
	/** The m full name. */
	private String mFullName;

	/** The m phonetic family name. */
	private String mPhoneticFamilyName;
	
	/** The m phonetic given name. */
	private String mPhoneticGivenName;
	
	/** The m phonetic middle name. */
	private String mPhoneticMiddleName;

	/** The m phonetic full name. */
	private String mPhoneticFullName;

	/** The m nick name list. */
	private List<String> mNickNameList;

	/** The m display name. */
	private String mDisplayName;

	/** The m birthday. */
	private String mBirthday;

	/** The m note list. */
	private List<String> mNoteList;
	
	/** The m phone list. */
	private List<PhoneData> mPhoneList;
	
	/** The m email list. */
	private List<EmailData> mEmailList;
	
	/** The m postal list. */
	private List<PostalData> mPostalList;
	
	/** The m organization list. */
	private List<OrganizationData> mOrganizationList;
	
	/** The m im list. */
	private List<ImData> mImList;
	
	/** The m photo list. */
	private List<PhotoData> mPhotoList;
	
	/** The m website list. */
	private List<String> mWebsiteList;
	
	/** The m android custom property list. */
	private List<List<String>> mAndroidCustomPropertyList;

	/** The m v card type. */
	private final int mVCardType;
	
	/** The m account. */
	private final Account mAccount;

	/**
	 * Instantiates a new v card entry.
	 */
	public VCardEntry() {
		this(VCardConfig.VCARD_TYPE_V21_GENERIC_UTF8);
	}

	/**
	 * Instantiates a new v card entry.
	 *
	 * @param vcardType the vcard type
	 */
	public VCardEntry(int vcardType) {
		this(vcardType, null);
	}

	/**
	 * Instantiates a new v card entry.
	 *
	 * @param vcardType the vcard type
	 * @param account the account
	 */
	public VCardEntry(int vcardType, Account account) {
		mVCardType = vcardType;
		mAccount = account;
	}

	/**
	 * Adds the phone.
	 *
	 * @param type the type
	 * @param data the data
	 * @param label the label
	 * @param isPrimary the is primary
	 */
	private void addPhone(int type, String data, String label, boolean isPrimary) {
		if (mPhoneList == null) {
			mPhoneList = new ArrayList<PhoneData>();
		}
		final StringBuilder builder = new StringBuilder();
		final String trimed = data.trim();
		final String formattedNumber;
		if (type == Phone.TYPE_PAGER) {
			formattedNumber = trimed;
		} else {
			final int length = trimed.length();
			for (int i = 0; i < length; i++) {
				char ch = trimed.charAt(i);
				if (('0' <= ch && ch <= '9') || (i == 0 && ch == '+')) {
					builder.append(ch);
				}
			}

			formattedNumber = PhoneNumberUtils.formatNumber(builder.toString());
		}
		PhoneData phoneData = new PhoneData(type, formattedNumber, label, isPrimary);
		mPhoneList.add(phoneData);
	}

	/**
	 * Adds the nick name.
	 *
	 * @param nickName the nick name
	 */
	private void addNickName(final String nickName) {
		if (mNickNameList == null) {
			mNickNameList = new ArrayList<String>();
		}
		mNickNameList.add(nickName);
	}

	/**
	 * Adds the email.
	 *
	 * @param type the type
	 * @param data the data
	 * @param label the label
	 * @param isPrimary the is primary
	 */
	private void addEmail(int type, String data, String label, boolean isPrimary){
		if (mEmailList == null) {
			mEmailList = new ArrayList<EmailData>();
		}
		mEmailList.add(new EmailData(type, data, label, isPrimary));
	}

	/**
	 * Adds the postal.
	 *
	 * @param type the type
	 * @param propValueList the prop value list
	 * @param label the label
	 * @param isPrimary the is primary
	 */
	private void addPostal(int type, List<String> propValueList, String label, boolean isPrimary){
		if (mPostalList == null) {
			mPostalList = new ArrayList<PostalData>(0);
		}
		mPostalList.add(new PostalData(type, propValueList, label, isPrimary));
	}

	/**
	 * Should be called via {@link #handleOrgValue(int, List, boolean)} or.
	 *
	 * @param type the type
	 * @param companyName the company name
	 * @param departmentName the department name
	 * @param titleName the title name
	 * @param isPrimary the is primary
	 * {@link #handleTitleValue(String)}.
	 */
	private void addNewOrganization(int type, final String companyName,
			final String departmentName,
			final String titleName, boolean isPrimary) {
		if (mOrganizationList == null) {
			mOrganizationList = new ArrayList<OrganizationData>();
		}
		mOrganizationList.add(new OrganizationData(type, companyName,
				departmentName, titleName, isPrimary));
	}

	/** The Constant sEmptyList. */
	private static final List<String> sEmptyList =
			Collections.unmodifiableList(new ArrayList<String>(0));

	/**
	 * Set "ORG" related values to the appropriate data. If there's more than one
	 *
	 * @param type the type
	 * @param orgList the org list
	 * @param isPrimary the is primary
	 * {@link OrganizationData} objects, this input data are attached to the last one which
	 * does not have valid values (not including empty but only null). If there's no
	 * {@link OrganizationData} object, a new {@link OrganizationData} is created,
	 * whose title is set to null.
	 */
	private void handleOrgValue(final int type, List<String> orgList, boolean isPrimary) {
		if (orgList == null) {
			orgList = sEmptyList;
		}
		final String companyName;
		final String departmentName;
		final int size = orgList.size();
		switch (size) {
		case 0: {
			companyName = "";
			departmentName = null;
			break;
		}
		case 1: {
			companyName = orgList.get(0);
			departmentName = null;
			break;
		}
		default: {  // More than 1.
			companyName = orgList.get(0);
			// We're not sure which is the correct string for department.
			// In order to keep all the data, concatinate the rest of elements.
			StringBuilder builder = new StringBuilder();
			for (int i = 1; i < size; i++) {
				if (i > 1) {
					builder.append(' ');
				}
				builder.append(orgList.get(i));
			}
			departmentName = builder.toString();
		}
		}
		if (mOrganizationList == null) {
			// Create new first organization entry, with "null" title which may be
			// added via handleTitleValue().
			addNewOrganization(type, companyName, departmentName, null, isPrimary);
			return;
		}
		for (OrganizationData organizationData : mOrganizationList) {
			// Not use TextUtils.isEmpty() since ORG was set but the elements might be empty.
			// e.g. "ORG;PREF:;" -> Both companyName and departmentName become empty but not null.
			if (organizationData.companyName == null &&
					organizationData.departmentName == null) {
				// Probably the "TITLE" property comes before the "ORG" property via
				// handleTitleLine().
				organizationData.companyName = companyName;
				organizationData.departmentName = departmentName;
				organizationData.isPrimary = isPrimary;
				return;
			}
		}
		// No OrganizatioData is available. Create another one, with "null" title, which may be
		// added via handleTitleValue().
		addNewOrganization(type, companyName, departmentName, null, isPrimary);
	}

	/**
	 * Set "title" value to the appropriate data. If there's more than one
	 * OrganizationData objects, this input is attached to the last one which does not
	 * have valid title value (not including empty but only null). If there's no
	 * OrganizationData object, a new OrganizationData is created, whose company name is
	 * set to null.
	 *
	 * @param title the title
	 */
	private void handleTitleValue(final String title) {
		if (mOrganizationList == null) {
			// Create new first organization entry, with "null" other info, which may be
			// added via handleOrgValue().
			addNewOrganization(DEFAULT_ORGANIZATION_TYPE, null, null, title, false);
			return;
		}
		for (OrganizationData organizationData : mOrganizationList) {
			if (organizationData.titleName == null) {
				organizationData.titleName = title;
				return;
			}
		}
		// No Organization is available. Create another one, with "null" other info, which may be
		// added via handleOrgValue().
		addNewOrganization(DEFAULT_ORGANIZATION_TYPE, null, null, title, false);
	}

	/**
	 * Adds the im.
	 *
	 * @param protocol the protocol
	 * @param customProtocol the custom protocol
	 * @param type the type
	 * @param propValue the prop value
	 * @param isPrimary the is primary
	 */
	private void addIm(int protocol, String customProtocol, int type,
			String propValue, boolean isPrimary) {
		if (mImList == null) {
			mImList = new ArrayList<ImData>();
		}
		mImList.add(new ImData(protocol, customProtocol, type, propValue, isPrimary));
	}

	/**
	 * Adds the note.
	 *
	 * @param note the note
	 */
	private void addNote(final String note) {
		if (mNoteList == null) {
			mNoteList = new ArrayList<String>(1);
		}
		mNoteList.add(note);
	}

	/**
	 * Adds the photo bytes.
	 *
	 * @param formatName the format name
	 * @param photoBytes the photo bytes
	 * @param isPrimary the is primary
	 */
	private void addPhotoBytes(String formatName, byte[] photoBytes, boolean isPrimary) {
		if (mPhotoList == null) {
			mPhotoList = new ArrayList<PhotoData>(1);
		}
		final PhotoData photoData = new PhotoData(0, null, photoBytes, isPrimary);
		mPhotoList.add(photoData);
	}

	/**
	 * Handle n property.
	 *
	 * @param elems the elems
	 */
	@SuppressWarnings("fallthrough")
	private void handleNProperty(List<String> elems) {
		// Family, Given, Middle, Prefix, Suffix. (1 - 5)
		int size;
		if (elems == null || (size = elems.size()) < 1) {
			return;
		}
		if (size > 5) {
			size = 5;
		}

		switch (size) {
		// fallthrough
		case 5: mSuffix = elems.get(4);
		case 4: mPrefix = elems.get(3);
		case 3: mMiddleName = elems.get(2);
		case 2: mGivenName = elems.get(1);
		default: mFamilyName = elems.get(0);
		}
	}

	/**
	 * Note: Some Japanese mobile phones use this field for phonetic name,
	 * since vCard 2.1 does not have "SORT-STRING" type.
	 * Also, in some cases, the field has some ';'s in it.
	 * Assume the ';' means the same meaning in N property
	 *
	 * @param elems the elems
	 */
	@SuppressWarnings("fallthrough")
	private void handlePhoneticNameFromSound(List<String> elems) {
		if (!(TextUtils.isEmpty(mPhoneticFamilyName) &&
				TextUtils.isEmpty(mPhoneticMiddleName) &&
				TextUtils.isEmpty(mPhoneticGivenName))) {
			// This means the other properties like "X-PHONETIC-FIRST-NAME" was already found.
			// Ignore "SOUND;X-IRMC-N".
			return;
		}

		int size;
		if (elems == null || (size = elems.size()) < 1) {
			return;
		}

		// Assume that the order is "Family, Given, Middle".
		// This is not from specification but mere assumption. Some Japanese phones use this order.
		if (size > 3) {
			size = 3;
		}

		if (elems.get(0).length() > 0) {
			boolean onlyFirstElemIsNonEmpty = true;
			for (int i = 1; i < size; i++) {
				if (elems.get(i).length() > 0) {
					onlyFirstElemIsNonEmpty = false;
					break;
				}
			}
			if (onlyFirstElemIsNonEmpty) {
				final String[] namesArray = elems.get(0).split(" ");
				final int nameArrayLength = namesArray.length;
				if (nameArrayLength == 3) {
					// Assume the string is "Family Middle Given".
					mPhoneticFamilyName = namesArray[0];
					mPhoneticMiddleName = namesArray[1];
					mPhoneticGivenName = namesArray[2];
				} else if (nameArrayLength == 2) {
					// Assume the string is "Family Given" based on the Japanese mobile
					// phones' preference.
					mPhoneticFamilyName = namesArray[0];
					mPhoneticGivenName = namesArray[1];
				} else {
					mPhoneticFullName = elems.get(0);
				}
				return;
			}
		}

		switch (size) {
		// fallthrough
		case 3: mPhoneticMiddleName = elems.get(2);
		case 2: mPhoneticGivenName = elems.get(1);
		default: mPhoneticFamilyName = elems.get(0);
		}
	}

	/**
	 * Adds the property.
	 *
	 * @param property the property
	 */
	public void addProperty(final Property property) {
		final String propName = property.mPropertyName;
		final Map<String, Collection<String>> paramMap = property.mParameterMap;
		final List<String> propValueList = property.mPropertyValueList;
		byte[] propBytes = property.mPropertyBytes;

		if (propValueList.size() == 0) {
			return;
		}
		final String propValue = listToString(propValueList).trim();

		if (propName.equals(VCardConstants.PROPERTY_VERSION)) {
			// vCard version. Ignore this.
		} else if (propName.equals(VCardConstants.PROPERTY_FN)) {
			mFullName = propValue;
		} else if (propName.equals(VCardConstants.PROPERTY_NAME) && mFullName == null) {
			// Only in vCard 3.0. Use this if FN, which must exist in vCard 3.0 but may not
			// actually exist in the real vCard data, does not exist.
			mFullName = propValue;
		} else if (propName.equals(VCardConstants.PROPERTY_N)) {
			handleNProperty(propValueList);
		} else if (propName.equals(VCardConstants.PROPERTY_SORT_STRING)) {
			mPhoneticFullName = propValue;
		} else if (propName.equals(VCardConstants.PROPERTY_NICKNAME) ||
				propName.equals(VCardConstants.ImportOnly.PROPERTY_X_NICKNAME)) {
			addNickName(propValue);
		} else if (propName.equals(VCardConstants.PROPERTY_SOUND)) {
			Collection<String> typeCollection = paramMap.get(VCardConstants.PARAM_TYPE);
			if (typeCollection != null
					&& typeCollection.contains(VCardConstants.PARAM_TYPE_X_IRMC_N)) {
				// As of 2009-10-08, Parser side does not split a property value into separated
				// values using ';' (in other words, propValueList.size() == 1),
				// which is correct behavior from the view of vCard 2.1.
				// But we want it to be separated, so do the separation here.
				final List<String> phoneticNameList =
						VCardUtils.constructListFromValue(propValue,
								VCardConfig.isV30(mVCardType));
				handlePhoneticNameFromSound(phoneticNameList);
			} else {
				// Ignore this field since Android cannot understand what it is.
			}
		} else if (propName.equals(VCardConstants.PROPERTY_ADR)) {
			boolean valuesAreAllEmpty = true;
			for (String value : propValueList) {
				if (value.length() > 0) {
					valuesAreAllEmpty = false;
					break;
				}
			}
			if (valuesAreAllEmpty) {
				return;
			}

			int type = -1;
			String label = "";
			boolean isPrimary = false;
			Collection<String> typeCollection = paramMap.get(VCardConstants.PARAM_TYPE);
			if (typeCollection != null) {
				for (String typeString : typeCollection) {
					typeString = typeString.toUpperCase(Locale.getDefault());
					if (typeString.equals(VCardConstants.PARAM_TYPE_PREF)) {
						isPrimary = true;
					} else if (typeString.equals(VCardConstants.PARAM_TYPE_HOME)) {
						type = StructuredPostal.TYPE_HOME;
						label = "";
					} else if (typeString.equals(VCardConstants.PARAM_TYPE_WORK) ||
							typeString.equalsIgnoreCase(VCardConstants.PARAM_EXTRA_TYPE_COMPANY)) {
						// "COMPANY" seems emitted by Windows Mobile, which is not
						// specifically supported by vCard 2.1. We assume this is same
						// as "WORK".
						type = StructuredPostal.TYPE_WORK;
						label = "";
					} else if (typeString.equals(VCardConstants.PARAM_ADR_TYPE_PARCEL) ||
							typeString.equals(VCardConstants.PARAM_ADR_TYPE_DOM) ||
							typeString.equals(VCardConstants.PARAM_ADR_TYPE_INTL)) {
						// We do not have any appropriate way to store this information.
					} else {
						if (typeString.startsWith("X-") && type < 0) {
							typeString = typeString.substring(2);
						}
						// vCard 3.0 allows iana-token. Also some vCard 2.1 exporters
						// emit non-standard types. We do not handle their values now.
						type = StructuredPostal.TYPE_CUSTOM;
						label = typeString;
					}
				}
			}
			// We use "HOME" as default
			if (type < 0) {
				type = StructuredPostal.TYPE_HOME;
			}

			addPostal(type, propValueList, label, isPrimary);
		} else if (propName.equals(VCardConstants.PROPERTY_EMAIL)) {
			int type = -1;
			String label = null;
			boolean isPrimary = false;
			Collection<String> typeCollection = paramMap.get(VCardConstants.PARAM_TYPE);
			if (typeCollection != null) {
				for (String typeString : typeCollection) {
					typeString = typeString.toUpperCase(Locale.getDefault());
					if (typeString.equals(VCardConstants.PARAM_TYPE_PREF)) {
						isPrimary = true;
					} else if (typeString.equals(VCardConstants.PARAM_TYPE_HOME)) {
						type = Email.TYPE_HOME;
					} else if (typeString.equals(VCardConstants.PARAM_TYPE_WORK)) {
						type = Email.TYPE_WORK;
					} else if (typeString.equals(VCardConstants.PARAM_TYPE_CELL)) {
						type = Email.TYPE_MOBILE;
					} else {
						if (typeString.startsWith("X-") && type < 0) {
							typeString = typeString.substring(2);
						}
						// vCard 3.0 allows iana-token.
						// We may have INTERNET (specified in vCard spec),
						// SCHOOL, etc.
						type = Email.TYPE_CUSTOM;
						label = typeString;
					}
				}
			}
			if (type < 0) {
				type = Email.TYPE_OTHER;
			}
			addEmail(type, propValue, label, isPrimary);
		} else if (propName.equals(VCardConstants.PROPERTY_ORG)) {
			// vCard specification does not specify other types.
			final int type = Organization.TYPE_WORK;
			boolean isPrimary = false;
			Collection<String> typeCollection = paramMap.get(VCardConstants.PARAM_TYPE);
			if (typeCollection != null) {
				for (String typeString : typeCollection) {
					if (typeString.equals(VCardConstants.PARAM_TYPE_PREF)) {
						isPrimary = true;
					}
				}
			}
			handleOrgValue(type, propValueList, isPrimary);
		} else if (propName.equals(VCardConstants.PROPERTY_TITLE)) {
			handleTitleValue(propValue);
		} else if (propName.equals(VCardConstants.PROPERTY_ROLE)) {
			// This conflicts with TITLE. Ignore for now...
			// handleTitleValue(propValue);
		} else if (propName.equals(VCardConstants.PROPERTY_PHOTO) ||
				propName.equals(VCardConstants.PROPERTY_LOGO)) {
			Collection<String> paramMapValue = paramMap.get("VALUE");
			if (paramMapValue != null && paramMapValue.contains("URL")) {
				// Currently we do not have appropriate example for testing this case.
			} else {
				final Collection<String> typeCollection = paramMap.get("TYPE");
				String formatName = null;
				boolean isPrimary = false;
				if (typeCollection != null) {
					for (String typeValue : typeCollection) {
						if (VCardConstants.PARAM_TYPE_PREF.equals(typeValue)) {
							isPrimary = true;
						} else if (formatName == null){
							formatName = typeValue;
						}
					}
				}
				addPhotoBytes(formatName, propBytes, isPrimary);
			}
		} else if (propName.equals(VCardConstants.PROPERTY_TEL)) {
			final Collection<String> typeCollection = paramMap.get(VCardConstants.PARAM_TYPE);
			final Object typeObject =
					VCardUtils.getPhoneTypeFromStrings(typeCollection, propValue);
			final int type;
			final String label;
			if (typeObject instanceof Integer) {
				type = (Integer)typeObject;
				label = null;
			} else {
				type = Phone.TYPE_CUSTOM;
				label = typeObject.toString();
			}

			final boolean isPrimary;
			if (typeCollection != null && typeCollection.contains(VCardConstants.PARAM_TYPE_PREF)) {
				isPrimary = true;
			} else {
				isPrimary = false;
			}
			addPhone(type, propValue, label, isPrimary);
		} else if (propName.equals(VCardConstants.PROPERTY_X_SKYPE_PSTNNUMBER)) {
			// The phone number available via Skype.
			Collection<String> typeCollection = paramMap.get(VCardConstants.PARAM_TYPE);
			final int type = Phone.TYPE_OTHER;
			final boolean isPrimary;
			if (typeCollection != null && typeCollection.contains(VCardConstants.PARAM_TYPE_PREF)) {
				isPrimary = true;
			} else {
				isPrimary = false;
			}
			addPhone(type, propValue, null, isPrimary);
		} else if (sImMap.containsKey(propName)) {
			final int protocol = sImMap.get(propName);
			boolean isPrimary = false;
			int type = -1;
			final Collection<String> typeCollection = paramMap.get(VCardConstants.PARAM_TYPE);
			if (typeCollection != null) {
				for (String typeString : typeCollection) {
					if (typeString.equals(VCardConstants.PARAM_TYPE_PREF)) {
						isPrimary = true;
					} else if (type < 0) {
						if (typeString.equalsIgnoreCase(VCardConstants.PARAM_TYPE_HOME)) {
							type = Im.TYPE_HOME;
						} else if (typeString.equalsIgnoreCase(VCardConstants.PARAM_TYPE_WORK)) {
							type = Im.TYPE_WORK;
						}
					}
				}
			}
			if (type < 0) {
				type = Phone.TYPE_HOME;
			}
			addIm(protocol, null, type, propValue, isPrimary);
		} else if (propName.equals(VCardConstants.PROPERTY_NOTE)) {
			addNote(propValue);
		} else if (propName.equals(VCardConstants.PROPERTY_URL)) {
			if (mWebsiteList == null) {
				mWebsiteList = new ArrayList<String>(1);
			}
			mWebsiteList.add(propValue);
		} else if (propName.equals(VCardConstants.PROPERTY_BDAY)) {
			mBirthday = propValue;
		} else if (propName.equals(VCardConstants.PROPERTY_X_PHONETIC_FIRST_NAME)) {
			mPhoneticGivenName = propValue;
		} else if (propName.equals(VCardConstants.PROPERTY_X_PHONETIC_MIDDLE_NAME)) {
			mPhoneticMiddleName = propValue;
		} else if (propName.equals(VCardConstants.PROPERTY_X_PHONETIC_LAST_NAME)) {
			mPhoneticFamilyName = propValue;
		} else if (propName.equals(VCardConstants.PROPERTY_X_ANDROID_CUSTOM)) {
			final List<String> customPropertyList =
					VCardUtils.constructListFromValue(propValue,
							VCardConfig.isV30(mVCardType));
			handleAndroidCustomProperty(customPropertyList);
			/*} else if (propName.equals("REV")) {
            // Revision of this VCard entry. I think we can ignore this.
        } else if (propName.equals("UID")) {
        } else if (propName.equals("KEY")) {
            // Type is X509 or PGP? I don't know how to handle this...
        } else if (propName.equals("MAILER")) {
        } else if (propName.equals("TZ")) {
        } else if (propName.equals("GEO")) {
        } else if (propName.equals("CLASS")) {
            // vCard 3.0 only.
            // e.g. CLASS:CONFIDENTIAL
        } else if (propName.equals("PROFILE")) {
            // VCard 3.0 only. Must be "VCARD". I think we can ignore this.
        } else if (propName.equals("CATEGORIES")) {
            // VCard 3.0 only.
            // e.g. CATEGORIES:INTERNET,IETF,INDUSTRY,INFORMATION TECHNOLOGY
        } else if (propName.equals("SOURCE")) {
            // VCard 3.0 only.
        } else if (propName.equals("PRODID")) {
            // VCard 3.0 only.
            // To specify the identifier for the product that created
            // the vCard object.*/
		} else {
			// Unknown X- words and IANA token.
		}
	}

	/**
	 * Handle android custom property.
	 *
	 * @param customPropertyList the custom property list
	 */
	private void handleAndroidCustomProperty(final List<String> customPropertyList) {
		if (mAndroidCustomPropertyList == null) {
			mAndroidCustomPropertyList = new ArrayList<List<String>>();
		}
		mAndroidCustomPropertyList.add(customPropertyList);
	}

	/**
	 * Construct the display name. The constructed data must not be null.
	 */
	private void constructDisplayName() {
		// FullName (created via "FN" or "NAME" field) is prefered.
		if (!TextUtils.isEmpty(mFullName)) {
			mDisplayName = mFullName;
		} else if (!(TextUtils.isEmpty(mFamilyName) && TextUtils.isEmpty(mGivenName))) {
			mDisplayName = VCardUtils.constructNameFromElements(mVCardType,
					mFamilyName, mMiddleName, mGivenName, mPrefix, mSuffix);
		} else if (!(TextUtils.isEmpty(mPhoneticFamilyName) &&
				TextUtils.isEmpty(mPhoneticGivenName))) {
			mDisplayName = VCardUtils.constructNameFromElements(mVCardType,
					mPhoneticFamilyName, mPhoneticMiddleName, mPhoneticGivenName);
		} else if (mEmailList != null && mEmailList.size() > 0) {
			mDisplayName = mEmailList.get(0).data;
		} else if (mPhoneList != null && mPhoneList.size() > 0) {
			mDisplayName = mPhoneList.get(0).data;
		} else if (mPostalList != null && mPostalList.size() > 0) {
			mDisplayName = mPostalList.get(0).getFormattedAddress(mVCardType);
		} else if (mOrganizationList != null && mOrganizationList.size() > 0) {
			mDisplayName = mOrganizationList.get(0).getFormattedString();
		}

		if (mDisplayName == null) {
			mDisplayName = "";
		}
	}

	/**
	 * Consolidate several fielsds (like mName) using name candidates,.
	 */
	public void consolidateFields() {
		constructDisplayName();

		if (mPhoneticFullName != null) {
			mPhoneticFullName = mPhoneticFullName.trim();
		}
	}

	/**
	 * Push into content resolver.
	 *
	 * @param resolver the resolver
	 * @return the uri
	 */
	public Uri pushIntoContentResolver(ContentResolver resolver) {
		ArrayList<ContentProviderOperation> operationList =
				new ArrayList<ContentProviderOperation>();
		// After applying the batch the first result's Uri is returned so it is important that
		// the RawContact is the first operation that gets inserted into the list
		ContentProviderOperation.Builder builder =
				ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
		String myGroupsId = null;
		if (mAccount != null) {
			builder.withValue(RawContacts.ACCOUNT_NAME, mAccount.name);
			builder.withValue(RawContacts.ACCOUNT_TYPE, mAccount.type);

			// Assume that caller side creates this group if it does not exist.
			if (ACCOUNT_TYPE_GOOGLE.equals(mAccount.type)) {
				final Cursor cursor = resolver.query(Groups.CONTENT_URI, new String[] {
						Groups.SOURCE_ID },
						Groups.TITLE + "=?", new String[] {
						GOOGLE_MY_CONTACTS_GROUP }, null);
				try {
					if (cursor != null && cursor.moveToFirst()) {
						myGroupsId = cursor.getString(0);
					}
				} finally {
					if (cursor != null) {
						cursor.close();
					}
				}
			}
		} else {
			builder.withValue(RawContacts.ACCOUNT_NAME, null);
			builder.withValue(RawContacts.ACCOUNT_TYPE, null);
		}
		operationList.add(builder.build());

		if (!nameFieldsAreEmpty()) {
			builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
			builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, 0);
			builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);

			builder.withValue(StructuredName.GIVEN_NAME, mGivenName);
			builder.withValue(StructuredName.FAMILY_NAME, mFamilyName);
			builder.withValue(StructuredName.MIDDLE_NAME, mMiddleName);
			builder.withValue(StructuredName.PREFIX, mPrefix);
			builder.withValue(StructuredName.SUFFIX, mSuffix);

			if (!(TextUtils.isEmpty(mPhoneticGivenName)
					&& TextUtils.isEmpty(mPhoneticFamilyName)
					&& TextUtils.isEmpty(mPhoneticMiddleName))) {
				builder.withValue(StructuredName.PHONETIC_GIVEN_NAME, mPhoneticGivenName);
				builder.withValue(StructuredName.PHONETIC_FAMILY_NAME, mPhoneticFamilyName);
				builder.withValue(StructuredName.PHONETIC_MIDDLE_NAME, mPhoneticMiddleName);
			} else if (!TextUtils.isEmpty(mPhoneticFullName)) {
				builder.withValue(StructuredName.PHONETIC_GIVEN_NAME, mPhoneticFullName);
			}

			builder.withValue(StructuredName.DISPLAY_NAME, getDisplayName());
			operationList.add(builder.build());
		}

		if (mNickNameList != null && mNickNameList.size() > 0) {
			for (String nickName : mNickNameList) {
				builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
				builder.withValueBackReference(Nickname.RAW_CONTACT_ID, 0);
				builder.withValue(Data.MIMETYPE, Nickname.CONTENT_ITEM_TYPE);
				builder.withValue(Nickname.TYPE, Nickname.TYPE_DEFAULT);
				builder.withValue(Nickname.NAME, nickName);
				operationList.add(builder.build());
			}
		}

		if (mPhoneList != null) {
			for (PhoneData phoneData : mPhoneList) {
				builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
				builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
				builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);

				builder.withValue(Phone.TYPE, phoneData.type);
				if (phoneData.type == Phone.TYPE_CUSTOM) {
					builder.withValue(Phone.LABEL, phoneData.label);
				}
				builder.withValue(Phone.NUMBER, phoneData.data);
				if (phoneData.isPrimary) {
					builder.withValue(Phone.IS_PRIMARY, 1);
				}
				operationList.add(builder.build());
				contactExist = contactExist(resolver, phoneData.data);
			}
		}

		if (mOrganizationList != null) {
			for (OrganizationData organizationData : mOrganizationList) {
				builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
				builder.withValueBackReference(Organization.RAW_CONTACT_ID, 0);
				builder.withValue(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
				builder.withValue(Organization.TYPE, organizationData.type);
				if (organizationData.companyName != null) {
					builder.withValue(Organization.COMPANY, organizationData.companyName);
				}
				if (organizationData.departmentName != null) {
					builder.withValue(Organization.DEPARTMENT, organizationData.departmentName);
				}
				if (organizationData.titleName != null) {
					builder.withValue(Organization.TITLE, organizationData.titleName);
				}
				if (organizationData.isPrimary) {
					builder.withValue(Organization.IS_PRIMARY, 1);
				}
				operationList.add(builder.build());
			}
		}

		if (mEmailList != null) {
			for (EmailData emailData : mEmailList) {
				builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
				builder.withValueBackReference(Email.RAW_CONTACT_ID, 0);
				builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);

				builder.withValue(Email.TYPE, emailData.type);
				if (emailData.type == Email.TYPE_CUSTOM) {
					builder.withValue(Email.LABEL, emailData.label);
				}
				builder.withValue(Email.DATA, emailData.data);
				if (emailData.m_isPrimary) {
					builder.withValue(Data.IS_PRIMARY, 1);
				}
				operationList.add(builder.build());
				contactExist = BackupRestoreUtils.checkEmailExist(emailData.data);
			}
		}

		if (mPostalList != null) {
			for (PostalData postalData : mPostalList) {
				builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
				VCardUtils.insertStructuredPostalDataUsingContactsStruct(
						mVCardType, builder, postalData);
				operationList.add(builder.build());
			}
		}

		if (mImList != null) {
			for (ImData imData : mImList) {
				builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
				builder.withValueBackReference(Im.RAW_CONTACT_ID, 0);
				builder.withValue(Data.MIMETYPE, Im.CONTENT_ITEM_TYPE);
				builder.withValue(Im.TYPE, imData.type);
				builder.withValue(Im.PROTOCOL, imData.protocol);
				if (imData.protocol == Im.PROTOCOL_CUSTOM) {
					builder.withValue(Im.CUSTOM_PROTOCOL, imData.customProtocol);
				}
				if (imData.isPrimary) {
					builder.withValue(Data.IS_PRIMARY, 1);
				}
			}
		}

		if (mNoteList != null) {
			for (String note : mNoteList) {
				builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
				builder.withValueBackReference(Note.RAW_CONTACT_ID, 0);
				builder.withValue(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE);
				builder.withValue(Note.NOTE, note);
				operationList.add(builder.build());
			}
		}

		if (mPhotoList != null) {
			for (PhotoData photoData : mPhotoList) {
				builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
				builder.withValueBackReference(Photo.RAW_CONTACT_ID, 0);
				builder.withValue(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
				builder.withValue(Photo.PHOTO, photoData.photoBytes);
				if (photoData.isPrimary) {
					builder.withValue(Photo.IS_PRIMARY, 1);
				}
				operationList.add(builder.build());
			}
		}

		if (mWebsiteList != null) {
			for (String website : mWebsiteList) {
				builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
				builder.withValueBackReference(Website.RAW_CONTACT_ID, 0);
				builder.withValue(Data.MIMETYPE, Website.CONTENT_ITEM_TYPE);
				builder.withValue(Website.URL, website);
				// There's no information about the type of URL in vCard.
				// We use TYPE_HOMEPAGE for safety.
				builder.withValue(Website.TYPE, Website.TYPE_HOMEPAGE);
						operationList.add(builder.build());
			}
		}

		if (!TextUtils.isEmpty(mBirthday)) {
			builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
			builder.withValueBackReference(Event.RAW_CONTACT_ID, 0);
			builder.withValue(Data.MIMETYPE, Event.CONTENT_ITEM_TYPE);
			builder.withValue(Event.START_DATE, mBirthday);
			builder.withValue(Event.TYPE, Event.TYPE_BIRTHDAY);
			operationList.add(builder.build());
		}

		if (mAndroidCustomPropertyList != null) {
			for (List<String> customPropertyList : mAndroidCustomPropertyList) {
				int size = customPropertyList.size();
				if (size < 2 || TextUtils.isEmpty(customPropertyList.get(0))) {
					continue;
				} else if (size > VCardConstants.MAX_DATA_COLUMN + 1) {
					size = VCardConstants.MAX_DATA_COLUMN + 1;
					customPropertyList =
							customPropertyList.subList(0, VCardConstants.MAX_DATA_COLUMN + 2);
				}

				int i = 0;
				for (final String customPropertyValue : customPropertyList) {
					if (i == 0) {
						final String mimeType = customPropertyValue;
						builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
						builder.withValueBackReference(GroupMembership.RAW_CONTACT_ID, 0);
						builder.withValue(Data.MIMETYPE, mimeType);
					} else {  // 1 <= i && i <= MAX_DATA_COLUMNS
							if (!TextUtils.isEmpty(customPropertyValue)) {
								builder.withValue("data" + i, customPropertyValue);
							}
					}

					i++;
				}
				operationList.add(builder.build());
			}
		}

		if (myGroupsId != null) {
			builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
			builder.withValueBackReference(GroupMembership.RAW_CONTACT_ID, 0);
			builder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
			builder.withValue(GroupMembership.GROUP_SOURCE_ID, myGroupsId);
			operationList.add(builder.build());
		}
		//Neu da ton tai so dien thoai thi khong add nua
		if (!contactExist) {
			try {
				ContentProviderResult[] results = resolver.applyBatch(
						ContactsContract.AUTHORITY, operationList);
				// the first result is always the raw_contact. return it's uri so
				// that it can be found later. do null checking for badly behaving
				// ContentResolvers
				return (results == null || results.length == 0 || results[0] == null)
						? null
								: results[0].uri;
			} catch (RemoteException e) {
				Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
				return null;
			} catch (OperationApplicationException e) {
				Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
				return null;
			}
		}
		else return null;
	}

	/**
	 * Builds the from resolver.
	 *
	 * @param resolver the resolver
	 * @return the v card entry
	 */
	public static VCardEntry buildFromResolver(ContentResolver resolver) {
		return buildFromResolver(resolver, Contacts.CONTENT_URI);
	}

	/**
	 * Builds the from resolver.
	 *
	 * @param resolver the resolver
	 * @param uri the uri
	 * @return the v card entry
	 */
	public static VCardEntry buildFromResolver(ContentResolver resolver, Uri uri) {

		return null;
	}

	/**
	 * Name fields are empty.
	 *
	 * @return true, if successful
	 */
	private boolean nameFieldsAreEmpty() {
		return (TextUtils.isEmpty(mFamilyName)
				&& TextUtils.isEmpty(mMiddleName)
				&& TextUtils.isEmpty(mGivenName)
				&& TextUtils.isEmpty(mPrefix)
				&& TextUtils.isEmpty(mSuffix)
				&& TextUtils.isEmpty(mFullName)
				&& TextUtils.isEmpty(mPhoneticFamilyName)
				&& TextUtils.isEmpty(mPhoneticMiddleName)
				&& TextUtils.isEmpty(mPhoneticGivenName)
				&& TextUtils.isEmpty(mPhoneticFullName));
	}

	/**
	 * Checks if is ignorable.
	 *
	 * @return true, if is ignorable
	 */
	public boolean isIgnorable() {
		return getDisplayName().length() == 0;
	}
	/**
	 * Contact exist.
	 *
	 * @param phoneNumber the phone number
	 * @return true, if successful
	 */
	public boolean contactExist(ContentResolver resolver, String phoneNumber) {
		phoneNumber = phoneNumber.replace("-", "");
		phoneNumber = phoneNumber.replace(" ", "");
		//Kiem tra so dien thoai da ton tai trong contact chua
		return checkContactExists(resolver, phoneNumber);
	}
	
	/**
	 * Kiem tra Contact Exists
	 * @param context
	 * @param number
	 * @return
	 */
	public static final boolean checkContactExists(ContentResolver resolver, String number) 
	{
		Cursor cur = null;
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		String[] mPhoneNumberProjection = {PhoneLookup._ID, 
		        PhoneLookup.NUMBER, PhoneLookup.DISPLAY_NAME, PhoneLookup.PHOTO_ID};
		cur = resolver.query(lookupUri, mPhoneNumberProjection, null, null, null);
		try 
		{
			if (cur != null && cur.moveToFirst()) return true;			
		} 
		finally 
		{
			if (cur != null) cur.close();
		}
		return false;
	}

	/**
	 * List to string.
	 *
	 * @param list the list
	 * @return the string
	 */
	private String listToString(List<String> list){
		final int size = list.size();
		if (size > 1) {
			StringBuilder builder = new StringBuilder();
			int i = 0;
			for (String type : list) {
				builder.append(type);
				if (i < size - 1) {
					builder.append(";");
				}
			}
			return builder.toString();
		} else if (size == 1) {
			return list.get(0);
		} else {
			return "";
		}
	}

	// All getter methods should be used carefully, since they may change
	// in the future as of 2009-10-05, on which I cannot be sure this structure
	// is completely consolidated.
	//
	// Also note that these getter methods should be used only after
	// all properties being pushed into this object. If not, incorrect
	// value will "be stored in the local cache and" be returned to you.

	/**
	 * Gets the family name.
	 *
	 * @return the family name
	 */
	public String getFamilyName() {
		return mFamilyName;
	}

	/**
	 * Gets the given name.
	 *
	 * @return the given name
	 */
	public String getGivenName() {
		return mGivenName;
	}

	/**
	 * Gets the middle name.
	 *
	 * @return the middle name
	 */
	public String getMiddleName() {
		return mMiddleName;
	}

	/**
	 * Gets the prefix.
	 *
	 * @return the prefix
	 */
	public String getPrefix() {
		return mPrefix;
	}

	/**
	 * Gets the suffix.
	 *
	 * @return the suffix
	 */
	public String getSuffix() {
		return mSuffix;
	}

	/**
	 * Gets the full name.
	 *
	 * @return the full name
	 */
	public String getFullName() {
		return mFullName;
	}

	/**
	 * Gets the phonetic family name.
	 *
	 * @return the phonetic family name
	 */
	public String getPhoneticFamilyName() {
		return mPhoneticFamilyName;
	}

	/**
	 * Gets the phonetic given name.
	 *
	 * @return the phonetic given name
	 */
	public String getPhoneticGivenName() {
		return mPhoneticGivenName;
	}

	/**
	 * Gets the phonetic middle name.
	 *
	 * @return the phonetic middle name
	 */
	public String getPhoneticMiddleName() {
		return mPhoneticMiddleName;
	}

	/**
	 * Gets the phonetic full name.
	 *
	 * @return the phonetic full name
	 */
	public String getPhoneticFullName() {
		return mPhoneticFullName;
	}

	/**
	 * Gets the nick name list.
	 *
	 * @return the nick name list
	 */
	public final List<String> getNickNameList() {
		return mNickNameList;
	}

	/**
	 * Gets the birthday.
	 *
	 * @return the birthday
	 */
	public String getBirthday() {
		return mBirthday;
	}

	/**
	 * Gets the notes.
	 *
	 * @return the notes
	 */
	public final List<String> getNotes() {
		return mNoteList;
	}

	/**
	 * Gets the phone list.
	 *
	 * @return the phone list
	 */
	public final List<PhoneData> getPhoneList() {
		return mPhoneList;
	}

	/**
	 * Gets the email list.
	 *
	 * @return the email list
	 */
	public final List<EmailData> getEmailList() {
		return mEmailList;
	}

	/**
	 * Gets the postal list.
	 *
	 * @return the postal list
	 */
	public final List<PostalData> getPostalList() {
		return mPostalList;
	}

	/**
	 * Gets the organization list.
	 *
	 * @return the organization list
	 */
	public final List<OrganizationData> getOrganizationList() {
		return mOrganizationList;
	}

	/**
	 * Gets the im list.
	 *
	 * @return the im list
	 */
	public final List<ImData> getImList() {
		return mImList;
	}

	/**
	 * Gets the photo list.
	 *
	 * @return the photo list
	 */
	public final List<PhotoData> getPhotoList() {
		return mPhotoList;
	}

	/**
	 * Gets the website list.
	 *
	 * @return the website list
	 */
	public final List<String> getWebsiteList() {
		return mWebsiteList;
	}

	/**
	 * Gets the display name.
	 *
	 * @return the display name
	 */
	public String getDisplayName() {
		if (mDisplayName == null) {
			constructDisplayName();
		}
		return mDisplayName;
	}

}
