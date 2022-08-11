package live.videosdk.rtc.android.java.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.json.JSONObject;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import live.videosdk.rtc.android.CustomStreamTrack;
import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.Stream;
import live.videosdk.rtc.android.VideoSDK;
import live.videosdk.rtc.android.java.Adapter.AudioDeviceListAdapter;
import live.videosdk.rtc.android.java.Adapter.LeaveOptionListAdapter;
import live.videosdk.rtc.android.java.Adapter.MessageAdapter;
import live.videosdk.rtc.android.java.Adapter.MoreOptionsListAdapter;
import live.videosdk.rtc.android.java.Adapter.ParticipantListAdapter;
import live.videosdk.rtc.android.java.Modal.ListItem;
import live.videosdk.rtc.android.java.R;
import live.videosdk.rtc.android.java.Roboto_font;
import live.videosdk.rtc.android.java.Utils.HelperClass;
import live.videosdk.rtc.android.lib.AppRTCAudioManager;
import live.videosdk.rtc.android.lib.JsonUtils;
import live.videosdk.rtc.android.lib.PeerConnectionUtils;
import live.videosdk.rtc.android.lib.PubSubMessage;
import live.videosdk.rtc.android.listeners.MeetingEventListener;
import live.videosdk.rtc.android.listeners.ParticipantEventListener;
import live.videosdk.rtc.android.listeners.PubSubMessageListener;
import live.videosdk.rtc.android.model.PubSubPublishOptions;

public class OneToOneCallActivity extends AppCompatActivity {
    private static Meeting meeting;
    private SurfaceViewRenderer svrLocal;
    private SurfaceViewRenderer svrParticipant;
    private FloatingActionButton btnWebcam;
    private ImageButton btnMic, btnAudioSelection, btnSwitchCameraMode;
    private FloatingActionButton btnLeave, btnChat, btnMore;
    private CardView localCard, participantCard;
    private LinearLayout micLayout;
    ArrayList<Participant> participants;
    private TextView txtLocalParticipantName, txtParticipantName;

    private VideoTrack participantTrack = null;

    private boolean micEnabled = true;
    private boolean webcamEnabled = true;
    private boolean recording = false;
    private boolean localScreenShare = false;
    private boolean fullScreen = false;
    int clickCount = 0;
    long startTime;
    static final int MAX_DURATION = 500;
    private Snackbar recordingStatusSnackbar;

    private static final String YOUTUBE_RTMP_URL = null;
    private static final String YOUTUBE_RTMP_STREAM_KEY = null;

    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;

    private Timer timer = new Timer();
    private boolean screenshareEnabled = false;
    private VideoTrack localTrack = null;
    private VideoTrack screenshareTrack;
    private BottomSheetDialog bottomSheetDialog;
    private String selectedAudioDeviceName;

