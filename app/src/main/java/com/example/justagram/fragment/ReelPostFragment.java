package com.example.justagram.fragment;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.justagram.InstagramService;
import com.example.justagram.R;
import com.example.justagram.Reel_video;

public class ReelPostFragment extends Fragment {
    private static final String TAG = "ReelPostFragment";
    private RecyclerView recyclerView;
    private ReelVideoAdapter adapter;
    private InstagramService instagramService;

    private static final String ACCESS_TOKEN = "IGAAS2qCIE595BZAFJ0SmVNaHBUbFFCM0NqOFBOYkdNOHhBdC1PR1hNTHV6ZAEtLZAm5RVTNZAa3lweFdqM0xxNVcwY2xLVlBadFdDUm54QkFBd0Jvdl8zRkJEMFFBNEtMZAkhyX2hfQUtIZAzNnVGdSa2pVYmtoX1I2bkZAxOFZAuOGp6VQZDZD";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reel_post, container, false);

        initViews(view);
        setupRecyclerView();
        loadReelsFromInstagram();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.reelsRecyclerView);
    }

    private void setupRecyclerView() {
        adapter = new ReelVideoAdapter(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Scroll listener để pause/play video khi scroll
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    adapter.pauseAllPlayers();
                }
            }
        });
    }

    private void loadReelsFromInstagram() {
        instagramService = new InstagramService(ACCESS_TOKEN);

        instagramService.fetchMyReels(new InstagramService.ReelsCallback() {
            @Override
            public void onSuccess(List<Reel_video> reels) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (reels != null && !reels.isEmpty()) {
                            adapter.setReelList(reels);
                            Toast.makeText(getContext(),
                                    "Loaded " + reels.size() + " reels",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Successfully loaded " + reels.size() + " reels");
                        } else {
                            Toast.makeText(getContext(),
                                    "No reels found",
                                    Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "No reels found");
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Error loading reels: " + error);
                        Toast.makeText(getContext(),
                                "Error: " + error,
                                Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.pauseAllPlayers();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.releaseAllPlayers();
        }
    }

    // Inner Adapter Class
    private static class ReelVideoAdapter extends RecyclerView.Adapter<ReelVideoAdapter.ReelViewHolder> {
        private final android.content.Context context;
        private List<Reel_video> reelList;
        private final Map<Integer, ExoPlayer> playerMap; // Cache players by position

        public ReelVideoAdapter(android.content.Context context) {
            this.context = context;
            this.reelList = new ArrayList<>();
            this.playerMap = new HashMap<>();
        }

        @NonNull
        @Override
        public ReelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_reel, parent, false);
            return new ReelViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReelViewHolder holder, int position) {
            Reel_video reel = reelList.get(position);

            holder.reelTitle.setText(reel.getTitle());
            holder.reelDate.setText(reel.getDate());
            holder.reelLikes.setText("♥ " + reel.getFormattedLikes());
            holder.reelComments.setText("💬 " + reel.getFormattedComments());
            holder.reelViews.setText("▶ " + reel.getFormattedViews());
            holder.reelDescription.setText(reel.getDescription());

            // Reuse player nếu đã có, hoặc tạo mới
            ExoPlayer player = playerMap.get(position);
            if (player == null) {
                player = new ExoPlayer.Builder(context).build();
                playerMap.put(position, player);

                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(reel.getVideoUrl()));
                player.setMediaItem(mediaItem);
                player.prepare();
                player.setPlayWhenReady(false);

                Log.d(TAG, "Created new player for position " + position);
            }

            holder.playerView.setPlayer(player);

            // Click listener để play/pause
            final ExoPlayer finalPlayer = player;
            holder.playerView.setOnClickListener(v -> {
                if (finalPlayer.isPlaying()) {
                    finalPlayer.pause();
                } else {
                    // Pause tất cả player khác trước
                    pauseAllPlayers();
                    finalPlayer.play();
                }
            });
        }

        @Override
        public void onViewRecycled(@NonNull ReelViewHolder holder) {
            super.onViewRecycled(holder);
            // Detach player khỏi view khi recycle
            if (holder.playerView.getPlayer() != null) {
                holder.playerView.getPlayer().pause();
                holder.playerView.setPlayer(null);
            }
        }

        @Override
        public int getItemCount() {
            return reelList.size();
        }

        public void setReelList(List<Reel_video> reelList) {
            // Release old players trước khi set list mới
            releaseAllPlayers();
            this.reelList = reelList;
            notifyDataSetChanged();
        }

        public void releaseAllPlayers() {
            for (ExoPlayer player : playerMap.values()) {
                if (player != null) {
                    player.release();
                }
            }
            playerMap.clear();
        }

        public void pauseAllPlayers() {
            for (ExoPlayer player : playerMap.values()) {
                if (player != null && player.isPlaying()) {
                    player.pause();
                }
            }
        }

        static class ReelViewHolder extends RecyclerView.ViewHolder {
            PlayerView playerView;
            TextView reelTitle;
            TextView reelDate;
            TextView reelLikes;
            TextView reelComments;
            TextView reelViews;
            TextView reelDescription;

            public ReelViewHolder(@NonNull View itemView) {
                super(itemView);
                playerView = itemView.findViewById(R.id.reelPlayerView);
                reelTitle = itemView.findViewById(R.id.reelTitle);
                reelDate = itemView.findViewById(R.id.reelDate);
                reelLikes = itemView.findViewById(R.id.reelLikes);
                reelComments = itemView.findViewById(R.id.reelComments);
                reelViews = itemView.findViewById(R.id.reelViews);
                reelDescription = itemView.findViewById(R.id.reelDescription);
            }
        }
    }
}