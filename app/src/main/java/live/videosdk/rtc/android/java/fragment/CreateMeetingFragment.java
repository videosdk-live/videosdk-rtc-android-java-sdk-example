package live.videosdk.rtc.android.java.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.material.snackbar.Snackbar;

import live.videosdk.rtc.android.java.Activity.CreateOrJoinActivity;
import live.videosdk.rtc.android.java.Activity.OneToOneCallActivity;
import live.videosdk.rtc.android.java.Utils.HelperClass;
import live.videosdk.rtc.android.java.Utils.NetworkUtils;
import live.videosdk.rtc.android.java.R;

public class CreateMeetingFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_meeting, container, false);

        EditText etName = view.findViewById(R.id.etName);

        Button btnJoin = view.findViewById(R.id.btnJoin);

        btnJoin.setOnClickListener(v -> {
            if ("".equals(etName.getText().toString())) {
                Toast.makeText(getContext(), "Please Enter Name", Toast.LENGTH_SHORT).show();
            } else {
                NetworkUtils networkUtils = new NetworkUtils(getContext());
                if (networkUtils.isNetworkAvailable()) {
                    networkUtils.getToken(token -> {
                        networkUtils.createMeeting(token, meetingId -> {
                            Intent intent = new Intent((CreateOrJoinActivity) getActivity(), OneToOneCallActivity.class);
                            intent.putExtra("token", token);
                            intent.putExtra("meetingId", meetingId);
                            intent.putExtra("webcamEnabled", ((CreateOrJoinActivity) getActivity()).isWebcamEnabled());
                            intent.putExtra("micEnabled", ((CreateOrJoinActivity) getActivity()).isMicEnabled());
                            intent.putExtra("participantName", etName.getText().toString().trim());
                            startActivity(intent);
                            ((CreateOrJoinActivity) getActivity()).finish();

                        });
                    });
                } else {
                    Snackbar snackbar = Snackbar.make(view.findViewById(R.id.createMeetingLayout), "No Internet Connection", Snackbar.LENGTH_LONG);
                    HelperClass.setSnackNarStyle(snackbar.getView(),0);
                    snackbar.show();
                }
            }

        });
        return view;
    }
}