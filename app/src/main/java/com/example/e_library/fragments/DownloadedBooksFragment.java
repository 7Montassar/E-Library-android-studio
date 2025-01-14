package com.example.e_library.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_library.R;
import com.example.e_library.adapters.DownloadedBooksAdapter;
import com.example.e_library.models.DownloadedBook;
import com.example.e_library.network.ApiService;
import com.example.e_library.network.RetrofitClient;
import com.example.e_library.utils.ReadingProgressManager;
import com.example.e_library.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadedBooksFragment extends Fragment {
    private static final String TAG = "DownloadedBooksFragment";
    private RecyclerView recyclerView;
    private DownloadedBooksAdapter adapter;
    private ApiService apiService;
    private SessionManager sessionManager;
    private View rootView;
    private DownloadedBook currentBook;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_downloaded_books, container, false);
        initializeComponents();
        setupRecyclerView();
        loadDownloadedBooks();
        return rootView;
    }

    private void initializeComponents() {
        apiService = RetrofitClient.getInstance().getApi();
        sessionManager = new SessionManager(requireContext());
    }

    private void setupRecyclerView() {
        recyclerView = rootView.findViewById(R.id.downloadedBooksRecyclerView);
        adapter = new DownloadedBooksAdapter(requireContext());
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);

        adapter.setOnBookClickListener(book -> {
            currentBook = book; // Store the current book
            String title = book.getTitle().replaceAll("[^a-zA-Z0-9]", "_");
            String author = book.getAuthor().replaceAll("[^a-zA-Z0-9]", "_");
            String fileName = title + "_" + author + ".pdf";

            File pdfFile = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), fileName);

            if (pdfFile.exists()) {
                openPDF(pdfFile, book);
            } else {
                Toast.makeText(getContext(),
                        "PDF file not found. Please download again.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openPDF(File pdfFile, DownloadedBook book) {
        try {
            Log.d(TAG, "File exists: " + pdfFile.exists());
            Log.d(TAG, "File path: " + pdfFile.getAbsolutePath());

            if (!pdfFile.exists()) {
                Toast.makeText(getContext(),
                        "PDF file not found. Please download again.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Get total pages using PdfRenderer
            int totalPages = getTotalPages(pdfFile);

            // Update reading progress before opening
            updateInitialProgress(book, totalPages);

            Uri pdfUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getApplicationContext().getPackageName() + ".provider",
                    pdfFile);

            openWithGoogleDrive(pdfUri, book.getId());
        } catch (Exception e) {
            Log.e(TAG, "Error opening PDF", e);
            Toast.makeText(getContext(),
                    "Error opening PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private int getTotalPages(File pdfFile) {
        try {
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(
                    pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer renderer = new PdfRenderer(fileDescriptor);
            int totalPages = renderer.getPageCount();
            renderer.close();
            fileDescriptor.close();
            return totalPages;
        } catch (IOException e) {
            Log.e(TAG, "Error getting PDF page count", e);
            return 100; // Fallback default value
        }
    }

    private void updateInitialProgress(DownloadedBook book, int totalPages) {
        ReadingProgressManager progressManager = new ReadingProgressManager(
                requireContext(),
                RetrofitClient.getInstance().getApi()
        );

        progressManager.updateProgress(book.getId(), 1, totalPages, new ReadingProgressManager.ProgressCallback() {
            @Override
            public void onSuccess(JsonObject response) {
                Log.d(TAG, "Initial progress updated successfully");
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error updating initial progress: " + message);
            }
        });
    }

    private void openWithGoogleDrive(Uri pdfUri, long bookId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setPackage("com.google.android.apps.docs");

        try {
            saveReadingStartTime(bookId);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showGoogleDriveInstallDialog();
        }
    }

    private void saveReadingStartTime(long bookId) {
        SharedPreferences prefs = requireContext().getSharedPreferences(
                "reading_session", Context.MODE_PRIVATE);
        prefs.edit().putLong("start_time_" + bookId, System.currentTimeMillis()).apply();
    }

    private void showGoogleDriveInstallDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Google Drive Required")
                .setMessage("Would you like to install Google Drive to view PDF files?")
                .setPositiveButton("Install", (dialog, which) -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=com.google.android.apps.docs")));
                    } catch (ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.docs")));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDownloadedBooks();
        updateReadingProgress();
    }

    private void updateReadingProgress() {
        if (currentBook != null) {
            SharedPreferences prefs = requireContext().getSharedPreferences(
                    "reading_session", Context.MODE_PRIVATE);
            long startTime = prefs.getLong("start_time_" + currentBook.getId(), 0);

            if (startTime > 0) {
                processReadingSession(startTime, currentBook, prefs);
            }
        }
    }

    private void processReadingSession(long startTime, DownloadedBook book, SharedPreferences prefs) {
        long readingTime = (System.currentTimeMillis() - startTime) / (1000 * 60);
        int estimatedPagesRead = (int) (readingTime / 2); // Assumes 2 minutes per page

        ReadingProgressManager progressManager = new ReadingProgressManager(
                requireContext(),
                RetrofitClient.getInstance().getApi()
        );

        progressManager.getProgress(book.getId(), new ReadingProgressManager.ProgressCallback() {
            @Override
            public void onSuccess(JsonObject response) {
                updateProgressAfterReading(response, book, estimatedPagesRead, prefs, progressManager);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error getting current progress: " + message);
            }
        });
    }

    private void updateProgressAfterReading(JsonObject response, DownloadedBook book,
                                            int estimatedPagesRead, SharedPreferences prefs, ReadingProgressManager progressManager) {
        try {
            JsonObject data = response.getAsJsonObject("data");
            int totalPages = data.get("total_pages").getAsInt();
            int lastPage = data.get("last_page").getAsInt();
            int newPage = Math.min(lastPage + estimatedPagesRead, totalPages);

            progressManager.updateProgress(book.getId(), newPage, totalPages,
                    new ReadingProgressManager.ProgressCallback() {
                        @Override
                        public void onSuccess(JsonObject response) {
                            Log.d(TAG, "Progress updated after reading session");
                            prefs.edit().remove("start_time_" + book.getId()).apply();
                            loadDownloadedBooks();
                        }

                        @Override
                        public void onError(String message) {
                            Log.e(TAG, "Error updating progress after reading: " + message);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error processing progress update", e);
        }
    }

    private void loadDownloadedBooks() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            handleError("Please login to view downloaded books");
            return;
        }

        apiService.getUserDownloadedBooks(userId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "Response code: " + response.code());

                if (!response.isSuccessful()) {
                    handleErrorResponse(response);
                    return;
                }

                handleSuccessResponse(response);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                handleError("Network error: " + t.getMessage());
            }
        });
    }

    private void handleErrorResponse(Response<JsonObject> response) {
        try {
            Log.e(TAG, "Error body: " + response.errorBody().string());
        } catch (Exception e) {
            Log.e(TAG, "Error reading error body", e);
        }
        handleError("Failed to load books: " + response.code());
    }

    private void handleSuccessResponse(Response<JsonObject> response) {
        if (response.body() != null) {
            try {
                Log.d(TAG, "Response body: " + response.body());
                List<DownloadedBook> books = parseDownloadedBooks(response.body());
                updateUI(books);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing book data", e);
                handleError("Error parsing book data: " + e.getMessage());
            }
        } else {
            handleError("Empty response from server");
        }
    }

    private void updateUI(List<DownloadedBook> books) {
        if (isAdded()) {
            View emptyState = rootView.findViewById(R.id.emptyStateContainer);

            if (books.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
                adapter.setBooks(books);
                updateBookCount(books.size());
            }
        }
    }

    private List<DownloadedBook> parseDownloadedBooks(JsonObject response) {
        List<DownloadedBook> books = new ArrayList<>();
        if (response.has("data")) {
            JsonArray booksArray = response.getAsJsonArray("data");

            for (JsonElement element : booksArray) {
                try {
                    JsonObject bookObject = element.getAsJsonObject();
                    DownloadedBook book = createBookFromJson(bookObject);
                    if (book != null) {
                        books.add(book);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing book: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return books;
    }

    private DownloadedBook createBookFromJson(JsonObject bookObject) {
        try {
            long id = bookObject.has("id") ? bookObject.get("id").getAsLong() : 0;
            String userId = bookObject.has("user_id") ?
                    String.valueOf(bookObject.get("user_id").getAsInt()) : "";
            long bookId = bookObject.has("book_id") ? bookObject.get("book_id").getAsLong() : 0;
            String downloadDate = bookObject.has("download_date") ?
                    bookObject.get("download_date").getAsString() : "";

            String lastReadDate = null;
            if (bookObject.has("last_read_date") && !bookObject.get("last_read_date").isJsonNull()) {
                lastReadDate = bookObject.get("last_read_date").getAsString();
            }

            int lastPageRead = bookObject.has("last_page_read") ?
                    bookObject.get("last_page_read").getAsInt() : 0;
            int totalPages = bookObject.has("total_pages") ?
                    bookObject.get("total_pages").getAsInt() : 0;
            float readingProgress = bookObject.has("reading_progress") ?
                    bookObject.get("reading_progress").getAsFloat() : 0f;
            String title = bookObject.has("title") ? bookObject.get("title").getAsString() : "";
            String author = bookObject.has("author") ? bookObject.get("author").getAsString() : "";
            String imageUrl = bookObject.has("image_url") ?
                    bookObject.get("image_url").getAsString() : "";

            return new DownloadedBook(
                    id, userId, bookId, downloadDate, lastReadDate,
                    lastPageRead, totalPages, readingProgress,
                    title, author, imageUrl
            );
        } catch (Exception e) {
            Log.e(TAG, "Error creating book from JSON: " + e.getMessage());
            return null;
        }
    }

    private void updateBookCount(int count) {
        if (isAdded()) {
            TextView bookCountText = requireActivity().findViewById(R.id.bookCountText);
            bookCountText.setText(count + " Books");
        }
    }

    private void handleError(String message) {
        if (isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}