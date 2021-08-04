package live.videosdk.rtc.android.java;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.VideoSDK;
import live.videosdk.rtc.android.listeners.MeetingEventListener;

public class MainActivity extends AppCompatActivity {
    private Meeting meeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String token = getIntent().getStringExtra("token");
        final String meetingId = getIntent().getStringExtra("meetingId");
        final String participantName = "John Doe";


        final TextView tvMeetingId = findViewById(R.id.tvMeetingId);
        tvMeetingId.setText(meetingId);

        final boolean micEnabled = true;
        final boolean webcamEnabled = true;

        // pass the token generated from api server
        VideoSDK.config(token);

        // create a new meeting instance
        meeting = VideoSDK.initMeeting(
                MainActivity.this, meetingId, participantName,
                micEnabled, webcamEnabled
        );

        meeting.addEventListener(meetingEventListener);

        checkPermissions();
    }

    private final MeetingEventListener meetingEventListener = new MeetingEventListener() {
        @Override
        public void onMeetingJoined() {
            Log.d("#meeting", "onMeetingJoined()");
        }

        @Override
        public void onMeetingLeft() {
            Log.d("#meeting", "onMeetingLeft()");
        }

        @Override
        public void onParticipantJoined(Participant participant) {
            Log.d("#meeting", "onParticipantJoined()");
        }

        @Override
        public void onParticipantLeft(Participant participant) {
            Log.d("#meeting", "onParticipantLeft()");
        }
    };

    private final PermissionHandler permissionHandler =
            new PermissionHandler() {
                @Override
                public void onGranted() {
                    if (meeting != null) meeting.join();
                }
            };


    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        String rationale = "Please provide permissions";
        Permissions.Options options =
                new Permissions.Options().setRationaleDialogTitle("Info").setSettingsDialogTitle("Warning");
        Permissions.check(this, permissions, rationale, options, permissionHandler);
    }

    @Override
    protected void onDestroy() {
        if (meeting != null) meeting.leave();

        super.onDestroy();
    }
}