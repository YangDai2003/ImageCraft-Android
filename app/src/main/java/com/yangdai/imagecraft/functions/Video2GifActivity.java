package com.yangdai.imagecraft.functions;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.yangdai.gifencoderlib.BitmapRetriever;
import com.yangdai.gifencoderlib.GifEncoder;
import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.databinding.ActivityVideo2GifBinding;
import com.yangdai.imagecraft.utils.FileUtils;
import com.yangdai.imagecraft.utils.Utils;
import com.yangdai.imagecraft.base.BaseImageProcessingActivity;

import java.io.File;
import java.util.List;

public class Video2GifActivity extends BaseImageProcessingActivity {

    private Player player;
    private int fps = 10;
    private int percent = 50;
    private int width;
    private int height;
    private boolean noInterval = true;
    private ActivityVideo2GifBinding binding;
    private BitmapRetriever bitmapRetriever;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideo2GifBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initSpecial();

        bitmapRetriever = new BitmapRetriever(FileUtils.getRealPathFromUri(viewModel.getUriList().get(0), viewModel.getContext()));
        width = bitmapRetriever.getVideoWidth();
        height = bitmapRetriever.getVideoHeight();
        binding.sizeOutput.setText(width / 2 + " × " + height / 2);

        viewModel.setRunnable(() -> {
            String outputFileName = FileUtils.generateDateName() + ".gif";
            File outputFile = new File(FileUtils.getOutputDirectory(viewModel.getContext()), outputFileName);
            convertVideoToGif(outputFile.getAbsolutePath());
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initUi() {
        player = new ExoPlayer.Builder(this).build();
        binding.playerView.setPlayer(player);
        player.setMediaItem(MediaItem.fromUri(viewModel.getUriList().get(0)));
        player.prepare();


        binding.radioGroup.setOnCheckedChangeListener((radioGroup, id) -> noInterval = id == R.id.auto);
        binding.slider.addOnChangeListener((slider, value, fromUser) -> {
            fps = (int) value;
            binding.edFps.setText(String.valueOf(fps));
        });
        binding.edFps.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.edFps.setSelection(s.length());
                try {
                    int input = Integer.parseInt(s.toString());
                    if (input > 4 && input < 21) {
                        fps = input;
                        binding.slider.setValue(fps);
                    }
                } catch (Exception ignored) {

                }
            }
        });
        binding.edFps.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                Utils.closeKeyboard(Video2GifActivity.this);
                binding.edFps.clearFocus();
                return true;
            }
            return false;
        });


        binding.slider2.addOnChangeListener((slider, value, fromUser) -> {
            percent = (int) value;
            binding.edSize.setText(String.valueOf(percent));
            double newWidth = width * ((double) percent / 100); // 将percent转换为浮点数
            double newHeight = height * ((double) percent / 100);
            binding.sizeOutput.setText((int) newWidth + " × " + (int) newHeight); // 四舍五入到整数
        });
        binding.edSize.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.edSize.setSelection(s.length());
                try {
                    int input = Integer.parseInt(s.toString());
                    if (input > 9 && input < 101) {
                        percent = input;
                        binding.slider2.setValue(percent);
                    }
                } catch (Exception ignored) {

                }
            }
        });
        binding.edSize.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                Utils.closeKeyboard(Video2GifActivity.this);
                binding.edSize.clearFocus();
                return true;
            }
            return false;
        });
        binding.btFloating.setOnClickListener(v -> {
            player.pause();
            start();
        });
    }

    // 视频转换为GIF
    @SuppressLint("SetTextI18n")
    private void convertVideoToGif(String outputGifPath) {
        bitmapRetriever.setFps(fps);
        double newWidth = width * ((double) percent / 100); // 将percent转换为浮点数
        double newHeight = height * ((double) percent / 100);
        bitmapRetriever.setOutputBitmapSize((int) newWidth, (int) newHeight);
        // 截取视频的起始时间
        List<Bitmap> bitmaps = bitmapRetriever.generateBitmaps();
        viewModel.setTaskCount(bitmaps.size());

        GifEncoder encoder = new GifEncoder();
        if (!noInterval) {
            encoder.setFrameRate(fps);
        }
        encoder.init(bitmaps.get(0));
        viewModel.addTaskDone();
        encoder.start(outputGifPath);

        for (int i = 1; i < viewModel.getTaskCount(); i++) {
            if (Thread.interrupted() || viewModel.isCancelled()) {
                break; // 终止图片处理
            }
            encoder.addFrame(bitmaps.get(i));
            viewModel.addTaskDone();
        }

        try {
            encoder.finish();
            bitmapRetriever.close();
            for (Bitmap bitmap : bitmaps) {
                bitmap.recycle(); // 手动回收每个Bitmap资源
            }
            bitmaps.clear(); // 清空列表
        } catch (Exception e) {
            e.printStackTrace();
        }
        viewModel.setFinished(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.stop();
        player.release();
    }
}