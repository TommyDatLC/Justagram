package com.example.justagram;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.justagram.Statistic.StatisticFragment;

public class HomeActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
       // InstagramAccountFragment test = new InstagramAccountFragment();
        ReelPostFragment test = new ReelPostFragment();
        LoadFragment(test);
    }
    void LoadFragment(Fragment test)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.test_fragment,test).commit();
    }
}
