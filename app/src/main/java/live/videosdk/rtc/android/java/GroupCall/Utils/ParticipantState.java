package live.videosdk.rtc.android.java.GroupCall.Utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.java.GroupCall.Listener.ParticipantChangeListener;
import live.videosdk.rtc.android.listeners.MeetingEventListener;

public class ParticipantState {

    private static final String TAG = "ParticipantState";
    private static ParticipantState participantState = null;
    Meeting meeting;
    int perPageParticipantSize =4;
   List<ParticipantChangeListener> participantChangeListenerList=new ArrayList<>();
    private boolean screenShare =false;

    // static method to create instance of Singleton class
    public static ParticipantState getInstance(Meeting meeting) {
        if (participantState == null)
            participantState = new ParticipantState(meeting);
        return participantState;
    }

    public static void destroy(){
        participantState = null;
    }

    ParticipantState(Meeting meeting){
        this.meeting=meeting;

        meeting.addEventListener(new MeetingEventListener() {

            @Override
            public void onMeetingJoined() {
                super.onMeetingJoined();
                for(int i=0;i<participantChangeListenerList.size();i++)
                {
                    participantChangeListenerList.get(i).onChangeParticipant(getParticipantList());
                }
            }

            @Override
            public void onParticipantJoined(Participant participant) {
                for(int i=0;i<participantChangeListenerList.size();i++)
                {
                    participantChangeListenerList.get(i).onChangeParticipant(getParticipantList());
                }

            }

            @Override
            public void onParticipantLeft(Participant participant) {
                for(int i=0;i<participantChangeListenerList.size();i++)
                {
                    participantChangeListenerList.get(i).onChangeParticipant(getParticipantList());
                }
            }
            @Override
            public void onPresenterChanged(String participantId) {
                super.onPresenterChanged(participantId);
                if(!TextUtils.isEmpty(participantId)){
                    perPageParticipantSize = 2;
                    screenShare =true;
                }else{
                    perPageParticipantSize =4;
                    screenShare=false;
                }
                for(int i=0;i<participantChangeListenerList.size();i++)
                {
                    participantChangeListenerList.get(i).onChangeParticipant(getParticipantList());
                    participantChangeListenerList.get(i).onPresenterChanged(screenShare);

                }
            }

        });

    }

    public List<List<Participant>> getParticipantList(){
        List<List<Participant>> finalParticipantList =new ArrayList<>();

        final Iterator<Participant> participants = meeting.getParticipants().values().iterator();

        if(finalParticipantList.size() == 0)
        {
            List<Participant> participants1=new ArrayList<>();
            participants1.add(meeting.getLocalParticipant());
            finalParticipantList.add(participants1);
        }

        for (int i = 0; i < meeting.getParticipants().size(); i++) {

            List<Participant> participantList= finalParticipantList.get(finalParticipantList.size()-1);

            if(participantList.size() == perPageParticipantSize){
                List<Participant> participants1=new ArrayList<>();
                participants1.add(participants.next());
                finalParticipantList.add(participants1);
            }else{
                final Participant participant = participants.next();
                participantList.add(participant);
            }

        }

        return finalParticipantList;

    }

    public void addParticipantChangeListener(ParticipantChangeListener listener)
    {
        participantChangeListenerList.add(listener);
        listener.onChangeParticipant(getParticipantList());
        listener.onPresenterChanged(screenShare);
    }

    public void removeParticipantChangeListener(ParticipantChangeListener listener)
    {
        participantChangeListenerList.remove(listener);
    }


}
