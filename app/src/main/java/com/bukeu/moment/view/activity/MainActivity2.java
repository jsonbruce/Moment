package com.bukeu.moment.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.MomentConfig;
import com.bukeu.moment.R;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.controller.adapter.MomentCardAdapter;
import com.bukeu.moment.controller.listener.OnRecyclerViewScrollListener;
import com.bukeu.moment.model.MomentList;
import com.bukeu.moment.model.Update;
import com.bukeu.moment.util.StringUtils;
import com.bukeu.moment.util.UIHelper;
import com.bukeu.moment.view.service.MomentBroadcastReceiver;
import com.bukeu.moment.view.service.PublishIntentService;
import com.bukeu.moment.view.service.UpdateIntentService;

public class MainActivity2 extends MainHeaderActivity {

    private static final int TO_REFRESH = 0;  //下拉刷新
    private static final int TO_LOADMORE = 1; //加载更多

    public static final String ACTION_SHOW_LOADING_ITEM = "action_show_loading_item";
    public static final String ACTION_FROM_SIGNIN = "action_from_signin";

    private static final int ANIM_DURATION_HEADER = 300;
    private static final int ANIM_DURATION_FAB = 400;
    private boolean pendingIntroAnimation;

    private MomentApplication momentApplication;
    private MainBroadcastReceiver mMainBroadcastReceiver;

    private ImageButton mFabButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mMomentRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private MomentCardAdapter mMomentCardAdapter;
    private int pageIndex = 0;

