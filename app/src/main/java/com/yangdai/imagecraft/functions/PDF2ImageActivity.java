package com.yangdai.imagecraft.functions;

import androidx.appcompat.app.AlertDialog;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yangdai.imagecraft.databinding.ActivityPdfactivityBinding;
import com.yangdai.imagecraft.imagedata.BitmapUtils;
import com.yangdai.imagecraft.imagedata.ImageTypeEnum;
import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.base.BaseImageProcessingActivity;

import java.io.IOException;

public class PDF2ImageActivity extends BaseImageProcessingActivity {
    private AlertDialog alertDialog;
    private ActivityPdfactivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfactivityBinding.inflate(getLayoutInflater());
        showProgressDialog();
        setContentView(binding.getRoot());

        initSpecial();

        binding.pdfView.fromUri(viewModel.getUriList().get(0))
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .onLoad(nbPages -> alertDialog.dismiss())
                .enableAnnotationRendering(false)
                .load();

        viewModel.setRunnable(() -> {
            try {
                // 获取 ContentResolver
                ContentResolver contentResolver = getContentResolver();
                // 使用 ContentResolver 打开文件描述符
                ParcelFileDescriptor fileDescriptor = contentResolver.openFileDescriptor(viewModel.getUriList().get(0), "r");
                if (fileDescriptor == null) {
                    return;
                }
                // 加载 PDF 文件
                PdfRenderer renderer = new PdfRenderer(fileDescriptor);

                // 遍历每一页并转换为图片
                viewModel.setTaskCount(renderer.getPageCount());

                Bitmap bitmap;
                for (int i = 0; i < viewModel.getTaskCount(); i++) {
                    if (Thread.interrupted() || viewModel.isCancelled()) {
                        break; // 终止图片处理
                    }
                    PdfRenderer.Page page = renderer.openPage(i);
                    int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
                    int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    // 以下三行处理图片存储到本地出现黑屏的问题，这个涉及到背景问题
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);
                    canvas.drawBitmap(bitmap, 0, 0, null);
                    Rect r = new Rect(0, 0, width, height);
                    page.render(bitmap, r, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    // 保存图片到文件
                    BitmapUtils.saveImage(bitmap, viewModel.getContext(), ImageTypeEnum.PNG);
                    // 关闭当前页
                    page.close();

                    viewModel.addTaskDone();
                }

                // 关闭 PDF 渲染器
                renderer.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                viewModel.setFinished(true);
            }
        });
    }
    @Override
    protected void initUi() {
        binding.btFloating.setOnClickListener(v -> start());
    }

    private void showProgressDialog() {
        alertDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.processing)
                .setView(R.layout.custom_progress_dialog_circle)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> PDF2ImageActivity.this.finish())
                .create();
        alertDialog.show();
    }

}