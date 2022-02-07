package com.hxw.camera_lib;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hxw.camera_lib.permission.IRequestPermissionListener;
import com.hxw.camera_lib.permission.PermissionManager;

import java.util.List;


/**
 * <p>文件描述：</p>
 * <p>作者：hanxw</p>
 * <p>创建时间：2022/1/30</p>
 * <p>更改时间：2022/1/30</p>
 * <p>版本号：1</p>
 */
public class CaptureActivity extends AppCompatActivity {
    private static final String IS_VIDEO_FLAG = "is_video_flag";

    public static void start(Context context, boolean isVideo) {
        Intent starter = new Intent(context, CaptureActivity.class);
        starter.putExtra(IS_VIDEO_FLAG, isVideo);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        final boolean isVideo = getIntent().getBooleanExtra(IS_VIDEO_FLAG, false);
        PermissionManager.factoryBuilder(this)
                .addPermission(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setListener(new IRequestPermissionListener() {
                    @Override
                    public void onSucceed() {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.capture_content_view,
                                        isVideo ? new CaptureVideoFragment()
                                                : new CaptureImageFragment())
                                .commit();
                    }

                    @Override
                    public void onRefused(List<String> refusedPermission, List<String> foreverRefusedPermission) {
                        Log.e("CaptureActivity", "权限拒绝了");
                    }
                })
                .build();

    }
}
