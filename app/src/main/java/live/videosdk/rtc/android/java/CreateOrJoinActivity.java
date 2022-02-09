package live.videosdk.rtc.android.java;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateOrJoinActivity extends AppCompatActivity {

    private final String AUTH_TOKEN = BuildConfig.AUTH_TOKEN;
    private final String AUTH_URL = BuildConfig.AUTH_URL;

    private EditText etMeetingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_join);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
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
            if ("".equals(meetingId)) {
                Toast.makeText(CreateOrJoinActivity.this, "Please enter meeting ID",
                        Toast.LENGTH_SHORT).show();
            } else if (!meetingId.matches("\\w{4}\\-\\w{4}\\-\\w{4}")) {
                Toast.makeText(CreateOrJoinActivity.this, "Please enter valid meeting ID",
                        Toast.LENGTH_SHORT).show();
            } else {
                getToken(meetingId);
            }
        });

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = networkInfo != null && networkInfo.isConnected();

        if (!isAvailable) {
            Snackbar.make(findViewById(R.id.layout), "No Internet Connection",
                    Snackbar.LENGTH_LONG).show();
        }

        return isAvailable;
    }

    private boolean isNullOrEmpty(String str) {
        return "null".equals(str) || "".equals(str) || null == str;
    }

    private void getToken(@Nullable String meetingId) {
        if (!isNetworkAvailable()) {
            return;
        }

        if (!isNullOrEmpty(AUTH_TOKEN) && !isNullOrEmpty(AUTH_URL)) {
            Toast.makeText(CreateOrJoinActivity.this,
                    "Please Provide only one - either auth_token or auth_url",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNullOrEmpty(AUTH_TOKEN)) {
            if (meetingId == null) {
                createMeeting(AUTH_TOKEN);
            } else {
                joinMeeting(AUTH_TOKEN, meetingId);
            }

            return;
        }

        if (!isNullOrEmpty(AUTH_URL)) {
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
                            Toast.makeText(CreateOrJoinActivity.this,
                                    anError.getErrorDetail(), Toast.LENGTH_SHORT).show();
                        }
                    });

            return;
        }

        Toast.makeText(CreateOrJoinActivity.this,
                "Please Provide auth_token or auth_url", Toast.LENGTH_SHORT).show();


    }


    private void createMeeting(String token) {
        AndroidNetworking.post("https://api.zujonow.com/api/meetings")
                .addHeaders("Authorization", token)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final String meetingId = response.getString("meetingId");

                            Intent intent = new Intent(CreateOrJoinActivity.this, JoinActivity.class);
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
                        Toast.makeText(CreateOrJoinActivity.this, anError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void joinMeeting(String token, String meetingId) {
        AndroidNetworking.post("https://api.zujonow.com/api/meetings/" + meetingId)
                .addHeaders("Authorization", token)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Intent intent = new Intent(CreateOrJoinActivity.this, JoinActivity.class);
                        intent.putExtra("token", token);
                        intent.putExtra("meetingId", meetingId);

                        startActivity(intent);

                        etMeetingId.getText().clear();
                    }

                    @Override
                    public void onError(ANError anError) {
                        anError.printStackTrace();
                        Toast.makeText(CreateOrJoinActivity.this, anError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}