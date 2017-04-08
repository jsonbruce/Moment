package com.bukeu.moment.view.activity;

import android.os.Bundle;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.R;
import com.bukeu.moment.model.UserList;
import com.bukeu.moment.view.fragment.FollowsFragment;
import com.bukeu.moment.view.fragment.LoadingFragment;

public class FollowsActivity extends BaseActivity {

    public static final String ACTION_FOLLOWERS = "com.bukeu.moment.view.activity.FOLLOWERS_VIEW";
    public static final String ACTION_FOLLOWING = "com.bukeu.moment.view.activity.FOLLOWING_VIEW";

    private UserList mUserList;
    private boolean isFollowingView = false;

    private LoadingFragment mLoadingFragment;
    private FollowsFragment mFollowsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follows);

        if (savedInstanceState == null) {
            mLoadingFragment = new LoadingFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_follows, mLoadingFragment).commit();
        }

        if (getIntent().getAction().equals(ACTION_FOLLOWERS)) {
            mUserList = MomentApplication.getContext().getFollowersList();
            isFollowingView = false;
            setTitle(getResources().getString(R.string.title_activity_followers));

        } else if (getIntent().getAction().equals(ACTION_FOLLOWING)) {
            mUserList = MomentApplication.getContext().getFollowingList();
            isFollowingView = true;
            setTitle(getResources().getString(R.string.title_activity_following));
        }

        if (mUserList != null) {
            mFollowsFragment = FollowsFragment.newInstance(mUserList, isFollowingView);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_follows, mFollowsFragment).commit();
        }
    }

}
