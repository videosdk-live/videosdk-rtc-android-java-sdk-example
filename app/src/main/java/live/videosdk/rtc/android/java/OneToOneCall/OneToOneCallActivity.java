package live.videosdk.rtc.android.java.OneToOneCall;

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

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

import live.videosdk.rtc.android.CustomStreamTrack;
import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.Stream;
import live.videosdk.rtc.android.VideoSDK;
import live.videosdk.rtc.android.VideoView;
import live.videosdk.rtc.android.java.Common.Activity.CreateOrJoinActivity;
import live.videosdk.rtc.android.java.Common.Adapter.AudioDeviceListAdapter;
import live.videosdk.rtc.android.java.Common.Adapter.LeaveOptionListAdapter;
import live.videosdk.rtc.android.java.Common.Adapter.MessageAdapter;
import live.videosdk.rtc.android.java.Common.Adapter.MoreOptionsListAdapter;
import live.videosdk.rtc.android.java.Common.Adapter.ParticipantListAdapter;
import live.videosdk.rtc.android.java.Common.Listener.ResponseListener;
import live.videosdk.rtc.android.java.Common.Modal.ListItem;
import live.videosdk.rtc.android.java.GroupCall.Utils.ParticipantState;
import live.videosdk.rtc.android.java.R;
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
import live.videosdk.rtc.android.permission.PermissionHandler;
import live.videosdk.rtc.android.permission.Permissions;

public class OneToOneCallActivity extends AppCompatActivity {
    private static Meeting meeting;
    private VideoView localVideoView;
    private VideoView participantVideoView;
    private FloatingActionButton btnWebcam;
    private ImageButton btnMic, btnAudioSelection, btnSwitchCameraMode;
    private FloatingActionButton btnLeave, btnChat, btnMore;
    private CardView localCard, participantCard;
    private LinearLayout micLayout;
    ArrayList<Participant> participants;
    private ImageView ivParticipantMicStatus;

    private ImageView ivLocalNetwork, ivParticipantNetwork, ivLocalScreenShareNetwork;
    private PopupWindow popupwindow_obj_local, popupwindow_obj;
    private TextView txtLocalParticipantName, txtParticipantName, tvName, tvLocalParticipantName;
    private String participantName;

    private VideoTrack participantTrack = null;

    private boolean micEnabled = true;
    private boolean webcamEnabled = true;
    private boolean recording = false;
    private boolean localScreenShare = false;
    private boolean fullScreen = false;
    private static String token;
    int clickCount = 0;
    long startTime;
    static final int MAX_DURATION = 500;
    private Snackbar recordingStatusSnackbar;


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
    private int meetingSeconds;
    private TextView txtMeetingTime;
    private Snackbar screenShareParticipantNameSnackbar;

    private Runnable runnable;
    final Handler handler = new Handler();

