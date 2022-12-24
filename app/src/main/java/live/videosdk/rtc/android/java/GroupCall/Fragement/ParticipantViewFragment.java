package live.videosdk.rtc.android.java.GroupCall.Adapter;


import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import live.videosdk.rtc.android.Meeting;

import live.videosdk.rtc.android.java.GroupCall.Fragement.ParticipantViewFragment;
import live.videosdk.rtc.android.listeners.MeetingEventListener;

public class ParticipantViewAdapter extends FragmentStateAdapter {

    Meeting meeting;
    int participantListSize;

    public ParticipantViewAdapter(@NonNull FragmentActivity fragmentActivity, Meeting meeting) {
        super(fragmentActivity);
        this.meeting=meeting;
        this.participantListSize = 4;
        meeting.addEventListener(new MeetingEventListener() {
            @Override
            public void onPresenterChanged(String participantId) {
                super.onPresenterChanged(participantId);
                if(!TextUtils.isEmpty(participantId)){
                    participantListSize = 2;
                }else{
                    participantListSize =4;
                }
                notifyDataSetChanged();
            }
        });
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new ParticipantViewFragment(meeting,position);
    }

    @Override
    public int getItemCount() {
        int size=(meeting.getParticipants().size()/participantListSize) +1;
        return size;
    }

}
