package com.example.aweather.receiver;

import com.example.aweather.service.AutoUpdateService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by Johnny on 2016/6/29.
 */
public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent intent_for_service = new Intent(context, AutoUpdateService.class);
        context.startService(intent_for_service);

    }
}
