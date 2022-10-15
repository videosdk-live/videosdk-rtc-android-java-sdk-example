package live.videosdk.rtc.android.java.Common.Listener;

public interface ResponseListener {

    void onResponse(String meetingId);

    void onMeetingTimeChanged(int meetingTime);

}
