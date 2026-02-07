package org.tcshare.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

/**
 * Created by FallRain on 2017/8/22.
 */

public class Notify {
    private static int MessageID = 0;

    public static void notifcation(Context context, CharSequence notificationTitle, CharSequence messageString){
        notifcation(context, notificationTitle, messageString, new Intent(), -1);
    }

    public static void notifcation(Context context, CharSequence notificationTitle, CharSequence messageString, Intent intent, int smallIconResID) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        long when = System.currentTimeMillis();
        String ticker = notificationTitle + " " + messageString;

        String channelId = null;
        // 8.0 以上需要特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel(context,"tcshare", "通知");
        } else {
            channelId = "";
        }

        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, 1000, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 1001, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }
        NotificationCompat.Builder notificationCompat = new NotificationCompat.Builder(context, channelId);
        notificationCompat.setAutoCancel(true)
                .setContentTitle(notificationTitle)
                .setContentIntent(pendingIntent)
                .setContentText(messageString)
                .setTicker(ticker)
                .setWhen(when);
        if(smallIconResID != -1){
            notificationCompat.setSmallIcon(smallIconResID);
        }else{
            notificationCompat.setSmallIcon(context.getApplicationInfo().icon);
        }

        Notification notification = notificationCompat.build();
        //display the notification
        mNotificationManager.notify(MessageID, notification);
        MessageID++;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String createNotificationChannel(Context ctx, String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }
}
