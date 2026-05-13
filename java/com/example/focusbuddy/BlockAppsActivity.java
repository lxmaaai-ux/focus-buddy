package com.example.focusbuddy;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import java.util.*;

public class BlockAppsActivity extends AppCompatActivity {

    ListView listView;
    List<AppModel> appList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_apps);

        listView = findViewById(R.id.appListView);

        // 🔥 MANUAL LIST (DISTRACTING APPS)

        appList.add(new AppModel("Instagram", "com.instagram.android", R.drawable.instagram));
        appList.add(new AppModel("YouTube", "com.google.android.youtube", R.drawable.youtube));
        appList.add(new AppModel("Snapchat", "com.snapchat.android", R.drawable.snapchat));
        appList.add(new AppModel("Facebook", "com.facebook.katana", R.drawable.facebook));
        appList.add(new AppModel("WhatsApp", "com.whatsapp", R.drawable.whatsapp));
        appList.add(new AppModel("Chrome", "com.android.chrome", R.drawable.chrome));

        AppAdapter adapter = new AppAdapter(this, appList);
        listView.setAdapter(adapter);

        Button saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Selected apps will be blocked during study!", Toast.LENGTH_SHORT).show();
        });

        saveBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Apps saved!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
