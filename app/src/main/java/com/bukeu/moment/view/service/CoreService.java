package com.bukeu.moment.view.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CoreService extends Service {
    public CoreService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
