package com.bukeu.moment.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by taomaogan on 15-3-30.
 */
public class ImageGetFromHttp {
    private static final String TAG = "Cache";

    public static Bitmap downloadBitmap(String url) {
        final HttpClient httpClient = new DefaultHttpClient();
        final HttpGet httpGet = new HttpGet(url);

        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }

            final HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = httpEntity.getContent();
                    FilterInputStream fileInputStream = new FlushedInputStream(inputStream);
                    android.util.Log.d(TAG, "I am from Http!------");
                    return BitmapFactory.decodeStream(fileInputStream);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }

                    httpEntity.consumeContent();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param urlString
     * @param outputStream
     * @return
     */
    public static boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), 8 * 1024);
            out = new BufferedOutputStream(outputStream, 8 * 1024);
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static class FlushedInputStream extends FilterInputStream {

        /**
         * Constructs a new {@code FilterInputStream} with the specified input
         * stream as source.
         * <p/>
         * <p><strong>Warning:</strong> passing a null source creates an invalid
         * {@code FilterInputStream}, that fails on every method that is not
         * overridden. Subclasses should check for null in their constructors.
         *
         * @param in the input stream to filter reads on.
         */
        protected FlushedInputStream(InputStream in) {
            super(in);
        }

        @Override
        public long skip(long byteCount) throws IOException {
            long totalBytesSkipped = 0l;
            while (totalBytesSkipped < byteCount) {
                long bytesSkipped = in.skip(byteCount - totalBytesSkipped);
                if (bytesSkipped == 0l) {
                    int by = read();
                    if (by < 0) {
                        break;
                    } else {
                        bytesSkipped = 1;
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
}
