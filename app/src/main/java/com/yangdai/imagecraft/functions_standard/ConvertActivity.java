package com.yangdai.imagecraft.functions_standard;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.yangdai.imagecraft.databinding.ActivityConvertBinding;
import com.yangdai.imagecraft.imagedata.BitmapDecoder;
import com.yangdai.imagecraft.imagedata.BitmapUtils;
import com.yangdai.imagecraft.imagedata.ImageType;
import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.base.BaseImageProcessingActivity;

public class ConvertActivity extends BaseImageProcessingActivity {
    private ImageType type = ImageType.JPEG;
    private ActivityConvertBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConvertBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        calculateTotalMemorySize(binding.infoContent);
        enableMultiThreadProcessing();
    }

    @Override
    protected void processImage(Uri uri) {
        BitmapDecoder bitmapDecoder = new BitmapDecoder(viewModel.getContext(), uri);
        Bitmap bitmap = bitmapDecoder.getBitmap();
        if (bitmap != null) {
            try {
                BitmapUtils.saveImage(bitmap, viewModel.getContext(), type);
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
                if (checkedId == R.id.buttonJPEG) {
                    type = ImageType.JPEG;
                } else if (checkedId == R.id.buttonPNG) {
                    type = ImageType.PNG;
                } else {
                    type = ImageType.WEBP;
                }
            }
        });
        binding.btFloating.setOnClickListener(v -> start());
    }
}