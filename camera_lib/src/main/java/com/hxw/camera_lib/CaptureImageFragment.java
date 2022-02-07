package com.hxw.camera_lib;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.hxw.camera_lib.databinding.FragmentCaptureImageBinding;
import com.hxw.camera_lib.utils.CameraConfig;
import com.hxw.camera_lib.utils.FileToGallery;
import com.hxw.camera_lib.utils.UriToPath;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * <p><p>文件描述：通过camerax获取图片</p></p>
 * <p>作者：hanxw</p>
 * <p>创建时间：2022/2/7</p>
 * <p>更改时间：2022/2/7</p>
 * <p>版本号：1</p>
 */
public class CaptureImageFragment extends Fragment {
    private static final String TAG = "----CaptureImage---";
    private FragmentCaptureImageBinding captureImageBinding = null;
    private List<CameraSelector> cameraSelectors;
    private int cameraIndex = 0;
    private ImageCapture imageCapture;
    private final MutableLiveData<String> captureLiveStatus = new MutableLiveData<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        captureImageBinding = FragmentCaptureImageBinding.inflate(inflater, container, false);
        return captureImageBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraSelectors = CameraConfig.getCameraSelectors(requireContext());
        initializeUI();
    }

    private void initializeUI() {
        captureImageBinding.previewView.postDelayed(new Runnable() {
            @Override
            public void run() {
                bindCaptureUsecase();
            }
        }, 100);
        captureImageBinding.cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraIndex = (cameraIndex + 1) % cameraSelectors.size();
                bindCaptureUsecase();
            }
        });
        captureImageBinding.captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        captureLiveStatus.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                captureImageBinding.captureStatus.setText(s);
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void takePicture() {
        final String name = "CameraX-recording-" +
                new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.CHINA)
                        .format(System.currentTimeMillis()) + ".jpg";

        final File imageFile = new File(requireContext().getExternalFilesDir(
                Environment.DIRECTORY_PICTURES).getAbsolutePath(),
                name);
        ImageCapture.Metadata metadata = new ImageCapture.Metadata();
        Integer lensFacing = getCameraSelector(cameraIndex).getLensFacing();
        if (lensFacing != null) {
            metadata.setReversedHorizontal(lensFacing == CameraSelector.LENS_FACING_FRONT);
        }
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture
                .OutputFileOptions.Builder(imageFile)
                .setMetadata(metadata)
                .build();

        imageCapture.takePicture(outputFileOptions, Executors.newSingleThreadExecutor(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Uri fileUri = FileToGallery.saveFile(requireContext(), imageFile.getAbsolutePath(), name);
                imageFile.delete();
                captureLiveStatus.postValue(UriToPath.getAbsolutePathFromUri(requireContext(), fileUri));
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {

            }
        });
    }


    private void bindCaptureUsecase() {
        CameraSelector cameraSelector = getCameraSelector(cameraIndex);
        imageCapture = CameraConfig.bindImageCaptureUseCase(
                requireContext(),
                cameraSelector,
                captureImageBinding.previewView,
                this);
    }

    /**
     * Retrieve the asked camera's type(lens facing type). In this sample, only 2 types:
     * idx is even number:  CameraSelector.LENS_FACING_BACK
     * odd number:   CameraSelector.LENS_FACING_FRONT
     */
    private CameraSelector getCameraSelector(int cameraIndex) {
        if (cameraSelectors.size() == 0) {
            Log.i(TAG, "Error: This device does not have any camera, bailing out");
            requireActivity().finish();
        }
        return (cameraSelectors.get(cameraIndex % cameraSelectors.size()));
    }

    @Override
    public void onDestroy() {
        captureImageBinding = null;
        super.onDestroy();
    }
}
