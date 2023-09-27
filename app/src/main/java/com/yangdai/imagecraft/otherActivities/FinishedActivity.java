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

public class FinishedActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished);
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable((SurfaceColors.SURFACE_0.getColor(this))));
        getSupportActionBar().setElevation(0f);
        imageView = findViewById(R.id.image_done);
        textView = findViewById(R.id.info_task);
        int total = getIntent().getIntExtra("total", 0);
        int done = getIntent().getIntExtra("done", 0);
        String finished = getString(R.string.finished);
        String toShow = String.format(finished, done, total - done);
        textView.setText(toShow);
        startZoomAnimation(imageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageView.clearAnimation();
    }
}