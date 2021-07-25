package com.developers.wajbaty.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.developers.wajbaty.R;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        findViewById(R.id.create_acc_btn).setOnClickListener(this);
        findViewById(R.id.sign_btn).setOnClickListener(this);
        findViewById(R.id.guest_btn).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.create_acc_btn) {
            startActivity(new Intent(this, RegisterActivity.class));
        } else if (v.getId() == R.id.sign_btn) {
            startActivity(new Intent(this, SigninActivity.class));
        } else if (v.getId() == R.id.guest_btn) {
//            startActivity(new Intent(this, RegisterActivity.class));
        }
    }
}