package com.yangdai.imagecraft.functions_special;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.DecimalFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.snackbar.Snackbar;
import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.databinding.ActivityExifBinding;
import com.yangdai.imagecraft.utils.FileUtils;
import com.yangdai.imagecraft.imagedata.BitmapDecoder;
import com.yangdai.imagecraft.imagedata.BitmapUtils;
import com.yangdai.imagecraft.imagedata.ExifAdapter;
import com.yangdai.imagecraft.imagedata.ExifItem;
import com.yangdai.imagecraft.utils.Utils;
import com.yangdai.imagecraft.otherActivities.PhotoActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

public class ExifActivity extends AppCompatActivity {
    private Uri uri;
    private BitmapDecoder bitmapDecoder;
    private ExifAdapter adapter;
    private ActivityExifBinding binding;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            saveExifInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExifBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable((SurfaceColors.SURFACE_0.getColor(this))));
        getSupportActionBar().setElevation(0f);

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        ArrayList<String> strList = getIntent().getStringArrayListExtra("uris");
        if (strList != null && !strList.isEmpty()) {
            uri = Uri.parse(strList.get(0));
        }

        initUi();
        showTotalMemorySizeInMB();

        Glide.with(this).load(uri).into(binding.imageView);

        populateRecyclerView();
    }

    @SuppressLint("SetTextI18n")
    private void showTotalMemorySizeInMB() {
        CompletableFuture.supplyAsync(() -> {
            long totalMemorySize = 0;
            try {
                try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                    if (inputStream != null) {
                        int fileSize = inputStream.available();
                        totalMemorySize += fileSize;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return totalMemorySize / (1024.0 * 1024.0);
        }).thenAccept(totalMemorySizeInMB -> {
            String formattedMemorySize = new DecimalFormat("#.##").format(totalMemorySizeInMB);
            runOnUiThread(() -> binding.infoContent.setText(formattedMemorySize + " MB"));
        });
    }

    private void initUi() {
        binding.btFloating.setOnClickListener(v -> showMissingKeysDialog());
        binding.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(ExifActivity.this, PhotoActivity.class);
            intent.putExtra("uri", uri);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ExifActivity.this, binding.imageView, "image");
            startActivity(intent, options.toBundle());
        });
    }

    private void showMissingKeysDialog() {
        List<String> missingKeysList = new ArrayList<>();

        for (String key : BitmapUtils.keysArray) {
            boolean found = getNewExifInfoList().stream().anyMatch(exifItem -> exifItem.name().equals(key));
            if (!found) {
                missingKeysList.add(key);
            }
        }
        String[] missingKeysArr = missingKeysList.toArray(new String[0]);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.exif)
                .setMultiChoiceItems(missingKeysArr, null, (dialog, which, isChecked) -> {
                })
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    SparseBooleanArray checkedItems = ((AlertDialog) dialog).getListView().getCheckedItemPositions();
                    IntStream.range(0, missingKeysArr.length)
                            .filter(checkedItems::get)
                            .mapToObj(i -> new ExifItem(missingKeysArr[i], ""))
                            .forEach(newExifItem -> adapter.addItem(newExifItem));
                })
                .show();
    }

    private void populateRecyclerView() {
        CompletableFuture.supplyAsync(() -> {
            bitmapDecoder = new BitmapDecoder(this, uri);
            Map<String, String> exifInfo = bitmapDecoder.getExifInfo();
            List<ExifItem> exifInfoList = new ArrayList<>(exifInfo.size());
            exifInfoList.add(new ExifItem("Width", String.valueOf(bitmapDecoder.getWidth())));
            exifInfoList.add(new ExifItem("Height", String.valueOf(bitmapDecoder.getHeight())));
            exifInfoList.add(new ExifItem("Orientation", String.valueOf(bitmapDecoder.getOrientation())));
            exifInfoList.add(new ExifItem("Format", String.valueOf(bitmapDecoder.getFormat())));

            exifInfo.forEach((name, data) -> {
                if (name != null && !name.isEmpty() && data != null && !data.isEmpty()) {
                    ExifItem exifItem = new ExifItem(name, data);
                    exifInfoList.add(exifItem);
                }
            });

            return exifInfoList;
        }).thenAccept(exifInfoList -> {
            adapter = new ExifAdapter(exifInfoList, this);
            runOnUiThread(() -> {
                binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
                binding.recyclerView.setAdapter(adapter);
            });
        });
    }

    private void saveExifInfo() {
        String uriStr = BitmapUtils.saveImage(bitmapDecoder.getBitmap(), this, bitmapDecoder.getImageType());
        String path = FileUtils.getRealPathFromUri(Uri.parse(uriStr), this);

        try {
            ExifInterface exifInterface = new ExifInterface(path);
            for (ExifItem exifItem : getNewExifInfoList()) {
                exifInterface.setAttribute(exifItem.name(), exifItem.data());
            }
            exifInterface.saveAttributes();
            Snackbar.make(binding.btFloating, getString(R.string.saved), Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.check), view -> Utils.viewAlbumInGallery(this, Uri.parse(uriStr)))
                    .show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<ExifItem> getNewExifInfoList() {
        return adapter.getNewExifInfoList();
    }
}