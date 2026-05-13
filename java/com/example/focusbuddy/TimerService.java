package com.example.focusbuddy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class TimerService extends Service {

    private static final String CHANNEL_ID = "TimerServiceChannel";
    private Handler handler = new Handler();
    private int seconds = 0;
    public static boolean isServiceRunning = false;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isServiceRunning) {
                seconds++;
                Log.d("TimerService", "Seconds: " + seconds);
                Intent intent = new Intent("TimerUpdate");
                intent.putExtra("seconds", seconds);
                intent.setPackage(getPackageName()); // ✅ Target only our app
                sendBroadcast(intent);
                updateNotification();
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TimerService", "Service Created");
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TimerService", "onStartCommand called");
        
        if (intent != null && "STOP".equals(intent.getAction())) {
            Log.d("TimerService", "Stopping Service");
            isServiceRunning = false;
            handler.removeCallbacks(runnable);
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!isServiceRunning) {
            isServiceRunning = true;
            if (intent != null) {
                seconds = intent.getIntExtra("initialSeconds", 0);
            }
            
            Log.d("TimerService", "Starting Foreground with seconds: " + seconds);
            
            Notification notification = getNotification("Timer is running...");
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
                } else {
                    startForeground(1, notification);
                }
            } catch (Exception e) {
                Log.e("TimerService", "Error starting foreground service", e);
                // Fallback if foreground type is rejected
                startForeground(1, notification);
            }
            
            handler.post(runnable);
        }

        return START_STICKY;
    }

    private void updateNotification() {
        int hrs = seconds / 3600;
        int mins = (seconds % 3600) / 60;
        int secs = seconds % 60;
        String time = String.format("%02d:%02d:%02d", hrs, mins, secs);
        
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null && isServiceRunning) {
            notificationManager.notify(1, getNotification("Study Time: " + time));
        }
    }

    private Notification getNotification(String contentText) {
        Intent notificationIntent = new Intent(this, TimerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Focus Mode Active")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d("TimerService", "Service Destroyed");
        isServiceRunning = false;
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
