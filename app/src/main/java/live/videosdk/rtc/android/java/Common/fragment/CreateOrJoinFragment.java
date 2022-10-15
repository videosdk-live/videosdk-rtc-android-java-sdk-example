package live.videosdk.rtc.android.java.Common.fragment;

import android.app.Fragment;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import live.videosdk.rtc.android.java.Common.Activity.CreateOrJoinActivity;
import live.videosdk.rtc.android.java.R;

public class CreateOrJoinFragment extends Fragment {

    public CreateOrJoinFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_createorjoin, container, false);

        view.findViewById(R.id.btnCreateMeeting).setOnClickListener(v -> {
            ((CreateOrJoinActivity) getActivity()).CreateMeetingFragment();
        });

        view.findViewById(R.id.btnJoinMeeting).setOnClickListener(v -> {
            ((CreateOrJoinActivity) getActivity()).joinMeetingFragment();
        });
        // Inflate the layout for this fragment
        return view;
    }
}