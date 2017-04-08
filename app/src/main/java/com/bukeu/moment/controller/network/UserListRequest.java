package com.bukeu.moment.controller.network;

import com.alibaba.fastjson.JSON;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.bukeu.moment.model.User;
import com.bukeu.moment.model.UserList;
import com.bukeu.moment.util.NetUtil;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Created by Max on 2015/4/14.
 */
public class UserListRequest extends BaseRequest<UserList> {

    private Response.Listener<UserList> mListener;

    public UserListRequest(String url, int page,
                           Response.Listener<UserList> listener, Response.ErrorListener errorListener) {
        this(url, listener, errorListener);
    }

    public UserListRequest(String url, Response.Listener<UserList> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.mListener = listener;
    }

    @Override
    protected Response<UserList> parseNetworkResponse(NetworkResponse response) {
        Map<String, String> headers = response.headers;
        int page = Integer.valueOf(headers.get(NetUtil.HEADER_PAGE_PAGE));
        int size = Integer.valueOf(headers.get(NetUtil.HEADER_PAGE_SIZE));
        int count = Integer.valueOf(headers.get(NetUtil.HEADER_TOTAL_COUNT));

        String usersJSON = new String(response.data, Charset.forName("utf-8"));
        List<User> users = JSON.parseArray(usersJSON, User.class);

        UserList userList = new UserList(page, size, count, users);

        return Response.success(userList, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(UserList response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }
}
