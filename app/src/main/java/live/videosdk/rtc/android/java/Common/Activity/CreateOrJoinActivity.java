package live.videosdk.rtc.android.java.Common.Activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.webrtc.Camera1Enumerator;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import live.videosdk.rtc.android.CustomStreamTrack;
import live.videosdk.rtc.android.VideoSDK;
import live.videosdk.rtc.android.VideoView;
import live.videosdk.rtc.android.java.Common.Adapter.DeviceAdapter;
import live.videosdk.rtc.android.java.R;
import live.videosdk.rtc.android.java.Common.fragment.CreateOrJoinFragment;
import live.videosdk.rtc.android.java.Common.fragment.JoinMeetingFragment;
import live.videosdk.rtc.android.java.Common.fragment.CreateMeetingFragment;
import live.videosdk.rtc.android.mediaDevice.AudioDeviceInfo;
import live.videosdk.rtc.android.mediaDevice.FacingMode;
import live.videosdk.rtc.android.mediaDevice.VideoDeviceInfo;
import live.videosdk.rtc.android.permission.Permission;
import live.videosdk.rtc.android.permission.PermissionHandler;
import live.videosdk.rtc.android.permission.Permissions;

public class CreateOrJoinActivity extends AppCompatActivity {

    private boolean micEnabled = false;
    private boolean webcamEnabled = false;
    private Menu optionsMenu;
    private TextView cameraOffText;

    private FloatingActionButton btnMic, btnWebcam;
    private VideoView joinView;

    Toolbar toolbar;
    ActionBar actionBar;

    CustomStreamTrack videoTrack;
    VideoCapturer videoCapturer;
    PeerConnectionFactory.InitializationOptions initializationOptions;
    PeerConnectionFactory peerConnectionFactory;
    VideoSource videoSource;

