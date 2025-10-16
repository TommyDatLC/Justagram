package com.example.justagram;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.justagram.fragment.Statistic.StatisticFragment;

public class HomeActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        StatisticFragment test = new StatisticFragment();
        //ReelPostFragment test = new ReelPostFragment();
        LoadFragment(test);
    }

    void LoadFragment(Fragment test) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.test_fragment, test).commit();
    }
}
