package com.yangdai.imagecraft.otherActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.elevation.SurfaceColors;
import com.yangdai.imagecraft.R;

import java.util.Objects;

public class AboutActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable((SurfaceColors.SURFACE_0.getColor(this))));
        getSupportActionBar().setElevation(0f);
        TextView textVersion = findViewById(R.id.version_info);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                textVersion.setText(getString(R.string.app_version) + " "
                        + getPackageManager().getPackageInfo(getPackageName(), PackageManager.PackageInfoFlags.of(0)).versionName);
            } else {
                textVersion.setText(getString(R.string.app_version) + " "
                        + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            textVersion.setText(getString(R.string.app_version) + " ");
        }
        TextView textView = findViewById(R.id.textView3);
        String websiteLink = getString(R.string.yang_s_codehub);
        SpannableString spannableString = new SpannableString(websiteLink);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // 在这里处理点击超链接的逻辑，比如打开网页
                Uri uri = Uri.parse("https://yangdai2003.github.io/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        };
        // 将ClickableSpan应用到SpannableString中
        spannableString.setSpan(clickableSpan, 0, websiteLink.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 设置TextView的点击事件为可点击
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spannableString);
    }
}