    package com.example.e_library.activities;

    import static android.content.ContentValues.TAG;

    import android.content.Intent;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.graphics.Rect;
    import android.os.Bundle;
    import android.util.Base64;
    import android.view.View;
    import android.widget.ImageView;
    import android.widget.TextView;

    import android.util.Log;
    import android.widget.Toast;

    import com.example.e_library.adapters.CategoryAdapter;
    import com.example.e_library.utils.SessionManager;
    import com.google.android.material.dialog.MaterialAlertDialogBuilder;
    import com.google.android.material.imageview.ShapeableImageView;
    import com.google.gson.JsonArray;
    import com.google.gson.JsonElement;
    import com.google.gson.JsonObject;
    import com.example.e_library.network.ApiService;
    import com.example.e_library.network.RetrofitClient;
    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.GridLayoutManager;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.e_library.R;
    import com.example.e_library.adapters.BookAdapter;
    import com.example.e_library.models.Book;
    import com.google.android.material.bottomnavigation.BottomNavigationView;
    import com.google.android.material.floatingactionbutton.FloatingActionButton;

    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.List;
    import java.util.stream.Collectors;

    public class HomeActivity extends AppCompatActivity {
        private RecyclerView booksRecyclerView;
        private BookAdapter bookAdapter;
        private TextView userNameText;
        private ShapeableImageView avatarImageView;
        private BottomNavigationView bottomNavigationView;
        private FloatingActionButton addBookFab;

        private RecyclerView categoriesRecyclerView;
        private CategoryAdapter categoryAdapter;
        private List<Book> allBooks = new ArrayList<>();

        private SessionManager sessionManager;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home);

            sessionManager = new SessionManager(this);

            initializeViews();
            setupRecyclerView();
            setupNavigation();
            loadUserData();
            loadBooks();
        }

        private void initializeViews() {
            booksRecyclerView = findViewById(R.id.booksRecyclerView);
            userNameText = findViewById(R.id.userNameText);
            bottomNavigationView = findViewById(R.id.bottomNavigationView);
            addBookFab = findViewById(R.id.addBookFab);
            avatarImageView = findViewById(R.id.avatarImageView);

            avatarImageView.setOnClickListener(v -> showLogoutDialog());

            categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
            setupCategoriesRecyclerView();


        }

        private void setupCategoriesRecyclerView() {
            String[] categories = {"All","Fiction", "Non-Fiction", "Science", "Technology", "Philosophy",
                    "History", "Poetry", "Biography", "Self-Help", "Travel", "Business",
                    "Religion", "Sports", "Cooking", "Art", "Music", "Literature",
                    "Nature", "Mythology"};

            LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                    LinearLayoutManager.HORIZONTAL, false);
            categoriesRecyclerView.setLayoutManager(layoutManager);
            categoryAdapter = new CategoryAdapter(Arrays.asList(categories),
                    this::filterBooks);
            categoriesRecyclerView.setAdapter(categoryAdapter);
        }

        private void filterBooks(String category) {
            if (category == null || category.equals("All")) {
                bookAdapter.updateBooks(allBooks);
            } else {
                List<Book> filteredBooks = allBooks.stream()
                        .filter(book -> category.equals(book.getCategory()))
                        .collect(Collectors.toList());
                bookAdapter.updateBooks(filteredBooks);
            }
        }

        private void showLogoutDialog() {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded)
                    .setTitle("Account Options")
                    .setItems(new String[]{"Logout"}, (dialog, which) -> {
                        if (which == 0) { // Logout option
                            logout();
                        }
                    });

            builder.show();
        }


        private void logout() {
            // Clear session
            sessionManager.clearSession();

            // Navigate to MainActivity and clear back stack
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        private void setupRecyclerView() {
            GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
            booksRecyclerView.setLayoutManager(layoutManager);
            booksRecyclerView.setHasFixedSize(true); // Add this if item sizes are fixed
            booksRecyclerView.setItemViewCacheSize(20); // Increase view cache
            booksRecyclerView.setDrawingCacheEnabled(true);
            booksRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

            // Add spacing between items
            int spacing = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
            booksRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                           @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    outRect.left = spacing;
                    outRect.right = spacing;
                    outRect.top = spacing;
                    outRect.bottom = spacing;
                }
            });

            // Initialize and set adapter
            bookAdapter = new BookAdapter(new ArrayList<>(), book -> {
                Intent intent = new Intent(HomeActivity.this, BookDetailsActivity.class);
                intent.putExtra("book_id", book.getId());
                startActivity(intent);
            });
            booksRecyclerView.setAdapter(bookAdapter);
        }

        private void setupNavigation() {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    return true;
                } else if (itemId == R.id.navigation_library) {
                    Intent intent = new Intent(HomeActivity.this, LibraryActivity.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            });

            addBookFab.setOnClickListener(v -> {
                 Intent intent = new Intent(HomeActivity.this, AddBookActivity.class);
                 startActivity(intent);
                 finish();
            });
        }
        private void loadAvatar() {
            String avatarBase64 = sessionManager.getAvatar();
            if (avatarBase64 != null && !avatarBase64.isEmpty()) {
                try {
                    // Decode Base64 to byte array
                    byte[] imageBytes = Base64.decode(avatarBase64, Base64.DEFAULT);

                    // Convert to Bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                    if (bitmap != null) {
                        avatarImageView.setImageBitmap(bitmap);
                    } else {
                        Log.e(TAG, "Failed to decode avatar bitmap");
                        avatarImageView.setImageResource(R.drawable.avatar);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading avatar", e);
                    avatarImageView.setImageResource(R.drawable.avatar);
                }
            } else {
                avatarImageView.setImageResource(R.drawable.avatar);
            }
        }

        private void loadUserData() {

            String username = sessionManager.getUsername();
            loadAvatar();
            userNameText.setText(username);
        }

        private void loadBooks() {
            ApiService apiService = RetrofitClient.getInstance().getApi();

            apiService.getBooks().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JsonObject jsonResponse = response.body();
                            JsonArray booksArray = jsonResponse.getAsJsonArray("books");
                            List<Book> books = new ArrayList<>();

                            for (JsonElement element : booksArray) {
                                JsonObject bookObject = element.getAsJsonObject();

                                Book book = new Book(
                                        bookObject.has("id") ? bookObject.get("id").getAsLong() : null,
                                        bookObject.has("title") ? bookObject.get("title").getAsString() : "",
                                        bookObject.has("author") ? bookObject.get("author").getAsString() : "",
                                        bookObject.has("category") ? bookObject.get("category").getAsString() : "",
                                        bookObject.has("image_url") ? bookObject.get("image_url").getAsString() : null
                                );

                                books.add(book);
                                allBooks = new ArrayList<>(books); // Store all books
                                runOnUiThread(() -> bookAdapter.updateBooks(allBooks));
                            }

                            runOnUiThread(() -> bookAdapter.updateBooks(books));
                        } catch (Exception e) {
                            Log.e("HomeActivity", "Error parsing books", e);
                            runOnUiThread(() -> handleError("Error loading books"));
                        }
                    } else {
                        runOnUiThread(() -> handleError("Failed to load books"));
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e("HomeActivity", "Network error", t);
                    runOnUiThread(() -> handleError("Network error: " + t.getMessage()));
                }
            });
        }

        private void handleError(String message) {
            runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
        }

    }