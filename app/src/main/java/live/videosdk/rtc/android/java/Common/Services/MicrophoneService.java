package live.videosdk.rtc.android.java.Common.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MicrophoneService extends Service {
    private static final String NOTIFICATION_CHANNEL_ID = "1001";
    private static final int NOTIFICATION_ID = 1001;
    private static final String NOTIFICATION_CHANNEL_DESC = "Microphone notification Channel";

    private final IBinder mBinder = new MicrophoneService.LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        MicrophoneService getService() {
            return MicrophoneService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        generateForegroundNotification();
        return START_STICKY;
    }

    private void generateForegroundNotification() {
        Log.d("TAG", "generateForegroundNotification: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Call Start foreground with notification
            Intent notificationIntent = new Intent(this, getApplication().getClass());
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            ApplicationInfo ai = getApplicationInfo();
            String notificationTitle = null;
            String notificationContent = null;
            int resourceId = 0;

            try {
                ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
                if (ai != null) {
                    Bundle bundle = ai.metaData;
                    if (bundle != null) {
                        notificationTitle = bundle.getString("notificationTitle");
                        notificationContent = bundle.getString("notificationContent");
                        resourceId = bundle.getInt("notificationIcon");
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (notificationTitle == null) {
                notificationTitle = "VideoSDK RTC is sharing your screen";
            }
            if (notificationContent == null) {
                notificationContent = "meeting is running";
            }

            if (resourceId == 0) {
                resourceId = getApplicationInfo().icon;
            }

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(notificationTitle)
                    .setSmallIcon(resourceId)
                    .setContentText(notificationContent)
                    .setContentIntent(pendingIntent);
            Notification notification = notificationBuilder.build();
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Microphone notification Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(NOTIFICATION_CHANNEL_DESC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(NOTIFICATION_ID, notification);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
        }
    }

    @Override
    public void onDestroy() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}