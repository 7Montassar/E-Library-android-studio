package com.example.e_library.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.e_library.R;
import com.example.e_library.adapters.CommentsAdapter;
import com.example.e_library.models.Comment;
import com.example.e_library.network.ApiService;
import com.example.e_library.network.RetrofitClient;
import com.example.e_library.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailsActivity extends AppCompatActivity {

    private ApiService apiService;
    private CommentsAdapter commentsAdapter;
    private SessionManager sessionManager;
    private long bookId;

    private String bookTitle;
    private String bookAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        apiService = RetrofitClient.getInstance().getApi();
        bookId = getIntent().getLongExtra("book_id", 0);

        if (bookId == -1) {
            Toast.makeText(this, "Error loading book details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViews();
        loadBookDetails();
    }

    private void setupViews() {

        sessionManager = new SessionManager(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MaterialCardView downloadCard = findViewById(R.id.downloadCard);
        MaterialCardView favoriteCard = findViewById(R.id.favoriteCard);
        MaterialButton addCommentButton = findViewById(R.id.addCommentButton);

        RecyclerView commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsAdapter = new CommentsAdapter(this);
        commentsRecyclerView.setAdapter(commentsAdapter);

        downloadCard.setOnClickListener(v -> downloadBook());
        favoriteCard.setOnClickListener(v -> toggleFavorite());
        addCommentButton.setOnClickListener(v -> addComment());
    }

    private void downloadBook() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login to download", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading PDF...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        // First download the PDF
        apiService.downloadBookPdf(bookId, userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Save PDF and then add to downloads
                    savePdfAndAddToDownloads(response.body(), progressDialog);
                } else {
                    progressDialog.dismiss();
                    handleError("Failed to download book");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                handleError("Network error during download: " + t.getMessage());
            }
        });
    }

    private void savePdfAndAddToDownloads(final ResponseBody body, ProgressDialog progressDialog) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Save PDF file logic (your existing code)
                String safeTitle = bookTitle.replaceAll("[^a-zA-Z0-9]", "_");
                String safeAuthor = bookAuthor.replaceAll("[^a-zA-Z0-9]", "_");
                String fileName = safeTitle + "_" + safeAuthor + ".pdf";

                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadsDir, fileName);

                boolean savedSuccessfully = false;

                try (InputStream inputStream = body.byteStream();
                     OutputStream outputStream = new FileOutputStream(file)) {

                    byte[] fileReader = new byte[4096];
                    long fileSize = body.contentLength();
                    long fileSizeDownloaded = 0;

                    while (true) {
                        int read = inputStream.read(fileReader);
                        if (read == -1) break;
                        outputStream.write(fileReader, 0, read);
                        fileSizeDownloaded += read;
                    }

                    outputStream.flush();
                    savedSuccessfully = true;
                }

                // If PDF was saved successfully, add to downloads
                if (savedSuccessfully) {
                    handler.post(() -> {
                        // Create download info
                        JsonObject downloadInfo = new JsonObject();
                        downloadInfo.addProperty("userId", sessionManager.getUserId());
                        downloadInfo.addProperty("bookId", bookId);
                        downloadInfo.addProperty("totalPages", 0); // You can update this later
                        downloadInfo.addProperty("lastPageRead", 0);
                        downloadInfo.addProperty("readingProgress", 0.0f);

                        // Add to downloads table
                        apiService.addToDownloads(bookId, downloadInfo).enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                progressDialog.dismiss();
                                if (response.isSuccessful()) {
                                    Toast.makeText(BookDetailsActivity.this,
                                            "Book downloaded and added to your library",
                                            Toast.LENGTH_LONG).show();
                                    loadBookDetails(); // Refresh to update download count
                                } else {
                                    handleError("Book downloaded but failed to add to library");
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                progressDialog.dismiss();
                                handleError("Book downloaded but failed to add to library: " + t.getMessage());
                            }
                        });
                    });
                }
            } catch (IOException e) {
                handler.post(() -> {
                    progressDialog.dismiss();
                    handleError("Error saving PDF: " + e.getMessage());
                });
            }
        });

        executor.shutdown();
    }

    private void toggleFavorite() {

        String userId = sessionManager.getUserId();

        Log.d("BookDetailsActivity", "UserId from session: " + userId);

        if (userId == null) {
            Toast.makeText(this, "Please login to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject favoriteBody = new JsonObject();
        favoriteBody.addProperty("userId", userId);
        apiService.toggleFavorite(bookId, favoriteBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookDetailsActivity.this, "Favorite updated", Toast.LENGTH_SHORT).show();
                    loadBookDetails(); // Refresh book details to update counts
                } else {
                    handleError("Failed to update favorite");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                handleError("Network error while updating favorite");
            }
        });
    }

    private void addComment() {
        TextInputEditText commentInput = findViewById(R.id.commentInput);
        String comment = commentInput.getText().toString().trim();
        String userId = sessionManager.getUserId();

        Log.d("BookDetailsActivity", "UserId from session: " + userId);

        if (userId == null) {
            Toast.makeText(this, "Please login to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (comment.isEmpty()) {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject commentBody = new JsonObject();
        commentBody.addProperty("content", comment);
        commentBody.addProperty("userId", userId);

        apiService.addComment(bookId, commentBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    commentInput.setText("");
                    Toast.makeText(BookDetailsActivity.this, "Comment added", Toast.LENGTH_SHORT).show();
                    loadBookDetails(); // Refresh to show new comment
                } else {
                    handleError("Failed to add comment");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                handleError("Network error while adding comment");
            }
        });
    }

    private void loadBookDetails() {
        apiService.getBookDetails(bookId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject bookObject = response.body().getAsJsonObject("book");
                    bookId = bookObject.get("id").getAsLong();
                    bookTitle = bookObject.get("title").getAsString();
                    bookAuthor = bookObject.get("author").getAsString();
                    updateUI(bookObject);
                } else {
                    handleError("Failed to load book details");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                handleError("Network error: " + t.getMessage());
            }
        });
    }

    private void updateUI(JsonObject bookObject) {
        TextView titleView = findViewById(R.id.bookTitle);
        TextView authorView = findViewById(R.id.bookAuthor);
        TextView descriptionView = findViewById(R.id.bookDescription);
        TextView downloadCountView = findViewById(R.id.downloadCount);
        TextView favoriteCountView = findViewById(R.id.favoriteCount);
        ImageView coverImageView = findViewById(R.id.bookCoverImage);

        titleView.setText(bookObject.get("title").getAsString());
        authorView.setText(bookObject.get("author").getAsString());
        descriptionView.setText(bookObject.get("description").getAsString());
        downloadCountView.setText(String.valueOf(bookObject.get("download_count").getAsInt()));
        favoriteCountView.setText(String.valueOf(bookObject.get("favorite_count").getAsInt()));

        Glide.with(this)
                .load(bookObject.get("image_url").getAsString())
                .placeholder(R.drawable.book_placeholder)
                .error(R.drawable.book_error)
                .into(coverImageView);

        // Handle comments
        if (bookObject.has("comments") && !bookObject.get("comments").isJsonNull()) {
            JsonArray commentsArray = bookObject.getAsJsonArray("comments");
            List<Comment> comments = new ArrayList<>();

            for (JsonElement element : commentsArray) {
                JsonObject commentObj = element.getAsJsonObject();
                Comment comment = new Comment(
                        commentObj.get("content").getAsString(),
                        commentObj.get("username").getAsString(),
                        commentObj.get("avatar").getAsString(), // Now it's a base64 string
                        formatDate(commentObj.get("created_at").getAsString())
                );
                comments.add(comment);
            }

            commentsAdapter.updateComments(comments);
        }
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(dateString);

            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);
            outputFormat.setTimeZone(TimeZone.getDefault());

            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    private void handleError(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}