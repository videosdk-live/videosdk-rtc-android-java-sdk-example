package live.videosdk.rtc.android.java;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.Stream;
import live.videosdk.rtc.android.VideoSDK;
import live.videosdk.rtc.android.lib.AppRTCAudioManager;
import live.videosdk.rtc.android.lib.PeerConnectionUtils;
import live.videosdk.rtc.android.lib.PubSubMessage;
import live.videosdk.rtc.android.listeners.MeetingEventListener;
import live.videosdk.rtc.android.listeners.MicRequestListener;
import live.videosdk.rtc.android.listeners.ParticipantEventListener;
import live.videosdk.rtc.android.listeners.PubSubMessageListener;
import live.videosdk.rtc.android.listeners.WebcamRequestListener;
import live.videosdk.rtc.android.model.LivestreamOutput;

public class MainActivity extends AppCompatActivity {
    private static Meeting meeting;
    private SurfaceViewRenderer svrShare;
    private FloatingActionButton btnMic, btnWebcam, btnScreenShare;
    private FloatingActionButton btnLeave, btnChat, btnSwitchCameraMode, btnMore;
    private ImageButton btnAudioSelection;

    private boolean micEnabled = true;
    private boolean webcamEnabled = true;
    private boolean recording = false;
    private boolean livestreaming = false;
    private boolean localScreenShare = false;
    private boolean isNetworkAvailable = true;


    private static final String YOUTUBE_RTMP_URL = null;
    private static final String YOUTUBE_RTMP_STREAM_KEY = null;

    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;

    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        btnLeave = findViewById(R.id.btnLeave);
        btnChat = findViewById(R.id.btnChat);
        btnMore = findViewById(R.id.btnMore);
        btnSwitchCameraMode = findViewById(R.id.btnSwitchCameraMode);
        btnScreenShare = findViewById(R.id.btnScreenShare);

        btnAudioSelection = (ImageButton) findViewById(R.id.btnAudioSelection);
        btnAudioSelection.setEnabled(false);

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

        // pass the token generated from api server
        VideoSDK.config(token);

        // create a new meeting instance
        meeting = VideoSDK.initMeeting(
                MainActivity.this, meetingId, participantName,
                micEnabled, webcamEnabled
        );

        //
        ((MainApplication) this.getApplication()).setMeeting(meeting);

        meeting.addEventListener(meetingEventListener);

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

        setAudioDeviceListeners();

