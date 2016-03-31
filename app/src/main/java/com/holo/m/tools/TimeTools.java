package com.holo.m.tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by 根深 on 2016/1/9.
 */
public class TimeTools {
    static long OneDay = 24 * 3600 * 1000;
    static long TwoDays = 48 * 3600 * 1000;
    static long OneWeek = 7 * 24 * 3600 * 1000;

    static SimpleDateFormat today = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
    static SimpleDateFormat yesterday = new SimpleDateFormat("昨天:HH:mm:ss", Locale.ENGLISH);
    static SimpleDateFormat week = new SimpleDateFormat("E HH:mm", Locale.ENGLISH);
    static SimpleDateFormat month = new SimpleDateFormat("MM-dd HH:mm", Locale.ENGLISH);
    static SimpleDateFormat year = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

    public static long getTime() {
        return System.currentTimeMillis() / 1000;   //return second
    }

    public static String getShowAbleDate(Object obj) {
        return getShowAbleDate((Long.valueOf(obj.toString())) * 1000);
    }

    public static String getShowAbleDate(long time) {
        long time_now = (new Date()).getTime();

        long difference = time_now - time;
        if (difference < OneDay) {
            return today.format(time);
        } else if (difference < TwoDays) {
            return yesterday.format(time);
        } else if (difference < OneWeek) {
            return week.format(time);
        } else {
            Calendar calendar = Calendar.getInstance();
            int YEAR_NOW = calendar.get(Calendar.YEAR);
            calendar.setTime(new Date(time));
            if (calendar.get(Calendar.YEAR) == YEAR_NOW) {
                return month.format(time);
            }
            return year.format(time);
        }
    }

    public static String getDurationDisplay(int duration) {
        duration /= 1000;
        int second = duration % 60;
        duration /= 60;
        int minute = duration % 60;
        duration /= 60;
        int hour = duration % 60;
        return (hour == 0 ? "" : hour + ":") +
                (minute < 10 ? "0" + minute + ":" : minute + ":") +
                (second < 10 ? "0" + second : second);
    }

    public static String getVoiceDurationDisplay(long duration) {
        duration /= 1000;
        byte second = (byte) (duration % 60);
        duration /= 60;
        byte minute = (byte) (duration % 60);
        return (minute == 0 ? "" : minute + "\':") +
                (second == 0 ? "1" : second) + "\"";
    }

}
