package live.videosdk.android.onetoonedemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.DrawableCompat;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.webrtc.VideoTrack;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.Stream;
import live.videosdk.rtc.android.VideoSDK;
import live.videosdk.rtc.android.VideoView;
import live.videosdk.rtc.android.listeners.MeetingEventListener;
import live.videosdk.rtc.android.listeners.ParticipantEventListener;

public class MeetingActivity extends AppCompatActivity {

    private static Meeting meeting;
    private boolean micEnabled = true;
    private boolean webcamEnabled = true;

    private FloatingActionButton btnWebcam, btnMic, btnLeave;
    private ImageButton btnSwitchCameraMode;

    private VideoView localView;
    private VideoView participantView;
    private CardView localCard, participantCard;
    private ImageView localParticipantImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        //
        btnMic = findViewById(R.id.btnMic);
        btnWebcam = findViewById(R.id.btnWebcam);
        btnLeave = findViewById(R.id.btnLeave);
        btnSwitchCameraMode = findViewById(R.id.btnSwitchCameraMode);

        localCard = findViewById(R.id.LocalCard);
        participantCard = findViewById(R.id.ParticipantCard);
        localView = findViewById(R.id.localView);
        participantView = findViewById(R.id.participantView);
        localParticipantImg = findViewById(R.id.localParticipant_img);

        //
        Toolbar toolbar = findViewById(R.id.material_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //
        String token = getIntent().getStringExtra("token");
        final String meetingId = getIntent().getStringExtra("meetingId");

        // set participant name
        String localParticipantName = "Alex";

        // Initialize VideoSDK
        VideoSDK.initialize(getApplicationContext());

        // pass the token generated from api server
        VideoSDK.config(token);

        // create a new meeting instance
        meeting = VideoSDK.initMeeting(
                MeetingActivity.this, meetingId, localParticipantName,
                micEnabled, webcamEnabled, null, null
        );

        // join the meeting
        if (meeting != null) meeting.join();

        //
        TextView textMeetingId = findViewById(R.id.txtMeetingId);
        textMeetingId.setText(meetingId);

        // copy meetingId to clipboard
        ((ImageButton) findViewById(R.id.btnCopyContent)).setOnClickListener(v -> copyTextToClipboard(meetingId));

        // actions
        setActionListeners();

        // setup local participant view
        setLocalListeners();

        // handle meeting events
        meeting.addEventListener(meetingEventListener);
    }

