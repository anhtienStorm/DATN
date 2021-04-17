/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package bkav.android.btalk.backup.vcard;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.content.Entity.NamedContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.util.Log;
import android.util.SparseArray;

// TODO: Auto-generated Javadoc
/**
 * <p>
 * The class for composing VCard from Contacts information. Note that this is
 * completely differnt implementation from
 * android.syncml.pim.vcard.VCardComposer, which is not maintained anymore.
 * </p>
 * 
 * <p>
 * Usually, this class should be used like this.
 * </p>
 * 
 * <pre class="prettyprint">
 * VCardComposer composer = null;
 * try {
 * 	composer = new VCardComposer(context);
 * 	composer.addHandler(composer.new HandlerForOutputStream(outputStream));
 * 	if (!composer.init()) {
 * 		// Do something handling the situation.
 * 		return;
 * 	}
 * 	while (!composer.isAfterLast()) {
 * 		if (mCanceled) {
 * 			// Assume a user may cancel this operation during the export.
 * 			return;
 * 		}
 * 		if (!composer.createOneEntry()) {
 * 			// Do something handling the error situation.
 * 			return;
 * 		}
 * 	}
 * } finally {
 * 	if (composer != null) {
 * 		composer.terminate();
 * 	}
 * }
 * </pre>
 */
public class VCardComposer {

	/** The Constant LOG_TAG. */
	private static final String LOG_TAG = "VCardComposer";

	/** The Constant DEFAULT_PHONE_TYPE. */
	public static final int DEFAULT_PHONE_TYPE = Phone.TYPE_HOME;

	/** The Constant DEFAULT_POSTAL_TYPE. */
	public static final int DEFAULT_POSTAL_TYPE = StructuredPostal.TYPE_HOME;

	/** The Constant DEFAULT_EMAIL_TYPE. */
	public static final int DEFAULT_EMAIL_TYPE = Email.TYPE_OTHER;

	/** The Constant FAILURE_REASON_FAILED_TO_GET_DATABASE_INFO. */
	public static final String FAILURE_REASON_FAILED_TO_GET_DATABASE_INFO = "Failed to get database information";

	/** The Constant FAILURE_REASON_NO_ENTRY. */
	public static final String FAILURE_REASON_NO_ENTRY = "There's no exportable in the database";

	/** The Constant FAILURE_REASON_NOT_INITIALIZED. */
	public static final String FAILURE_REASON_NOT_INITIALIZED = "The vCard composer object is not correctly initialized";

	/**
	 * Should be visible only from developers... (no need to translate,
	 * hopefully)
	 */
	public static final String FAILURE_REASON_UNSUPPORTED_URI = "The Uri vCard composer received is not supported by the composer.";

	/** The Constant NO_ERROR. */
	public static final String NO_ERROR = "No error";

	/** The Constant VCARD_TYPE_STRING_DOCOMO. */
	public static final String VCARD_TYPE_STRING_DOCOMO = "docomo";

	/** The Constant SHIFT_JIS. */
	private static final String SHIFT_JIS = "SHIFT_JIS";

	/** The Constant UTF_8. */
	private static final String UTF_8 = "UTF-8";

	/**
	 * Special URI for testing.
	 */
	public static final String VCARD_TEST_AUTHORITY = "com.android.unit_tests.vcard";

	/** The Constant VCARD_TEST_AUTHORITY_URI. */
	public static final Uri VCARD_TEST_AUTHORITY_URI = Uri.parse("content://"
			+ VCARD_TEST_AUTHORITY);

	/** The Constant CONTACTS_TEST_CONTENT_URI. */
	public static final Uri CONTACTS_TEST_CONTENT_URI = Uri.withAppendedPath(
			VCARD_TEST_AUTHORITY_URI, "contacts");

	/** The Constant sImMap. */
	private static final SparseArray<String> sImMap;

