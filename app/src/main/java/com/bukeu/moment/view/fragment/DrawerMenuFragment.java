package com.bukeu.moment.view.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.R;
import com.bukeu.moment.controller.adapter.DrawerMenuAdapter;
import com.bukeu.moment.util.UIHelper;
import com.bukeu.moment.view.activity.UserProfileActivity;
import com.bukeu.moment.view.widget.HorizontalDividerItemDecoration;

public class DrawerMenuFragment extends Fragment implements DrawerMenuAdapter.OnDrawerMenuClickListener {

    public static final String TAG = "DrawerMenuFragment";

    private Activity mActivity;
    private LinearLayout mDrawerMenuLayout;
    private RecyclerView mDrawerMenu;
    private RecyclerView.LayoutManager mLayoutManager;
    private DrawerMenuAdapter mDrawerMenuAdapter;

    private OnDrawerMenuFragmentInteractionListener mListener;

    public static DrawerMenuFragment newInstance() {
        DrawerMenuFragment fragment = new DrawerMenuFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
        try {
            mListener = (OnDrawerMenuFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDrawerMenuFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_drawer_menu, container, false);

        mDrawerMenuLayout = (LinearLayout) rootView.findViewById(R.id.layout_drawer_menu);
        mDrawerMenu = (RecyclerView) rootView.findViewById(R.id.rv_drawer_menu);

        mLayoutManager = new LinearLayoutManager(mActivity);
        mDrawerMenu.setHasFixedSize(true);
        mDrawerMenu.setLayoutManager(mLayoutManager);
        mDrawerMenuAdapter = new DrawerMenuAdapter(mActivity, this);
        mDrawerMenu.setAdapter(mDrawerMenuAdapter);
        mDrawerMenu.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mActivity)
                .paintProvider(mDrawerMenuAdapter).visibilityProvider(mDrawerMenuAdapter).build());

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * Update the header after signin/up
     */
    public void updateDrawerMenuHeader() {
        mDrawerMenuAdapter.updateDrawerMenuHeader();
    }

    @Override
    public void onDrawerMenuHeaderClick(View view) {
        if (MomentApplication.getContext().getUser() == null) {
            UIHelper.startSigninActivity(mActivity);
        } else {
            int[] startingLocation = new int[2];
            view.getLocationOnScreen(startingLocation);
            startingLocation[0] += view.getWidth() / 2;
            UserProfileActivity.startUserProfileFromLocation(startingLocation, mActivity);
            mActivity.overridePendingTransition(0, 0);
        }
    }

    private void setCurrentMenuItem(View view) {
        View homeItem = mDrawerMenu.findViewWithTag(mActivity.getResources().getString(R.string.drawer_menu_home));
        View groupItem = mDrawerMenu.findViewWithTag(mActivity.getResources().getString(R.string.drawer_menu_group));
        View nearbyItem = mDrawerMenu.findViewWithTag(mActivity.getResources().getString(R.string.drawer_menu_nearby));

        if (view.getTag().equals(mActivity.getResources().getString(R.string.drawer_menu_home))) {
            homeItem.setBackgroundResource(R.color.btn_context_menu_pressed);
            groupItem.setBackgroundResource(R.color.btn_context_menu_normal);
            nearbyItem.setBackgroundResource(R.color.btn_context_menu_normal);

        } else if (view.getTag().equals(mActivity.getResources().getString(R.string.drawer_menu_group))) {
            homeItem.setBackgroundResource(R.color.btn_context_menu_normal);
            groupItem.setBackgroundResource(R.color.btn_context_menu_pressed);
            nearbyItem.setBackgroundResource(R.color.btn_context_menu_normal);

        } else if (view.getTag().equals(mActivity.getResources().getString(R.string.drawer_menu_nearby))) {
            homeItem.setBackgroundResource(R.color.btn_context_menu_normal);
            groupItem.setBackgroundResource(R.color.btn_context_menu_normal);
            nearbyItem.setBackgroundResource(R.color.btn_context_menu_pressed);
        }
    }

    @Override
    public void onDrawerMenuItemClick(View view, int position) {
        setCurrentMenuItem(view);

        if (view.getTag().equals(mActivity.getResources().getString(R.string.drawer_menu_home))) {
            if (mListener != null) {
                mListener.onHomeMenuClick(view);
            }

        } else if (view.getTag().equals(mActivity.getResources().getString(R.string.drawer_menu_group))) {
            if (mListener != null) {
                mListener.onGroupClick(view);
            }

        } else if (view.getTag().equals(mActivity.getResources().getString(R.string.drawer_menu_nearby))) {
            if (mListener != null) {
                mListener.onNearbyClick(view);
            }
        } else if (view.getTag().equals(mActivity.getResources().getString(R.string.drawer_menu_settings))) {
            UIHelper.startSettingsActivity(mActivity);

        } else if (view.getTag().equals(mActivity.getResources().getString(R.string.drawer_menu_help))) {
            TextView content = (TextView) mActivity.getLayoutInflater().inflate(R.layout.about_view, null);
            content.setMovementMethod(LinkMovementMethod.getInstance());
            content.setText(Html.fromHtml(getString(R.string.about_body)));
            new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.about)
                    .setView(content)
                    .setInverseBackgroundForced(true)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }
    }

    public interface OnDrawerMenuFragmentInteractionListener {
        void onHomeMenuClick(View view);

        void onGroupClick(View view);

        void onNearbyClick(View view);
    }
}
