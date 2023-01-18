package live.videosdk.rtc.android.java.GroupCall.Fragement;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.Stream;
import live.videosdk.rtc.android.VideoView;
import live.videosdk.rtc.android.java.Common.Listener.ParticipantStreamChangeListener;
import live.videosdk.rtc.android.java.Common.Utils.HelperClass;
import live.videosdk.rtc.android.java.GroupCall.Activity.GroupCallActivity;
import live.videosdk.rtc.android.java.GroupCall.Listener.ParticipantChangeListener;
import live.videosdk.rtc.android.java.GroupCall.Utils.ParticipantState;
import live.videosdk.rtc.android.java.R;
import live.videosdk.rtc.android.listeners.ParticipantEventListener;

public class ParticipantViewFragment extends Fragment {

    GridLayout participantGridLayout;
    int position;
    Meeting meeting;
    ParticipantChangeListener participantChangeListener;
    ParticipantState participantState;

    private List<Participant> participants;
    private List<List<Participant>> participantListArr;

    TabLayoutMediator tabLayoutMediator;
    ViewPager2 viewPager2;
    TabLayout tabLayout;
    private PopupWindow popupwindow_obj;
    private Map<String, Participant> participantsInGrid;
    private Map<String, View> participantsView = new HashMap<>();

    public ParticipantViewFragment() {
        // Required empty public constructor
    }

    public ParticipantViewFragment(Meeting meeting, int position) {
        this.meeting = meeting;
        this.position = position;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_view, container, false);

        participantGridLayout = view.findViewById(R.id.participantGridLayout);

        viewPager2 = getActivity().findViewById(R.id.view_pager_video_grid);
        tabLayout = getActivity().findViewById(R.id.tab_layout_dots);

        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        participantGridLayout.setOnTouchListener(((GroupCallActivity) getActivity()).getOnTouchListener());

        participantChangeListener = new ParticipantChangeListener() {
            @Override
            public void onChangeParticipant(List<List<Participant>> participantList) {
                changeLayout(participantList, null);
            }

            @Override
            public void onPresenterChanged(boolean screenShare) {
                showInGUI(null);
                updateGridLayout(screenShare);
            }

            @Override
            public void onSpeakerChanged(List<List<Participant>> participantList, Participant activeSpeaker) {
                if (participantList != null)
                    changeLayout(participantList, activeSpeaker);
                else
                    activeSpeakerLayout(activeSpeaker);
            }
        };

