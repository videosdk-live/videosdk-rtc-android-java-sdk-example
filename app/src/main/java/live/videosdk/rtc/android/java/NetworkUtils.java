package live.videosdk.rtc.android.java;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

public class NetworkUtils {

    static Context context;

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

    private boolean isNullOrEmpty(String str) {
        return "null".equals(str) || "".equals(str) || null == str;
    }

    public void getToken(ResponseListener responseListener) {

        if (!isNullOrEmpty(AUTH_TOKEN) && !isNullOrEmpty(AUTH_URL)) {
            Toast.makeText(context,
                    "Please Provide only one - either auth_token or auth_url",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNullOrEmpty(AUTH_TOKEN)) {
            Log.d("TAG", "getToken: ");
            responseListener.onResponse(AUTH_TOKEN);
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

    public void createMeeting(String token, ResponseListener meetingEventListener) {

        AndroidNetworking.post("https://api.videosdk.live/v1/meetings")
                .addHeaders("Authorization", token)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                                final String meetingId = response.getString("meetingId");
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

    public void joinMeeting(String token, String meetingId, ResponseListener meetingEventListener) {

        AndroidNetworking.post("https://api.videosdk.live/v1/meetings/" + meetingId)
                .addHeaders("Authorization", token)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        meetingEventListener.onResponse(meetingId);
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
