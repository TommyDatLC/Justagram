package com.example.justagram;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import com.example.justagram.fragment.Statistic.StatisticFragment;
public class HomeActivity extends AppCompatActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login) ;// gán layout vào activity
        StatisticFragment test = new StatisticFragment();
        //ReelPostFragment test = new ReelPostFragment();
        LoadFragment(test);
        Button a = new Button(this);
        a.setOnClickListener((v) -> {

        });
    }

    void LoadFragment(Fragment test)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.test_fragment,test).commit();
    }

}

