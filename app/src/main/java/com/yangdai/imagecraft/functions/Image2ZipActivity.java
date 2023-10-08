package com.yangdai.imagecraft.functions;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.databinding.ActivityZipBinding;
import com.yangdai.imagecraft.utils.FileUtils;
import com.yangdai.imagecraft.utils.Utils;
import com.yangdai.imagecraft.base.BaseImageProcessingActivity;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class Image2ZipActivity extends BaseImageProcessingActivity {
    private boolean isNotCrypt = true;
    private String fileName = "";
    private String password = "";
    private ActivityZipBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityZipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        calculateTotalMemorySize(binding.infoContent);
        viewModel.setRunnable(() -> {
            String outputDirectory = FileUtils.getOutputDirectory(viewModel.getContext());
            String zipFileName = fileName.isEmpty() ? FileUtils.generateDateName() : fileName;
            File zipFile = new File(outputDirectory, zipFileName + ".zip");

            if (isNotCrypt || password.isEmpty()) {
                try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {
                    byte[] buffer = new byte[1024];

                    for (int i = 0; i < viewModel.getTaskCount(); i++) {
                        if (Thread.interrupted() || viewModel.isCancelled()) {
                            break; // 终止图片处理
                        }

                        Uri imageUri = viewModel.getUriList().get(i);
                        String imagePath = FileUtils.getRealPathFromUri(imageUri, viewModel.getContext());
                        File imageFile = new File(imagePath);

                        if (imageFile.exists()) {
                            FileInputStream fileInputStream = new FileInputStream(imageFile);
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                            // 获取图片文件的实际文件名和文件格式
                            String fileName = imageFile.getName();
                            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

                            // 创建一个新的ZipEntry
                            ZipEntry zipEntry = new ZipEntry("image_" + (i + 1) + "." + fileExtension);
                            zipOutputStream.putNextEntry(zipEntry);

                            // Write the file data to the zip output stream
                            int bytesRead;
                            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                                zipOutputStream.write(buffer, 0, bytesRead);
                            }

                            // Close the current entry
                            zipOutputStream.closeEntry();

                            // Close the input streams
                            bufferedInputStream.close();
                            fileInputStream.close();
                            viewModel.addTaskDone();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // 通知系统刷新文件夹
                    MediaScannerConnection.scanFile(
                            viewModel.getContext(),
                            new String[]{zipFile.getAbsolutePath()},
                            null,
                            (path, uri) -> {
                                // 扫描完成后的回调方法
                                // 可以在这里执行一些操作，例如显示一个Toast消息
                                // 文件现在应该在文件管理器中可见
                            }
                    );
                    viewModel.setFinished(true);
                }
            } else {
                ZipParameters zipParameters = new ZipParameters();
                zipParameters.setEncryptFiles(true);
                zipParameters.setEncryptionMethod(EncryptionMethod.AES);
                try (ZipFile zipFile1 = new ZipFile(zipFile, password.toCharArray())) {
                    for (int i = 0; i < viewModel.getTaskCount(); i++) {
                        if (Thread.interrupted() || viewModel.isCancelled()) {
                            break; // 终止图片处理
                        }

                        Uri imageUri = viewModel.getUriList().get(i);
                        String imagePath = FileUtils.getRealPathFromUri(imageUri, viewModel.getContext());
                        File imageFile = new File(imagePath);
                        try {
                            zipFile1.addFile(imageFile, zipParameters);
                            viewModel.addTaskDone();
                        } catch (ZipException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    viewModel.setFinished(true);
                }
            }
        });
    }

    @Override
    protected void initUi() {
        binding.radioGroup.setOnCheckedChangeListener((radioGroup, id)
                -> {
            isNotCrypt = id == R.id.auto;
            if (isNotCrypt) {
                binding.lrForPassword.setVisibility(View.INVISIBLE);
            } else {
                binding.lrForPassword.setVisibility(View.VISIBLE);
            }
        });
        binding.edPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    password = editable.toString();
                } catch (Exception e) {
                    password = "";
                }

            }
        });
        binding.edName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    fileName = editable.toString();
                } catch (Exception e) {
                    fileName = "";
                }

            }
        });
        binding.edPassword.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                Utils.closeKeyboard(Image2ZipActivity.this);
                binding.edPassword.clearFocus();
                return true;
            }
            return false;
        });
        binding.edName.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                Utils.closeKeyboard(Image2ZipActivity.this);
                binding.edName.clearFocus();
                return true;
            }
            return false;
        });
        binding.btFloating.setOnClickListener(v -> start());
    }
}