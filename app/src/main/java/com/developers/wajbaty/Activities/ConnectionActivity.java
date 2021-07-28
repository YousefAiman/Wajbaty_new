package com.developers.wajbaty.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.WifiUtil;

public class ConnectionActivity extends AppCompatActivity {

    public static final int CONNECTION_RESULT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        findViewById(R.id.connectionRetryBtn).setOnClickListener(v -> {

            if (WifiUtil.isConnectedToInternet(this)) {

                setResult(CONNECTION_RESULT);
                finish();

            } else {

                Toast.makeText(ConnectionActivity.this,
                        "Please check your internet connection!", Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}