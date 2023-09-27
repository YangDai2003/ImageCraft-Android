package com.yangdai.imagecraft.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

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
        Cursor cursor = context.getContentResolver().query(uri, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex("_data");
                    return cursor.getString(idx);
                }
            } finally {
                cursor.close();
            }
        }
        return uri.getPath();
    }

    /** @noinspection ResultOfMethodCallIgnored*/
    public static String getOutputDirectory(Context context) {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), context.getString(R.string.app_name));
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory.getAbsolutePath();
    }
}
