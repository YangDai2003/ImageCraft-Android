package com.yangdai.imagecraft.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.yangdai.imagecraft.R;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileUtils {
    public static String generateDateName() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
        return currentDateTime.format(formatter);
    }

    /**
     * 获取真实路径
     */
    public static String getRealPathFromUri(Uri uri, Context context) {
        String filePath = "";
        String scheme = uri.getScheme();
        if (scheme == null)
            filePath = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            filePath = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
            if (TextUtils.isEmpty(filePath)) {
                filePath = getFilePathForNonMediaUri(context, uri);
            }
        }
        return filePath;
    }

    //非媒体文件中查找
    private static String getFilePathForNonMediaUri(Context context, Uri uri) {
        String filePath = "";
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow("_data");
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return filePath;
    }

    /**
     * @noinspection ResultOfMethodCallIgnored
     */
    public static String getOutputDirectory(Context context) {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), context.getString(R.string.app_name));
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory.getAbsolutePath();
    }
}
