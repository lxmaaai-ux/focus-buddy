package com.example.focusbuddy;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BlockScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_screen);
        Toast.makeText(this, "Apps are blocked during study time!", Toast.LENGTH_LONG).show();

        TextView msg = findViewById(R.id.blockMsg);
        Button backBtn = findViewById(R.id.backBtn);

        msg.setText("🚫 Focus Mode ON!\nGet back to study!");

        backBtn.setOnClickListener(v -> finish());
    }
}
