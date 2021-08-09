package com.developers.wajbaty.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import com.developers.wajbaty.R;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        Toolbar aboutToolbar = findViewById(R.id.aboutUsToolbar);

        aboutToolbar.setNavigationOnClickListener(v -> finish());
    }
}