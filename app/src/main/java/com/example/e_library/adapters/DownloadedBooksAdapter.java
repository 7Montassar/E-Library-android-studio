package com.example.e_library.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.e_library.R;
import com.example.e_library.models.DownloadedBook;

import java.util.ArrayList;
import java.util.List;

import com.example.e_library.models.DownloadedBook;

public class DownloadedBooksAdapter extends RecyclerView.Adapter<DownloadedBooksAdapter.ViewHolder> {
    private Context context;
    private List<DownloadedBook> books;
    private OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(DownloadedBook book);
    }

    public DownloadedBooksAdapter(Context context) {
        this.context = context;
        this.books = new ArrayList<>();
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.listener = listener;
    }

    public void setBooks(List<DownloadedBook> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_downloaded_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DownloadedBook book = books.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView bookCover;
        private TextView bookTitle;
        private TextView bookAuthor;
        private ProgressBar readingProgress;
        private TextView progressText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.bookCover);
            bookTitle = itemView.findViewById(R.id.bookTitle);
            bookAuthor = itemView.findViewById(R.id.bookAuthor);
            readingProgress = itemView.findViewById(R.id.readingProgress);
            progressText = itemView.findViewById(R.id.progressText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBookClick(books.get(position));
                }
            });
        }

        public void bind(DownloadedBook book) {
            bookTitle.setText(book.getTitle());
            bookAuthor.setText(book.getAuthor());

            // Set reading progress
            int progress = (int) book.getReadingProgress();
            readingProgress.setProgress(progress);
            progressText.setText(progress + "%");

            // Load book cover using Glide
            Glide.with(context)
                    .load(book.getImageUrl())
                    .placeholder(R.drawable.book_placeholder)
                    .error(R.drawable.book_error)
                    .into(bookCover);
        }
    }
}