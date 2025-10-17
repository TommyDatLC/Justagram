package com.example.justagram;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.justagram.fragment.ReelPostFragment;

public class ContentPagerAdapter extends FragmentStateAdapter {

    public ContentPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PostFeedFragment(); // Fragment cho Posts
            case 1:
                return new ReelPostFragment(); // Fragment cho Reels
            default:
                return new PostFeedFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // 2 tabs: Posts và Reels
    }
}