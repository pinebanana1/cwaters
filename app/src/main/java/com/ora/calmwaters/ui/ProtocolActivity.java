package com.ora.calmwaters.ui;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import calmwaters.BuildConfig;
import com.ora.calmwaters.data.localstorage.DailyLog;
import com.ora.calmwaters.LogHelper;
import com.ora.calmwaters.OverlayService;
import com.ora.calmwaters.PowerConnectionReceiver;
import com.ora.calmwaters.AzureUploader;
import com.ora.calmwaters.data.localstorage.FileDateHeaderFormat;

import calmwaters.R;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ProtocolActivity extends AppCompatActivity {
    final static String TAG = "OraHome";
    static final int CAMERA_REQUEST = 1888;
    static final int VIDEO_REQUEST = 1889;
    static final int ACCESSIBILITY_REQUEST = 1999;
    static final int BIOMETRIC_ENROLL_REQUEST = 1777;
    static final int WIFI_REQUEST = 1666;

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private int mPrevImageCount;

    final static int SCREEN_TIMEOUT = 30 * 60 * 1000;
    static long alarmStartTimeMs;

    public void enableButton(boolean enable) {
        isButtonEnabled = enable;
        //FloatingActionButton fab = findViewById(R.id.fab);
        //fab.setEnabled(enable);
        Log.i(TAG, ("enableButton:" + enable));
        dailyLog.append("enableButton:" + enable);
    }

//    public void enableAlarmsButton(boolean enable) {
//        FloatingActionButton fab = findViewById(R.id.alarm_fab);
//        fab.setEnabled(enable);
//    }

    public enum EyeSide {
        Left, Right
    }

    public enum EyeDirection {
        Front, Up
    }

    static public EyeSide side;
    static public EyeDirection direction = EyeDirection.Front;
    static int timeline = 0;
    static boolean timelineVisible = true;
    static int phoneID = -1;
    static String protocol = "--";
    static String subjectID = "0";
    static String vistID = "0";
    static String assessment = "unassessed";
    static int assessmentIndex;
    static int photoCount;
    static boolean isAlarmed;
    static boolean hasAlarmGoneOff;
    static String alarmHM;
    static  boolean isButtonEnabled;
    static int snoozeState;

    static int lastUpdate;

    static final String[] timelines = {
            "0", "10", "30", "60", "90"
    };

    static final String[] assessments = {
            "Inclusion",
            "Pre-Dose",
            "Post-Dose",
    };

    //alarms are set via 24 hour form (17 -> 17-12 = 5:00pm)
    static int alarms[] = {
            9, 17
            //7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19//, 20, 21, 22, 23
    };

    static int snoozeMins;
    static boolean initialAlarm = false;

    MediaObserver mediaObserver = null;
    ScreenReceiver screenReceiver = null;
    PowerConnectionReceiver powerConnectReceiver = null;

    public static DailyLog dailyLog;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (dailyLog == null)
            dailyLog = new DailyLog(getExternalMediaDirs()[0], "Log");
        dailyLog.append("onCreate");

        setContentView(R.layout.activity_main);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar actionBar = getSupportActionBar();
        // Enable the Up button
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //toolbar.showOverflowMenu();

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
            //            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//            @RequiresApi(api = Build.VERSION_CODES.M)
//            @Override
//            public void onClick(View v) {
//                v.clearAnimation();
//                takePicture();
//            }
//        });
        enableButton(false);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_CAMERA_PERMISSION_CODE);
        }

        biometricEnroll();

        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.about:
                    Toast.makeText(getApplicationContext(),
                            "v" + BuildConfig.VERSION_NAME + " Phone ID: #"+ phoneID, Toast.LENGTH_SHORT)
                            .show();
                    break;
                // TODO: Other cases
                default:
                    dailyLog.append("setOnMenuItemClickListener default");
            }
            return true;
        });

        if (mediaObserver == null) {
            Log.d(TAG, "OnCreate: registerContentObserver");
            mediaObserver = new MediaObserver(null);

            //set the observer for photos
            getContentResolver().registerContentObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    true, mediaObserver);

            getContentResolver().registerContentObserver(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    true, mediaObserver);
        } else {
            Log.w(TAG, "OnCreate: mediaObserver exists");
        }

        if (screenReceiver == null) {
            screenReceiver = new ScreenReceiver();
            IntentFilter screenOffOnFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            screenOffOnFilter.addAction(Intent.ACTION_SCREEN_ON);
            registerReceiver(screenReceiver, screenOffOnFilter);
        }

        if (powerConnectReceiver == null) {
            IntentFilter powerConnectedFilter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
            powerConnectReceiver = new PowerConnectionReceiver();
            registerReceiver(powerConnectReceiver, powerConnectedFilter);
        }

        if (isAlarmed) { // if somehow we get recreated
            Log.w(TAG, "recreated alarmed");
            dailyLog.append("recreated alarmed");
//            ExtendedFloatingActionButton efab = findViewById(R.id.alarm_fab);
//            efab.setText(alarmHM);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (isEdgeToEdgeEnabled(this) != 2) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Please contact support")
                    .setMessage("Navigation buttons are not disabled")
                    .setCancelable(false)
                    //.setIcon(android.R.drawable.ic_lock_power_off)
                    .show();
        }

        ConnectivityManager cm = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
        if (cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnected()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("📶 Network not onnected")
                    .setMessage("Select a wif-fi connection")
                    .setCancelable(false)
                    //.setIcon(android.R.drawable.)
                    .setPositiveButton("Ok", (dialog, which) -> {
                        IntentFilter intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                        wifiReceiver = new WifiReceiver();
                        registerReceiver(wifiReceiver, intentFilter);
                        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                        finish();
                    })
                    .show();
        }

        if (!isAccessibilityServiceEnabled(this, OverlayService.class)) {
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, ACCESSIBILITY_REQUEST);
        } else {
//            KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
//            if (km.isKeyguardSecure()) {
//                Intent authIntent = km.createConfirmDeviceCredentialIntent("Keyguard title", "Keyguard Message");
//                startActivity(authIntent);
//            }

            //authenticate();
        }

