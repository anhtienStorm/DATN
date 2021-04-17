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
import android.util.Log;

import android.util.Base64;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class VCardEntryConstructor.
 */
public class VCardEntryConstructor implements VCardInterpreter {

	/** The log tag. */
	private static String LOG_TAG = "VCardEntryConstructor";

	/**
	 * If there's no other information available, this class uses this charset for encoding
	 * byte arrays to String.
	 */
	/* package */ static final String DEFAULT_CHARSET_FOR_DECODED_BYTES = "UTF-8";

	/** The m current property. */
	private VCardEntry.Property mCurrentProperty = new VCardEntry.Property();

	/** The m current contact struct. */
	private VCardEntry mCurrentContactStruct;

	/** The m param type. */
	private String mParamType;

	/**
	 * The charset using which {@link VCardInterpreter} parses the text.
	 */
	private String mInputCharset;

	/**
	 * The charset with which byte array is encoded to String.
	 */
	final private String mCharsetForDecodedBytes;

	/** The m strict line break parsing. */
	final private boolean mStrictLineBreakParsing;

	/** The m v card type. */
	final private int mVCardType;

	/** The m account. */
	final private Account mAccount;

	/** For measuring performance. */
	private long mTimePushIntoContentResolver;

	/** The m entry handlers. */
	final private List<VCardEntryHandler> mEntryHandlers = new ArrayList<VCardEntryHandler>();

	/**
	 * Instantiates a new v card entry constructor.
	 */
	public VCardEntryConstructor() {
		this(null, null, false, VCardConfig.VCARD_TYPE_V21_GENERIC_UTF8, null);
	}

	/**
	 * Instantiates a new v card entry constructor.
	 *
	 * @param vcardType the vcard type
	 */
	public VCardEntryConstructor(final int vcardType) {
		this(null, null, false, vcardType, null);
	}

	/**
	 * Instantiates a new v card entry constructor.
	 *
	 * @param charset the charset
	 * @param strictLineBreakParsing the strict line break parsing
	 * @param vcardType the vcard type
	 * @param account the account
	 */
	public VCardEntryConstructor(final String charset, final boolean strictLineBreakParsing,
			final int vcardType, final Account account) {
		this(null, charset, strictLineBreakParsing, vcardType, account);
	}

	/**
	 * Instantiates a new v card entry constructor.
	 *
	 * @param inputCharset the input charset
	 * @param charsetForDetodedBytes the charset for detoded bytes
	 * @param strictLineBreakParsing the strict line break parsing
	 * @param vcardType the vcard type
	 * @param account the account
	 */
	public VCardEntryConstructor(final String inputCharset, final String charsetForDetodedBytes,
			final boolean strictLineBreakParsing, final int vcardType,
			final Account account) {
		if (inputCharset != null) {
			mInputCharset = inputCharset;
		} else {
			mInputCharset = VCardConfig.DEFAULT_CHARSET;
		}
		if (charsetForDetodedBytes != null) {
			mCharsetForDecodedBytes = charsetForDetodedBytes;
		} else {
			mCharsetForDecodedBytes = DEFAULT_CHARSET_FOR_DECODED_BYTES;
		}
		mStrictLineBreakParsing = strictLineBreakParsing;
		mVCardType = vcardType;
		mAccount = account;
		
	}

	/**
	 * Adds the entry handler.
	 *
	 * @param entryHandler the entry handler
	 */
	public void addEntryHandler(VCardEntryHandler entryHandler) {
		mEntryHandlers.add(entryHandler);
	}

	/* (non-Javadoc)
	 * @see bms.backup.VCardInterpreter#start()
	 */
	public void start() {
		for (VCardEntryHandler entryHandler : mEntryHandlers) {
			entryHandler.onStart();
		}
	}

	/* (non-Javadoc)
	 * @see bms.backup.VCardInterpreter#end()
	 */
	public void end() {
		for (VCardEntryHandler entryHandler : mEntryHandlers) {
			entryHandler.onEnd();
		}
	}

