package com.bukeu.moment.controller.network;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.bukeu.moment.model.Moment;
import com.bukeu.moment.model.MomentList;
import com.bukeu.moment.util.NetUtil;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Max on 2015/4/7.
 */
public class MomentListRequest extends BaseRequest<MomentList> {

    private Response.Listener<MomentList> mListener;
    private static Map<String, String> mHeaders = new HashMap<String, String>();

    static {
        mHeaders.put("APP-Key", "Bukeu-App-Key");
        mHeaders.put("APP-Secret", "ad12msa234das232in");
    }

    public MomentListRequest(String url, Map<String, String> headers,
                             Response.Listener<MomentList> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);

        this.mListener = listener;

        if (headers != null) {
            mHeaders.putAll(headers);
        }

        setShouldCache(true);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders != null ? mHeaders : super.getHeaders();
    }

    @Override
    protected Response<MomentList> parseNetworkResponse(NetworkResponse response) {
        Map<String, String> headers = response.headers;
        int page = Integer.valueOf(headers.get(NetUtil.HEADER_PAGE_PAGE));
        int size = Integer.valueOf(headers.get(NetUtil.HEADER_PAGE_SIZE));
        int count = Integer.valueOf(headers.get(NetUtil.HEADER_TOTAL_COUNT));

        String momentsJSON = new String(response.data, Charset.forName("utf-8"));

        try {
            List<Moment> moments = JSON.parseArray(momentsJSON, Moment.class);
            MomentList momentList = new MomentList(page, size, count, moments);

            return Response.success(momentList, HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new VolleyError(e));
        }

    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        return super.parseNetworkError(volleyError);
    }

    @Override
    protected void deliverResponse(MomentList response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }
}
