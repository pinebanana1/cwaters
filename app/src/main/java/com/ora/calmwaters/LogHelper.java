package com.ora.calmwaters;

import android.util.Log;

public class LogHelper {

    private static final String TAG = "OraInOffice";

    LogHelper() {}

    static public void d(String message){
        Log.d(TAG, message);
    }

    static public void e(String error){
        Log.e(TAG, error);
    }

    static public void w(String warning) {
        Log.w(TAG, warning);
    }


}
