package com.bukeu.moment.view.service;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.WindowManager;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.R;
import com.bukeu.moment.model.Update;

public class MomentBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_RECEIVER_START_MOMENTSERVICE= "com.bukeu.moment.action.START_MOMENT_SERVICE";
    public static final String ACTION_RECEIVER_START_LIKES = "com.bukeu.moment.action.START_MOMENT_LIKES";

    public static final String ACTION_RECEIVER_SYNCMOMENTSCACHE = "com.bukeu.moment.action.MOMENT_SYNCMOMENTSCACHE";
    public static final String ACTION_RECEIVER_SYNCFOLLOWS= "com.bukeu.moment.action.MOMENT_SYNCFOLLOWS";
    public static final String ACTION_RECEIVER_LIKES = "com.bukeu.moment.action.MOMENT_LIKES";

    public static final String ACTION_RECEIVER_CHECK_APPVERSION = "com.bukeu.moment.action.CHECK_APPVERSION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (intent.getAction().equals(ACTION_RECEIVER_START_MOMENTSERVICE)) {
                context.startService(new Intent(context, MomentService.class));
//                UIHelper.showToastMessage(context, "#Broadcast# MomentService Start");
            } else if (intent.getAction().equals(ACTION_RECEIVER_START_LIKES)) {
                if (MomentApplication.getContext().getLikeOperation().size() > 0) {
                    PublishIntentService.startActionLikeInBatch(context);
//                    UIHelper.showToastMessage(context, "#Broadcast# AddLikesInBatch Start");
                }
            } else if (intent.getAction().equals(ACTION_RECEIVER_SYNCMOMENTSCACHE)) {
//                UIHelper.showToastMessage(context, "#Broadcast# SyncMomentCache Finish");
            } else if (intent.getAction().equals(ACTION_RECEIVER_SYNCFOLLOWS)) {
//                UIHelper.showToastMessage(context, "#Broadcast# SyncFollows Finish");
            } else if (intent.getAction().equals(ACTION_RECEIVER_LIKES)) {
//                UIHelper.showToastMessage(context, "#Broadcast# AddLikesInBatch Finish");
            } else if (intent.getAction().equals(ACTION_RECEIVER_CHECK_APPVERSION)) {
                showUpdateDialog(context, intent);
            }
        }
    }


    private void showUpdateDialog(final Context context, Intent intent) {
        final Update update = (Update) intent.getSerializableExtra(UpdateIntentService.EXTRA_PARAM_UPDATE);
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(context.getString(R.string.info_app_update))
                .setMessage(update.getUpdateLog())
                .setPositiveButton(context.getString(R.string.info_update), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        UpdateIntentService.startActionUpdate(context, update);
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = alert.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

}