    boolean permissionsGranted = false;
    private final com.nabinbhandari.android.permissions.PermissionHandler permissionHandler = new com.nabinbhandari.android.permissions.PermissionHandler() {
        @Override
        public void onGranted() {
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


    private PermissionHandler permissionHandlerSDK = new PermissionHandler() {
        @Override
        public void onGranted() {
            permissionsGranted = true;

            micEnabled = true;
            btnMic.setImageResource(R.drawable.ic_mic_on);
            changeFloatingActionButtonLayout(btnMic, micEnabled);

            webcamEnabled = true;
            btnWebcam.setImageResource(R.drawable.ic_video_camera);
            changeFloatingActionButtonLayout(btnWebcam, webcamEnabled);

            updateCameraView(null);
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


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_join);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("VideoSDK RTC");

        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        btnMic = findViewById(R.id.btnMic);
        btnWebcam = findViewById(R.id.btnWebcam);
        joinView = findViewById(R.id.joiningView);
        cameraOffText= findViewById(R.id.cameraOffText);

        checkPermissions();

        LinearLayout fragContainer = (LinearLayout) findViewById(R.id.fragContainer);

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);

        getFragmentManager().beginTransaction().replace(R.id.fragContainer, new CreateOrJoinFragment(), "CreateOrJoinFragment").commit();

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
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        optionsMenu = menu;
        setAudioDeviceChangeListener();
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

        switch (item.getItemId()) {
            case R.id.Camera:
                changeCamera();
                return true;
            case R.id.Audio:
                getAudioDevices();
                return true;
            default:super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private List<String> previousAvailableDevices = new ArrayList<>();

    private void setAudioDeviceChangeListener() {
        VideoSDK.setAudioDeviceChangeListener(new VideoSDK.AudioDeviceChangeEvent() {
            @Override
            public void onAudioDeviceChanged(AudioDeviceInfo selectedAudioDevice, Set<AudioDeviceInfo> availableAudioDevices) {
                Log.d(TAG, "setAudioDeviceChangeListener: " + selectedAudioDevice.getLabel());

                List<String> currentAvailableDevices = new ArrayList<>();
                for (AudioDeviceInfo device : availableAudioDevices) {
                    currentAvailableDevices.add(device.getLabel());
                }
                Log.d(TAG, "Current available : " + currentAvailableDevices);

                List<String> addedDevices = new ArrayList<>();
                for (String device : currentAvailableDevices) {
                    if (!previousAvailableDevices.contains(device)) {
                        addedDevices.add(device);
                    }
                }
                Log.d(TAG, "Added audio devices: " + addedDevices);

                List<String> removedDevices = new ArrayList<>();
                for (String device : previousAvailableDevices) {
                    if (!currentAvailableDevices.contains(device)) {
                        removedDevices.add(device);
                    }
                }
                Log.d(TAG, "Removed audio devices: " + removedDevices);

                previousAvailableDevices = currentAvailableDevices;

                if (!addedDevices.isEmpty() && !addedDevices.equals(previousAvailableDevices)) {
                    Toast.makeText(getApplicationContext(), addedDevices + " Connected", Toast.LENGTH_SHORT).show();
                }
                if (!removedDevices.isEmpty()) {
                    Toast.makeText(getApplicationContext(), removedDevices + " Removed", Toast.LENGTH_SHORT).show();
                }

                switch (selectedAudioDevice.getLabel()) {
                    case "BLUETOOTH":
                        optionsMenu.findItem(R.id.Audio).setIcon(R.drawable.baseline_bluetooth_connected_24);
                        break;
                    case "WIRED_HEADSET":
                        optionsMenu.findItem(R.id.Audio).setIcon(R.drawable.baseline_headphones_24);
                        break;
                    case "SPEAKER_PHONE":
                        optionsMenu.findItem(R.id.Audio).setIcon(R.drawable.baseline_volume_up_24);
                        break;
                    case "EARPIECE":
                        optionsMenu.findItem(R.id.Audio).setIcon(R.drawable.phone_call);
                        break;
                }
            }
        });
    }

    private void getAudioDevices() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        RecyclerView recyclerView = bottomSheetView.findViewById(R.id.rvItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Set<AudioDeviceInfo> audioDevice = VideoSDK.getAudioDevices();
        List<String> labels = new ArrayList<>();

        for (AudioDeviceInfo device : audioDevice) {
            String label = device.getLabel();
            labels.add(label);
        }

        DeviceAdapter deviceAdapter = new DeviceAdapter(labels, new DeviceAdapter.ClickListener() {
            @Override
            public void onClick(String itemDto) {
                for (AudioDeviceInfo device : audioDevice) {
                    if (device.getLabel().equals(itemDto)) {
                        VideoSDK.setSelectedAudioDevice(device);
                    }
                }
                bottomSheetDialog.cancel();
                Toast.makeText(getApplicationContext(), "Selected " + itemDto, Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(deviceAdapter);
        bottomSheetDialog.show();
    }

    private void changeCamera() {

        Set<VideoDeviceInfo> videoDevices = VideoSDK.getVideoDevices();

        VideoDeviceInfo currentDevice = VideoSDK.getSelectedVideoDevice();

        FacingMode currentFacingMode = currentDevice.getFacingMode();

        FacingMode facingMode = FacingMode.front;
        VideoDeviceInfo videoDevice = null;

        if (currentFacingMode.equals(FacingMode.front)) {
            facingMode = FacingMode.back;
        } else if (currentFacingMode.equals(FacingMode.back)) {
            facingMode = FacingMode.front;
        }

        for (VideoDeviceInfo device : videoDevices) {
            if (device.getFacingMode().equals(facingMode)) {
                videoDevice = device;
            }
        }

        if (!facingMode.equals(FacingMode.front)) {
            joinView.setMirror(false);
        }

        if (videoDevice != null) {
            VideoSDK.setSelectedVideoDevice(videoDevice);
            updateCameraView(videoDevice);
        }

        Toast.makeText(this, "Camera switched", Toast.LENGTH_SHORT).show();


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
        updateCameraView(null);
        changeFloatingActionButtonLayout(btnWebcam, webcamEnabled);
    }


    private void updateCameraView(VideoDeviceInfo videoDevice) {
        if (webcamEnabled) {

            if (cameraOffText != null) {
                cameraOffText.setVisibility(View.GONE);
            }

            if (joinView != null) {
                joinView.setVisibility(View.VISIBLE);
            }
            // create PeerConnectionFactory
            initializationOptions =
                    PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions();
            PeerConnectionFactory.initialize(initializationOptions);
            peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();

            videoTrack = VideoSDK.createCameraVideoTrack(
                    "h720p_w960p",
                    "front",
                    CustomStreamTrack.VideoMode.TEXT,
                    true,
                    this,videoDevice
            );

            // display in localView
            joinView.addTrack((VideoTrack) videoTrack.getTrack());
        } else {
            joinView.removeTrack();
            joinView.releaseSurfaceViewRenderer();
//
            if (joinView != null) {
                joinView.setVisibility(View.INVISIBLE);
            }

            if (cameraOffText != null) {
                cameraOffText.setVisibility(View.VISIBLE);
            }
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
        joinView.removeTrack();

        joinView.releaseSurfaceViewRenderer();

        closeCapturer();

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        joinView.removeTrack();

        joinView.releaseSurfaceViewRenderer();

        closeCapturer();
        super.onPause();
    }

    @Override
    protected void onRestart() {
        updateCameraView(null);
        super.onRestart();
    }

    private void closeCapturer() {

        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            videoCapturer.dispose();
            videoCapturer = null;
        }

        if (videoSource != null) {
            videoSource.dispose();
            videoSource = null;
        }

        if (peerConnectionFactory != null) {
            peerConnectionFactory.stopAecDump();
            peerConnectionFactory.dispose();
            peerConnectionFactory = null;
        }

        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
    }

}