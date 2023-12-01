package com.yangdai.imagecraft.otherActivities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.yangdai.imagecraft.databinding.ActivityPhotoBinding;
import com.yangdai.imagecraft.imagedata.BitmapDecoder;


public class PhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPhotoBinding binding = ActivityPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAfterTransition();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        binding.photoView.setOnClickListener(v -> finishAfterTransition());

        Uri uri = getIntent().getParcelableExtra("uri");
        Bitmap bitmap = new BitmapDecoder(this, uri).getBitmap();
        int colorMode = ActivityInfo.COLOR_MODE_DEFAULT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (bitmap.hasGainmap()) {
                colorMode = ActivityInfo.COLOR_MODE_HDR;
            }
        }
        getWindow().setColorMode(colorMode);

        Glide.with(this).load(bitmap).into(binding.photoView);
    }

}