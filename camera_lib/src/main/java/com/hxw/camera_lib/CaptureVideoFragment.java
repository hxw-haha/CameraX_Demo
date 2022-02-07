package com.hxw.camera_lib;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.RecordingStats;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.hxw.camera_lib.databinding.FragmentCaptureVideoBinding;
import com.hxw.camera_lib.utils.CameraConfig;
import com.hxw.camera_lib.utils.UriToPath;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * <p>文件描述：</p>
 * <p>作者：hanxw</p>
 * <p>创建时间：2022/1/30</p>
 * <p>更改时间：2022/1/30</p>
 * <p>版本号：1</p>
 */
public class CaptureVideoFragment extends Fragment {
    private static final String TAG = "----CaptureVideo---";
    private FragmentCaptureVideoBinding captureVideoBinding = null;
    private List<CameraSelector> cameraSelectors;
    private int cameraIndex = 0;
    private VideoCapture<Recorder> videoCapture;
    private VideoRecordEvent recordingState;
    private Recording currentRecording;
    private final MutableLiveData<String> captureLiveStatus = new MutableLiveData<>();

    private final Consumer<VideoRecordEvent> captureListener = new Consumer<VideoRecordEvent>() {
        @Override
        public void accept(VideoRecordEvent event) {
            if (!(event instanceof VideoRecordEvent.Status)) {
                recordingState = event;
            }
            updateUI(event);

            if (event instanceof VideoRecordEvent.Finalize) {
                // display the captured video
                Log.e(TAG, "outputUri::" + ((VideoRecordEvent.Finalize) event).getOutputResults().getOutputUri());
            }
        }
    };

    private void updateUI(VideoRecordEvent event) {
        final String state = (event instanceof VideoRecordEvent.Status)
                ? getVideoEventName(recordingState) : getVideoEventName(event);
        RecordingStats stats = event.getRecordingStats();
        long time = TimeUnit.NANOSECONDS.toSeconds(stats.getRecordedDurationNanos());
        String text = state + ": recorded" + stats.getNumBytesRecorded() / 1000
                + "KB, in " + time + "second";
        if (event instanceof VideoRecordEvent.Finalize) {
            Uri videoUri = ((VideoRecordEvent.Finalize) event).getOutputResults().getOutputUri();
            text = text + "\nFile saved to:" + UriToPath.getAbsolutePathFromUri(requireContext(), videoUri);
        }
        captureLiveStatus.postValue(text);
        if (captureVideoBinding != null) {
            captureVideoBinding.captureButton.updateProgress(time);
            if (time == captureVideoBinding.captureButton.getMaxDuration()) {
                currentRecording.stop();
            }
        }
    }

    private String getVideoEventName(VideoRecordEvent event) {
        if (event instanceof VideoRecordEvent.Status) {
            return "Status";
        }
        if (event instanceof VideoRecordEvent.Start) {
            return "Started";
        }
        if (event instanceof VideoRecordEvent.Finalize) {
            return "Finalized";
        }
        if (event instanceof VideoRecordEvent.Pause) {
            return "Paused";
        }
        if (event instanceof VideoRecordEvent.Resume) {
            return "Resumed";
        }
        return "";
    }

    private void startRecording() {
        final String name = "CameraX-recording-" +
                new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.CHINA)
                        .format(System.currentTimeMillis()) + ".mp4";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, name);
        MediaStoreOutputOptions mediaStoreOutput = new MediaStoreOutputOptions.Builder(
                requireActivity().getContentResolver(),
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build();
        PendingRecording pendingRecording = videoCapture.getOutput()
                .prepareRecording(requireActivity(), mediaStoreOutput);
        currentRecording = pendingRecording.start(
                ContextCompat.getMainExecutor(requireContext()), captureListener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        captureVideoBinding = FragmentCaptureVideoBinding.inflate(inflater, container, false);
        return captureVideoBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraSelectors = CameraConfig.getCameraSelectors(requireContext());
        initCameraFragment();
    }

    private void initCameraFragment() {
        initializeUI();
        captureVideoBinding.previewView.postDelayed(new Runnable() {
            @Override
            public void run() {
                bindCaptureUsecase();
            }
        }, 100);
    }

    private void initializeUI() {
        captureVideoBinding.cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraIndex = (cameraIndex + 1) % cameraSelectors.size();
                bindCaptureUsecase();
            }
        });
        captureVideoBinding.captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recordingState == null || recordingState instanceof VideoRecordEvent.Finalize) {
                    startRecording();
                    captureVideoBinding.stopButton.setVisibility(View.VISIBLE);
                } else {
                    if (currentRecording == null) {
                        return;
                    }
                    if (recordingState instanceof VideoRecordEvent.Start) {
                        currentRecording.pause();
                        captureVideoBinding.stopButton.setVisibility(View.VISIBLE);
                    } else if (recordingState instanceof VideoRecordEvent.Pause) {
                        currentRecording.resume();
                    } else if (recordingState instanceof VideoRecordEvent.Resume) {
                        currentRecording.pause();
                    }
                }
            }
        });
        captureVideoBinding.stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureVideoBinding.stopButton.setVisibility(View.INVISIBLE);
                if (currentRecording == null || recordingState instanceof VideoRecordEvent.Finalize) {
                    return;
                }
                Recording recording = currentRecording;
                recording.stop();
                currentRecording = null;
            }
        });

        captureLiveStatus.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                captureVideoBinding.captureStatus.setText(s);
            }
        });
        captureLiveStatus.postValue(getString(R.string.Idle));
    }

    private void bindCaptureUsecase() {
        try {
            CameraSelector cameraSelector = getCameraSelector(cameraIndex);
            videoCapture = CameraConfig.bindVideoCaptureUseCase(
                    requireContext(),
                    cameraSelector, captureVideoBinding.previewView,
                    this);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void onPause() {
        super.onPause();
        if (currentRecording != null
                && (recordingState instanceof VideoRecordEvent.Start
                || recordingState instanceof VideoRecordEvent.Resume)) {
            currentRecording.pause();
        }
    }

    @Override
    public void onDestroyView() {
        captureVideoBinding = null;
        super.onDestroyView();
    }

}