    private EditText etmessage;
    private MessageAdapter messageAdapter;
    private PubSubMessageListener pubSubMessageListener;


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_to_one_call);

        //
        Toolbar toolbar = findViewById(R.id.material_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //
        btnLeave = findViewById(R.id.btnLeave);
        btnChat = findViewById(R.id.btnChat);
        btnMore = findViewById(R.id.btnMore);
        btnSwitchCameraMode = findViewById(R.id.btnSwitchCameraMode);

        micLayout = findViewById(R.id.micLayout);
        localCard = findViewById(R.id.LocalCard);
        participantCard = findViewById(R.id.ParticipantCard);

        txtLocalParticipantName = findViewById(R.id.txtLocalParticipantName);
        txtParticipantName = findViewById(R.id.txtParticipantName);

        svrLocal = findViewById(R.id.svrLocal);
        svrLocal.init(PeerConnectionUtils.getEglContext(), null);

        svrParticipant = findViewById(R.id.svrParticipant);
        svrParticipant.init(PeerConnectionUtils.getEglContext(), null);

        btnMic = findViewById(R.id.btnMic);
        btnWebcam = findViewById(R.id.btnWebcam);
        btnAudioSelection = findViewById(R.id.btnAudioSelection);

        final String token = getIntent().getStringExtra("token");
        final String meetingId = getIntent().getStringExtra("meetingId");
        micEnabled = getIntent().getBooleanExtra("micEnabled", true);
        webcamEnabled = getIntent().getBooleanExtra("webcamEnabled", true);

        String participantName = getIntent().getStringExtra("participantName");
        if (participantName == null) {
            participantName = "John Doe";
        }
        txtLocalParticipantName.setText(participantName.substring(0, 1));

        //

        TextView textMeetingId = findViewById(R.id.txtMeetingId);
        textMeetingId.setText(meetingId);

        // pass the token generated from api server
        VideoSDK.config(token);

        // create a new meeting instance
        meeting = VideoSDK.initMeeting(
                OneToOneCallActivity.this, meetingId, participantName,
                false, false, null, null
        );

        meeting.addEventListener(meetingEventListener);

        //show Progress
        HelperClass.showProgress(getWindow().getDecorView().getRootView());

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

        recordingStatusSnackbar = Snackbar.make(findViewById(R.id.mainLayout), "Recording will be started in few moments",
                Snackbar.LENGTH_INDEFINITE);
        HelperClass.setSnackNarStyle(recordingStatusSnackbar.getView());
        recordingStatusSnackbar.setGestureInsetBottomIgnored(true);

        ((FrameLayout) findViewById(R.id.participants_frameLayout)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_UP:

                        clickCount++;

                        if (clickCount == 1) {
                            startTime = System.currentTimeMillis();
                        } else if (clickCount == 2) {
                            long duration = System.currentTimeMillis() - startTime;
                            if (duration <= MAX_DURATION) {
                                if (fullScreen) {
                                    getSupportActionBar().show();
                                    ((LinearLayout) findViewById(R.id.layout_action)).setVisibility(View.VISIBLE);
                                } else {
                                    getSupportActionBar().hide();
                                    ((LinearLayout) findViewById(R.id.layout_action)).setVisibility(View.GONE);
                                }
                                fullScreen = !fullScreen;
                                clickCount = 0;
                            } else {
                                clickCount = 1;
                                startTime = System.currentTimeMillis();
                            }
                            break;
                        }
                }

                return true;
            }
        });

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                isNetworkAvailable();
            }
        }, 0, 10000);

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = networkInfo != null && networkInfo.isConnected();

        if (!isAvailable) {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.mainLayout), "No Internet Connection",
                    Snackbar.LENGTH_LONG);
            HelperClass.setSnackNarStyle(snackbar.getView());
            snackbar.show();
        }

        return isAvailable;
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void toggleMicIcon(boolean micEnabled) {
        if (micEnabled) {
            btnMic.setImageResource(R.drawable.ic_mic_on);
            btnAudioSelection.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);
            micLayout.setBackground(ContextCompat.getDrawable(OneToOneCallActivity.this, R.drawable.layout_selected));
        } else {
            btnMic.setImageResource(R.drawable.ic_mic_off_24);
            btnAudioSelection.setImageResource(R.drawable.ic_baseline_arrow_drop_down);
            micLayout.setBackgroundColor(Color.WHITE);
            micLayout.setBackground(ContextCompat.getDrawable(OneToOneCallActivity.this, R.drawable.layout_nonselected));
        }
    }

    @SuppressLint("ResourceType")
    private void toggleWebcamIcon(Boolean webcamEnabled) {
        if (webcamEnabled) {
            btnWebcam.setImageResource(R.drawable.ic_video_camera);
            btnWebcam.setColorFilter(Color.WHITE);
            Drawable buttonDrawable = btnWebcam.getBackground();
            buttonDrawable = DrawableCompat.wrap(buttonDrawable);
            //the color is a direct color int and not a color resource
            if (buttonDrawable != null) DrawableCompat.setTint(buttonDrawable, Color.TRANSPARENT);
            btnWebcam.setBackground(buttonDrawable);

        } else {
            btnWebcam.setImageResource(R.drawable.ic_video_camera_off);
            btnWebcam.setColorFilter(Color.BLACK);
            Drawable buttonDrawable = btnWebcam.getBackground();
            buttonDrawable = DrawableCompat.wrap(buttonDrawable);
            //the color is a direct color int and not a color resource
            if (buttonDrawable != null) DrawableCompat.setTint(buttonDrawable, Color.WHITE);
            btnWebcam.setBackground(buttonDrawable);
        }
    }

    private final MeetingEventListener meetingEventListener = new MeetingEventListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onMeetingJoined() {
            //hide progress when meetingJoined
            HelperClass.hideProgress(getWindow().getDecorView().getRootView());

            // if more than 2 participant join than leave the meeting
            if (meeting.getParticipants().size() <= 1) {

                toggleMicIcon(micEnabled);
                toggleWebcamIcon(webcamEnabled);

                micEnabled = !micEnabled;
                webcamEnabled = !webcamEnabled;

                toggleMic();
                toggleWebCam();

                // Local participant listeners
                setLocalListeners();
                // notify user of any new messages
                meeting.pubSub.subscribe("CHAT", new PubSubMessageListener() {
                    @Override
                    public void onMessageReceived(PubSubMessage pubSubMessage) {
                        if (!pubSubMessage.getSenderId().equals(meeting.getLocalParticipant().getId())) {
                            View parentLayout = findViewById(android.R.id.content);
                            Snackbar snackbar =
                                    Snackbar.make(parentLayout, pubSubMessage.getSenderName() + " says: " +
                                                    pubSubMessage.getMessage(), Snackbar.LENGTH_SHORT)
                                            .setDuration(2000);
                            View snackbarView = snackbar.getView();
                            HelperClass.setSnackNarStyle(snackbarView);
                            snackbar.show();
                        }
                    }
                });


                //terminate meeting in 10 minutes
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isDestroyed())
                            new MaterialAlertDialogBuilder(OneToOneCallActivity.this)
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
            } else {
                View progressLayout = LayoutInflater.from(getApplicationContext()).inflate(R.layout.progress_layout, findViewById(R.id.layout_progress));


                if (!((Activity) OneToOneCallActivity.this).isFinishing())
                    HelperClass.checkParticipantSize(getWindow().getDecorView().getRootView(), progressLayout);

                progressLayout.findViewById(R.id.leaveBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        meeting.leave();
                    }
                });

            }
        }

        @Override
        public void onMeetingLeft() {
            Log.d("#meeting", "onMeetingLeft()");
            meeting = null;
            if (!isDestroyed()) {
                Intent intents = new Intent(OneToOneCallActivity.this, CreateOrJoinActivity.class);
                intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intents);
                finish();
            }
        }

        @Override
        public void onParticipantJoined(Participant participant) {
            if (meeting.getParticipants().size() < 2) {
                participant.setQuality("high");
                showParticipantCard();
                txtParticipantName.setText(participant.getDisplayName().substring(0, 1));
                Toast.makeText(OneToOneCallActivity.this, participant.getDisplayName() + " joined",
                        Toast.LENGTH_SHORT).show();
            }
            participant.addEventListener(participantEventListener);
        }

        @Override
        public void onParticipantLeft(Participant participant) {
            if (meeting.getParticipants().size() < 1) {
                hideParticipantCard();
                if (screenshareTrack != null) {
                    if (participantTrack != null) participantTrack.removeSink(svrLocal);
                    svrLocal.clearImage();
                    svrLocal.setVisibility(View.GONE);
                    showParticipantCard();
                    if (localTrack != null) {
                        localTrack.addSink(svrLocal);
                        svrLocal.setVisibility(View.VISIBLE);
                    }
                    screenshareTrack.addSink(svrParticipant);
                    svrParticipant.setVisibility(View.VISIBLE);

                }
                Toast.makeText(OneToOneCallActivity.this, participant.getDisplayName() + " left",
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onPresenterChanged(String participantId) {
            updatePresenter(participantId);
        }

        @Override
        public void onRecordingStarted() {
            recording = true;

            recordingStatusSnackbar.dismiss();
            (findViewById(R.id.recordIcon)).setVisibility(View.VISIBLE);
            Toast.makeText(OneToOneCallActivity.this, "Recording started",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRecordingStopped() {
            recording = false;

            (findViewById(R.id.recordIcon)).setVisibility(View.GONE);

            Toast.makeText(OneToOneCallActivity.this, "Recording stopped",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onExternalCallStarted() {
            Toast.makeText(OneToOneCallActivity.this, "onExternalCallStarted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(JSONObject error) {
            try {
                JSONObject errorCodes = VideoSDK.getErrorCodes();
                int code = error.getInt("code");
                if (code == errorCodes.getInt("START_LIVESTREAM_FAILED")) {
                    Log.d("#error", "Error is: " + error.get("message"));
                } else if (code == errorCodes.getInt("START_RECORDING_FAILED")) {
                    Log.d("#error", "Error is: " + error.get("message"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void showParticipantCard() {
        localCard.setLayoutParams(new CardView.LayoutParams(getWindowWidth() / 4, getWindowHeight() / 5, Gravity.RIGHT | Gravity.BOTTOM));
        ViewGroup.MarginLayoutParams cardViewMarginParams = (ViewGroup.MarginLayoutParams) localCard.getLayoutParams();
        cardViewMarginParams.setMargins(30, 0, 60, 40);
        localCard.requestLayout();
        txtLocalParticipantName.setLayoutParams(new FrameLayout.LayoutParams(120, 120, Gravity.CENTER));
        txtLocalParticipantName.setTextSize(24);
        txtLocalParticipantName.setGravity(Gravity.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            txtLocalParticipantName.setForegroundGravity(Gravity.CENTER);
        }
        participantCard.setVisibility(View.VISIBLE);

    }

    private void hideParticipantCard() {
        localCard.setLayoutParams(new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ViewGroup.MarginLayoutParams cardViewMarginParams = (ViewGroup.MarginLayoutParams) localCard.getLayoutParams();
        cardViewMarginParams.setMargins(30, 30, 30, 30);
        localCard.requestLayout();
        txtLocalParticipantName.setLayoutParams(new FrameLayout.LayoutParams(220, 220, Gravity.CENTER));
        txtLocalParticipantName.setTextSize(40);
        txtLocalParticipantName.setGravity(Gravity.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            txtLocalParticipantName.setForegroundGravity(Gravity.CENTER);
        }
        participantCard.setVisibility(View.GONE);
    }


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
            Toast.makeText(OneToOneCallActivity.this, "You didn't give permission to capture the screen.", Toast.LENGTH_SHORT).show();
            localScreenShare = false;
            return;
        }

        //Used custom track for screen share.
//        VideoSDK.createScreenShareVideoTrack("h720p_15fps", data, this, (track) -> {
//            meeting.enableScreenShare(track);
//        });

        meeting.enableScreenShare(data);
    }

    private void updatePresenter(String participantId) {
        if (participantId == null) {
            screenshareEnabled = false;
            return;
        } else {
            screenshareEnabled = true;
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

        screenshareTrack = (VideoTrack) shareStream.getTrack();
        onTrackChange();

        // listen for share stop event
        participant.addEventListener(new ParticipantEventListener() {
            @Override
            public void onStreamDisabled(Stream stream) {
                if (stream.getKind().equals("share")) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    screenshareTrack = null;
                    track.removeSink(svrParticipant);
                    svrParticipant.clearImage();
                    svrParticipant.setVisibility(View.GONE);
                    removeTrack(participantTrack, true);
                    onTrackChange();
                    screenshareEnabled = false;
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
                selectedAudioDeviceName = selectedAudioDevice.toString();
            }
        });
    }

    private void copyTextToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied text", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(OneToOneCallActivity.this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
    }

    private void toggleMic() {
        if (micEnabled) {
            meeting.muteMic();
        } else {
            JSONObject noiseConfig = new JSONObject();
            JsonUtils.jsonPut(noiseConfig, "acousticEchoCancellation", true);
            JsonUtils.jsonPut(noiseConfig, "noiseSuppression", true);
            JsonUtils.jsonPut(noiseConfig, "autoGainControl", true);

            CustomStreamTrack audioCustomTrack = VideoSDK.createAudioTrack("high_quality", noiseConfig, this);

            meeting.unmuteMic(audioCustomTrack);
        }
        micEnabled = !micEnabled;
    }

    private void toggleWebCam() {
        if (webcamEnabled) {
            meeting.disableWebcam();
        } else {
            CustomStreamTrack videoCustomTrack = VideoSDK.createCameraVideoTrack("h240p_w320p", "front", this);
            meeting.enableWebcam(videoCustomTrack);
        }
        webcamEnabled = !webcamEnabled;
    }

    private void setActionListeners() {
        // Toggle mic
        micLayout.setOnClickListener(view -> {
            toggleMic();
        });

        btnMic.setOnClickListener(view -> {
            toggleMic();
        });

        // Toggle webcam
        btnWebcam.setOnClickListener(view -> {
            toggleWebCam();
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
            if (meeting != null) {
                openChat();
            }
        });

    }

    private void toggleScreenSharing() {
        if (!screenshareEnabled) {
            if (!localScreenShare) {
                askPermissionForScreenShare();
            }
            localScreenShare = !localScreenShare;
        } else {
            if (localScreenShare) {
                meeting.disableScreenShare();
            } else {
                Toast.makeText(this, "You can't share your screen", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void showLeaveOrEndDialog() {
        ArrayList<ListItem> OptionsArrayList = new ArrayList<>();
        ListItem leaveMeeting = new ListItem("Leave", "Only you will leave the call", AppCompatResources.getDrawable(OneToOneCallActivity.this, R.drawable.ic_leave));
        ListItem endMeeting = new ListItem("End", "End call for all the participants", AppCompatResources.getDrawable(OneToOneCallActivity.this, R.drawable.ic_end_meeting));

        OptionsArrayList.add(leaveMeeting);
        OptionsArrayList.add(endMeeting);

        ArrayAdapter arrayAdapter = new LeaveOptionListAdapter(OneToOneCallActivity.this, R.layout.leave_options_list_layout, OptionsArrayList);
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(OneToOneCallActivity.this, R.style.AlertDialogCustom)
                .setAdapter(arrayAdapter, (dialog, which) -> {
                    switch (which) {
                        case 0: {
                            meeting.leave();
                            break;
                        }
                        case 1: {
                            meeting.end();
                            break;
                        }
                    }
                });

        AlertDialog alertDialog = materialAlertDialogBuilder.create();

        ListView listView = alertDialog.getListView();
        listView.setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.divider_color))); // set color
        listView.setFooterDividersEnabled(false);
        listView.addFooterView(new View(OneToOneCallActivity.this));
        listView.setDividerHeight(2);

        WindowManager.LayoutParams wmlp = alertDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.BOTTOM | Gravity.LEFT;

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        layoutParams.width = (int) Math.round(getWindowWidth() * 0.8);
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        alertDialog.getWindow().setAttributes(layoutParams);
        alertDialog.show();
    }


    private void showAudioInputDialog() {
        Set<AppRTCAudioManager.AudioDevice> mics = meeting.getMics();
        ListItem audioDeviceListItem = null;
        ArrayList<ListItem> audioDeviceList = new ArrayList<>();
        // Prepare list
        String item;
        for (int i = 0; i < mics.size(); i++) {
            item = mics.toArray()[i].toString();
            String mic = item.substring(0, 1).toUpperCase() + item.substring(1).toLowerCase();
            mic = mic.replace("_", " ");
            audioDeviceListItem = new ListItem(mic, null, item.equals(selectedAudioDeviceName));
            audioDeviceList.add(audioDeviceListItem);
        }

        ArrayAdapter arrayAdapter = new AudioDeviceListAdapter(OneToOneCallActivity.this, R.layout.audio_device_list_layout, audioDeviceList);

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(OneToOneCallActivity.this, R.style.AlertDialogCustom)
                .setAdapter(arrayAdapter, (dialog, which) -> {
                    AppRTCAudioManager.AudioDevice audioDevice = null;
                    switch (audioDeviceList.get(which).getItemName()) {
                        case "Bluetooth":
                            audioDevice = AppRTCAudioManager.AudioDevice.BLUETOOTH;
                            break;
                        case "Wired headset":
                            audioDevice = AppRTCAudioManager.AudioDevice.WIRED_HEADSET;
                            break;
                        case "Speaker phone":
                            audioDevice = AppRTCAudioManager.AudioDevice.SPEAKER_PHONE;
                            break;
                        case "Earpiece":
                            audioDevice = AppRTCAudioManager.AudioDevice.EARPIECE;
                            break;
                    }
                    meeting.changeMic(audioDevice);
                });

        AlertDialog alertDialog = materialAlertDialogBuilder.create();

        ListView listView = alertDialog.getListView();
        listView.setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.divider_color))); // set color
        listView.setFooterDividersEnabled(false);
        listView.addFooterView(new View(OneToOneCallActivity.this));
        listView.setDividerHeight(2);

        WindowManager.LayoutParams wmlp = alertDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.BOTTOM | Gravity.LEFT;

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        layoutParams.width = (int) Math.round(getWindowWidth() * 0.6);
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        alertDialog.getWindow().setAttributes(layoutParams);

        alertDialog.show();
    }

    private void showMoreOptionsDialog() {
        int participantSize = meeting.getParticipants().size() + 1;
        ArrayList<ListItem> moreOptionsArrayList = new ArrayList<>();
        ListItem start_screen_share = new ListItem("Share screen", AppCompatResources.getDrawable(OneToOneCallActivity.this, R.drawable.ic_screen_share));
        ListItem stop_screen_share = new ListItem("Stop screen share", AppCompatResources.getDrawable(OneToOneCallActivity.this, R.drawable.ic_screen_share));
        ListItem start_recording = new ListItem("Start recording", AppCompatResources.getDrawable(OneToOneCallActivity.this, R.drawable.ic_recording));
        ListItem stop_recording = new ListItem("Stop recording", AppCompatResources.getDrawable(OneToOneCallActivity.this, R.drawable.ic_recording));
        ListItem participant_list = new ListItem("Participants (" + participantSize + ")", AppCompatResources.getDrawable(OneToOneCallActivity.this, R.drawable.ic_people));
        if (localScreenShare) {
            moreOptionsArrayList.add(stop_screen_share);
        } else {
            moreOptionsArrayList.add(start_screen_share);
        }

        if (recording) {
            moreOptionsArrayList.add(stop_recording);
        } else {
            moreOptionsArrayList.add(start_recording);

        }

        moreOptionsArrayList.add(participant_list);


        ArrayAdapter arrayAdapter = new MoreOptionsListAdapter(OneToOneCallActivity.this, R.layout.more_options_list_layout, moreOptionsArrayList);
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(OneToOneCallActivity.this, R.style.AlertDialogCustom)
                .setAdapter(arrayAdapter, (dialog, which) -> {
                    switch (which) {
                        case 0: {
                            toggleScreenSharing();
                            break;
                        }
                        case 1: {
                            toggleRecording();
                            break;
                        }
                        case 2: {
                            openParticipantList();
                            break;
                        }
                    }
                });

        AlertDialog alertDialog = materialAlertDialogBuilder.create();

        ListView listView = alertDialog.getListView();
        listView.setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.divider_color))); // set color
        listView.setFooterDividersEnabled(false);
        listView.addFooterView(new View(OneToOneCallActivity.this));
        listView.setDividerHeight(2);

        WindowManager.LayoutParams wmlp = alertDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.BOTTOM | Gravity.RIGHT;

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        layoutParams.width = (int) Math.round(getWindowWidth() * 0.8);
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        alertDialog.getWindow().setAttributes(layoutParams);
        alertDialog.show();
    }

    private void toggleRecording() {
        if (!recording) {
            recordingStatusSnackbar.show();
            meeting.startRecording(null);

        } else {
            meeting.stopRecording();
        }
    }

    @Override
    public void onBackPressed() {
        showLeaveOrEndDialog();
    }

    @Override
    protected void onDestroy() {
        if (meeting != null) meeting.leave();

        if (svrParticipant != null) {
            svrParticipant.clearImage();
            svrLocal.setVisibility(View.GONE);
            svrParticipant.release();
        }

        if (svrLocal != null) {
            svrLocal.clearImage();
            svrLocal.setVisibility(View.GONE);
            svrLocal.release();
        }

        timer.cancel();

        super.onDestroy();
    }

    private void onTrackChange() {
        if (screenshareTrack != null) {

            if (meeting.getParticipants().size() == 0) {
                showParticipantCard();

            } else {
                if (localTrack != null) {
                    localTrack.removeSink(svrLocal);
                    svrLocal.clearImage();
                    svrLocal.setVisibility(View.GONE);
                }

                if (participantTrack != null) {
                    participantTrack.removeSink(svrParticipant);
                    svrParticipant.clearImage();
                    participantTrack.addSink(svrLocal);
                    svrLocal.setVisibility(View.VISIBLE);
                }
            }

            screenshareTrack.addSink(svrParticipant);
            svrParticipant.setVisibility(View.VISIBLE);
        } else {

            if (participantTrack != null) {
                svrParticipant.setVisibility(View.VISIBLE);
                participantTrack.addSink(svrParticipant);
            }
            if (localTrack != null) {
                svrLocal.setVisibility(View.VISIBLE);
                svrLocal.setZOrderMediaOverlay(true);
                localTrack.addSink(svrLocal);
                ((View) localCard).bringToFront();

            }

        }


    }

    private void removeTrack(VideoTrack track, Boolean isLocal) {
        if (screenshareTrack == null) {
            if (isLocal) {
                if (track != null) track.removeSink(svrLocal);
                svrLocal.clearImage();
                svrLocal.setVisibility(View.GONE);
            } else {
                if (track != null) track.removeSink(svrParticipant);
                svrParticipant.clearImage();
                svrParticipant.setVisibility(View.GONE);
            }
        } else {
            if (!isLocal) {
                if (track != null) track.removeSink(svrLocal);
                svrLocal.clearImage();
                svrLocal.setVisibility(View.GONE);
                onTrackChange();
            } else {
                if (track != null) track.removeSink(svrParticipant);
                svrParticipant.clearImage();
                svrParticipant.setVisibility(View.GONE);
                onTrackChange();
            }
        }
    }

    private final ParticipantEventListener participantEventListener = new ParticipantEventListener() {
        @Override
        public void onStreamEnabled(Stream stream) {
            if (stream.getKind().equalsIgnoreCase("video")) {
                if (meeting.getParticipants().size() < 2) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    participantTrack = track;
                    onTrackChange();
                }
            }
            if (stream.getKind().equalsIgnoreCase("audio")) {
                stream.pause();
            }
        }

        @Override
        public void onStreamDisabled(Stream stream) {
            if (stream.getKind().equalsIgnoreCase("video")) {
                if (meeting.getParticipants().size() < 2) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    if (track != null) participantTrack = null;
                    removeTrack(track, false);
                }
            }
            if (stream.getKind().equalsIgnoreCase("audio")) {
                stream.pause();
            }
        }
    };

    private void setLocalListeners() {
        meeting.getLocalParticipant().addEventListener(new ParticipantEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onStreamEnabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    localTrack = track;
                    onTrackChange();
                    webcamEnabled = true;
                    toggleWebcamIcon(true);
                } else if (stream.getKind().equalsIgnoreCase("audio")) {
                    toggleMicIcon(true);
                } else if (stream.getKind().equalsIgnoreCase("share")) {
                    // display share video
                    VideoTrack videoTrack = (VideoTrack) stream.getTrack();
                    screenshareTrack = videoTrack;
                    onTrackChange();
                    //
                    localScreenShare = true;
                    screenshareEnabled = true;
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onStreamDisabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    if (track != null) localTrack = null;
                    removeTrack(track, true);
                    webcamEnabled = false;
                    toggleWebcamIcon(false);
                } else if (stream.getKind().equalsIgnoreCase("audio")) {
                    toggleMicIcon(false);
                } else if (stream.getKind().equalsIgnoreCase("share")) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    if (track != null) screenshareTrack = null;
                    track.removeSink(svrParticipant);
                    svrParticipant.clearImage();
                    svrParticipant.setVisibility(View.GONE);
                    if (meeting.getParticipants().size() == 0) hideParticipantCard();

                    removeTrack(participantTrack, true);
                    onTrackChange();
                    //
                    localScreenShare = false;
                    screenshareEnabled = false;
                }
            }
        });
    }

    public void openParticipantList() {
        RecyclerView participantsListView;
        ImageView close;
        bottomSheetDialog = new BottomSheetDialog(this);
        View v3 = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_participants_list_view, findViewById(R.id.layout_participants));
        bottomSheetDialog.setContentView(v3);
        participantsListView = v3.findViewById(R.id.rvParticipantsLinearView);
        ((TextView) v3.findViewById(R.id.participant_heading)).setTypeface(Roboto_font.getTypeFace(OneToOneCallActivity.this));
        close = v3.findViewById(R.id.ic_close);
        participantsListView.setMinimumHeight(getWindowHeight());
        bottomSheetDialog.show();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
        meeting.addEventListener(meetingEventListener);
        participants = getAllParticipants();
        participantsListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        participantsListView.setAdapter(new ParticipantListAdapter(participants, meeting, getApplicationContext()));
        participantsListView.setHasFixedSize(true);
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        (OneToOneCallActivity.this).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    private int getWindowWidth() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        (OneToOneCallActivity.this).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public ArrayList<Participant> getAllParticipants() {
        ArrayList participantList = new ArrayList();
        final Iterator<Participant> participants = meeting.getParticipants().values().iterator();
        for (int i = 0; i < meeting.getParticipants().size(); i++) {
            final Participant participant = participants.next();
            participantList.add(participant);

        }
        return participantList;
    }


    public void openChat() {
        RecyclerView messageRcv;
        ImageView close;
        bottomSheetDialog = new BottomSheetDialog(this);
        View v3 = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_chat, findViewById(R.id.layout_chat));
        bottomSheetDialog.setContentView(v3);

        messageRcv = v3.findViewById(R.id.messageRcv);
        messageRcv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        RelativeLayout.LayoutParams lp =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getWindowHeight() / 2);
        messageRcv.setLayoutParams(lp);

        BottomSheetBehavior.BottomSheetCallback mBottomSheetCallback
                = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet,
                                       @BottomSheetBehavior.State int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    RelativeLayout.LayoutParams lp =
                            new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getWindowHeight() / 2);
                    messageRcv.setLayoutParams(lp);
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    RelativeLayout.LayoutParams lp =
                            new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                    messageRcv.setLayoutParams(lp);
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        };

        bottomSheetDialog.getBehavior().addBottomSheetCallback(mBottomSheetCallback);

        etmessage = v3.findViewById(R.id.etMessage);
        ImageButton btnSend = v3.findViewById(R.id.btnSend);
        btnSend.setEnabled(false);
        etmessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    etmessage.setHint("");
                }
            }
        });
        etmessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!etmessage.getText().toString().isEmpty()) {
                    btnSend.setEnabled(true);
                    btnSend.setSelected(true);
                } else {
                    btnSend.setEnabled(false);
                    btnSend.setSelected(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //
        pubSubMessageListener = new PubSubMessageListener() {
            @Override
            public void onMessageReceived(PubSubMessage message) {
                messageAdapter.addItem(message);
                messageRcv.scrollToPosition(messageAdapter.getItemCount() - 1);
            }
        };

        // Subscribe for 'CHAT' topic
        List<PubSubMessage> pubSubMessageList = meeting.pubSub.subscribe("CHAT", pubSubMessageListener);

        //
        messageAdapter = new MessageAdapter(this, R.layout.item_message_list, pubSubMessageList, meeting);
        messageRcv.setAdapter(messageAdapter);
        messageRcv.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) ->
                messageRcv.scrollToPosition(messageAdapter.getItemCount() - 1));

        v3.findViewById(R.id.btnSend).setOnClickListener(view -> {
            String message = etmessage.getText().toString();
            if (!message.equals("")) {
                PubSubPublishOptions publishOptions = new PubSubPublishOptions();
                publishOptions.setPersist(true);

                meeting.pubSub.publish("CHAT", message, publishOptions);
                etmessage.setText("");
            } else {
                Toast.makeText(OneToOneCallActivity.this, "Please Enter Message",
                        Toast.LENGTH_SHORT).show();
            }

        });


        close = v3.findViewById(R.id.ic_close);
        bottomSheetDialog.show();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                meeting.pubSub.unsubscribe("CHAT", pubSubMessageListener);
                bottomSheetDialog.dismiss();
            }
        });

    }


}