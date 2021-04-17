/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.dialer.calllog;

import android.database.Cursor;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.Time;

import com.android.contacts.common.compat.CompatUtils;
import com.android.contacts.common.util.DateUtils;
import com.android.contacts.common.util.PhoneNumberHelper;
import com.android.dialer.util.AppCompatConstants;
import com.android.dialer.util.DialerUtils;
import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * Anhdts
 * Groups together calls in the call log.  The primary grouping attempts to group together calls
 * to and from the same number into a single row on the call log.
 * A secondary grouping assigns calls, grouped via the primary grouping, to "day groups".  The day
 * groups provide a means of identifying the calls which occurred "Today", "Yesterday", "Last week",
 * or "Other".
 * <p>
 * This class is meant to be used in conjunction with {@link GroupingListAdapter}.
 */
public class CallLogGroupBuilder {

    public interface GroupCreator {

        /**
         * Defines the interface for adding a group to the call log.
         * The primary group for a call log groups the calls together based on the number which was
         * dialed.
         *
         * @param cursorPosition The starting position of the group in the cursor.
         */
        public void addGroup(int cursorPosition, int size);

        /**
         * Defines the interface for tracking the day group each call belongs to.  Calls in a call
         * group are assigned the same day group as the first call in the group.  The day group
         * assigns calls to the buckets: Today, Yesterday, Last week, and Other
         *
         * @param rowId    The row Id of the current call.
         * @param dayGroup The day group the call belongs in.
         */
        public void setDayGroup(long rowId, int dayGroup);

        /**
         * Defines the interface for clearing the day groupings information on rebind/regroup.
         */
        public void clearDayGroups();
    }

    /**
     * Day grouping for call log entries used to represent no associated day group.  Used primarily
     * when retrieving the previous day group, but there is no previous day group (i.e. we are at
     * the start of the list).
     */
    public static final int DAY_GROUP_NONE = -1;

    /**
     * Day grouping for calls which occurred today.
     */
    public static final int DAY_GROUP_TODAY = 0;

    /**
     * Day grouping for calls which occurred yesterday.
     */
    public static final int DAY_GROUP_YESTERDAY = 1;

    /**
     * Day grouping for calls which occurred before last week.
     */
    public static final int DAY_GROUP_OTHER = 2;

    /**
     * Instance of the mTimeQuery object used for mTimeQuery calculations.
     */
    private static final Time TIME = new Time();

    /**
     * The object on which the groups are created.
     */
    private final GroupCreator mGroupCreator;

    public CallLogGroupBuilder(GroupCreator groupCreator) {
        mGroupCreator = groupCreator;
    }

    private ArrayList<Long> mListFilter;

    private static final long TIME_A_HOUR = 3600000;