    private void copyTextToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied text", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(MeetingActivity.this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
    }

    private void setActionListeners() {
        // Toggle mic
        btnMic.setOnClickListener(view -> toggleMic());

        // Toggle webcam
        btnWebcam.setOnClickListener(view -> toggleWebCam());

        // Leave meeting
        btnLeave.setOnClickListener(view -> {
            // this will make the local participant leave the meeting
            meeting.leave();
        });

        // Switch camera
        btnSwitchCameraMode.setOnClickListener(view -> {
            //a participant can change stream from front/rear camera during the meeting.
            meeting.changeWebcam();
        });

    }

    private void toggleMic() {
        if (micEnabled) {
            // this will mute the local participant's mic
            meeting.muteMic();
        } else {
            // this will unmute the local participant's mic
            meeting.unmuteMic();
        }
        micEnabled = !micEnabled;
        // change mic icon according to micEnable status
        toggleMicIcon();
    }

    @SuppressLint("ResourceType")
    private void toggleMicIcon() {
        if (micEnabled) {
            btnMic.setImageResource(R.drawable.ic_mic_on);
            btnMic.setColorFilter(Color.WHITE);
            Drawable buttonDrawable = btnMic.getBackground();
            buttonDrawable = DrawableCompat.wrap(buttonDrawable);
            //the color is a direct color int and not a color resource
            if (buttonDrawable != null) DrawableCompat.setTint(buttonDrawable, Color.TRANSPARENT);
            btnMic.setBackground(buttonDrawable);

        } else {
            btnMic.setImageResource(R.drawable.ic_mic_off);
            btnMic.setColorFilter(Color.BLACK);
            Drawable buttonDrawable = btnMic.getBackground();
            buttonDrawable = DrawableCompat.wrap(buttonDrawable);
            //the color is a direct color int and not a color resource
            if (buttonDrawable != null) DrawableCompat.setTint(buttonDrawable, Color.WHITE);
            btnMic.setBackground(buttonDrawable);
        }
    }

    private void toggleWebCam() {
        if (webcamEnabled) {
            // this will disable the local participant webcam
            meeting.disableWebcam();
        } else {
            // this will enable the local participant webcam
            meeting.enableWebcam();
        }
        webcamEnabled = !webcamEnabled;
        // change webCam icon according to webcamEnabled status
        toggleWebcamIcon();
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

    private void setLocalListeners() {
        meeting.getLocalParticipant().addEventListener(new ParticipantEventListener() {
            @Override
            public void onStreamEnabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    localView.setVisibility(View.VISIBLE);
                    localView.addTrack(track);
                    localView.setZOrderMediaOverlay(true);
                    localCard.bringToFront();
                }
            }

            @Override
            public void onStreamDisabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    localView.removeTrack();
                    localView.setVisibility(View.GONE);
                }
            }
        });
    }

    private final ParticipantEventListener participantEventListener = new ParticipantEventListener() {
        // trigger when participant enabled mic/webcam
        @Override
        public void onStreamEnabled(Stream stream) {
            if (stream.getKind().equalsIgnoreCase("video")) {
                localView.setZOrderMediaOverlay(true);
                localCard.bringToFront();
                VideoTrack track = (VideoTrack) stream.getTrack();
                participantView.setVisibility(View.VISIBLE);
                participantView.addTrack(track);
            }
        }

        // trigger when participant disabled mic/webcam
        @Override
        public void onStreamDisabled(Stream stream) {
            if (stream.getKind().equalsIgnoreCase("video")) {
                participantView.removeTrack();
                participantView.setVisibility(View.GONE);
            }
        }
    };

    private final MeetingEventListener meetingEventListener = new MeetingEventListener() {
        @Override
        public void onMeetingJoined() {
            // change mic,webCam icon after meeting successfully joined
            toggleMicIcon();
            toggleWebcamIcon();
        }

        @Override
        public void onMeetingLeft() {
            if (!isDestroyed()) {
                Intent intent = new Intent(MeetingActivity.this, JoinActivity.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        public void onParticipantJoined(Participant participant) {
            // Display local participant as miniView when other participant joined
            changeLocalParticipantView(true);
            Toast.makeText(MeetingActivity.this, participant.getDisplayName() + " joined",
                    Toast.LENGTH_SHORT).show();
            participant.addEventListener(participantEventListener);
        }

        @Override
        public void onParticipantLeft(Participant participant) {
            // Display local participant as largeView when other participant left
            changeLocalParticipantView(false);
            Toast.makeText(MeetingActivity.this, participant.getDisplayName() + " left",
                    Toast.LENGTH_SHORT).show();
        }
    };

    private void changeLocalParticipantView(boolean isMiniView) {
        if (isMiniView) {
            // show localCard as miniView
            localCard.setLayoutParams(new CardView.LayoutParams(300, 430, Gravity.RIGHT | Gravity.BOTTOM));
            ViewGroup.MarginLayoutParams cardViewMarginParams = (ViewGroup.MarginLayoutParams) localCard.getLayoutParams();
            cardViewMarginParams.setMargins(30, 0, 60, 40);
            localCard.requestLayout();
            // set height-width of localParticipant_img
            localParticipantImg.setLayoutParams(new FrameLayout.LayoutParams(150, 150, Gravity.CENTER));
            participantCard.setVisibility(View.VISIBLE);
        } else {
            // show localCard as largeView
            localCard.setLayoutParams(new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ViewGroup.MarginLayoutParams cardViewMarginParams = (ViewGroup.MarginLayoutParams) localCard.getLayoutParams();
            cardViewMarginParams.setMargins(30, 5, 30, 30);
            localCard.requestLayout();
            // set height-width of localParticipant_img
            localParticipantImg.setLayoutParams(new FrameLayout.LayoutParams(400, 400, Gravity.CENTER));
            participantCard.setVisibility(View.GONE);
        }
    }

    protected void onDestroy() {
        if (meeting != null) {
            meeting.removeAllListeners();
            meeting.getLocalParticipant().removeAllListeners();
            meeting.leave();
            meeting = null;
        }
        if (participantView != null) {
            participantView.setVisibility(View.GONE);
            participantView.releaseSurfaceViewRenderer();
        }

        if (localView != null) {
            localView.setVisibility(View.GONE);
            localView.releaseSurfaceViewRenderer();
        }

        super.onDestroy();
    }

}