        participantState = ParticipantState.getInstance(meeting);
        participantState.addParticipantChangeListener(participantChangeListener);

    }

    private void changeLayout(List<List<Participant>> participantList, Participant activeSpeaker) {
        participantListArr = participantList;
        if (position < participantList.size()) {
            participants = participantList.get(position);
            if (popupwindow_obj != null && popupwindow_obj.isShowing())
                popupwindow_obj.dismiss();
            showInGUI(activeSpeaker);
            tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, true,
                    (tab, position) -> Log.d("TAG", "onCreate: ")
            );

            if (tabLayoutMediator.isAttached()) {
                tabLayoutMediator.detach();
            }

            tabLayoutMediator.attach();

            if (participantList.size() == 1) {
                tabLayout.setVisibility(View.GONE);
            } else {
                tabLayout.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public void onResume() {
        if (position < participantListArr.size()) {

            List<Participant> currentParticipants = participantListArr.get(position);
            for (int i = 0; i < currentParticipants.size(); i++) {
                Participant participant = currentParticipants.get(i);
                if (!participant.isLocal()) {
                    for (Map.Entry<String, Stream> entry : participant.getStreams().entrySet()) {
                        Stream stream = entry.getValue();
                        if (stream.getKind().equalsIgnoreCase("video"))
                            stream.resume();
                    }
                }
            }
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        if (position < participantListArr.size()) {
            List<Participant> otherParticipants = new ArrayList<>();

            for (int i = 0; i < participantListArr.size(); i++) {
                if (position == i) {
                    continue;
                }
                otherParticipants = participantListArr.get(i);
            }

            for (int i = 0; i < otherParticipants.size(); i++) {
                Participant participant = otherParticipants.get(i);
                if (!participant.isLocal()) {
                    for (Map.Entry<String, Stream> entry : participant.getStreams().entrySet()) {
                        Stream stream = entry.getValue();
                        if (stream.getKind().equalsIgnoreCase("video")) {
                            stream.pause();
                        }
                    }
                }
            }
        }
        super.onPause();
    }

    // Call where View ready.
    private void showInGUI(Participant activeSpeaker) {
        for (int i = 0; i < participants.size(); i++) {
            Participant participant = participants.get(i);
            if (participantsInGrid != null) {
                for (Map.Entry<String, Participant> entry : participantsInGrid.entrySet()) {
                    Participant key = entry.getValue();
                    if (!participants.contains(key)) {
                        participantsInGrid.remove(key.getId());
                        VideoView participantVideoView = participantsView.get(key.getId()).findViewById(R.id.participantVideoView);
                        participantVideoView.releaseSurfaceViewRenderer();
                        participantGridLayout.removeView(participantsView.get(key.getId()));
                        participantsView.remove(key.getId());
                        updateGridLayout(false);
                    }
                }

            }

            if (participantsInGrid == null || !participantsInGrid.containsKey(participant.getId())) {

                if (participantsInGrid == null)
                    participantsInGrid = new ConcurrentHashMap<>();

                participantsInGrid.put(participant.getId(), participant);
                View participantView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_participant, participantGridLayout, false);

                participantsView.put(participant.getId(), participantView);
                CardView participantCard = participantView.findViewById(R.id.ParticipantCard);
                ImageView ivMicStatus = participantView.findViewById(R.id.ivMicStatus);
//            GifImageView img_participantActiveSpeaker = participantView.findViewById(R.id.img_participantActiveSpeaker);

                if (activeSpeaker == null) {
                    participantCard.setForeground(null);
//                img_participantActiveSpeaker.setVisibility(View.GONE);
//                ivMicStatus.setVisibility(View.VISIBLE);
                } else {
                    if (participant.getId().equals(activeSpeaker.getId())) {
                        participantCard.setForeground(getContext().getDrawable(R.drawable.layout_bg));
//                    ivMicStatus.setVisibility(View.GONE);
//                    img_participantActiveSpeaker.setVisibility(View.VISIBLE);
                    } else {
                        participantCard.setForeground(null);
//                    img_participantActiveSpeaker.setVisibility(View.GONE);
//                    ivMicStatus.setVisibility(View.VISIBLE);
                    }
                }

                ParticipantStreamChangeListener participantStreamChangeListener;

                ImageView ivNetwork = participantView.findViewById(R.id.ivNetwork);

                participantStreamChangeListener = new ParticipantStreamChangeListener() {
                    @Override
                    public void onStreamChanged() {
                        if (participant.getStreams().isEmpty()) {
                            ivNetwork.setVisibility(View.GONE);
                        } else {
                            ivNetwork.setVisibility(View.VISIBLE);
                        }
                    }
                };


                ivNetwork.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupwindow_obj = HelperClass.callStatsPopupDisplay(participant, ivNetwork, getContext(), false);
                        popupwindow_obj.showAsDropDown(ivNetwork, -350, -85);
                    }
                });


                TextView tvName = participantView.findViewById(R.id.tvName);
                TextView txtParticipantName = participantView.findViewById(R.id.txtParticipantName);

                VideoView participantVideoView = participantView.findViewById(R.id.participantVideoView);

                if (participant.getId().equals(meeting.getLocalParticipant().getId())) {
                    tvName.setText("You");
                } else {
                    tvName.setText(participant.getDisplayName());
                }
                txtParticipantName.setText(participant.getDisplayName().substring(0, 1));

                for (Map.Entry<String, Stream> entry : participant.getStreams().entrySet()) {
                    Stream stream = entry.getValue();
                    if (stream.getKind().equalsIgnoreCase("video")) {
                        participantVideoView.setVisibility(View.VISIBLE);
                        VideoTrack videoTrack = (VideoTrack) stream.getTrack();
                        participantVideoView.addTrack(videoTrack);
                        participantStreamChangeListener.onStreamChanged();
                        break;
                    } else if (stream.getKind().equalsIgnoreCase("audio")) {
                        participantStreamChangeListener.onStreamChanged();
                        ivMicStatus.setImageResource(R.drawable.ic_audio_on);
                    }

                }

                participant.addEventListener(new ParticipantEventListener() {
                    @Override
                    public void onStreamEnabled(Stream stream) {
                        if (stream.getKind().equalsIgnoreCase("video")) {
                            participantVideoView.setVisibility(View.VISIBLE);

                            VideoTrack videoTrack = (VideoTrack) stream.getTrack();
                            participantVideoView.addTrack(videoTrack);
                            participantStreamChangeListener.onStreamChanged();

                        } else if (stream.getKind().equalsIgnoreCase("audio")) {
                            participantStreamChangeListener.onStreamChanged();
                            ivMicStatus.setImageResource(R.drawable.ic_audio_on);
                        }
                    }

                    @Override
                    public void onStreamDisabled(Stream stream) {
                        if (stream.getKind().equalsIgnoreCase("video")) {
                            VideoTrack track = (VideoTrack) stream.getTrack();
                            if (track != null) participantVideoView.removeTrack();

                            participantVideoView.setVisibility(View.GONE);

                        } else if (stream.getKind().equalsIgnoreCase("audio")) {
                            ivMicStatus.setImageResource(R.drawable.ic_audio_off);
                        }
                    }
                });

                participantGridLayout.addView(participantView);
                updateGridLayout(false);

            }
        }
    }

    public void activeSpeakerLayout(Participant activeSpeaker) {
        for (int j = 0; j < participantGridLayout.getChildCount(); j++) {
            Participant participant = participants.get(j);
            View participantView = participantGridLayout.getChildAt(j);

            CardView participantCard = participantView.findViewById(R.id.ParticipantCard);
//            ImageView ivMicStatus = participantView.findViewById(R.id.ivMicStatus);
//            GifImageView img_participantActiveSpeaker = participantView.findViewById(R.id.img_participantActiveSpeaker);

            if (activeSpeaker == null) {
                participantCard.setForeground(null);
//                img_participantActiveSpeaker.setVisibility(View.GONE);
//                ivMicStatus.setVisibility(View.VISIBLE);
            } else {
                if (participant.getId().equals(activeSpeaker.getId())) {
                    participantCard.setForeground(getContext().getDrawable(R.drawable.layout_bg));
//                    ivMicStatus.setVisibility(View.GONE);
//                    img_participantActiveSpeaker.setVisibility(View.VISIBLE);
                } else {
                    participantCard.setForeground(null);
//                    img_participantActiveSpeaker.setVisibility(View.GONE);
//                    ivMicStatus.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if (participantChangeListener != null) {
            participantState.removeParticipantChangeListener(participantChangeListener);
        }

        for (int i = 0; i < participantGridLayout.getChildCount(); i++) {
            View view = participantGridLayout.getChildAt(i);
            VideoView videoView = view.findViewById(R.id.participantVideoView);
            if (videoView != null) {
                videoView.setVisibility(View.GONE);
                videoView.releaseSurfaceViewRenderer();
            }
        }

        participantGridLayout.removeAllViews();
        participantsInGrid = null;
        super.onDestroy();
    }

    public void updateGridLayout(boolean screenShareFlag) {
        if (screenShareFlag) {
            int col = 0, row = 0;
            for (int i = 0; i < participantGridLayout.getChildCount(); i++) {
                GridLayout.LayoutParams params = (GridLayout.LayoutParams) participantGridLayout.getChildAt(i).getLayoutParams();
                params.columnSpec = GridLayout.spec(col, 1, 1f);
                params.rowSpec = GridLayout.spec(row, 1, 1f);
                if (col + 1 == 2) {
                    col = 0;
                    row++;
                } else {
                    col++;
                }

            }
            participantGridLayout.requestLayout();
        } else {
            int col = 0, row = 0;
            for (int i = 0; i < participantGridLayout.getChildCount(); i++) {
                GridLayout.LayoutParams params = (GridLayout.LayoutParams) participantGridLayout.getChildAt(i).getLayoutParams();
                params.columnSpec = GridLayout.spec(col, 1, 1f);
                params.rowSpec = GridLayout.spec(row, 1, 1f);
                if (col + 1 == getNormalLayoutColumnCount()) {
                    col = 0;
                    row++;
                } else {
                    col++;
                }

            }
            participantGridLayout.requestLayout();

        }

    }

    private int getNormalLayoutRowCount() {
        return min(max(1, participantsView.size()), 2);
    }

    private int getNormalLayoutColumnCount() {
        int maxColumns = 2;
        int result = max(1, (participantsView.size() + getNormalLayoutRowCount() - 1) / getNormalLayoutRowCount());
        if (result > maxColumns) {
            throw new IllegalStateException(
                    "${result} videos not allowed."
            );
        }
        return result;
    }

}