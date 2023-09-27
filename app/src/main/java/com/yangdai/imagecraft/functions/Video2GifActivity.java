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
    private boolean noInterval = true;
    private ActivityVideo2GifBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideo2GifBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initSpecial();

        viewModel.setRunnable(() -> {
            String outputFileName = FileUtils.generateDateName() + ".gif";
            File outputFile = new File(FileUtils.getOutputDirectory(viewModel.getContext()), outputFileName);
            convertVideoToGif(FileUtils.getRealPathFromUri(viewModel.getUriList().get(0), viewModel.getContext()), outputFile.getAbsolutePath());
        });
    }

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
        binding.btFloating.setOnClickListener(v -> {
            player.pause();
            start();
        });
    }

    // 视频转换为GIF
    @SuppressLint("SetTextI18n")
    private void convertVideoToGif(String inputVideoPath, String outputGifPath) {
        BitmapRetriever extractor = new BitmapRetriever();
        extractor.setFPS(fps);
        // 截取视频的起始时间
        List<Bitmap> bitmaps = extractor.generateBitmaps(inputVideoPath);
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

        encoder.finish();
        viewModel.setFinished(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.stop();
        player.release();
    }
}