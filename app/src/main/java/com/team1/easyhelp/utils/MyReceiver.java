package com.team1.easyhelp.utils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.team1.easyhelp.receive.SOSReceiveMapActivity;

import cn.jpush.android.api.JPushInterface;


public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "broadcastreceivertest";
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d(TAG, "onReceive - " + intent.getAction());

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            //System.out.println("收到了自定义消息。消息内容是：" + bundle.getString(JPushInterface.EXTRA_MESSAGE));
            // 自定义消息不会展示在通知栏，完全要开发者写代码去处理
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            //System.out.println("收到了通知");
            // 在这里可以做些统计，或者做些其他工作
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            //System.out.println("用户点击打开了通知");
            // 在这里可以自己写代码去定义用户点击后的行为
            int event_id = receivingNotification(context,bundle);

            Intent i = new Intent(context, SOSReceiveMapActivity.class);  //自定义打开的界面

            //Bundle mBundle = new Bundle();
            //mBundle.putInt("event_id", event_id);
            i.putExtra("event_id", event_id);

            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

        } else {
            Log.d(TAG, "Unhandled intent - " + intent.getAction());
        }
    }
    private int receivingNotification(Context context, Bundle bundle){
        String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
        Log.v(TAG, " title : " + title);
        String message = bundle.getString(JPushInterface.EXTRA_ALERT);
        Log.v(TAG, "message : " + message);
        int i = message.indexOf("：");
        String s = String.valueOf(i);
        Log.v(TAG, s);
        String ss = message.substring(i+1);
        Log.v(TAG, ss);
        int res = Integer.parseInt(ss);
        return res;
    }
}
