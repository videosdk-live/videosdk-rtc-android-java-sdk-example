package live.videosdk.rtc.android.java;

import android.content.Intent;
import android.os.Bundle;
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

    private final String Token = BuildConfig.TOKEN;

    private EditText etMeetingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        final Button btnCreate = findViewById(R.id.btnCreate);
        final Button btnJoin = findViewById(R.id.btnJoin);
        etMeetingId = findViewById(R.id.etMeetingId);

        btnCreate.setOnClickListener(v -> {
            createMeeting(Token);
        });

        btnJoin.setOnClickListener(v -> {
            String meetingId = etMeetingId.getText().toString();
            joinMeeting(Token, meetingId);
        });

    }

    private void createMeeting(String token) {
        AndroidNetworking.post("https://api.zujonow.com/api/meetings")
                .addHeaders("Content-Type", "application/json")
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