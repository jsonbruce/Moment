package com.bukeu.moment.controller.network;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.bukeu.moment.MomentApplication;

/**
 * Created by Max on 2015/3/31.
 */
public abstract class BaseRequest<T> extends Request<T> {

    public BaseRequest(int method, String url, Response.ErrorListener listener) {
        super(method, url, listener);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        return null;
    }

    public void enqueue() {
        MomentApplication.getContext().getRequestQueue().add(this);
    }

}
