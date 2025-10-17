package com.example.justagram;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ContentManagerFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ContentPagerAdapter pagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content_manager, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        setupViewPager();

        return view;
    }

    private void setupViewPager() {
        pagerAdapter = new ContentPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(" Posts");
                    break;
                case 1:
                    tab.setText(" Reels");
                    break;
            }
        }).attach();

        // Optional: Smooth swipe
        viewPager.setOffscreenPageLimit(1);
    }
}