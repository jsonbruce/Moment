package com.bukeu.moment.controller.listener;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;

import com.bukeu.moment.R;
import com.bukeu.moment.model.Moment;

import java.util.List;

/*
* This class is a ScrollListener for RecyclerView that allows to show/hide
* views when list is scrolled.
* */
public abstract class OnRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    public static enum LAYOUT_MANAGER_TYPE {
        LINEAR_LAYOUT_MANAGER,
        GRID_LAYOUT_MANAGER,
        STAGGERED_GRID_LAYOUT_MANAGER
    }

    protected LAYOUT_MANAGER_TYPE layoutManagerType;

    /**
     * 最后一个的位置
     */
    private int[] lastPositions;

    /**
     * 最后一个可见的item的位置
     */
    private int lastVisibleItemPosition;

    /**
     * 当前滑动的状态
     */
    private int currentScrollState = 0;

    private static final int HIDE_THRESHOLD = 20;

    private int mScrolledDistance = 0;
    private boolean mControlsVisible = true;

    private Context mContext;
    private View mHeaderLayout;
    private List<Moment> data;

    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean isLoading = true; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.
    int firstVisibleItemPosition;
    int visibleItemCount;
    int totalItemCount;

    private int pageIndex = 1;

    private RecyclerView.LayoutManager mLayoutManager;

    private View mLoadingFooter;

    public OnRecyclerViewScrollListener(RecyclerView.LayoutManager mLayoutManager) {
        this.mLayoutManager = mLayoutManager;
    }

    public OnRecyclerViewScrollListener(Context context, View header,
                                        RecyclerView.LayoutManager mLayoutManager,
                                        List<Moment> data) {
        this.mContext = context;
        this.mHeaderLayout = header;
        this.mLayoutManager = mLayoutManager;
        this.data = data;

        mLoadingFooter = LayoutInflater.from(mContext).inflate(R.layout.custom_bottom_progressbar, null);
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        currentScrollState = newState;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        if ((visibleItemCount > 0 && currentScrollState == RecyclerView.SCROLL_STATE_IDLE &&
                (lastVisibleItemPosition) >= totalItemCount - 1)) {

//            View footer = LayoutInflater.from(mContext).inflate(R.layout.custom_bottom_progressbar, null);
//            recyclerView.addView(footer);

            onLoadMore(pageIndex++);
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        onScroll(recyclerView, dx, dy);

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManagerType == null) {
            if (layoutManager instanceof LinearLayoutManager) {
                layoutManagerType = LAYOUT_MANAGER_TYPE.LINEAR_LAYOUT_MANAGER;
            } else if (layoutManager instanceof GridLayoutManager) {
                layoutManagerType = LAYOUT_MANAGER_TYPE.GRID_LAYOUT_MANAGER;
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                layoutManagerType = LAYOUT_MANAGER_TYPE.STAGGERED_GRID_LAYOUT_MANAGER;
            } else {
                throw new RuntimeException(
                        "Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
            }
        }
        switch (layoutManagerType) {
            case LINEAR_LAYOUT_MANAGER:
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager)
                        .findLastVisibleItemPosition();
                break;
            case GRID_LAYOUT_MANAGER:
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager)
                        .findLastVisibleItemPosition();
                break;
            case STAGGERED_GRID_LAYOUT_MANAGER:
                StaggeredGridLayoutManager staggeredGridLayoutManager
                        = (StaggeredGridLayoutManager) layoutManager;
                if (lastPositions == null) {
                    lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                }
                staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                lastVisibleItemPosition = findMax(lastPositions);
                break;
        }

        visibleItemCount = recyclerView.getChildCount();
        mLayoutManager.getChildCount();
        totalItemCount = mLayoutManager.getItemCount();
        if (mLayoutManager instanceof LinearLayoutManager) {
            firstVisibleItemPosition = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
            lastVisibleItemPosition = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
        }


//        if (isLoading) {
//            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
//                isLoading = false;
//                Log.i("Moment", "Last Item Wow !");
//                pageIndex++;
//                onLoadMore(pageIndex);
//            }
//        }

        //lastVisibleItemPosition >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
        // dy>0 表示手势向上
//        if (lastVisibleItemPosition >= totalItemCount - 4 && dy > 0) {
//            if (isLoading) {
//            } else {
//                isLoading = false;
//            }
//        }
//
//        if (!isLoading && (data.size() > totalItemCount)
//                && dy > 0 && (totalItemCount < lastVisibleItemPosition + visibleThreshold)) {
//            pageIndex++;
//            onLoadMore(pageIndex);
//        }


//        if (isLoading) {
//            if (totalItemCount > previousTotal) {
//                isLoading = false;
//                previousTotal = totalItemCount;
//            }
//        }
//        if (!isLoading && (totalItemCount - visibleItemCount)
//                <= (firstVisibleItemPosition + visibleThreshold)) {
//            // End has been reached
//
//            // Do something
//            pageIndex++;
//
//            onLoadMore(pageIndex);
//
//            isLoading = true;
//        }

//        int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

//        int headerHeight = UIHelper.getToolbarHeight(mContext) + UIHelper.getSpinnerHeight(mContext);
        int toolbarHeight = ((ActionBarActivity) mContext).getSupportActionBar().getHeight();
//        int toolbarHeight2 = UIHelper.getToolbarHeight(mContext);

        int headerHeight = mHeaderLayout != null ? mHeaderLayout.getHeight() : toolbarHeight;

        if (mControlsVisible && dy > 0 && mScrolledDistance >= headerHeight) {         // onScrollUp
            onHide();
            mControlsVisible = false;
            mScrolledDistance = 0;
        } else if (!mControlsVisible && dy < 0 && mScrolledDistance > HIDE_THRESHOLD) { // onScrollSown
            onShow();
            mControlsVisible = true;
            mScrolledDistance = 0;
        }

//        if (firstVisibleItemPosition == 0) {
//            if(!mControlsVisible) {
//                onShow();
//                mControlsVisible = true;
//            }
//        } else {
//            if (mScrolledDistance > HIDE_THRESHOLD && mControlsVisible) {
//                onHide();
//                mControlsVisible = false;
//                mScrolledDistance = 0;
//            } else if (mScrolledDistance < -HIDE_THRESHOLD && !mControlsVisible) {
//                onShow();
//                mControlsVisible = true;
//                mScrolledDistance = 0;
//            }
//        }
        if ((mControlsVisible && dy > 0) || (!mControlsVisible && dy < 0)) {
            mScrolledDistance += Math.abs(dy);
        }
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public abstract void onScroll(RecyclerView recyclerView, int dx, int dy);

    public abstract void onHide();

    public abstract void onShow();

    public abstract void onLoadMore(int pageIndex);

}
