package live.videosdk.rtc.android.java;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.VideoSDK;

public class MainApplication extends Application {

    private Meeting meeting;

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        VideoSDK.initialize(getApplicationContext());
        AndroidNetworking.initialize(getApplicationContext());
    }
}