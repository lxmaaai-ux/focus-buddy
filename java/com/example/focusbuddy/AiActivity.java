package com.example.focusbuddy;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class AiActivity extends AppCompatActivity {

    EditText planInput;
    Button generateBtn;
    TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        planInput = findViewById(R.id.planInput);
        generateBtn = findViewById(R.id.generateBtn);
        resultText = findViewById(R.id.resultText);

        generateBtn.setOnClickListener(v -> {

            String userPlan = planInput.getText().toString().trim();

            if (userPlan.isEmpty()) {
                Toast.makeText(this, "Enter your routine", Toast.LENGTH_SHORT).show();
                return;
            }

            resultText.setText("Generating... ⏳");

            new Thread(() -> {
                try {

                    String urlStr = Config.BASE_URL + "ai.php?plan="
                            + URLEncoder.encode(userPlan, "UTF-8");

                    Log.d("API_URL", urlStr); // debug

                    URL url = new URL(urlStr);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream())
                    );

                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }

                    br.close();

                    runOnUiThread(() -> resultText.setText(result.toString()));

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();
        });
    }
}