package com.example.justagram;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public abstract class ScheduleWork extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent)
    {
        Toast.makeText(ctx, "Schedule task executed!",Toast.LENGTH_LONG).show();
    }
}


