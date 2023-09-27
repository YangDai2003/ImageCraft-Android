package com.yangdai.imagecraft.base;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.DecimalFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.MultiBrowseCarouselStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.imagedata.CarouselAdapter;
import com.yangdai.imagecraft.otherActivities.CancelledActivity;
import com.yangdai.imagecraft.otherActivities.FinishedActivity;
import com.yangdai.imagecraft.otherActivities.PhotoActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class BaseImageProcessingActivity extends AppCompatActivity {
    protected BaseViewModel viewModel;
    protected LinearProgressIndicator indicator;
    protected TextView progressTv;
    protected AlertDialog materialDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        viewModel = new ViewModelProvider(this).get(BaseViewModel.class);
        viewModel.getTaskDone().observe(this, progress -> {
            if (progress != 0) {
                int currentPercent = progress * 100 / viewModel.getTaskCount();
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        indicator.setProgressCompat(currentPercent, true);
                        progressTv.setText(currentPercent + "%");
                    } catch (Exception e) {
                        setMaterialDialog();
                        materialDialog.show();
                    }
                });
            }
        });
        viewModel.getFinishStatus().observe(this, isFinished -> {
            if (isFinished && !viewModel.isCancelled()) {
                materialDialog.dismiss();
                Intent intent = new Intent(this, FinishedActivity.class);
                intent.putExtra("total", viewModel.getTaskCount());
                intent.putExtra("done", viewModel.getTaskDone().getValue());
                startActivity(intent);
                finish();
            }
        });
        viewModel.getCancelStatus().observe(this, isCancelled -> {
            if (isCancelled) {
                viewModel.futures.forEach(future -> future.cancel(true));
                Intent intent = new Intent(this, CancelledActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    protected void init() {
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable((SurfaceColors.SURFACE_0.getColor(this))));
        getSupportActionBar().setElevation(0f);
        initUi();
        initializeCarousel();
    }

    protected void initSpecial() {
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable((SurfaceColors.SURFACE_0.getColor(this))));
        getSupportActionBar().setElevation(0f);
        ArrayList<String> strList = getIntent().getStringArrayListExtra("uris");
        if (strList != null && !strList.isEmpty()) {
            if (viewModel.isFirstInitiated()) {
                viewModel.setTaskCount(strList.size());
                for (String str : strList) {
                    Uri uri = Uri.parse(str);
                    viewModel.getUriList().add(uri);
                }
                viewModel.setFirstInit(false);
            }
        }
        initUi();
    }

    protected abstract void initUi();

    protected void processImage(Uri uri) {

    }

    protected void enableMultiThreadProcessing() {
        viewModel.setRunnable(() -> {
            try {
                List<Uri> uriList = viewModel.getUriList();
                List<Callable<Void>> tasks = new ArrayList<>();

                for (Uri uri : uriList) {
                    if (viewModel.isCancelled()) {
                        break;
                    }
                    tasks.add(() -> {
                        if (Thread.interrupted() || viewModel.isCancelled()) {
                            return null;
                        }
                        // 在这里执行实际的任务逻辑
                        processImage(uri);
                        return null;
                    });
                }

                viewModel.futures = viewModel.executor.invokeAll(tasks);
                // 处理任务结果
                for (Future<Void> future : viewModel.futures) {
                    future.get();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                // 关闭线程池
                viewModel.executor.shutdown();
                viewModel.setFinished(true);
            }
        });
    }

    protected void start() {
        setMaterialDialog();
        materialDialog.show();
        viewModel.start();
    }

    private void setMaterialDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View inflatedView = inflater.inflate(R.layout.custom_progress_dialog_linear, null);
        indicator = inflatedView.findViewById(R.id.progressBar);
        progressTv = inflatedView.findViewById(R.id.progressText);
        materialDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.processing)
                .setView(inflatedView)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> viewModel.setCancelled(true))
                .create();
    }

    private void initializeCarousel() {
        ArrayList<String> strList = getIntent().getStringArrayListExtra("uris");
        if (strList != null && !strList.isEmpty()) {
            if (viewModel.isFirstInitiated()) {
                viewModel.setTaskCount(strList.size());
                for (String str : strList) {
                    Uri uri = Uri.parse(str);
                    viewModel.getUriList().add(uri);
                }
                viewModel.setFirstInit(false);
            }
            setupRecyclerView(viewModel.getUriList());
        }
    }

    private void setupRecyclerView(List<Uri> uriList) {
        RecyclerView recyclerView = findViewById(R.id.carousel_recycler_view);
        CarouselLayoutManager carouselLayoutManager = new CarouselLayoutManager();
        carouselLayoutManager.setCarouselStrategy(new MultiBrowseCarouselStrategy());
        recyclerView.setLayoutManager(carouselLayoutManager);
        CarouselAdapter adapter = new CarouselAdapter(uriList);
        adapter.setOnItemClickListener((uri, view) -> {
            Intent intent = new Intent(this, PhotoActivity.class);
            intent.putExtra("uri", uri);
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, view, "image");
            startActivity(intent, options.toBundle());
        });
        recyclerView.setAdapter(adapter);
    }

    @SuppressLint("SetTextI18n")
    protected void calculateTotalMemorySize(TextView textView) {
        CompletableFuture.supplyAsync(() -> {
            long totalMemorySize = 0;
            for (Uri uri : viewModel.getUriList()) {
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
            }
            return totalMemorySize / (1024.0 * 1024.0);
        }).thenApplyAsync(totalMemorySizeInMB -> {
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            String formattedMemorySize = decimalFormat.format(totalMemorySizeInMB);
            return viewModel.getUriList().size() + " " + getString(R.string.selected) + " " + formattedMemorySize + " MB";
        }).thenAccept(result -> runOnUiThread(() -> textView.setText(result)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (materialDialog != null) {
            materialDialog.dismiss();
            materialDialog = null;
        }
    }
}
