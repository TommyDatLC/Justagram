package com.example.justagram.Helper;

public interface ScrollAwareFragment {
    void setOnScrollChangeListener(OnScrollChangeListener listener);

    interface OnScrollChangeListener {
        void onScrollUp();
        void onScrollDown();
    }
}
