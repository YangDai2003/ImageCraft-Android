package com.yangdai.imagecraft.functions_standard;

import android.annotation.SuppressLint;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.yangdai.imagecraft.databinding.ActivityResizeBinding;
import com.yangdai.imagecraft.imagedata.BitmapDecoder;
import com.yangdai.imagecraft.imagedata.BitmapUtils;
import com.yangdai.imagecraft.imagedata.ImageType;
import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.utils.Utils;
import com.yangdai.imagecraft.base.BaseImageProcessingActivity;


public class ResizeActivity extends BaseImageProcessingActivity {
    private boolean isPercentage = true;
    private int percentage = 50;
    private int width, height;
    private ActivityResizeBinding binding;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResizeBinding.inflate(getLayoutInflater());
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
                Bitmap resizedBitmap = isPercentage ?
                        BitmapUtils.resizeBitmapByPercentage(bitmap, percentage) : BitmapUtils.resizeBitmapByPixel(bitmap, width, height);
                BitmapUtils.saveImage(resizedBitmap, viewModel.getContext(), type);
                resizedBitmap.recycle();
                viewModel.addTaskDone();
            } finally {
                bitmap.recycle();
            }
        }
    }

    @Override
    protected void initUi() {
        binding.radioGroup.setOnCheckedChangeListener((radioGroup, id)
                -> {
            isPercentage = id == R.id.percent;
            if (isPercentage) {
                binding.lrForSlider.setVisibility(View.VISIBLE);
                binding.lrForCustom.setVisibility(View.GONE);
            } else {
                binding.lrForCustom.setVisibility(View.VISIBLE);
                binding.lrForSlider.setVisibility(View.GONE);
            }
        });
        binding.slider.addOnChangeListener((slider, value, fromUser) -> {
            percentage = (int) value;
            binding.edPercent.setText(String.valueOf(percentage));
        });
        binding.edPercent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                binding.edPercent.setSelection(editable.length());
                try {
                    int p = Integer.parseInt(editable.toString());
                    if (p > 0 && p < 100) {
                        binding.slider.setValue(p);
                    }
                } catch (Exception ignored) {

                }
            }
        });
        binding.edPercent.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                Utils.closeKeyboard(ResizeActivity.this);
                binding.edPercent.clearFocus();
                return true;
            }
            return false;
        });
        binding.edHeight.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                Utils.closeKeyboard(ResizeActivity.this);
                binding.edHeight.clearFocus();
                return true;
            }
            return false;
        });
        binding.edWidth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                binding.edWidth.setSelection(editable.length());
                try {
                    width = Integer.parseInt(editable.toString());
                } catch (Exception ignored) {

                }
            }
        });
        binding.edHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                binding.edHeight.setSelection(editable.length());
                try {
                    height = Integer.parseInt(editable.toString());
                } catch (Exception ignored) {

                }
            }
        });
        binding.btFloating.setOnClickListener(v -> start());
    }
}