package com.example.e_library.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e_library.R;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends BaseAuthActivity {


    @Override
    protected boolean shouldBeLoggedIn() {
        return false; // This screen should only be accessible when logged out
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else {
            setContentView(R.layout.activity_main);

            MaterialButton signInButton = findViewById(R.id.signInButton);
            MaterialButton signUpButton = findViewById(R.id.signUpButton);

            signInButton.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            signUpButton.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}