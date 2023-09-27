package com.yangdai.imagecraft.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.utils.Utils;
import com.yangdai.imagecraft.otherActivities.AboutActivity;

public class MoreFragment extends Fragment {
    private SharedPreferences defaultSharedPrefs;

    public MoreFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        LinearLayout linearLayout0 = view.findViewById(R.id.more_content0);
        LinearLayout linearLayout1 = view.findViewById(R.id.more_content1);
        LinearLayout linearLayout2 = view.findViewById(R.id.more_content2);
        linearLayout0.setBackground(Utils.createRoundedDrawable(requireContext()));
        linearLayout1.setBackground(Utils.createRoundedDrawable(requireContext()));
        linearLayout2.setBackground(Utils.createRoundedDrawable(requireContext()));
        AppBarLayout appBarLayout = view.findViewById(R.id.appBar_layout);
        appBarLayout.setBackgroundColor(SurfaceColors.SURFACE_0.getColor(requireContext()));

        setupEmbeddingBehavior(view);
        setupThemeSelection(view);
        setupPhotoPickerSelection(view);
        setupLanguageSelection(view);
        setupRateApp(view);
        setupShareApp(view);
        setupSendFeedback(view);
        setupGithub(view);
        setupPrivacyPolicy(view);
        setupVersionInfo(view);
        setupOpenSourceLicenses(view);
        setupOtherApps(view);
    }

    private void setupGithub(View view) {
        view.findViewById(R.id.itemGithub).setOnClickListener(v -> {
            Uri uri = Uri.parse("https://github.com/YangDai2003/ImageCraft-Android");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
    }

    private void setupEmbeddingBehavior(View view) {
        view.findViewById(R.id.itemEmbedding).setOnClickListener(v -> {
            SharedPreferences.Editor editor = defaultSharedPrefs.edit();
            boolean beforeState = defaultSharedPrefs.getBoolean("embedding", false);
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.activity_embedding)
                    .setIcon(R.drawable.slit)
                    .setCancelable(false)
                    .setSingleChoiceItems(
                            new String[]{getString(R.string.off), getString(R.string.on)},
                            beforeState ? 1 : 0,
                            (dialog, which) -> {
                                editor.putBoolean("embedding", which != 0);
                                editor.apply();
                            })
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        int checkedItemPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        boolean state = checkedItemPosition != 0;
                        if (beforeState != state) {
                            Context context = requireContext().getApplicationContext();
                            PackageManager packageManager = context.getPackageManager();
                            Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
                            Intent mainIntent = null;
                            if (intent != null) {
                                mainIntent = Intent.makeRestartActivityTask(intent.getComponent());
                            }
                            context.startActivity(mainIntent);
                            Runtime.getRuntime().exit(0);
                        }
                    })
                    .show();
        });
    }

    private void setupThemeSelection(View view) {
        view.findViewById(R.id.itemTheme).setOnClickListener(v -> {
            SharedPreferences.Editor editor = defaultSharedPrefs.edit();
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.themeContent)
                    .setIcon(R.drawable.palette_icon)
                    .setCancelable(false)
                    .setSingleChoiceItems(
                            new String[]{getString(R.string.light), getString(R.string.dark), getString(R.string.systemTheme)},
                            defaultSharedPrefs.getInt("themeSetting", 2),
                            (dialog, which) -> {
                                editor.putInt("themeSetting", which);
                                editor.apply();
                            })
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        int theme = defaultSharedPrefs.getInt("themeSetting", 2);
                        switch (theme) {
                            case 0 ->
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            case 1 ->
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            case 2 ->
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            default -> {
                            }
                        }
                    })
                    .show();
        });
    }

    private void setupPhotoPickerSelection(View view) {
        view.findViewById(R.id.itemSelect).setOnClickListener(v -> {
            SharedPreferences.Editor editor = defaultSharedPrefs.edit();
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.photo_picker)
                    .setIcon(R.drawable.gallery_thumbnail)
                    .setCancelable(false)
                    .setSingleChoiceItems(
                            new String[]{getString(R.string.use_picker), getString(R.string.use_system_albums)},
                            defaultSharedPrefs.getInt("picker", 0),
                            (dialog, which) -> {
                                editor.putInt("picker", which);
                                editor.apply();
                            })
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        });
    }

    private void setupLanguageSelection(View view) {
        LinearLayout tvLanguage = view.findViewById(R.id.itemLanguage);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            tvLanguage.setVisibility(View.GONE);
            view.findViewById(R.id.language_div).setVisibility(View.GONE);
        }
        tvLanguage.setOnClickListener(v -> {
            try {
                @SuppressLint("InlinedApi") Intent intent = new Intent(Settings.ACTION_APP_LOCALE_SETTINGS);
                intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
                startActivity(intent);
            } catch (Exception e) {
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
                    startActivity(intent);
                } catch (Exception ignored) {
                }
            }
        });
    }

    private void setupRateApp(View view) {
        view.findViewById(R.id.itemRate).setOnClickListener(v -> {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.yangdai.imagecraft");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
    }

    private void setupShareApp(View view) {
        view.findViewById(R.id.itemShare).setOnClickListener(v -> {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareText));
            startActivity(Intent.createChooser(sendIntent, getString(R.string.app_name)));
        });
    }

    private void setupSendFeedback(View view) {
        view.findViewById(R.id.itemMail).setOnClickListener(v -> {
            Intent email = new Intent(Intent.ACTION_SEND);
            email.setType("message/rfc822");
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{"dy15800837435@gmail.com"});
            email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
            startActivity(Intent.createChooser(email, "feedback"));
        });
    }

    private void setupPrivacyPolicy(View view) {
        view.findViewById(R.id.itemPP).setOnClickListener(v -> {
            Uri uri = Uri.parse("https://note.youdao.com/s/Z2917Xfm");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
    }

    @SuppressLint("SetTextI18n")
    private void setupVersionInfo(View view) {
        LinearLayout linearLayout = view.findViewById(R.id.itemVersion);
        linearLayout.setOnClickListener(v -> startActivity(new Intent(requireContext(), AboutActivity.class)));
        linearLayout.setOnLongClickListener(v -> {
            Toast.makeText(requireContext(), getString(R.string.thank), Toast.LENGTH_LONG).show();
            return true;
        });

        TextView textVersion = view.findViewById(R.id.title6);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                textVersion.setText(getString(R.string.app_version) + " "
                        + requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), PackageManager.PackageInfoFlags.of(0)).versionName);
            } else {
                textVersion.setText(getString(R.string.app_version) + " "
                        + requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            textVersion.setText(getString(R.string.app_version) + " ");
        }
    }

    private void setupOpenSourceLicenses(View view) {
        view.findViewById(R.id.itemOSS).setOnClickListener(v -> {
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.app_osl));
            startActivity(new Intent(requireContext(), OssLicensesMenuActivity.class));
        });
    }

    private void setupOtherApps(View view) {
        view.findViewById(R.id.itemApps).setOnClickListener(v -> {
            Uri uri = Uri.parse("https://play.google.com/store/apps/dev?id=7281798021912275557");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
    }
}