    /**
     * <p>
     * For entries that are not grouped with others, we do not need to create a group of size one.
     * <p>
     * It assumes that the cursor will not change during its execution.
     *
     * @see GroupingListAdapter#addGroups(Cursor)
     */
    public void addGroups(Cursor cursor) {

        final int count = cursor.getCount();
        if (count == 0) {
            return;
        }
        if (mListFilter == null) {
            mListFilter = new ArrayList<>();
        } else {
            mListFilter.clear();
        }

        // Clear any previous day grouping information.
        mGroupCreator.clearDayGroups();

        // Get current system mTimeQuery, used for calculating which day group calls belong to.
        long currentTime = System.currentTimeMillis();
        cursor.moveToFirst();

        // Determine the day group for the first call in the cursor.
        final long firstDate = cursor.getLong(CallLogQuery.DATE);
        final long firstRowId = cursor.getLong(CallLogQuery.ID);
        int groupDayGroup = getDayGroup(firstDate, currentTime);
        mGroupCreator.setDayGroup(firstRowId, groupDayGroup);

        // Instantiate the group values to those of the first call in the cursor.
        String groupNumber = cursor.getString(CallLogQuery.NUMBER);
        String groupPostDialDigits = CompatUtils.isNCompatible()
                ? cursor.getString(CallLogQuery.POST_DIAL_DIGITS) : "";
        String groupViaNumbers = CompatUtils.isNCompatible()
                ? cursor.getString(CallLogQuery.VIA_NUMBER) : "";
        int groupCallType = cursor.getInt(CallLogQuery.CALL_TYPE);
        boolean groupIsVideoCall = (cursor.getInt(CallLogQuery.FEATURES) & CallLog.Calls.FEATURES_VIDEO) == CallLog.Calls.FEATURES_VIDEO;
        String groupAccountComponentName = cursor.getString(CallLogQuery.ACCOUNT_COMPONENT_NAME);
        String groupAccountId = cursor.getString(CallLogQuery.ACCOUNT_ID);
        boolean isGroupConfCallLog = DialerUtils.isConferenceURICallLog(groupNumber,
                groupPostDialDigits);
        int groupSize = 1;

        String number;
        String numberPostDialDigits;
        String numberViaNumbers;
        int callType;
        String accountComponentName;
        String accountId;
        boolean isVideoCall;
        boolean isNumberConfCallLog = false;

        // Anhdts init mGroupTime
        mGroupTime = firstDate;

        int changePosition = -1;
        int posStart = 0;
        mListFilter.add(cursor.getLong(CallLogQuery.ID));

        while (cursor.moveToNext()) {
            boolean isLast = false;
            if (mListFilter.contains(cursor.getLong(CallLogQuery.ID))) {
                // Neu phan tu cuoi thi check lai
                if (cursor.isLast()) {
                    isLast = true;
                } else {
                    continue;
                }
            }
            if (!isLast) {
                // Obtain the values for the current call to group.
                number = cursor.getString(CallLogQuery.NUMBER);
                numberPostDialDigits = CompatUtils.isNCompatible()
                        ? cursor.getString(CallLogQuery.POST_DIAL_DIGITS) : "";
                numberViaNumbers = CompatUtils.isNCompatible()
                        ? cursor.getString(CallLogQuery.VIA_NUMBER) : "";
                callType = cursor.getInt(CallLogQuery.CALL_TYPE);
                accountComponentName = cursor.getString(CallLogQuery.ACCOUNT_COMPONENT_NAME);
                accountId = cursor.getString(CallLogQuery.ACCOUNT_ID);
                isNumberConfCallLog = DialerUtils.isConferenceURICallLog(number, numberPostDialDigits);
                isVideoCall = (cursor.getInt(CallLogQuery.FEATURES) & CallLog.Calls.FEATURES_VIDEO) == CallLog.Calls.FEATURES_VIDEO;

                final boolean isSameNumber = equalNumbers(groupNumber, isGroupConfCallLog,
                        number, isNumberConfCallLog);
                final boolean isSamePostDialDigits = groupPostDialDigits.equals(numberPostDialDigits);
                final boolean isSameViaNumbers = groupViaNumbers.equals(numberViaNumbers);
                final boolean isSameAccount = isSameAccount(
                        groupAccountComponentName, accountComponentName, groupAccountId, accountId);

                //Anhdts check cung ngay
                final boolean isSameDay = isSameDay(cursor);
                // Group with the same number and account. Never group voicemails. Only group blocked
                // calls with other blocked calls.
                // Cac cuoc goi gan nhau hoạc trong cung mot ngay nhung khong phai gan day
                // Bkav HuyNQN o giao dien recoder thi khong gop call log
                if (!mIsRecoder && isSameNumber && isSameAccount && isSamePostDialDigits && isSameViaNumbers
                        && areBothNotVoicemail(callType, groupCallType)
                        && (areBothNotBlocked(callType, groupCallType)
                        || areBothBlocked(callType, groupCallType))
                        && callType == groupCallType /**Anhdts tach nhom */
                        && isVideoCall == groupIsVideoCall /** check la cuoc goi video*/
                        && (isSameDay /**check cung ngay*/
                        || ((System.currentTimeMillis() - mTimeQuery < TIME_A_HOUR)))) {
                    // Increment the size of the group to include the current call, but do not create
                    // the group until finding a call that does not match.
//                listGroupId.add(cursor.getPosition());
                    groupSize++;
                    mListFilter.add(cursor.getLong(CallLogQuery.ID));
                } else {
                    // Neu khong cung ngay hoac la cac cuoc goi gan day va co cuoc goi chen o giua
                    if (!isSameDay/* || ((System.currentTimeMillis() - mTimeQuery < TIME_A_HOUR) && changePosition != -1)*/) {
                        // Create a group for the previous group of calls, which does not include the
                        // current call.

                        mGroupCreator.addGroup(posStart, groupSize);

                        if (changePosition != -1) {
                            cursor.moveToPosition(changePosition);
                            posStart = changePosition;
                        } else {
                            posStart = cursor.getPosition();
                        }
                        mTimeQuery = cursor.getLong(CallLogQuery.DATE);
                        // Anhdts set mTimeQuery cho group
                        mGroupTime = mTimeQuery;

                        // Start a new group; it will include at least the current call.
                        groupSize = 1;

                        // Update the group values to those of the current call.
                        groupNumber = cursor.getString(CallLogQuery.NUMBER);
                        groupPostDialDigits = CompatUtils.isNCompatible()
                                ? cursor.getString(CallLogQuery.POST_DIAL_DIGITS) : "";
                        groupViaNumbers = CompatUtils.isNCompatible()
                                ? cursor.getString(CallLogQuery.VIA_NUMBER) : "";
                        groupCallType = cursor.getInt(CallLogQuery.CALL_TYPE);
                        groupIsVideoCall = (cursor.getInt(CallLogQuery.FEATURES) & CallLog.Calls.FEATURES_VIDEO) == CallLog.Calls.FEATURES_VIDEO;
                        groupAccountComponentName = cursor.getString(CallLogQuery.ACCOUNT_COMPONENT_NAME);
                        groupAccountId = cursor.getString(CallLogQuery.ACCOUNT_ID);
                        isGroupConfCallLog = DialerUtils.isConferenceURICallLog(groupNumber, groupPostDialDigits);

                        changePosition = -1;
                        mListFilter.add(cursor.getLong(CallLogQuery.ID));

                        // Save the day group associated with the current call.
                        // The call group has changed. Determine the day group for the new call group.
                        final long date = cursor.getLong(CallLogQuery.DATE);
                        groupDayGroup = getDayGroup(date, currentTime);
                        final long currentCallId = cursor.getLong(CallLogQuery.ID);
                        mGroupCreator.setDayGroup(currentCallId, groupDayGroup);
                        continue;
                    } else {
                        // Luu gia tri dau tien thay doi
                        if (changePosition == -1) {
                            changePosition = cursor.getPosition();
                        }
                    }
                }
            }

            if (cursor.isLast()) {
                mGroupCreator.addGroup(posStart, groupSize);

                // Neu cai thay doi khong phai phan tu cuoi thi chay tiep; neu khong them vao group va return
                if (changePosition != -1 && cursor.getPosition() != changePosition) {
                    cursor.moveToPosition(changePosition);
                    posStart = changePosition;
                } else if (!mListFilter.contains(cursor.getLong(CallLogQuery.ID))) {
                    mListFilter.add(cursor.getLong(CallLogQuery.ID));
                    mGroupCreator.addGroup(cursor.getPosition(), 1);
                    break;
                }
                mTimeQuery = cursor.getLong(CallLogQuery.DATE);
                // Anhdts set mTimeQuery cho group
                mGroupTime = mTimeQuery;

                // Start a new group; it will include at least the current call.
                groupSize = 1;

                // Update the group values to those of the current call.
                groupNumber = cursor.getString(CallLogQuery.NUMBER);
                groupPostDialDigits = CompatUtils.isNCompatible()
                        ? cursor.getString(CallLogQuery.POST_DIAL_DIGITS) : "";
                groupViaNumbers = CompatUtils.isNCompatible()
                        ? cursor.getString(CallLogQuery.VIA_NUMBER) : "";
                groupCallType = cursor.getInt(CallLogQuery.CALL_TYPE);
                groupIsVideoCall = (cursor.getInt(CallLogQuery.FEATURES) & CallLog.Calls.FEATURES_VIDEO) == CallLog.Calls.FEATURES_VIDEO;

                groupAccountComponentName = cursor.getString(CallLogQuery.ACCOUNT_COMPONENT_NAME);
                groupAccountId = cursor.getString(CallLogQuery.ACCOUNT_ID);
                isGroupConfCallLog = DialerUtils.isConferenceURICallLog(groupNumber, groupPostDialDigits);

                changePosition = -1;
                mListFilter.add(cursor.getLong(CallLogQuery.ID));

                // Save the day group associated with the current call.
                // The call group has changed. Determine the day group for the new call group.
                final long date = cursor.getLong(CallLogQuery.DATE);
                groupDayGroup = getDayGroup(date, currentTime);
                final long currentCallId = cursor.getLong(CallLogQuery.ID);
                mGroupCreator.setDayGroup(currentCallId, groupDayGroup);
            }

        }
        if (cursor.getCount() == 1) {
            mGroupCreator.addGroup(posStart, groupSize);
        }
    }

