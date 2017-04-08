package com.bukeu.moment.controller.network;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.bukeu.moment.model.BaseModel;
import com.bukeu.moment.util.NetUtil;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Max on 2015/4/16.
 */
public class PageableRequest<T> extends BaseRequest<T> {

    private final Class<T> mClass;
    private final Response.Listener<T> mListener;
    private static Map<String, String> mHeaders = new HashMap<String, String>();

    static {
        mHeaders.put("APP-Key", "Bukeu-App-Key");
        mHeaders.put("APP-Secret", "ad12msa234das232in");
    }

    public PageableRequest(String url, Class<T> clazz, Map<String, String> headers,
                           Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);

        this.mClass = clazz;
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
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        Map<String, String> headers = response.headers;
        int page = Integer.valueOf(headers.get(NetUtil.HEADER_PAGE_PAGE));
        int size = Integer.valueOf(headers.get(NetUtil.HEADER_PAGE_SIZE));
        int count = Integer.valueOf(headers.get(NetUtil.HEADER_TOTAL_COUNT));

        String json = new String(response.data, Charset.forName("utf-8"));

        try {
            List<BaseModel> datas = JSON.parseArray(json, BaseModel.class);

            //设定构造函数的参数类型
            Class<?>[] parameterTypes = new Class<?>[4];
            parameterTypes[0] = int.class;
            parameterTypes[1] = int.class;
            parameterTypes[2] = int.class;
            parameterTypes[3] = List.class;
            //获取构造器
            Constructor<T> constructor = mClass.getConstructor(parameterTypes);

            //初始化构造参数
            Object[] pars = new Object[4];
            pars[0] = page;
            pars[1] = size;
            pars[2] = count;
            pars[3] = datas;

            //构造对象
            T dataList = constructor.newInstance(pars);

            return Response.success(dataList, HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }
}
