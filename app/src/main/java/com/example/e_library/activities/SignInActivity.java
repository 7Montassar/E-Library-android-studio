package com.example.e_library.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_library.activities.HomeActivity;
import androidx.appcompat.app.AppCompatActivity;

import com.example.e_library.R;
import com.example.e_library.network.ApiService;
import com.example.e_library.network.RetrofitClient;
import com.example.e_library.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends BaseAuthActivity {
    private SessionManager sessionManager;


    private static final String TAG = "SignInActivity";

    private TextInputEditText emailEditText, passwordEditText;
    private MaterialButton signInButton;
    private TextView signUpLinkTextView;   // e.g. a link to go to SignUpActivity

    private ApiService apiService;

    @Override
    protected boolean shouldBeLoggedIn() {
        return false; // This screen should only be accessible when logged out
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Get the API service instance
        apiService = RetrofitClient.getInstance().getApi();

        sessionManager = new SessionManager(this);
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton = findViewById(R.id.signInButton);
        signUpLinkTextView = findViewById(R.id.signUpLinkTextView);
    }

    private void setupClickListeners() {
        signInButton.setOnClickListener(v -> validateAndSignIn());

        // If you want to allow user to go from sign-in to sign-up
        signUpLinkTextView.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        });
    }

    private void validateAndSignIn() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Valid email required");
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            return;
        }

        proceedWithSignIn(email, password);
    }

    private void proceedWithSignIn(String email, String password) {
        showLoading();

        Map<String, Object> signInRequest = new HashMap<>();
        signInRequest.put("email", email);
        signInRequest.put("password", password);

        apiService.signIn(signInRequest).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                hideLoading();
                Log.d(TAG, "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = response.body();
                    Log.d(TAG, "Success response: " + json.toString());

                    try {
                        if (json.has("message")) {
                            String userId = json.get("id").getAsString();
                            String username = json.get("username").getAsString();
                            String userEmail = json.get("email").getAsString();

                            // Handle avatar buffer data
                            String avatarBase64 = null;
                            if (json.has("avatar") && !json.get("avatar").isJsonNull()) {
                                JsonObject avatarObj = json.getAsJsonObject("avatar");
                                if (avatarObj.has("data") && avatarObj.has("type") &&
                                        avatarObj.get("type").getAsString().equals("Buffer")) {

                                    try {
                                        // Get the data array
                                        JsonArray dataArray = avatarObj.getAsJsonArray("data");
                                        byte[] avatarBytes = new byte[dataArray.size()];
                                        for (int i = 0; i < avatarBytes.length; i++) {
                                            avatarBytes[i] = (byte) dataArray.get(i).getAsInt();
                                        }

                                        // First try to validate if it's a valid image
                                        Bitmap testBitmap = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                                        if (testBitmap != null) {
                                            avatarBase64 = Base64.encodeToString(avatarBytes, Base64.NO_WRAP);
                                            Log.d(TAG, "Successfully decoded avatar image");
                                        } else {
                                            Log.e(TAG, "Invalid image data received");
                                            avatarBase64 = null;
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error processing avatar data", e);
                                        avatarBase64 = null;
                                    }
                                }
                            }

                            sessionManager.saveUserSession(userId, username, userEmail, avatarBase64);
                            showSuccessAndNavigate();
                        } else {
                            handleError("Sign in response parsing error");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                        handleError("Error processing sign in response");
                    }
                } else {
                    // Error handling remains the same
                    try {
                        if (response.errorBody() != null) {
                            String errorBodyString = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBodyString);

                            JSONObject errorJson = new JSONObject(errorBodyString);
                            String errorMsg = errorJson.optString("message", "Sign In Failed");
                            handleError(errorMsg);
                        } else {
                            handleError("Sign In Failed - no error body");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing sign-in error body", e);
                        handleError("Sign In Failed");
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                hideLoading();
                Log.e(TAG, "Network error", t);
                handleError("Network error: " + t.getMessage());
            }
        });
    }

    private void handleError(String errorMessage) {
        Toast.makeText(SignInActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void showLoading() {
        signInButton.setEnabled(false);
        signInButton.setText("Signing In...");
    }

    private void hideLoading() {
        signInButton.setEnabled(true);
        signInButton.setText("Sign In");
    }

    private void showSuccessAndNavigate() {
        // Show success and move forward
        Toast.makeText(this, "Signed in successfully!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}