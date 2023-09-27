package com.yangdai.imagecraft.functions_special;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.utils.FileUtils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


public class ThemeActivity extends AppCompatActivity {
    private Uri uri;
    private AlertDialog alertDialog;
    private DynamicColorsOptions dynamicColorsOptions;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<String> strList = getIntent().getStringArrayListExtra("uris");
        if (strList != null && !strList.isEmpty()) {
            uri = Uri.parse(strList.get(0));
        }

        alertDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.processing)
                .setView(R.layout.custom_progress_dialog_circle)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> ThemeActivity.this.finish())
                .create();
        alertDialog.show();

        CompletableFuture.supplyAsync(() -> BitmapFactory.decodeFile(FileUtils.getRealPathFromUri(uri, ThemeActivity.this)))
                .thenAcceptAsync(bitmap -> {
                    if (bitmap != null) {
                        dynamicColorsOptions = new DynamicColorsOptions.Builder()
                                .setContentBasedSource(bitmap)
                                .build();
                        DynamicColors.applyToActivityIfAvailable(this,
                                dynamicColorsOptions);
                    }
                    runOnUiThread(() -> {
                        setContentView(R.layout.activity_theme);
                        getWindow().setStatusBarColor(SurfaceColors.SURFACE_5.getColor(this));
                        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(SurfaceColors.SURFACE_5.getColor(this)));
                        alertDialog.dismiss();
                    });
                });
    }
}