//        retrievePhoneId();
    }



    public static int isEdgeToEdgeEnabled(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android");
        if (resourceId > 0) {
            return resources.getInteger(resourceId);
        }
        return 0;
    }

    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> service) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(context.getPackageName()) && enabledServiceInfo.name.equals(service.getName()))
                return true;
        }

        return false;
    }

    private void setupAlarms() {
        AlarmManager alarmMgr =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(), PopupActivity.AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        LocalDateTime alarmTime = LocalDateTime.now();
        int m = alarmTime.getMinute();
        m -= m % 15; // 4xhr
        LocalDateTime nextAlarm = null;
        long milli = 0;
        boolean isMorningNext = true;


        for (int i = 0; i < alarms.length; i++) {
            if (snoozeMins==0)
                if (initialAlarm)
                    nextAlarm = alarmTime.withHour(alarms[i]).withMinute(alarmTime.getMinute() + 1).withSecond(0);
                else {
                    nextAlarm = alarmTime.withHour(alarms[i]).withMinute(0).withSecond(0);
                    //nextAlarm = alarmTime.plusMinutes(15);
                    //nextAlarm = nextAlarm.minusMinutes(nextAlarm.getMinute() % 15);
                }
            else {
                nextAlarm = alarmTime.plusMinutes(snoozeMins);
                snoozeMins = 0;
            }
            milli = nextAlarm.toEpochSecond(OffsetDateTime.now().getOffset()) * 1000;
            String offs = OffsetDateTime.now().getOffset().toString();

            if (milli > System.currentTimeMillis()) {
                isMorningNext = false;
                break;
            }
        }
        if (isMorningNext) {
            nextAlarm = alarmTime.plusDays(1).withHour(alarms[0]).withMinute(0).withSecond(0);
            milli = nextAlarm.toEpochSecond(OffsetDateTime.now().getOffset()) * 1000;
        }
        alarmTime = alarmTime.withMinute(m + 1).withSecond(0);

        long sysMillis = System.currentTimeMillis();
        long diff = milli - sysMillis;

        int min = nextAlarm.getMinute();

        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                milli, alarmIntent);

        Instant nextAlarmInstant = Instant.ofEpochSecond(milli / 1000);
        //nextAlarmInstant.get(ChronoField.NANO_OF_DAY);
        LocalTime time = LocalTime.ofInstant(nextAlarmInstant, ZoneId.systemDefault());
        int minofH = time.getMinute();
        //nextAlarmInstant.get(ChronoField.MINUTE_OF_HOUR);

        alarmHM = time.format(DateTimeFormatter.ofPattern("hh:mma"));
        dailyLog.append("time=" + alarmHM);

