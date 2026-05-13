package com.example.focusbuddy;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    ListView historyListView;
    List<StudyEntry> studyEntries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyListView = findViewById(R.id.historyListView);
        loadHistory();
    }

    private void loadHistory() {
        SharedPreferences prefs = getSharedPreferences("study_data", MODE_PRIVATE);
        String historyJson = prefs.getString("study_history", "[]");

        try {
            JSONArray array = new JSONArray(historyJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                studyEntries.add(new StudyEntry(
                        obj.getString("date"),
                        obj.getString("time"),
                        obj.getString("subject"),
                        obj.getInt("duration")
                ));
            }
            // Show latest first
            Collections.reverse(studyEntries);
            
            HistoryAdapter adapter = new HistoryAdapter();
            historyListView.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class StudyEntry {
        String date, time, subject;
        int duration;

        StudyEntry(String date, String time, String subject, int duration) {
            this.date = date;
            this.time = time;
            this.subject = subject;
            this.duration = duration;
        }
    }

    class HistoryAdapter extends BaseAdapter {
        @Override
        public int getCount() { return studyEntries.size(); }
        @Override
        public Object getItem(int i) { return studyEntries.get(i); }
        @Override
        public long getItemId(int i) { return i; }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(HistoryActivity.this).inflate(R.layout.item_history, viewGroup, false);
            }

            TextView dateText = view.findViewById(R.id.historyDate);
            TextView subText = view.findViewById(R.id.historySubject);
            TextView durationText = view.findViewById(R.id.historyDuration);

            StudyEntry entry = studyEntries.get(i);
            dateText.setText(entry.date + " at " + entry.time);
            subText.setText("Studied: " + entry.subject);
            
            int mins = entry.duration / 60;
            int secs = entry.duration % 60;
            durationText.setText(String.format(Locale.getDefault(), "%02d:%02d mins", mins, secs));

            return view;
        }
    }
}
