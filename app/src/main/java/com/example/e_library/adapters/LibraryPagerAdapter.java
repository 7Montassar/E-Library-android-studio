package com.example.e_library.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.e_library.fragments.DownloadedBooksFragment;

public class LibraryPagerAdapter extends FragmentStateAdapter {
    public LibraryPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DownloadedBooksFragment();
            case 1:
//                return new CollectionsFragment();
            case 2:
//                return new RecentBooksFragment();
            default:
                return new DownloadedBooksFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}