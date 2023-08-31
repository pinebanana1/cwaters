package com.ora.calmwaters.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;

import com.ora.calmwaters.OverlayService;
import calmwaters.R;

public class PopupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_popup);
        Log.i(ProtocolActivity.TAG, "PopupActivity");

        //Will turn on screen, but it's still locked
//        setTurnScreenOn(true);
//        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//        keyguardManager.requestDismissKeyguard(this, null);

        Ringtone r = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        r.setLooping(false); //Min 28 to not loop
        r.play();

        Handler handler = new Handler(Looper.getMainLooper());

        AlertDialog alertDialog = new AlertDialog.Builder(this)
            .setTitle(this.getString(R.string.app_name))
            .setMessage("Time to take some photos")
            .setPositiveButton("OK"/*\uD83D\uDCF7"*/, (dialog, which) -> {
                r.stop();
                handler.removeCallbacksAndMessages(null);
                //OverlayService.instance.startCamera(this);
                ProtocolActivity.isButtonEnabled = true;
                ProtocolActivity.hasAlarmGoneOff = true;
                finish();
                //startActivity(new Intent(this, MainActivity.class));
            })
            .setNeutralButton(ProtocolActivity.snoozeState==2? "Skip this photo session" : "Snooze 30 mins", (dialog, which) -> {
                r.stop();
                handler.removeCallbacksAndMessages(null);
                ProtocolActivity.snoozeState++;
                if (ProtocolActivity.snoozeState <= 2)
                    ProtocolActivity.snoozeMins += 30;
                ProtocolActivity.isAlarmed = false;
                onBackPressed();
            })
            .setCancelable(false)
            //.setIcon(android.R.drawable.ic_menu_camera)
            .show();

        //lertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(TypedValue.COMPLEX_UNIT_SP, 40.0f);
        alertDialog.getWindow().setGravity(Gravity.TOP);
        //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_camera, android.R.drawable.ic_menu_camera, android.R.drawable.ic_menu_camera,android.R.drawable.ic_menu_camera);

        //timeout after 3 mins
        handler.postDelayed(() -> {
            if (r.isPlaying()) {
                r.stop();
                ProtocolActivity.snoozeState++;
                if (ProtocolActivity.snoozeState < 2)
                    ProtocolActivity.snoozeMins += 30;
                ProtocolActivity.isAlarmed = false;
                onBackPressed();
            }
        }, 3 * 60*1000);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("OraAlarm", "AlarmReceiver:OnReceive");
            OverlayService.instance.enableClicks(true);
            context.startActivity(new Intent(context, PopupActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}