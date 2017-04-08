package com.bukeu.moment.util;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

/**
 * Created by taomaogan on 15-3-30.
 */
public class ImageMemoryCache {
    private static final String TAG = "Cache";

    private static final int SOFT_CACHE_SIZE = 15;
    private static LruCache<String, Bitmap> mLruCache;
    private static LinkedHashMap<String, SoftReference> mSoftCache;

    private int mLruCacheSize = -1;

    public ImageMemoryCache(Context context) {
        this(context, -1);
    }

    public ImageMemoryCache(Context context, int lruCacheSize) {
        mLruCacheSize = lruCacheSize;
        if (mLruCacheSize <= 0) {
            int maxMemory = (int) Runtime.getRuntime().maxMemory();
            int memoryClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
            mLruCacheSize = 1024 * 1024 * memoryClass / 4;
        }

        mLruCache = new LruCache<String, Bitmap>(mLruCacheSize) {

            @Override
            protected int sizeOf(String key, Bitmap value) {
                if (value != null) {
                    return value.getRowBytes() * value.getHeight();
                }
                return 0;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                if (oldValue != null) {
                    android.util.Log.d(TAG, "I am from SoftReference save!------");
                    mSoftCache.put(key, new SoftReference(oldValue));
                }
            }
        };

        mSoftCache = new LinkedHashMap(SOFT_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Entry eldest) {
                if (size() > SOFT_CACHE_SIZE) {
                    return true;
                }
                return false;
            }
        };
    }

    public Bitmap getBitmapFromCache(String url) {
        Bitmap bitmap;
        synchronized (mLruCache) {
            bitmap = mLruCache.get(url);
            if (bitmap != null) {
                android.util.Log.d(TAG, "I am from LruCache!------");
                mLruCache.remove(url);
                mLruCache.put(url, bitmap);
                return bitmap;
            }
        }
        synchronized (mSoftCache) {
            SoftReference<Bitmap> bitmapReference = mSoftCache.get(url);
            if (bitmapReference != null) {
                bitmap = bitmapReference.get();
                if (bitmap != null) {
                    mLruCache.put(url, bitmap);
                    mSoftCache.remove(url);
                    android.util.Log.d(TAG, "I am from SoftCache!------");
                    return bitmap;
                } else {
                    mSoftCache.remove(url);
                }
            }
        }

        return null;
    }

    public void addBitmapToCache(String url, Bitmap bitmap) {
        if (bitmap != null) {
            synchronized (mLruCache) {
                android.util.Log.d(TAG, "I am from LruCache save!------");
                mLruCache.put(url, bitmap);
            }
        }
    }


}
