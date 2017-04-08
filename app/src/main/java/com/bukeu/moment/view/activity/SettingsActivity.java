package com.bukeu.moment.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.MomentConfig;
import com.bukeu.moment.R;
import com.bukeu.moment.util.UIHelper;
import com.bukeu.moment.view.service.UpdateIntentService;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    private RelativeLayout feedbackLayout;
    private RelativeLayout checkUpdateLayout;
    private RelativeLayout aboutUsLayout;
    private Button logoffButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        feedbackLayout = (RelativeLayout) findViewById(R.id.layout_settings_feedback);
        checkUpdateLayout = (RelativeLayout) findViewById(R.id.layout_settings_checkupdate);
        aboutUsLayout = (RelativeLayout) findViewById(R.id.layout_settings_about);
        logoffButton = (Button) findViewById(R.id.btn_settings_logoff);

        if (!MomentApplication.getContext().hasNewAppVersion()) {
            checkUpdateLayout.getChildAt(1).setVisibility(View.VISIBLE);
        }

        initData();
    }

    private void initData() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIHelper.startMainActivity(SettingsActivity.this);
                SettingsActivity.this.finish();
            }
        });

        logoffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MomentApplication.getContext().deleteSignin();
                startActivity(new Intent(SettingsActivity.this, SigninActivity.class));
            }
        });

        feedbackLayout.setOnClickListener(this);
        checkUpdateLayout.setOnClickListener(this);
        aboutUsLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_settings_feedback:
                startActivity(new Intent(SettingsActivity.this, FeedbackActivity.class));
                break;
            case R.id.layout_settings_checkupdate:
                if (MomentConfig.getConfig(this).isNetworkConnected()){
                    UpdateIntentService.startActionCheck(SettingsActivity.this);
                } else {
                    UIHelper.showToastMessage(SettingsActivity.this, getResources().getString(R.string.error_no_network));
                }
                break;
            case R.id.layout_settings_about:
                startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                break;
        }
    }
}
