package com.developers.wajbaty.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.developers.wajbaty.R;

import java.io.Serializable;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    private Map<String, Object> addressMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        addressMap = (Map<String, Object>) getIntent().getSerializableExtra("addressMap");

        findViewById(R.id.create_acc_btn).setOnClickListener(this);
        findViewById(R.id.sign_btn).setOnClickListener(this);
        findViewById(R.id.guest_btn).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.create_acc_btn) {
            startActivity(new Intent(this, RegisterActivity.class)
                    .putExtra("addressMap", (Serializable) addressMap));
        } else if (v.getId() == R.id.sign_btn) {
            startActivity(new Intent(this, SigninActivity.class)
                    .putExtra("addressMap", (Serializable) addressMap));
        } else if (v.getId() == R.id.guest_btn) {
//            startActivity(new Intent(this, RegisterActivity.class));
        }
    }
}