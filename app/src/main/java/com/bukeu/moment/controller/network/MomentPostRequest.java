package com.bukeu.moment.controller.network;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.model.Moment;
import com.bukeu.moment.util.NetUtil;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * POST moments
 *
 * Created by Max on 2015/3/31.
 */
public class MomentPostRequest extends BaseRequest<Moment>{

    private final Response.Listener<Moment> mListener;

    public MomentPostRequest(Response.Listener<Moment> listener, Response.ErrorListener errorListener) {
        super(Method.POST, NetUtil.URL_MOMENTS, errorListener);
        this.mListener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return super.getHeaders();
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return super.getParams();
    }

    @Override
    public String getBodyContentType() {
        return super.getBodyContentType();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return super.getBody();
    }

    @Override
    protected Response<Moment> parseNetworkResponse(NetworkResponse response) {
        String momentsJSON = new String(response.data, Charset.forName("utf-8"));
        Moment moment = JSON.parseObject(momentsJSON, Moment.class);
        MomentApplication.getContext().setMoment(moment);
        MomentApplication.getContext().getMomentsCache().getMoments().add(0, moment);
        return Response.success(moment, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(Moment response) {
        mListener.onResponse(response);
    }
}
