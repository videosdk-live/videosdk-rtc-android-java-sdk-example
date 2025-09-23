package live.videosdk.rtc.android.java.GroupCall.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;
import org.webrtc.RendererCommon;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import live.videosdk.rtc.android.CustomStreamTrack;
import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.Stream;
import live.videosdk.rtc.android.VideoSDK;
import live.videosdk.rtc.android.VideoView;
import live.videosdk.rtc.android.java.GroupCall.Adapter.ParticipantViewAdapter;
import live.videosdk.rtc.android.java.GroupCall.Utils.ParticipantState;
import live.videosdk.rtc.android.java.R;
import live.videosdk.rtc.android.java.Common.Activity.CreateOrJoinActivity;
import live.videosdk.rtc.android.java.Common.Adapter.AudioDeviceListAdapter;
import live.videosdk.rtc.android.java.Common.Adapter.LeaveOptionListAdapter;
import live.videosdk.rtc.android.java.Common.Adapter.MessageAdapter;
import live.videosdk.rtc.android.java.Common.Adapter.MoreOptionsListAdapter;
import live.videosdk.rtc.android.java.Common.Adapter.ParticipantListAdapter;
import live.videosdk.rtc.android.java.Common.Listener.ResponseListener;
import live.videosdk.rtc.android.java.Common.Modal.ListItem;
import live.videosdk.rtc.android.java.Common.Roboto_font;
import live.videosdk.rtc.android.java.Common.Utils.HelperClass;
import live.videosdk.rtc.android.java.Common.Utils.NetworkUtils;
import live.videosdk.rtc.android.lib.AppRTCAudioManager;
import live.videosdk.rtc.android.lib.JsonUtils;
import live.videosdk.rtc.android.lib.PubSubMessage;
import live.videosdk.rtc.android.listeners.MeetingEventListener;
import live.videosdk.rtc.android.listeners.MicRequestListener;
import live.videosdk.rtc.android.listeners.ParticipantEventListener;
import live.videosdk.rtc.android.listeners.PubSubMessageListener;
import live.videosdk.rtc.android.listeners.WebcamRequestListener;
import live.videosdk.rtc.android.model.PubSubPublishOptions;
import live.videosdk.rtc.android.permission.Permission;

public class GroupCallActivity extends AppCompatActivity {

    private static Meeting meeting;
    private FloatingActionButton btnWebcam;
    private ImageButton btnMic, btnAudioSelection, btnSwitchCameraMode;
    private FloatingActionButton btnLeave, btnChat, btnMore;

    private LinearLayout micLayout;
    ArrayList<Participant> participants;
    private VideoView shareView;
    private FrameLayout shareLayout;

    private boolean micEnabled = true;
    private boolean webcamEnabled = true;
    private boolean recording = false;
    private boolean localScreenShare = false;
    private static String token;
    private Snackbar recordingStatusSnackbar;


    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;

    private boolean screenshareEnabled = false;
    private BottomSheetDialog bottomSheetDialog;
    private String selectedAudioDeviceName;

    private EditText etmessage;
    private MessageAdapter messageAdapter;
    private PubSubMessageListener pubSubMessageListener;
    private ViewPager2 viewPager2;
    private ParticipantViewAdapter viewAdapter;
    private int meetingSeconds;
    private TextView txtMeetingTime;
    private Button btnStopScreenShare;

