package com.bukeu.moment.view.fragment;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.R;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.model.Moment;
import com.bukeu.moment.model.MomentList;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class NearbyFragment extends Fragment implements LocationSource, AMapLocationListener,
        AMap.OnMapLoadedListener, AMap.InfoWindowAdapter, AMap.OnInfoWindowClickListener,
        AMap.OnMarkerClickListener, View.OnClickListener,
        AMap.OnMapClickListener {

    public static final String TAG = "NearbyFragment";

    private Activity mActivity;

    private AMap aMap;
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private LocationManagerProxy mAMapLocationManager;

    private List<Moment> mMoments = new ArrayList<>();
    private List<LatLng> mLatlngs = new ArrayList<>();
    private List<Marker> mMarkers = new ArrayList<>();

    private OnNearbyFragmentInteractionListener onNearbyFragmentInteractionListener;

    public static NearbyFragment newInstance() {
        NearbyFragment fragment = new NearbyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
        try {
            onNearbyFragmentInteractionListener = (OnNearbyFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnNearbyFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);

        mapView = (MapView) rootView.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        init();

        return rootView;
    }

    /* 初始化
    */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(false);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

        aMap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
        aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
        aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
        aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
        addMarkersToMap();// 往地图上添加marker
    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap() {
        if (MomentApplication.getContext().getMomentsCache() != null) {
            mMoments = MomentApplication.getContext().getMomentsCache().getMoments();
        }

        ApiController.getMoments(0, new Response.Listener<MomentList>() {
                    @Override
                    public void onResponse(MomentList response) {
                        mMoments.addAll(response.getMoments());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        for (Moment moment : mMoments) {
            Double lon = moment.getLocationLongitude();
            Double lat = moment.getLocationLatitude();
            if (lon != null && lat != null) {
                LatLng latLng = new LatLng(lat, lon);
                mLatlngs.add(latLng);
                MarkerOptions markerOptions = new MarkerOptions().anchor(0.5f, 0.5f)
                        .position(latLng)
                        .title(moment.getNickname())
                        .snippet(moment.getText())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .perspective(true);
                Marker marker = aMap.addMarker(markerOptions);
                marker.setObject(moment);
                mMarkers.add(marker);
            }
        }

        if (mMarkers.size() > 0) {
            mMarkers.get(0).showInfoWindow();
        }

    }

    /**
     * 监听amap地图加载成功事件回调
     */
    @Override
    public void onMapLoaded() {
        // 设置所有maker显示在当前可视区域地图中
        LatLngBounds bounds = null;
        for (LatLng latLng : mLatlngs) {
            bounds = new LatLngBounds.Builder()
                    .include(latLng).build();
        }
        if (bounds != null) {
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
        }
    }

    /**
     * 监听自定义infowindow窗口的infocontents事件回调
     */
    @Override
    public View getInfoContents(Marker marker) {
        View infoContent = mActivity.getLayoutInflater().inflate(
                R.layout.view_map_marker, null);
        render(marker, infoContent);
        return infoContent;
    }

    /**
     * 监听自定义infowindow窗口的infowindow事件回调
     */
    @Override
    public View getInfoWindow(Marker marker) {
        View infoWindow = mActivity.getLayoutInflater().inflate(
                R.layout.view_map_marker, null);
        render(marker, infoWindow);
        return infoWindow;
    }

    /**
     * 自定义infowinfow窗口
     */
    public void render(Marker marker, View view) {
        Moment moment = (Moment) marker.getObject();

        ImageView mAvaterView = (ImageView) view.findViewById(R.id.iv_card_avater);
        TextView mNicknameView = (TextView) view.findViewById(R.id.tv_card_nickname);
        TextView mMomentTextView = (TextView) view.findViewById(R.id.tv_card_text);
//        ImageView mMomentImageView = (ImageView) view.findViewById(R.id.iv_card_image);

//        Glide.with(mActivity).load(moment.getImage())
//                .placeholder(R.drawable.default_moment)
//                .error(R.drawable.default_moment)
//                .into(mMomentImageView);
        Glide.with(mActivity).load(moment.getAvater())
                .placeholder(R.drawable.default_avater)
                .error(R.drawable.default_avater)
                .into(mAvaterView);

        mNicknameView.setText(moment.getNickname());
        mMomentTextView.setText(moment.getText());
    }


    @Override
    public void onMapClick(LatLng latLng) {
    }

    /**
     * 对marker标注点点击响应事件
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        return false;
    }

    /**
     * 监听点击infowindow窗口事件回调
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Moment moment = (Moment) marker.getObject();
        marker.hideInfoWindow();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }


    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 此方法已经废弃
     */
    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getAMapException().getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
            } else {
                Log.e("AmapErr", "Location ERR:" + amapLocation.getAMapException().getErrorCode());
            }
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(mActivity);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用destroy()方法
            // 其中如果间隔时间为-1，则定位只定一次
            // 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
            mAMapLocationManager.requestLocationData(
                    LocationProviderProxy.AMapNetwork, 60 * 1000, 10, this);
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mAMapLocationManager != null) {
            mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destroy();
        }
        mAMapLocationManager = null;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        onNearbyFragmentInteractionListener = null;
    }

    public interface OnNearbyFragmentInteractionListener {
        void onNearbyFragmentInteraction();
    }

}
