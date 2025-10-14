package com.example.justagram.Fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SimpleFragment extends Fragment {

    private static final String ARG_TITLE = "title";

    public static SimpleFragment newInstance(String title) {
        SimpleFragment fragment = new SimpleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Use a FrameLayout to center the TextView
        FrameLayout layout = new FrameLayout(getContext());
        layout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        TextView textView = new TextView(getContext());
        textView.setText(getArguments() != null ? getArguments().getString(ARG_TITLE) : "No Title");
        textView.setTextSize(24f);
        textView.setGravity(Gravity.CENTER); // Center text inside TextView

        // Center the TextView inside the FrameLayout
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        textView.setLayoutParams(params);

        layout.addView(textView);
        return layout;
    }
}
