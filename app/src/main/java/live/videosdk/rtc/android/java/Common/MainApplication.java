package live.videosdk.rtc.android.java.Common;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;

import live.videosdk.rtc.android.VideoSDK;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VideoSDK.initialize(getApplicationContext());
        AndroidNetworking.initialize(getApplicationContext());
    }
}