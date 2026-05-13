package com.example.focusbuddy;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import java.util.*;

public class AppBlockService extends Service {

    private boolean isRunning = false;
    private Thread monitorThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            startMonitoring();
        }
        return START_STICKY;
    }

    private void startMonitoring() {
        monitorThread = new Thread(() -> {
            try {
                UsageStatsManager usm = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
                SharedPreferences prefs = getSharedPreferences("block", MODE_PRIVATE);

                while (isRunning) {
                    Set<String> blockedApps = prefs.getStringSet("apps", new HashSet<>());
                    long time = System.currentTimeMillis();

                    // Query stats for the last 10 seconds
                    List<UsageStats> stats = usm.queryUsageStats(
                            UsageStatsManager.INTERVAL_DAILY,
                            time - 10000,
                            time
                    );

                    if (stats != null && !stats.isEmpty()) {
                        UsageStats recent = null;
                        for (UsageStats s : stats) {
                            if (recent == null || s.getLastTimeUsed() > recent.getLastTimeUsed()) {
                                recent = s;
                            }
                        }

                        if (recent != null) {
                            String currentApp = recent.getPackageName();
                            
                            // ✅ CRITICAL FIX: Ensure FocusBuddy (own package) is NEVER blocked
                            // And ensure we don't block the launcher or system UI if not intended
                            if (!currentApp.equals(getPackageName()) && 
                                !currentApp.equals("com.android.systemui") && 
                                blockedApps.contains(currentApp)) {
                                
                                Log.d("AppBlockService", "Blocking app: " + currentApp);
                                Intent i = new Intent(this, BlockScreenActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                            }
                        }
                    }
                    Thread.sleep(1000); // Check every second
                }
            } catch (InterruptedException e) {
                Log.d("AppBlockService", "Monitoring thread interrupted");
            } catch (Exception e) {
                Log.e("AppBlockService", "Error in monitoring thread", e);
            }
        });
        monitorThread.start();
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (monitorThread != null) {
            monitorThread.interrupt();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
