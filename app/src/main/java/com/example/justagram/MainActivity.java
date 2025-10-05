package com.example.justagram;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.home_page);
        super.onCreate(savedInstanceState);
        InstagramAccountFragment test = new InstagramAccountFragment();
        var transaction  = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.test_fragment,test).commit();


    }

}
