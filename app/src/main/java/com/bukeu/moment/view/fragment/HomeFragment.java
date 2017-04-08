package com.bukeu.moment.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.MomentConfig;
import com.bukeu.moment.R;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.controller.adapter.MomentAdapter;
import com.bukeu.moment.controller.listener.OnRecyclerViewScrollListener;
import com.bukeu.moment.model.Moment;
import com.bukeu.moment.model.MomentList;
import com.bukeu.moment.util.UIHelper;
import com.bukeu.moment.view.service.MomentBroadcastReceiver;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnHomeFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements
        MomentAdapter.OnMomentInteractionListener, MomentDetailFragment.OnMomentDetailFragmentInteractionListener {

    public static final String TAG = "MomentFragment";

    public static final String EXTRA_PARAM_MOMENTLIST = "MomentList";

    private static final int TO_REFRESH = 0;  //下拉刷新
    private static final int TO_LOADMORE = 1; //加载更多

    private Activity mActivity;
    private OnHomeFragmentInteractionListener mListener;

    private ImageButton mFabButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mMomentRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    private MomentList mMomentList;
    private MomentAdapter mMomentAdapter;
    private MomentAdapter.MomentHolder mMomentHolderClicked;

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
                            mMomentAdapter.updateMoments(mMomentList.getMoments());
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
                            mMomentAdapter.loadOldMoments(mMomentList.getMoments());
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

    public static HomeFragment newInstance(MomentList momentList) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_PARAM_MOMENTLIST, momentList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        try {
            mListener = (OnHomeFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMomentFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMomentList = (MomentList) getArguments().getSerializable(EXTRA_PARAM_MOMENTLIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        initView(rootView);
        initData();

        return rootView;
    }

    private void initView(View rootView) {
        mFabButton = (ImageButton) rootView.findViewById(R.id.btn_fab);
        mMomentRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_moment);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int actionBarHeight = ((ActionBarActivity)mActivity).getSupportActionBar().getHeight();
        int swipeEndTarget = actionBarHeight + (int) (64 * metrics.density);
        swipeRefreshLayout.setProgressViewOffset(false, actionBarHeight, swipeEndTarget);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary);

        mLayoutManager = new LinearLayoutManager(mActivity) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        mMomentRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mMomentRecyclerView.setLayoutManager(mLayoutManager);

    }

    private void initData() {
        mMomentAdapter = new MomentAdapter(mActivity, mMomentList.getMoments());
        mMomentAdapter.setOnMomentInteractionListener(this);
        mMomentRecyclerView.setAdapter(mMomentAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHandler.sendEmptyMessage(TO_REFRESH);
            }
        });

        mFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MomentApplication.getContext().getUser() == null) {
                    UIHelper.startSigninActivity(mActivity);
                } else {
                    if (!MomentConfig.getConfig(mActivity).hasPublished()) {
                        UIHelper.startCameraActivity(mActivity);
                    } else {
                        UIHelper.showToastMessage(mActivity, mActivity.getString(R.string.info_has_published));
                    }
                }
            }
        });

        mMomentRecyclerView.setOnScrollListener(new OnRecyclerViewScrollListener(mActivity, null,
                mLayoutManager, mMomentList.getMoments()) {
            @Override
            public void onScroll(RecyclerView recyclerView, int dx, int dy) {
                if (MomentConfig.getConfig(mActivity).isNetworkConnected() &&
                        MomentApplication.getContext().getUser() != null) {
                    mActivity.sendBroadcast(new Intent(MomentBroadcastReceiver.ACTION_RECEIVER_START_LIKES));
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
                Message message = new Message();
                message.what = TO_LOADMORE;
                message.obj = pageIndex;
                mHandler.sendMessage(message);
            }
        });
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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (MomentApplication.getContext().getUser() != null) {
            mActivity.sendBroadcast(new Intent(MomentBroadcastReceiver.ACTION_RECEIVER_START_LIKES));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMomentClick(MomentAdapter.MomentHolder holder, Moment moment) {
        this.mMomentHolderClicked = holder;
        MomentDetailFragment.newInstance(moment, holder.isLiked)
                .setOnMomentDetailFragmentInteractionListener(this)
                .show(getFragmentManager(), MomentDetailFragment.TAG);
    }

    @Override
    public void onMomentDetailLikeButtonClick(boolean isLiked) {
        mMomentAdapter.syncLikeButton(mMomentHolderClicked);
    }

    public interface OnHomeFragmentInteractionListener {
        public void onHomeFragmentInteraction(Uri uri);
    }

}
