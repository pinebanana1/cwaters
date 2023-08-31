package com.ora.calmwaters.data.localstorage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FileDateHeaderFormat {

    public static final String GMT = "yyyyMMddHHmmssz";
    public static final String diaryFileName = "dd-MM-yyyy hh-mm-ss";
    public static final String dailyLogPath = "dd-MM-yyyy";
    public static final String dailyLogTimeEntry = "hh:mm:ss ";

    public static String getGMTTime() {
        SimpleDateFormat gmt = new SimpleDateFormat(GMT, Locale.getDefault());
        gmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        return gmt.format(new Date());
    }
}
