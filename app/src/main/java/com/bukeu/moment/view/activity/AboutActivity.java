package com.bukeu.moment.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.bukeu.moment.R;

public class AboutActivity extends BaseActivity implements View.OnClickListener{

    private LinearLayout weixin;
    private LinearLayout weibo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        weixin = (LinearLayout) findViewById(R.id.layout_about_weixin);
        weibo = (LinearLayout) findViewById(R.id.layout_about_weibo);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_about_weixin:
                break;
            case R.id.layout_about_weibo:
                break;
        }
    }
}
