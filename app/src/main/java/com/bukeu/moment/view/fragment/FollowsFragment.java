package com.bukeu.moment.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bukeu.moment.R;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.controller.adapter.FollowsAdapter;
import com.bukeu.moment.model.UserList;
import com.bukeu.moment.util.UIHelper;
import com.bukeu.moment.view.widget.DividerItemDecoration;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

public class FollowsFragment extends Fragment implements FollowsAdapter.OnItemClickListener {
    private static final String ARG_PARAM_USERlIST = "userlist";
    private static final String ARG_PARAM_FOLLOWERS = "view_followers";

    private UserList mUserList;
    private boolean isFollowingView = false;

    RecyclerView mFollowsRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    FollowsAdapter mFollowsAdapter;

    public static FollowsFragment newInstance(UserList userList, boolean isFollowers) {
        FollowsFragment fragment = new FollowsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_USERlIST, userList);
        args.putBoolean(ARG_PARAM_FOLLOWERS, isFollowers);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserList = (UserList) getArguments().getSerializable(ARG_PARAM_USERlIST);
            isFollowingView = getArguments().getBoolean(ARG_PARAM_FOLLOWERS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_follows, container, false);

        mFollowsRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_follows);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mFollowsRecyclerView.setLayoutManager(mLayoutManager);
        mFollowsAdapter = new FollowsAdapter(getActivity(), mUserList, isFollowingView);
        mFollowsAdapter.setOnItemClickListener(this);
        mFollowsRecyclerView.setAdapter(mFollowsAdapter);
        mFollowsRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), DividerItemDecoration.VERTICAL_LIST));

        return rootView;
    }

    @Override
    public void onFollowsActionClickListener(final String uuid, final View view, final int position) {
        if (isFollowingView) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Tips")
                    .setMessage("unfollow him?")
                    .setInverseBackgroundForced(true)
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ApiController.updateFollowing(uuid, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    mFollowsAdapter.removeItem(position);
                                    UIHelper.showToastMessage(getActivity(), "follows success");
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    UIHelper.showToastMessage(getActivity(), "follows failed");
                                }
                            });
                        }
                    }).create().show();

        } else {
            ApiController.updateFollowing(uuid, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    ((ImageView) view).setBackgroundResource(android.R.color.holo_orange_dark);
                    ((ImageView) view).setImageResource(R.drawable.abc_btn_rating_star_on_mtrl_alpha);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    UIHelper.showToastMessage(getActivity(), "follows failed");
                }
            });

        }
    }
}
