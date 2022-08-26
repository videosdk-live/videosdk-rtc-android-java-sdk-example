package live.videosdk.rtc.android.java.Activity;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.webrtc.Camera1Enumerator;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;

import live.videosdk.rtc.android.java.R;
import live.videosdk.rtc.android.java.fragment.CreateOrJoinFragment;
import live.videosdk.rtc.android.java.fragment.JoinMeetingFragment;
import live.videosdk.rtc.android.java.fragment.CreateMeetingFragment;
import live.videosdk.rtc.android.lib.PeerConnectionUtils;

public class CreateOrJoinActivity extends AppCompatActivity {

    private boolean micEnabled = false;
    private boolean webcamEnabled = false;

    private FloatingActionButton btnMic, btnWebcam;
    private SurfaceViewRenderer svrJoin;

    Toolbar toolbar;
    ActionBar actionBar;

    VideoTrack videoTrack;
    VideoCapturer videoCapturer;
    PeerConnectionFactory.InitializationOptions initializationOptions;
    PeerConnectionFactory peerConnectionFactory;
    VideoSource videoSource;

    boolean permissionsGranted = false;
    private final PermissionHandler permissionHandler = new PermissionHandler() {
        @Override
        public void onGranted() {
            permissionsGranted = true;

            micEnabled = true;
            btnMic.setImageResource(R.drawable.ic_mic_on);
            changeFloatingActionButtonLayout(btnMic, micEnabled);

            webcamEnabled = true;
            btnWebcam.setImageResource(R.drawable.ic_video_camera);
            changeFloatingActionButtonLayout(btnWebcam, webcamEnabled);

            updateCameraView();
        }

        @Override
        public void onDenied(Context context, ArrayList<String> deniedPermissions) {
            super.onDenied(context, deniedPermissions);
            Toast.makeText(CreateOrJoinActivity.this,
                    "Permission(s) not granted. Some feature may not work", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onBlocked(Context context, ArrayList<String> blockedList) {
            Toast.makeText(CreateOrJoinActivity.this,
                    "Permission(s) not granted. Some feature may not work", Toast.LENGTH_SHORT).show();
            return super.onBlocked(context, blockedList);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_join);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("VideoSDK RTC");

        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        btnMic = findViewById(R.id.btnMic);
        btnWebcam = findViewById(R.id.btnWebcam);
        svrJoin = findViewById(R.id.svrJoiningView);

        checkPermissions();

        LinearLayout fragContainer = (LinearLayout) findViewById(R.id.fragContainer);

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);

        getFragmentManager().beginTransaction().add(R.id.fragContainer, new CreateOrJoinFragment(), "CreateOrJoinFragment").commit();

        fragContainer.addView(ll);

        btnMic.setOnClickListener(v ->
        {
            toggleMic();
        });

        btnWebcam.setOnClickListener(v ->
        {
            toggleWebcam();
        });

    }

    public boolean isMicEnabled() {
        return micEnabled;
    }

    public boolean isWebcamEnabled() {
        return webcamEnabled;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    if (getFragmentManager().getBackStackEntryCount() > 0) {
                        actionBar.setDisplayHomeAsUpEnabled(true);
                    } else {
                        actionBar.setDisplayHomeAsUpEnabled(false);
                    }
                    toolbar.invalidate();
                }
            });

            getFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(item);
    }

    public void CreateMeetingFragment() {
        setActionBar();

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragContainer, new CreateMeetingFragment(), "NameFragment");
        ft.addToBackStack("CreateOrJoinFragment");
        ft.commit();
    }

    public void joinMeetingFragment() {
        setActionBar();

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragContainer, new JoinMeetingFragment(), "JoinMeetingFragment");
        ft.addToBackStack("CreateOrJoinFragment");
        ft.commit();
    }

    public void setActionBar() {
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        } else {
            throw new NullPointerException("Something went wrong");
        }
    }

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

    private void changeFloatingActionButtonLayout(FloatingActionButton btn, boolean enabled) {
        if (enabled) {
            btn.setColorFilter(Color.BLACK);
            btn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_grey_300)));
        } else {
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
        if (micEnabled) {
            btnMic.setImageResource(R.drawable.ic_mic_on);
        } else {
            btnMic.setImageResource(R.drawable.ic_mic_off);
        }
        changeFloatingActionButtonLayout(btnMic, micEnabled);
    }

    private void toggleWebcam() {
        if (!permissionsGranted) {
            checkPermissions();
            return;
        }
        webcamEnabled = !webcamEnabled;
        if (webcamEnabled) {
            btnWebcam.setImageResource(R.drawable.ic_video_camera);
        } else {
            btnWebcam.setImageResource(R.drawable.ic_video_camera_off);
        }
        updateCameraView();
        changeFloatingActionButtonLayout(btnWebcam, webcamEnabled);
    }


    private void updateCameraView() {
        if (webcamEnabled) {
            // create PeerConnectionFactory
            initializationOptions =
                    PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions();
            PeerConnectionFactory.initialize(initializationOptions);
            peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();


            svrJoin.init(PeerConnectionUtils.getEglContext(), null);
            svrJoin.setMirror(true);

            SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", PeerConnectionUtils.getEglContext());

            // create VideoCapturer
            videoCapturer = createCameraCapturer();
            videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
            videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
            videoCapturer.startCapture(480, 640, 30);

            // create VideoTrack
            videoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);

            // display in localView
            videoTrack.addSink(svrJoin);
        } else {
            if (videoTrack != null) videoTrack.removeSink(svrJoin);
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

    @Override
    protected void onDestroy() {
        if (videoTrack != null) videoTrack.removeSink(svrJoin);

        svrJoin.clearImage();
        svrJoin.release();

        closeCapturer();

        super.onDestroy();
    }


    private void closeCapturer() {
        final String TAG = "PeerConnectionUtils";

        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            videoCapturer.dispose();
            videoCapturer = null;
        }

        Log.d(TAG, "Stopped capture.");

        if (videoSource != null) {
            videoSource.dispose();
            videoSource = null;
        }

        if (peerConnectionFactory != null) {
            peerConnectionFactory.stopAecDump();
            peerConnectionFactory.dispose();
            peerConnectionFactory = null;
        }

        Log.d(TAG, "Closed video source.");

        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
        Log.d(TAG, "Closed peer connection.");
    }

}