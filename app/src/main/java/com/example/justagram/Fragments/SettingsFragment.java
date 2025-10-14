package com.example.justagram.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.justagram.Helper.ScrollAwareFragment;
import com.example.justagram.R;

public class SettingsFragment extends Fragment implements ScrollAwareFragment {

    private OnScrollChangeListener scrollChangeListener;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        NestedScrollView scrollView = view.findViewById(R.id.nestedScrollViewSettings);
        if (scrollView == null) return;

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            private int lastY = 0;

            @Override
            public void onScrollChanged() {
                int currentY = scrollView.getScrollY();
                if (scrollChangeListener == null) return;

                if (currentY > lastY + 10) {
                    scrollChangeListener.onScrollDown();
                } else if (currentY < lastY - 10) {
                    scrollChangeListener.onScrollUp();
                }
                lastY = currentY;
            }
        });
    }

    @Override
    public void setOnScrollChangeListener(OnScrollChangeListener listener) {
        this.scrollChangeListener = listener;
    }
}
