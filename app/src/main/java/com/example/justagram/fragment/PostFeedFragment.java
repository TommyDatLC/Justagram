package com.example.justagram.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justagram.R;

import java.util.Arrays;
import java.util.List;

public class PostFeedFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Gắn layout (trước là activity_post_feed.xml)
        View view = inflater.inflate(R.layout.fragment_post_feed, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setNestedScrollingEnabled(false);

        List<Integer> images = Arrays.asList(
                R.mipmap.picture_1, R.mipmap.picture_2, R.mipmap.picture_3,
                R.mipmap.picture_4, R.mipmap.picture_5, R.mipmap.picture_6,
                R.mipmap.picture_7, R.mipmap.picture_8, R.mipmap.picture_9,
                R.mipmap.picture_10, R.mipmap.picture_11, R.mipmap.picture_12,
                R.mipmap.picture_13, R.mipmap.picture_14, R.mipmap.picture_15,
                R.mipmap.picture_16, R.mipmap.picture_17, R.mipmap.picture_18
        );

        List<String> likes = Arrays.asList(
                "123", "456", "789", "145", "234", "678", "901", "520", "340",
                "124", "4567", "7899", "1455", "294", "678", "901", "520", "340"
        );

        PostAdapter adapter = new PostAdapter(requireContext(), images, likes);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
