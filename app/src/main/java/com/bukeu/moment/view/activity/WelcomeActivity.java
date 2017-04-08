package com.bukeu.moment.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.MomentConfig;
import com.bukeu.moment.R;
import com.bukeu.moment.util.UIHelper;

public class WelcomeActivity extends ActionBarActivity {

    private Handler mHandler = new Handler();

    private View welcomeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    private void initView() {
        welcomeView = View.inflate(this, R.layout.activity_welcome, null);
        setContentView(welcomeView);

        initData();

        //渐变展示启动屏
        AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
        aa.setDuration(2000);
        welcomeView.startAnimation(aa);
        aa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startMainActivity();
                    }
                }, 3000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }

        });
    }

    private void initData() {
        if (!MomentConfig.getConfig(this).isNetworkConnected()) {
            UIHelper.showToastMessage(WelcomeActivity.this, getResources().getString(R.string.error_no_network));
        }else {
            MomentApplication.getContext().checkSignin();
        }
    }

    private void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

}
