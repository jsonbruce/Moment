package com.bukeu.moment.controller;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.controller.network.MomentListRequest;
import com.bukeu.moment.controller.network.UserListRequest;
import com.bukeu.moment.model.Feedback;
import com.bukeu.moment.model.Moment;
import com.bukeu.moment.model.MomentList;
import com.bukeu.moment.model.Update;
import com.bukeu.moment.model.User;
import com.bukeu.moment.util.NetUtil;
import com.bukeu.moment.util.SecurityUtil;
import com.bukeu.moment.util.StringUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;
import org.apache.http.HttpStatus;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Max on 2015/3/29.
 */
public class ApiController {

    public static final String UTF_8 = "UTF-8";
    public final static int TIMEOUT_CONNECTION = 20000;
    public final static int TIMEOUT_SOCKET = 20000;
    public final static int RETRY_TIME = 3;

    private static SyncHttpClient mSyncHttpClient = new SyncHttpClient();
    private static AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();

    /**
     * 使用异步GET方法，这种情况下URL包含参数
     *
     * @param url
     * @param asyncHttpResponse
     */
    public static void doGetAsync(String url,
                                  AsyncHttpResponseHandler asyncHttpResponse) {
        mAsyncHttpClient.get(url, asyncHttpResponse);
    }

    /**
     * HTTP请求使用异步GET方法
     *
     * @param url
     * @param params            GET方法的URL参数
     * @param asyncHttpResponse
     */
    public static void doGetAsync(String url, RequestParams params,
                                  AsyncHttpResponseHandler asyncHttpResponse) {
        mAsyncHttpClient.get(url, params, asyncHttpResponse);
    }

    /**
     * HTTP请求使用异步POST方法
     *
     * @param url
     * @param params
     * @param asyncHttpResponseHandler
     */
    public static void doPostAsync(String url,
                                   RequestParams params,
                                   AsyncHttpResponseHandler asyncHttpResponseHandler) {
        mAsyncHttpClient.post(url, params, asyncHttpResponseHandler);
    }

