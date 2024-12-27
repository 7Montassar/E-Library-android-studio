package com.example.e_library.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.e_library.utils.SessionManager;

public abstract class BaseAuthActivity extends AppCompatActivity {
    protected SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        checkAuthState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuthState();
    }

    private void checkAuthState() {
        boolean isLoggedIn = sessionManager.isLoggedIn();
        if (shouldBeLoggedIn() && !isLoggedIn) {
            // User needs to be logged in but isn't
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else if (!shouldBeLoggedIn() && isLoggedIn) {
            // User is logged in but shouldn't be on this screen
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    // Override this in each activity
    protected abstract boolean shouldBeLoggedIn();
}