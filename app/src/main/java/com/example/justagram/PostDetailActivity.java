package com.example.justagram;
import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class PostDetailActivity extends AppCompatActivity {
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        ImageView image = findViewById(R.id.imageDetail);
        VideoView video = findViewById(R.id.videoDetail);
        TextView caption = findViewById(R.id.captionDetail);
        TextView likes = findViewById(R.id.likesDetail);
        TextView comments = findViewById(R.id.commentsDetail);

        String mediaUrl = getIntent().getStringExtra("mediaUrl");
        String mediaType = getIntent().getStringExtra("mediaType");
        String captionText = getIntent().getStringExtra("caption");
        int likeCount = getIntent().getIntExtra("likeCount", 0);
        int commentCount = getIntent().getIntExtra("commentsCount", 0);

        caption.setText(captionText);
        likes.setText("❤️ " + likeCount + " likes");
        comments.setText("💬 " + commentCount + " comments");

        if ("VIDEO".equals(mediaType)) {
            video.setVisibility(VideoView.VISIBLE);
            video.setVideoURI(Uri.parse(mediaUrl));
            video.setOnPreparedListener(mp -> mp.setLooping(true));
            video.start();
        } else {
            image.setVisibility(ImageView.VISIBLE);
            Glide.with(this).load(mediaUrl).into(image);
        }
    }
}