    public static String doGetSync(String urlString) {
        String response = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 200) {

                InputStream inputStream = connection.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                response = byteArrayOutputStream.toString("utf-8");
                byteArrayOutputStream.close();
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static <K, V> String doPostSync(String urlString, Map<K, V> params) {
        String response = null;

        HttpURLConnection connection = null;
        URL url = null;
        try {
            url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty(" Content-Type ", " application/x-www-form-urlencoded ");
            connection.connect();

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            // params
            StringBuilder paramsEntity = new StringBuilder();
            for (Map.Entry<K, V> entry : params.entrySet()) {
                if (paramsEntity.length() > 0) {
                    paramsEntity.append("&");
                }
                paramsEntity.append(entry.getKey() + "=");
                paramsEntity.append(entry.getValue());
            }
            out.writeBytes(paramsEntity.toString());

            out.flush();
            out.close();

            InputStream inputStream = null;

            if (connection.getResponseCode() >= HttpStatus.SC_BAD_REQUEST) {
                inputStream = connection.getErrorStream();
            } else {
                inputStream = connection.getInputStream();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, UTF_8));
            StringBuilder result = new StringBuilder();
            String r = null;
            while ((r = br.readLine()) != null) {
                result.append(r);
            }
            response = result.toString();

            inputStream.close();
            br.close();
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    public static HttpURLConnection getHttpURLConnection(String urlStirng) {
        HttpURLConnection httpURLConnection = null;

        String boundary = "**********";

        URL url;
        try {
            url = new URL(urlStirng);
            httpURLConnection = (HttpURLConnection) url.openConnection();

            // 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
            // 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流
            httpURLConnection.setChunkedStreamingMode(256 * 1024);// 256KB

            // 允许输入输出流
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);

            // 使用POST方法
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            httpURLConnection.connect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return httpURLConnection;
    }

    public static <T, K> String doPostByForm(String url, Map<T, K> params, List<String> filepaths) {
        String twoHyphens = "--";
        String boundary = "**********";
        String lineEnd = "\r\n";

        HttpURLConnection httpURLConnection = null;
        FileInputStream fileInputStream = null;
        DataOutputStream outputStream = null;
        InputStream inputStream = null;

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 256 * 1024;// 256KB

        String response = null;

        try {
            httpURLConnection = getHttpURLConnection(url);
            // 获得HTTP连接的输出流
            outputStream = new DataOutputStream(
                    httpURLConnection.getOutputStream());

            // params
            if (params != null) {
                StringBuilder textEntity = new StringBuilder();
                for (Map.Entry<T, K> entry : params.entrySet()) {// 构造文本类型参数的实体数据
                    textEntity.append(twoHyphens);
                    textEntity.append(boundary);
                    textEntity.append(lineEnd);
                    textEntity.append("Content-Disposition: form-data; name=\""
                            + entry.getKey() + "\"" + lineEnd + lineEnd);
                    textEntity.append(entry.getValue());
                    textEntity.append(lineEnd);
                }
                textEntity.append(lineEnd);
                outputStream.writeBytes(textEntity.toString());
            }

            // images
            if (filepaths != null) {
                for (int i = 0, size = filepaths.size(); i < size; i++) {
                    String path = filepaths.get(i);
                    if (path != null) {
                        File img = new File(path);
                        fileInputStream = new FileInputStream(img);
                        outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                        outputStream.writeBytes("Content-Disposition: form-data;"
                                + "name=\"file\";" + "filename=\"" + img.getName()
                                + "\"" + lineEnd + lineEnd);
                        // 设置Buffer, 文件流和HTTP连接流的缓冲区
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];
                        // 将文件流读入Buffer, byteRead为实际读取的字节数
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        while (bytesRead > 0) {
                            outputStream.write(buffer, 0, bufferSize);

                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        }
                        outputStream.writeBytes(lineEnd);
                    }
                }
            }
            // end
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                    + lineEnd);
            outputStream.flush();

            // 获取Response
            if (httpURLConnection.getResponseCode() >= HttpStatus.SC_BAD_REQUEST) {
                inputStream = httpURLConnection.getErrorStream();
            } else {
                inputStream = httpURLConnection.getInputStream();
            }
            InputStreamReader isr = new InputStreamReader(inputStream, UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder result = new StringBuilder();
            String r = null;
            while ((r = br.readLine()) != null) {
                result.append(r);
            }

            response = result.toString();

            br.close();
            inputStream.close();
            outputStream.close();
            fileInputStream.close();
            httpURLConnection.disconnect();

            return response;
        } catch (Exception ex) {
            return null;
        }
    }

    public static void uploadFile(String url, String filepath, RequestParams params,
                                  AsyncHttpResponseHandler asyncHttpResponseHandle) {
        File myFile = new File(filepath);
        try {
            params.put(NetUtil.PARAM_FILE, myFile);
            mAsyncHttpClient.post(url, params, asyncHttpResponseHandle);
        } catch (FileNotFoundException e) {
        }
    }

    public static void downloadFile(String url, FileAsyncHttpResponseHandler fileAsyncHttpResponseHandler) {
        mAsyncHttpClient.get(url, fileAsyncHttpResponseHandler);
    }


// method for business

    public static void signin(String email, String pass, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put(NetUtil.PARAM_EMAIL, email);
        params.put(NetUtil.PARAM_PASSWORD, SecurityUtil.md5(pass));
        doPostAsync(NetUtil.URL_USER_SIGNIN, params, handler);
    }

    public static void signup(String email, String pass, Response.Listener success, Response.ErrorListener error) {
        User user = new User(email, SecurityUtil.md5(pass));
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, NetUtil.URL_USER_SIGNUP, JSON.toJSONString(user),
                success, error);
        MomentApplication.getContext().getRequestQueue().add(request);
    }

    public static void getUser(String uuid, AsyncHttpResponseHandler handler) {
        String url = String.format(NetUtil.URL_USER_INFO, uuid);
        doGetAsync(url, handler);
    }

