package live.videosdk.rtc.android.java;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;

import live.videosdk.rtc.android.lib.PeerConnectionUtils;

public class JoinActivity extends AppCompatActivity {

    private final String AUTH_TOKEN = BuildConfig.AUTH_TOKEN;
    private final String AUTH_URL= BuildConfig.AUTH_URL;

    private FloatingActionButton btnMic,btnWebcam;
    private boolean micEnabled=false;
    private boolean webcamEnabled=false;
    private SurfaceViewRenderer svrJoin;
    VideoTrack videoTrack;
    private EditText etName;

    boolean permissionsGranted = false;
    private final PermissionHandler permissionHandler = new PermissionHandler() {
        @Override
        public void onGranted() {
           permissionsAllowed();
        }

        @Override
        public void onDenied(Context context, ArrayList<String> deniedPermissions) {
            super.onDenied(context, deniedPermissions);
            Toast.makeText(JoinActivity.this, "Permission(s) not granted. Some feature may not work", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onBlocked(Context context, ArrayList<String> blockedList) {
            Toast.makeText(JoinActivity.this, "Permission(s) not granted. Some feature may not work", Toast.LENGTH_SHORT).show();
            return super.onBlocked(context, blockedList);
        }
    };

    private void permissionsAllowed() {
        permissionsGranted = true;
        micEnabled=true;
        webcamEnabled=true;
       btnMic.setImageResource(R.drawable.ic_outline_mic_none_24);
        btnWebcam.setImageResource(R.drawable.ic_outline_videocam_24);
        changeFloatingActionButtonLayout(btnMic,micEnabled);
        changeFloatingActionButtonLayout(btnWebcam,webcamEnabled);
        updateCameraView();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

         final Button btnJoin = findViewById(R.id.btnJoin);
         btnMic = findViewById(R.id.btnMic);
        btnWebcam = findViewById(R.id.btnWebcam);
        svrJoin = findViewById(R.id.svrJoiningView);
        etName = findViewById(R.id.etName);
        checkPermissions();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Join Meeting");
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();

        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        } else {
            throw new NullPointerException("Something went wrong");
        }

        btnMic.setOnClickListener(v -> {
            toggleMic();
        });

        btnWebcam.setOnClickListener(v -> {
            toggleWebcam();
        });

        final String token = getIntent().getStringExtra("token");
        final String meetingId = getIntent().getStringExtra("meetingId");

        btnJoin.setOnClickListener(v -> {
            if("".equals(etName.getText().toString()))
            {
                Toast.makeText(JoinActivity.this, "Please Enter Name", Toast.LENGTH_SHORT).show();
            }else {

                Intent intent = new Intent(JoinActivity.this, MainActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("meetingId", meetingId);
                intent.putExtra("micEnabled", micEnabled);
                intent.putExtra("webcamEnabled", webcamEnabled);
                intent.putExtra("paticipantName", etName.getText().toString());
                startActivity(intent);
            }
        });

    }

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

    private void changeFloatingActionButtonLayout(FloatingActionButton btn, boolean Enabled)
    {
        if(Enabled)
        {
            btn.setColorFilter(Color.BLACK);
            btn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_grey_300)));
        }else
        {
            btn.setColorFilter(Color.WHITE);
            btn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_red_500)));

        }
     }

    private void toggleMic() {
        if (!permissionsGranted) {
            checkPermissions();
            return;
        }
        micEnabled = !micEnabled;
        if (micEnabled){
            btnMic.setImageResource(R.drawable.ic_outline_mic_none_24);
        }else {
            btnMic.setImageResource(R.drawable.ic_baseline_mic_off_24);
        }
        changeFloatingActionButtonLayout(btnMic,micEnabled);
    }

    private void toggleWebcam() {
        if (!permissionsGranted) {
            checkPermissions();
            return;
        }
        webcamEnabled = !webcamEnabled;
        if (webcamEnabled) {
            btnWebcam.setImageResource(R.drawable.ic_outline_videocam_24);
        }
        else {
            btnWebcam.setImageResource(R.drawable.ic_outline_videocam_off_24);
        }
        updateCameraView();
        changeFloatingActionButtonLayout(btnWebcam,webcamEnabled);
    }


    private void updateCameraView() {
        if (webcamEnabled) {
            // create PeerConnectionFactory
            PeerConnectionFactory.InitializationOptions initializationOptions =
                    PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions();
            PeerConnectionFactory.initialize(initializationOptions);
            PeerConnectionFactory peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();


            svrJoin.init(PeerConnectionUtils.getEglContext(), null);

            SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", PeerConnectionUtils.getEglContext());
            // create VideoCapturer
            VideoCapturer videoCapturer = createCameraCapturer();
            VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
            videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
            videoCapturer.startCapture(480, 640, 30);
            // create VideoTrack
            videoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
            // display in localView
            videoTrack.addSink(svrJoin);
           } else {
            if (videoTrack != null)
                videoTrack.removeSink(svrJoin);
            svrJoin.clearImage();
            svrJoin.release();
        }
    }


    private VideoCapturer createCameraCapturer() {
        Camera1Enumerator enumerator = new Camera1Enumerator(false);
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }


}