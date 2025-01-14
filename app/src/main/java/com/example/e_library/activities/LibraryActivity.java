package com.example.e_library.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.e_library.R;
import com.example.e_library.adapters.LibraryPagerAdapter;
import com.example.e_library.network.ApiService;
import com.example.e_library.network.RetrofitClient;
import com.example.e_library.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class LibraryActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private LibraryPagerAdapter pagerAdapter;
    private ApiService apiService;
    private SessionManager sessionManager;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton addBookFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        initializeComponents();
        setupViewPager();
        setupTabLayout();
        setupNavigation();
    }

    private void setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_library) {
                return true;
            } else if (itemId == R.id.navigation_home) {
                Intent intent = new Intent(LibraryActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
            return false;
        });

        addBookFab.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, AddBookActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void initializeComponents() {
        apiService = RetrofitClient.getInstance().getApi();
        sessionManager = new SessionManager(this);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        addBookFab = findViewById(R.id.addBookFab);

        // Set the library item as selected
        bottomNavigationView.setSelectedItemId(R.id.navigation_library);
    }

    private void setupViewPager() {
        pagerAdapter = new LibraryPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Prevent swipe behavior if desired
        // viewPager.setUserInputEnabled(false);
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Downloaded");
                            break;
                        case 1:
                            tab.setText("Collections");
                            break;
                    }
                }
        ).attach();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh content if needed
        if (pagerAdapter != null) {
            pagerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If we're on the first page, handle back press normally
            super.onBackPressed();
        } else {
            // Otherwise, select the previous page
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }
}