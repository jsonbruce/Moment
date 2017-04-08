package com.bukeu.moment.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by taomaogan on 15-3-30.
 */
public class ImageFileCache {
    private static final String TAG = "Cache";

    private static final String CACHE_DIR = "imageCache";
    private static final String WHOLESALE_CONV = ".cach";

    private static final int MB = 1024 * 1024;
    private static final int CACHE_SIZE = 10;
    private static final int FREE_SD_SPACE_NEEDED_TO_CACHE = 10;

    public ImageFileCache() {
        removeCache(getDirectory());
    }

    public Bitmap getImageFromFile(String url) {
        String path = getDirectory() + "/" + covertUrlToFileName(url);
        File file = new File(path);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap == null) {
                file.delete();
            } else {
                updateFileTime(path);
                android.util.Log.d(TAG, "I am from FileCache!------");
                return bitmap;
            }
        }
        return null;
    }

    /**
     * 将图片存储到sd卡
     * @param url
     * @param bitmap
     */
    public void saveBitmap(String url, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }

        //保证存储控空间足够
        if (FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
            return;
        }

        //文件名
        String fileName = covertUrlToFileName(url);
        //sd卡路径
        String dir = getDirectory();
        File dirFile = new File(dir);
        //判断路径是否存在
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        File file = new File(dir + "/" + fileName);
        try {
            file.createNewFile();
            OutputStream outputStream = new FileOutputStream(file);
            //存储png图片,图片质量为最高,意味着当存储大图时，效率低，有可能oom
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            android.util.Log.d(TAG, "I am from FileCache save!------");
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean removeCache(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) {
            return true;
        }
        //sd卡是否有读取权限
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return false;
        }

        int dirSize = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().contains(WHOLESALE_CONV)) {
                dirSize += files[i].length();
            }
        }

        if (dirSize > CACHE_SIZE * MB || FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
            int removeFactor = (int) ((0.4 * files.length) + 1);
            Arrays.sort(files, new FileLastModifSort());
            for (int i = 0; i < removeFactor; i++) {
                if (files[i].getName().contains(WHOLESALE_CONV)) {
                    files[i].delete();
                }
            }
        }
        //可用空间比需要的空间少
        if (freeSpaceOnSd() <= CACHE_SIZE) {
            return false;
        }

        return true;
    }

    public void updateFileTime(String path) {
        File file = new File(path);
        long newModifiedTime = System.currentTimeMillis();
        file.setLastModified(newModifiedTime);
    }

    private int freeSpaceOnSd() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdFreeMB = ((double) statFs.getAvailableBlocksLong() * (double) statFs.getBlockSizeLong()) / MB;
        return (int) sdFreeMB;
    }

    private String covertUrlToFileName(String url) {
        String[] strs = url.split("/");
        return strs[strs.length - 1] + WHOLESALE_CONV;
    }

    private String getDirectory() {
        String dir = getSDPath() + "/" + CACHE_DIR;
        return dir;
    }

    private String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        if (sdDir != null) {
            return sdDir.toString();
        } else {
            return "";
        }
    }

    private class FileLastModifSort implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            if (lhs.lastModified() > rhs.lastModified()) {
                return 1;
            } else if (lhs.lastModified() == rhs.lastModified()) {
                return 0;
            } else {
                return -1;
            }
        }
    }
 }




















