package com.example.justagram;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import com.example.justagram.fragment.Statistic.StatisticFragment;
import com.example.justagram.IgPublisherActivity;
public class HomeActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        openIgPublisherActivity();
    }

    private void openIgPublisherActivity() {
        Intent intent = new Intent(HomeActivity.this, IgPublisherActivity.class);
        startActivity(intent);
        setContentView(R.layout.activity_login) ;// gán layout vào activity
        StatisticFragment test = new StatisticFragment();
        //ReelPostFragment test = new ReelPostFragment();
        LoadFragment(test);

    }

    void LoadFragment(Fragment test)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.test_fragment,test).commit();
    }
}
