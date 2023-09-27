package com.yangdai.imagecraft.otherActivities;

import static com.yangdai.imagecraft.utils.Utils.startZoomAnimation;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.elevation.SurfaceColors;
import com.yangdai.imagecraft.R;

import java.util.Objects;

public class CancelledActivity extends AppCompatActivity {
    ImageView imageView;
    TextView textView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancelled);
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable((SurfaceColors.SURFACE_0.getColor(this))));
        getSupportActionBar().setElevation(0f);
        imageView = findViewById(R.id.image_cancelled);
        textView = findViewById(R.id.info_task);
        String cancelled = getString(R.string.cancelled);
        textView.setText(cancelled);
        startZoomAnimation(imageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageView.clearAnimation();
    }
}