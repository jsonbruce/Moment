package com.bukeu.moment.view.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.MomentConfig;
import com.bukeu.moment.R;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.model.MomentList;
import com.bukeu.moment.model.Update;
import com.bukeu.moment.util.UIHelper;
import com.bukeu.moment.view.fragment.DrawerMenuFragment;
import com.bukeu.moment.view.fragment.FollowingMomentFragment;
import com.bukeu.moment.view.fragment.HomeFragment;
import com.bukeu.moment.view.fragment.LoadingFragment;
import com.bukeu.moment.view.fragment.NearbyFragment;
import com.bukeu.moment.view.service.PublishIntentService;
import com.bukeu.moment.view.service.UpdateIntentService;

public class MainActivity extends ActionBarActivity implements
        DrawerMenuFragment.OnDrawerMenuFragmentInteractionListener,
        HomeFragment.OnHomeFragmentInteractionListener,
        FollowingMomentFragment.OnFollowingMomentFragmentInteractionListener,
        NearbyFragment.OnNearbyFragmentInteractionListener{

    public static final String ACTION_SHOW_LOADING_ITEM = "action_show_loading_item";
    public static final String ACTION_FROM_SIGNIN = "action_from_signin";

    private MainBroadcastReceiver mMainBroadcastReceiver;

    protected Toolbar mToolbar;
    private Menu mMenu;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private FrameLayout mDrawerContentLayout;
    private FrameLayout mDrawerContentFrame;
    private FrameLayout mDrawerMenuFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_drawer_menu, DrawerMenuFragment.newInstance(), DrawerMenuFragment.TAG)
                    .commit();

            setDrawerContentFragment(
                    HomeFragment.newInstance(MomentApplication.getContext().getMomentsCache()),
                    HomeFragment.TAG);
        }

    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerContentLayout = (FrameLayout) findViewById(R.id.layout_drawer_content);
        mDrawerContentFrame = (FrameLayout) findViewById(R.id.frame_drawer_content);
        mDrawerMenuFrame = (FrameLayout) findViewById(R.id.frame_drawer_menu);

        int screenWidth = UIHelper.getScreenWidth(this);
        DrawerLayout.LayoutParams layoutParams = new DrawerLayout.LayoutParams(screenWidth * 3 / 4,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.LEFT);
        mDrawerLayout.updateViewLayout(mDrawerMenuFrame, layoutParams);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            @Override
            public boolean onOptionsItemSelected(MenuItem item) {
                return super.onOptionsItemSelected(item);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    private void initData() {
        if (MomentConfig.getConfig(this).isNetworkConnected()) {
            UpdateIntentService.startActionCheck(this);
        }

        //register BroadcastReceiver
        mMainBroadcastReceiver = new MainBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(PublishIntentService.ACTION_PUBLISH);
        intentFilter.addAction(UpdateIntentService.ACTION_CHECK);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mMainBroadcastReceiver, intentFilter);

    }

    private void setDrawerContentFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_drawer_content, fragment, tag);
        transaction.commit();

        mDrawerLayout.closeDrawer(mDrawerMenuFrame);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (ACTION_FROM_SIGNIN.equals(intent.getAction())) {
            ((DrawerMenuFragment)getSupportFragmentManager().findFragmentByTag(DrawerMenuFragment.TAG))
                    .updateDrawerMenuHeader();
        } else if (ACTION_SHOW_LOADING_ITEM.equals(intent.getAction())) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerMenuFrame);
        menu.findItem(R.id.action_search).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_search:
                UIHelper.startSearchActivity(MainActivity.this);
                return true;
            case R.id.action_settings:
                UIHelper.startSettingsActivity(MainActivity.this);
                break;
            case R.id.action_help:
                TextView content = (TextView) getLayoutInflater().inflate(R.layout.about_view, null);
                content.setMovementMethod(LinkMovementMethod.getInstance());
                content.setText(Html.fromHtml(getString(R.string.about_body)));
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.about)
                        .setView(content)
                        .setInverseBackgroundForced(true)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MomentApplication.getContext().saveMomentsCache();

        if (mMainBroadcastReceiver != null) {
            unregisterReceiver(mMainBroadcastReceiver);
        }
    }


    @Override
    public void onHomeMenuClick(View view) {
        resetToolbar(getResources().getColor(R.color.primary), 0, true);

        Fragment homeFragment = getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
        if (homeFragment != null && homeFragment.isVisible()) {
            mDrawerLayout.closeDrawer(mDrawerMenuFrame);
        } else {
            setDrawerContentFragment(LoadingFragment.newInstance(null, false), LoadingFragment.TAG);
            ApiController.getMoments(0, new Response.Listener<MomentList>() {
                @Override
                public void onResponse(MomentList response) {
                    setDrawerContentFragment(HomeFragment.newInstance(response), HomeFragment.TAG);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }
    }

    @Override
    public void onGroupClick(View view) {
        resetToolbar(getResources().getColor(R.color.primary), 0, true);

        setDrawerContentFragment(LoadingFragment.newInstance(null, false), LoadingFragment.TAG);

        ApiController.getFollowingMoments(0, new Response.Listener<MomentList>() {
            @Override
            public void onResponse(MomentList response) {
                setDrawerContentFragment(FollowingMomentFragment.newInstance(response), FollowingMomentFragment.TAG);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Moment", error.getLocalizedMessage()+"");
            }
        });

        mDrawerLayout.closeDrawer(mDrawerMenuFrame);
    }

    @Override
    public void onNearbyClick(View view) {
        resetToolbar(Color.TRANSPARENT, UIHelper.getToolbarHeight(this), false);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(NearbyFragment.TAG);
        if (fragment != null && fragment.isVisible()) {
            mDrawerLayout.closeDrawer(mDrawerMenuFrame);
        } else {
            setDrawerContentFragment(NearbyFragment.newInstance(), NearbyFragment.TAG);
        }
    }

    private void resetToolbar(int color, int topMargin, boolean showSearch) {
        mToolbar.setBackgroundColor(color);
        if (mMenu != null) {
            for (int i = 0; i < mMenu.size(); i++){
                mMenu.getItem(i).setVisible(showSearch);
                mMenu.getItem(i).setEnabled(showSearch);
            }
        }
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        );
        layoutParams.setMargins(0,topMargin,0,0);
        mDrawerContentLayout.updateViewLayout(mDrawerContentFrame, layoutParams);
    }

    @Override
    public void onHomeFragmentInteraction(Uri uri) {

    }

    @Override
    public void OnFollowingMomentFragmentInteraction(Uri uri) {
    }

    @Override
    public void onNearbyFragmentInteraction() {

    }

    public class MainBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PublishIntentService.ACTION_PUBLISH)) {
                int result = intent.getIntExtra(PublishIntentService.EXTRA_PARAM_PUBLISHED, 0);
                if (result == 1) {
                    Toast.makeText(MainActivity.this, "Moment Published", Toast.LENGTH_SHORT).show();
                }
            } else if (intent.getAction().equals(UpdateIntentService.ACTION_CHECK)) {
                final Update update = (Update) intent.getSerializableExtra(UpdateIntentService.EXTRA_PARAM_UPDATE);
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle(context.getString(R.string.info_app_update))
                        .setMessage(update.getUpdateLog())
                        .setPositiveButton(context.getString(R.string.info_update), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                UpdateIntentService.startActionUpdate(MainActivity.this, update);
                            }
                        })
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alert.create().show();
            }
        }
    }

}
