package live.videosdk.rtc.android.java;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.Stream;
import live.videosdk.rtc.android.VideoSDK;
import live.videosdk.rtc.android.lib.PeerConnectionUtils;
import live.videosdk.rtc.android.listeners.MeetingEventListener;
import live.videosdk.rtc.android.listeners.MicRequestListener;
import live.videosdk.rtc.android.listeners.ParticipantEventListener;
import live.videosdk.rtc.android.listeners.WebcamRequestListener;
import live.videosdk.rtc.android.model.LivestreamOutput;

public class MainActivity extends AppCompatActivity {
    private Meeting meeting;
    private SurfaceViewRenderer svrLocal;

    private boolean micEnabled = true;
    private boolean webcamEnabled = true;
    private boolean recording = false;
    private boolean livestreaming = false;

    private static final String YOUTUBE_RTMP_URL = null;
    private static final String YOUTUBE_RTMP_STREAM_KEY = null;

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
            meeting = null;
            finish();
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

        @Override
        public void onRecordingStarted() {
            recording = true;
            Toast.makeText(MainActivity.this, "Recording started",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRecordingStopped() {
            recording = false;
            Toast.makeText(MainActivity.this, "Recording stopped",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLivestreamStarted() {
            livestreaming = true;
            Toast.makeText(MainActivity.this, "Livestream started",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLivestreamStopped() {
            livestreaming = false;
            Toast.makeText(MainActivity.this, "Livestream stopped",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onMicRequested(String participantId, MicRequestListener listener) {
            showMicRequestDialog(listener);
        }

        @Override
        public void onWebcamRequested(String participantId, WebcamRequestListener listener) {
            showWebcamRequestDialog(listener);
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
                    svrLocal.setVisibility(View.VISIBLE);

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
                    if (track != null) track.removeSink(svrLocal);

                    svrLocal.clearImage();
                    svrLocal.setVisibility(View.GONE);

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
            showLeaveOrEndDialog();
        });

        // Participants list
        findViewById(R.id.btnParticipants).setOnClickListener(view -> {
            showParticipantsDialog();
        });

        findViewById(R.id.btnMore).setOnClickListener(v -> showMoreOptionsDialog());
    }

    private void showLeaveOrEndDialog() {
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("Leave or End meeting")
                .setMessage("Leave from meeting or end the meeting for everyone ?")
                .setPositiveButton("Leave", (dialog, which) -> {
                    meeting.leave();
                    finish();
                })
                .setNegativeButton("End", (dialog, which) -> {
                    meeting.end();
                    finish();
                })
                .show();
    }

    private void showParticipantsDialog() {
        // Prepare list
        final int nParticipants = meeting.getParticipants().size();

        final String[] items = nParticipants > 0
                ? new String[nParticipants]
                : new String[]{"No participants have joined yet."};

        final Iterator<Participant> participants = meeting.getParticipants().values().iterator();

        for (int i = 0; i < nParticipants; i++) {
            final Participant participant = participants.next();
            items[i] = participant.getId() + " - " + participant.getDisplayName();
        }

        // Display list in dialog
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle(getString(R.string.participants_list))
                .setItems(items, null)
                .show();
    }

    private void showMoreOptionsDialog() {
        final String[] items = new String[]{
                recording ? "Stop recording" : "Start recording",
                livestreaming ? "Stop livestreaming" : "Start livestreaming"
        };

        new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle(getString(R.string.more_options))
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0: {
                            toggleRecording();
                            break;
                        }
                        case 1: {
                            toggleLivestreaming();
                            break;
                        }
                    }
                })
                .show();
    }

    private void toggleRecording() {
        if (!recording) {
            meeting.startRecording(null);
        } else {
            meeting.stopRecording();
        }
    }

    private void toggleLivestreaming() {
        if (!livestreaming) {
            if (YOUTUBE_RTMP_URL == null || YOUTUBE_RTMP_STREAM_KEY == null) {
                throw new Error("RTMP url or stream key missing.");
            }

            List<LivestreamOutput> outputs = new ArrayList<>();
            outputs.add(new LivestreamOutput(YOUTUBE_RTMP_URL, YOUTUBE_RTMP_STREAM_KEY));

            meeting.startLivestream(outputs);
        } else {
            meeting.stopLivestream();
        }
    }

    private void showMicRequestDialog(MicRequestListener listener) {
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("Mic requested")
                .setMessage("Host is asking you to unmute your mic, do you want to allow ?")
                .setPositiveButton("Yes", (dialog, which) -> listener.accept())
                .setNegativeButton("No", (dialog, which) -> listener.reject())
                .show();
    }

    private void showWebcamRequestDialog(WebcamRequestListener listener) {
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("Webcam requested")
                .setMessage("Host is asking you to enable your webcam, do you want to allow ?")
                .setPositiveButton("Yes", (dialog, which) -> listener.accept())
                .setNegativeButton("No", (dialog, which) -> listener.reject())
                .show();
    }

    @Override
    public void onBackPressed() {
        showLeaveOrEndDialog();
    }

    @Override
    protected void onDestroy() {
        if (meeting != null) meeting.leave();

        super.onDestroy();
    }
}