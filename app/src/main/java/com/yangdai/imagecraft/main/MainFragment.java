package com.yangdai.imagecraft.main;

import static android.app.Activity.RESULT_OK;

import static com.yangdai.imagecraft.main.LaunchFactory.handleResult;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;
import com.yangdai.imagecraft.utils.FileUtils;
import com.yangdai.imagecraft.utils.PermissionUtils;
import com.yangdai.imagecraft.imagedata.BitmapUtils;
import com.yangdai.imagecraft.imagedata.ImageTypeEnum;
import com.yangdai.imagecraft.R;
import com.yangdai.imagecraft.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainFragment extends Fragment {

    private static final int MAX_SELECTED_MEDIA = 20;
    private SharedPreferences sharedPreferences;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaForCrop;
    private ActivityResultLauncher<PickVisualMediaRequest> pickGifMediaForImage;
    private ActivityResultLauncher<PickVisualMediaRequest> pickVideoMediaForGif;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaForTheme;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaForColor;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMediaForResize;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMediaForCompress;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMediaForConvert;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMediaForRotate;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMediaForPDF;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMediaForZip;
    private ActivityResultLauncher<String> pickPdfMedia;

    private ActivityResultLauncher<Intent> pickImageLauncherForExif;
    private ActivityResultLauncher<Intent> pickImageLauncherForCrop;
    private ActivityResultLauncher<Intent> pickImageLauncherForTheme;
    private ActivityResultLauncher<Intent> pickImageLauncherForColor;
    private ActivityResultLauncher<String> pickGifLauncher;
    private ActivityResultLauncher<Intent> pickVideoLauncher;
    private ActivityResultLauncher<Intent> pickMultipleImagesLauncherForResize;
    private ActivityResultLauncher<Intent> pickMultipleImagesLauncherForCompress;
    private ActivityResultLauncher<Intent> pickMultipleImagesLauncherForConvert;
    private ActivityResultLauncher<Intent> pickMultipleImagesLauncherForRotate;
    private ActivityResultLauncher<Intent> pickMultipleImagesLauncherForPDF;
    private ActivityResultLauncher<Intent> pickMultipleImagesLauncherForZip;

    private int color;

    public MainFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivityResultLaunchers();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    private void setupActivityResultLaunchers() {
        pickPdfMedia = registerForActivityResult(new ActivityResultContracts.GetContent(), o -> LaunchFactory.handlePDFResult(requireContext(), o));
        pickMediaForCrop = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::handleCropMediaResult);
        pickMediaForTheme = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), o -> LaunchFactory.handleThemeMediaResult(requireContext(), o));
        pickMediaForColor = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), o -> LaunchFactory.handleColorMediaResult(requireContext(), o));
        pickGifMediaForImage = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), o -> LaunchFactory.handleGifMediaResult(requireContext(), o));
        pickVideoMediaForGif = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), o -> LaunchFactory.handleVideoMediaResult(requireContext(), o));
        pickMultipleMediaForZip = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(MAX_SELECTED_MEDIA), o -> LaunchFactory.handleZipMediaResult(requireContext(), o));
        pickMultipleMediaForPDF = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(MAX_SELECTED_MEDIA), o -> LaunchFactory.handlePDFMediaResult(requireContext(), o));
        pickMultipleMediaForResize = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(MAX_SELECTED_MEDIA), o -> LaunchFactory.handleResizeMediaResult(requireContext(), o));
        pickMultipleMediaForCompress = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(MAX_SELECTED_MEDIA), o -> LaunchFactory.handleCompressMediaResult(requireContext(), o));
        pickMultipleMediaForConvert = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(MAX_SELECTED_MEDIA), o -> LaunchFactory.handleConvertMediaResult(requireContext(), o));
        pickMultipleMediaForRotate = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(MAX_SELECTED_MEDIA), o -> LaunchFactory.handleRotateMediaResult(requireContext(), o));

        pickImageLauncherForCrop = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                handleCropMediaResult(result.getData().getData());
            }
        });
        pickImageLauncherForTheme = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                LaunchFactory.handleThemeMediaResult(requireContext(), result.getData().getData());
            }
        });
        pickImageLauncherForColor = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                LaunchFactory.handleColorMediaResult(requireContext(), result.getData().getData());
            }
        });
        pickGifLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                LaunchFactory.handleGifMediaResult(requireContext(), result);
            }
        });
        pickVideoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                LaunchFactory.handleVideoMediaResult(requireContext(), result.getData().getData());
            }
        });
        pickImageLauncherForExif = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                LaunchFactory.handleExifMediaResult(requireContext(), result.getData().getData());
            }
        });

        pickMultipleImagesLauncherForResize = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> handleResult(requireContext(), o, "resize"));
        pickMultipleImagesLauncherForCompress = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> handleResult(requireContext(), o, "compress"));
        pickMultipleImagesLauncherForConvert = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> handleResult(requireContext(), o, "convert"));
        pickMultipleImagesLauncherForRotate = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> handleResult(requireContext(), o, "rotate"));
        pickMultipleImagesLauncherForPDF = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> handleResult(requireContext(), o, "pdf"));
        pickMultipleImagesLauncherForZip = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> handleResult(requireContext(), o, "zip"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUi(view);
        initEvents(view);
        TextView textView = view.findViewById(R.id.title0);
        color = textView.getCurrentTextColor();
    }

    private void initUi(View view) {
        LinearLayout linearLayout0 = view.findViewById(R.id.main_content0);
        LinearLayout linearLayout1 = view.findViewById(R.id.main_content1);
        LinearLayout linearLayout2 = view.findViewById(R.id.main_content2);
        linearLayout0.setBackground(Utils.createRoundedDrawable(requireContext()));
        linearLayout1.setBackground(Utils.createRoundedDrawable(requireContext()));
        linearLayout2.setBackground(Utils.createRoundedDrawable(requireContext()));
        AppBarLayout appBarLayout = view.findViewById(R.id.appBar_layout);
        appBarLayout.setBackgroundColor(SurfaceColors.SURFACE_0.getColor(requireContext()));
    }

    private void initEvents(View view) {
        view.findViewById(R.id.hint).setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.using_help)
                .setIcon(R.drawable.baseline_help_outline_24)
                .setMessage(R.string.help_content)
                .setPositiveButton(android.R.string.ok, null)
                .show());
        view.findViewById(R.id.itemCompress).setOnClickListener(this::pickImages);
        view.findViewById(R.id.itemResize).setOnClickListener(this::pickImages);
        view.findViewById(R.id.itemCrop).setOnClickListener(this::pickImage);
        view.findViewById(R.id.itemConvert).setOnClickListener(this::pickImages);
        view.findViewById(R.id.itemRotate).setOnClickListener(this::pickImages);
        view.findViewById(R.id.itemMd3).setOnClickListener(this::pickImage);
        view.findViewById(R.id.itemColorPicker).setOnClickListener(this::pickImage);
        view.findViewById(R.id.itemPdf2Image).setOnClickListener(this::openSystemFolderForPDF);
        view.findViewById(R.id.itemImage2Pdf).setOnClickListener(this::pickImages);
        view.findViewById(R.id.itemImage2Zip).setOnClickListener(this::pickImages);
        view.findViewById(R.id.itemGif2Image).setOnClickListener(this::pickImage);
        view.findViewById(R.id.itemVideo2Gif).setOnClickListener(this::pickImage);
        view.findViewById(R.id.itemExif).setOnClickListener(this::pickImage);
        boolean isVersionGreaterThanAndroid12 = Build.VERSION.SDK_INT > Build.VERSION_CODES.S;
        if (!isVersionGreaterThanAndroid12) {
            view.findViewById(R.id.itemMd3).setVisibility(View.GONE);
            view.findViewById(R.id.md_div).setVisibility(View.GONE);
        }
        view.findViewById(R.id.itemOcr).setOnClickListener(v -> LaunchFactory.openGooglePlay(requireContext()));
    }

    private void createPermissionDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.permissions)
                .setMessage(R.string.permission_hint)
                .setNegativeButton(R.string.exit, (dialog, which) -> {
                    requireActivity().finish();
                    System.exit(0);
                })
                .setPositiveButton(R.string.open_settings, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setCancelable(false)
                .show();
    }

    // 在需要打开系统文件夹并选择 PDF 文件的地方调用此方法
    private void openSystemFolderForPDF(View ignoredView) {
        pickPdfMedia.launch("application/pdf");
    }

    public void pickImage(View view) {
        if (!PermissionUtils.checkPermissions(requireActivity()).isEmpty()) {
            createPermissionDialog();
        } else {
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            intent.setAction(Intent.ACTION_PICK);
            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //直接打开系统相册  不设置会有选择相册一步（例：系统相册、QQ浏览器相册）

            if (sharedPreferences.getInt("picker", 0) == 0) {
                if (view.getId() == R.id.itemCrop) {
                    pickMediaForCrop.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                } else if (view.getId() == R.id.itemColorPicker) {
                    pickMediaForColor.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                } else if (view.getId() == R.id.itemGif2Image) {
                    String mimeType = "image/gif";
                    pickGifMediaForImage.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(new ActivityResultContracts.PickVisualMedia.SingleMimeType(mimeType))
                            .build());
                } else if (view.getId() == R.id.itemVideo2Gif) {
                    pickVideoMediaForGif.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                            .build());
                } else if (view.getId() == R.id.itemMd3) {
                    pickMediaForTheme.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                }
            } else {
                if (view.getId() == R.id.itemCrop) {
                    pickImageLauncherForCrop.launch(intent);
                } else if (view.getId() == R.id.itemColorPicker) {
                    pickImageLauncherForColor.launch(intent);
                } else if (view.getId() == R.id.itemGif2Image) {
                    pickGifLauncher.launch("image/gif");
                } else if (view.getId() == R.id.itemVideo2Gif) {
                    Intent intentV = new Intent();
                    intentV.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                    intentV.setAction(Intent.ACTION_PICK);
                    intentV.setData(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    pickVideoLauncher.launch(intentV);
                } else if (view.getId() == R.id.itemMd3) {
                    pickImageLauncherForTheme.launch(intent);
                }
            }
            if (view.getId() == R.id.itemExif) {
                pickImageLauncherForExif.launch(intent);
            }
        }
    }

    public void pickImages(View view) {
        if (!PermissionUtils.checkPermissions(requireActivity()).isEmpty()) {
            createPermissionDialog();
        } else {
            if (sharedPreferences.getInt("picker", 0) == 0) {
                PickVisualMediaRequest.Builder requestBuilder = new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE);

                if (view.getId() == R.id.itemResize) {
                    pickMultipleMediaForResize.launch(requestBuilder.build());
                } else if (view.getId() == R.id.itemCompress) {
                    pickMultipleMediaForCompress.launch(requestBuilder.build());
                } else if (view.getId() == R.id.itemConvert) {
                    pickMultipleMediaForConvert.launch(requestBuilder.build());
                } else if (view.getId() == R.id.itemRotate) {
                    pickMultipleMediaForRotate.launch(requestBuilder.build());
                } else if (view.getId() == R.id.itemImage2Pdf) {
                    pickMultipleMediaForPDF.launch(requestBuilder.build());
                } else if (view.getId() == R.id.itemImage2Zip) {
                    pickMultipleMediaForZip.launch(requestBuilder.build());
                }
            } else {
                Intent intent = new Intent();
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //直接打开系统相册  不设置会有选择相册一步（例：系统相册、QQ浏览器相册）

                if (view.getId() == R.id.itemResize) {
                    pickMultipleImagesLauncherForResize.launch(intent);
                } else if (view.getId() == R.id.itemCompress) {
                    pickMultipleImagesLauncherForCompress.launch(intent);
                } else if (view.getId() == R.id.itemConvert) {
                    pickMultipleImagesLauncherForConvert.launch(intent);
                } else if (view.getId() == R.id.itemRotate) {
                    pickMultipleImagesLauncherForRotate.launch(intent);
                } else if (view.getId() == R.id.itemImage2Pdf) {
                    pickMultipleImagesLauncherForPDF.launch(intent);
                } else if (view.getId() == R.id.itemImage2Zip) {
                    pickMultipleImagesLauncherForZip.launch(intent);
                }
            }

        }
    }

    private void handleCropMediaResult(Uri uri) {
        if (uri != null) {
            String filePath = FileUtils.getRealPathFromUri(uri, requireContext());
            String imageType = BitmapUtils.getMimeType(filePath);
            if (!"image/png".equals(imageType) && !"image/jpeg".equals(imageType) && !"image/webp".equals(imageType)) {
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                if (bitmap != null) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    byte[] jpegData = outputStream.toByteArray();
                    File convertedFile = new File(requireContext().getCacheDir(), "temp.jpeg");
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(convertedFile);
                        fileOutputStream.write(jpegData);
                        fileOutputStream.close();
                        cropImage(Uri.fromFile(convertedFile), ImageTypeEnum.JPEG);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("PhotoPicker", "Failed to decode bitmap");
                }
            } else {
                cropImage(uri, BitmapUtils.getImageType(filePath));
            }
        } else {
            Log.d("PhotoPicker", "No media selected");
        }
    }

    private void cropImage(Uri uri, ImageTypeEnum type) {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(SurfaceColors.SURFACE_0.getColor(requireContext()));
        options.setToolbarWidgetColor(color);
        options.setStatusBarColor(SurfaceColors.SURFACE_0.getColor(requireContext()));
        options.setDimmedLayerColor(Color.TRANSPARENT);
        options.setRootViewBackgroundColor(SurfaceColors.SURFACE_0.getColor(requireContext()));
        options.setCropFrameColor(SurfaceColors.SURFACE_5.getColor(requireContext()));
        options.setCropGridColor(SurfaceColors.SURFACE_5.getColor(requireContext()));
        options.setActiveControlsWidgetColor(Color.parseColor("#021A5F"));
        options.setAllowedGestures(UCropActivity.ALL, UCropActivity.ALL, UCropActivity.ALL);
        options.setHideBottomControls(false);
        Bitmap.CompressFormat compressFormat = type.getCompressFormat();
        options.setCompressionFormat(compressFormat);
        options.setFreeStyleCropEnabled(true);
        options.setShowCropGrid(true);

        Uri targetUri = Uri.fromFile(new File(requireActivity().getApplicationContext().getCacheDir(), "temp.jpeg"));
        UCrop.of(uri, targetUri)
                .withOptions(options)
                .start(requireActivity(), UCrop.REQUEST_CROP);
    }
}