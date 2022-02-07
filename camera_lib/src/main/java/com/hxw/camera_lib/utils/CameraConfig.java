package com.hxw.camera_lib.utils;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.VideoCapture;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>文件描述：</p>
 * <p>作者：hanxw</p>
 * <p>创建时间：2022/1/30</p>
 * <p>更改时间：2022/1/30</p>
 * <p>版本号：1</p>
 */
public class CameraConfig {
    public static final double RATIO_4_3_VALUE = 4.0 / 3.0;
    public static final double RATIO_16_9_VALUE = 16.0 / 9.0;

    public static int aspectRatio(int widthPixels, int heightPixels) {
        double previewRatio = (double) Math.max(widthPixels, heightPixels) / (double) Math.min(widthPixels, heightPixels);
        if (Math.abs(previewRatio - RATIO_4_3_VALUE) <= Math.abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    public static List<CameraSelector> getCameraSelectors(@NonNull Context context) {
        final List<CameraSelector> cameraSelectors = new ArrayList<>();
        try {
            ProcessCameraProvider provider = ProcessCameraProvider.getInstance(context).get();
            provider.unbindAll();
            for (CameraSelector cameraSelector : getAllCameraSelector()) {
                if (provider.hasCamera(cameraSelector)) {
                    cameraSelectors.add(cameraSelector);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cameraSelectors;
    }

    public static List<CameraSelector> getAllCameraSelector() {
        return Arrays.asList(CameraSelector.DEFAULT_BACK_CAMERA, CameraSelector.DEFAULT_FRONT_CAMERA);
    }

    public static VideoCapture<Recorder> bindVideoCaptureUseCase(@NonNull Context context,
                                                                 @NonNull CameraSelector cameraSelector,
                                                                 @NonNull PreviewView previewView,
                                                                 @NonNull LifecycleOwner lifecycleOwner) {
        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            previewView.getDisplay().getRealMetrics(displayMetrics);
            //获取宽高比
            int screenAspectRatio = aspectRatio(displayMetrics.widthPixels, displayMetrics.heightPixels);
            int rotation = previewView.getDisplay().getRotation();

            ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(context).get();
            //设置视频清晰度
            QualitySelector qualitySelector = QualitySelector.from(Quality.HD);

            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)
                    previewView.getLayoutParams();
            CameraConfig.SmartSize previewSize = CameraConfig.getDisplaySmartSize(
                    previewView.getDisplay());
            layoutParams.dimensionRatio = "V," + previewSize.size.getWidth()
                    + ":" + previewSize.size.getHeight();

            Preview preview = new Preview.Builder()
                    //设置宽高比
                    .setTargetAspectRatio(screenAspectRatio)
                    //设置当前屏幕的旋转
                    .setTargetRotation(rotation)
                    .build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            Recorder recorder = new Recorder.Builder()
                    .setQualitySelector(qualitySelector)
                    .build();
            VideoCapture<Recorder> videoCapture = VideoCapture.withOutput(recorder);
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    videoCapture,
                    preview);
            return videoCapture;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ImageCapture bindImageCaptureUseCase(@NonNull Context context,
                                                       @NonNull CameraSelector cameraSelector,
                                                       @NonNull PreviewView previewView,
                                                       @NonNull LifecycleOwner lifecycleOwner) {
        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            previewView.getDisplay().getRealMetrics(displayMetrics);
            //获取宽高比
            int screenAspectRatio = aspectRatio(displayMetrics.widthPixels, displayMetrics.heightPixels);
            int rotation = previewView.getDisplay().getRotation();

            ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(context).get();

            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)
                    previewView.getLayoutParams();
            CameraConfig.SmartSize previewSize = CameraConfig.getDisplaySmartSize(
                    previewView.getDisplay());
            layoutParams.dimensionRatio = "V," + previewSize.size.getWidth()
                    + ":" + previewSize.size.getHeight();

            Preview preview = new Preview.Builder()
                    //设置宽高比
                    .setTargetAspectRatio(screenAspectRatio)
                    //设置当前屏幕的旋转
                    .setTargetRotation(rotation)
                    .build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            ImageCapture imageCapture = new ImageCapture.Builder()
                    //优化捕获速度，可能降低图片质量
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    //设置输出JPEG图像压缩质量
                    .setJpegQuality(60)
                    //设置宽高比
                    .setTargetAspectRatio(screenAspectRatio)
                    //设置初始的旋转角度
                    .setTargetRotation(rotation)
                    .build();
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageCapture,
                    preview);
            return imageCapture;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SmartSize getDisplaySmartSize(Display display) {
        Point outPoint = new Point();
        display.getRealSize(outPoint);
        return new SmartSize(outPoint.x, outPoint.y);
    }

    public static class SmartSize {
        public final Size size;
        public final int maxSize;
        public final int minSize;

        public SmartSize(int width, int height) {
            size = new Size(width, height);
            maxSize = Math.max(size.getWidth(), size.getHeight());
            minSize = Math.min(size.getWidth(), size.getHeight());
        }
    }
}
