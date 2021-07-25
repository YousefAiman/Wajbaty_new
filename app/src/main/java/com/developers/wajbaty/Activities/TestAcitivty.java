package com.developers.wajbaty.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import com.developers.wajbaty.Adapters.ImagePagerAdapter;
import com.developers.wajbaty.R;

import java.util.ArrayList;

public class TestAcitivty extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_acitivty);

//        ArrayList<String> images = new ArrayList<>();
//        images.add("https://firebasestorage.googleapis.com/v0/b/wajbatytestproject.appspot.com/o/1%2FRestaurant_Main_Image?alt=media&token=c52c128e-3c36-41a7-b78a-18d337c76033");
//        images.add("https://firebasestorage.googleapis.com/v0/b/wajbatytestproject.appspot.com/o/1%2FRestaurant_Main_Image?alt=media&token=c52c128e-3c36-41a7-b78a-18d337c76033");
//        images.add("https://firebasestorage.googleapis.com/v0/b/wajbatytestproject.appspot.com/o/1%2FRestaurant_Main_Image?alt=media&token=c52c128e-3c36-41a7-b78a-18d337c76033");
//        ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(images);
//
//
//        ViewPager viewPager = findViewById(R.id.viewPager);
//        viewPager.setAdapter(imagePagerAdapter);

    }
}