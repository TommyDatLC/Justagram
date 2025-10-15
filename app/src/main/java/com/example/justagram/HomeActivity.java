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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        openIgPublisherActivity();
    }

    private void openIgPublisherActivity() {
        Intent intent = new Intent(HomeActivity.this, IgPublisherActivity.class);
        startActivity(intent);
    }
    void LoadFragment(Fragment test)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.test_fragment,test).commit();
    }




}