    /**
     * Group cursor entries by date, with only one entry per group. This is used for listing
     * voicemails in the archive tab.
     */
    public void addVoicemailGroups(Cursor cursor) {
        if (cursor.getCount() == 0) {
            return;
        }

        // Clear any previous day grouping information.
        mGroupCreator.clearDayGroups();

        // Get current system mTimeQuery, used for calculating which day group calls belong to.
        long currentTime = System.currentTimeMillis();

        // Reset cursor to start before the first row
        cursor.moveToPosition(-1);

        // Create an individual group for each voicemail
        while (cursor.moveToNext()) {
            mGroupCreator.addGroup(cursor.getPosition(), 1);
            mGroupCreator.setDayGroup(cursor.getLong(CallLogQuery.ID),
                    getDayGroup(cursor.getLong(CallLogQuery.DATE), currentTime));

        }
    }

    @VisibleForTesting
    boolean equalNumbers(String number1, String number2) {
        return equalNumbers(number1, false, number2, false);
    }

    boolean equalNumbers(String number1, boolean isConf1, String number2, boolean isConf2) {
        if (PhoneNumberHelper.isUriNumber(number1) || PhoneNumberHelper.isUriNumber(number2)) {
            return compareSipAddresses(number1, number2);
        } else if (isConf1 && isConf2) {
            Pattern pattern = Pattern.compile("[,;]");
            String[] num1 = pattern.split(number1);
            String[] num2 = pattern.split(number2);
            if (num1 == null || num2 == null || num1.length != num2.length) {
                return false;
            }
            for (int i = 0; i < num1.length; i++) {
                if (!PhoneNumberUtils.compare(num1[i], num2[i])) {
                    return false;
                }
            }
            return true;
        } else if (isConf1 != isConf2) {
            return false;
        } else {
            return PhoneNumberUtils.compare(number1, number2);
        }
    }

