package com.hxw.camera_lib.permission;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>文件描述：权限申请fragment</p>
 * <p>作者：hanxw</p>
 * <p>创建时间：2021/8/31</p>
 * <p>更改时间：2021/8/31</p>
 * <p>版本号：1</p>
 */
public class PermissionsFragment extends Fragment {
    private static final int PERMISSIONS_REQUEST_CODE = 42;
    private IRequestPermissionListener mPermissionListener;

    public PermissionsFragment() {
    }

    @TargetApi(Build.VERSION_CODES.M)
    void requestPermissions(@NonNull String[] permissions, @NonNull IRequestPermissionListener permissionListener) {
        this.mPermissionListener = permissionListener;
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSIONS_REQUEST_CODE) return;
        if (grantResults.length > 0) {
            //全部拒绝的权限
            final List<String> refusedPermission = new ArrayList<>();
            //永远拒绝的权限
            final List<String> foreverRefusedPermission = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                final int grantResult = grantResults[i];
                final String permission = permissions[i];
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    refusedPermission.add(permission);
                    if (!shouldShowRequestPermissionRationale(permission)) {
                        foreverRefusedPermission.add(permission);
                    }
                }
            }
            if (refusedPermission.isEmpty()) {
                mPermissionListener.onSucceed();
            } else {
                mPermissionListener.onRefused(refusedPermission, foreverRefusedPermission);
            }
        }
    }
}
