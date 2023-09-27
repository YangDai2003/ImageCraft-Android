package com.yangdai.imagecraft.imagedata;

import android.graphics.Bitmap;

public enum ImageTypeEnum {
    JPEG("image/jpeg", Bitmap.CompressFormat.JPEG),
    PNG("image/png", Bitmap.CompressFormat.PNG),
    WEBP("image/webp", Bitmap.CompressFormat.WEBP);

    private final String mimeType;
    private final Bitmap.CompressFormat compressFormat;

    ImageTypeEnum(String mimeType, Bitmap.CompressFormat compressFormat) {
        this.mimeType = mimeType;
        this.compressFormat = compressFormat;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Bitmap.CompressFormat getCompressFormat() {
        return compressFormat;
    }
}
