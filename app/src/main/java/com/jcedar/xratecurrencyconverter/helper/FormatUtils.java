package com.jcedar.xratecurrencyconverter.helper;

import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by OLUWAPHEMMY on 11/10/2016.
 */
public class FormatUtils {

    public static String getReadableDate (long timestamp) {
        Date date = new Date(timestamp * 1000);
//        SimpleDateFormat dateFormat = new SimpleDateFormat("E, MMM d, hh:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(dateFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long now = System.currentTimeMillis();
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(date.getTime(), now, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
        String mm = dateFormat.format(date);
        if (timeAgo.equals("Today")) {
            return timeAgo + " at " + mm;
        } else
            return (String) timeAgo;
    }

    private static String getDaysAgo (Date date) {
        long days = (new Date().getTime() - date.getTime()) / 86400000;
        if (days == 0)
            return "Today";
        else if (days == 1)
            return "Yesterday";
        else
            return days + " days ago";

    }

}
