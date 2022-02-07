package com.hxw.camera_lib.permission;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

/**
 * <p>文件描述：</p>
 * <p>作者：hanxw</p>
 * <p>创建时间：2021/8/31</p>
 * <p>更改时间：2021/8/31</p>
 * <p>版本号：1</p>
 */
@FunctionalInterface
public interface Lazy<V> {
    V get();
}

class LazyPermissionsFragment implements Lazy<PermissionsFragment> {
    private static final String TAG = LazyPermissionsFragment.class.getSimpleName();
    @NonNull
    private final FragmentManager fragmentManager;
    private PermissionsFragment permissionsFragment;

    LazyPermissionsFragment(@NonNull FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @Override
    public PermissionsFragment get() {
        if (permissionsFragment == null) {
            permissionsFragment = getPermissionsFragment(fragmentManager);
        }
        return permissionsFragment;
    }

    private PermissionsFragment getPermissionsFragment(@NonNull final FragmentManager fragmentManager) {
        PermissionsFragment permissionsFragment = findPermissionsFragment(fragmentManager);
        boolean isNewInstance = permissionsFragment == null;
        if (isNewInstance) {
            permissionsFragment = new PermissionsFragment();
            fragmentManager
                    .beginTransaction()
                    .add(permissionsFragment, TAG)
                    .commitNow();
        }
        return permissionsFragment;
    }

    private PermissionsFragment findPermissionsFragment(@NonNull final FragmentManager fragmentManager) {
        return (PermissionsFragment) fragmentManager.findFragmentByTag(TAG);
    }
}