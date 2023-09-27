package com.yangdai.imagecraft.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.yalantis.ucrop.UCrop;
import com.yangdai.imagecraft.utils.FileUtils;
import com.yangdai.imagecraft.utils.PermissionUtils;
import com.yangdai.imagecraft.imagedata.BitmapUtils;
import com.yangdai.imagecraft.imagedata.ImageTypeEnum;
import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.utils.Utils;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_0.getColor(this));
        getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);
        NavController navController = Objects.requireNonNull(navHostFragment).getNavController();
        NavigationBarView navigationBarView = findViewById(R.id.navigation_bar);
        NavigationUI.setupWithNavController(navigationBarView, navController);

        PermissionUtils.checkAndRequestPermissions(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    // 权限已被授予
                    Log.d("Permissions", permissions[i] + " granted.");
                } else {
                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setTitle(R.string.permissions)
                            .setMessage(R.string.permission_hint)
                            .setNegativeButton(R.string.exit, (dialog, which) -> {
                                MainActivity.this.finish();
                                System.exit(0);
                            })
                            .setPositiveButton(R.string.open_settings, (dialog, which) -> {
                                if (!PermissionUtils.checkPermissions(MainActivity.this).isEmpty()) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            })
                            .setCancelable(false)
                            .show();
                    // 权限被拒绝
                    Log.d("Permissions", permissions[i] + " denied.");
                }
            }
        }
    }

    /**
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                String path = FileUtils.getRealPathFromUri(resultUri, this);
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                ImageTypeEnum type = BitmapUtils.getImageType(path);
                String savedPath = BitmapUtils.saveImage(bitmap, this, type);
                if (!savedPath.isEmpty()) {
                    Snackbar.make(findViewById(R.id.fragment_container_view), getString(R.string.saved), Snackbar.LENGTH_SHORT)
                            .setAction(getString(R.string.check), view -> Utils.viewAlbumInGallery(this, Uri.parse(savedPath)))
                            .show();
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                Log.e("ucrop", cropError.toString());
            }
        }
    }
}