	/**
	 * Called when the parse failed between {@link #startEntry()} and {@link #endEntry()}.
	 */
	public void clear() {
		mCurrentContactStruct = null;
		mCurrentProperty = new VCardEntry.Property();
	}

	/**
	 * Assume that VCard is not nested. In other words, this code does not accept 
	 */
	public void startEntry() {
		if (mCurrentContactStruct != null) {
			Log.e(LOG_TAG, "Nested VCard code is not supported now.");
		}
		mCurrentContactStruct = new VCardEntry(mVCardType, mAccount);
	}

	/* (non-Javadoc)
	 * @see bms.backup.VCardInterpreter#endEntry()
	 */
	public void endEntry() {
		mCurrentContactStruct.consolidateFields();
	
		for (VCardEntryHandler entryHandler : mEntryHandlers) {
			entryHandler.onEntryCreated(mCurrentContactStruct);
		}
		mCurrentContactStruct = null;
	}

	/* (non-Javadoc)
	 * @see bms.backup.VCardInterpreter#startProperty()
	 */
	public void startProperty() {
		mCurrentProperty.clear();
	}

	/* (non-Javadoc)
	 * @see bms.backup.VCardInterpreter#endProperty()
	 */
	public void endProperty() {
		mCurrentContactStruct.addProperty(mCurrentProperty);
	}

	/* (non-Javadoc)
	 * @see bms.backup.VCardInterpreter#propertyName(java.lang.String)
	 */
	public void propertyName(String name) {
		mCurrentProperty.setPropertyName(name);
	}

	/* (non-Javadoc)
	 * @see bms.backup.VCardInterpreter#propertyGroup(java.lang.String)
	 */
	public void propertyGroup(String group) {
	}

	/* (non-Javadoc)
	 * @see bms.backup.VCardInterpreter#propertyParamType(java.lang.String)
	 */
	public void propertyParamType(String type) {
		if (mParamType != null) {
			Log.e(LOG_TAG, "propertyParamType() is called more than once " +
					"before propertyParamValue() is called");
		}
		mParamType = type;
	}

	/* (non-Javadoc)
	 * @see bms.backup.VCardInterpreter#propertyParamValue(java.lang.String)
	 */
	public void propertyParamValue(String value) {
		if (mParamType == null) {
			// From vCard 2.1 specification. vCard 3.0 formally does not allow this case.
			mParamType = "TYPE";
		}
		mCurrentProperty.addParameter(mParamType, value);
		mParamType = null;
	}

	/**
	 * Encode string.
	 *
	 * @param originalString the original string
	 * @param charsetForDecodedBytes the charset for decoded bytes
	 * @return the string
	 */
	private String encodeString(String originalString, String charsetForDecodedBytes) {
		if (mInputCharset.equalsIgnoreCase(charsetForDecodedBytes)) {
			return originalString;
		}
		Charset charset = Charset.forName(mInputCharset);
		ByteBuffer byteBuffer = charset.encode(originalString);
		// byteBuffer.array() "may" return byte array which is larger than
		// byteBuffer.remaining(). Here, we keep on the safe side.
		byte[] bytes = new byte[byteBuffer.remaining()];
		byteBuffer.get(bytes);
		try {
			return new String(bytes, charsetForDecodedBytes);
		} catch (UnsupportedEncodingException e) {
			Log.e(LOG_TAG, "Failed to encode: charset=" + charsetForDecodedBytes);
			return null;
		}
	}

