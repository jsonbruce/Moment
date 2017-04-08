package com.bukeu.moment.view.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.R;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.model.Update;
import com.bukeu.moment.util.FileUtils;
import com.bukeu.moment.util.UIHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateIntentService extends IntentService {

    public static final String ACTION_CHECK = "com.bukeu.moment.view.service.action.CHECK";
    public static final String ACTION_UPDATE = "com.bukeu.moment.view.service.action.UPDATE";

    public static final String EXTRA_PARAM_UPDATE = "com.bukeu.moment.view.service.extra.APPUPDATE";
    public static final String EXTRA_BROADCAST_UPDATE = "com.bukeu.moment.view.broadcast.extra.UPDATE";

    private static final int NOTIFICATION_ID = 2;
    NotificationManager mNotificationManager;
    Notification mNotification;

    private int curVersionCode;
    private Update mUpdate;
    private File fileInstall;

    public static void startActionCheck(Context context) {
        Intent intent = new Intent(context, UpdateIntentService.class);
        intent.setAction(ACTION_CHECK);
        context.startService(intent);
    }

    public static void startActionUpdate(Context context, Update update) {
        Intent intent = new Intent(context, UpdateIntentService.class);
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(EXTRA_PARAM_UPDATE, update);
        context.startService(intent);
    }

    public UpdateIntentService() {
        super("UpdateIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK.equals(action)) {
                handleActionCheck();
            } else if (ACTION_UPDATE.equals(action)) {
                mUpdate = (Update) intent.getSerializableExtra(EXTRA_PARAM_UPDATE);
                handleActionUpdate();
            }
        }
    }

    private void handleActionCheck() {
        getCurrentVersion();
        mUpdate = ApiController.getUpdate();

        if(mUpdate != null && curVersionCode < mUpdate.getVersionCode()){
            MomentApplication.getContext().syncAppVersion(true);

            Intent intent = new Intent(MomentBroadcastReceiver.ACTION_RECEIVER_CHECK_APPVERSION);
            intent.putExtra(EXTRA_PARAM_UPDATE, mUpdate);
            sendBroadcast(intent);
        } else {
            FileUtils.deleteDirectory("/Moment/update");
            MomentApplication.getContext().syncAppVersion(false);
        }
    }

    private void handleActionUpdate() {

        if (mUpdate == null) {
            return;
        }

        Handler updateHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // 更新通知栏
                if (msg.what > 0 && msg.what < 100) {
                    mNotification.contentView.setTextViewText(
                            R.id.notificationPercent, msg.what + "%");
                    mNotification.contentView.setProgressBar(R.id.notificationProgress,
                            100, msg.what, false);
                    mNotificationManager.notify(NOTIFICATION_ID, mNotification);
                } else {
                    mNotification.contentView.setTextViewText(
                            R.id.notificationPercent, "下载完成");
                    mNotification.contentView.setProgressBar(R.id.notificationProgress,
                            100, msg.what, false);// 清除通知栏
                    mNotificationManager.cancel(NOTIFICATION_ID);
                    installApk(fileInstall);
                }
            }
        };

        //建立下载的apk文件
        fileInstall = FileUtils.createSDCardFile("/Moment/update/", "moment-" + mUpdate.getVersionName() + ".apk");

        if (fileInstall == null) {
            UIHelper.showToastMessage(this, "Check SDCard!");
            return;
        }

        if (fileInstall.exists()) {
            installApk(fileInstall);
            return;
        }

        String title = getResources().getText(R.string.app_name).toString()+"更新";
        String content= "点击更新"+getResources().getText(R.string.app_name).toString();
        String tickerText = getResources().getText(R.string.app_name).toString()+"更新！";
        int icon = R.mipmap.ic_launcher;
        mNotification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText(content)
                .setTicker(tickerText)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(false)
                .setSmallIcon(icon)
                .setContent(new RemoteViews(getPackageName(), R.layout.notification_download))
                .build();
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);

        downLoadSchedule(mUpdate.getDownloadUrl(), updateHandler, fileInstall);

    }

    /**
     * 获取当前客户端版本信息
     */
    private void getCurrentVersion(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            curVersionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
    }


    public static void downLoadSchedule(final String uri,
                                        final Handler handler, final File file) {
        // 每次读取文件的长度
        final int perLength = 4096;
        try {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(20000);
            conn.setDoInput(true);
            conn.connect();
            InputStream in = conn.getInputStream();
            // 2865412
            long length = conn.getContentLength();
            // 每次读取1k
            byte[] buffer = new byte[perLength];
            int len = -1;
            FileOutputStream out = new FileOutputStream(file);
            int temp = 0;
            while ((len = in.read(buffer)) != -1) {
                // 写入文件
                out.write(buffer, 0, len);
                // 当前进度
                int schedule = (int) ((file.length() * 100) / length);
                // 通知更新进度（10,7,4整除才通知，没必要每次都更新进度）
                if (temp != schedule
                        && (schedule % 10 == 0 || schedule % 4 == 0 || schedule % 7 == 0)) {
                    // 保证同一个数据只发了一次
                    temp = schedule;
                    handler.sendEmptyMessage(schedule);
                }
            }
            out.flush();
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 安装apk
     *
     * @param file
     */
    private void installApk(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }

}
