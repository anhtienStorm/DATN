package bkav.android.btalk.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import bkav.android.btalk.R;

/**
 * Created by anhdt on 31/03/2017.
 */

public class DateUtil {
    private static volatile DateUtil sInstance;

    public static DateUtil get() {
        if (sInstance == null) {
            sInstance = new DateUtil();
        }
        return sInstance;
    }
    /**
     * Bkav Anhdts
     * dinh dang thoi gian goi
     */
    public String getCallTime(Context context, long time, long now) {
        StringBuilder builder = new StringBuilder();
        Resources resource = context.getResources();

        if (now > time) {
            long duration = now - time;

            if (duration < DateUtils.MINUTE_IN_MILLIS) {
                long seconds = duration / DateUtils.SECOND_IN_MILLIS;
                return String.format(resource.getQuantityString(
                        R.plurals.recentCalls_timeInSeconds, (int) seconds), seconds);
            } else if (duration < DateUtils.HOUR_IN_MILLIS) {
                long minutes = duration / DateUtils.MINUTE_IN_MILLIS;
                return String.format(resource.getQuantityString(
                        R.plurals.recentCalls_timeInMinutes, (int) minutes), minutes);
            } else {
                Calendar calCalled = Calendar.getInstance();
                calCalled.setTimeInMillis(time);

                int tempTime = calCalled.get(Calendar.HOUR_OF_DAY);
                builder.append(tempTime < 10 ? "0" + tempTime : tempTime).append(":");

                tempTime = calCalled.get(Calendar.MINUTE);
                builder.append(tempTime < 10 ? "0" + tempTime : tempTime);
            }
        }

        return builder.toString();

    }

    /**
     * Anhdts
     * @param context
     * @param when
     * @return dinh dang label ngay goi
     */
    @SuppressLint("SimpleDateFormat")
    public String formatDateRecentCall(Context context, long when) {
        String dateFormat;
        Calendar now = Calendar.getInstance();
        Calendar then = Calendar.getInstance();
        then.setTimeInMillis(when);
        // Neu la ngay trong tuan thi hien thi luon ten ngay do
        String timeFormat = bkavFormatDate(context, then, now);
        // Neu khong phai thi hient hi ngay theo dinh dang ngay/thang/nam
        if (timeFormat == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date(when);
            dateFormat = sdf.format(date);
        } else {
            dateFormat = timeFormat;
        }

        return dateFormat;
    }

    /**
     * Bkav Anhdts - Fomat theo dinh dang ngay trong tuan Sunday, Monday,....
     *
     */
    private String bkavFormatDate(Context context, Calendar then, Calendar now) {
        if (now.get(Calendar.YEAR) - then.get(Calendar.YEAR) == 0) {
            if (now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)) {
                return context.getString(R.string.day_of_week_long_today);
            } else if (now.get(Calendar.DAY_OF_YEAR) - then.get(Calendar.DAY_OF_YEAR) == 1) {
                return context.getString(R.string.yesterday);
            } else if (now.get(Calendar.DAY_OF_YEAR) - then.get(Calendar.DAY_OF_YEAR) > 1
                    && now.get(Calendar.DAY_OF_YEAR) - then.get(Calendar.DAY_OF_YEAR) < 7) {
                int day = then.get(Calendar.DAY_OF_WEEK);
                if (day == Calendar.SUNDAY) {
                    return context.getString(R.string.day_of_week_long_sunday);
                } else if (day == Calendar.MONDAY) {
                    return context.getString(R.string.day_of_week_long_monday);
                } else if (day == Calendar.TUESDAY) {
                    return context.getString(R.string.day_of_week_long_tuesday);
                } else if (day == Calendar.WEDNESDAY) {
                    return context.getString(R.string.day_of_week_long_wednesday);
                } else if (day == Calendar.THURSDAY) {
                    return context.getString(R.string.day_of_week_long_thursday);
                } else if (day == Calendar.FRIDAY) {
                    return context.getString(R.string.day_of_week_long_friday);
                } else {
                    return context.getString(R.string.day_of_week_long_saturday);
                }
            }
        }
        return null;
    }

}
