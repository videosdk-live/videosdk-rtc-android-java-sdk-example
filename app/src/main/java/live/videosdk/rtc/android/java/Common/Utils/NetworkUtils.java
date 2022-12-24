package live.videosdk.rtc.android.java.Common.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import live.videosdk.rtc.android.java.BuildConfig;
import live.videosdk.rtc.android.java.Common.Listener.ResponseListener;

public class NetworkUtils {

    static Context context;
    private static int activeMeetingSeconds;

    public NetworkUtils(Context context) {
        this.context = context;
    }

    private final String AUTH_TOKEN = BuildConfig.AUTH_TOKEN;
    private final String AUTH_URL = BuildConfig.AUTH_URL;


    public boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = networkInfo != null && networkInfo.isConnected();
        return isAvailable;
    }

    public void getToken(ResponseListener<String> responseListener) {

        if (!HelperClass.isNullOrEmpty(AUTH_TOKEN) && !HelperClass.isNullOrEmpty(AUTH_URL)) {
            Toast.makeText(context,
                    "Please Provide only one - either auth_token or auth_url",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!HelperClass.isNullOrEmpty(AUTH_TOKEN)) {
            responseListener.onResponse(AUTH_TOKEN);
            return;
        }

        if (!HelperClass.isNullOrEmpty(AUTH_URL)) {
            AndroidNetworking.get(AUTH_URL + "/get-token")
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String token = response.getString("token");
                                responseListener.onResponse(token);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            anError.printStackTrace();
                            Toast.makeText(context,
                                    anError.getErrorDetail(), Toast.LENGTH_SHORT).show();
                        }
                    });

            return;
        }

        Toast.makeText(context,
                "Please Provide auth_token or auth_url", Toast.LENGTH_SHORT).show();


    }

    public void createMeeting(String token, ResponseListener<String> meetingEventListener) {

        AndroidNetworking.post("https://api.videosdk.live/v2/rooms")
                .addHeaders("Authorization", token)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final String meetingId = response.getString("roomId");

                            meetingEventListener.onResponse(meetingId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        anError.printStackTrace();
                        Toast.makeText(context, anError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void joinMeeting(String token, String roomId, ResponseListener<String> meetingEventListener) {


        AndroidNetworking.get("https://api.videosdk.live/v2/rooms/validate/" + roomId)
                .addHeaders("Authorization", token)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        meetingEventListener.onResponse(roomId);
                    }

                    @Override
                    public void onError(ANError anError) {
                        anError.printStackTrace();
                        Toast.makeText(context, anError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void fetchMeetingTime(String meetingId, String token,ResponseListener<Integer> responseListener) {
        AndroidNetworking.get("https://api.videosdk.live/v2/sessions/?roomId=" + meetingId)
                .addHeaders("Authorization", token)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = (JSONArray) response.get("data");
                            String startMeetingTime = jsonArray.getJSONObject(0).get("start").toString();
                            Date startMeetingDate = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                startMeetingDate = Date.from(Instant.parse(startMeetingTime));
                            }

                            Date currentTime = Calendar.getInstance().getTime();
                            long difference = currentTime.getTime() - startMeetingDate.getTime();

                            activeMeetingSeconds = Math.toIntExact(TimeUnit.MILLISECONDS.toSeconds(difference));

                            responseListener.onResponse(activeMeetingSeconds);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        anError.printStackTrace();
                        Toast.makeText(context, anError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }


}