//        ExtendedFloatingActionButton efab = findViewById(R.id.alarm_fab);
//        efab.setText(alarmHM);
//        efab.setIconTintResource(R.color.material_on_background_disabled);

        isAlarmed = true;
        initialAlarm = false;

        if (isMorningNext) {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, ifilter);

            // Are we charging / charged?
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = level * 100 / (float) scale;

            if (!isCharging) {
                r = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));

                r.play();

                Handler handler = new Handler(Looper.getMainLooper());

                powerAlertDialog = new AlertDialog.Builder(this)
                        .setTitle(this.getString(R.string.app_name))
                        .setMessage("\uD83D\uDD0CPlug the phone in please")
                        .setCancelable(false)
                        //.setIcon(android.R.drawable.ic_lock_power_off)
                        .show();
            }
        }
        // register our power status receivers
    }

    class MediaObserver extends ContentObserver {
        public MediaObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange,
                             Uri uri,
                             int flags) {
            super.onChange(selfChange, uri, flags);

            Log.d(TAG, "MediaObserver onChange() : " + uri.toString() + " flags:" + flags + " selfChange:" + selfChange);

            String[] projection;
                if (uri.toString().contains("video")) {
                LogHelper.d("MediaObserver onChange() : URI matched to be External Video content setting projection");
                projection = new String[]{
                        MediaStore.Video.Media.DISPLAY_NAME,
                        //MediaStore.Video.Media._COUNT,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media._ID,
                        //MediaStore.Video.Media.DATE_ADDED,
                        //MediaStore.Video.Media.DATE_MODIFIED,
                        //MediaStore.Video.Media.DATA,
                        //MediaStore.Video.Media.ISO,
                        //MediaStore.Video.Media.CONTENT_TYPE,
                        //MediaStore.Video.Media.BUCKET_DISPLAY_NAME
                        MediaStore.Video.Media.DESCRIPTION,
                        MediaStore.Video.Media.DOCUMENT_ID,
                        MediaStore.Video.Media.DEFAULT_SORT_ORDER,
                        MediaStore.Video.Media.INSTANCE_ID,
                        MediaStore.Video.Media.MIME_TYPE,
                        MediaStore.Video.Media.ORIGINAL_DOCUMENT_ID,
                        MediaStore.Video.Media.OWNER_PACKAGE_NAME,
                        MediaStore.Video.Media.XMP,
                        MediaStore.Video.Media.MINI_THUMB_MAGIC,
                        MediaStore.Video.Media.DOCUMENT_ID,
                        MediaStore.Video.Media.LATITUDE
                };
            } else {
                LogHelper.d("MediaObserver onChange() : URI matched to be External Photo content setting projection");
                projection = new String[]{
                        MediaStore.Images.Media.DISPLAY_NAME,
                        //MediaStore.Images.Media._COUNT,
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media._ID,
                        //MediaStore.Images.Media.DATE_ADDED,
                        //MediaStore.Images.Media.DATE_MODIFIED,
                        //MediaStore.Images.Media.DATA,
                        //MediaStore.Images.Media.ISO,
                        //MediaStore.Images.Media.CONTENT_TYPE,
                        //MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                        MediaStore.Images.Media.DESCRIPTION,
                        MediaStore.Images.Media.DOCUMENT_ID,
                        MediaStore.Images.Media.DEFAULT_SORT_ORDER,
                        MediaStore.Images.Media.INSTANCE_ID,
                        MediaStore.Images.Media.MIME_TYPE,
                        MediaStore.Images.Media.ORIGINAL_DOCUMENT_ID,
                        MediaStore.Images.Media.OWNER_PACKAGE_NAME,
                        MediaStore.Images.Media.XMP,
                        MediaStore.Images.Media.MINI_THUMB_MAGIC,
                        MediaStore.Images.Media.PICASA_ID,
                        MediaStore.Images.Media.LATITUDE
                };
            }

            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            cursor.moveToFirst();
            String filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            int id;

            if (uri.toString().contains("video")) {
                LogHelper.d( "filename=" + filename);
                LogHelper.d( "size=" + cursor.getInt(cursor.getColumnIndex(OpenableColumns.SIZE)));
                //Log.d(TAG, "_COUNT=" + cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._COUNT)));
                id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                LogHelper.d( "_ID=" + id);
                //Log.d(TAG, "DATA=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
                //Log.d(TAG, "DATE_ADDED=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)));
                //Log.d(TAG, "DATE_MODIFIED=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)));
                //Log.d(TAG, "ISO=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ISO)));
                //Log.d(TAG, "CONTENT_TYPE=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.CONTENT_TYPE)));
                //Log.d(TAG, "BUCKET_DISPLAY_NAME=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)));
                //Log.d(TAG, "DATE_MODIFIED=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ISO)));
                LogHelper.d( "DESCRIPTION=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DESCRIPTION)));
                LogHelper.d( "DOCUMENT_ID=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DOCUMENT_ID)));
                LogHelper.d( "DEFAULT_SORT_ORDER=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DEFAULT_SORT_ORDER)));
                LogHelper.d( "INSTANCE_ID=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.INSTANCE_ID)));
                LogHelper.d( "MIME_TYPE=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)));
                LogHelper.d( "ORIGINAL_DOCUMENT_ID=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ORIGINAL_DOCUMENT_ID)));
                LogHelper.d( "OWNER_PACKAGE_NAME=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.OWNER_PACKAGE_NAME)));
                LogHelper.d( "XMP=" + cursor.getBlob(cursor.getColumnIndex(MediaStore.Video.Media.XMP)));
                LogHelper.d( "MINI_THUMB_MAGIC=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MINI_THUMB_MAGIC)));
                LogHelper.d( "PICASA_ID=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DOCUMENT_ID)));
                LogHelper.d( "LATITUDE=" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.LATITUDE)));
            } else {
                LogHelper.d( "filename=" + filename);
                LogHelper.d( "size=" + cursor.getInt(cursor.getColumnIndex(OpenableColumns.SIZE)));
                //Log.d(TAG, "_COUNT=" + cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._COUNT)));
                id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                LogHelper.d( "_ID=" + id);
                //Log.d(TAG, "DATA=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                //Log.d(TAG, "DATE_ADDED=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)));
                //Log.d(TAG, "DATE_MODIFIED=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)));
                //Log.d(TAG, "ISO=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.ISO)));
                //Log.d(TAG, "CONTENT_TYPE=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.CONTENT_TYPE)));
                //Log.d(TAG, "BUCKET_DISPLAY_NAME=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)));
                //Log.d(TAG, "DATE_MODIFIED=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.ISO)));
                LogHelper.d( "DESCRIPTION=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION)));
                LogHelper.d( "DOCUMENT_ID=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DOCUMENT_ID)));
                LogHelper.d( "DEFAULT_SORT_ORDER=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DEFAULT_SORT_ORDER)));
                LogHelper.d( "INSTANCE_ID=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.INSTANCE_ID)));
                LogHelper.d( "MIME_TYPE=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)));
                LogHelper.d( "ORIGINAL_DOCUMENT_ID=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.ORIGINAL_DOCUMENT_ID)));
                LogHelper.d( "OWNER_PACKAGE_NAME=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.OWNER_PACKAGE_NAME)));
                LogHelper.d( "XMP=" + cursor.getBlob(cursor.getColumnIndex(MediaStore.Images.Media.XMP)));
                LogHelper.d( "MINI_THUMB_MAGIC=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MINI_THUMB_MAGIC)));
                LogHelper.d( "PICASA_ID=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.PICASA_ID)));
                LogHelper.d( "LATITUDE=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE)));
            }


            String fileExtension = filename.substring(filename.length()-4);
            LogHelper.d("flags: ContentResolver.Notify_Update? : " + (flags == ContentResolver.NOTIFY_UPDATE));
            LogHelper.d("filename contains \"PXL\": " + filename.contains("PXL_"));
            LogHelper.d("filename extension is " + fileExtension + ", is it an MP4?" + fileExtension.contains("mp4"));
            if (flags == ContentResolver.NOTIFY_UPDATE
                    && filename.contains("PXL_")
                    && fileExtension.contains("mp4")
                    && id != lastUpdate) {
                LogHelper.d("MediaObserver - Video :" + uri);

                lastUpdate = id;
                dailyLog.append("video URI: " + uri + "lastUpdate=" + lastUpdate);

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setClass(ProtocolActivity.this, VideoReviewActivity.class);
                intent.putExtra(VideoReviewActivity.VIDEO_INTENT_KEY, uri);
                startActivityForResult(intent, VIDEO_REQUEST);
            }
            else if (flags == ContentResolver.NOTIFY_UPDATE
                    && filename.contains("PXL_") && !filename.endsWith(".dng")
                    && id != lastUpdate) {
                LogHelper.d("MediaObserver - Photo :" + uri);

                lastUpdate = id;
                dailyLog.append("photo URI:" + uri + " lastUpdate=" + lastUpdate);

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setClass(ProtocolActivity.this, TouchImageViewActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, CAMERA_REQUEST);
            }
            else {
                LogHelper.e("Could not determine content was photo or video.");
            }

            cursor.close();
        }
    }

    WifiReceiver wifiReceiver;

    class WifiReceiver extends BroadcastReceiver {
        Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "WifiReceiver onReceive");

            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                Log.d(TAG, "WifiReceiver onReceive NETWORK_STATE_CHANGED_ACTION");

                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info != null && info.isConnected()) {
                    Log.d(TAG, "WifiReceiver onReceive isConnected");
                    unregisterReceiver(wifiReceiver);
                    wifiReceiver = null;
                    context.startActivity(new Intent(context, ProtocolActivity.class));
                } else {
                    Log.e(TAG, "WifiReceiver onReceive not connected");
                    // wifi connection was lost
                }
            }
        }
    }

    class ScreenReceiver extends BroadcastReceiver {
        Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.d(TAG, "onReceive ACTION_SCREEN_OFF");
                //OverlayService.instance.enableClicks(true);
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.d(TAG, "onReceive ACTION_SCREEN_ON");
                //OverlayService.instance.enableClicks(false);
                //finishActivity(ProtocolActivity.CAMERA_REQUEST);
                //finish();
            } else {
                Log.e(TAG, "onReceive unhandled intent");
                dailyLog.append("ScreenReceiver unhandled intent");
            }
        }
    }

//    private void wakeUpDevice(Context context) {
//        PowerManager.WakeLock wakeLock = context.getWakeLock(); // get WakeLock reference via AppContext
//        if (wakeLock.isHeld()) {
//            wakeLock.release(); // release old wake lock
//        }
//
//        // create a new wake lock...
//        wakeLock.acquire();
//
//        // ... and release again
//        wakeLock.release();
//    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        //Show the action bar menu
        getMenuInflater().inflate(R.menu.top_app_bar, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent:" + intent.getAction() + " "+intent.getDataString());
        dailyLog.append("onNewIntent:" + intent.getAction() + " "+intent.getDataString());
        super.onNewIntent(intent);

        if (r != null)
            r.stop();
        if (powerAlertDialog != null)
            powerAlertDialog.hide();
    }

    Ringtone r = null;
    AlertDialog powerAlertDialog;

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        dailyLog.append("OnResume hasAlarmGoneOff="+hasAlarmGoneOff+" isAlarmed="+isAlarmed);
        super.onResume();

//        final ExtendedFloatingActionButton efab = findViewById(R.id.alarm_fab);
//        final FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setEnabled(isButtonEnabled);

        if (hasAlarmGoneOff) {
            if (side==EyeSide.Right && direction == EyeDirection.Front)
                authenticate();

//            efab.setText("Photo time");
            //efab.setEnabled(true);
//            efab.setIconTintResource(R.color.purple_200);

            final Animation animation = new AlphaAnimation(1, 0.25f); // Change alpha from fully visible to invisible
            animation.setDuration(1000);
            animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
            animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
            animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
//            fab.startAnimation(animation);
        }
        else {
//            efab.setText(alarmHM);
//            efab.setIconTintResource(R.color.material_on_background_disabled);
        }

//        if (!isAlarmed)
//            setupAlarms();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause");
        dailyLog.append("onPause");

        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop");
        dailyLog.append("onStop");

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //getContentResolver().unregisterContentObserver(photoObserver);
        //photoObserver = null;
        unregisterReceiver(screenReceiver);
        screenReceiver = null;
        unregisterReceiver(powerConnectReceiver);
        powerConnectReceiver = null;

        boolean fin = this.isFinishing();
        Log.d(TAG, "onDestroy: isFinishing="+fin);
        dailyLog.append("onDestroy: isFinishing="+fin);
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        dailyLog.append("onRestart");

        super.onRestart();
        if (OverlayService.instance != null)
            OverlayService.instance.enableClicks(true);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        dailyLog.append("onStart");

        super.onStart();
    }

    private void biometricEnroll() {
        BiometricManager biometricManager = BiometricManager.from(this);

        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("MY_APP_TAG", "No biometric features available on this device.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG);
                startActivityForResult(enrollIntent, BIOMETRIC_ENROLL_REQUEST);
                finish();
        }
    }

    private BiometricPrompt biometricPrompt;
    //private BiometricPrompt.PromptInfo promptInfo;

    private void authenticate() {

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authentication")
                .setSubtitle("Put your finger on circle the back of the phone")
                // Can't call setNegativeButtonText() and
                // setAllowedAuthenticators(...|DEVICE_CREDENTIAL) at the same time.
                .setNegativeButtonText("Skip")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();

        biometricPrompt = new BiometricPrompt(ProtocolActivity.this,
                ContextCompat.getMainExecutor(this), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                                "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();

                new MaterialAlertDialogBuilder(ProtocolActivity.this)
                        .setTitle("Authentication error")
                        .setMessage("Unable to authenticate with fingerprint; your iris will be used to verify you")
                        .setCancelable(false)
                        //.setIcon(android.R.drawable.)
                        .setPositiveButton("Proceed", (dialog, which) -> {})
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(PopupActivity.this, ProtocolActivity.class));//Flags(Intent.FLAG_ACTIVITY_NEW_TASK));
                //finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                                Toast.LENGTH_SHORT)
                        .show();

                biometricPrompt.authenticate(promptInfo);

//                new MaterialAlertDialogBuilder(ProtocolActivity.this)
//                        .setTitle("Authentication failed")
//                        .setMessage("Please try again")
//                        .setCancelable(false)
//                        //.setIcon(android.R.drawable.)
//                        .setPositiveButton("Ok", (dialog, which) -> {
//                            biometricPrompt.authenticate(promptInfo);
//                        })
//                        .show();
            }
        });

        biometricPrompt.authenticate(promptInfo);
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
    }

    @Override
    public void onBackPressed() {
        Log.w(TAG, "onBackPressed ingored");
        dailyLog.append("onBackPressed ingored");
//        new MaterialAlertDialogBuilder(this)
//                .setTitle("Confirm logout?")
//                .setCancelable(false)
//                .setPositiveButton("Logout", (dialog, id) -> ProtocolActivity.super.onBackPressed())
//                .setNeutralButton("Cancel", null)
//                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                dailyLog.append("onOptionsItemSelected default");
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Requesting for premissons
     * @param requestCode
     * @param permissions
     * @param grantResults
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults.length!= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "permissions granted", Toast.LENGTH_LONG).show();
//                retrievePhoneId();

            }
            else
            {
                Toast.makeText(this, "camera permission denied - please try again", Toast.LENGTH_LONG).show();
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_CAMERA_PERMISSION_CODE);
            }
        }
    }

    /**
     * Start an activity for result
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        dailyLog.append("onActivityResult: " + requestCode + ":" + resultCode);
        if (requestCode == CAMERA_REQUEST){
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Uri imageUri = data.getData();
                    Log.i(TAG, "onActivityResult:" + imageUri.toString());
                } else
                    Log.i(TAG, "onActivityResult");

                Cursor cursor = loadImageCursor();
                //get the paths to newly added images
                //String[] paths = getImagePaths(cursor, mPrevImageCount);
                String path = getImagePath(cursor, mPrevImageCount);
                cursor.close();

                if (path == null) {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Back button hit")
                            .setMessage("Please retake the picture")
                            .setPositiveButton("OK", (dialog, which) -> {
                                OverlayService.instance.startCamera(this);
                            })
                            .show();
                    return;
                }

                exitingCamera(path);
                photoCount++;

                enableButton(true);

                SwitchMaterial dirSwitch = (SwitchMaterial) findViewById(R.id.directionSwitch);
                dirSwitch.toggle();
                if (direction == EyeDirection.Up) {
                    return;
                }

                RadioGroup sideGrp = findViewById(R.id.eyeLRGrp);
                if (side == EyeSide.Right) {
                    sideGrp.check(R.id.leftEye);
                    return;
                } else {
                    sideGrp.check(R.id.rightEye);
                }

                enableButton(false);
                hasAlarmGoneOff = false;
            } else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "onActivityResult: Cancelled");
                OverlayService.instance.startCamera(this);
            } else {
                Log.i(TAG, "onActivityResult: Skipping");
            }
            getIntent().putExtra(PatientInfoFragment.CURRENT_STEP_EXTRA, 4);
        }
        else if (requestCode == VIDEO_REQUEST) {
            LogHelper.d("ProtocolActivity - onActivityResult type Video Request");
            switch (resultCode) {
                case RESULT_OK:
                    LogHelper.d("ProtocolActivity - onActivityResult: Video Result good");
                    if (data != null){
                        Uri videoUri = data.getData();
                        LogHelper.d("URI is: " + videoUri.toString());

                        Cursor cursor = loadVideoCursor();
                        //get the paths to newly added images
                        String path = getImagePath(cursor, mPrevImageCount);
                        LogHelper.d("Path is " + path);
                        cursor.close();
                        copyFile(path, "videos");

                    } else {
                        LogHelper.d("ProtocolActivity - onActivityResult uri is null");
                    }

                    //access the data and
                    break;

                case VideoReviewActivity.RESULT_REJECT:
                    LogHelper.d("ProtocolActivity - onActivityResult: Video Rejected");
                    OverlayService.instance.startVideoRecording(this);
                    break;

                case RESULT_CANCELED:
                    LogHelper.d("ProtocolActivity - onActivityResult: Video Cancelled Result");
                    OverlayService.instance.startVideoRecording(this);
                    break;

                default:
                    LogHelper.d("ProtocolActivity - onActivityResult: code is " + resultCode );
                break;
            }
            getIntent().putExtra(PatientInfoFragment.CURRENT_STEP_EXTRA, 3);
        }
        else if (requestCode == ACCESSIBILITY_REQUEST) {
            recreate();
        }
        else if (requestCode == BIOMETRIC_ENROLL_REQUEST) {
            recreate();
        }
    }

    private void takePicture() {
        protocol = getEditText(R.id.protocol).replace(" ", "");
        subjectID = getEditText(R.id.subject);
        assessment = getEditText(R.id.assessment).replace(" ", "");

//        ChipGroup chipGroup = findViewById(R.id.timelineGroup);
//        Chip checkedChip = findViewById(chipGroup.getCheckedChipId());
//        if (checkedChip != null) {
//            CharSequence timelineS = checkedChip.getText(); //debugging only, not used
//            timeline = chipGroup.indexOfChild(checkedChip) + 1;
//        }
//        else
//            timeline = 0;

        Cursor cursor = loadImageCursor();
        //current images in mediaStore
        mPrevImageCount = cursor.getCount();
        cursor.close();

//        getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI, true,
//                new ContentObserver(new Handler()) {
//                    @Override
//                    public void onChange(boolean selfChange) {
//                        Log.d("your_tag","Internal Media has been changed");
//                        super.onChange(selfChange);
//                        Long timestamp = readLastDateFromMediaStore(getApplicationContext(), MediaStore.Images.Media.INTERNAL_CONTENT_URI);
//                        // comapare with your stored last value and do what you need to do
//
//                    }
//                }
//        );

//        Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
//        //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//        //startActivityForResult(cameraIntent, CAMERA_REQUEST);
//        startActivity(cameraIntent);
        OverlayService.instance.startCamera(this);
    }

    private Long readLastDateFromMediaStore(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, "date_added DESC");
        //PhotoHolder media = null;
        Long dateAdded =-1L;
        if (cursor.moveToNext()) {
            dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED));
        }
        cursor.close();
        return dateAdded;
    }

    private String getEditText(int id) {
        return ((EditText)findViewById(id)).getText().toString();
    }

    public Cursor loadImageCursor(){
        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media.DATE_ADDED;

        return getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,null,null, orderBy);
    }

    public Cursor loadVideoCursor() {
        final String[] columns = { MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID };
        final String orderBy = MediaStore.Video.Media.DATE_ADDED;

        return getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns,null,null, orderBy);
    }

    public String[] getImagePaths(Cursor cursor, int startPosition){
        int size = cursor.getCount() - startPosition;
        if (size <= 0)
            return null;

        String []paths = new String[size];
        List<String>pathList = new ArrayList<String>();

        int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        String names[] = cursor.getColumnNames();
        for (int i = startPosition; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            String imageS = cursor.getString(dataColumnIndex);
            if (imageS.contains("DCIM/Camera/PXL_"));
                pathList.add(imageS);
            //paths[i - startPosition] = cursor.getString(dataColumnIndex);
            Log.d(TAG, "getImagePaths:" + i + ',' + imageS);
        }
        return (String[]) pathList.toArray();

        //return paths;
    }

    public String getImagePath(Cursor cursor, int startPosition){
        int size = cursor.getCount() - startPosition;
        if (size <= 0)
            return null;

        cursor.moveToLast();
        do {
            String imageS = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            if (imageS.contains("DCIM/Camera/PXL_")) {
                Log.i(TAG, "getImagePath uri:" + imageS);
                return imageS;
            } else {
                Log.w(TAG, "getImagePath internal uri:" + imageS);
                cursor.moveToPrevious();
            }
        } while (cursor.getPosition() >= startPosition);

        Log.e(TAG, "getImagePath no external uri");
        return null;
    }

    private void exitingCamera(Uri uri){

//        while (paths == null) {
//            try {
//                Thread.sleep(1000);
//                paths = getImagePaths(cursor, mPrevImageCount);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        File from = new File(uri.toString());

        File imagePath = new File(getExternalMediaDirs()[0], "images");
        //File imagePath = new File(getFilesDir(), "images");
        if (!imagePath.exists())
            imagePath.mkdir();

        SimpleDateFormat gmt = new SimpleDateFormat(FileDateHeaderFormat.GMT, Locale.getDefault());
        String currentDateTime = gmt.format(new Date()); // Today's date in GMT

        File newFile = new File(imagePath,
                //currentDateTime +
                //"_phone_" + phoneID +
                protocol + '_' +
                        subjectID + '_' +
                        "Visit" + vistID + '_' +
                        assessment + '_' +
                        timelines[timeline] +
                        (side==EyeSide.Right? "_OD_" : "_OS_") +
                        (direction==EyeDirection.Front? "FrontFacing_" : "Superior_") +
                        "Dev"+ phoneID + '_' +
                        //userid + '_' +
                        currentDateTime +  ".jpg");

//        res = from.renameTo(newFile);

        Path sourcePath = from.toPath();
        Path targetDirPath = imagePath.toPath();

        Path resolved = targetDirPath.resolve(newFile.getName());
        try {
            //boolean resNew = newFile.createNewFile();
            //Log.v(TAG, "create res=" + res);
            //Path newPath = Files.createFile(targetDirPath);
            //Files.move(sourcePath, targetDirPath.resolve(sourcePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            Path copyPath = Files.copy(sourcePath, targetDirPath.resolve(newFile.getName()), StandardCopyOption.REPLACE_EXISTING);
            //Files.move(sourcePath, targetDirPath.resolve(sourcePath.getFileName()), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            Log.e(TAG, "Failed to copy " + from + " to " + imagePath + " - " + ex.getMessage());

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Error")
                    .setMessage("Failed to copy image")
                    .setPositiveButton("OK", (dialog, which) ->
                            dialog.dismiss())
                    .show();
        }

        //res = from.delete();

//        ContentResolver contentResolver = getContentResolver();
//        long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
//        Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
//        int irest = contentResolver.delete(deleteUri, null, null);

        //Uri uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName()+".fileprovider", newFile);

        // process images
        //process(paths);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void exitingCamera(String path){

//        while (paths == null) {
//            try {
//                Thread.sleep(1000);
//                paths = getImagePaths(cursor, mPrevImageCount);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        File from = new File(path);

        File imagePath = new File(getExternalMediaDirs()[0], "images");
        //File imagePath = new File(getFilesDir(), "images");
        if (!imagePath.exists())
            imagePath.mkdir();

        SimpleDateFormat gmt = new SimpleDateFormat(FileDateHeaderFormat.GMT, Locale.getDefault());
        gmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        String currentDateTime = gmt.format(new Date()); // Today's date in GMT

        File newFile = new File(imagePath,
                //currentDateTime +
                        //"_phone_" + phoneID +
                        protocol + '_' +
                        subjectID + '_' +
                        assessment + '_' +
                        timelines[timeline] +
                        (side==EyeSide.Right? "_OD_" : "_OS_") +
                        (direction==EyeDirection.Front? "FrontFacing_" : "Superior_") +
                        "Dev"+ phoneID + '_' +
                        //userid + '_' +
                        currentDateTime +  ".jpg");

//        res = from.renameTo(newFile);

        Path sourcePath = from.toPath();
        Path targetDirPath = imagePath.toPath();

        Path resolved = targetDirPath.resolve(newFile.getName());
        try {
            //boolean resNew = newFile.createNewFile();
            //Log.v(TAG, "create res=" + res);
            //Path newPath = Files.createFile(targetDirPath);
            //Files.move(sourcePath, targetDirPath.resolve(sourcePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            Path copyPath = Files.copy(sourcePath, targetDirPath.resolve(newFile.getName()), StandardCopyOption.REPLACE_EXISTING);
            //Files.move(sourcePath, targetDirPath.resolve(sourcePath.getFileName()), StandardCopyOption.ATOMIC_MOVE);
            dailyLog.append("label:"+newFile.getName());
        } catch (IOException ex) {
            Log.e(TAG, "Failed to copy " + from + " to " + imagePath + " - " + ex.getMessage());
            dailyLog.append("label FAIL:"+newFile.getName());

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Error")
                    .setMessage("Failed to copy image")
                    .setPositiveButton("OK", (dialog, which) ->
                            dialog.dismiss())
                    .show();
        }

        AzureUploader.UploadImage(this, Uri.fromFile(newFile));

        //res = from.delete();

//        ContentResolver contentResolver = getContentResolver();
//        long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
//        Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
//        int irest = contentResolver.delete(deleteUri, null, null);

        //Uri uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName()+".fileprovider", newFile);

        // process images
        //process(paths);
    }

//    private void retrievePhoneId() {
//        //Check for ID
//        File pub = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        String storageState = getExternalStorageState();
//
//        if (!storageState.equals(android.os.Environment.MEDIA_MOUNTED)) {
//            dailyLog.append("getExternalStorageState:" + storageState);
//        }
//
//        //String[] files = pub.list(new MyFileNameFilter());
//        String[] files = pub.list((dir, name) -> {
//            return name.toLowerCase().endsWith(".png") && name.toLowerCase().startsWith("gp");
//        });
//
//        if (files == null || files.length == 0) {
//            dailyLog.append("Downloaded files:"+Arrays.toString(pub.list()));
//
//            new MaterialAlertDialogBuilder(this)
//                    .setTitle("No ID file found")
//                    .setMessage("Put a file name gp##.png in the Downloads folder")
//                    .setPositiveButton("OK", (dialog, which) ->
//                            dialog.dismiss())
//                    .setNegativeButton("Debug", (dialog, which) -> {
//                        new MaterialAlertDialogBuilder(this)
//                                .setTitle("files:"+files==null? "null": String.valueOf(files.length))
//                                .setMessage("pub="+pub.toString()+ " dir:" + pub.isDirectory() + " file list:"+Arrays.toString(pub.list()))
//                                .setPositiveButton("OK", (dialog2, which2) ->
//                                        dialog2.dismiss())
//                                .show();
//                    })
//                    .show();
//            phoneID = -1;
//        } else if (files.length > 1) {
//            new MaterialAlertDialogBuilder(this)
//                    .setTitle("Multiple ID files found")
//                    .setMessage("Make sure there's only one gp##.png in the Downloads folder")
//                    .setPositiveButton("OK", (dialog, which) ->
//                            dialog.dismiss())
//                    .show();
//            phoneID = -2;
//        } else {
//            phoneID = Integer.valueOf(files[0].substring(2, files[0].length() - 4));
//        }
//    }

    /**
     * Copy a given file of a filepath to storage associated with the application.
     * @param originalPath is a filepath, generally retrieved using Cursor and a URI
     * @param destinationPath
     */
    public void copyFile(String originalPath, String destinationPath) {
        File from = new File(originalPath);

        File filePath = new File(getExternalMediaDirs()[0], destinationPath);
        if (!filePath.exists())
            filePath.mkdir();

        SimpleDateFormat gmt = new SimpleDateFormat(FileDateHeaderFormat.GMT, Locale.getDefault());
        gmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        String currentDateTime = gmt.format(new Date()); // Today's date in GMT

        String fileExtension = "";
        if (destinationPath.contentEquals("videos")) {
            fileExtension = ".mp4";
        }

        else if (destinationPath.contentEquals("images")) {
            fileExtension = ".jpg";
        }

        File newFile = new File(filePath,
                //currentDateTime +
                //"_phone_" + phoneID +
                protocol + '_' +
                        subjectID + '_' +
                        assessment + '_' +
                        timelines[timeline] +
                        (side==EyeSide.Right? "_OD_" : "_OS_") +
                        (direction==EyeDirection.Front? "FrontFacing_" : "Superior_") +
                        "Dev"+ phoneID + '_' +
                        //userid + '_' +
                        currentDateTime +  fileExtension);

        Path sourcePath = from.toPath();
        Path targetDirPath = filePath.toPath();

        try {
            Path copyPath = Files.copy(sourcePath, targetDirPath.resolve(newFile.getName()), StandardCopyOption.REPLACE_EXISTING);
            dailyLog.append("label:"+newFile.getName());
        } catch (IOException ex) {
            Log.e(TAG, "Failed to copy " + from + " to " + filePath + " - " + ex.getMessage());
            dailyLog.append("label FAIL:"+newFile.getName());

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Error")
                    .setMessage("Failed to copy file")
                    .setPositiveButton("OK", (dialog, which) ->
                            dialog.dismiss())
                    .show();
        }
        sendFileThroughAzure(newFile);
    }

    public void sendFileThroughAzure(File aFile) {
        AzureUploader.UploadImage(this, Uri.fromFile(aFile));
    }
}