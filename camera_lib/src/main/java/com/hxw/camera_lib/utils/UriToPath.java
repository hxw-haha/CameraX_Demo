package com.hxw.camera_lib.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

/**
 * <p>文件描述：</p>
 * <p>作者：hanxw</p>
 * <p>创建时间：2022/2/7</p>
 * <p>更改时间：2022/2/7</p>
 * <p>版本号：1</p>
 */
public class UriToPath {
    public static String getAbsolutePathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(contentUri,
                    new String[]{MediaStore.Images.Media.DATA},
                    null, null, null);
            if (cursor == null) {
                return null;
            }
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(columnIndex);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static Long getFileSizeFromUri(Context context, Uri contentUri) {
        Cursor cursor = context.getContentResolver().query(contentUri,
                null, null, null, null);
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst();
        long fileSize = cursor.getLong(sizeIndex);
        cursor.close();
        return fileSize;
    }
}
