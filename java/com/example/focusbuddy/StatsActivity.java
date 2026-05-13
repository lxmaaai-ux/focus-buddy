package com.example.focusbuddy;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class StatsActivity extends AppCompatActivity {

    BarChart weekChart, monthChart;
    TextView todayText, monthText;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        todayText = findViewById(R.id.todayText);
        monthText = findViewById(R.id.monthText);
        weekChart = findViewById(R.id.weekChart);
        monthChart = findViewById(R.id.monthChart);

        prefs = getSharedPreferences("study_data", MODE_PRIVATE);

        loadRealData();
    }

    private void loadRealData() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        SimpleDateFormat displaySdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        // 1. TODAY'S DATA
        String todayKey = sdf.format(cal.getTime());
        int todaySeconds = prefs.getInt(todayKey, 0);
        todayText.setText(formatTime(todaySeconds));

        // 2. WEEKLY DATA (Last 7 days)
        ArrayList<BarEntry> weekEntries = new ArrayList<>();
        String[] weekLabels = new String[7];
        
        for (int i = 6; i >= 0; i--) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, -i);
            String key = sdf.format(c.getTime());
            int seconds = prefs.getInt(key, 0);
            float minutes = seconds / 60f;
            weekEntries.add(new BarEntry(6 - i, minutes));
            weekLabels[6 - i] = displaySdf.format(c.getTime());
        }
        setupChart(weekChart, weekEntries, weekLabels, "Study Minutes", Color.parseColor("#FFC107"));

        // 3. MONTHLY DATA (Last 30 days)
        ArrayList<BarEntry> monthEntries = new ArrayList<>();
        long totalMonthSeconds = 0;
        
        for (int i = 29; i >= 0; i--) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, -i);
            String key = sdf.format(c.getTime());
            int seconds = prefs.getInt(key, 0);
            totalMonthSeconds += seconds;
            float hours = seconds / 3600f;
            monthEntries.add(new BarEntry(29 - i, hours));
        }
        monthText.setText(formatTime((int)totalMonthSeconds));
        setupChart(monthChart, monthEntries, null, "Study Hours", Color.parseColor("#2ECC71"));
    }

    private void setupChart(BarChart chart, ArrayList<BarEntry> entries, String[] labels, String label, int color) {
        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        chart.setData(data);

        // Styling
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.setFitBars(true);
        chart.animateY(1000);

        // X-Axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        if (labels != null) {
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    if (index >= 0 && index < labels.length) return labels[index];
                    return "";
                }
            });
            xAxis.setLabelCount(labels.length);
        }

        // Y-Axis
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setEnabled(false);
        
        chart.invalidate();
    }

    private String formatTime(int totalSeconds) {
        int hrs = totalSeconds / 3600;
        int mins = (totalSeconds % 3600) / 60;
        int secs = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs);
    }
}
