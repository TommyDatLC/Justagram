package com.example.justagram.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.justagram.Helper.ScrollAwareFragment;
import com.example.justagram.R;

public class AnalyticsFragment extends Fragment implements ScrollAwareFragment {
    public static AnalyticsFragment newInstance() {
        AnalyticsFragment fragment = new AnalyticsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private OnScrollChangeListener scrollChangeListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = view.findViewById(R.id.scrollViewAnalytics);

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
