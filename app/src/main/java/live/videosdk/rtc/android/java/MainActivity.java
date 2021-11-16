package live.videosdk.rtc.android.java;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.Stream;
import live.videosdk.rtc.android.VideoSDK;
import live.videosdk.rtc.android.lib.PeerConnectionUtils;
import live.videosdk.rtc.android.listeners.MeetingEventListener;
import live.videosdk.rtc.android.listeners.ParticipantEventListener;

public class MainActivity extends AppCompatActivity {
    private Meeting meeting;
    private SurfaceViewRenderer svrLocal;

    private boolean micEnabled = true;
    private boolean webcamEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        svrLocal = findViewById(R.id.svrLocal);
        svrLocal.init(PeerConnectionUtils.getEglContext(), null);

        final String token = getIntent().getStringExtra("token");
        final String meetingId = getIntent().getStringExtra("meetingId");
        final String participantName = "John Doe";

        // pass the token generated from api server
        VideoSDK.config(token);

        // create a new meeting instance
        meeting = VideoSDK.initMeeting(
                MainActivity.this, meetingId, participantName,
                micEnabled, webcamEnabled
        );

        meeting.addEventListener(meetingEventListener);

        //
        final TextView tvMeetingId = findViewById(R.id.tvMeetingId);
        tvMeetingId.setText(meetingId);
        tvMeetingId.setOnClickListener(v -> copyTextToClipboard(meetingId));

        //
        final RecyclerView rvParticipants = findViewById(R.id.rvParticipants);
        rvParticipants.setLayoutManager(new GridLayoutManager(this, 2));
        rvParticipants.setAdapter(new ParticipantAdapter(meeting));

        // Local participant listeners
        setLocalListeners();

        //
        checkPermissions();

        // Actions
        setActionListeners();
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
            Toast.makeText(MainActivity.this, participant.getDisplayName() + " joined",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onParticipantLeft(Participant participant) {
            Toast.makeText(MainActivity.this, participant.getDisplayName() + " left",
                    Toast.LENGTH_SHORT).show();
        }
    };

    private final PermissionHandler permissionHandler = new PermissionHandler() {
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

    private void setLocalListeners() {
        meeting.getLocalParticipant().addEventListener(new ParticipantEventListener() {
            @Override
            public void onStreamEnabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    track.addSink(svrLocal);

                    webcamEnabled = true;
                    Toast.makeText(MainActivity.this, "Webcam enabled", Toast.LENGTH_SHORT).show();
                } else if (stream.getKind().equalsIgnoreCase("audio")) {
                    micEnabled = true;
                    Toast.makeText(MainActivity.this, "Mic enabled", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStreamDisabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    track.removeSink(svrLocal);
                    svrLocal.clearImage();

                    webcamEnabled = false;
                    Toast.makeText(MainActivity.this, "Webcam disabled", Toast.LENGTH_SHORT).show();
                } else if (stream.getKind().equalsIgnoreCase("audio")) {
                    micEnabled = false;
                    Toast.makeText(MainActivity.this, "Mic disabled", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void copyTextToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied text", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(MainActivity.this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
    }


    private void setActionListeners() {
        // Toggle mic
        findViewById(R.id.btnMic).setOnClickListener(view -> {
            if (micEnabled) {
                meeting.muteMic();
            } else {
                meeting.unmuteMic();
            }
        });

        // Toggle webcam
        findViewById(R.id.btnWebcam).setOnClickListener(view -> {
            if (webcamEnabled) {
                meeting.disableWebcam();
            } else {
                meeting.enableWebcam();
            }
        });

        // Leave meeting
        findViewById(R.id.btnLeave).setOnClickListener(view -> {
            meeting.leave();
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        if (meeting != null) meeting.leave();

        super.onDestroy();
    }
}