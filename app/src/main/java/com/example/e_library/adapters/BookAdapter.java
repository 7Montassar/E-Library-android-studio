package com.example.e_library.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.e_library.R;
import com.example.e_library.models.Book;
import com.bumptech.glide.Glide;

import com.bumptech.glide.load.engine.DiskCacheStrategy;



import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private static List<Book> books;
    private static OnBookClickListener listener;
    // Interface for click handling
    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public BookAdapter(List<Book> books, OnBookClickListener listener) {
        this.books = books;
        this.listener = listener;
    }

    public void updateBooks(List<Book> newBooks) {
        this.books = newBooks != null ? newBooks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book, listener);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookTitle;
        TextView bookAuthor;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.bookCover);
            bookTitle = itemView.findViewById(R.id.bookTitle);
            bookAuthor = itemView.findViewById(R.id.bookAuthor);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBookClick(books.get(position));
                }
            });
        }
        void bind(final Book book, final OnBookClickListener listener) {
            // Clear any previous loading
            Glide.with(itemView.getContext()).clear(bookCover);

            bookTitle.setText(book.getTitle());
            bookAuthor.setText(book.getAuthor());
            // Show placeholder immediately
            bookCover.setImageResource(R.drawable.book_placeholder);

            String coverUrl = book.getCoverUrl();
            if (coverUrl != null && !coverUrl.trim().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(coverUrl)
                        .override(300, 400)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.book_placeholder)
                        .error(R.drawable.book_error)
                        .transition(DrawableTransitionOptions.withCrossFade(100))
                        .into(bookCover);
            }
        }


    }
}