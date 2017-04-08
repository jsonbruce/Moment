package com.bukeu.moment.view.fragment;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bukeu.moment.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoadingFragment extends Fragment {

    public static final String TAG = "LoadingFragment";

    private static final String ARG_PARAM_MESSAGE = "message";
    private static final String ARG_PARAM_SHOWNOTFOUND = "not_found";

    private String mLoadingMessage;
    private Boolean mShowNotFound = false;

    private ProgressBar mProgressBar;
    private TextView mMessageView;

    private OnFragmentInteractionListener mListener;

    public static LoadingFragment newInstance(String message, boolean showNotFound) {
        LoadingFragment loadingFragment = new LoadingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_MESSAGE, message);
        args.putBoolean(ARG_PARAM_SHOWNOTFOUND, showNotFound);
        loadingFragment.setArguments(args);
        return loadingFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLoadingMessage = getArguments().getString(ARG_PARAM_MESSAGE);
            mShowNotFound = getArguments().getBoolean(ARG_PARAM_SHOWNOTFOUND);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_loading, container, false);
        rootView.setTag(TAG);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.pr_search_progress);
        mMessageView = (TextView) rootView.findViewById(R.id.tv_search_progress_message);

        if (mShowNotFound) {
            setProgressBarVisible(false);
        }
        if (mLoadingMessage != null) {
            mMessageView.setText(mLoadingMessage);
        }

        return rootView;
    }

    public void setProgressBarVisible(boolean visible) {
        this.mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}
