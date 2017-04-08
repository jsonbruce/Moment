package com.bukeu.moment;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Max on 2015/3/28.
 */
public class MomentConfig {

    private Context mContext;
    private static MomentConfig momentConfig;

    /* Local Paths */
    public static final String PATH_BASE = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath()
            + "/Moment";
    public static final String PATH_TMP = PATH_BASE + "/tmp";
    public static final String PATH_TMP_IMG = PATH_TMP + "/img/";
    public static final String PATH_CACHE = PATH_BASE + "/cache";

    /* SharedPreferences */
    public static final String PREF_USER = "user";
    public static final String PREF_KEY_USER_SIGNED = "user_signed";
    public static final String PREF_KEY_USER_FOLLOWING = "user_following";
    public static final String PREF_KEY_USER_FOLLOWERS = "user_followers";
    public static final String PREF_KEY_HASPUBLISHED = "today_has_published";
    public static final String PREF_KEY_USER_LIKED_MOMENTID = "user_liked_momentid";
    public static final String PREF_KEY_USER_MOMENTS = "user_moments";
    public static final String PREF_MOMENT = "moment";
    public static final String PREF_KEY_MOMENTS_CACHE = "moments_cache";
    public static final String PREF_APP = "app";
    public static final String PREF_KEY_APP_UPDATE = "update";

    public synchronized static MomentConfig getConfig(Context context) {
        if (momentConfig == null) {
            momentConfig = new MomentConfig();
            momentConfig.mContext = context;
        }
        return momentConfig;
    }

    public static void initLocalStorage() {
        File basePath = new File(PATH_BASE);
        File cachePath = new File(PATH_CACHE);
        File tmpPath = new File(PATH_TMP);
        File imgPath = new File(PATH_TMP_IMG);

        if (!basePath.exists()) {
            if (basePath.mkdirs()) {
                System.out.println("/Moment");
            }else {
                System.out.println("Fail to create " + basePath.getPath());
            }
        }
        if (!cachePath.exists()) {
            if (cachePath.mkdir()) {
                System.out.println("/Moment/cache");
            }else {
                System.out.println("Fail to create " + cachePath.getAbsolutePath());
            }
        }
        if (!tmpPath.exists()) {
            if (tmpPath.mkdir()) {
                System.out.println("/Moment/tmp");
            }else {
                System.out.println("Fail to create " + tmpPath.getAbsolutePath());
            }
        }
        if (!imgPath.exists()) {
            if (imgPath.mkdir()) {
                System.out.println("/Moment/tmp/img");
            }else {
                System.out.println("Fail to create " + imgPath.getAbsolutePath());
            }
        }
    }

    public SharedPreferences getSharedPreferences(String name){
        return mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public SharedPreferences.Editor getSharedPreferencesEditor(String key){
        return mContext.getSharedPreferences(key, Context.MODE_PRIVATE).edit();
    }


    public void setHasPublished(boolean hasPublished) {
        getSharedPreferencesEditor(PREF_USER).putBoolean(PREF_KEY_HASPUBLISHED, hasPublished).apply();
    }

    public boolean hasPublished() {
        return getSharedPreferences(PREF_USER).getBoolean(PREF_KEY_HASPUBLISHED, false);
    }

    private String getToday() {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat ("yyyy-MM-dd");
        String ctime = formatter.format(new Date());
        return ctime;
    }

    /**
     * 检测网络是否可用
     *
     * @return true表示网络可用
     */
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    /**
     * 获取手机号码
     * @return
     */
    public String getPhoneNumber() {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceid = tm.getDeviceId();// 获取智能设备唯一编号
        String te1 = tm.getLine1Number();// 获取本机号码
        String imei = tm.getSimSerialNumber();// 获得SIM卡的序号
        String imsi = tm.getSubscriberId();// 得到用户Id

        if (te1 != null) {
            return te1;
        }else {
            return "";
        }
    }

    public static boolean isApi21() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
            info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }

    /**
     * get process name with pid
     *
     * @param context
     * @param pid
     * @return
     */
    @Nullable
    public static String getProcessName(Context context, int pid){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps != null && !runningApps.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
                if (procInfo.pid == pid) {
                    return procInfo.processName;
                }
            }
        }
        return null;
    }

    /**
     * get current process name
     *
     * @param context
     * @return
     */
    @Nullable
    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {

                return appProcess.processName;
            }
        }
        return null;
    }

    public static String getPhoneExtraInfo() {
        //BOARD 主板
        String phoneInfo = "BOARD: " + android.os.Build.BOARD;
        phoneInfo += ", BOOTLOADER: " + android.os.Build.BOOTLOADER;
//BRAND 运营商
        phoneInfo += ", BRAND: " + android.os.Build.BRAND;
        phoneInfo += ", CPU_ABI: " + android.os.Build.CPU_ABI;
        phoneInfo += ", CPU_ABI2: " + android.os.Build.CPU_ABI2;
//DEVICE 驱动
        phoneInfo += ", DEVICE: " + android.os.Build.DEVICE;
//DISPLAY 显示
        phoneInfo += ", DISPLAY: " + android.os.Build.DISPLAY;
//指纹
        phoneInfo += ", FINGERPRINT: " + android.os.Build.FINGERPRINT;
//HARDWARE 硬件
        phoneInfo += ", HARDWARE: " + android.os.Build.HARDWARE;
        phoneInfo += ", HOST: " + android.os.Build.HOST;
        phoneInfo += ", ID: " + android.os.Build.ID;
//MANUFACTURER 生产厂家
        phoneInfo += ", MANUFACTURER: " + android.os.Build.MANUFACTURER;
//MODEL 机型
        phoneInfo += ", MODEL: " + android.os.Build.MODEL;
        phoneInfo += ", PRODUCT: " + android.os.Build.PRODUCT;
        phoneInfo += ", RADIO: " + android.os.Build.RADIO;
        phoneInfo += ", RADITAGSO: " + android.os.Build.TAGS;
        phoneInfo += ", TIME: " + android.os.Build.TIME;
        phoneInfo += ", TYPE: " + android.os.Build.TYPE;
        phoneInfo += ", USER: " + android.os.Build.USER;
//VERSION.RELEASE 固件版本
        phoneInfo += ", VERSION.RELEASE: " + android.os.Build.VERSION.RELEASE;
        phoneInfo += ", VERSION.CODENAME: " + android.os.Build.VERSION.CODENAME;
//VERSION.INCREMENTAL 基带版本
        phoneInfo += ", VERSION.INCREMENTAL: " + android.os.Build.VERSION.INCREMENTAL;
//VERSION.SDK SDK版本
        phoneInfo += ", VERSION.SDK: " + android.os.Build.VERSION.SDK;
        phoneInfo += ", VERSION.SDK_INT: " + android.os.Build.VERSION.SDK_INT;

        return phoneInfo;
    }

}
