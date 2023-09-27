package com.yangdai.imagecraft.otherActivities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.yangdai.imagecraft.databinding.ActivityPhotoBinding;


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
        RequestBuilder<Drawable> requestBuilder = Glide.with(this).asDrawable().sizeMultiplier(0.5f);
        Glide.with(this).load(uri).thumbnail(requestBuilder).into(binding.photoView);
    }

}