package com.example.e_library.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.e_library.R;
import com.example.e_library.network.ApiService;
import com.example.e_library.network.RetrofitClient;
import com.example.e_library.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends BaseAuthActivity {


    private static final String TAG = "SignUpActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    private ShapeableImageView avatarImageView;
    private TextInputEditText fullNameEditText, emailEditText, usernameEditText, passwordEditText;
    private MaterialButton signUpButton;
    private Uri selectedImageUri;
    private ApiService apiService;

    private SessionManager sessionManager;


    @Override
    protected boolean shouldBeLoggedIn() {
        return false; // This screen should only be accessible when logged out
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Retrofit instance
        apiService = RetrofitClient.getInstance().getApi();

        sessionManager = new SessionManager(this);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        avatarImageView = findViewById(R.id.avatarImageView);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton = findViewById(R.id.signUpButton);
    }

    private void setupClickListeners() {
        avatarImageView.setOnClickListener(v -> openImagePicker());
        findViewById(R.id.uploadAvatarText).setOnClickListener(v -> openImagePicker());
        signUpButton.setOnClickListener(v -> validateAndSignUp());
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Avatar"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                avatarImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                Log.e(TAG, "Failed to load image", e);
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void validateAndSignUp() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (fullName.isEmpty()) {
            fullNameEditText.setError("Full name is required");
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Valid email is required");
            return;
        }

        if (username.isEmpty()) {
            usernameEditText.setError("Username is required");
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return;
        }

        proceedWithSignUp(fullName, email, username, password);
    }

    private void proceedWithSignUp(String fullName, String email, String username, String password) {
        showLoading();

        // Convert avatar image to byte array
        byte[] avatarBytes = null;
        if (selectedImageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                // Scale down the bitmap if it's too large
                bitmap = scaleBitmap(bitmap, 800); // max width/height of 800px
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                avatarBytes = stream.toByteArray();
            } catch (IOException e) {
                Log.e(TAG, "Failed to process avatar", e);
                handleError("Failed to process avatar image");
                return;
            }
        }

        // Build a map for the request body
        Map<String, Object> signupRequest = new HashMap<>();
        signupRequest.put("fullName", fullName);
        signupRequest.put("email", email);
        signupRequest.put("username", username);
        signupRequest.put("password", password);
        if (avatarBytes != null) {
            signupRequest.put("avatar", Base64.encodeToString(avatarBytes, Base64.NO_WRAP));
        }

        byte[] finalAvatarBytes = avatarBytes;
        apiService.signup(signupRequest).enqueue(new Callback<JsonObject>() {
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
                            String avatarBase64 = null;

                            if (finalAvatarBytes != null) {
                                avatarBase64 = Base64.encodeToString(finalAvatarBytes, Base64.NO_WRAP);
                            }

                            sessionManager.saveUserSession(userId, username, userEmail, avatarBase64);
                            showSuccessAndFinish();
                        } else {
                            handleError("Registration response parsing error");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing success response", e);
                        handleError("Error processing registration response");
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorBodyString = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBodyString);

                            JSONObject errorBody = new JSONObject(errorBodyString);
                            String errorMessage = errorBody.optString("message", "Registration Failed");
                            handleError(errorMessage);
                        } else {
                            handleError("Registration Failed - no error body");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                        handleError("Registration Failed");
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

    // Add this helper method to scale down large images
    private Bitmap scaleBitmap(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = originalWidth;
        int newHeight = originalHeight;

        if (originalHeight > maxDimension || originalWidth > maxDimension) {
            if (originalWidth > originalHeight) {
                newWidth = maxDimension;
                newHeight = (int) (originalHeight * ((float) maxDimension / originalWidth));
            } else {
                newHeight = maxDimension;
                newWidth = (int) (originalWidth * ((float) maxDimension / originalHeight));
            }
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }
        return bitmap;
    }
    private void handleError(String errorMessage) {
        // Show in Toast
        Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void showLoading() {
        signUpButton.setEnabled(false);
        signUpButton.setText("Creating Account...");
    }

    private void hideLoading() {
        signUpButton.setEnabled(true);
        signUpButton.setText("Sign Up");
    }

    private void showSuccessAndFinish() {
        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}