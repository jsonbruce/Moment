package com.bukeu.moment.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bukeu.moment.R;
import com.bukeu.moment.controller.adapter.TimelineAdapter;
import com.bukeu.moment.model.MomentList;

public class TimelineFragment extends Fragment {

    private static final String TAG = "TimelineFragment";

    private static final String ARG_PARAM1 = "param1";

    private MomentList mMomentList;

    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;
    private TimelineAdapter mTimelineAdapter;

    public static TimelineFragment newInstance(MomentList momentList) {
        TimelineFragment fragment = new TimelineFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, momentList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMomentList = (MomentList) getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_timeline, container, false);
        rootView.setTag(TAG);

        mRecyclerView =(RecyclerView) rootView.findViewById(R.id.rv_timeline);
        mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mTimelineAdapter = new TimelineAdapter(getActivity(), mMomentList);
        mRecyclerView.setAdapter(mTimelineAdapter);

        return rootView;
    }

}
