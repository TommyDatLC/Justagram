package com.example.justagram.fragment.PostFeed;

import com.example.justagram.R;
// if u need to change something, here r the related files
// postItem.java
// layout : items_post_feed, fragment_post_feed, activity_main, activity_post_detail
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.justagram.fragment.Statistic.PostItem;

import androidx.media3.ui.PlayerView;

import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final Context context;
    private final List<PostItem> postList;
    private final String accessToken;
    private OnSelectionChangedListener selectionListener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    public PostAdapter(Context context, List<PostItem> postList, String accessToken) {
        this.context = context;
        this.postList = postList;
        this.accessToken = accessToken;
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.items_post_feed, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostItem post = postList.get(position);

        holder.likeCount.setText( Integer.toString(post.getLikeCount()));
        holder.commentCount.setText(Integer.toString(post.getCommentCount()));

        String mediaUrl = post.getMediaUrl();
        String mediaType = post.getMediaType();

        // Hiển thị overlay nếu đã chọn
        holder.overlay.setVisibility(post.isSelected() ? View.VISIBLE : View.GONE);


        if (mediaUrl == null) return;

        // 👉 Nếu là VIDEO → chỉ hiển thị thumbnail
        if (mediaType != null && mediaType.equalsIgnoreCase("VIDEO")) {
            holder.playerView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);

            String finalUrl = mediaUrl + "?access_token=" + accessToken;

            // Nếu có thumbnail sẵn (API trả về)
            if (post.getThumbnailUrl() != null && !post.getThumbnailUrl().isEmpty()) {
                Glide.with(context)
                        .load(post.getThumbnailUrl())
                        .centerCrop()
                        .into(holder.imageView);
            } else {
                // 🔥 Nếu không có, tự lấy frame đầu tiên bằng MediaMetadataRetriever
                new Thread(() -> {
                    try {
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(finalUrl, new HashMap<>());
                        final android.graphics.Bitmap bitmap = retriever.getFrameAtTime(1000000); // frame tại 1s
                        retriever.release();

                        holder.imageView.post(() -> {
                            Glide.with(context)
                                    .load(bitmap)
                                    .centerCrop()
                                    .into(holder.imageView);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } else {
            // 👉 Nếu là IMAGE
            holder.playerView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(mediaUrl)
                    .centerCrop()
                    .into(holder.imageView);
        }

        // 🖱️ Click để chọn
        holder.itemView.setOnClickListener(v -> {
            post.setSelected(!post.isSelected());
            notifyItemChanged(position);
            if (selectionListener != null) selectionListener.onSelectionChanged();
        });

        // 🔥 Long click → mở chi tiết video / ảnh
        holder.itemView.setOnLongClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("mediaUrl", post.getMediaUrl());
            intent.putExtra("mediaType", post.getMediaType());
            intent.putExtra("caption", "Sample Post");
            intent.putExtra("likes", post.getLikeCount());
            intent.putExtra("comments", post.getCommentCount());
            intent.putExtra("timestamp", System.currentTimeMillis());
            context.startActivity(intent);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public int getTotalLikes() {
        int total = 0;
        for (PostItem post : postList)
            if (post.isSelected()) total += post.getLikeCount();
        return total;
    }

    public int getTotalComments() {
        int total = 0;
        for (PostItem post : postList)
            if (post.isSelected()) total += post.getCommentCount();
        return total;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        PlayerView playerView;
        TextView likeCount, commentCount;
        FrameLayout overlay;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.postImage);
            playerView = itemView.findViewById(R.id.postVideo);
            likeCount = itemView.findViewById(R.id.likeCount);
            commentCount = itemView.findViewById(R.id.commentCount);
            overlay = itemView.findViewById(R.id.overlay);

        }
    }
}