package com.yangdai.imagecraft.functions_standard;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.databinding.ActivityCompressBinding;
import com.yangdai.imagecraft.imagedata.BitmapDecoder;
import com.yangdai.imagecraft.imagedata.BitmapUtils;
import com.yangdai.imagecraft.imagedata.ImageType;
import com.yangdai.imagecraft.utils.Utils;
import com.yangdai.imagecraft.base.BaseImageProcessingActivity;

public class CompressActivity extends BaseImageProcessingActivity {
    private boolean isAuto = true;
    private int size = 2048;
    private ActivityCompressBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        calculateTotalMemorySize(binding.infoContent);
        enableMultiThreadProcessing();
    }

    @Override
    protected void processImage(Uri uri) {
        BitmapDecoder bitmapDecoder = new BitmapDecoder(viewModel.getContext(), uri);
        Bitmap bitmap = bitmapDecoder.getBitmap();
        ImageType type = bitmapDecoder.getImageType();
        if (bitmap != null) {
            try {
                Bitmap compressedBitmap = isAuto ? BitmapUtils.compressBitmap(bitmap, type) : BitmapUtils.compressBitmap(bitmap, type, size);
                if (type == ImageType.PNG) {
                    type = ImageType.JPEG;
                }
                if (compressedBitmap != null) {
                    // 保存更改后的图像
                    BitmapUtils.saveImage(compressedBitmap, viewModel.getContext(), type);
                    compressedBitmap.recycle();
                }
                viewModel.addTaskDone();
            } finally {
                bitmap.recycle();
            }
        }
    }

    @Override
    protected void initUi() {
        binding.radioGroup.setOnCheckedChangeListener((radioGroup, id) -> {
            isAuto = id == R.id.auto;
            binding.lrForSlider.setVisibility(isAuto ? View.INVISIBLE : View.VISIBLE);
        });
        binding.slider.addOnChangeListener((slider, value, fromUser) -> {
            size = (int) value;
            binding.edKb.setText(String.valueOf(size));
        });
        binding.edKb.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                binding.edKb.setSelection(editable.length());
                try {
                    int p = Integer.parseInt(editable.toString());
                    if (p > 499 && p < 2049) {
                        binding.slider.setValue(p);
                        size = p;
                    }
                } catch (Exception ignored) {

                }
            }
        });
        binding.edKb.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                Utils.closeKeyboard(CompressActivity.this);
                binding.edKb.clearFocus();
                return true;
            }
            return false;
        });
        binding.btFloating.setOnClickListener(v -> start());
    }
}