package live.videosdk.rtc.android.java;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

public class JoinActivity extends AppCompatActivity {

    private final String AUTH_TOKEN = BuildConfig.AUTH_TOKEN;
    private final String AUTH_URL= BuildConfig.AUTH_URL;

    private EditText etMeetingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        final Button btnCreate = findViewById(R.id.btnCreate);
        final Button btnJoin = findViewById(R.id.btnJoin);
        etMeetingId = findViewById(R.id.etMeetingId);

        btnCreate.setOnClickListener(v -> {
            getToken(null);
        });

        btnJoin.setOnClickListener(v -> {
            String meetingId = etMeetingId.getText().toString();
            if("".equals(meetingId))
            {
                Toast.makeText(JoinActivity.this, "Please Enter Meeting Id", Toast.LENGTH_SHORT).show();
            }else {
                getToken(meetingId);
            }
        });

    }

    private boolean isNullOrEmpty(String str){
        return "null".equals(str) || "".equals(str) || null==str;
    }

    private void getToken(@Nullable String meetingId) {
        if(!isNullOrEmpty(AUTH_TOKEN) && !isNullOrEmpty(AUTH_URL) )
        {
            Toast.makeText(JoinActivity.this, "Please Provide only one - either auth_token or auth_url", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(JoinActivity.this, anError.getErrorDetail(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(JoinActivity.this, "Please Provide auth_token or auth_url", Toast.LENGTH_SHORT).show();
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

                    Intent intent = new Intent(JoinActivity.this, MainActivity.class);
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
                Toast.makeText(JoinActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Intent intent = new Intent(JoinActivity.this, MainActivity.class);
                        intent.putExtra("token", token);
                        intent.putExtra("meetingId", meetingId);

                        startActivity(intent);
                    }

                    @Override
                    public void onError(ANError anError) {
                        anError.printStackTrace();
                        Toast.makeText(JoinActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}