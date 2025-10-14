package com.example.justagram;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.justagram.fragment.Statistic.StatisticFragment;
import com.example.justagram.IgPublisherActivity;
public class HomeActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_ig_publisher_tabs);
        IgPublisherActivity test = new IgPublisherActivity();
        //ReelPostFragment test = new ReelPostFragment();
        LoadFragment(test);
    }
    void LoadFragment(Fragment test)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.test_fragment,test).commit();
    }
    void TestInstagramPostFragment() {
        setContentView(R.layout.activity_home_page);
        IgPublisherFragment test = new IgPublisherFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.add(R.id.test_fragment, test).commit();
        // Open the Activity version instead of fragment so buttons interact immediately
        Intent i = new Intent(HomeActivity.this, IgPublisherActivity.class);
        startActivity(i);
    }



}
