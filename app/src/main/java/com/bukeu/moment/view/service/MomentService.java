package com.bukeu.moment.view.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.MomentConfig;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.model.MomentList;

public class MomentService extends Service {

    private Handler mHandler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                List<Moment> moments = ApiController.getMoments();
//                MomentApplication.getContext().setMomentsCache(moments);
//
//                Intent intent1 = new Intent(MomentBroadcastReceiver.ACTION_RECEIVER_SYNCMOMENTSCACHE);
//                sendBroadcast(intent1);
//            }
//        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (MomentConfig.getConfig(MomentService.this).isNetworkConnected()) {
                    ApiController.getMoments(0, new Response.Listener<MomentList>() {
                        @Override
                        public void onResponse(MomentList response) {
                            MomentApplication.getContext().setMomentsCache(response);

                            Intent intent1 = new Intent(MomentBroadcastReceiver.ACTION_RECEIVER_SYNCMOMENTSCACHE);
                            sendBroadcast(intent1);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });

                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (MomentConfig.getConfig(MomentService.this).isNetworkConnected()) {
                    if (MomentApplication.getContext().getUser() != null) {

                        MomentApplication.getContext().syncUserState();
                        MomentApplication.getContext().syncLikedMomentId();
                        MomentApplication.getContext().syncFollowing();
                        MomentApplication.getContext().syncFollowers();
                        MomentApplication.getContext().syncUserMoments();

                        Intent intent1 = new Intent(MomentBroadcastReceiver.ACTION_RECEIVER_SYNCFOLLOWS);
                        sendBroadcast(intent1);
                    }
                }

            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
