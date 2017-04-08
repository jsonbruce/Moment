package com.bukeu.moment.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author max xu
 */
public class ImageManager {
    private static ImageManager sImageManager;

    private ImageMemoryCache mImageMemoryCache;
    private DiskLruCache mDiskLruCache;
    private ImageGetFromHttp mImageGetFromHttp;

    private Set<BitmapWorkerTask> taskCollection;
    private View mView;

    private int mLruCacheSize = -1;

    public static ImageManager getInstance(Context context, View root) {
        if (sImageManager == null) {
            sImageManager = new ImageManager(context, root);
        }
        return sImageManager;
    }

    private ImageManager(Context context, View root) {
        this.mView = root;
        taskCollection = new HashSet<>();
        mImageMemoryCache = new ImageMemoryCache(context);
        mImageGetFromHttp = new ImageGetFromHttp();
        try {
            File cacheDir = getDiskCacheDir(context, "images");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            mDiskLruCache = DiskLruCache
                    .open(cacheDir, getAppVersion(context), 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();   // /sdcard/Android/data/<>/cache
        } else {
            cachePath = context.getCacheDir().getPath();          // /data/data/<>/cache
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    public int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public void setLruCacheSize(int size) {
        mLruCacheSize = size;
    }

    public Bitmap getBitmap(String url) {
        Bitmap bitmap = mImageMemoryCache.getBitmapFromCache(url);
        if (bitmap == null) {
            FileDescriptor fileDescriptor = null;
            FileInputStream fileInputStream = null;
            DiskLruCache.Snapshot snapShot = null;
            try {
                final String key = hashKeyForDisk(url);
                snapShot = mDiskLruCache.get(key);
                if (snapShot == null) {
                    DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                    if (editor != null) {
                        OutputStream outputStream = editor.newOutputStream(0);
                        if (ImageGetFromHttp.downloadUrlToStream(url, outputStream)) {
                            editor.commit();
                        } else {
                            editor.abort();
                        }
                    }
                    snapShot = mDiskLruCache.get(key);
                }
                if (snapShot != null) {
                    fileInputStream = (FileInputStream) snapShot.getInputStream(0);
                    fileDescriptor = fileInputStream.getFD();
                }
                if (fileDescriptor != null) {
                    bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                    mImageMemoryCache.addBitmapToCache(url, bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileDescriptor == null && fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        return bitmap;
    }

    public void loadBitmaps(ImageView imageView, String imageUrl) {
        try {
            Bitmap bitmap = mImageMemoryCache.getBitmapFromCache(imageUrl);
            if (bitmap == null) {
                BitmapWorkerTask task = new BitmapWorkerTask(mView);
                taskCollection.add(task);
                task.execute(imageUrl);
            } else {
                if (imageView != null && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

        private View view;

        public BitmapWorkerTask(View view) {
            this.view = view;
        }

        private String imageUrl;

        @Override
        protected Bitmap doInBackground(String... params) {
            imageUrl = params[0];
            Bitmap bitmap = null;
            bitmap = getBitmap(imageUrl);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageView = (ImageView) view.findViewWithTag(imageUrl);
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
            taskCollection.remove(this);
        }


    }
}
