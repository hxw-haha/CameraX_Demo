package com.hxw.camera_lib.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>文件描述：保存图片、视频文件至相册</p>
 * <p>作者：hanxw</p>
 * <p>创建时间：2022/2/7</p>
 * <p>更改时间：2022/2/7</p>
 * <p>版本号：1</p>
 */
public class FileToGallery {
    public static Uri saveFile(Context context, String filePath, String name) {
        OutputStream outputStream = null;
        FileInputStream fileInputStream = null;
        Uri fileUri = null;
        try {
            File originalFile = new File(filePath);

            String extension = originalFile.getAbsolutePath().substring(originalFile.getAbsolutePath().lastIndexOf("."));
            fileUri = generateUri(context, extension, name);
            outputStream = context.getContentResolver().openOutputStream(fileUri);
            fileInputStream = new FileInputStream(originalFile);

            final int defaultBufferSize = 1024 * 3;
            byte[] buffer = new byte[defaultBufferSize];

            int count;

            while ((count = fileInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, count);
            }
            outputStream.flush();
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, fileUri));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileUri;
    }

    public static Uri generateUri(Context context, String extension, String name) {
        String fileName;
        if (TextUtils.isEmpty(name)) {
            fileName = String.valueOf(System.currentTimeMillis());
        } else {
            fileName = name;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            String mimeType = getMIMEType(extension);
            if (!TextUtils.isEmpty(mimeType)) {
                values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
                if (mimeType.startsWith("video")) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES);
                }
            }
            return context.getContentResolver().insert(uri, values);
        } else {
            String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Environment.DIRECTORY_PICTURES;
            File appDir = new File(storePath);
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            if (!TextUtils.isEmpty(extension)) {
                fileName = fileName + "." + extension;
            }
            return Uri.fromFile(new File(appDir, fileName));
        }
    }

    public static String getMIMEType(String extension) {
        String type = "";
        if (!TextUtils.isEmpty(extension)) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return type;
    }
}
