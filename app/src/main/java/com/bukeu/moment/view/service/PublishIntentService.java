package com.bukeu.moment.view.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.support.v4.app.NotificationCompat;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.MomentConfig;
import com.bukeu.moment.R;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.model.Feedback;
import com.bukeu.moment.model.Moment;
import com.bukeu.moment.util.UIHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 * handle `publish moment`, `post likes`
 */
public class PublishIntentService extends IntentService {

    public static final String ACTION_PUBLISH = "com.bukeu.moment.view.service.action.PUBLISH";
    public static final String ACTION_LIKES_INBATCH = "com.bukeu.moment.view.service.action.LIKES";
    public static final String ACTION_FEEDBACK = "com.bukeu.moment.view.service.action.FEEDBACK";
    public static final String EXTRA_PARAM_MOMENT = "com.bukeu.moment.view.service.extra.EXTRA_MOMENT";
    public static final String EXTRA_PARAM_PUBLISHED = "com.bukeu.moment.view.service.extra.EXTRA_PUBLISHED";
    public static final String EXTRA_PARAM_FEEDBACK = "com.bukeu.moment.view.service.extra.EXTRA_FEEDBACK";

    private static final int NOTIFICATION_ID = 1;
    NotificationManager mNotificationManager;
    Notification mNotification;

    public static void startActionPublish(Context context, Moment moment) {
        Intent intent = new Intent(context, PublishIntentService.class);
        intent.setAction(ACTION_PUBLISH);
        intent.putExtra(EXTRA_PARAM_MOMENT, moment);
        context.startService(intent);
    }

    public static void startActionLikeInBatch(Context context) {
        Intent intent = new Intent(context, PublishIntentService.class);
        intent.setAction(ACTION_LIKES_INBATCH);
        context.startService(intent);
    }

    public static void startActionFeedback(Context context, Feedback feedback) {
        Intent intent = new Intent(context, PublishIntentService.class);
        intent.setAction(ACTION_FEEDBACK);
        intent.putExtra(EXTRA_PARAM_FEEDBACK, feedback);
        context.startService(intent);
    }

    public PublishIntentService() {
        super("PublishIntentService");
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
            if (ACTION_PUBLISH.equals(action)) {
                Moment moment = (Moment) intent.getSerializableExtra(EXTRA_PARAM_MOMENT);
                if (moment != null) {
                    handleActionPublish(moment);
                }
            } else if (ACTION_LIKES_INBATCH.equals(action)) {
                handleActionLikeInBatch();
            } else if (ACTION_FEEDBACK.equals(action)) {
                Feedback feedback = (Feedback) intent.getSerializableExtra(EXTRA_PARAM_FEEDBACK);
                handleActionFeedback(feedback);
            }
        }
    }

    private void handleActionPublish(Moment m) {
        //generate notification
        String notificationText = "Publishing...";
        mNotification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("Moment")
                .setContentText(notificationText)
                .setTicker("Notification!")
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);

        Moment moment = ApiController.postMoment(m);
        if (moment != null) {
            MomentConfig.getConfig(this).setHasPublished(true);

            MomentApplication.getContext().setMoment(moment);
            MomentApplication.getContext().getMomentsCache().getMoments().add(0, moment);
        } else {
            MomentConfig.getConfig(this).setHasPublished(false);
            UIHelper.showToastMessage(this, "publish error");
        }

        //published
        mNotificationManager.cancel(NOTIFICATION_ID);
        Intent intentPublished = new Intent();
        intentPublished.setAction(ACTION_PUBLISH);
        intentPublished.addCategory(Intent.CATEGORY_DEFAULT);
        intentPublished.putExtra(EXTRA_PARAM_PUBLISHED, 1);
        sendBroadcast(intentPublished);
    }

    private void handleActionLikeInBatch() {
        Map<Long, Long> likeOperation = MomentApplication.getContext().getLikeOperation();
        List<Long> changedMomentId = new ArrayList<>();
        for (Long id : likeOperation.keySet()) {
            long op = likeOperation.get(id);
            if (1 == (op & 1)) {
                changedMomentId.add(id);
            }
        }
        if (changedMomentId.size() < 0) {
            return;
        }
        MomentApplication.getContext().clearLikeOperation();

        List<Moment> moments = ApiController.updateLikesInBatchSync(changedMomentId);
        if (moments != null) {
            MomentApplication.getContext().syncLikedMomentId();
            Intent intent = new Intent(MomentBroadcastReceiver.ACTION_RECEIVER_LIKES);
            sendBroadcast(intent);
        } else {
            UIHelper.showToastMessage(this, "no likes to post");
        }
    }

    private void handleActionFeedback(Feedback feedback) {
        PackageInfo info = MomentConfig.getConfig(this).getPackageInfo();
        if (info != null) {
            feedback.setAppversionCode(info.versionCode);
            feedback.setAppversionName(info.versionName);
        }
        feedback.setBuildVersion(android.os.Build.VERSION.RELEASE);
        feedback.setPhoneModel(android.os.Build.MODEL);
        feedback.setTelephone(MomentConfig.getConfig(this).getPhoneNumber());
        feedback.setExtra(MomentConfig.getPhoneExtraInfo());

        ApiController.postFeedback(feedback);
    }

}