    private boolean isSameAccount(String name1, String name2, String id1, String id2) {
        return TextUtils.equals(name1, name2) && TextUtils.equals(id1, id2);
    }

    @VisibleForTesting
    boolean compareSipAddresses(String number1, String number2) {
        if (number1 == null || number2 == null) return number1 == number2;

        int index1 = number1.indexOf('@');
        final String userinfo1;
        final String rest1;
        if (index1 != -1) {
            userinfo1 = number1.substring(0, index1);
            rest1 = number1.substring(index1);
        } else {
            userinfo1 = number1;
            rest1 = "";
        }

        int index2 = number2.indexOf('@');
        final String userinfo2;
        final String rest2;
        if (index2 != -1) {
            userinfo2 = number2.substring(0, index2);
            rest2 = number2.substring(index2);
        } else {
            userinfo2 = number2;
            rest2 = "";
        }

        return userinfo1.equals(userinfo2) && rest1.equalsIgnoreCase(rest2);
    }

    /**
     * Given a call date and the current date, determine which date group the call belongs in.
     *
     * @param date The call date.
     * @param now  The current date.
     * @return The date group the call belongs in.
     */
    private int getDayGroup(long date, long now) {
        int days = DateUtils.getDayDifference(TIME, date, now);

        if (days == 0) {
            return DAY_GROUP_TODAY;
        } else if (days == 1) {
            return DAY_GROUP_YESTERDAY;
        } else {
            return DAY_GROUP_OTHER;
        }
    }

    private boolean areBothNotVoicemail(int callType, int groupCallType) {
        return callType != AppCompatConstants.CALLS_VOICEMAIL_TYPE
                && groupCallType != AppCompatConstants.CALLS_VOICEMAIL_TYPE;
    }

    private boolean areBothNotBlocked(int callType, int groupCallType) {
        return callType != AppCompatConstants.CALLS_BLOCKED_TYPE
                && groupCallType != AppCompatConstants.CALLS_BLOCKED_TYPE;
    }

    private boolean areBothBlocked(int callType, int groupCallType) {
        return callType == AppCompatConstants.CALLS_BLOCKED_TYPE
                && groupCallType == AppCompatConstants.CALLS_BLOCKED_TYPE;
    }

    /****************BKAV***************/
    // Anhdts
    private long mGroupTime;
    // Anhdts truong thoi gian goi
    private long mTimeQuery;

    // Anhdts check cung ngay trong 1 nhom
    private boolean isSameDay(Cursor cursor) {
        mTimeQuery = cursor.getLong(CallLogQuery.DATE);
        Calendar calendarCall = Calendar.getInstance();
        Calendar calendarGroup = Calendar.getInstance();
        calendarCall.setTimeInMillis(mTimeQuery);
        calendarGroup.setTimeInMillis(mGroupTime);
        return calendarCall.get(Calendar.YEAR) == calendarGroup.get(Calendar.YEAR) &&
                calendarCall.get(Calendar.DAY_OF_YEAR) == calendarGroup.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Anhdts get list da duoc sap xep
     */
    public ArrayList<Long> getSortList() {
        return mListFilter;
    }

    // Bkav HuyNQN them bien check giao dien recoder
    private boolean mIsRecoder=false;

    public void setRecoder(boolean recoder) {
        mIsRecoder = recoder;
    }
}
