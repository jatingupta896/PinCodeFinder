package com.example.jatin.AddressLocator;

import android.app.Application;

/**
 * Created by JATIN on 18-07-2017.
 * for internet connection state
 */

public class NetworkChecking extends Application {

    private static NetworkChecking mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized NetworkChecking getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(NetworkConnectivityReceiver.ConnectivityReceiverListener listener) {
        NetworkConnectivityReceiver.connectivityReceiverListener = listener;
    }
}
