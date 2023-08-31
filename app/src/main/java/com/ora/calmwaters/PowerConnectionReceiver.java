package com.ora.calmwaters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ora.calmwaters.ui.ProtocolActivity;

public class PowerConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("OraPower", "PowerConnectionReceiver:OnReceive");

        switch (intent.getAction()) {
//                case Intent.ACTION_BATTERY_LOW:
//                    if (!isSentBatteryLow) {
//                        message = botService.getString(R.string.battery_low);
//                        isSentBatteryLow = true;
//                    }
//                    break;
//                case Intent.ACTION_BATTERY_OKAY:
//                    isSentBatteryLow = false;
//                    break;
//                case Intent.ACTION_POWER_DISCONNECTED:
//                    message = botService.getString(R.string.power_disconnected) + getStatus();
//                    break;
            case Intent.ACTION_POWER_CONNECTED:
                context.startActivity(new Intent(context, ProtocolActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
        };
        //ProtocolActivity.isAlarmed = false;
        //context.startActivity(new Intent(context, PopupActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}