        ((ImageButton) findViewById(R.id.btnCopyContent)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyTextToClipboard(meetingId);
            }
        });

        btnAudioSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAudioInputDialog();
            }
        });

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                isNetworkAvailable = isNetworkAvailable();
                if (!isNetworkAvailable) {
                    runOnUiThread(() -> {
                        if (!isDestroyed()) {
                            new MaterialAlertDialogBuilder(MainActivity.this)
                                    .setMessage("No Internet Connection")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", (dialog, which) -> {
                                        if (!isDestroyed()) {
                                            meeting.leave();
                                        }
                                    })
                                    .create().show();
                        }
                    });
                }
            }
        }, 0, 10000);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = networkInfo != null && networkInfo.isConnected();

        if (!isAvailable) {
            Snackbar.make(findViewById(R.id.mainLayout), "No Internet Connection",
                    Snackbar.LENGTH_LONG).show();
        }

        return isAvailable;
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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


            //enable all the Buttons when meetingJoined
            btnMic.setEnabled(true);
            btnWebcam.setEnabled(true);
            btnLeave.setEnabled(true);
            btnSwitchCameraMode.setEnabled(true);
            btnChat.setEnabled(true);
            btnScreenShare.setEnabled(true);
            btnMore.setEnabled(true);
            btnAudioSelection.setEnabled(true);


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

            //terminate meeting in 10 minutes
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isDestroyed())
                        new MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle("Meeting Left")
                                .setMessage("Demo app limits meeting to 10 Minutes")
                                .setCancelable(false)
                                .setPositiveButton("Ok", (dialog, which) -> {
                                    if (!isDestroyed())
                                        meeting.leave();
                                    Log.d("Auto Terminate", "run: Meeting Terminated");
                                })
                                .create().show();
                }
            }, 600000);
        }

        @Override
        public void onMeetingLeft() {
            Log.d("#meeting", "onMeetingLeft()");
            meeting = null;
            if (!isDestroyed()) {
                Intent intents = new Intent(MainActivity.this, CreateOrJoinActivity.class);
                intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intents);
                finish();
            }
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

        @Override
        public void onExternalCallStarted() {
            Toast.makeText(MainActivity.this, "onExternalCallStarted", Toast.LENGTH_SHORT).show();
        }
    };


    @TargetApi(21)
    private void askPermissionForScreenShare() {
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
            return;
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(MainActivity.this, "You didn't give permission to capture the screen.", Toast.LENGTH_SHORT).show();
            localScreenShare = false;
            return;
        }
        meeting.enableScreenShare(data);
        btnScreenShare.setImageResource(R.drawable.ic_outline_stop_screen_share_24);
    }

    private void updatePresenter(String participantId) {
        if (participantId == null) {
            svrShare.clearImage();
            svrShare.setVisibility(View.GONE);
            btnScreenShare.setEnabled(true);
            return;
        } else {
            btnScreenShare.setEnabled(meeting.getLocalParticipant().getId().equals(participantId));
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
        svrShare.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

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
                    localScreenShare = false;
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
                Manifest.permission.READ_PHONE_STATE
        };
        String rationale = "Please provide permissions";
        Permissions.Options options =
                new Permissions.Options().setRationaleDialogTitle("Info").setSettingsDialogTitle("Warning");
        Permissions.check(this, permissions, rationale, options, permissionHandler);
    }

    private void setAudioDeviceListeners() {
        meeting.setAudioDeviceChangeListener(new AppRTCAudioManager.AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(AppRTCAudioManager.AudioDevice selectedAudioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
                switch (selectedAudioDevice) {
                    case BLUETOOTH:
                        ((ImageButton) findViewById(R.id.btnAudioSelection)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_bluetooth_audio_24));
                        break;
                    case WIRED_HEADSET:
                        ((ImageButton) findViewById(R.id.btnAudioSelection)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_headset_24));
                        break;
                    case SPEAKER_PHONE:
                        ((ImageButton) findViewById(R.id.btnAudioSelection)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_volume_up_24));
                        break;
                    case EARPIECE:
                        ((ImageButton) findViewById(R.id.btnAudioSelection)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_phone_in_talk_24));
                        break;
                }
            }
        });
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
                } else if (stream.getKind().equalsIgnoreCase("share")) {
                    // display share video
                    svrShare.setVisibility(View.VISIBLE);
                    svrShare.setZOrderMediaOverlay(true);
                    svrShare.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

                    VideoTrack videoTrack = (VideoTrack) stream.getTrack();
                    videoTrack.addSink(svrShare);
                    //
                    localScreenShare = true;
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
                } else if (stream.getKind().equalsIgnoreCase("share")) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    if (track != null) track.removeSink(svrShare);
                    svrShare.clearImage();
                    svrShare.setVisibility(View.GONE);
                    //
                    localScreenShare = false;
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
        btnLeave.setOnClickListener(view -> {
            showLeaveOrEndDialog();
        });

        btnMore.setOnClickListener(v -> showMoreOptionsDialog());

        btnSwitchCameraMode.setOnClickListener(view -> {
            meeting.changeWebcam();
        });

        // Chat
        btnChat.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        //
        btnScreenShare.setOnClickListener(view -> {
            toggleScreenSharing();
        });
    }

    private void toggleScreenSharing() {
        if (!localScreenShare) {
            askPermissionForScreenShare();
        } else {
            meeting.disableScreenShare();
            btnScreenShare.setImageResource(R.drawable.ic_outline_screen_share_24);
        }
        localScreenShare = !localScreenShare;
    }


    private void showLeaveOrEndDialog() {

        new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("Leave or End meeting")
                .setMessage("Leave from meeting or end the meeting for everyone ?")
                .setPositiveButton("Leave", (dialog, which) -> {
                    meeting.leave();
                })
                .setNegativeButton("End", (dialog, which) -> {
                    meeting.end();
                })
                .show();
    }

    private void showAudioInputDialog() {
        Set<AppRTCAudioManager.AudioDevice> mics = meeting.getMics();

        // Prepare list
        final String[] items = new String[mics.size()];
        for (int i = 0; i < mics.size(); i++) {
            items[i] = mics.toArray()[i].toString();
        }
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle(getString(R.string.audio_options))
                .setItems(items, (dialog, which) -> {
                    AppRTCAudioManager.AudioDevice audioDevice = null;
                    switch (items[which]) {
                        case "BLUETOOTH":
                            audioDevice = AppRTCAudioManager.AudioDevice.BLUETOOTH;
                            break;
                        case "WIRED_HEADSET":
                            audioDevice = AppRTCAudioManager.AudioDevice.WIRED_HEADSET;
                            break;
                        case "SPEAKER_PHONE":
                            audioDevice = AppRTCAudioManager.AudioDevice.SPEAKER_PHONE;
                            break;
                        case "EARPIECE":
                            audioDevice = AppRTCAudioManager.AudioDevice.EARPIECE;
                            break;
                    }
                    meeting.changeMic(audioDevice);
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

        timer.cancel();

        super.onDestroy();
    }
}