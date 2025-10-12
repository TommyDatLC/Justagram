package com.example.justagram;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ReelPostFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Gắn layout activity_story_list.xml cho Fragment
        View view = inflater.inflate(R.layout.fragment_story_post, container, false);

        // Ánh xạ view
        View post1 = view.findViewById(R.id.post_item_1);
        View post2 = view.findViewById(R.id.post_item_2);
        View post3 = view.findViewById(R.id.post_item_3);

        // Xử lý sự kiện click
        View.OnClickListener postClickListener = v -> {
            v.setSelected(!v.isSelected());
        };

        post1.setOnClickListener(postClickListener);
        post2.setOnClickListener(postClickListener);
        post3.setOnClickListener(postClickListener);

        return view;
    }
}