	static {
		sImMap = new SparseArray<String>();
		sImMap.put(Im.PROTOCOL_AIM, VCardConstants.PROPERTY_X_AIM);
		sImMap.put(Im.PROTOCOL_MSN, VCardConstants.PROPERTY_X_MSN);
		sImMap.put(Im.PROTOCOL_YAHOO, VCardConstants.PROPERTY_X_YAHOO);
		sImMap.put(Im.PROTOCOL_ICQ, VCardConstants.PROPERTY_X_ICQ);
		sImMap.put(Im.PROTOCOL_JABBER, VCardConstants.PROPERTY_X_JABBER);
		sImMap.put(Im.PROTOCOL_SKYPE, VCardConstants.PROPERTY_X_SKYPE_USERNAME);
		// Google talk is a special case.
	}

	/**
	 * The Interface OneEntryHandler.
	 */
	public static interface OneEntryHandler {

		/**
		 * On init.
		 *
		 * @param context the context
		 * @return true, if successful
		 */
		public boolean onInit(Context context);

		/**
		 * On entry created.
		 *
		 * @param vcard the vcard
		 * @return true, if successful
		 */
		public boolean onEntryCreated(String vcard);

		/**
		 * On terminate.
		 */
		public void onTerminate();
	}

	/**
	 * <p>
	 * An useful example handler, which emits VCard String to outputstream one
	 * by one.
	 * </p>
	 * <p>
	 * The input OutputStream object is closed() on {@link #onTerminate()}. Must
	 * not close the stream outside.
	 * </p>
	 */
	public class HandlerForOutputStream implements OneEntryHandler {

		/** The Constant LOG_TAG. */
		private static final String LOG_TAG = "vcard.VCardComposer.HandlerForOutputStream";

		/** The m output stream. */
		final private OutputStream mOutputStream; // mWriter will close this.

		/** The m writer. */
		private Writer mWriter;

		/** The m on terminate is called. */
		private boolean mOnTerminateIsCalled = false;

		/**
		 * Input stream will be closed on the detruction of this object.
		 *
		 * @param outputStream the output stream
		 */
		public HandlerForOutputStream(OutputStream outputStream) {
			mOutputStream = outputStream;
		}

		/* (non-Javadoc)
		 * @see bms.backup.VCardComposer.OneEntryHandler#onInit(android.content.Context)
		 */
		public boolean onInit(Context context) {
			try {
				mWriter = new BufferedWriter(new OutputStreamWriter(
						mOutputStream, mCharsetString));
			} catch (UnsupportedEncodingException e1) {
				Log.e(LOG_TAG, "Unsupported charset: " + mCharsetString);
				mErrorReason = "Encoding is not supported (usually this does not happen!): "
						+ mCharsetString;
				return false;
			}

			if (mIsDoCoMo) {
				try {
					// Create one empty entry.
					mWriter.write(createOneEntryInternal("-1", null));
				} catch (VCardException e) {
					Log.e(LOG_TAG,
							"VCardException has been thrown during on Init(): "
									+ e.getMessage());
					return false;
				} catch (IOException e) {
					Log.e(LOG_TAG,
							"IOException occurred during exportOneContactData: "
									+ e.getMessage());
					mErrorReason = "IOException occurred: " + e.getMessage();
					return false;
				}
			}
			return true;
		}

		/* (non-Javadoc)
		 * @see bms.backup.VCardComposer.OneEntryHandler#onEntryCreated(java.lang.String)
		 */
		public boolean onEntryCreated(String vcard) {
			try {
				mWriter.write(vcard);
			} catch (IOException e) {
				Log.e(LOG_TAG,
						"IOException occurred during exportOneContactData: "
								+ e.getMessage());
				mErrorReason = "IOException occurred: " + e.getMessage();
				return false;
			}
			return true;
		}

