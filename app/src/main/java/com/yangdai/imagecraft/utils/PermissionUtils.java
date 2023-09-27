package com.yangdai.imagecraft.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    private static final int PERMISSION_REQUEST_CODE = 1024;

    /**
     * 检查并申请权限
     */
    public static List<String> checkPermissions(Activity activity) {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (Build.VERSION.SDK_INT >= 34) {
                permissions = new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                };
            } else {
                permissions = new String[]{
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_IMAGES
                };
            }
        } else {
            permissions = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        }

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        return permissionsToRequest;
    }

    public static void checkAndRequestPermissions(Activity activity) {
        List<String> permissionsToRequest = checkPermissions(activity);
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }
}
