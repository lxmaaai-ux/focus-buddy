package com.example.focusbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

public class DashboardActivity extends AppCompatActivity {

    Button addTaskBtn, aiBtn, startTimerBtn, statsBtn;
    TextView totalTimeText, logoutBtn;
    ProgressBar dailyProgress;
    TextView streakText, levelText;
    ImageButton menuBtn;

    SharedPreferences prefs;
    int DAILY_GOAL = 240 * 60; // 4 hours

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        menuBtn = findViewById(R.id.menuBtn);
        addTaskBtn = findViewById(R.id.addTaskBtn);
        aiBtn = findViewById(R.id.aiBtn);
        startTimerBtn = findViewById(R.id.startTimerBtn);
        statsBtn = findViewById(R.id.viewStatusBtn);
        totalTimeText = findViewById(R.id.totalTimeText);
        logoutBtn = findViewById(R.id.logoutBtn);
        dailyProgress = findViewById(R.id.dailyProgress);
        streakText = findViewById(R.id.streakText);
        levelText = findViewById(R.id.levelText);
        Button blockAppsBtn = findViewById(R.id.blockAppsBtn);

        prefs = getSharedPreferences("study_data", MODE_PRIVATE);

        // 🔥 MENU BUTTON CLICK
        menuBtn.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(DashboardActivity.this, menuBtn);
            popup.getMenu().add("Study History");
            popup.getMenu().add("Logout");
            
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Study History")) {
                    startActivity(new Intent(DashboardActivity.this, HistoryActivity.class));
                    return true;
                } else if (item.getTitle().equals("Logout")) {
                    logout();
                    return true;
                }
                return false;
            });
            popup.show();
        });

        addTaskBtn.setOnClickListener(v -> startActivity(new Intent(this, TaskActivity.class)));
        aiBtn.setOnClickListener(v -> startActivity(new Intent(this, AiActivity.class)));
        startTimerBtn.setOnClickListener(v -> startActivity(new Intent(this, TimerActivity.class)));
        statsBtn.setOnClickListener(v -> startActivity(new Intent(this, StatsActivity.class)));
        blockAppsBtn.setOnClickListener(v -> startActivity(new Intent(this, BlockAppsActivity.class)));
        logoutBtn.setOnClickListener(v -> logout());
    }

    private void logout() {
        SharedPreferences session = getSharedPreferences("user_session", MODE_PRIVATE);
        session.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String dateKey = android.text.format.DateFormat.format("yyyyMMdd", System.currentTimeMillis()).toString();
        int todayTime = prefs.getInt(dateKey, 0);

        totalTimeText.setText("Today Study Time: " + formatTime(todayTime));
        int percent = (int) ((todayTime * 100.0f) / DAILY_GOAL);
        dailyProgress.setProgress(Math.min(percent, 100));
        streakText.setText("🔥 Streak: " + prefs.getInt("streak", 0) + " days");
        int xp = prefs.getInt("xp", 0);
        levelText.setText("🎮 Level: " + (xp / 100) + " (XP: " + xp + ")");
    }

    private String formatTime(int totalSeconds) {
        int hrs = totalSeconds / 3600;
        int mins = (totalSeconds % 3600) / 60;
        int secs = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hrs, mins, secs);
    }
}
