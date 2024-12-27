package com.example.e_library.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.e_library.R;
import com.example.e_library.network.ApiService;
import com.example.e_library.network.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddBookActivity extends AppCompatActivity {
    private static final int PDF_PICK_CODE = 1000;
    private static final String TAG = "AddBookActivity";


    private ProgressDialog progressDialog;


    private Uri selectedPdfUri;
    private TextInputEditText bookNameInput, authorInput, descriptionInput;
    private AutoCompleteTextView categoryDropdown;
    private TextView selectedFileName;
    private MaterialButton selectPdfButton, addButton;
    private ApiService apiService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        apiService = RetrofitClient.getInstance().getApi();

        initializeViews();
        setupCategoryDropdown();
        setupListeners();
        setupNavigation();
    }

    private void initializeViews() {
        bookNameInput = findViewById(R.id.bookNameInput);
        authorInput = findViewById(R.id.authorInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        categoryDropdown = findViewById(R.id.categoryDropdown);
        selectedFileName = findViewById(R.id.selectedFileName);
        selectPdfButton = findViewById(R.id.selectPdfButton);
        addButton = findViewById(R.id.addButton);
    }

    private void setupNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Clear the selected item when entering AddBookActivity
        bottomNavigationView.setSelectedItemId(R.id.navigation_add);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Fiction", "Non-Fiction", "Science", "Technology", "Philosophy", "History", "Poetry", "Biography", "Self-Help", "Travel", "Business", "Religion", "Sports", "Cooking", "Art", "Music", "Literature", "Nature", "Mythology"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        categoryDropdown.setAdapter(adapter);
        categoryDropdown.setOnClickListener(v -> categoryDropdown.showDropDown()); // Show dropdown on click
    }

    private void setupListeners() {
        selectPdfButton.setOnClickListener(v -> selectPdf());
        addButton.setOnClickListener(v -> validateAndUploadBook());
    }

    private void selectPdf() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PDF_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedPdfUri = data.getData();
            String fileName = getFileName(selectedPdfUri);
            selectedFileName.setText(fileName);
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void validateAndUploadBook() {
        String title = bookNameInput.getText().toString().trim();
        String author = authorInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String category = categoryDropdown.getText().toString().trim();

        if (title.isEmpty()) {
            bookNameInput.setError("Title is required");
            return;
        }
        if (author.isEmpty()) {
            authorInput.setError("Author is required");
            return;
        }
        if (description.isEmpty()) {
            descriptionInput.setError("Description is required");
            return;
        }
        if (category.isEmpty()) {
            categoryDropdown.setError("Category is required");
            return;
        }
        if (selectedPdfUri == null) {
            Toast.makeText(this, "Please select a PDF file", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadBook(title, author, description, category);
    }

    private void uploadBook(String title, String author, String description, String category) {
        showLoading();

        try {
            // Create request bodies for text fields
            RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
            RequestBody authorBody = RequestBody.create(MediaType.parse("text/plain"), author);
            RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), description);
            RequestBody categoryBody = RequestBody.create(MediaType.parse("text/plain"), category);

            // Create request body for PDF file
            String fileName = getFileName(selectedPdfUri);
            InputStream inputStream = getContentResolver().openInputStream(selectedPdfUri);
            byte[] pdfBytes = convertUriToBytes(selectedPdfUri);
            RequestBody pdfBody = RequestBody.create(MediaType.parse("application/pdf"), pdfBytes);
            MultipartBody.Part pdfPart = MultipartBody.Part.createFormData("pdf", fileName, pdfBody);

            // Make API call
            apiService.addBook(titleBody, authorBody, descriptionBody, categoryBody, pdfPart)
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            hideLoading();
                            if (response.isSuccessful() && response.body() != null) {
                                showSuccessAndFinish();
                            } else {
                                try {
                                    if (response.errorBody() != null) {
                                        String errorBody = response.errorBody().string();
                                        try {
                                            // Try to parse as JSON first
                                            JSONObject errorJson = new JSONObject(errorBody);
                                            String errorMessage = errorJson.optString("message", "Failed to upload book");
                                            handleError(errorMessage);
                                        } catch (JSONException e) {
                                            // If not JSON, handle the error code
                                            String errorMessage = "Error: " + response.code();
                                            switch (response.code()) {
                                                case 404:
                                                    errorMessage = "Endpoint not found";
                                                    break;
                                                case 400:
                                                    errorMessage = "Invalid request";
                                                    break;
                                                case 500:
                                                    errorMessage = "Server error";
                                                    break;
                                            }
                                            handleError(errorMessage);
                                        }
                                    } else {
                                        handleError("Unknown error occurred");
                                    }
                                } catch (IOException e) {
                                    Log.e(TAG, "Error reading error body", e);
                                    handleError("Failed to upload book");
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

        } catch (IOException e) {
            hideLoading();
            Log.e(TAG, "Error reading PDF file", e);
            handleError("Error reading PDF file");
        }
    }

    private byte[] convertUriToBytes(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void handleError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void showLoading() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading book...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showSuccessAndFinish() {
        Toast.makeText(this, "Book uploaded successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}