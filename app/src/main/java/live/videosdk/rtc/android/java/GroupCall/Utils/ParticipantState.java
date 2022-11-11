package live.videosdk.rtc.android.java.GroupCall.Utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.java.Common.Utils.HelperClass;
import live.videosdk.rtc.android.java.GroupCall.Listener.ParticipantChangeListener;
import live.videosdk.rtc.android.listeners.MeetingEventListener;

public class ParticipantState {

    private static final String TAG = "ParticipantState";
    private static ParticipantState participantState = null;
    Meeting meeting;
    int perPageParticipantSize = 4;
    List<ParticipantChangeListener> participantChangeListenerList = new ArrayList<>();
    private boolean screenShare = false;
    private List<List<Participant>> activeSpeakerParticipantList;
    private List<List<Participant>> participantsArr;

    // static method to create instance of Singleton class
    public static ParticipantState getInstance(Meeting meeting) {
        if (participantState == null)
            participantState = new ParticipantState(meeting);
        return participantState;
    }

    public static void destroy() {
        participantState = null;
    }

    ParticipantState(Meeting meeting) {
        this.meeting = meeting;

        meeting.addEventListener(new MeetingEventListener() {

            @Override
            public void onMeetingJoined() {
                super.onMeetingJoined();
                for (int i = 0; i < participantChangeListenerList.size(); i++) {
                    participantChangeListenerList.get(i).onChangeParticipant(getParticipantList());
                }
            }

            @Override
            public void onParticipantJoined(Participant participant) {
                for (int i = 0; i < participantChangeListenerList.size(); i++) {
                    participantChangeListenerList.get(i).onChangeParticipant(getParticipantList());
                }

            }

            @Override
            public void onParticipantLeft(Participant participant) {
                for (int i = 0; i < participantChangeListenerList.size(); i++) {
                    participantChangeListenerList.get(i).onChangeParticipant(getParticipantList());
                }
            }

            @Override
            public void onPresenterChanged(String participantId) {
                super.onPresenterChanged(participantId);
                if (!TextUtils.isEmpty(participantId)) {
                    perPageParticipantSize = 2;
                    screenShare = true;
                } else {
                    perPageParticipantSize = 4;
                    screenShare = false;
                }
                for (int i = 0; i < participantChangeListenerList.size(); i++) {
                    participantChangeListenerList.get(i).onChangeParticipant(getParticipantList());
                    participantChangeListenerList.get(i).onPresenterChanged(screenShare);

                }
            }

            @Override
            public void onSpeakerChanged(String participantId) {
                super.onSpeakerChanged(participantId);
                boolean updateGrid = true;
                Participant activeSpeaker = null;
                if (!HelperClass.isNullOrEmpty(participantId)) {

                    if (meeting.getLocalParticipant().getId().equals(participantId)) {
                        activeSpeaker = meeting.getLocalParticipant();
                    } else {
                        activeSpeaker = meeting.getParticipants().get(participantId);
                    }

                    List<Participant> participants;
                    if (activeSpeakerParticipantList == null) {
                        participants = getParticipantList().get(0);
                    } else {
                        if (!activeSpeakerParticipantList.equals(participantsArr)) {
                            activeSpeakerParticipantList = participantsArr;
                        }
                        participants = activeSpeakerParticipantList.get(0);
                    }

                    for (int j = 0; j < participants.size(); j++) {
                        Participant participant = participants.get(j);
                        if (participant.getId().equals(participantId)) {
                            updateGrid = false;
                            break;
                        }
                    }

                    if (updateGrid) {
                        activeSpeakerParticipantList = getActiveSpeakerParticipantList(activeSpeaker);
                        participantsArr = activeSpeakerParticipantList;
                    }
                } else {
                    updateGrid = false;
                }

                for (int i = 0; i < participantChangeListenerList.size(); i++) {
                    if (updateGrid)
                        participantChangeListenerList.get(i).onSpeakerChanged(activeSpeakerParticipantList, activeSpeaker);
                    else
                        participantChangeListenerList.get(i).onSpeakerChanged(null, activeSpeaker);
                }
            }
        });

    }

    public List<List<Participant>> getParticipantList() {
        List<List<Participant>> participantListArr = new ArrayList<>();

        final Iterator<Participant> participants = meeting.getParticipants().values().iterator();

        if (participantListArr.size() == 0) {
            List<Participant> firstPageParticipantArr = new ArrayList<>();
            firstPageParticipantArr.add(meeting.getLocalParticipant());
            participantListArr.add(firstPageParticipantArr);
        }

        for (int i = 0; i < meeting.getParticipants().size(); i++) {

            List<Participant> participantList = participantListArr.get(participantListArr.size() - 1);

            if (participantList.size() == perPageParticipantSize) {
                List<Participant> newParticipantArr = new ArrayList<>();
                newParticipantArr.add(participants.next());
                participantListArr.add(newParticipantArr);
            } else {
                final Participant participant = participants.next();
                participantList.add(participant);
            }

        }

        participantsArr = participantListArr;

        return participantListArr;

    }

    public List<List<Participant>> getActiveSpeakerParticipantList(Participant activeSpeaker) {
        List<List<Participant>> participantListArr = new ArrayList<>();

        final Iterator<Participant> participants = meeting.getParticipants().values().iterator();

        if (participantListArr.size() == 0) {
            List<Participant> firstPageParticipantArr = new ArrayList<>();
            firstPageParticipantArr.add(meeting.getLocalParticipant());

            if (activeSpeaker != null && !(meeting.getLocalParticipant().getId().equals(activeSpeaker.getId()))) {
                firstPageParticipantArr.add(activeSpeaker);
            }

            participantListArr.add(firstPageParticipantArr);
        }


        for (int i = 0; i < meeting.getParticipants().size(); i++) {

            List<Participant> participantList = participantListArr.get(participantListArr.size() - 1);

            if (participantList.size() == perPageParticipantSize) {
                List<Participant> newParticipantArr = new ArrayList<>();
                final Participant participant = participants.next();
                if (!(activeSpeaker != null && participant.getId().equals(activeSpeaker.getId()))) {
                    newParticipantArr.add(participant);
                }
                participantListArr.add(newParticipantArr);
            } else {
                final Participant participant = participants.next();
                if (!(activeSpeaker != null && participant.getId().equals(activeSpeaker.getId()))) {
                    participantList.add(participant);
                }

            }

        }

        return participantListArr;

    }

    public void addParticipantChangeListener(ParticipantChangeListener listener) {
        participantChangeListenerList.add(listener);
        listener.onChangeParticipant(getParticipantList());
        listener.onPresenterChanged(screenShare);
        listener.onSpeakerChanged(null, null);
    }

    public void removeParticipantChangeListener(ParticipantChangeListener listener) {
        participantChangeListenerList.remove(listener);
    }


}
