package com.example.justagram;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String what = intent != null ? intent.getStringExtra("what") : null;
        if (what == null) what = "Task";
        Toast.makeText(context, "Alarm fired: " + what, Toast.LENGTH_LONG).show();
    }
}