	/**
	 * Handle one value.
	 *
	 * @param value the value
	 * @param charsetForDecodedBytes the charset for decoded bytes
	 * @param encoding the encoding
	 * @return the string
	 */
	private String handleOneValue(String value, String charsetForDecodedBytes, String encoding) {
		if (encoding != null) {
			if (encoding.equals("BASE64") || encoding.equals("B")) {
				mCurrentProperty.setPropertyBytes(Base64.decode(value.getBytes(),Base64.DEFAULT));
				return value;
			} else if (encoding.equals("QUOTED-PRINTABLE")) {
				// "= " -> " ", "=\t" -> "\t".
				// Previous code had done this replacement. Keep on the safe side.
				StringBuilder builder = new StringBuilder();
				int length = value.length();
				for (int i = 0; i < length; i++) {
					char ch = value.charAt(i);
					if (ch == '=' && i < length - 1) {
						char nextCh = value.charAt(i + 1);
						if (nextCh == ' ' || nextCh == '\t') {

							builder.append(nextCh);
							i++;
							continue;
						}
					}
					builder.append(ch);
				}
				String quotedPrintable = builder.toString();

				String[] lines;
				if (mStrictLineBreakParsing) {
					lines = quotedPrintable.split("\r\n");
				} else {
					builder = new StringBuilder();
					length = quotedPrintable.length();
					ArrayList<String> list = new ArrayList<String>();
					for (int i = 0; i < length; i++) {
						char ch = quotedPrintable.charAt(i);
						if (ch == '\n') {
							list.add(builder.toString());
							builder = new StringBuilder();
						} else if (ch == '\r') {
							list.add(builder.toString());
							builder = new StringBuilder();
							if (i < length - 1) {
								char nextCh = quotedPrintable.charAt(i + 1);
								if (nextCh == '\n') {
									i++;
								}
							}
						} else {
							builder.append(ch);
						}
					}
					String finalLine = builder.toString();
					if (finalLine.length() > 0) {
						list.add(finalLine);
					}
					lines = list.toArray(new String[0]);
				}

				builder = new StringBuilder();
				for (String line : lines) {
					if (line.endsWith("=")) {
						line = line.substring(0, line.length() - 1);
					}
					builder.append(line);
				}
				byte[] bytes;
				try {
					bytes = builder.toString().getBytes(mInputCharset);
				} catch (UnsupportedEncodingException e1) {
					Log.e(LOG_TAG, "Failed to encode: charset=" + mInputCharset);
					bytes = builder.toString().getBytes();
				}

				try {
					bytes = QuotedPrintableCodec.decodeQuotedPrintable(bytes);
				} catch (DecoderException e) {
					Log.e(LOG_TAG, "Failed to decode quoted-printable: " + e);
					return "";
				}

				try {
					return new String(bytes, charsetForDecodedBytes);
				} catch (UnsupportedEncodingException e) {
					Log.e(LOG_TAG, "Failed to encode: charset=" + charsetForDecodedBytes);
					return new String(bytes);
				}
			}
			// Unknown encoding. Fall back to default.
		}
		return encodeString(value, charsetForDecodedBytes);
	}

	/* (non-Javadoc)
	 * @see bms.backup.VCardInterpreter#propertyValues(java.util.List)
	 */
	public void propertyValues(List<String> values) {
		if (values == null || values.isEmpty()) {
			return;
		}

		final Collection<String> charsetCollection = mCurrentProperty.getParameters("CHARSET");
		final String charset =
				((charsetCollection != null) ? charsetCollection.iterator().next() : null);
		final Collection<String> encodingCollection = mCurrentProperty.getParameters("ENCODING");
		final String encoding =
				((encodingCollection != null) ? encodingCollection.iterator().next() : null);

		String charsetForDecodedBytes = CharsetUtils.nameForDefaultVendor(charset);
		if (charsetForDecodedBytes == null || charsetForDecodedBytes.length() == 0) {
			charsetForDecodedBytes = mCharsetForDecodedBytes;
		}

		for (final String value : values) {
			mCurrentProperty.addToPropertyValueList(
					handleOneValue(value, charsetForDecodedBytes, encoding));
		}
	}

	/**
	 * Show performance info.
	 */
	public void showPerformanceInfo() {
		Log.d(LOG_TAG, "time for insertToMMS ContactStruct to database: " +
				mTimePushIntoContentResolver + " ms");
	}
}
