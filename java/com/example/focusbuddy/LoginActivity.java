package com.example.focusbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;
    TextView goRegister, forgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔥 AUTO LOGIN CHECK
        SharedPreferences session = getSharedPreferences("user_session", MODE_PRIVATE);
        if (session.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        goRegister = findViewById(R.id.goRegister);
        forgot = findViewById(R.id.forgot);
        loginBtn = findViewById(R.id.loginBtn);

        // 🔵 LOGIN BUTTON
        loginBtn.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            String userPassword = password.getText().toString().trim();

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    URL url = new URL(Config.BASE_URL + "login.php");
                    Log.d("Login", "Attempting login to: " + url.toString());

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.setDoOutput(true);

                    // ✅ URL Encode parameters
                    String data = "email=" + URLEncoder.encode(userEmail, "UTF-8") +
                                 "&password=" + URLEncoder.encode(userPassword, "UTF-8");

                    OutputStream os = conn.getOutputStream();
                    os.write(data.getBytes());
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    Log.d("Login", "Response Code: " + responseCode);

                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(
                                    responseCode == 200 ? conn.getInputStream() : conn.getErrorStream()
                            )
                    );

                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    String res = response.toString().trim();
                    Log.d("Login", "Server Response: " + res);

                    runOnUiThread(() -> {
                        if (res.isEmpty()) {
                            Toast.makeText(this, "Server returned empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        switch (res) {
                            case "success":
                                SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
                                prefs.edit()
                                        .putBoolean("isLoggedIn", true)
                                        .putString("userEmail", userEmail)
                                        .apply();
                                Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, DashboardActivity.class));
                                finish();
                                break;
                            case "wrong_password":
                                Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();
                                break;
                            case "no_user":
                                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(this, "Server: " + res, Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (Exception e) {
                    Log.e("Login", "Error: ", e);
                    runOnUiThread(() ->
                            Toast.makeText(this, "Connection Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();
        });

        // 🟣 REGISTER
        goRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        // 🔴 FORGOT
        forgot.setOnClickListener(v ->
                Toast.makeText(this, "Reset feature coming soon", Toast.LENGTH_SHORT).show()
        );
    }
}
