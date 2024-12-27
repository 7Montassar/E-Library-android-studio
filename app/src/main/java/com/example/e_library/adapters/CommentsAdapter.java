package com.example.e_library.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.e_library.R;
import com.example.e_library.models.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private List<Comment> comments;
    private Context context;

    public CommentsAdapter(Context context) {
        this.context = context;
        this.comments = new ArrayList<>();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void updateComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatarView;
        private final TextView usernameView;
        private final TextView contentView;
        private final TextView timeView;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.commentUserAvatar);
            usernameView = itemView.findViewById(R.id.commentUsername);
            contentView = itemView.findViewById(R.id.commentContent);
            timeView = itemView.findViewById(R.id.commentTime);
        }

        void bind(Comment comment) {
            usernameView.setText(comment.getUsername());
            contentView.setText(comment.getContent());
            timeView.setText(comment.getCreatedAt());

            Glide.with(context)
                    .load(comment.getAvatar())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(avatarView);
        }
    }
}