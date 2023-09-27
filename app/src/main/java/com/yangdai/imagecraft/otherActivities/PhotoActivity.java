package com.yangdai.imagecraft.otherActivities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.yangdai.imagecraft.R;
import com.yangdai.imageviewpro.ImageViewPro;


public class PhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAfterTransition();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        ImageViewPro photoView = findViewById(R.id.photoView);
        photoView.setOnClickListener(v -> finishAfterTransition());
        Uri uri = getIntent().getParcelableExtra("uri");
        RequestBuilder<Drawable> requestBuilder = Glide.with(this).asDrawable().sizeMultiplier(0.5f);
        Glide.with(this).load(uri).thumbnail(requestBuilder).into(photoView);
    }

}