package com.yangdai.imagecraft.functions_special;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yangdai.colorpickerlib.ColorInfo;
import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.databinding.ActivityPickBinding;

import java.util.ArrayList;
import java.util.Objects;

public class PickColorActivity extends AppCompatActivity {
    private Uri uri;
    private ActivityPickBinding binding;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaForColor;
    private ActivityResultLauncher<Intent> pickImageLauncherForColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPickBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ArrayList<String> strList = getIntent().getStringArrayListExtra("uris");
        if (strList != null && !strList.isEmpty()) {
            uri = Uri.parse(strList.get(0));
            updateImage(uri);
        }
        pickMediaForColor = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::updateImage);
        pickImageLauncherForColor = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                updateImage(result.getData().getData());
            }
        });

        initUi();
    }

    private void initUi() {
        binding.colorPickerView.setOnColorSelectedListener((colorInfo, fromUser) -> setTextViewColor(colorInfo));
        if (binding.bottomAppBar != null) {
            binding.bottomAppBar.setOnMenuItemClickListener(item -> {
                String text = binding.colorText.getText().toString();
                if (item.getItemId() == R.id.copyColor) {
                    copyToClipboard(text);
                } else if (item.getItemId() == R.id.shareColor) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.app_name)));
                }
                return false;
            });
        }

        if (binding.copy != null) {
            binding.copy.setOnClickListener(v -> {
                String text = binding.colorText.getText().toString();
                copyToClipboard(text);
            });

        }
        if (binding.share != null) {
            binding.share.setOnClickListener(v -> {
                String text = binding.colorText.getText().toString();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(sendIntent, getString(R.string.app_name)));
            });
        }

        binding.btFloating.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PickColorActivity.this);
            int picker = sharedPreferences.getInt("picker", 0);
            if (picker == 0) {
                pickMediaForColor.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            } else {
                Intent intent = new Intent();
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intent.setAction(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncherForColor.launch(intent);
            }
        });
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("color", text);
        clipboardManager.setPrimaryClip(clipData);
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void setTextViewColor(ColorInfo colorInfo) {
        GradientDrawable backgroundDrawable = (GradientDrawable) getDrawable(R.drawable.rounded_corner_background);
        if (backgroundDrawable != null) {
            backgroundDrawable.setColor(colorInfo.getColor());
        }
        binding.colorText.setBackground(backgroundDrawable);
        binding.colorText.setText("#" + colorInfo.getHexCode());
    }

    private void updateImage(Uri uri) {
        this.uri = uri;
        binding.colorPickerView.setImageDrawable(null);
        Glide.with(this).asBitmap().load(uri).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                binding.colorPickerView.setImageBitmap(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }
}