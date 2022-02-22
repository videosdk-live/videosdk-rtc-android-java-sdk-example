package live.videosdk.rtc.android.java;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.Stream;
import live.videosdk.rtc.android.VideoSDK;
import live.videosdk.rtc.android.lib.PeerConnectionUtils;
import live.videosdk.rtc.android.lib.PubSubMessage;
import live.videosdk.rtc.android.listeners.MeetingEventListener;
import live.videosdk.rtc.android.listeners.MicRequestListener;
import live.videosdk.rtc.android.listeners.ParticipantEventListener;
import live.videosdk.rtc.android.listeners.PubSubMessageListener;
import live.videosdk.rtc.android.listeners.WebcamRequestListener;
import live.videosdk.rtc.android.model.LivestreamOutput;

public class MainActivity extends AppCompatActivity {
    private Meeting meeting;
    private SurfaceViewRenderer svrShare;
    private FloatingActionButton btnMic, btnWebcam;

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

        svrShare = findViewById(R.id.svrShare);
        svrShare.init(PeerConnectionUtils.getEglContext(), null);

        btnMic = findViewById(R.id.btnMic);
        btnWebcam = findViewById(R.id.btnWebcam);

        final String token = getIntent().getStringExtra("token");
        final String meetingId = getIntent().getStringExtra("meetingId");
        micEnabled = getIntent().getBooleanExtra("micEnabled", true);
        webcamEnabled = getIntent().getBooleanExtra("webcamEnabled", true);
        String participantName = getIntent().getStringExtra("paticipantName");
        if (participantName == null) {
            participantName = "John Doe";
        }

        //
        toggleMicIcon();
        toggleWebcamIcon();

        //
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(meetingId);
        toolbar.setOnMenuItemClickListener(menu -> {
            if (menu.getItemId() == R.id.contentCopy) {
                copyTextToClipboard(meetingId);
            }
            return true;
        });

        // pass the token generated from api server
        VideoSDK.config(token);

        // create a new meeting instance
        meeting = VideoSDK.initMeeting(
                MainActivity.this, meetingId, participantName,
                micEnabled, webcamEnabled
        );

        meeting.addEventListener(meetingEventListener);

        //
        ((MainApplication) this.getApplication()).setMeeting(meeting);

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

    private void toggleMicIcon() {
        if (micEnabled) {
            btnMic.setImageResource(R.drawable.ic_baseline_mic_24);
            btnMic.setColorFilter(Color.WHITE);
            btnMic.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        } else {
            btnMic.setImageResource(R.drawable.ic_baseline_mic_off_24);
            btnMic.setColorFilter(Color.BLACK);
            btnMic.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_grey_300)));
        }
    }

    private void toggleWebcamIcon() {
        if (webcamEnabled) {
            btnWebcam.setImageResource(R.drawable.ic_baseline_videocam_24);
            btnWebcam.setColorFilter(Color.WHITE);
            btnWebcam.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));

        } else {
            btnWebcam.setImageResource(R.drawable.ic_baseline_videocam_off_24);
            btnWebcam.setColorFilter(Color.BLACK);
            btnWebcam.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_grey_300)));
        }
    }


    private final MeetingEventListener meetingEventListener = new MeetingEventListener() {
        @Override
        public void onMeetingJoined() {
            Log.d("#meeting", "onMeetingJoined()");

            // notify user of any new messages
            meeting.pubSub.subscribe("CHAT", new PubSubMessageListener() {
                @Override
                public void onMessageReceived(PubSubMessage pubSubMessage) {
                    if (!pubSubMessage.getSenderId().equals(meeting.getLocalParticipant().getId())) {
                        View parentLayout = findViewById(android.R.id.content);
                        Snackbar.make(parentLayout, pubSubMessage.getSenderName() + " says: " +
                                pubSubMessage.getMessage(), Snackbar.LENGTH_SHORT)
                                .setDuration(2000).show();
                    }
                }
            });
        }

        @Override
        public void onMeetingLeft() {
            Log.d("#meeting", "onMeetingLeft()");
            meeting = null;
            if (!isDestroyed()) finish();
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
        public void onPresenterChanged(String participantId) {
            updatePresenter(participantId);
        }

        @Override
        public void onRecordingStarted() {
            recording = true;

            (findViewById(R.id.recordIcon)).setVisibility(View.VISIBLE);

            Toast.makeText(MainActivity.this, "Recording started",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRecordingStopped() {
            recording = false;

            (findViewById(R.id.recordIcon)).setVisibility(View.GONE);

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

    private void updatePresenter(String participantId) {
        if (participantId == null) {
            svrShare.clearImage();
            svrShare.setVisibility(View.GONE);
            return;
        }

        // find participant
        Participant participant = meeting.getParticipants().get(participantId);
        if (participant == null) return;

        // find share stream in participant
        Stream shareStream = null;

        for (Stream stream : participant.getStreams().values()) {
            if (stream.getKind().equals("share")) {
                shareStream = stream;
                break;
            }
        }

        if (shareStream == null) return;

        // display share video
        svrShare.setVisibility(View.VISIBLE);
        svrShare.setZOrderMediaOverlay(true);

        VideoTrack videoTrack = (VideoTrack) shareStream.getTrack();
        videoTrack.addSink(svrShare);

        // listen for share stop event
        participant.addEventListener(new ParticipantEventListener() {
            @Override
            public void onStreamDisabled(Stream stream) {
                if (stream.getKind().equals("share")) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    if (track != null) track.removeSink(svrShare);
                    svrShare.clearImage();
                    svrShare.setVisibility(View.GONE);
                }
            }
        });
    }


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
                    webcamEnabled = true;
                    toggleWebcamIcon();
                } else if (stream.getKind().equalsIgnoreCase("audio")) {
                    micEnabled = true;
                    toggleMicIcon();
                }
            }

            @Override
            public void onStreamDisabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    webcamEnabled = false;
                    toggleWebcamIcon();
                } else if (stream.getKind().equalsIgnoreCase("audio")) {
                    micEnabled = false;
                    toggleMicIcon();
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
        btnMic.setOnClickListener(view -> {
            if (micEnabled) {
                meeting.muteMic();
            } else {
                meeting.unmuteMic();
            }
        });

        // Toggle webcam
        btnWebcam.setOnClickListener(view -> {
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

        findViewById(R.id.btnMore).setOnClickListener(v -> showMoreOptionsDialog());

        findViewById(R.id.btnSwitchCameraMode).setOnClickListener(view -> {
            meeting.changeWebcam();
        });

        // Chat
        findViewById(R.id.btnChat).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });
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

        if (svrShare != null) svrShare.release();

        ((RecyclerView) findViewById(R.id.rvParticipants)).setAdapter(null);

        super.onDestroy();
    }
}