package com.example.justagram;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep your layout alive! Friends can still see it
        setContentView(R.layout.activity_home_page);

        // Launch DashboardActivity
        Intent intent = new Intent(HomeActivity.this, DashboardActivity.class);

        // Make sure HomeActivity is cleared from the back stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);

        // Close HomeActivity so back button goes to LoginActivity or exits app
        finish();
    }
}