    int clickCount = 0;
    long startTime;
    static final int MAX_DURATION = 500;
    boolean fullScreen = false;
    View.OnTouchListener onTouchListener;
    private Snackbar screenShareParticipantNameSnackbar;
    private Runnable runnable;
    final Handler handler = new Handler();
    private PubSubMessageListener chatListener;
    private PubSubMessageListener raiseHandListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_call);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
        btnMic = findViewById(R.id.btnMic);
        btnWebcam = findViewById(R.id.btnWebcam);
        btnAudioSelection = findViewById(R.id.btnAudioSelection);
        txtMeetingTime = findViewById(R.id.txtMeetingTime);
        btnStopScreenShare = findViewById(R.id.btnStopScreenShare);


        viewPager2 = findViewById(R.id.view_pager_video_grid);
        shareLayout = findViewById(R.id.shareLayout);
        shareView = findViewById(R.id.shareView);

        token = getIntent().getStringExtra("token");
        final String meetingId = getIntent().getStringExtra("meetingId");
        micEnabled = getIntent().getBooleanExtra("micEnabled", true);
        webcamEnabled = getIntent().getBooleanExtra("webcamEnabled", true);

        String localParticipantName = getIntent().getStringExtra("participantName");
        if (localParticipantName == null) {
            localParticipantName = "John Doe";
        }

        // pass the token generated from api server
        VideoSDK.config(token);

        Map<String, CustomStreamTrack> customTracks = new HashMap<>();

        CustomStreamTrack videoCustomTrack = VideoSDK.createCameraVideoTrack("h720p_w960p", "front", CustomStreamTrack.VideoMode.TEXT, true, this,VideoSDK.getSelectedVideoDevice());
        customTracks.put("video", videoCustomTrack);

        CustomStreamTrack audioCustomTrack = VideoSDK.createAudioTrack("high_quality", this);
        customTracks.put("mic", audioCustomTrack);

        // create a new meeting instance
        meeting = VideoSDK.initMeeting(
                GroupCallActivity.this, meetingId, localParticipantName,
                micEnabled, webcamEnabled, null, null, true, customTracks,null
        );

        //
        TextView textMeetingId = findViewById(R.id.txtMeetingId);
        textMeetingId.setText(meetingId);

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

        btnStopScreenShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (localScreenShare) {
                    meeting.disableScreenShare();
                }
            }
        });

        recordingStatusSnackbar = Snackbar.make(findViewById(R.id.mainLayout), "Recording will be started in few moments",
                Snackbar.LENGTH_INDEFINITE);
        HelperClass.setSnackBarStyle(recordingStatusSnackbar.getView(), 0);
        recordingStatusSnackbar.setGestureInsetBottomIgnored(true);

        viewAdapter = new ParticipantViewAdapter(GroupCallActivity.this, meeting);

        onTouchListener = new View.OnTouchListener() {
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
                                    toolbar.setVisibility(View.VISIBLE);
                                    for (int i = 0; i < toolbar.getChildCount(); i++) {
                                        toolbar.getChildAt(i).setVisibility(View.VISIBLE);
                                    }

                                    Toolbar.LayoutParams params = new Toolbar.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                    );
                                    params.setMargins(22, 10, 0, 0);
                                    findViewById(R.id.meetingLayout).setLayoutParams(params);

                                    shareLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, HelperClass.dpToPx(420, GroupCallActivity.this)));
                                    ((LinearLayout) findViewById(R.id.localScreenShareView)).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, HelperClass.dpToPx(420, GroupCallActivity.this)));

                                    TranslateAnimation toolbarAnimation = new TranslateAnimation(
                                            0,
                                            0,
                                            0,
                                            10);
                                    toolbarAnimation.setDuration(500);
                                    toolbarAnimation.setFillAfter(true);
                                    toolbar.startAnimation(toolbarAnimation);

                                    BottomAppBar bottomAppBar = findViewById(R.id.bottomAppbar);
                                    bottomAppBar.setVisibility(View.VISIBLE);
                                    for (int i = 0; i < bottomAppBar.getChildCount(); i++) {
                                        bottomAppBar.getChildAt(i).setVisibility(View.VISIBLE);
                                    }

                                    TranslateAnimation animate = new TranslateAnimation(
                                            0,
                                            0,
                                            findViewById(R.id.bottomAppbar).getHeight(),
                                            0);
                                    animate.setDuration(300);
                                    animate.setFillAfter(true);
                                    findViewById(R.id.bottomAppbar).startAnimation(animate);
                                } else {
                                    toolbar.setVisibility(View.GONE);
                                    for (int i = 0; i < toolbar.getChildCount(); i++) {
                                        toolbar.getChildAt(i).setVisibility(View.GONE);
                                    }

                                    shareLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, HelperClass.dpToPx(500, GroupCallActivity.this)));

                                    ((LinearLayout) findViewById(R.id.localScreenShareView)).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, HelperClass.dpToPx(500, GroupCallActivity.this)));

                                    TranslateAnimation toolbarAnimation = new TranslateAnimation(
                                            0,
                                            0,
                                            0,
                                            10);
                                    toolbarAnimation.setDuration(500);
                                    toolbarAnimation.setFillAfter(true);
                                    toolbar.startAnimation(toolbarAnimation);

                                    BottomAppBar bottomAppBar = findViewById(R.id.bottomAppbar);
                                    bottomAppBar.setVisibility(View.GONE);
                                    for (int i = 0; i < bottomAppBar.getChildCount(); i++) {
                                        bottomAppBar.getChildAt(i).setVisibility(View.GONE);
                                    }

                                    TranslateAnimation animate = new TranslateAnimation(
                                            0,
                                            0,
                                            0,
                                            findViewById(R.id.bottomAppbar).getHeight());
                                    animate.setDuration(400);
                                    animate.setFillAfter(true);
                                    findViewById(R.id.bottomAppbar).startAnimation(animate);
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
        };

        findViewById(R.id.participants_Layout).setOnTouchListener(onTouchListener);

        findViewById(R.id.ivParticipantScreenShareNetwork).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Participant> participantList = getAllParticipants();
                Participant participant = participantList.get(0);
                PopupWindow popupwindow_obj = HelperClass.callStatsPopupDisplay(participant, findViewById(R.id.ivParticipantScreenShareNetwork), GroupCallActivity.this, true);
                popupwindow_obj.showAsDropDown(findViewById(R.id.ivParticipantScreenShareNetwork), -350, -85);
            }
        });

        findViewById(R.id.ivLocalScreenShareNetwork).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupWindow popupwindow_obj = HelperClass.callStatsPopupDisplay(meeting.getLocalParticipant(), findViewById(R.id.ivLocalScreenShareNetwork), GroupCallActivity.this, true);
                popupwindow_obj.showAsDropDown(findViewById(R.id.ivLocalScreenShareNetwork), -350, -85);
            }
        });
    }

    public View.OnTouchListener getOnTouchListener() {
        return onTouchListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void toggleMicIcon() {
        if (micEnabled) {
            btnMic.setImageResource(R.drawable.ic_mic_on);
            btnAudioSelection.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);
            micLayout.setBackground(ContextCompat.getDrawable(GroupCallActivity.this, R.drawable.layout_selected));
        } else {
            btnMic.setImageResource(R.drawable.ic_mic_off_24);
            btnAudioSelection.setImageResource(R.drawable.ic_baseline_arrow_drop_down);
            micLayout.setBackgroundColor(Color.WHITE);
            micLayout.setBackground(ContextCompat.getDrawable(GroupCallActivity.this, R.drawable.layout_nonselected));
        }
    }

    @SuppressLint("ResourceType")
    private void toggleWebcamIcon() {
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
            if (meeting != null) {
                //hide progress when meetingJoined
                HelperClass.hideProgress(getWindow().getDecorView().getRootView());

                toggleMicIcon();
                toggleWebcamIcon();

                setLocalListeners();

                new NetworkUtils(GroupCallActivity.this).fetchMeetingTime(meeting.getMeetingId(), token, new ResponseListener<Integer>() {
                    @Override
                    public void onResponse(Integer meetingTime) {
                        meetingSeconds = meetingTime;
                        showMeetingTime();
                    }
                });


                viewPager2.setOffscreenPageLimit(1);
                viewPager2.setAdapter(viewAdapter);


                raiseHandListener = new PubSubMessageListener() {
                    @Override
                    public void onMessageReceived(PubSubMessage pubSubMessage) {
                        View parentLayout = findViewById(android.R.id.content);
                        SpannableStringBuilder builderTextLeft = new SpannableStringBuilder();
                        if (pubSubMessage.getSenderId().equals(meeting.getLocalParticipant().getId())) {
                            builderTextLeft.append("   You raised hand");
                        } else {
                            builderTextLeft.append("   " + pubSubMessage.getSenderName() + " raised hand  ");
                        }
                        Drawable drawable = getResources().getDrawable(R.drawable.ic_raise_hand);
                        drawable.setBounds(0, 0, 50, 65);
                        builderTextLeft.setSpan(new ImageSpan(drawable), 0, 1, 0);
                        Snackbar snackbar = Snackbar.make(parentLayout, builderTextLeft,
                                Snackbar.LENGTH_SHORT);
                        HelperClass.setSnackBarStyle(snackbar.getView(), 0);
                        snackbar.setGestureInsetBottomIgnored(true);
                        snackbar.getView().setOnClickListener(view -> snackbar.dismiss());
                        snackbar.show();
                    }
                };

                // notify user for raise hand
                meeting.pubSub.subscribe("RAISE_HAND", raiseHandListener);


                chatListener = new PubSubMessageListener() {
                    @Override
                    public void onMessageReceived(PubSubMessage pubSubMessage) {
                        if (!pubSubMessage.getSenderId().equals(meeting.getLocalParticipant().getId())) {
                            View parentLayout = findViewById(android.R.id.content);
                            Snackbar snackbar =
                                    Snackbar.make(parentLayout, pubSubMessage.getSenderName() + " says: " +
                                                    pubSubMessage.getMessage(), Snackbar.LENGTH_SHORT)
                                            .setDuration(2000);
                            View snackbarView = snackbar.getView();
                            HelperClass.setSnackBarStyle(snackbarView, 0);
                            snackbar.getView().setOnClickListener(view -> snackbar.dismiss());
                            snackbar.show();
                        }
                    }
                };
                // notify user of any new messages
                meeting.pubSub.subscribe("CHAT", chatListener);

                //terminate meeting in 10 minutes
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isDestroyed()) {
                            AlertDialog alertDialog = new MaterialAlertDialogBuilder(GroupCallActivity.this, R.style.AlertDialogCustom).create();
                            alertDialog.setCanceledOnTouchOutside(false);

                            LayoutInflater inflater = GroupCallActivity.this.getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.alert_dialog_layout, null);
                            alertDialog.setView(dialogView);

                            TextView title = (TextView) dialogView.findViewById(R.id.title);
                            title.setText("Meeting Left");
                            TextView message = (TextView) dialogView.findViewById(R.id.message);
                            message.setText("Demo app limits meeting to 10 Minutes");

                            Button positiveButton = dialogView.findViewById(R.id.positiveBtn);
                            positiveButton.setText("Ok");
                            positiveButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!isDestroyed()) {
                                        ParticipantState.destroy();
                                        unSubscribeTopics();
                                        meeting.leave();
                                    }
                                    alertDialog.dismiss();
                                }
                            });

                            Button negativeButton = dialogView.findViewById(R.id.negativeBtn);
                            negativeButton.setVisibility(View.GONE);

                            alertDialog.show();
                        }

                    }
                }, 600000);
            }
        }

        @Override
        public void onMeetingLeft() {
            handler.removeCallbacks(runnable);
            if (!isDestroyed()) {
                Intent intents = new Intent(GroupCallActivity.this, CreateOrJoinActivity.class);
                intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intents);
                finish();
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
            (findViewById(R.id.recordingLottie)).setVisibility(View.VISIBLE);
            Toast.makeText(GroupCallActivity.this, "Recording started",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRecordingStopped() {
            recording = false;

            (findViewById(R.id.recordingLottie)).setVisibility(View.GONE);

            Toast.makeText(GroupCallActivity.this, "Recording stopped",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onExternalCallStarted() {
            Toast.makeText(GroupCallActivity.this, "onExternalCallStarted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(JSONObject error) {
            try {
                JSONObject errorCodes = VideoSDK.getErrorCodes();
                int code = error.getInt("code");
                if (code == errorCodes.getInt("PREV_RECORDING_PROCESSING")) {
                    recordingStatusSnackbar.dismiss();

                }
                Snackbar snackbar = Snackbar.make(findViewById(R.id.mainLayout), error.optString("message"),
                        Snackbar.LENGTH_LONG);
                HelperClass.setSnackBarStyle(snackbar.getView(), 0);
                snackbar.getView().setOnClickListener(view -> snackbar.dismiss());
                snackbar.show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onSpeakerChanged(String participantId) {

        }

        @Override
        public void onMeetingStateChanged(String state) {
            if (state == "FAILED") {
                View parentLayout = findViewById(android.R.id.content);
                SpannableStringBuilder builderTextLeft = new SpannableStringBuilder();
                builderTextLeft.append("   Call disconnected. Reconnecting...");
                builderTextLeft.setSpan(new ImageSpan(GroupCallActivity.this, R.drawable.ic_call_disconnected), 0, 1, 0);
                Snackbar snackbar = Snackbar.make(parentLayout, builderTextLeft, Snackbar.LENGTH_LONG);
                HelperClass.setSnackBarStyle(snackbar.getView(), getResources().getColor(R.color.md_red_400));
                snackbar.getView().setOnClickListener(view -> snackbar.dismiss());
                snackbar.show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (handler.hasCallbacks(runnable))
                        handler.removeCallbacks(runnable);
                }
            }
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


    private void setLocalListeners() {

        meeting.getLocalParticipant().addEventListener(new ParticipantEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onStreamEnabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    webcamEnabled = true;
                    toggleWebcamIcon();
                } else if (stream.getKind().equalsIgnoreCase("audio")) {
                    micEnabled = true;
                    toggleMicIcon();
                } else if (stream.getKind().equalsIgnoreCase("share")) {

                    findViewById(R.id.localScreenShareView).setVisibility(View.VISIBLE);
                    screenShareParticipantNameSnackbar = Snackbar.make(findViewById(R.id.mainLayout), "You started presenting",
                            Snackbar.LENGTH_SHORT);
                    HelperClass.setSnackBarStyle(screenShareParticipantNameSnackbar.getView(), 0);
                    screenShareParticipantNameSnackbar.setGestureInsetBottomIgnored(true);
                    screenShareParticipantNameSnackbar.getView().setOnClickListener(view -> screenShareParticipantNameSnackbar.dismiss());
                    screenShareParticipantNameSnackbar.show();

                    localScreenShare = true;
                    screenshareEnabled = true;
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onStreamDisabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    webcamEnabled = false;
                    toggleWebcamIcon();
                } else if (stream.getKind().equalsIgnoreCase("audio")) {
                    micEnabled = false;
                    toggleMicIcon();
                } else if (stream.getKind().equalsIgnoreCase("share")) {

                    findViewById(R.id.localScreenShareView).setVisibility(View.GONE);

                    localScreenShare = false;
                    screenshareEnabled = false;
                }
            }
        });
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
            Toast.makeText(GroupCallActivity.this, "You didn't give permission to capture the screen.", Toast.LENGTH_SHORT).show();
            localScreenShare = false;
            return;
        }

        meeting.enableScreenShare(data);
    }

    private final com.nabinbhandari.android.permissions.PermissionHandler permissionHandler = new com.nabinbhandari.android.permissions.PermissionHandler() {
        @Override
        public void onGranted() {

        }

        @Override
        public void onDenied(Context context, ArrayList<String> deniedPermissions) {
            super.onDenied(context, deniedPermissions);
            Toast.makeText(GroupCallActivity.this,
                    "Permission(s) not granted. Some feature may not work", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onBlocked(Context context, ArrayList<String> blockedList) {
            Toast.makeText(GroupCallActivity.this,
                    "Permission(s) not granted. Some feature may not work", Toast.LENGTH_SHORT).show();
            return super.onBlocked(context, blockedList);
        }
    };


    private live.videosdk.rtc.android.permission.PermissionHandler permissionHandlerSDK = new live.videosdk.rtc.android.permission.PermissionHandler() {
        @Override
        public void onGranted() {
            if (meeting != null) meeting.join();
        }

        @Override
        public boolean onBlocked(Context context, ArrayList<Permission> blockedList) {
            for (Permission blockedPermission : blockedList) {
                Log.d("VideoSDK Permission", "onBlocked: " + blockedPermission);
            }
            return super.onBlocked(context, blockedList);
        }

        @Override
        public void onDenied(Context context, ArrayList<Permission> deniedPermissions) {
            for (Permission deniedPermission : deniedPermissions) {
                Log.d("VideoSDK Permission", "onDenied: " + deniedPermission);
            }
            super.onDenied(context, deniedPermissions);
        }

        @Override
        public void onJustBlocked(Context context, ArrayList<Permission> justBlockedList, ArrayList<Permission> deniedPermissions) {
            for (Permission justBlockedPermission : justBlockedList) {
                Log.d("VideoSDK Permission", "onJustBlocked: " + justBlockedPermission);
            }
            super.onJustBlocked(context, justBlockedList, deniedPermissions);
        }
    };

    private void checkPermissions() {
        List<String> permissionList = new ArrayList<>();
        permissionList.add(Manifest.permission.INTERNET);
        permissionList.add(Manifest.permission.READ_PHONE_STATE);

        com.nabinbhandari.android.permissions.Permissions.Options options = new com.nabinbhandari.android.permissions.Permissions.Options().sendDontAskAgainToSettings(false);
        com.nabinbhandari.android.permissions.Permissions.check(this, permissionList.toArray(new String[0]), null, options, permissionHandler);

        List<Permission> permissionListSDK = new ArrayList<>();
        permissionListSDK.add(Permission.audio);
        permissionListSDK.add(Permission.video);
        permissionListSDK.add(Permission.bluetooth);

        live.videosdk.rtc.android.permission.Permissions.Options optionsSDK = new live.videosdk.rtc.android.permission.Permissions.Options()
                .setRationaleDialogTitle("Info")
                .setSettingsDialogTitle("Warning");

        VideoSDK.checkPermissions(this, permissionListSDK, optionsSDK, permissionHandlerSDK);
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

        Toast.makeText(GroupCallActivity.this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
    }

    private void toggleMic() {
        if (micEnabled) {
            meeting.muteMic();
        } else {
            CustomStreamTrack audioCustomTrack = VideoSDK.createAudioTrack("high_quality", this);

            meeting.unmuteMic(audioCustomTrack);
        }
    }

    private void toggleWebCam() {
        if (webcamEnabled) {
            meeting.disableWebcam();
        } else {
            CustomStreamTrack videoCustomTrack = VideoSDK.createCameraVideoTrack("h720p_w960p", "front", CustomStreamTrack.VideoMode.DETAIL, true, this,VideoSDK.getSelectedVideoDevice());
            meeting.enableWebcam(videoCustomTrack);
        }
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

    private void updatePresenter(String participantId) {
        if (participantId == null) {
            shareView.setVisibility(View.GONE);
            shareLayout.setVisibility(View.GONE);
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
        ((TextView) findViewById(R.id.tvScreenShareParticipantName)).setText(participant.getDisplayName() + " is presenting");
        findViewById(R.id.tvScreenShareParticipantName).setVisibility(View.VISIBLE);
        findViewById(R.id.ivParticipantScreenShareNetwork).setVisibility(View.VISIBLE);

        // display share video
        shareLayout.setVisibility(View.VISIBLE);
        shareView.setVisibility(View.VISIBLE);
        shareView.setZOrderMediaOverlay(true);
        shareView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        VideoTrack videoTrack = (VideoTrack) shareStream.getTrack();
        shareView.addTrack(videoTrack);

        screenShareParticipantNameSnackbar = Snackbar.make(findViewById(R.id.mainLayout), participant.getDisplayName() + " started presenting",
                Snackbar.LENGTH_SHORT);
        HelperClass.setSnackBarStyle(screenShareParticipantNameSnackbar.getView(), 0);
        screenShareParticipantNameSnackbar.setGestureInsetBottomIgnored(true);
        screenShareParticipantNameSnackbar.getView().setOnClickListener(view -> screenShareParticipantNameSnackbar.dismiss());
        screenShareParticipantNameSnackbar.show();

        // listen for share stop event
        participant.addEventListener(new ParticipantEventListener() {
            @Override
            public void onStreamDisabled(Stream stream) {
                if (stream.getKind().equals("share")) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    shareView.removeTrack();

                    shareView.setVisibility(View.GONE);
                    shareLayout.setVisibility(View.GONE);
                    findViewById(R.id.tvScreenShareParticipantName).setVisibility(View.GONE);
                    findViewById(R.id.ivParticipantScreenShareNetwork).setVisibility(View.GONE);
                    localScreenShare = false;
                }
            }
        });
    }

    private void showLeaveOrEndDialog() {
        ArrayList<ListItem> OptionsArrayList = new ArrayList<>();
        ListItem leaveMeeting = new ListItem("Leave", "Only you will leave the call", AppCompatResources.getDrawable(GroupCallActivity.this, R.drawable.ic_leave));
        ListItem endMeeting = new ListItem("End", "End call for all the participants", AppCompatResources.getDrawable(GroupCallActivity.this, R.drawable.ic_end_meeting));

        OptionsArrayList.add(leaveMeeting);
        OptionsArrayList.add(endMeeting);

        ArrayAdapter arrayAdapter = new LeaveOptionListAdapter(GroupCallActivity.this, R.layout.leave_options_list_layout, OptionsArrayList);
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(GroupCallActivity.this, R.style.AlertDialogCustom)
                .setAdapter(arrayAdapter, (dialog, which) -> {
                    switch (which) {
                        case 0: {
                            viewPager2.setAdapter(null);
                            ParticipantState.destroy();
                            unSubscribeTopics();
                            meeting.leave();
                            break;
                        }
                        case 1: {
                            viewPager2.setAdapter(null);
                            ParticipantState.destroy();
                            unSubscribeTopics();
                            meeting.end();
                            break;
                        }
                    }
                });

        AlertDialog alertDialog = materialAlertDialogBuilder.create();

        ListView listView = alertDialog.getListView();
        listView.setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.md_grey_200))); // set color
        listView.setFooterDividersEnabled(false);
        listView.addFooterView(new View(GroupCallActivity.this));
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

        ArrayAdapter arrayAdapter = new AudioDeviceListAdapter(GroupCallActivity.this, R.layout.audio_device_list_layout, audioDeviceList);

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(GroupCallActivity.this, R.style.AlertDialogCustom)
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

                    meeting.changeMic(audioDevice, VideoSDK.createAudioTrack("high_quality", this));
                });

        AlertDialog alertDialog = materialAlertDialogBuilder.create();

        ListView listView = alertDialog.getListView();
        listView.setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.md_grey_200))); // set color
        listView.setFooterDividersEnabled(false);
        listView.addFooterView(new View(GroupCallActivity.this));
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
        ListItem raised_hand = new ListItem("Raise Hand", AppCompatResources.getDrawable(GroupCallActivity.this, R.drawable.raise_hand));
        ListItem start_screen_share = new ListItem("Share screen", AppCompatResources.getDrawable(GroupCallActivity.this, R.drawable.ic_screen_share));
        ListItem stop_screen_share = new ListItem("Stop screen share", AppCompatResources.getDrawable(GroupCallActivity.this, R.drawable.ic_screen_share));
        ListItem start_recording = new ListItem("Start recording", AppCompatResources.getDrawable(GroupCallActivity.this, R.drawable.ic_recording));
        ListItem stop_recording = new ListItem("Stop recording", AppCompatResources.getDrawable(GroupCallActivity.this, R.drawable.ic_recording));
        ListItem participant_list = new ListItem("Participants (" + participantSize + ")", AppCompatResources.getDrawable(GroupCallActivity.this, R.drawable.ic_people));

        moreOptionsArrayList.add(raised_hand);

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


        ArrayAdapter arrayAdapter = new MoreOptionsListAdapter(GroupCallActivity.this, R.layout.more_options_list_layout, moreOptionsArrayList);
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(GroupCallActivity.this, R.style.AlertDialogCustom)
                .setAdapter(arrayAdapter, (dialog, which) -> {
                    switch (which) {
                        case 0: {
                            raisedHand();
                            break;
                        }
                        case 1: {
                            toggleScreenSharing();
                            break;
                        }
                        case 2: {
                            toggleRecording();
                            break;
                        }
                        case 3: {
                            openParticipantList();
                            break;
                        }
                    }
                });

        AlertDialog alertDialog = materialAlertDialogBuilder.create();

        ListView listView = alertDialog.getListView();
        listView.setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.md_grey_200))); // set color
        listView.setFooterDividersEnabled(false);
        listView.addFooterView(new View(GroupCallActivity.this));
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

    private void raisedHand() {
        meeting.pubSub.publish("RAISE_HAND", "Raise Hand by Me", new PubSubPublishOptions());
    }

    private void toggleRecording() {
        if (!recording) {
            recordingStatusSnackbar.show();
            JSONObject config = new JSONObject();
            JSONObject layout = new JSONObject();
            JsonUtils.jsonPut(layout, "type", "SPOTLIGHT");
            JsonUtils.jsonPut(layout, "priority", "PIN");
            JsonUtils.jsonPut(layout, "gridSize", 12);
            JsonUtils.jsonPut(config, "layout", layout);
            JsonUtils.jsonPut(config, "orientation", "portrait");
            JsonUtils.jsonPut(config, "theme", "DARK");
            meeting.startRecording(null, null, config,null);

        } else {
            meeting.stopRecording();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showLeaveOrEndDialog();
    }

    @Override
    protected void onDestroy() {

        if (meeting != null) {
            meeting.removeAllListeners();
            meeting.getLocalParticipant().removeAllListeners();
            meeting.leave();
            meeting = null;
        }
        if (shareView != null) {
            shareView.setVisibility(View.GONE);
            shareLayout.setVisibility(View.GONE);
            shareView.releaseSurfaceViewRenderer();
        }

        super.onDestroy();
    }

    public void unSubscribeTopics() {
        if (meeting != null) {
            meeting.pubSub.unsubscribe("CHAT", chatListener);
            meeting.pubSub.unsubscribe("RAISE_HAND", raiseHandListener);
        }
    }

    public void openParticipantList() {
        RecyclerView participantsListView;
        ImageView close;
        bottomSheetDialog = new BottomSheetDialog(this);
        View v3 = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_participants_list_view, findViewById(R.id.layout_participants));
        bottomSheetDialog.setContentView(v3);
        participantsListView = v3.findViewById(R.id.rvParticipantsLinearView);
        ((TextView) v3.findViewById(R.id.participant_heading)).setTypeface(Roboto_font.getTypeFace(GroupCallActivity.this));
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
        participantsListView.setAdapter(new ParticipantListAdapter(participants, meeting, GroupCallActivity.this));
        participantsListView.setHasFixedSize(true);
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        (GroupCallActivity.this).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    private int getWindowWidth() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        (GroupCallActivity.this).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
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


    @SuppressLint("ClickableViewAccessibility")
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
        etmessage.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View view, MotionEvent event) {
                // TODO Auto-generated method stub
                if (view.getId() == R.id.etMessage) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

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

        etmessage.setVerticalScrollBarEnabled(true);
        etmessage.setScrollbarFadingEnabled(false);

        etmessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!etmessage.getText().toString().trim().isEmpty()) {
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
                Toast.makeText(GroupCallActivity.this, "Please Enter Message",
                        Toast.LENGTH_SHORT).show();
            }

        });


        close = v3.findViewById(R.id.ic_close);
        bottomSheetDialog.show();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                meeting.pubSub.unsubscribe("CHAT", pubSubMessageListener);
            }
        });

    }

    public void showMeetingTime() {

        runnable = new Runnable() {
            @Override
            public void run() {
                int hours = meetingSeconds / 3600;
                int minutes = (meetingSeconds % 3600) / 60;
                int secs = meetingSeconds % 60;

                // Format the seconds into minutes,seconds.
                String time = String.format(Locale.getDefault(),
                        "%02d:%02d:%02d", hours,
                        minutes, secs);

                txtMeetingTime.setText(time);

                meetingSeconds++;

                handler.postDelayed(this, 1000);
            }
        };

        handler.post(runnable);

    }

    private void showMicRequestDialog(MicRequestListener listener) {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(GroupCallActivity.this, R.style.AlertDialogCustom).create();
        alertDialog.setCanceledOnTouchOutside(false);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_layout, null);
        alertDialog.setView(dialogView);

        TextView title = (TextView) dialogView.findViewById(R.id.title);
        title.setVisibility(View.GONE);
        TextView message = (TextView) dialogView.findViewById(R.id.message);
        message.setText("Host is asking you to unmute your mic, do you want to allow ?");

        Button positiveButton = dialogView.findViewById(R.id.positiveBtn);
        positiveButton.setText("Yes");
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.accept();
                alertDialog.dismiss();
            }
        });

        Button negativeButton = dialogView.findViewById(R.id.negativeBtn);
        negativeButton.setText("No");
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.reject();
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }


    private void showWebcamRequestDialog(WebcamRequestListener listener) {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(GroupCallActivity.this, R.style.AlertDialogCustom).create();
        alertDialog.setCanceledOnTouchOutside(false);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_layout, null);
        alertDialog.setView(dialogView);

        TextView title = (TextView) dialogView.findViewById(R.id.title);
        title.setVisibility(View.GONE);
        TextView message = (TextView) dialogView.findViewById(R.id.message);
        message.setText("Host is asking you to enable your webcam, do you want to allow ?");

        Button positiveButton = dialogView.findViewById(R.id.positiveBtn);
        positiveButton.setText("Yes");
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.accept();
                alertDialog.dismiss();
            }
        });

        Button negativeButton = dialogView.findViewById(R.id.negativeBtn);
        negativeButton.setText("No");
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.reject();
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }


}