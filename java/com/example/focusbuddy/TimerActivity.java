package com.example.focusbuddy;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimerActivity extends AppCompatActivity {

    TextView timerText;
    EditText subjectInput;
    Button startBtn, stopBtn, resetBtn, saveBtn;

    int currentSeconds = 0;
    boolean isRunning = false;

    private BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentSeconds = intent.getIntExtra("seconds", 0);
            updateTimerUI(currentSeconds);
        }
    };

    private void updateTimerUI(int seconds) {
        int hrs = seconds / 3600;
        int mins = (seconds % 3600) / 60;
        int secs = seconds % 60;
        timerText.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs));
    }

    private boolean hasPermissions() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
                
        boolean hasUsageStats = (mode == AppOpsManager.MODE_ALLOWED);
        
        boolean hasOverlay = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasOverlay = Settings.canDrawOverlays(this);
        }
        
        return hasUsageStats && hasOverlay;
    }

    private void requestPermissions() {
        Toast.makeText(this, "Please grant required permissions.", Toast.LENGTH_LONG).show();
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        timerText = findViewById(R.id.timerText);
        subjectInput = findViewById(R.id.subjectInput);
        startBtn = findViewById(R.id.startBtn);
        stopBtn = findViewById(R.id.stopBtn);
        resetBtn = findViewById(R.id.resetBtn);
        saveBtn = findViewById(R.id.saveBtn);

        // START
        startBtn.setOnClickListener(v -> {
            if (!hasPermissions()) {
                requestPermissions();
                return;
            }
            if (!TimerService.isServiceRunning) {
                Intent serviceIntent = new Intent(this, TimerService.class);
                serviceIntent.putExtra("initialSeconds", currentSeconds);
                ContextCompat.startForegroundService(this, serviceIntent);
                
                startService(new Intent(this, AppBlockService.class));
                isRunning = true;
                Toast.makeText(this, "Focus Mode ON!", Toast.LENGTH_SHORT).show();
            }
        });

        // STOP
        stopBtn.setOnClickListener(v -> {
            stopTimerAndBlocker();
            Toast.makeText(this, "Focus Mode OFF!", Toast.LENGTH_SHORT).show();
        });

        // RESET
        resetBtn.setOnClickListener(v -> {
            stopTimerAndBlocker();
            currentSeconds = 0;
            updateTimerUI(0);
        });

        // SAVE TIME
        saveBtn.setOnClickListener(v -> {
            if (currentSeconds <= 0) {
                Toast.makeText(this, "No time to save!", Toast.LENGTH_SHORT).show();
                return;
            }

            String subject = subjectInput.getText().toString().trim();
            if (subject.isEmpty()) {
                Toast.makeText(this, "Please enter what you studied!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get SharedPreferences
            SharedPreferences studyPrefs = getSharedPreferences("study_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = studyPrefs.edit();

            // 1. Get Today's Key
            Calendar cal = Calendar.getInstance();
            String dateKey = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.getTime());
            String displayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.getTime());
            String displayTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.getTime());

            // 2. Update Daily Time
            int oldDailyTime = studyPrefs.getInt(dateKey, 0);
            editor.putInt(dateKey, oldDailyTime + currentSeconds);

            // 3. Update History
            try {
                String historyJson = studyPrefs.getString("study_history", "[]");
                JSONArray historyArray = new JSONArray(historyJson);
                
                JSONObject entry = new JSONObject();
                entry.put("date", displayDate);
                entry.put("time", displayTime);
                entry.put("subject", subject);
                entry.put("duration", currentSeconds);
                
                historyArray.put(entry);
                editor.putString("study_history", historyArray.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 4. Update XP
            int xpEarned = currentSeconds / 60;
            int currentXp = studyPrefs.getInt("xp", 0);
            editor.putInt("xp", currentXp + xpEarned);

            // 5. Update Streak
            if (oldDailyTime == 0) {
                int streak = studyPrefs.getInt("streak", 0);
                editor.putInt("streak", streak + 1);
            }

            editor.apply();

            Toast.makeText(this, "Time Saved! XP Earned: " + xpEarned, Toast.LENGTH_SHORT).show();
            
            stopTimerAndBlocker();
            currentSeconds = 0;
            updateTimerUI(0);
            subjectInput.setText("");
            finish(); // Go back to dashboard
        });
    }

    private void stopTimerAndBlocker() {
        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.setAction("STOP");
        startService(stopIntent);
        
        stopService(new Intent(this, AppBlockService.class));
        isRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        int receiverFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? ContextCompat.RECEIVER_NOT_EXPORTED : 0;
        ContextCompat.registerReceiver(this, timerReceiver, new IntentFilter("TimerUpdate"), receiverFlags);

        if (TimerService.isServiceRunning) {
            isRunning = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(timerReceiver);
    }
}
