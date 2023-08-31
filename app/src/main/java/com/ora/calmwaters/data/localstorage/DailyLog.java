package com.ora.calmwaters.data.localstorage;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import calmwaters.BuildConfig;


public class DailyLog {
    private File dailyLog = null;
    private String date = null;
    private String prefix = null;
    private File mediaDir = null;

    final String version = BuildConfig.VERSION_NAME;    

    public DailyLog(File path, String s) {
        mediaDir = path;
        prefix = s;
    }

    private File prepareLogfile() {
        String currentDate = new SimpleDateFormat(FileDateHeaderFormat.dailyLogPath, Locale.getDefault()).format(new Date());
        if (currentDate.equals(date))
            return dailyLog;

        File imagePath = new File(mediaDir, "images");
        //File imagePath = new File(getFilesDir(), "images");
        if (!imagePath.exists())
            imagePath.mkdir();

        dailyLog = new File(imagePath, prefix +"_v" + version + '_' + currentDate + ".txt");
        try {
            dailyLog.createNewFile();
            return dailyLog;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public  void append(String text) {
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(prepareLogfile(), true));
            String currentTime = new SimpleDateFormat(FileDateHeaderFormat.dailyLogTimeEntry, Locale.getDefault()).format(new Date()); //TODO: Check tz
            buf.append(currentTime + text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void v(String TAG, String text) {
        Log.v(TAG, text);
        append(TAG + ':' + text);
    }

    public void d(String TAG, String text) {
        Log.d(TAG, text);
        append(TAG + ':' + text);
    }

    public void i(String TAG, String text) {
        Log.i(TAG, text);
        append(TAG + ':' + text);
    }

    public void w(String TAG, String text) {
        Log.w(TAG, text);
        append(TAG + ':' + text);
    }

    public void e(String TAG, String text) {
        Log.e(TAG, text);
        append(TAG + ':' + text);
    }
}
