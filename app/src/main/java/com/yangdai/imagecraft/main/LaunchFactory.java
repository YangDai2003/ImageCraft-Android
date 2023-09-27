package com.yangdai.imagecraft.main;

import static android.app.Activity.RESULT_OK;
import static com.yangdai.imagecraft.utils.Utils.isAppInstalled;
import static com.yangdai.imagecraft.utils.Utils.openApp;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResult;

import com.yangdai.imagecraft.functions_standard.CompressActivity;
import com.yangdai.imagecraft.functions_standard.ConvertActivity;
import com.yangdai.imagecraft.functions_special.ExifActivity;
import com.yangdai.imagecraft.functions.Gif2ImageActivity;
import com.yangdai.imagecraft.functions.Image2PDFActivity;
import com.yangdai.imagecraft.functions.Image2ZipActivity;
import com.yangdai.imagecraft.functions.PDF2ImageActivity;
import com.yangdai.imagecraft.functions_special.PickColorActivity;
import com.yangdai.imagecraft.functions_standard.ResizeActivity;
import com.yangdai.imagecraft.functions_standard.RotateActivity;
import com.yangdai.imagecraft.functions_special.ThemeActivity;
import com.yangdai.imagecraft.functions.Video2GifActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LaunchFactory {

    public static void handleResult(Context context, ActivityResult result, String tag) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            List<Uri> uris = null;
            if (data.getClipData() != null) {
                // 处理多个图片Uri
                ClipData clipData = data.getClipData();
                int count = clipData.getItemCount();
                uris = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    uris.add(uri);
                }
            } else if (data.getData() != null) {
                // 处理单个图片Uri
                Uri uri = data.getData();
                uris = Collections.singletonList(uri);
            }

            if (uris != null) {
                switch (tag) {
                    case "zip" -> handleZipMediaResult(context, uris);
                    case "pdf" -> handlePDFMediaResult(context, uris);
                    case "rotate" -> handleRotateMediaResult(context, uris);
                    case "convert" -> handleConvertMediaResult(context, uris);
                    case "compress" -> handleCompressMediaResult(context, uris);
                    case "resize" -> handleResizeMediaResult(context, uris);
                    default -> {
                    }
                }
            }
        }
    }

    public static void handleExifMediaResult(Context context, Uri uri) {
        if (uri == null) {
            return;
        }
        List<Uri> list = Collections.singletonList(uri);
        handleMediaResult(context, list, ExifActivity.class);
    }

    public static void handlePDFResult(Context context, Uri uri) {
        if (uri == null) {
            return;
        }
        List<Uri> list = Collections.singletonList(uri);
        handleMediaResult(context, list, PDF2ImageActivity.class);
    }

    public static void handleVideoMediaResult(Context context, Uri uri) {
        if (uri == null) {
            return;
        }
        List<Uri> list = Collections.singletonList(uri);
        handleMediaResult(context, list, Video2GifActivity.class);
    }

    public static void handleGifMediaResult(Context context, Uri uri) {
        if (uri == null) {
            return;
        }
        List<Uri> list = Collections.singletonList(uri);
        handleMediaResult(context, list, Gif2ImageActivity.class);
    }

    public static void handleThemeMediaResult(Context context, Uri uri) {
        if (uri == null) {
            return;
        }
        List<Uri> list = Collections.singletonList(uri);
        handleMediaResult(context, list, ThemeActivity.class);
    }

    public static void handleColorMediaResult(Context context, Uri uri) {
        if (uri == null) {
            return;
        }
        List<Uri> list = Collections.singletonList(uri);
        handleMediaResult(context, list, PickColorActivity.class);
    }

    public static void handleZipMediaResult(Context context, List<Uri> uris) {
        handleMediaResult(context, uris, Image2ZipActivity.class);
    }

    public static void handlePDFMediaResult(Context context, List<Uri> uris) {
        handleMediaResult(context, uris, Image2PDFActivity.class);
    }

    public static void handleResizeMediaResult(Context context, List<Uri> uris) {
        handleMediaResult(context, uris, ResizeActivity.class);
    }

    public static void handleCompressMediaResult(Context context, List<Uri> uris) {
        handleMediaResult(context, uris, CompressActivity.class);
    }

    public static void handleConvertMediaResult(Context context, List<Uri> uris) {
        handleMediaResult(context, uris, ConvertActivity.class);
    }

    public static void handleRotateMediaResult(Context context, List<Uri> uris) {
        handleMediaResult(context, uris, RotateActivity.class);
    }

    public static void handleMediaResult(Context context, List<Uri> uris, Class<?> activityClass) {
        if (!uris.isEmpty()) {
            ArrayList<String> uriList = uris.stream().map(Uri::toString).collect(Collectors.toCollection(ArrayList::new));
            Intent intent = new Intent(context, activityClass);
            intent.putStringArrayListExtra("uris", uriList);
            context.startActivity(intent);
            Log.d("PhotoPicker", "Number of items selected: " + uris.size());
        } else {
            Log.d("PhotoPicker", "No media selected");
        }
    }

    public static void openGooglePlay(Context context) {
        if (isAppInstalled(context, "com.yangdai.simpleocr")) {
            openApp(context.getApplicationContext(), "com.yangdai.simpleocr");
        } else {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.yangdai.simpleocr");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }
    }
}
