package com.bukeu.moment.view.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.bukeu.moment.R;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.model.User;
import com.bukeu.moment.view.fragment.LoadingFragment;
import com.bukeu.moment.view.fragment.SearchFragment;
import com.bukeu.moment.view.widget.ClearEditText;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.nio.charset.Charset;

public class SearchActivity extends BaseActivity {

    private ImageButton mBackButton;
    private ClearEditText mKeywordsEdit;
    LinearLayout notFoundLayout;
    Button refreshButton;

    private String mKeywords = null;

    private SearchFragment mSearchFragment;
    private LoadingFragment mLoadingFragment;

    private static final int SEARCH_NOT_FOUNT = 1;
    private static final int SEARCH_FOUND = 2;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEARCH_NOT_FOUNT:
                    setNotFoundLayoutVisible(true);
                    break;
                case SEARCH_FOUND:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initView();
        initData();

    }

    private void initView() {
        mBackButton = (ImageButton) findViewById(R.id.iv_back);
        mKeywordsEdit = (ClearEditText) findViewById(R.id.et_search_keywords);
        notFoundLayout = (LinearLayout) findViewById(R.id.layout_search_not_found);
        refreshButton = (Button) findViewById(R.id.btn_search_refresh);
    }

    private void initData() {
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearch();
            }
        });

        mKeywordsEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mKeywords = s.toString();
                if (!TextUtils.isEmpty(mKeywords)) {
                    doSearch();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                mKeywords = s.toString();
            }
        });
    }

    private void doSearch() {
        setNotFoundLayoutVisible(false);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        mLoadingFragment = new LoadingFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.frame_search_result, mLoadingFragment, LoadingFragment.TAG)
                .commit();

        ApiController.searchUser(mKeywords, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String s = new String(responseBody, Charset.forName("utf-8"));
                User u = JSON.parseObject(s, User.class);
                if (u != null) {
                    mSearchFragment = SearchFragment.newInstance(u);
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_search_result, mSearchFragment)
                            .commit();
                    setNotFoundLayoutVisible(false);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String s = new String(responseBody, Charset.forName("utf-8"));
                if (statusCode == 404) {
                    mHandler.sendEmptyMessage(SEARCH_NOT_FOUNT);
                }
            }
        });
    }

    private void setNotFoundLayoutVisible(boolean visible) {
        if (visible) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(LoadingFragment.TAG);
            if (fragment != null) {
                fragmentManager.beginTransaction().remove(fragment).commit();
            }
        }
        this.notFoundLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

}