    private PubSubMessageListener chatListener;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_to_one_call);
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
        localCard = findViewById(R.id.LocalCard);
        participantCard = findViewById(R.id.ParticipantCard);

        txtLocalParticipantName = findViewById(R.id.txtLocalParticipantName);
        txtParticipantName = findViewById(R.id.txtParticipantName);
        tvName = findViewById(R.id.tvName);
        tvLocalParticipantName = findViewById(R.id.tvLocalParticipantName);

        localVideoView = findViewById(R.id.localVideoView);
        localVideoView.setMirror(true);

        participantVideoView = findViewById(R.id.participantVideoView);

        btnMic = findViewById(R.id.btnMic);
        btnWebcam = findViewById(R.id.btnWebcam);
        btnAudioSelection = findViewById(R.id.btnAudioSelection);
        txtMeetingTime = findViewById(R.id.txtMeetingTime);

        ivLocalNetwork = findViewById(R.id.ivLocalNetwork);
        ivParticipantNetwork = findViewById(R.id.ivParticipantNetwork);
        ivLocalScreenShareNetwork = findViewById(R.id.ivLocalScreenShareNetwork);


        ivParticipantMicStatus = findViewById(R.id.ivParticipantMicStatus);

        token = getIntent().getStringExtra("token");
        final String meetingId = getIntent().getStringExtra("meetingId");
        micEnabled = getIntent().getBooleanExtra("micEnabled", true);
        webcamEnabled = getIntent().getBooleanExtra("webcamEnabled", true);

        String localParticipantName = getIntent().getStringExtra("participantName");
        if (localParticipantName == null) {
            localParticipantName = "John Doe";
        }
        txtLocalParticipantName.setText(localParticipantName.substring(0, 1));
        tvLocalParticipantName.setText("You");
        //

        TextView textMeetingId = findViewById(R.id.txtMeetingId);
        textMeetingId.setText(meetingId);

        // pass the token generated from api server
        VideoSDK.config(token);

        Map<String, CustomStreamTrack> customTracks = new HashMap<>();

        CustomStreamTrack videoCustomTrack = VideoSDK.createCameraVideoTrack("h720p_w960p", "front", CustomStreamTrack.VideoMode.TEXT, true,this,VideoSDK.getSelectedVideoDevice());
        customTracks.put("video", videoCustomTrack);

        CustomStreamTrack audioCustomTrack = VideoSDK.createAudioTrack("high_quality", this);
        customTracks.put("mic", audioCustomTrack);

        // create a new meeting instance
        meeting = VideoSDK.initMeeting(
                OneToOneCallActivity.this, meetingId, localParticipantName,false,
                false, null, null, false, customTracks,null
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
        HelperClass.setSnackBarStyle(recordingStatusSnackbar.getView(), 0);
        recordingStatusSnackbar.setGestureInsetBottomIgnored(true);

        findViewById(R.id.btnStopScreenShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (localScreenShare) {
                    meeting.disableScreenShare();
                }
            }
        });

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
                                    toolbar.setVisibility(View.VISIBLE);
                                    for (int i = 0; i < toolbar.getChildCount(); i++) {
                                        toolbar.getChildAt(i).setVisibility(View.VISIBLE);
                                    }

                                    Toolbar.LayoutParams params = new Toolbar.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                    );
                                    params.setMargins(30, 10, 0, 0);
                                    findViewById(R.id.meetingLayout).setLayoutParams(params);

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
        });

        ivLocalNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupwindow_obj_local = HelperClass.callStatsPopupDisplay(meeting.getLocalParticipant(), ivLocalNetwork, OneToOneCallActivity.this, false);
                if (getAllParticipants().size() == 0) {
                    if (screenshareEnabled)
                        popupwindow_obj_local.showAsDropDown(ivLocalNetwork, 100, -380);
                    else
                        popupwindow_obj_local.showAsDropDown(ivLocalNetwork, -350, -85);
                } else {
                    if (screenshareEnabled) {
                        ArrayList<Participant> participantList = getAllParticipants();
                        Participant participant = participantList.get(0);
                        popupwindow_obj_local = HelperClass.callStatsPopupDisplay(participant, ivLocalNetwork, OneToOneCallActivity.this, false);
                    }
                    popupwindow_obj_local.showAsDropDown(ivLocalNetwork, 100, -380);
                }
            }
        });

        ivParticipantNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Participant> participantList = getAllParticipants();
                Participant participant = participantList.get(0);
                popupwindow_obj = HelperClass.callStatsPopupDisplay(participant, ivParticipantNetwork, OneToOneCallActivity.this, screenshareEnabled);
                popupwindow_obj.showAsDropDown(ivParticipantNetwork, -350, -85);
            }
        });

        ivLocalScreenShareNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupwindow_obj = HelperClass.callStatsPopupDisplay(meeting.getLocalParticipant(), ivLocalScreenShareNetwork, OneToOneCallActivity.this, true);
                popupwindow_obj.showAsDropDown(ivLocalScreenShareNetwork, -350, -85);
            }
        });
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

            if (meeting != null) {
                //hide progress when meetingJoined
                HelperClass.hideProgress(getWindow().getDecorView().getRootView());

                localCard.setVisibility(View.VISIBLE);

                // if more than 2 participant join than leave the meeting
                if (meeting.getParticipants().size() <= 1) {
                    toggleMicIcon(micEnabled);

                    micEnabled = !micEnabled;
                    webcamEnabled = !webcamEnabled;

                    toggleMic();
                    toggleWebCam();

                    // Local participant listeners
                    setLocalListeners();


                    new NetworkUtils(OneToOneCallActivity.this).fetchMeetingTime(meeting.getMeetingId(), token, new ResponseListener<Integer>() {
                        @Override
                        public void onResponse(Integer meetingTime) {
                            meetingSeconds = meetingTime;
                            showMeetingTime();
                        }
                    });

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
                                AlertDialog alertDialog = new MaterialAlertDialogBuilder(OneToOneCallActivity.this, R.style.AlertDialogCustom).create();
                                alertDialog.setCanceledOnTouchOutside(false);

                                LayoutInflater inflater = OneToOneCallActivity.this.getLayoutInflater();
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
        }

        @Override
        public void onMeetingLeft() {
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
                showParticipantCard();
                txtParticipantName.setText(participant.getDisplayName().substring(0, 1));
                participantName = participant.getDisplayName();
                tvName.setText(participantName);
                if (popupwindow_obj_local != null && popupwindow_obj_local.isShowing())
                    popupwindow_obj_local.dismiss();
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
                    if (participantTrack != null) localVideoView.removeTrack();
                    localVideoView.setVisibility(View.GONE);
                    showParticipantCard();
                    if (localTrack != null) {
                        localVideoView.addTrack(localTrack);
                        localVideoView.setVisibility(View.VISIBLE);
                    }
                    participantVideoView.addTrack(screenshareTrack);
                    participantVideoView.setVisibility(View.VISIBLE);
                }
                if (popupwindow_obj != null && popupwindow_obj.isShowing())
                    popupwindow_obj.dismiss();
                if (popupwindow_obj_local != null && popupwindow_obj_local.isShowing())
                    popupwindow_obj_local.dismiss();
                Toast.makeText(OneToOneCallActivity.this, participant.getDisplayName() + " left",
                        Toast.LENGTH_SHORT).show();
            }

            participant.removeAllListeners();
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
            Toast.makeText(OneToOneCallActivity.this, "Recording started",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRecordingStopped() {
            recording = false;

            (findViewById(R.id.recordingLottie)).setVisibility(View.GONE);

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
//            if (!HelperClass.isNullOrEmpty(participantId)) {
//                if (participantId.equals(meeting.getLocalParticipant().getId())) {
//                    img_localActiveSpeaker.setVisibility(View.VISIBLE);
//                    img_participantActiveSpeaker.setVisibility(View.GONE);
//                } else {
//                    img_participantActiveSpeaker.setVisibility(View.VISIBLE);
//                    img_localActiveSpeaker.setVisibility(View.GONE);
//                }
//            } else {
//                img_participantActiveSpeaker.setVisibility(View.GONE);
//                img_localActiveSpeaker.setVisibility(View.GONE);
//            }
        }

        @Override
        public void onMeetingStateChanged(String state) {
            if (state == "FAILED" ) {
                View parentLayout = findViewById(android.R.id.content);
                SpannableStringBuilder builderTextLeft = new SpannableStringBuilder();
                builderTextLeft.append("   Call disconnected. Reconnecting...");
                builderTextLeft.setSpan(new ImageSpan(OneToOneCallActivity.this, R.drawable.ic_call_disconnected), 0, 1, 0);
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

    private void showParticipantCard() {
        localCard.setLayoutParams(new CardView.LayoutParams(getWindowWidth() / 4, getWindowHeight() / 5, Gravity.RIGHT | Gravity.BOTTOM));
        ViewGroup.MarginLayoutParams cardViewMarginParams = (ViewGroup.MarginLayoutParams) localCard.getLayoutParams();
        cardViewMarginParams.setMargins(30, 0, 60, 40);
        localCard.requestLayout();
        txtLocalParticipantName.setLayoutParams(new FrameLayout.LayoutParams(120, 120, Gravity.CENTER));
        txtLocalParticipantName.setTextSize(24);
        txtLocalParticipantName.setGravity(Gravity.CENTER);
        tvLocalParticipantName.setVisibility(View.GONE);
//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(50, 50, Gravity.RIGHT);
//        layoutParams.setMargins(0, 12, 12, 0);
//        img_localActiveSpeaker.setLayoutParams(layoutParams);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            txtLocalParticipantName.setForegroundGravity(Gravity.CENTER);
        }

        participantCard.setVisibility(View.VISIBLE);

    }

    private void hideParticipantCard() {
        localCard.setLayoutParams(new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ViewGroup.MarginLayoutParams cardViewMarginParams = (ViewGroup.MarginLayoutParams) localCard.getLayoutParams();
        cardViewMarginParams.setMargins(30, 5, 30, 30);
        localCard.requestLayout();
        txtLocalParticipantName.setLayoutParams(new FrameLayout.LayoutParams(220, 220, Gravity.CENTER));
        txtLocalParticipantName.setTextSize(40);
        txtLocalParticipantName.setGravity(Gravity.CENTER);
        tvLocalParticipantName.setVisibility(View.VISIBLE);

//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(75, 75, Gravity.RIGHT);
//        layoutParams.setMargins(0, 30, 30, 0);
//        img_localActiveSpeaker.setLayoutParams(layoutParams);
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
        if (participantName != null)
            txtLocalParticipantName.setText(participantName.substring(0, 1));

        tvName.setText(participantName + " is presenting");
        ivParticipantMicStatus.setVisibility(View.GONE);
        ivParticipantNetwork.setVisibility(View.VISIBLE);
        onTrackChange();
        checkStream(participant, ivLocalNetwork);


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
                    screenshareTrack = null;
                    participantVideoView.removeTrack();
                    participantVideoView.setVisibility(View.GONE);
                    removeTrack(participantTrack, true);
                    txtLocalParticipantName.setText(meeting.getLocalParticipant().getDisplayName().substring(0, 1));
                    tvName.setText(participantName);
                    ivParticipantMicStatus.setVisibility(View.VISIBLE);
                    onTrackChange();
                    checkStream(participant, ivParticipantNetwork);
                    checkStream(meeting.getLocalParticipant(), ivLocalNetwork);
                    screenshareEnabled = false;
                    localScreenShare = false;
                }
            }
        });
    }
    private final com.nabinbhandari.android.permissions.PermissionHandler permissionHandler = new com.nabinbhandari.android.permissions.PermissionHandler() {
        @Override
        public void onGranted() {

        }

        @Override
        public void onDenied(Context context, ArrayList<String> deniedPermissions) {
            super.onDenied(context, deniedPermissions);
            Toast.makeText(OneToOneCallActivity.this,
                    "Permission(s) not granted. Some feature may not work", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onBlocked(Context context, ArrayList<String> blockedList) {
            Toast.makeText(OneToOneCallActivity.this,
                    "Permission(s) not granted. Some feature may not work", Toast.LENGTH_SHORT).show();
            return super.onBlocked(context, blockedList);
        }
    };


    private PermissionHandler permissionHandlerSDK = new PermissionHandler() {
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

        Permissions.Options optionsSDK = new Permissions.Options()
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

        Toast.makeText(OneToOneCallActivity.this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
    }

    private void toggleMic() {
        if (micEnabled) {
            meeting.muteMic();
        } else {
            CustomStreamTrack audioCustomTrack = VideoSDK.createAudioTrack("high_quality", this);

            meeting.unmuteMic(audioCustomTrack);
        }
        micEnabled = !micEnabled;
    }

    private void toggleWebCam() {
        if (webcamEnabled) {
            meeting.disableWebcam();
        } else {
            CustomStreamTrack videoCustomTrack = VideoSDK.createCameraVideoTrack("h720p_w960p", "front", CustomStreamTrack.VideoMode.DETAIL, true,this,VideoSDK.getSelectedVideoDevice()
            );
            meeting.enableWebcam(videoCustomTrack);
        }
        webcamEnabled = !webcamEnabled;
        toggleWebcamIcon(webcamEnabled);

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
                            unSubscribeTopics();
                            meeting.leave();
                            break;
                        }
                        case 1: {
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

                    meeting.changeMic(audioDevice, VideoSDK.createAudioTrack("high_quality", this));

                });

        AlertDialog alertDialog = materialAlertDialogBuilder.create();

        ListView listView = alertDialog.getListView();
        listView.setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.md_grey_200))); // set color
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
        listView.setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.md_grey_200))); // set color
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
            JSONObject config = new JSONObject();
            JSONObject layout = new JSONObject();
            JsonUtils.jsonPut(layout, "type", "SPOTLIGHT");
            JsonUtils.jsonPut(layout, "priority", "PIN");
            JsonUtils.jsonPut(layout, "gridSize", 12);
            JsonUtils.jsonPut(config, "layout", layout);
            JsonUtils.jsonPut(config, "orientation", "portrait");
            JsonUtils.jsonPut(config, "theme", "DARK");
            meeting.startRecording(null,null,config,null);

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

        if (participantVideoView != null) {
            participantVideoView.setVisibility(View.GONE);
            participantVideoView.releaseSurfaceViewRenderer();
        }

        if (localVideoView != null) {
            localVideoView.setVisibility(View.GONE);
            localVideoView.releaseSurfaceViewRenderer();
        }

        timer.cancel();

        super.onDestroy();
    }

    private void unSubscribeTopics() {
        if (meeting != null) {
            meeting.pubSub.unsubscribe("CHAT", chatListener);
        }

    }

    private void onTrackChange() {
        if (screenshareTrack != null) {

            if (meeting.getParticipants().size() == 0) {
                showParticipantCard();
                if (localTrack != null) {
                    localVideoView.addTrack(localTrack);
                    localVideoView.setVisibility(View.VISIBLE);
                }

            } else {
                if (localTrack != null) {
                    localVideoView.removeTrack();
                    localVideoView.setVisibility(View.GONE);
                }

                if (participantTrack != null) {
                    participantVideoView.removeTrack();
                    participantVideoView.clearImage();
                    localVideoView.addTrack(participantTrack);
                    if (participantName != null)
                        txtLocalParticipantName.setText(participantName.substring(0, 1));
                    localVideoView.setVisibility(View.VISIBLE);
                }
            }
            if (localScreenShare) {
                participantCard.setVisibility(View.GONE);
                findViewById(R.id.localScreenShareView).setVisibility(View.VISIBLE);
            } else {
                participantVideoView.addTrack(screenshareTrack);
                participantVideoView.setVisibility(View.VISIBLE);
            }
        } else {

            if (participantTrack != null) {
                participantVideoView.setVisibility(View.VISIBLE);
                participantVideoView.addTrack(participantTrack);
//                ((View) img_participantActiveSpeaker).bringToFront();
            }
            if (localTrack != null) {
                localVideoView.setVisibility(View.VISIBLE);
                localVideoView.setZOrderMediaOverlay(true);
                localVideoView.addTrack(localTrack);
//                ((View) img_localActiveSpeaker).bringToFront();
                ((View) localCard).bringToFront();

            }

        }
    }

    private void checkStream(Participant participant, ImageView imageView) {
        if (!participant.getStreams().isEmpty()) {
            for (Map.Entry<String, Stream> entry : participant.getStreams().entrySet()) {
                Stream stream = entry.getValue();
                if (stream.getKind().equalsIgnoreCase("video") || stream.getKind().equalsIgnoreCase("audio")) {
                    imageView.setVisibility(View.VISIBLE);
                    break;
                } else {
                    imageView.setVisibility(View.GONE);
                }
            }
        } else
            imageView.setVisibility(View.GONE);
    }

    private void removeTrack(VideoTrack track, Boolean isLocal) {
        if (screenshareTrack == null) {
            findViewById(R.id.localScreenShareView).setVisibility(View.GONE);
            participantCard.setVisibility(View.VISIBLE);
            if (isLocal) {
                if (track != null) localVideoView.removeTrack();
                localVideoView.setVisibility(View.GONE);
            } else {
                if (track != null) participantVideoView.removeTrack();
                participantVideoView.setVisibility(View.GONE);
            }
        } else {
            if (!isLocal) {
                if (track != null) localVideoView.removeTrack();
                localVideoView.setVisibility(View.GONE);
                onTrackChange();
            } else {
                if (meeting.getParticipants().size() == 0) {
                    if (track != null) localVideoView.removeTrack();
                    localVideoView.setVisibility(View.GONE);
                } else {
                    if (track != null) participantVideoView.removeTrack();
                    participantVideoView.setVisibility(View.GONE);
                    onTrackChange();
                }
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
                    setQuality("high");
                    ArrayList<Participant> participantList = getAllParticipants();
                    Participant participant = participantList.get(0);
                    if (screenshareEnabled) {
                        checkStream(participant, ivLocalNetwork);
                    } else
                        checkStream(participant, ivParticipantNetwork);
                }
            }
            if (stream.getKind().equalsIgnoreCase("audio")) {
                if (meeting.getParticipants().size() >= 2) {
                    stream.pause();
                } else {
                    ivParticipantMicStatus.setImageResource(R.drawable.ic_audio_on);
                    ArrayList<Participant> participantList = getAllParticipants();
                    Participant participant = participantList.get(0);
                    if (screenshareEnabled) {
                        checkStream(participant, ivLocalNetwork);
                    } else
                        checkStream(participant, ivParticipantNetwork);
                }
            }
        }

        @Override
        public void onStreamDisabled(Stream stream) {
            if (stream.getKind().equalsIgnoreCase("video")) {
                if (meeting.getParticipants().size() < 2) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    if (track != null) participantTrack = null;
                    removeTrack(track, false);
                    ArrayList<Participant> participantList = getAllParticipants();
                    Participant participant = participantList.get(0);
                    if (screenshareEnabled) {
                        checkStream(participant, ivLocalNetwork);
                    } else
                        checkStream(participant, ivParticipantNetwork);

                }
            }
            if (stream.getKind().equalsIgnoreCase("audio")) {
                if (meeting.getParticipants().size() >= 2) {
                    stream.pause();
                } else {
                    ivParticipantMicStatus.setImageResource(R.drawable.ic_audio_off);
                    ArrayList<Participant> participantList = getAllParticipants();
                    Participant participant = participantList.get(0);
                    if (screenshareEnabled) {
                        checkStream(participant, ivLocalNetwork);
                    } else
                        checkStream(participant, ivParticipantNetwork);
                }
            }
        }
    };

    private void setQuality(String quality) {
        final Iterator<Participant> participants = meeting.getParticipants().values().iterator();

        for (int i = 0; i < meeting.getParticipants().size(); i++) {
            Participant participant = participants.next();
            participant.setQuality(quality);
        }
    }

    private void setLocalListeners() {
        meeting.getLocalParticipant().addEventListener(new ParticipantEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onStreamEnabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    localTrack = track;
                    onTrackChange();
                    if (screenshareEnabled) {
                        if (getAllParticipants().size() == 0) {
                            checkStream(meeting.getLocalParticipant(), ivLocalNetwork);
                        }
                    } else
                        checkStream(meeting.getLocalParticipant(), ivLocalNetwork);
                } else if (stream.getKind().equalsIgnoreCase("audio")) {
//                    ivLocalParticipantMicStatus.setImageResource(R.drawable.ic_audio_on);
                    if (screenshareEnabled) {
                        if (getAllParticipants().size() == 0) {
                            checkStream(meeting.getLocalParticipant(), ivLocalNetwork);
                        }
                    } else
                        checkStream(meeting.getLocalParticipant(), ivLocalNetwork);

                    toggleMicIcon(true);
                } else if (stream.getKind().equalsIgnoreCase("share")) {
                    // display share video
                    VideoTrack videoTrack = (VideoTrack) stream.getTrack();
                    screenshareTrack = videoTrack;
                    if (participantName != null)
                        txtLocalParticipantName.setText(participantName.substring(0, 1));

                    tvName.setVisibility(View.GONE);

                    screenShareParticipantNameSnackbar = Snackbar.make(findViewById(R.id.mainLayout), "You started presenting",
                            Snackbar.LENGTH_SHORT);
                    HelperClass.setSnackBarStyle(screenShareParticipantNameSnackbar.getView(), 0);
                    screenShareParticipantNameSnackbar.setGestureInsetBottomIgnored(true);
                    screenShareParticipantNameSnackbar.getView().setOnClickListener(view -> screenShareParticipantNameSnackbar.dismiss());
                    screenShareParticipantNameSnackbar.show();

                    ivParticipantMicStatus.setVisibility(View.GONE);
                    if (screenshareEnabled) {
                        if (getAllParticipants().size() == 0) {
                            checkStream(meeting.getLocalParticipant(), ivLocalNetwork);
                        } else {
                            ArrayList<Participant> participantList = getAllParticipants();
                            Participant participant = participantList.get(0);
                            checkStream(participant, ivLocalNetwork);
                        }
                    } else {
                        if (getAllParticipants().size() == 0)
                            checkStream(meeting.getLocalParticipant(), ivLocalNetwork);
                        else {
                            ArrayList<Participant> participantList = getAllParticipants();
                            Participant participant = participantList.get(0);
                            checkStream(participant, ivLocalNetwork);
                        }
                    }

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
                    toggleWebcamIcon(false);
                    if (screenshareEnabled) {
                        if (getAllParticipants().size() == 0) {
                            checkStream(meeting.getLocalParticipant(), ivLocalNetwork);
                        }
                    } else
                        checkStream(meeting.getLocalParticipant(), ivLocalNetwork);

                } else if (stream.getKind().equalsIgnoreCase("audio")) {
//                    ivLocalParticipantMicStatus.setImageResource(R.drawable.ic_audio_off);
                    toggleMicIcon(false);
                    if (screenshareEnabled) {
                        if (getAllParticipants().size() == 0) {
                            checkStream(meeting.getLocalParticipant(), ivLocalNetwork);
                        }
                    } else
                        checkStream(meeting.getLocalParticipant(), ivLocalNetwork);

                } else if (stream.getKind().equalsIgnoreCase("share")) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    if (track != null) screenshareTrack = null;
                    participantVideoView.removeTrack();
                    participantVideoView.setVisibility(View.GONE);
                    if (meeting.getParticipants().size() == 0) hideParticipantCard();

                    removeTrack(participantTrack, true);
                    txtLocalParticipantName.setText(meeting.getLocalParticipant().getDisplayName().substring(0, 1));
                    tvName.setVisibility(View.VISIBLE);
                    ivParticipantMicStatus.setVisibility(View.VISIBLE);
                    checkStream(meeting.getLocalParticipant(), ivLocalNetwork);
                    if (getAllParticipants().size() > 0) {
                        ArrayList<Participant> participantList = getAllParticipants();
                        Participant participant = participantList.get(0);
                        checkStream(participant, ivParticipantNetwork);
                    }
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
        participantsListView.setAdapter(new ParticipantListAdapter(participants, meeting, OneToOneCallActivity.this));
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
                Toast.makeText(OneToOneCallActivity.this, "Please Enter Message",
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
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(OneToOneCallActivity.this, R.style.AlertDialogCustom).create();
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
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(OneToOneCallActivity.this, R.style.AlertDialogCustom).create();
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