package com.yangdai.imagecraft.imagedata;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.exifinterface.media.ExifInterface;

import com.yangdai.imagecraft.utils.FileUtils;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BitmapDecoder {
    private Bitmap bitmap = null;
    private final int orientation;
    private final ImageType imageType;
    private final Map<String, String> exifInfo;

    public BitmapDecoder(Context context, Uri uri) {
        try {
            bitmap = getBitmapFromUri(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        orientation = decodeBitmapOrientation(context, uri);
        imageType = BitmapUtils.getImageType(FileUtils.getRealPathFromUri(uri, context));
        exifInfo = decodeExifInfo(context, uri);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getOrientation() {
        return orientation;
    }

    public int getWidth() {
        return bitmap.getWidth();
    }

    public int getHeight() {
        return bitmap.getHeight();
    }

    public ImageType getImageType() {
        return imageType;
    }

    public String getFormat() {
        return bitmap.getConfig().toString();
    }

    public Map<String, String> getExifInfo() {
        return exifInfo;
    }

    private Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = Objects.requireNonNull(parcelFileDescriptor).getFileDescriptor();
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return bitmap;
    }

    private Map<String, String> decodeExifInfo(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            ExifInterface exifInterface;
            if (inputStream != null) {
                exifInterface = new ExifInterface(inputStream);
                inputStream.close();

                Map<String, String> exifInfo = new HashMap<>();
                for (String str : BitmapUtils.keysArray) {
                    exifInfo.put(str, exifInterface.getAttribute(str));
                }
                return exifInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    private int decodeBitmapOrientation(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                ExifInterface exifInterface = new ExifInterface(inputStream);
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                inputStream.close();
                return orientation;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}