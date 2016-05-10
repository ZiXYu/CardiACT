package com.example.osorekoxuan.cardiact;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat.MediaStyle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.media.session.MediaSession;
import android.widget.Toast;

import java.util.List;


/**
 * Created by osorekoxuan on 16/3/25.
 */
public class NotificationUtils {

    private String TAG = NotificationUtils.class.getSimpleName();

    private Context mContext;

    public NotificationUtils() {
    }

    public NotificationUtils(Context mContext) {
        this.mContext = mContext;
    }

    public void showNotificationMessage(String title, String message, String objectId, Intent intent) {

        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return;
        if (isAppIsInBackground(mContext)) {
           // notification icon
            int icon = R.drawable.aed_logo;

            int mNotificationId = 100;

            Bundle b = new Bundle();
            b.putString("objectId", objectId);
            intent.putExtras(b);

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            mContext,
                            0,
                            intent,
                            PendingIntent.FLAG_CANCEL_CURRENT
                    );

            Intent intent2 = new Intent(mContext, MainActivity.class);

            PendingIntent buttonIntent2 =
                    PendingIntent.getActivity(
                            mContext,
                            0,
                            intent2,
                            PendingIntent.FLAG_CANCEL_CURRENT
                    );


            /*MediaStyle mediaStyle=new MediaStyle();
            MediaSessionCompat mediaSession=new MediaSessionCompat(mContext,TAG);*/
            //MediaSession mediaSession = new MediaSession(mContext,TAG);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    mContext);
            Notification notification = mBuilder.setSmallIcon(icon)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setAutoCancel(false)
                    //.addAction(R.drawable.ic_done_24dp, "Accept", buttonIntent1) // #0
                    .addAction(R.drawable.ic_close_24dp, "Decline", buttonIntent2)  // #1
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(resultPendingIntent)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .build();
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(mNotificationId, notification);
        }else{
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(mContext, notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Bundle b = new Bundle();
            b.putString("objectId", objectId);
            intent.putExtras(b);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mContext.startActivity(intent);
        }
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
}
