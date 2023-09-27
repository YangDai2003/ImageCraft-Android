package com.yangdai.imagecraft.functions_standard;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.yangdai.imagecraft.databinding.ActivityRotateBinding;
import com.yangdai.imagecraft.imagedata.BitmapDecoder;
import com.yangdai.imagecraft.imagedata.BitmapUtils;
import com.yangdai.imagecraft.imagedata.ImageTypeEnum;
import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.base.BaseImageProcessingActivity;


public class RotateActivity extends BaseImageProcessingActivity {
    private int degree = 0;
    private boolean isVFlipped = false;
    private boolean isHFlipped = false;
    private ActivityRotateBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRotateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        calculateTotalMemorySize(binding.infoContent);
        setRunnable();
    }

    @Override
    protected void processImage(Uri uri) {
        BitmapDecoder bitmapDecoder = new BitmapDecoder(viewModel.getContext(), uri);
        Bitmap bitmap = bitmapDecoder.getBitmap();
        ImageTypeEnum type = bitmapDecoder.getImageType();
        if (bitmap != null) {
            try {
                Bitmap rotatedBitmap = BitmapUtils.rotateBitmap(bitmap, degree, isVFlipped, isHFlipped);
                BitmapUtils.saveImage(rotatedBitmap, viewModel.getContext(), type);
                rotatedBitmap.recycle();
                viewModel.addTaskDone();
            } finally {
                bitmap.recycle();
            }
        }
    }

    @Override
    protected void initUi() {
        binding.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.button0) {
                    degree = 0;
                    binding.imageAfter.setImageResource(R.drawable.example);
                } else if (checkedId == R.id.button90) {
                    degree = 90;
                    binding.imageAfter.setImageResource(R.drawable.example_90);
                } else if (checkedId == R.id.button180) {
                    degree = 180;
                    binding.imageAfter.setImageResource(R.drawable.example_180);
                } else {
                    degree = 270;
                    binding.imageAfter.setImageResource(R.drawable.example_270);
                }
                changeExample(isVFlipped, isHFlipped);
            }
        });

        binding.flipV.setOnCheckedChangeListener((compoundButton, b) -> {
            isVFlipped = b;
            changeExample(isVFlipped, isHFlipped);
        });
        binding.flipH.setOnCheckedChangeListener((compoundButton, b) -> {
            isHFlipped = b;
            changeExample(isVFlipped, isHFlipped);
        });

        binding.btFloating.setOnClickListener(v -> start());
    }

    private void changeExample(boolean isVFlipped, boolean isHFlipped) {
        binding.imageAfter.setScaleX(isHFlipped ? -1 : 1);
        binding.imageAfter.setScaleY(isVFlipped ? -1 : 1);
    }

}