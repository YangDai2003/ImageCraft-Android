package com.yangdai.imagecraft.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.elevation.SurfaceColors;

import java.util.List;

public class Utils {

    /**
     * 创建背景图片
     */
    public static Drawable createRoundedDrawable(Context context) {
        float cornerRadiusPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16,
                context.getResources().getDisplayMetrics()
        );
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(SurfaceColors.SURFACE_1.getColor(context));
        drawable.setCornerRadius(cornerRadiusPx);
        return drawable;
    }

    /**
     * 查看图片
     */
    public static void viewAlbumInGallery(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }  // 处理没有找到图库应用程序的情况，例如显示错误消息或提供备选操作
    }

    /**
     * 收起键盘
     */
    public static void closeKeyboard(Activity activity) {
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    /**
     * 打开ocr
     */
    public static void openApp(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageName);
        List<ResolveInfo> apps = packageManager.queryIntentActivities(resolveIntent, 0);
        if (apps.size() == 0) {
            return;
        }
        ResolveInfo resolveInfo = apps.iterator().next();
        if (resolveInfo != null) {
            String className = resolveInfo.activityInfo.name;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            context.startActivity(intent);
        }
    }

    /**
     * 检查包是否存在
     */
    public static boolean isAppInstalled(Context ctx, String PackageName) {
        try {
            PackageManager packageManager = ctx.getPackageManager();
            PackageInfo info = packageManager.getPackageInfo(PackageName, 0);
            if (info != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void startZoomAnimation(View view) {
        ScaleAnimation zoomInAnimation = new ScaleAnimation(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        zoomInAnimation.setDuration(1000);
        zoomInAnimation.setFillAfter(true);
        zoomInAnimation.setRepeatMode(Animation.REVERSE);
        zoomInAnimation.setRepeatCount(Animation.INFINITE);
        view.startAnimation(zoomInAnimation);
    }
}
