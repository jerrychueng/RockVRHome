package com.rockchip.vr.home.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by waha on 2016/7/22.
 */
public class StorageUtils {

    public static long[] getStorageState(Context context, int memoryType) {
        String path = getStoragePath(context)[memoryType];
        if (TextUtils.isEmpty(path)) {
            return new long[]{0, 0};
        }
        StatFs statFs = new StatFs(path);
        /* Block的size */
        long blockSize = statFs.getBlockSizeLong();
        /* 总Block数量 */
        long totalBlocks = statFs.getBlockCountLong();
        /* 已使用的Block数量 */
        long availableBlocks = statFs.getAvailableBlocksLong();
        return new long[]{availableBlocks * blockSize, totalBlocks * blockSize};
    }

    /**
     * @param context
     * @return String[1]sd卡路径，String[0]内存路径，String[2]u盘路径
     */
    public static String[] getStoragePath(Context context) {
        String sdcard_dir = null, usb_dir = null;
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class clazzStorageManager = Class.forName("android.os.storage.StorageManager");
            Method methodGetVolumes = clazzStorageManager.getMethod("getVolumes", new Class[0]);
            final List volumes = (List) methodGetVolumes.invoke(storageManager, new Object[]{});
            Class clazzVolumeInfo = Class.forName("android.os.storage.VolumeInfo");
            Method methodGetDescriptionComparator = clazzVolumeInfo.getMethod("getDescriptionComparator", new Class[0]);
            methodGetDescriptionComparator.invoke(new Object[]{});
            Collections.sort(volumes, (Comparator) methodGetDescriptionComparator.invoke(null, new Object[]{}));
            Method methodGetType = clazzVolumeInfo.getMethod("getType", new Class[0]);
            Method methodGetDisk = clazzVolumeInfo.getMethod("getDisk", new Class[0]);
            Method methodBuildStorageVolume = clazzVolumeInfo.getMethod("buildStorageVolume", Context.class, int.class, boolean.class);

            Class clazzDiskInfo = Class.forName("android.os.storage.DiskInfo");
            Method methodisSd = clazzDiskInfo.getMethod("isSd", new Class[0]);
            Method methodisUsb = clazzDiskInfo.getMethod("isUsb", new Class[0]);

            Class clazzContext = Class.forName("android.content.Context");
            Method methodGetUserId = clazzContext.getMethod("getUserId", new Class[0]);

            Class clazzStorageVolume = Class.forName("android.os.storage.StorageVolume");
            Method methodGetPath = clazzStorageVolume.getMethod("getPath", new Class[0]);
            for (int i = 0; i < volumes.size(); i++) {
                if ((int) methodGetType.invoke(volumes.get(i), new Object[]{}) == 0) {
                    Object cdisk = methodGetDisk.invoke(volumes.get(i), new Object[]{});
                    if (cdisk != null) {
                        if ((boolean) methodisSd.invoke(cdisk, new Object[]{})) {
                            Object cStorageVolume = methodBuildStorageVolume.invoke(volumes.get(i), context, (int) methodGetUserId.invoke(context, new Object[]{}), false);
                            sdcard_dir = (String) methodGetPath.invoke(cStorageVolume, new Object[]{});
                        } else if ((boolean) methodisUsb.invoke(cdisk, new Object[]{})) {
                            Object cStorageVolume = methodBuildStorageVolume.invoke(volumes.get(i), context, (int) methodGetUserId.invoke(context, new Object[]{}), false);
                            usb_dir = (String) methodGetPath.invoke(cStorageVolume, new Object[]{});
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] paths = {
                Environment.getExternalStorageDirectory().getPath(),
                sdcard_dir, usb_dir};
        RockLog.d("storage path: " + paths[0] + "===" + paths[1] + "===" + paths[2]);
        return paths;
    }

}
