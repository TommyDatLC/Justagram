package com.example.justagram;
// if u need to change something, here r the related files
// postItem.java
// layout : items_post_feed, fragment_post_feed, activity_main, activity_post_detail

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    private ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        ImageView imageDetail = findViewById(R.id.imageDetail);
        PlayerView videoDetail = findViewById(R.id.videoDetail);
        TextView tvCaption = findViewById(R.id.tvDetailCaption);
        TextView tvLikes = findViewById(R.id.tvLikes);
        TextView tvComments = findViewById(R.id.tvComments);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvDuration = findViewById(R.id.tvDuration);

        String mediaUrl = getIntent().getStringExtra("mediaUrl");
        String mediaType = getIntent().getStringExtra("mediaType");
        String caption = getIntent().getStringExtra("caption");
        int likes = getIntent().getIntExtra("likes", 0);
        int comments = getIntent().getIntExtra("comments", 0);
        long timestamp = getIntent().getLongExtra("timestamp", System.currentTimeMillis());

        if (caption != null && !caption.isEmpty()) {
            tvCaption.setText(caption);
            tvDescription.setText(caption);
        } else {
            tvCaption.setText("Untitled Post");
            tvDescription.setText("No description available");
        }

        tvLikes.setText("❤️ " + likes);
        tvComments.setText("💬 " + comments);

        SimpleDateFormat sdf = new SimpleDateFormat("d 'thg' M, yyyy", new Locale("vi", "VN"));
        String dateStr = sdf.format(new Date(timestamp));
        tvDate.setText(dateStr);

        if (mediaType != null && mediaType.equalsIgnoreCase("VIDEO")) {
            imageDetail.setVisibility(View.GONE);
            videoDetail.setVisibility(View.VISIBLE);
            tvDuration.setVisibility(View.VISIBLE);

            player = new ExoPlayer.Builder(this).build();
            videoDetail.setPlayer(player);

            MediaItem mediaItem = MediaItem.fromUri(mediaUrl);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(false);

            tvDuration.setText("16:26");

        } else {
            videoDetail.setVisibility(View.GONE);
            imageDetail.setVisibility(View.VISIBLE);
            tvDuration.setVisibility(View.GONE);

            Glide.with(this)
                    .load(mediaUrl)
                    .centerCrop()
                    .into(imageDetail);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}