package com.bukeu.moment.view.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.MomentConfig;
import com.bukeu.moment.R;
import com.bukeu.moment.model.Moment;
import com.bukeu.moment.util.ImageUtils;
import com.bukeu.moment.util.StringUtils;
import com.bukeu.moment.util.UIHelper;
import com.bukeu.moment.view.service.PublishIntentService;

/**
 * Created by Miroslaw Stanek on 21.02.15.
 */
public class PublishActivity extends BaseActivity implements
        AMapLocationListener {

    public static final String ARG_TAKEN_PHOTO_PATH = "arg_taken_photo_path";

    private LocationManagerProxy mLocationManagerProxy;

    LinearLayout wrapperLayout;
    ImageView momentImage;
    EditText momentText;
    TextView locationView;

    private String photoPath;
    private int photoSize;

    private Moment mMoment = new Moment();

    public static void openWithPhotoPath(Activity openingActivity, String path) {
        Intent intent = new Intent(openingActivity, PublishActivity.class);
        intent.putExtra(ARG_TAKEN_PHOTO_PATH, path);
        openingActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (savedInstanceState == null) {
                photoPath = getIntent().getStringExtra(ARG_TAKEN_PHOTO_PATH);
            } else {
                photoPath = savedInstanceState.getString(ARG_TAKEN_PHOTO_PATH);
            }
        } catch (Exception e) {
            UIHelper.showToastMessage(this, "error");
        }

        initView();
        initData();

    }

    private void initView() {
        setContentView(R.layout.activity_publish);

        updateStatusBarColor();

        wrapperLayout = (LinearLayout) findViewById(R.id.layout_publish_card);
        momentImage = (ImageView) findViewById(R.id.ivPhoto);
        momentText = (EditText) findViewById(R.id.etDescription);
        locationView = (TextView) findViewById(R.id.tv_publish_location);

        momentImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                momentImage.getViewTreeObserver().removeOnPreDrawListener(this);
                startMomentImageAnimation();
                return true;
            }
        });
    }

    private void initData() {
        photoSize = getResources().getDimensionPixelSize(R.dimen.publish_photo_thumbnail_size);

        initLocation();
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        // 初始化定位，只采用网络定位
        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        mLocationManagerProxy.setGpsEnable(false);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用destroy()方法
        // 其中如果间隔时间为-1，则定位只定一次,
        // 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
        mLocationManagerProxy.requestLocationData(
                LocationProviderProxy.AMapNetwork, 60 * 1000, 15, this);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateStatusBarColor() {
        if (MomentConfig.isApi21()) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary));
        }
    }

    private void startMomentImageAnimation() {
        wrapperLayout.updateViewLayout(momentImage, new LinearLayout.LayoutParams(
                wrapperLayout.getWidth(), wrapperLayout.getWidth()));

        momentImage.setScaleX(0);
        momentImage.setScaleY(0);
        Bitmap momentImage = ImageUtils.loadImgThumbnail(photoPath, photoSize, photoSize);
        momentImage = ImageUtils.getBitmapByPath(photoPath);
        if (momentImage != null) {
            this.momentImage.setImageBitmap(momentImage);
        } else {
            this.momentImage.setImageResource(R.drawable.default_moment);
            UIHelper.showToastMessage(this, "image error...");
        }
        this.momentImage.animate()
                .scaleX(1.f).scaleY(1.f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(400)
                .setStartDelay(100)
                .start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_publish, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_publish) {
            backToMainActivity();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void backToMainActivity() {
        String text = momentText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Moment text!", Toast.LENGTH_SHORT).show();
            return;
        }
        mMoment.setText(text.equals("") ? "Yo" : text);
        mMoment.setImage(photoPath);
        mMoment.setUuid(MomentApplication.getContext().getUser().getUuid());
        mMoment.setCreateDate(StringUtils.getCurrentDatetime());

        MomentConfig.getConfig(this).setHasPublished(true);
        PublishIntentService.startActionPublish(PublishActivity.this, mMoment);

        MomentApplication.getContext().getMoment().setText(text.equals("") ? "Yo" : text);
        MomentApplication.getContext().getMoment().setCreateDate(StringUtils.getCurrentDatetime());
        MomentApplication.getContext().getMoment().setImage(photoPath);
        MomentApplication.getContext().getMoment().setUuid(MomentApplication.getContext().getUser().getUuid());
        MomentApplication.getContext().getMoment().setNickname(MomentApplication.getContext().getUser().getNickname());
        MomentApplication.getContext().getMoment().setAvater(MomentApplication.getContext().getUser().getAvater());

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(MainActivity.ACTION_SHOW_LOADING_ITEM);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_TAKEN_PHOTO_PATH, photoPath);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 移除定位请求
        mLocationManagerProxy.removeUpdates(this);
        // 销毁定位
        mLocationManagerProxy.destroy();
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null && amapLocation.getAMapException().getErrorCode() == 0) {

            mMoment.setLocationLongitude(amapLocation.getLongitude());
            mMoment.setLocationLatitude(amapLocation.getLatitude());
            mMoment.setLocationName(amapLocation.getAddress());

            locationView.setText(amapLocation.getAddress());

        } else {
            locationView.setText("Cannot get your position");
            Log.e("AmapErr", "Location ERR:" + amapLocation.getAMapException().getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
