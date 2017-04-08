package com.bukeu.moment.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.MomentConfig;
import com.bukeu.moment.R;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.controller.adapter.UserProfileAdapter;
import com.bukeu.moment.controller.listener.OnRecyclerViewScrollListener;
import com.bukeu.moment.model.Moment;
import com.bukeu.moment.model.MomentList;
import com.bukeu.moment.model.User;
import com.bukeu.moment.util.UIHelper;
import com.bukeu.moment.view.widget.RevealBackgroundView;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.nio.charset.Charset;
import java.util.List;

public class UserProfileActivity extends BaseActivity implements RevealBackgroundView.OnStateChangeListener {
    public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";

    private static final int TO_REFRESH = 0;
    private static final int TO_LOADMORE = 1;

    private Context mContext;

    RevealBackgroundView mRevealBackgroundView;
    private SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView mUserProfileRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private UserProfileAdapter mUserProfileAdapter;
    private ImageButton mFabButton;

    private MomentList mMomentList;

    private static final int UPDATE_INFO = 2;
    private static final int LOAD_MOMENTS = 3;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_INFO:
                    mUserProfileAdapter.updateUserInfo();
                    break;
                case LOAD_MOMENTS:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<Moment> moments = ApiController.getUserMoments();
                            mUserProfileAdapter.updateMoments(moments);
                        }
                    }).start();
                    break;
                case TO_REFRESH:
                    ApiController.getUserMoments(0, new Response.Listener<MomentList>() {
                        @Override
                        public void onResponse(MomentList response) {
                            mMomentList = response;
                            mUserProfileAdapter.updateMoments(mMomentList.getMoments());
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                    break;
                case TO_LOADMORE:
                    ApiController.getUserMoments(mMomentList.getPage()+1, new Response.Listener<MomentList>() {
                        @Override
                        public void onResponse(MomentList response) {
                            mMomentList = response;
                            mUserProfileAdapter.loadOldMoments(mMomentList.getMoments());
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                    break;
            }
        }
    };

    public static void startUserProfileFromLocation(int[] startingLocation, Activity startingActivity) {
        Intent intent = new Intent(startingActivity, UserProfileActivity.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        startingActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        this.mContext = this;

        mToolbar.setTitle(MomentApplication.getContext().getUser().getNickname());
        mToolbar.setOnMenuItemClickListener(onMenuItemClickListener);

        mFabButton = (ImageButton) findViewById(R.id.btn_fab);
        mFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MomentConfig.getConfig(UserProfileActivity.this).hasPublished()) {
                    UIHelper.startCameraActivity(UserProfileActivity.this);
                } else {
                    UIHelper.showToastMessage(UserProfileActivity.this, getString(R.string.info_has_published));
                }
            }
        });

        mRevealBackgroundView = (RevealBackgroundView) findViewById(R.id.vRevealBackground);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int actionBarHeight = getSupportActionBar().getHeight();
        int swipeEndTarget = actionBarHeight + (int) (64 * metrics.density);
        swipeRefreshLayout.setProgressViewOffset(false, actionBarHeight, swipeEndTarget);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary);

        mRecyclerLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        mUserProfileRecyclerView = (RecyclerView) findViewById(R.id.rv_user_profile);
        mUserProfileRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateUserInfo();
                mHandler.sendEmptyMessage(TO_REFRESH);
            }
        });

        initUserProfileView();
        initRevealBackground(savedInstanceState);

    }

    private void updateUserInfo() {
        ApiController.getUser(MomentApplication.getContext().getUser().getUuid(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String s = new String(responseBody, Charset.forName("utf-8"));
                User u = JSON.parseObject(s, User.class);
                if (!u.equals(MomentApplication.getContext().getUser())) {
                    MomentApplication.getContext().setUser(u);
                    mHandler.sendEmptyMessage(UPDATE_INFO);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void initRevealBackground(Bundle savedInstanceState) {
        mRevealBackgroundView.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
            mRevealBackgroundView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRevealBackgroundView.getViewTreeObserver().removeOnPreDrawListener(this);
                    mRevealBackgroundView.startFromLocation(startingLocation);
                    return true;
                }
            });
        } else {
            mRevealBackgroundView.setToFinishedFrame();
            mUserProfileAdapter.setLockedAnimations(true);
        }
    }

    private void initUserProfileView() {
        mUserProfileRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                mUserProfileAdapter.setLockedAnimations(true);
            }
        });
    }

    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            mUserProfileRecyclerView.setVisibility(View.VISIBLE);

            mMomentList = MomentApplication.getContext().getUserMomentList();

            mUserProfileRecyclerView.setOnScrollListener(new OnRecyclerViewScrollListener(this, null,
                    mRecyclerLayoutManager, mMomentList != null ? mMomentList.getMoments() : null) {
                @Override
                public void onScroll(RecyclerView recyclerView, int dx, int dy) {

                }

                @Override
                public void onHide() {
                    hideOnScroll();
                }

                @Override
                public void onShow() {
                    showOnScroll();
                }

                @Override
                public void onLoadMore(int pageIndex) {
                    Message message = new Message();
                    message.what = TO_LOADMORE;
                    message.obj = pageIndex;
                    mHandler.sendMessage(message);
                }
            });

            mUserProfileAdapter = new UserProfileAdapter(this, mMomentList != null ? mMomentList.getMoments() : null);
            mUserProfileAdapter.setOnInteractionListener(new UserProfileAdapter.OnUserProfileItemClickListener() {
                @Override
                public void onMomentsCountClickListener() {
                    mRecyclerLayoutManager.scrollToPosition(1);
                }

                @Override
                public void onFollowersCountClickListener() {
                    Intent intent = new Intent(UserProfileActivity.this, FollowsActivity.class);
                    intent.setAction(FollowsActivity.ACTION_FOLLOWERS);
                    startActivity(intent);
                }

                @Override
                public void onFollowingCountClickListener() {
                    Intent intent = new Intent(UserProfileActivity.this, FollowsActivity.class);
                    intent.setAction(FollowsActivity.ACTION_FOLLOWING);
                    startActivity(intent);
                }
            });
            mUserProfileRecyclerView.setAdapter(mUserProfileAdapter);

            updateUserInfo();

        } else {
            mUserProfileRecyclerView.setVisibility(View.INVISIBLE);
        }
    }

    private void hideOnScroll() {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFabButton.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        mFabButton.animate().translationY(mFabButton.getHeight() + fabBottomMargin)
                .setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showOnScroll() {
        mFabButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mHandler.sendEmptyMessage(LOAD_MOMENTS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_profile_update) {
                UpdateProfileActivity.startUpdateProfile(UserProfileActivity.this, UpdateProfileActivity.ACTION_FROM_PROFILE);
            } else if (menuItem.getItemId() == R.id.action_settings) {
                UIHelper.startSettingsActivity(UserProfileActivity.this);
            }
            return true;
        }
    };
}
