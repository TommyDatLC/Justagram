package com.example.justagram;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class HomeActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.home_page);

        var FragmentContainer = findViewById(R.id.test_fragment);

        InstagramAccountFragment test = new InstagramAccountFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(test,"vua").commit();

    }
}
