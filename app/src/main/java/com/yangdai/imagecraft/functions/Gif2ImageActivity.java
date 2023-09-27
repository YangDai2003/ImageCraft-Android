package com.yangdai.imagecraft.functions;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.yangdai.imagecraft.databinding.ActivityGif2ImageBinding;
import com.yangdai.imagecraft.utils.FileUtils;
import com.yangdai.imagecraft.imagedata.BitmapUtils;
import com.yangdai.imagecraft.imagedata.ImageTypeEnum;
import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.base.BaseImageProcessingActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import pl.droidsonroids.gif.GifDrawable;

public class Gif2ImageActivity extends BaseImageProcessingActivity {
    private ImageTypeEnum type = ImageTypeEnum.JPEG;
    private ActivityGif2ImageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGif2ImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initSpecial();

        Glide.with(this).load(viewModel.getUriList().get(0)).into(binding.gifView);
        viewModel.setRunnable(() -> {
            GifDrawable gifDrawable;
            try {
                gifDrawable = new GifDrawable(FileUtils.getRealPathFromUri(viewModel.getUriList().get(0), viewModel.getContext()));
            } catch (IOException e) {
                return;
            }
            viewModel.setTaskCount(gifDrawable.getNumberOfFrames());

            List<Callable<Void>> tasks = new ArrayList<>();

            for (int i = 0; i < viewModel.getTaskCount(); i++) {
                final int currentIndex = i;
                tasks.add(() -> {
                    if (Thread.interrupted() || viewModel.isCancelled()) {
                        return null; // 终止图片处理
                    }
                    // 保存bitmap为图片文件
                    Bitmap bitmap = gifDrawable.seekToFrameAndGet(currentIndex);
                    BitmapUtils.saveImage(bitmap, viewModel.getContext(), type);
                    bitmap.recycle();
                    viewModel.addTaskDone();
                    return null;
                });
            }

            try {
                // 并行执行任务
                viewModel.futures = viewModel.executor.invokeAll(tasks);
                // 处理任务结果
                for (Future<Void> future : viewModel.futures) {
                    future.get();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                viewModel.executor.shutdown();
                viewModel.setFinished(true);
            }
        });
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