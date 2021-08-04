package live.videosdk.rtc.android.java;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;

import org.webrtc.EglBase;

import live.videosdk.rtc.android.VideoSDK;

public class MainApplication extends Application {

    private static EglBase mEglBase = EglBase.create();

    @Override
    public void onCreate() {
        super.onCreate();

        VideoSDK.initialize(getApplicationContext());
        AndroidNetworking.initialize(getApplicationContext());
    }

    public static EglBase.Context getEglContext() {
        return mEglBase.getEglBaseContext();
    }

}