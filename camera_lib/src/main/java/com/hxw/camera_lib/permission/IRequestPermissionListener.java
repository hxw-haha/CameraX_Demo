package com.hxw.camera_lib.permission;

import java.util.List;

/**
 * <p>文件描述：权限申请回调接口</p>
 * <p>作者：hanxw</p>
 * <p>创建时间：2021/8/31</p>
 * <p>更改时间：2021/8/31</p>
 * <p>版本号：1</p>
 */
public interface IRequestPermissionListener {
    /**
     * 权限申请成功
     */
    void onSucceed();

    /**
     * 权限申请拒绝
     *
     * @param refusedPermission        拒绝的权限
     * @param foreverRefusedPermission 永远拒绝的权限
     */
    void onRefused(List<String> refusedPermission, List<String> foreverRefusedPermission);
}