		/* (non-Javadoc)
		 * @see bms.backup.VCardComposer.OneEntryHandler#onTerminate()
		 */
		public void onTerminate() {
			mOnTerminateIsCalled = true;
			if (mWriter != null) {
				try {
					// Flush and sync the data so that a user is able to pull
					// the SDCard just after
					// the export.
					mWriter.flush();
					if (mOutputStream != null
							&& mOutputStream instanceof FileOutputStream) {
						((FileOutputStream) mOutputStream).getFD().sync();
					}
				} catch (IOException e) {
					Log.d(LOG_TAG,
							"IOException during closing the output stream: "
									+ e.getMessage());
				} finally {
					try {
						mWriter.close();
					} catch (IOException e) {
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#finalize()
		 */
		@Override
		public void finalize() {
			if (!mOnTerminateIsCalled) {
				onTerminate();
			}
		}
	}

	/** The m context. */
	private final Context mContext;

	/** The m v card type. */
	private final int mVCardType;

	/** The m care handler errors. */
	private final boolean mCareHandlerErrors;

	/** The m content resolver. */
	private final ContentResolver mContentResolver;

	/** The m is do co mo. */
	private final boolean mIsDoCoMo;

	/** The m uses shift jis. */
	private final boolean mUsesShiftJis;

	/** The m cursor. */
	private Cursor mCursor;

	/** The m id column. */
	private int mIdColumn;

	/** The m charset string. */
	private final String mCharsetString;

	/** The m terminate is called. */
	private boolean mTerminateIsCalled;

	/** The m handler list. */
	private final List<OneEntryHandler> mHandlerList;

	/** The m error reason. */
	private String mErrorReason = NO_ERROR;

	/** The Constant sContactsProjection. */
	private static final String[] sContactsProjection = new String[] { Contacts._ID, };

	/**
	 * Instantiates a new v card composer.
	 *
	 * @param context the context
	 */
	public VCardComposer(Context context) {
		this(context, VCardConfig.VCARD_TYPE_DEFAULT, true);
	}

	/**
	 * Instantiates a new v card composer.
	 *
	 * @param context the context
	 * @param vcardType the vcard type
	 */
	public VCardComposer(Context context, int vcardType) {
		this(context, vcardType, true);
	}

	/**
	 * Instantiates a new v card composer.
	 *
	 * @param context the context
	 * @param vcardTypeStr the vcard type str
	 * @param careHandlerErrors the care handler errors
	 */
	public VCardComposer(Context context, String vcardTypeStr,
			boolean careHandlerErrors) {
		this(context, VCardConfig.getVCardTypeFromString(vcardTypeStr),
				careHandlerErrors);
	}

	/**
	 * Construct for supporting call log entry vCard composing.
	 *
	 * @param context the context
	 * @param vcardType the vcard type
	 * @param careHandlerErrors the care handler errors
	 */
	public VCardComposer(final Context context, final int vcardType,
			final boolean careHandlerErrors) {
		mContext = context;
		mVCardType = vcardType;
		mCareHandlerErrors = careHandlerErrors;
		mContentResolver = context.getContentResolver();

		mIsDoCoMo = VCardConfig.isDoCoMo(vcardType);
		mUsesShiftJis = VCardConfig.usesShiftJis(vcardType);
		mHandlerList = new ArrayList<OneEntryHandler>();

		if (mIsDoCoMo) {
			String charset;
			try {
				charset = CharsetUtils.charsetForVendor(SHIFT_JIS, "docomo")
						.name();
			} catch (UnsupportedCharsetException e) {
				Log.e(LOG_TAG,
						"DoCoMo-specific SHIFT_JIS was not found. Use SHIFT_JIS as is.");
				charset = SHIFT_JIS;
			}
			mCharsetString = charset;
		} else if (mUsesShiftJis) {
			String charset;
			try {
				charset = CharsetUtils.charsetForVendor(SHIFT_JIS).name();
			} catch (UnsupportedCharsetException e) {
				Log.e(LOG_TAG,
						"Vendor-specific SHIFT_JIS was not found. Use SHIFT_JIS as is.");
				charset = SHIFT_JIS;
			}
			mCharsetString = charset;
		} else {
			mCharsetString = UTF_8;
		}
	}

	/**
	 * Must be called before {@link #init()}.
	 *
	 * @param handler the handler
	 */
	public void addHandler(OneEntryHandler handler) {
		if (handler != null) {
			mHandlerList.add(handler);
		}
	}

	/**
	 * Inits the.
	 *
	 * @return Returns true when initialization is successful and all the other
	 * methods are available. Returns false otherwise.
	 */
	public boolean init() {
		return init(null, null);
	}

	/**
	 * Inits the.
	 *
	 * @param selection the selection
	 * @param selectionArgs the selection args
	 * @return true, if successful
	 */
	public boolean init(final String selection, final String[] selectionArgs) {
		return init(Contacts.CONTENT_URI, selection, selectionArgs, null);
	}

	/**
	 * Note that this is unstable interface, may be deleted in the future.
	 *
	 * @param contentUri the content uri
	 * @param selection the selection
	 * @param selectionArgs the selection args
	 * @param sortOrder the sort order
	 * @return true, if successful
	 */
	public boolean init(final Uri contentUri, final String selection,
			final String[] selectionArgs, final String sortOrder) {
		if (contentUri == null) {
			return false;
		}

		if (mCareHandlerErrors) {
			List<OneEntryHandler> finishedList = new ArrayList<OneEntryHandler>(
					mHandlerList.size());
			for (OneEntryHandler handler : mHandlerList) {
				if (!handler.onInit(mContext)) {
					for (OneEntryHandler finished : finishedList) {
						finished.onTerminate();
					}
					return false;
				}
			}
		} else {
			// Just ignore the false returned from onInit().
			for (OneEntryHandler handler : mHandlerList) {
				handler.onInit(mContext);
			}
		}

		final String[] projection;
		if (Contacts.CONTENT_URI.equals(contentUri)
				|| CONTACTS_TEST_CONTENT_URI.equals(contentUri)) {
			projection = sContactsProjection;
		} else {
			mErrorReason = FAILURE_REASON_UNSUPPORTED_URI;
			return false;
		}
		mCursor = mContentResolver.query(contentUri, projection, selection,
				selectionArgs, sortOrder);

		if (mCursor == null) {
			mErrorReason = FAILURE_REASON_FAILED_TO_GET_DATABASE_INFO;
			return false;
		}

		if (getCount() == 0 || !mCursor.moveToFirst()) {
			try {
				mCursor.close();
			} catch (SQLiteException e) {
				Log.e(LOG_TAG,
						"SQLiteException on Cursor#close(): " + e.getMessage());
			} finally {
				mCursor = null;
				mErrorReason = FAILURE_REASON_NO_ENTRY;
			}
			return false;
		}

		mIdColumn = mCursor.getColumnIndex(Contacts._ID);

		return true;
	}

	/**
	 * Creates the one entry.
	 *
	 * @return true, if successful
	 */
	public boolean createOneEntry(int modeCode) {
		return createOneEntry(null, modeCode);
	}

	/**
	 * Creates the one entry.
	 *
	 * @param getEntityIteratorMethod For Dependency Injection.
	 * @return true, if successful
	 * @hide just for testing.
	 */
	public boolean createOneEntry(Method getEntityIteratorMethod,int modeCode) {
		if (mCursor == null || mCursor.isAfterLast()) {
			mErrorReason = FAILURE_REASON_NOT_INITIALIZED;
			return false;
		}
		String vcard;
		try {
			if (mIdColumn >= 0) {
				vcard = createOneEntryInternal(mCursor.getString(mIdColumn),
						getEntityIteratorMethod);
			} else {
				Log.e(LOG_TAG, "Incorrect mIdColumn: " + mIdColumn);
				return true;
			}
		} catch (VCardException e) {
			Log.e(LOG_TAG, "VCardException has been thrown: " + e.getMessage());
			return false;
		} catch (OutOfMemoryError error) {
			// Maybe some data (e.g. photo) is too big to have in memory. But it
			// should be rare.
			Log.e(LOG_TAG, "OutOfMemoryError occured. Ignore the entry.");
			System.gc();
			return true;
		} finally {
			mCursor.moveToNext();
		}

		// This function does not care the OutOfMemoryError on the handler side
		// :-P
		if (mCareHandlerErrors) {
			new ArrayList<OneEntryHandler>(
					mHandlerList.size());
			for (OneEntryHandler handler : mHandlerList) {
				if (!handler.onEntryCreated(vcard)) {
					return false;
				}
			}
		} else {
			for (OneEntryHandler handler : mHandlerList) {
				handler.onEntryCreated(vcard);
			}
		}

		return true;
	}

	/**
	 * Creates the one entry internal.
	 *
	 * @param contactId the contact id
	 * @param getEntityIteratorMethod the get entity iterator method
	 * @return the string
	 * @throws VCardException the v card exception
	 */
	@TargetApi(8)
	private String createOneEntryInternal(final String contactId,
			Method getEntityIteratorMethod) throws VCardException {
		final Map<String, List<ContentValues>> contentValuesListMap = new HashMap<String, List<ContentValues>>();
		// The resolver may return the entity iterator with no data. It is
		// possible.
		// e.g. If all the data in the contact of the given contact id are not
		// exportable ones,
		// they are hidden from the view of this method, though contact id
		// itself exists.
		EntityIterator entityIterator = null;
		try {
			final Uri uri = RawContactsEntity.CONTENT_URI.buildUpon().build();
			final String selection = Data.CONTACT_ID + "=?";
			final String[] selectionArgs = new String[] { contactId };
			if (getEntityIteratorMethod != null) {
				// Please note that this branch is executed by some tests only
				try {
					entityIterator = (EntityIterator) getEntityIteratorMethod
							.invoke(null, mContentResolver, uri, selection,
									selectionArgs, null);
				} catch (IllegalArgumentException e) {
					Log.e(LOG_TAG, "IllegalArgumentException has been thrown: "
							+ e.getMessage());
				} catch (IllegalAccessException e) {
					Log.e(LOG_TAG, "IllegalAccessException has been thrown: "
							+ e.getMessage());
				} catch (InvocationTargetException e) {
					Log.e(LOG_TAG,
							"InvocationTargetException has been thrown: ");
					StackTraceElement[] stackTraceElements = e.getCause()
							.getStackTrace();
					for (StackTraceElement element : stackTraceElements) {
						Log.e(LOG_TAG, "    at " + element.toString());
					}
					throw new VCardException(
							"InvocationTargetException has been thrown: "
									+ e.getCause().getMessage());
				}
			} else {
				// entityIterator =
				// RawContacts.newEntityIterator(mContentResolver
				// .query(uri, null, selection, selectionArgs, null));
				entityIterator = newEntityIterator(mContentResolver.query(uri,
						null, selection, selectionArgs, null));
			}

			if (entityIterator == null) {
				Log.e(LOG_TAG, "EntityIterator is null");
				return "";
			}

			if (!entityIterator.hasNext()) {
				Log.w(LOG_TAG, "Data does not exist. contactId: " + contactId);
				return "";
			}

			while (entityIterator.hasNext()) {
				Entity entity = entityIterator.next();
				for (NamedContentValues namedContentValues : entity
						.getSubValues()) {
					ContentValues contentValues = namedContentValues.values;
					String key = contentValues.getAsString(Data.MIMETYPE);
					if (key != null) {
						List<ContentValues> contentValuesList = contentValuesListMap
								.get(key);
						if (contentValuesList == null) {
							contentValuesList = new ArrayList<ContentValues>();
							contentValuesListMap.put(key, contentValuesList);
						}
						contentValuesList.add(contentValues);
					}
				}
			}
		} finally {
			if (entityIterator != null) {
				entityIterator.close();
			}
		}

		final VCardBuilder builder = new VCardBuilder(mVCardType);
		builder.appendNameProperties(
				contentValuesListMap.get(StructuredName.CONTENT_ITEM_TYPE))
				.appendNickNames(
						contentValuesListMap.get(Nickname.CONTENT_ITEM_TYPE))
						.appendPhones(contentValuesListMap.get(Phone.CONTENT_ITEM_TYPE))
						.appendEmails(contentValuesListMap.get(Email.CONTENT_ITEM_TYPE))
						.appendPostals(
								contentValuesListMap
								.get(StructuredPostal.CONTENT_ITEM_TYPE))
								.appendOrganizations(
										contentValuesListMap
										.get(Organization.CONTENT_ITEM_TYPE))
										.appendWebsites(
												contentValuesListMap.get(Website.CONTENT_ITEM_TYPE));
		builder.appendNotes(contentValuesListMap.get(Note.CONTENT_ITEM_TYPE))
		.appendEvents(contentValuesListMap.get(Event.CONTENT_ITEM_TYPE))
		.appendIms(contentValuesListMap.get(Im.CONTENT_ITEM_TYPE))
		.appendRelation(
				contentValuesListMap.get(Relation.CONTENT_ITEM_TYPE));
		return builder.toString();
	}

	/**
	 * Terminate.
	 */
	public void terminate() {
		for (OneEntryHandler handler : mHandlerList) {
			handler.onTerminate();
		}

		if (mCursor != null) {
			try {
				mCursor.close();
			} catch (SQLiteException e) {
				Log.e(LOG_TAG,
						"SQLiteException on Cursor#close(): " + e.getMessage());
			}
			mCursor = null;
		}

		mTerminateIsCalled = true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() {
		if (!mTerminateIsCalled) {
			terminate();
		}
	}

	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	public int getCount() {
		if (mCursor == null) {
			return 0;
		}
		return mCursor.getCount();
	}

	/**
	 * Checks if is after last.
	 *
	 * @return true, if is after last
	 */
	public boolean isAfterLast() {
		if (mCursor == null) {
			return false;
		}
		return mCursor.isAfterLast();
	}

	/**
	 * Gets the error reason.
	 *
	 * @return Return the error reason if possible.
	 */
	public String getErrorReason() {
		return mErrorReason;
	}

	/**
	 * New entity iterator.
	 *
	 * @param cursor the cursor
	 * @return the entity iterator
	 */
	private EntityIterator newEntityIterator(Cursor cursor) {
		return new EntityIteratorImpl(cursor);
	}

	/**
	 * The Class EntityIteratorImpl.
	 */
	private static class EntityIteratorImpl extends CursorEntityIterator {

		/** The Constant DATA_KEYS. */
		private static final String[] DATA_KEYS = new String[] { Data.DATA1,
			Data.DATA2, Data.DATA3, Data.DATA4, Data.DATA5, Data.DATA6,
			Data.DATA7, Data.DATA8, Data.DATA9, Data.DATA10, Data.DATA11,
			Data.DATA12, Data.DATA13, Data.DATA14, Data.DATA15, Data.SYNC1,
			Data.SYNC2, Data.SYNC3, Data.SYNC4 };

		/** The Constant DIRTY. */
		private static final String DIRTY = "dirty";

		/** The Constant DELETED. */
		private static final String DELETED = "deleted";

		/** The Constant CONTACT_ID. */
		private static final String CONTACT_ID = "contact_id";

		/** The Constant STARRED. */
		private static final String STARRED = "starred";

		/** The Constant _ID. */
		private static final String _ID = "_id";

		/** The Constant VERSION. */
		private static final String VERSION = "version";

		/** The Constant ACCOUNT_NAME. */
		private static final String ACCOUNT_NAME = "account_name";

		/** The Constant ACCOUNT_TYPE. */
		private static final String ACCOUNT_TYPE = "account_type";

		/** The Constant SOURCE_ID. */
		private static final String SOURCE_ID = "sourceid";

		/** The Constant SYNC1. */
		private static final String SYNC1 = "sync1";

		/** The Constant SYNC2. */
		private static final String SYNC2 = "sync2";

		/** The Constant SYNC3. */
		private static final String SYNC3 = "sync3";

		/** The Constant SYNC4. */
		private static final String SYNC4 = "sync4";

		/** The Constant RES_PACKAGE. */
		private static final String RES_PACKAGE = "res_package";

		/**
		 * Instantiates a new entity iterator impl.
		 *
		 * @param cursor the cursor
		 */
		public EntityIteratorImpl(Cursor cursor) {
			super(cursor);
		}

		/* (non-Javadoc)
		 * @see bms.backup.CursorEntityIterator#getEntityAndIncrementCursor(android.database.Cursor)
		 */
		@TargetApi(8)
		@Override
		public Entity getEntityAndIncrementCursor(Cursor cursor)
				throws RemoteException {
			final int columnRawContactId = cursor
					.getColumnIndexOrThrow(RawContacts._ID);
			final long rawContactId = cursor.getLong(columnRawContactId);

			// we expect the cursor is already at the row we need to read from
			ContentValues cv = new ContentValues();
			cursorStringToContentValuesIfPresent(cursor, cv, ACCOUNT_NAME);
			cursorStringToContentValuesIfPresent(cursor, cv, ACCOUNT_TYPE);
			cursorLongToContentValuesIfPresent(cursor, cv, _ID);
			cursorLongToContentValuesIfPresent(cursor, cv, DIRTY);
			cursorLongToContentValuesIfPresent(cursor, cv, VERSION);
			cursorStringToContentValuesIfPresent(cursor, cv, SOURCE_ID);
			cursorStringToContentValuesIfPresent(cursor, cv, SYNC1);
			cursorStringToContentValuesIfPresent(cursor, cv, SYNC2);
			cursorStringToContentValuesIfPresent(cursor, cv, SYNC3);
			cursorStringToContentValuesIfPresent(cursor, cv, SYNC4);
			cursorLongToContentValuesIfPresent(cursor, cv, DELETED);
			cursorLongToContentValuesIfPresent(cursor, cv, CONTACT_ID);
			cursorLongToContentValuesIfPresent(cursor, cv, STARRED);
			Entity contact = new Entity(cv);

			// read data rows until the contact id changes
			do {
				if (rawContactId != cursor.getLong(columnRawContactId)) {
					break;
				}
				// add the data to to the contact
				cv = new ContentValues();
				cv.put(Data._ID,
						cursor.getLong(cursor
								.getColumnIndexOrThrow(RawContacts.Entity.DATA_ID)));
				cursorStringToContentValuesIfPresent(cursor, cv, RES_PACKAGE);
				cursorStringToContentValuesIfPresent(cursor, cv, Data.MIMETYPE);
				cursorLongToContentValuesIfPresent(cursor, cv, Data.IS_PRIMARY);
				cursorLongToContentValuesIfPresent(cursor, cv,
						Data.IS_SUPER_PRIMARY);
				cursorLongToContentValuesIfPresent(cursor, cv,
						Data.DATA_VERSION);
				cursorStringToContentValuesIfPresent(cursor, cv,
						CommonDataKinds.GroupMembership.GROUP_SOURCE_ID);
				cursorStringToContentValuesIfPresent(cursor, cv,
						Data.DATA_VERSION);
				for (String key : DATA_KEYS) {
					final int columnIndex = cursor.getColumnIndexOrThrow(key);
					if (cursor.isNull(columnIndex)) {
						// don't put anything
					} else {
						try {
							cv.put(key, cursor.getString(columnIndex));
						} catch (SQLiteException e) {
							cv.put(key, cursor.getBlob(columnIndex));
						}
					}
				}
				contact.addSubValue(Data.CONTENT_URI, cv);
			} while (cursor.moveToNext());

			return contact;
		}

		/**
		 * Cursor long to content values if present.
		 *
		 * @param cursor the cursor
		 * @param values the values
		 * @param column the column
		 */
		private void cursorLongToContentValuesIfPresent(Cursor cursor,
				ContentValues values, String column) {
			final int index = cursor.getColumnIndexOrThrow(column);
			if (!cursor.isNull(index)) {
				values.put(column, cursor.getLong(index));
			}
		}

		/**
		 * Cursor string to content values if present.
		 *
		 * @param cursor the cursor
		 * @param values the values
		 * @param column the column
		 */
		public void cursorStringToContentValuesIfPresent(Cursor cursor,
				ContentValues values, String column) {
			final int index = cursor.getColumnIndexOrThrow(column);
			if (!cursor.isNull(index)) {
				values.put(column, cursor.getString(index));
			}
		}
	}
}
