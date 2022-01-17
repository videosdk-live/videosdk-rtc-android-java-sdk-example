package live.videosdk.rtc.android.java;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import live.videosdk.rtc.android.lib.PeerConnectionUtils;

public class CreateOrJoinActivity extends AppCompatActivity {

    private final String AUTH_TOKEN = BuildConfig.AUTH_TOKEN;
    private final String AUTH_URL= BuildConfig.AUTH_URL;

    private EditText etMeetingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_join);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("VideoSDK RTC");
        setSupportActionBar(toolbar);
        isNetworkAvailable();
        final Button btnCreate = findViewById(R.id.btnCreateMeeting);
        final Button btnJoin = findViewById(R.id.btnJoinMeeting);
        etMeetingId = findViewById(R.id.etMeetingId);

        btnCreate.setOnClickListener(v -> {
            getToken(null);
        });

        btnJoin.setOnClickListener(v -> {
            String meetingId = etMeetingId.getText().toString();
            if("".equals(meetingId))
            {
                Toast.makeText(CreateOrJoinActivity.this, "Please Enter Meeting Id", Toast.LENGTH_SHORT).show();
            }else if(!meetingId.matches("\\w{4}\\-\\w{4}\\-\\w{4}")){
                Toast.makeText(CreateOrJoinActivity.this, "Invalid Meeting Id", Toast.LENGTH_SHORT).show();
            }
            else {
                getToken(meetingId);
            }
        });

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        } else {
            Snackbar.make(findViewById(R.id.layout), "No Internet Connection", Snackbar.LENGTH_LONG).show();
        }
        return isAvailable;
    }

    private boolean isNullOrEmpty(String str){
        return "null".equals(str) || "".equals(str) || null==str;
    }

    private void getToken(@Nullable String meetingId) {
        if (!isNetworkAvailable()) {
            return;
        }
        if(!isNullOrEmpty(AUTH_TOKEN) && !isNullOrEmpty(AUTH_URL) )
        {
            Toast.makeText(CreateOrJoinActivity.this, "Please Provide only one - either auth_token or auth_url", Toast.LENGTH_SHORT).show();
        }else
        {
            if(!isNullOrEmpty(AUTH_URL)) {
                AndroidNetworking.get(AUTH_URL + "/get-token")
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String token = response.getString("token");
                                    if (meetingId == null) {
                                        createMeeting(token);
                                    } else {
                                        joinMeeting(token, meetingId);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                anError.printStackTrace();
                                Toast.makeText(CreateOrJoinActivity.this, anError.getErrorDetail(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }else if(!isNullOrEmpty(AUTH_TOKEN))
            {
                try {
                    if (meetingId == null) {
                        createMeeting(AUTH_TOKEN);
                    } else {
                        joinMeeting(AUTH_TOKEN, meetingId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else
            {
                Toast.makeText(CreateOrJoinActivity.this, "Please Provide auth_token or auth_url", Toast.LENGTH_SHORT).show();
            }
        }


    }


    private void createMeeting(String token) {
        AndroidNetworking.post("https://api.zujonow.com/api/meetings")
                .addHeaders("Authorization",token)
                .build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    final String meetingId = response.getString("meetingId");
                    Intent intent = new Intent(CreateOrJoinActivity.this, MainActivity.class);
                    intent.putExtra("token", token);
                    intent.putExtra("meetingId", meetingId);

                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ANError anError) {
                anError.printStackTrace();
                Toast.makeText(CreateOrJoinActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinMeeting(String token, String meetingId) {
        AndroidNetworking.post("https://api.zujonow.com/api/meetings/"+meetingId)
                .addHeaders("Authorization",token)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Intent intent = new Intent(CreateOrJoinActivity.this, JoinActivity.class);
                        intent.putExtra("token", token);
                        intent.putExtra("meetingId", meetingId);

                        startActivity(intent);
                    }

                    @Override
                    public void onError(ANError anError) {
                        anError.printStackTrace();
                        Toast.makeText(CreateOrJoinActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}