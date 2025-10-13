package com.example.justagram;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Integer> postImages;
    private List<String> likeCounts;
    private Set<Integer> selectedPositions = new HashSet<>();

    // ✅ Constructor đầy đủ 3 tham số
    public PostAdapter(Context context, List<Integer> postImages, List<String> likeCounts) {
        this.context = context;
        this.postImages = postImages;
        this.likeCounts = likeCounts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_post_feed, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.postImage.setImageResource(postImages.get(position));
        holder.likeCount.setText("♥ " + likeCounts.get(position));

        // Hiển thị overlay nếu đang được chọn
        boolean isSelected = selectedPositions.contains(position);
        holder.overlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        // Khi click — tính lại trạng thái tại thời điểm click
        holder.itemView.setOnClickListener(v -> {
            boolean currentlySelected = selectedPositions.contains(position);

            if (currentlySelected) {
                selectedPositions.remove(position);
            } else {
                selectedPositions.add(position);
            }

            // 🔥 Gọi cập nhật lại chỉ item này
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return postImages.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImage;
        FrameLayout overlay;
        TextView likeCount;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.postImage);
            overlay = itemView.findViewById(R.id.overlay);
            likeCount = itemView.findViewById(R.id.likeCount);
        }
    }
}

