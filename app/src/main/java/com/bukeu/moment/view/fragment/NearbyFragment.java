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

    /* ��ʼ��
    */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
    }

    /**
     * ����һЩamap������
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// ���ö�λ����
        aMap.getUiSettings().setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// ����Ĭ�϶�λ��ť�Ƿ���ʾ
        aMap.setMyLocationEnabled(false);// ����Ϊtrue��ʾ��ʾ��λ�㲢�ɴ�����λ��false��ʾ���ض�λ�㲢���ɴ�����λ��Ĭ����false
        // ���ö�λ������Ϊ��λģʽ �������ɶ�λ��������ͼ������������ת����
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

        aMap.setOnMapLoadedListener(this);// ����amap���سɹ��¼�������
        aMap.setInfoWindowAdapter(this);// �����Զ���InfoWindow��ʽ
        aMap.setOnMarkerClickListener(this);// ���õ��marker�¼�������
        aMap.setOnInfoWindowClickListener(this);// ���õ��infoWindow�¼�������
        addMarkersToMap();// ����ͼ�����marker
    }

    /**
     * �ڵ�ͼ�����marker
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
     * ����amap��ͼ���سɹ��¼��ص�
     */
    @Override
    public void onMapLoaded() {
        // ��������maker��ʾ�ڵ�ǰ���������ͼ��
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
     * �����Զ���infowindow���ڵ�infocontents�¼��ص�
     */
    @Override
    public View getInfoContents(Marker marker) {
        View infoContent = mActivity.getLayoutInflater().inflate(
                R.layout.view_map_marker, null);
        render(marker, infoContent);
        return infoContent;
    }

    /**
     * �����Զ���infowindow���ڵ�infowindow�¼��ص�
     */
    @Override
    public View getInfoWindow(Marker marker) {
        View infoWindow = mActivity.getLayoutInflater().inflate(
                R.layout.view_map_marker, null);
        render(marker, infoWindow);
        return infoWindow;
    }

    /**
     * �Զ���infowinfow����
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
     * ��marker��ע������Ӧ�¼�
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        return false;
    }

    /**
     * �������infowindow�����¼��ص�
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
     * ����������д
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * ����������д
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    /**
     * ����������д
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * ����������д
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * �˷����Ѿ�����
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
     * ��λ�ɹ���ص�����
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getAMapException().getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// ��ʾϵͳС����
            } else {
                Log.e("AmapErr", "Location ERR:" + amapLocation.getAMapException().getErrorCode());
            }
        }
    }

    /**
     * ���λ
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(mActivity);
            // �˷���Ϊÿ���̶�ʱ��ᷢ��һ�ζ�λ����Ϊ�˼��ٵ������Ļ������������ģ�
            // ע�����ú��ʵĶ�λʱ��ļ������С���֧��Ϊ2000ms���������ں���ʱ�����removeUpdates()������ȡ����λ����
            // �ڶ�λ�������ں��ʵ��������ڵ���destroy()����
            // ����������ʱ��Ϊ-1����λֻ��һ��
            // �ڵ��ζ�λ����£���λ���۳ɹ���񣬶��������removeUpdates()�����Ƴ����󣬶�λsdk�ڲ����Ƴ�
            mAMapLocationManager.requestLocationData(
                    LocationProviderProxy.AMapNetwork, 60 * 1000, 10, this);
        }
    }

    /**
     * ֹͣ��λ
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
