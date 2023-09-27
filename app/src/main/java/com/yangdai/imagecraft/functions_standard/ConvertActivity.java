package com.yangdai.imagecraft.functions_standard;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.yangdai.imagecraft.databinding.ActivityConvertBinding;
import com.yangdai.imagecraft.imagedata.BitmapDecoder;
import com.yangdai.imagecraft.imagedata.BitmapUtils;
import com.yangdai.imagecraft.imagedata.ImageTypeEnum;
import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.base.BaseImageProcessingActivity;

public class ConvertActivity extends BaseImageProcessingActivity {
    private ImageTypeEnum type = ImageTypeEnum.JPEG;
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
                    type = ImageTypeEnum.JPEG;
                } else if (checkedId == R.id.buttonPNG) {
                    type = ImageTypeEnum.PNG;
                } else {
                    type = ImageTypeEnum.WEBP;
                }
            }
        });
        binding.btFloating.setOnClickListener(v -> start());
    }
}