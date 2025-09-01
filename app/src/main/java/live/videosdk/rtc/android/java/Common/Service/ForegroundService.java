package live.videosdk.rtc.android.java.Common.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import live.videosdk.rtc.android.java.Common.MainApplication;
import live.videosdk.rtc.android.java.R;

public class ForegroundService extends Service {

    private static final String TAG = "ForegroundService";
    public static final int NOTIFICATION_ID = 1001;
    public static final String CHANNEL_ID = "AppChannelID";

    // Actions
    public static final String ACTION_START = "live.videosdk.rtc.android.java.Common.Service.START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP = "live.videosdk.rtc.android.java.Common.Service.STOP_FOREGROUND_SERVICE";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate");
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand");

        if (intent != null) {
            switch (intent.getAction()) {
                case ACTION_START:
                    startForegroundService();
                    break;
                case ACTION_STOP:
                    stopForegroundService();
                    break;
                default:
                    // Default action - start the service
                    startForegroundService();
                    break;
            }
        } else {
            // Default action - start the service
            startForegroundService();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service onDestroy");
    }

    private void startForegroundService() {
        Log.d(TAG, "Starting foreground service");

        try {
            Notification notification = createNotification();
            startForeground(NOTIFICATION_ID, notification);
            Log.d(TAG, "Foreground service started successfully");
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception while starting foreground service", e);
            // This usually means missing permissions
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "Error starting foreground service", e);
            throw e;
        }
    }

    public void stopForegroundService() {
        Log.d(TAG, "Stopping foreground service");
        stopForeground(true);
        stopSelf();
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainApplication.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("Camera and microphone are active")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Channel for foreground service notifications");
            channel.setShowBadge(false);
            channel.enableLights(false);
            channel.enableVibration(false);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }
}