    private MomentList mMomentList;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TO_REFRESH:
                    ApiController.getMoments(0, new Response.Listener<MomentList>() {
                        @Override
                        public void onResponse(MomentList response) {
                            mMomentList = response;
                            mMomentCardAdapter.updateMoments(mMomentList.getMoments());
                            swipeRefreshLayout.setRefreshing(false);//停止刷新
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                    break;
                case TO_LOADMORE:
                    ApiController.getMoments(mMomentList.getPage()+1, new Response.Listener<MomentList>() {
                        @Override
                        public void onResponse(MomentList response) {
                            mMomentList = response;
                            mMomentCardAdapter.updateMoments(mMomentList.getMoments());
                            swipeRefreshLayout.setRefreshing(false);//停止刷新
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (MomentConfig.isApi21()) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        initView();
        initData();

        if (savedInstanceState == null) {
            pendingIntroAnimation = true;
        } else {
            mMomentCardAdapter.updateMoments(mMomentList.getMoments(), false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mHandler.sendEmptyMessage(TO_REFRESH);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (momentApplication.getUser() != null) {
            sendBroadcast(new Intent(MomentBroadcastReceiver.ACTION_RECEIVER_START_LIKES));
        }
    }

    public void initView() {
        setContentView(R.layout.activity_main2);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mFabButton = (ImageButton) findViewById(R.id.btn_fab);
        mMomentRecyclerView = (RecyclerView) findViewById(R.id.rv_moment);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int headerHeight = mHeaderLayout.getHeight();
        int swipeEndTarget = headerHeight + (int) (64 * metrics.density);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary);
        swipeRefreshLayout.setProgressViewOffset(false, headerHeight, swipeEndTarget + headerHeight);

        mLinearLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        mMomentRecyclerView.setItemAnimator(new DefaultItemAnimator());
        int paddingTop = UIHelper.getToolbarHeight(this) + UIHelper.getSpinnerHeight(this);
        mMomentRecyclerView.setPadding(mMomentRecyclerView.getPaddingLeft(), paddingTop,
                mMomentRecyclerView.getPaddingRight(), mMomentRecyclerView.getPaddingBottom());
        mMomentRecyclerView.setLayoutManager(mLinearLayoutManager);

    }

    public void initData() {
        momentApplication = MomentApplication.getContext();
        if (MomentConfig.getConfig(this).isNetworkConnected()) {
            UpdateIntentService.startActionCheck(this);
        }

        //register BroadcastReceiver
        mMainBroadcastReceiver = new MainBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(PublishIntentService.ACTION_PUBLISH);
        intentFilter.addAction(UpdateIntentService.ACTION_CHECK);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mMainBroadcastReceiver, intentFilter);

        mMomentCardAdapter = new MomentCardAdapter(this, mMomentList.getMoments());
        mMomentRecyclerView.setAdapter(mMomentCardAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHandler.sendEmptyMessage(TO_REFRESH);
            }
        });
        mMomentRecyclerView.setOnScrollListener(new OnRecyclerViewScrollListener(MainActivity2.this,
                mHeaderLayout, mLinearLayoutManager, mMomentList.getMoments()) {
            @Override
            public void onScroll(RecyclerView recyclerView, int dx, int dy) {
                if (momentApplication.getUser() != null) {
                    sendBroadcast(new Intent(MomentBroadcastReceiver.ACTION_RECEIVER_START_LIKES));
                }
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
                mHandler.sendEmptyMessage(TO_LOADMORE);
            }

        });

        mFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (momentApplication.getUser() == null) {
                    UIHelper.startSigninActivity(MainActivity2.this);
                } else {
                    UIHelper.startCameraActivity(MainActivity2.this);
                }
            }
        });

        mHeaderAvater.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (momentApplication.getUser() == null) {
                    UIHelper.startSigninActivity(MainActivity2.this);
                } else {
                    int[] startingLocation = new int[2];
                    v.getLocationOnScreen(startingLocation);
                    startingLocation[0] += v.getWidth() / 2;
                    UserProfileActivity.startUserProfileFromLocation(startingLocation, MainActivity2.this);
                    overridePendingTransition(0, 0);
                }
            }
        });

    }

    private void hideOnScroll() {
        mHeaderLayout.animate().translationY(-mHeaderLayout.getHeight())
                .setInterpolator(new AccelerateInterpolator(2.5f));

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFabButton.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        mFabButton.animate().translationY(mFabButton.getHeight() + fabBottomMargin)
                .setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showOnScroll() {
        mHeaderLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
//        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
//        mSpinner.animate().translationY(0).setInterpolator(new DecelerateInterpolator(1));
        mFabButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (ACTION_SHOW_LOADING_ITEM.equals(intent.getAction())) {
            mMomentRecyclerView.smoothScrollToPosition(0);
            mMomentCardAdapter.addMoment(momentApplication.getMoment());
        } else if (ACTION_SHOW_LOADING_ITEM.equals(intent.getAction())) {
            loadHeaderUserInfo();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (pendingIntroAnimation) {
            pendingIntroAnimation = false;
            startHeaderAnimation();
        }
        return true;
    }

    private void startHeaderAnimation() {
        mFabButton.setTranslationY(2 * getResources().getDimensionPixelOffset(R.dimen.btn_fab_size));

        int toolbarHeight = StringUtils.dpToPx(UIHelper.getToolbarHeight(this));
        int spinnerHeight = UIHelper.getSpinnerHeight(this);
        int headerHeight = toolbarHeight + spinnerHeight;
        mToolbar.setTranslationY(-toolbarHeight);
        mSpinner.setTranslationY(-headerHeight);

        mToolbar.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_HEADER)
                .setStartDelay(300);
        mSpinner.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_HEADER)
                .setStartDelay(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startContentAnimation();
                    }
                })
                .start();
    }

    private void startContentAnimation() {
        mFabButton.animate()
                .translationY(0)
                .setInterpolator(new OvershootInterpolator(1.f))
                .setStartDelay(200)
                .setDuration(ANIM_DURATION_FAB)
                .start();

        mMomentList = momentApplication.getMomentsCache();
//        mMomentCardAdapter.updateMoments(mMoments);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        momentApplication.saveMomentsCache();

        if (mMainBroadcastReceiver != null) {
            unregisterReceiver(mMainBroadcastReceiver);
        }
    }

    public class MainBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PublishIntentService.ACTION_PUBLISH)) {
                int result = intent.getIntExtra(PublishIntentService.EXTRA_PARAM_PUBLISHED, 0);
                if (result == 1) {
                    Toast.makeText(MainActivity2.this, "Moment Published", Toast.LENGTH_SHORT).show();
                }
            } else if (intent.getAction().equals(UpdateIntentService.ACTION_CHECK)) {
                final Update update = (Update) intent.getSerializableExtra(UpdateIntentService.EXTRA_PARAM_UPDATE);
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity2.this);
                alert.setTitle("软件升级")
                        .setMessage(update.getUpdateLog())
                        .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                UpdateIntentService.startActionUpdate(MainActivity2.this, update);
                            }
                        })
                        .setNegativeButton("取消",new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alert.create().show();
            }
        }
    }
}
