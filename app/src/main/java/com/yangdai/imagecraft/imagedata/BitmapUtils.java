package com.yangdai.imagecraft.imagedata;

import static com.yangdai.imagecraft.utils.FileUtils.generateDateName;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.exifinterface.media.ExifInterface;

import com.yangdai.imagecraft.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitmapUtils {

    public static final String[] keysArray = {
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_LENS_MAKE,
            ExifInterface.TAG_LENS_MODEL,
            ExifInterface.TAG_SOFTWARE,
            ExifInterface.TAG_EXIF_VERSION,

            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_SUBSEC_TIME,
            ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
            ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
            ExifInterface.TAG_DATETIME_ORIGINAL,
            ExifInterface.TAG_DATETIME_DIGITIZED,

            ExifInterface.TAG_APERTURE_VALUE,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_ISO_SPEED,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM,
            ExifInterface.TAG_F_NUMBER,

            ExifInterface.TAG_EXPOSURE_TIME,
            ExifInterface.TAG_EXPOSURE_INDEX,
            ExifInterface.TAG_EXPOSURE_MODE,
            ExifInterface.TAG_EXPOSURE_BIAS_VALUE,
            ExifInterface.TAG_RECOMMENDED_EXPOSURE_INDEX,

            ExifInterface.TAG_DIGITAL_ZOOM_RATIO,
            ExifInterface.TAG_SCENE_CAPTURE_TYPE,
            ExifInterface.TAG_SPECTRAL_SENSITIVITY,
            ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
            ExifInterface.TAG_STANDARD_OUTPUT_SENSITIVITY,

            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_PROCESSING_METHOD,

            ExifInterface.TAG_IMAGE_DESCRIPTION,
            ExifInterface.TAG_ARTIST,
            ExifInterface.TAG_COPYRIGHT,
            ExifInterface.TAG_COLOR_SPACE,

            ExifInterface.TAG_GAMMA,
            ExifInterface.TAG_WHITE_BALANCE,
            ExifInterface.TAG_WHITE_POINT,
            ExifInterface.TAG_RESOLUTION_UNIT,
            ExifInterface.TAG_BRIGHTNESS_VALUE,
            ExifInterface.TAG_CONTRAST,
            ExifInterface.TAG_SATURATION,
            ExifInterface.TAG_SHARPNESS
    };


    /**
     * 获取图片文件格式
     */
    public static String getMimeType(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        return options.outMimeType;
    }

    /**
     * 根据图片原格式返回类型，默认jpeg
     */
    public static ImageTypeEnum getImageType(String imagePath) {
        String type = getMimeType(imagePath);
        if ("image/png".equals(type)) {
            return ImageTypeEnum.PNG;
        } else if ("image/webp".equals(type)) {
            return ImageTypeEnum.WEBP;
        } else {
            return ImageTypeEnum.JPEG;
        }
    }

    /**
     * 根据百分比缩放图片
     */
    public static Bitmap resizeBitmapByPercentage(Bitmap bitmap, int percentage) {
        return Bitmap.createScaledBitmap(bitmap,
                bitmap.getWidth() - bitmap.getWidth() * percentage / 100,
                bitmap.getHeight() - bitmap.getHeight() * percentage / 100,
                true);
    }

    /**
     * 根据宽高缩放图片
     */
    public static Bitmap resizeBitmapByPixel(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    /**
     * 使用矩阵，根据exif信息旋转
     *
     * @noinspection unused
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        try {
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90);
                case ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180);
                case ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270);
                default -> {
                    return bitmap;
                }
            }
            return Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 使用矩阵，自定义图片旋转
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, float degrees, boolean flipV, boolean flipH) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        if (flipV) {
            matrix.postScale(1, -1);
        }
        if (flipH) {
            matrix.postScale(-1, 1);
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 压缩方法
     */
    public static Bitmap compressBitmap(Bitmap bitmap, ImageTypeEnum type) {
        Bitmap.CompressFormat compressFormat;
        if (type == ImageTypeEnum.JPEG || type == ImageTypeEnum.PNG) {
            compressFormat = Bitmap.CompressFormat.JPEG;
        } else {
            compressFormat = Bitmap.CompressFormat.WEBP;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(compressFormat, 50, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        return BitmapFactory.decodeStream(inputStream);
    }

    /**
     * 压缩图片到指定大小
     */
    public static Bitmap compressBitmap(Bitmap bitmap, ImageTypeEnum type, int maxSizeInKb) {
        Bitmap.CompressFormat compressFormat;
        if (type == ImageTypeEnum.JPEG || type == ImageTypeEnum.PNG) {
            compressFormat = Bitmap.CompressFormat.JPEG;
        } else {
            compressFormat = Bitmap.CompressFormat.WEBP;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            bitmap.compress(compressFormat, 100, outputStream);
            byte[] compressedData = outputStream.toByteArray();
            int quality = 100;

            while (compressedData.length / 1024 > maxSizeInKb) {
                quality -= 10;
                if (quality <= 0) {
                    break;
                }

                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * 2 / 3, bitmap.getHeight() * 2 / 3, true);
                outputStream.reset();
                bitmap.compress(compressFormat, quality, outputStream);
                compressedData = outputStream.toByteArray();
            }

            return BitmapFactory.decodeByteArray(compressedData, 0, compressedData.length);
        } catch (Exception e) {
            e.printStackTrace();
            // 处理异常或记录错误
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                // 处理异常或记录错误
            }
        }

        return null; // 如果压缩失败，则返回 null
    }

    /**
     * API29 中的最新保存图片到相册的方法
     */
    public static String saveImage(Bitmap bitmap, Context context, ImageTypeEnum type) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + context.getString(R.string.app_name));
        values.put(MediaStore.Images.Media.IS_PENDING, true);
        values.put(MediaStore.Images.Media.MIME_TYPE, type.getMimeType());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, generateDateName()); // 设置图片名称
        Bitmap.CompressFormat compressFormat = type.getCompressFormat();

        Uri uri = context.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try {
                OutputStream outputStream = context.getApplicationContext().getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(compressFormat, 100, outputStream);
                    outputStream.close();
                    values.put(MediaStore.Images.Media.IS_PENDING, false);
                    context.getApplicationContext().getContentResolver().update(uri, values, null, null);
                    return uri.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}