    public static User updateAvater(String nickname, String path) {
        Map<String, String> params = new HashMap<>();
        try {
            nickname = URLEncoder.encode(nickname, UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        params.put(NetUtil.PARAM_NICKNAME, nickname);
        User user = null;
        try {
            String url = String.format(NetUtil.URL_USER_UPDATE_AVATER, MomentApplication.getContext().getUser().getUuid());
            String response = doPostByForm(url, params, Arrays.asList(path));
            user = JSON.parseObject(response, User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public static void searchUser(String nickanem, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();

        try {
            nickanem = URLEncoder.encode(nickanem, UTF_8);
            params.put(NetUtil.PARAM_NICKNAME, nickanem);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        doGetAsync(NetUtil.URL_SEARCH, params, handler);
    }


    public static void getMomentsWithDifferURL(String url,
                                           Response.Listener<MomentList> listener,
                                           Response.ErrorListener errorListener) {
        new MomentListRequest(url, null, listener, errorListener).enqueue();
    }


    public static void getMoments(int page,
                                  Response.Listener<MomentList> listener,
                                  Response.ErrorListener errorListener) {
        RequestParams rp = new RequestParams();
        rp.put(NetUtil.PARAM_PAGE, page);
        rp.put(NetUtil.PARAM_SIZE, MomentList.DEFAULT_SIZE);
        rp.put(NetUtil.PARAM_SORT, "createDate");
        rp.put(NetUtil.PARAM_ORDER, "desc");
        String url = AsyncHttpClient.getUrlWithQueryString(false, NetUtil.URL_MOMENTS, rp);

        new MomentListRequest(url, null, listener, errorListener).enqueue();
    }

    public static void getMostLikedMoments(int page,
                                  Response.Listener<MomentList> listener,
                                  Response.ErrorListener errorListener) {
        RequestParams rp = new RequestParams();
        rp.put(NetUtil.PARAM_PAGE, page);
        rp.put(NetUtil.PARAM_SIZE, MomentList.DEFAULT_SIZE);
        rp.put(NetUtil.PARAM_SORT, "likesCount");
        rp.put(NetUtil.PARAM_ORDER, "desc");
        String url = AsyncHttpClient.getUrlWithQueryString(false, NetUtil.URL_MOMENTS, rp);

        new MomentListRequest(url, null, listener, errorListener).enqueue();
    }

    public static void getMoments(int page, AsyncHttpResponseHandler handler) {
        RequestParams rp = new RequestParams();
        rp.put(NetUtil.PARAM_PAGE, page);
        rp.put(NetUtil.PARAM_SIZE, 1);
        rp.put(NetUtil.PARAM_SORT, "createDate");
        rp.put(NetUtil.PARAM_ORDER, "desc");

        doGetAsync(NetUtil.URL_MOMENTS, rp, handler);
    }

    public static List<Moment> getMoments(int page) {

        List<Moment> moments = new ArrayList<>();

        RequestParams rp = new RequestParams();
        rp.put(NetUtil.PARAM_PAGE, page);
        rp.put(NetUtil.PARAM_SIZE, 1);
        rp.put(NetUtil.PARAM_SORT, "createDate");
        rp.put(NetUtil.PARAM_ORDER, "desc");
        try {
            String realURL = AsyncHttpClient.getUrlWithQueryString(false, NetUtil.URL_MOMENTS, rp);
            String response = doGetSync(realURL);
            if (response != null) {
                moments = JSON.parseArray(response, Moment.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return moments;
    }

    public static Moment postMoment(Moment moment) {
        try {
            moment.setText(URLEncoder.encode(moment.getText(), UTF_8));
            if (moment.getLocationName() != null) {
                moment.setLocationName(URLEncoder.encode(moment.getLocationName(), UTF_8));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map<String, String> params = new HashMap<>();
        params.put(NetUtil.PARAM_MOMENT, JSON.toJSONString(moment));

        String response = doPostByForm(NetUtil.URL_MOMENTS, params, Arrays.asList(moment.getImage()));
        if (response != null) {
            return JSON.parseObject(response, Moment.class);
        } else {
            return null;
        }
    }



    public static Moment getUserTodayMoment() {
        String url = String.format(NetUtil.URL_USER_TODAY_MOMENT, MomentApplication.getContext().getUser().getUuid());
        String response = doGetSync(url);
        if (!StringUtils.isEmpty(response)) {
            Moment moment = JSON.parseObject(response, Moment.class);
            return moment;
        }
        return null;
    }

    public static void getUserMoments(int page,
                                  Response.Listener<MomentList> listener,
                                  Response.ErrorListener errorListener) {
        RequestParams rp = new RequestParams();
        rp.put(NetUtil.PARAM_PAGE, page);
        rp.put(NetUtil.PARAM_SIZE, MomentList.DEFAULT_SIZE);
        rp.put(NetUtil.PARAM_SORT, "createDate");
        rp.put(NetUtil.PARAM_ORDER, "desc");
        String url = String.format(NetUtil.URL_USER_MOMENTS, MomentApplication.getContext().getUser().getUuid());
        String realurl = AsyncHttpClient.getUrlWithQueryString(false, url, rp);

        new MomentListRequest(realurl, null, listener, errorListener).enqueue();
    }

    public static void getUserMoments(AsyncHttpResponseHandler handler) {
        String url = String.format(NetUtil.URL_USER_MOMENTS, MomentApplication.getContext().getUser().getUuid());
        doGetAsync(url, handler);
    }

    public static List<Moment> getUserMoments() {
        String url = String.format(NetUtil.URL_USER_MOMENTS, MomentApplication.getContext().getUser().getUuid());
        String response = doGetSync(url);
        List<Moment> moments = new ArrayList<>();
        try {
            moments = JSON.parseArray(response, Moment.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return moments;
    }


// follows

    public static List<User> getFollowing() {

        List<User> following = new ArrayList<>();

        String url = String.format(NetUtil.URL_FOLLOWING, MomentApplication.getContext().getUser().getUuid());
        String response = null;
        try {
            response = doGetSync(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (response != null) {
            following = JSON.parseArray(response, User.class);
        }
        return following;
    }

    public static List<User> getFollowers() {
        List<User> following = new ArrayList<>();

        String url = String.format(NetUtil.URL_FOLLOWERS, MomentApplication.getContext().getUser().getUuid());
        String response = null;
        try {
            response = doGetSync(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (response != null) {
            following = JSON.parseArray(response, User.class);
        }
        return following;
    }

    public static void getFollowingAsync(Response.Listener listener, Response.ErrorListener errorListener) {
        String url = String.format(NetUtil.URL_FOLLOWING, MomentApplication.getContext().getUser().getUuid());
        new UserListRequest(url, listener, errorListener).enqueue();
    }

    public static void updateFollowing(String followerUuid, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put(NetUtil.PARAM_UUID, followerUuid);
        params.put(NetUtil.PARAM_FOLLOWER_UUID, MomentApplication.getContext().getUser().getUuid());
        doPostAsync(NetUtil.URL_FOLLOWS, params, handler);
    }

    public static void getFollowingMoments(int page,
                                  Response.Listener<MomentList> listener,
                                  Response.ErrorListener errorListener) {
        RequestParams rp = new RequestParams();
        rp.put(NetUtil.PARAM_PAGE, page);
        rp.put(NetUtil.PARAM_SIZE, MomentList.DEFAULT_SIZE);
        String url = String.format(NetUtil.URL_FOLLOWING_MOMENTS, MomentApplication.getContext().getUser().getUuid());
        String realurl = AsyncHttpClient.getUrlWithQueryString(false, url, rp);

        new MomentListRequest(realurl, null, listener, errorListener).enqueue();
    }


    /**
     * Get user's liked momentId today
     *
     * @return
     */
    public static List<Long> getLikedMomentId() {
        String url = String.format(NetUtil.URL_LIKES_TODAY, MomentApplication.getContext().getUser().getUuid());
        List<Long> momentIds = null;
        try {
            String response = doGetSync(url);
            momentIds = JSON.parseArray(response, Long.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return momentIds;
    }

    public static void updateLike(long momentId, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put(NetUtil.PARAM_UUID, MomentApplication.getContext().getUser().getUuid());
        params.put(NetUtil.PARAM_MOMENTID, momentId);
        doPostAsync(NetUtil.URL_LIKES, params, handler);
    }

    public static void addLikesInBatch() {
        Map<Long, Long> likeOperation = MomentApplication.getContext().getLikeOperation();
        List<Long> changedMomentId = new ArrayList<>();
        for (Long id : likeOperation.keySet()) {
            long op = likeOperation.get(id);
            if (1 == (op & 1)) {
                updateLike(id, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d("Moment", new String(responseBody));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.d("Moment", new String(responseBody));
                    }
                });
                changedMomentId.add(id);
            }
        }
    }

    public static List<Moment> updateLikesInBatchSync(List<Long> changedMomentId) {

        String momentIDs = JSON.toJSONString(changedMomentId);
        Map<String, String> params = new HashMap<>();
        params.put(NetUtil.PARAM_UUID, MomentApplication.getContext().getUser().getUuid());
        params.put(NetUtil.PARAM_MOMENTIDS, momentIDs);
        String response = doPostSync(NetUtil.URL_LIKES, params);

        List<Moment> moments = null;
        try {
            moments = JSON.parseArray(response, Moment.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return moments;
    }

    public static Update getUpdate() {
        String response = doGetSync(NetUtil.URL_APPVERSION);
        Update update = null;
        try {
            update = JSON.parseObject(response, Update.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return update;
    }

    public static void postFeedback(Feedback feedback) {
        Map<String, String> params = new HashMap<>();
        try {
            feedback.setContent(URLEncoder.encode(feedback.getContent(), UTF_8));
            params.put(NetUtil.PARAM_FEEDBACK, JSON.toJSONString(feedback));
            doPostSync(NetUtil.URL_FEEDBACK, params);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
