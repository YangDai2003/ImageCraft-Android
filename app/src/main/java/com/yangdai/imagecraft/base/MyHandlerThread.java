package com.yangdai.imagecraft.base;

import android.os.Handler;
import android.os.HandlerThread;

public class MyHandlerThread extends HandlerThread {
    private static final String TAG = "ImageProcessingThread" + System.currentTimeMillis();
    private Handler handler;

    public MyHandlerThread() {
        super(TAG, HandlerThread.MAX_PRIORITY);
        start();
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        handler = new Handler(getLooper());
    }

    public Handler getHandler() {
        return handler;
    }

}
