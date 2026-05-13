package com.example.focusbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    EditText email, password, confirmPassword;
    Button registerBtn;
    TextView goLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 🔹 Bind UI
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        registerBtn = findViewById(R.id.registerBtn);
        goLogin = findViewById(R.id.goLogin);

        // 🔵 REGISTER BUTTON
        registerBtn.setOnClickListener(v -> {

            String userEmail = email.getText().toString().trim();
            String userPass = password.getText().toString().trim();
            String confirmPass = confirmPassword.getText().toString().trim();

            // ✅ Validation
            if (userEmail.isEmpty() || userPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!userPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔥 API CALL
            new Thread(() -> {
                try {
                    URL url = new URL(Config.BASE_URL + "register.php");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    String data = "email=" + userEmail + "&password=" + userPass;

                    OutputStream os = conn.getOutputStream();
                    os.write(data.getBytes());
                    os.flush();

                    int responseCode = conn.getResponseCode();

                    runOnUiThread(() -> {
                        if (responseCode == 200) {
                            Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();

                            // 🔄 Go to login page
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }).start();
        });

        // 🔵 GO BACK TO LOGIN
        goLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
