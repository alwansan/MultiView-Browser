package com.alwansan.multiview;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

/**
 * Opt-in same-process foreground service for an actively requested persistent
 * multi-page browser session. This increases process importance, not immunity
 * to LMK, WebView renderer death, device reboot or user force-stop.
 */
public final class SessionKeepAliveService extends Service {
    private static final String CHANNEL="multiview_session";
    private static final int ID=1701;
    public static final String ACTION_STOP="com.alwansan.multiview.STOP_SESSION";
    @Override public void onCreate(){
        super.onCreate();
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel channel=new NotificationChannel(CHANNEL,
                getString(R.string.keep_session),NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
                .createNotificationChannel(channel);
        }
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent!=null && ACTION_STOP.equals(intent.getAction())){
            getSharedPreferences("session_guard",MODE_PRIVATE).edit()
                .putBoolean("enabled",false).apply();
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }
        Intent open=new Intent(this,MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int pendingFlags=PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE;
        PendingIntent view=PendingIntent.getActivity(this,0,open,pendingFlags);
        Intent stop=new Intent(this,SessionKeepAliveService.class).setAction(ACTION_STOP);
        PendingIntent stopAction=PendingIntent.getService(this,1,stop,pendingFlags);
        Notification.Builder b=Build.VERSION.SDK_INT>=26
            ?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        b.setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.keep_session))
            .setContentText(getString(R.string.keep_session_notification))
            .setContentIntent(view)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.disable),stopAction);
        if(Build.VERSION.SDK_INT>=34) {
            startForeground(ID,b.build(),ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        }else startForeground(ID,b.build());
        return START_NOT_STICKY;
    }
    @Override public IBinder onBind(Intent intent){return null;}
}