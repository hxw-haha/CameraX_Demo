package com.hxw.camera_lib.permission;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>文件描述：权限申请管理类</p>
 * <p>作者：hanxw</p>
 * <p>创建时间：2021/8/31</p>
 * <p>更改时间：2021/8/31</p>
 * <p>版本号：1</p>
 */
public final class PermissionManager implements IRequestPermissionListener {

    public static Builder factoryBuilder(@NonNull final FragmentActivity activity) {
        return new Builder(activity);
    }

    public static Builder factoryBuilder(@NonNull final Fragment fragment) {
        return new Builder(fragment);
    }

    private final Builder mBuilder;

    private PermissionManager(@NonNull Builder builder) {
        mBuilder = builder;
        if (builder.permissionsSet.size() == 0) {
            onSucceed();
            return;
        }
        final String[] permissions = mBuilder.permissionsSet.toArray(new String[0]);
        mBuilder.mPermissionsFragment.get().requestPermissions(permissions, this);
    }

    @Override
    public void onSucceed() {
        if (mBuilder.listener != null) {
            mBuilder.listener.onSucceed();
        }
    }

    @Override
    public void onRefused(List<String> refusedPermission, List<String> foreverRefusedPermission) {
        if (mBuilder.listener != null) {
            mBuilder.listener.onRefused(refusedPermission, foreverRefusedPermission);
        }
    }

    public static final class Builder {
        private final Context context;
        private final Set<String> permissionsSet = new HashSet<>();
        private IRequestPermissionListener listener;
        @VisibleForTesting
        private final Lazy<PermissionsFragment> mPermissionsFragment;

        public Builder(@NonNull final FragmentActivity activity) {
            context = activity;
            mPermissionsFragment = new LazyPermissionsFragment(activity.getSupportFragmentManager());
        }

        public Builder(@NonNull final Fragment fragment) {
            context = fragment.getContext();
            mPermissionsFragment = new LazyPermissionsFragment(fragment.getChildFragmentManager());
        }

        public Builder addPermission(@NonNull String... permissions) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != 0) {
                    permissionsSet.add(permission);
                }
            }
            return this;
        }

        public Builder addPermission(@NonNull List<String> permissions) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != 0) {
                    permissionsSet.add(permission);
                }
            }
            return this;
        }

        public Builder setListener(@NonNull IRequestPermissionListener listener) {
            this.listener = listener;
            return this;
        }

        public PermissionManager build() {
            return new PermissionManager(this);
        }
    }
}
