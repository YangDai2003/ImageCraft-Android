package com.yangdai.imagecraft.functions;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import com.yangdai.colorpickerlib.ColorListener;
import com.yangdai.colorpickerlib.ColorPickerDialog;
import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.databinding.ActivityImage2PdfactivityBinding;
import com.yangdai.imagecraft.utils.FileUtils;
import com.yangdai.imagecraft.imagedata.BitmapDecoder;
import com.yangdai.imagecraft.base.BaseImageProcessingActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class Image2PDFActivity extends BaseImageProcessingActivity {
    private boolean saveAsMultipleFiles = true;
    private boolean isVertical = true;
    private int color = Color.WHITE;
    private int pageType = 0;
    private ActivityImage2PdfactivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImage2PdfactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        calculateTotalMemorySize(binding.infoContent);

        viewModel.setRunnable(() -> {
            String outputDirectory = FileUtils.getOutputDirectory(viewModel.getContext());

            if (saveAsMultipleFiles) {

                List<Callable<Void>> tasks = new ArrayList<>();

                for (int i = 0; i < viewModel.getTaskCount(); i++) {
                    if (viewModel.isCancelled()) {
                        break; // 终止图片处理
                    }
                    final int currentIndex = i;
                    tasks.add(() -> {
                        if (Thread.interrupted() || viewModel.isCancelled()) {
                            return null; // 终止图片处理
                        }
                        Uri uri = viewModel.getUriList().get(currentIndex);
                        BitmapDecoder bitmapDecoder = new BitmapDecoder(viewModel.getContext(), uri);
                        Bitmap bitmap = bitmapDecoder.getBitmap();
                        if (bitmap != null) {
                            PdfDocument pdfDocument = new PdfDocument();
                            PdfDocument.PageInfo pageInfo = getPageInfoByType(pageType, bitmap, isVertical);
                            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                            Canvas canvas = page.getCanvas();
                            canvas.drawColor(color);
                            float scaleFactor = Math.min(
                                    (float) pageInfo.getPageWidth() / bitmap.getWidth(),
                                    (float) pageInfo.getPageHeight() / bitmap.getHeight()
                            );
                            int scaledWidth = Math.round(bitmap.getWidth() * scaleFactor);
                            int scaledHeight = Math.round(bitmap.getHeight() * scaleFactor);
                            float left = (pageInfo.getPageWidth() - scaledWidth) / 2f;
                            float top = (pageInfo.getPageHeight() - scaledHeight) / 2f;
                            RectF destinationRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);
                            canvas.drawBitmap(bitmap, null, destinationRect, null);
                            pdfDocument.finishPage(page);

                            String outputFileName = FileUtils.generateDateName() + ".pdf";
                            File outputFile = new File(outputDirectory, outputFileName);
                            try {
                                pdfDocument.writeTo(new FileOutputStream(outputFile));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            pdfDocument.close();
                            bitmap.recycle();
                            viewModel.addTaskDone();
                        }
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
            } else {
                String outputFileName = FileUtils.generateDateName() + ".pdf";
                PdfDocument pdfDocument = new PdfDocument();

                for (int i = 0; i < viewModel.getTaskCount(); i++) {
                    if (Thread.interrupted() || viewModel.isCancelled()) {
                        break; // 终止图片处理
                    }
                    Uri uri = viewModel.getUriList().get(i);
                    BitmapDecoder bitmapDecoder = new BitmapDecoder(viewModel.getContext(), uri);
                    Bitmap bitmap = bitmapDecoder.getBitmap();
                    if (bitmap != null) {
                        PdfDocument.PageInfo pageInfo = getPageInfoByType(pageType, bitmap, isVertical);
                        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                        Canvas canvas = page.getCanvas();
                        canvas.drawColor(color);
                        float scaleFactor = Math.min(
                                (float) pageInfo.getPageWidth() / bitmap.getWidth(),
                                (float) pageInfo.getPageHeight() / bitmap.getHeight()
                        );
                        int scaledWidth = Math.round(bitmap.getWidth() * scaleFactor);
                        int scaledHeight = Math.round(bitmap.getHeight() * scaleFactor);
                        float left = (pageInfo.getPageWidth() - scaledWidth) / 2f;
                        float top = (pageInfo.getPageHeight() - scaledHeight) / 2f;
                        RectF destinationRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);
                        canvas.drawBitmap(bitmap, null, destinationRect, null);
                        pdfDocument.finishPage(page);
                        bitmap.recycle();
                        viewModel.addTaskDone();
                    }
                }

                File outputFile = new File(outputDirectory, outputFileName);
                try {
                    pdfDocument.writeTo(new FileOutputStream(outputFile));
                    pdfDocument.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 通知系统刷新文件夹
                MediaScannerConnection.scanFile(
                        viewModel.getContext(),
                        new String[]{outputFile.getAbsolutePath()},
                        null,
                        (path, uri) -> {
                            // 扫描完成后的回调方法
                            // 可以在这里执行一些操作，例如显示一个Toast消息
                            // 文件现在应该在文件管理器中可见
                        }
                );
                viewModel.setFinished(true);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initUi() {
        binding.radioGroup.setOnCheckedChangeListener((radioGroup, id)
                -> saveAsMultipleFiles = id == R.id.auto);
        binding.toggleGroupOrientation.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isVertical = checkedId == R.id.buttonV;
            }
        });
        binding.toggleGroupSize.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.buttonImage) {
                    pageType = 0;
                } else if (checkedId == R.id.buttonA1) {
                    pageType = 1;
                } else if (checkedId == R.id.buttonA3) {
                    pageType = 2;
                } else if (checkedId == R.id.buttonA4) {
                    pageType = 3;
                }
            }
        });
        binding.btChooseColor.setOnClickListener(v -> new ColorPickerDialog.Builder(this, R.style.CustomDialog)
                .setTitle(R.string.choose_background)
                .setOnColorSelectedListener((colorInfo, fromUser) -> {
                    binding.tvColor.setText("#" + colorInfo.getHexCode());
                    setTextViewBackgroundColor(colorInfo.getColor());
                })
                .setPositiveButton(android.R.string.ok, (ColorListener) (colorInfo, fromUser) -> {
                    color = colorInfo.getColor();
                    setTextViewBackgroundColor(color);
                    binding.tvColor.setText("#" + colorInfo.getHexCode());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show());
        binding.btFloating.setOnClickListener(v -> start());
    }

    private void setTextViewBackgroundColor(int color) {
        @SuppressLint("UseCompatLoadingForDrawables")
        GradientDrawable backgroundDrawable = (GradientDrawable) getDrawable(R.drawable.rounded_corner_background);
        if (backgroundDrawable != null) {
            backgroundDrawable.setColor(color);
        }
        binding.tvColor.setBackground(backgroundDrawable);
    }

    private PdfDocument.PageInfo getPageInfoByType(int pageType, Bitmap bitmap, boolean isVertical) {
        if (isVertical) {
            return switch (pageType) {
                case 1 -> new PdfDocument.PageInfo.Builder(2480, 3508, 1).create(); // A1 size
                case 2 -> new PdfDocument.PageInfo.Builder(1748, 2480, 1).create(); // A3 size
                case 3 -> new PdfDocument.PageInfo.Builder(1240, 1754, 1).create(); // A4 size
                default ->
                        new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create(); // Default size (same as image size)
            };
        } else {
            return switch (pageType) {
                case 1 ->
                        new PdfDocument.PageInfo.Builder(3508, 2480, 1).create(); // A1 size (rotated)
                case 2 ->
                        new PdfDocument.PageInfo.Builder(2480, 1748, 1).create(); // A3 size (rotated)
                case 3 ->
                        new PdfDocument.PageInfo.Builder(1754, 1240, 1).create(); // A4 size (rotated)
                default ->
                        new PdfDocument.PageInfo.Builder(bitmap.getHeight(), bitmap.getWidth(), 1).create(); // Default size (rotated)
            };
        }
    }
}