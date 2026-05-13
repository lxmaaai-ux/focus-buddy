package com.example.focusbuddy;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class TaskActivity extends AppCompatActivity {

    EditText taskInput;
    LinearLayout subtaskContainer;
    Button addTimeBtn, addSubtaskBtn;
    ListView taskList;

    ArrayList<String> tasks = new ArrayList<>();
    ArrayList<Integer> taskIds = new ArrayList<>();
    ArrayList<Integer> totalTimes = new ArrayList<>();

    // 🔥 IMPORTANT
    ArrayList<ArrayList<Boolean>> checkedStates = new ArrayList<>();
    HashMap<Integer, ArrayList<Boolean>> savedStates = new HashMap<>();

    BaseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        taskInput = findViewById(R.id.taskInput);
        subtaskContainer = findViewById(R.id.subtaskContainer);
        addSubtaskBtn = findViewById(R.id.addSubtaskBtn);
        addTimeBtn = findViewById(R.id.addTimeBtn);
        taskList = findViewById(R.id.taskList);

        addSubtaskField();

        addSubtaskBtn.setOnClickListener(v -> addSubtaskField());

        adapter = new BaseAdapter() {

            @Override public int getCount() { return tasks.size(); }
            @Override public Object getItem(int i) { return tasks.get(i); }
            @Override public long getItemId(int i) { return i; }

            @Override
            public View getView(int i, View view, ViewGroup parent) {

                view = getLayoutInflater().inflate(R.layout.item_task, null);

                TextView title = view.findViewById(R.id.taskTitle);
                LinearLayout subLayout = view.findViewById(R.id.subtaskLayout);
                TextView deleteBtn = view.findViewById(R.id.deleteBtn);
                ProgressBar progressBar = view.findViewById(R.id.progressBar);

                int position = i;

                String[] parts = tasks.get(i).split("\n• ");

                title.setText(parts[0] + "\n⏱ Total: " + totalTimes.get(position) + " min");

                subLayout.removeAllViews();

                for (int j = 1; j < parts.length; j++) {

                    int subIndex = j - 1;

                    CheckBox cb = new CheckBox(TaskActivity.this);
                    cb.setText(parts[j]);
                    cb.setTextColor(getResources().getColor(R.color.white));

                    cb.setChecked(checkedStates.get(position).get(subIndex));

                    if (cb.isChecked()) {
                        cb.setPaintFlags(cb.getPaintFlags() |
                                android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                    }

                    cb.setOnClickListener(v -> {

                        if (!cb.isChecked()) {
                            checkedStates.get(position).set(subIndex, false);
                            cb.setPaintFlags(0);

                            savedStates.put(taskIds.get(position), checkedStates.get(position));
                            updateProgress(progressBar, position, parts);
                            return;
                        }

                        EditText input = new EditText(TaskActivity.this);
                        input.setHint("Enter time in minutes");

                        new AlertDialog.Builder(TaskActivity.this)
                                .setTitle("Time Spent")
                                .setMessage("Enter time spent for this subtopic")
                                .setView(input)
                                .setPositiveButton("OK", (dialog, which) -> {

                                    String t = input.getText().toString().trim();

                                    if (t.isEmpty()) {
                                        Toast.makeText(TaskActivity.this, "Enter time", Toast.LENGTH_SHORT).show();
                                        cb.setChecked(false);
                                        return;
                                    }

                                    int time = Integer.parseInt(t);

                                    totalTimes.set(position,
                                            totalTimes.get(position) + time);

                                    checkedStates.get(position).set(subIndex, true);

                                    savedStates.put(taskIds.get(position), checkedStates.get(position));

                                    cb.setPaintFlags(cb.getPaintFlags() |
                                            android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);

                                    title.setText(parts[0] +
                                            "\n⏱ Total: " + totalTimes.get(position) + " min");

                                    updateProgress(progressBar, position, parts);

                                })
                                .setNegativeButton("Cancel", (d, w) -> cb.setChecked(false))
                                .show();
                    });

                    subLayout.addView(cb);
                }

                updateProgress(progressBar, position, parts);

                deleteBtn.setOnClickListener(v -> deleteTask(taskIds.get(position)));

                return view;
            }
        };

        taskList.setAdapter(adapter);

        loadTasks();

        // ✅ DONE BUTTON
        addTimeBtn.setOnClickListener(v -> {

            String task = taskInput.getText().toString().trim();

            if (task.isEmpty()) {
                Toast.makeText(this, "Enter task", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder subBuilder = new StringBuilder();

            for (int i = 0; i < subtaskContainer.getChildCount(); i++) {
                EditText et = (EditText) subtaskContainer.getChildAt(i);
                String text = et.getText().toString().trim();

                if (!text.isEmpty()) {
                    subBuilder.append(text).append(",");
                }
            }

            String sub = subBuilder.toString();

            // 🔥 SAVE CURRENT STATE BEFORE RELOAD
            for (int i = 0; i < taskIds.size(); i++) {
                savedStates.put(taskIds.get(i), checkedStates.get(i));
            }

            new Thread(() -> {
                try {
                    URL url = new URL(
                            Config.BASE_URL + "add_task.php?task="
                                    + java.net.URLEncoder.encode(task, "UTF-8") 
                                    + "&subtask=" + java.net.URLEncoder.encode(sub, "UTF-8") 
                                    + "&time=0"
                    );

                    url.openStream();

                    runOnUiThread(() -> {

                        Toast.makeText(this, "Task Saved", Toast.LENGTH_SHORT).show();

                        loadTasks();

                        taskInput.setText("");
                        subtaskContainer.removeAllViews();
                        addSubtaskField();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();
        });
    }

    private void updateProgress(ProgressBar progressBar, int position, String[] parts) {

        int totalSubtasks = parts.length - 1;
        int completed = 0;

        for (boolean b : checkedStates.get(position)) {
            if (b) completed++;
        }

        int progress = totalSubtasks > 0 ? (completed * 100) / totalSubtasks : 0;
        progressBar.setProgress(progress);
    }

    private void addSubtaskField() {
        EditText et = new EditText(this);
        et.setHint("Subtopic");
        et.setTextColor(getResources().getColor(R.color.white));
        et.setHintTextColor(getResources().getColor(R.color.hint));
        et.setBackground(getDrawable(R.drawable.input_bg));
        et.setPadding(12, 12, 12, 12);

        subtaskContainer.addView(et);
    }

    private void loadTasks() {
        new Thread(() -> {
            try {
                URL url = new URL(Config.BASE_URL + "get_tasks.php");

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(url.openStream())
                );

                StringBuilder json = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    json.append(line);
                }

                JSONArray arr = new JSONArray(json.toString());

                tasks.clear();
                taskIds.clear();
                totalTimes.clear();
                checkedStates.clear();

                for (int i = 0; i < arr.length(); i++) {

                    JSONObject obj = arr.getJSONObject(i);

                    String task = obj.getString("task");
                    String sub = obj.getString("subtask");

                    String[] subs = sub.split(",");

                    StringBuilder formatted = new StringBuilder();
                    ArrayList<Boolean> subChecks = new ArrayList<>();

                    for (String s : subs) {
                        if (!s.isEmpty()) {
                            formatted.append("\n• ").append(s);
                            subChecks.add(false);
                        }
                    }

                    int id = obj.getInt("id");

                    tasks.add(task + formatted);
                    taskIds.add(id);
                    totalTimes.add(0);

                    if (savedStates.containsKey(id)) {
                        checkedStates.add(savedStates.get(id));
                    } else {
                        checkedStates.add(subChecks);
                    }
                }

                runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void deleteTask(int id) {
        new Thread(() -> {
            try {
                URL url = new URL(
                        Config.BASE_URL + "delete_task.php?id=" + id
                );
                url.openStream();

                runOnUiThread(this::loadTasks);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}