package com.bukeu.moment.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.bukeu.moment.R;
import com.bukeu.moment.view.activity.CameraActivity;
import com.bukeu.moment.view.activity.MainActivity;
import com.bukeu.moment.view.activity.SearchActivity;
import com.bukeu.moment.view.activity.SettingsActivity;
import com.bukeu.moment.view.activity.SigninActivity;

/**
 * Created by Max on 2015/3/28.
 */
public class UIHelper {

    private static int screenWidth = 0;
    private static int screenHeight = 0;

    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }

    public static int getSpinnerHeight(Context context) {
        return (int) context.getResources().getDimension(R.dimen.spinnerHeight);
    }

    public static int getScreenWidth(Context c) {
        if (screenWidth == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
        }

        return screenWidth;
    }

    public static int getScreenHeight(Context c) {
        if (screenHeight == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
        }

        return screenHeight;
    }

    public static void showToastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void startMainActivity(Context context, String action) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(action);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_top);
    }

    public static void startMainActivity(Context context) {
        startMainActivity(context, null);
    }

    public static void startSigninActivity(Context context) {
        Intent intent = new Intent(context, SigninActivity.class);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_top);
    }

    public static void startCameraActivity(Context context) {
        Intent intent = new Intent(context, CameraActivity.class);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_top);
    }

    public static void startSearchActivity(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        context.startActivity(intent);
//        ((Activity)context).overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_top);
    }

    public static void startSettingsActivity(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
//        ((Activity)context).overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

}
