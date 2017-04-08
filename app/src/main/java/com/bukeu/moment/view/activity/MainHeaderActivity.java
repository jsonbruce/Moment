package com.bukeu.moment.view.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.R;
import com.bukeu.moment.controller.adapter.SpinnerAdapter;
import com.bukeu.moment.util.UIHelper;
import com.bumptech.glide.Glide;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Max on 2015/3/28.
 */
public class MainHeaderActivity extends ActionBarActivity {

    protected LinearLayout mHeaderLayout;
    protected Toolbar mToolbar;
    protected Spinner mSpinner;
    protected ImageView mHeaderAvater;
    protected TextView mHeaderNickname;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        initHeader();
    }

    private void initHeader() {
        mHeaderLayout = (LinearLayout) findViewById(R.id.layout_header);
        mHeaderAvater = (ImageView) findViewById(R.id.iv_header_avater);
        mHeaderNickname = (TextView) findViewById(R.id.tv_header_nickname);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle("匿名用户");
            mToolbar.setClipChildren(false);
            mToolbar.removeAllViews();
            mToolbar.addView(mHeaderAvater);
            mToolbar.addView(mHeaderNickname);
            setSupportActionBar(mToolbar);
            mToolbar.showOverflowMenu();
            loadHeaderUserInfo();
            mToolbar.setOnMenuItemClickListener(onMenuItemClickListener);
        }

        mSpinner = (Spinner) findViewById(R.id.header_spinner);
        List<String> moments = Arrays.asList(getResources().getStringArray(R.array.spinner_choices));
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this, moments);
        mSpinner.setAdapter(spinnerAdapter);
    }

    protected void loadHeaderUserInfo() {
        if (MomentApplication.getContext().getUser() != null) {
            Glide.with(this)
                    .load(MomentApplication.getContext().getUser().getAvater())
                    .placeholder(R.drawable.default_avater)
                    .into(mHeaderAvater);
            mHeaderNickname.setText(MomentApplication.getContext().getUser().getNickname());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_search) {
                UIHelper.startSearchActivity(MainHeaderActivity.this);

            } else if (menuItem.getItemId() == R.id.action_settings) {
                UIHelper.startSettingsActivity(MainHeaderActivity.this);

            } else if (menuItem.getItemId() == R.id.action_help) {
                TextView content = (TextView) getLayoutInflater().inflate(R.layout.about_view, null);
                content.setMovementMethod(LinkMovementMethod.getInstance());
                content.setText(Html.fromHtml(getString(R.string.about_body)));
                new AlertDialog.Builder(MainHeaderActivity.this)
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
            return true;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
