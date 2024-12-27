package com.example.e_library.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_library.R;
import com.google.android.material.chip.Chip;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private final List<String> categories;
    private String selectedCategory = null;
    private final OnCategorySelectedListener listener;

    public interface OnCategorySelectedListener {
        void onCategorySelected(String category);
    }

    public CategoryAdapter(List<String> categories, OnCategorySelectedListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Chip chip = (Chip) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(chip);
    }

    @NonNull


    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final Chip chip;

        CategoryViewHolder(Chip chip) {
            super(chip);
            this.chip = chip;
        }

        void bind(String category) {
            chip.setText(category);
            chip.setChecked(category.equals(selectedCategory));

            chip.setOnClickListener(v -> {
                if (category.equals(selectedCategory)) {
                    selectedCategory = null;
                    chip.setChecked(false);
                } else {
                    selectedCategory = category;
                    chip.setChecked(true);
                }
                listener.onCategorySelected(selectedCategory);
                notifyDataSetChanged();
            });
        }
    }
}