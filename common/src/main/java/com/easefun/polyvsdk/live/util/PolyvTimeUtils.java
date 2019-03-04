package com.easefun.polyvsdk.live.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 常用时间工具类
 */
public class PolyvTimeUtils {

    private static SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static SimpleDateFormat formatDay = new SimpleDateFormat("d", Locale.getDefault());

    private static SimpleDateFormat formatMonthDay = new SimpleDateFormat("M-d", Locale.getDefault());

    private static SimpleDateFormat formatDateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());

    private static SimpleDateFormat formatDateTime2 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());


    /**
     * 格式化日期
     *
     * @return 年月日
     */
    private static String formatDate(Date date) {

        return formatDate.format(date);
    }


    /**
     * 格式化日期
     *
     * @return 年月日 时分秒
     */
    private static String formatDateTime(Date date) {

        return formatDateTime.format(date);
    }

    /**
     * 格式化日期
     *
     * @return (年月日) 时分秒
     */
    private static String formatDateTime2(Date date) {
        Calendar cal = Calendar.getInstance();

        // 判断是否是同一天
        String curDate = formatDate.format(cal.getTime());
        String paramDate = formatDate.format(date);
        return curDate.equals(paramDate) ? formatDateTime2.format(date) : formatDateTime.format(date);
    }


    /**
     * 将时间戳解析成日期
     *
     * @return 年月日
     */
    public static String parseDate(long timeInMillis) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        Date date = calendar.getTime();
        return formatDate(date);
    }


    /**
     * 将时间戳解析成日期
     *
     * @return 年月日 时分秒
     */
    public static String parseDateTime(long timeInMillis) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        Date date = calendar.getTime();
        return formatDateTime(date);
    }


    /**
     * 解析日期
     */
    public static Date parseDate(String date) {

        Date mDate = null;
        try {
            mDate = formatDate.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return mDate;
    }


    /**
     * 解析日期
     */
    private static Date parseDateTime(String datetime) {

        Date mDate = null;
        try {
            mDate = formatDateTime.parse(datetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return mDate;
    }

    /**
     * 以友好的方式显示时间
     */
    public static String friendlyTime(long timeInMillis) {
        Date time = parseDateTime(parseDateTime(timeInMillis));
        if (time == null) {
            return "Unknown";
        }
        return formatDateTime2(time);
    }

    /**
     * 以友好的方式显示时间
     */
    public static String friendlyTime(String sdate) {

        Date time = parseDateTime(sdate);
        if (time == null) {
            return "Unknown";
        }
        String ftime = "";
        Calendar cal = Calendar.getInstance();

        // 判断是否是同一天
        String curDate = formatDate.format(cal.getTime());
        String paramDate = formatDate.format(time);
        if (curDate.equals(paramDate)) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0) {
                ftime = Math.max(
                        (cal.getTimeInMillis() - time.getTime()) / 60000, 1)
                        + "分钟前";
            } else {
                ftime = hour + "小时前";
            }
            return ftime;
        }

        long lt = time.getTime() / 86400000;
        long ct = cal.getTimeInMillis() / 86400000;
        int days = (int) (ct - lt);
        if (days == 0) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0) {
                ftime = Math.max(
                        (cal.getTimeInMillis() - time.getTime()) / 60000, 1)
                        + "分钟前";
            } else {
                ftime = hour + "小时前";
            }
        } else if (days == 1) {
            ftime = "昨天";
        } else if (days == 2) {
            ftime = "前天";
        } else if (days > 2 && days <= 5) {
            ftime = days + "天前";
        } else if (days > 5) {
            ftime = formatDate.format(time);
        }
        return ftime;
    }

    /**
     * 根据日期获取当期是周几
     *
     * @param date
     * @return
     */
    public static String getWeek(Date date) {

        String[] weeks = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (week_index < 0) {
            week_index = 0;
        }
        return weeks[week_index];
    }

    /**
     * 以友好的方式显示视频时间
     */
    public static String generateTime(long millisecond) {
        return generateTime(millisecond, false);
    }

    /**
     * 以友好的方式显示视频时间
     *
     * @param millisecond 毫秒
     * @param fit         小时是否适配"00"
     * @return
     */
    public static String generateTime(long millisecond, boolean fit) {
        int totalSeconds = (int) (millisecond / 1000);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (fit || hours > 0)
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
