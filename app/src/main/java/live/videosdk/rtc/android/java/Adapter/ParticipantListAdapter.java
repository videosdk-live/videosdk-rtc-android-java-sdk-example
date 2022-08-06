package live.videosdk.rtc.android.java.Adapter;


import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Map;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.Stream;
import live.videosdk.rtc.android.java.R;
import live.videosdk.rtc.android.java.Roboto_font;
import live.videosdk.rtc.android.listeners.MeetingEventListener;
import live.videosdk.rtc.android.listeners.ParticipantEventListener;

public class ParticipantListAdapter extends RecyclerView.Adapter<ParticipantListAdapter.ViewHolder> {

    private ArrayList<Participant> participants=new ArrayList<>();
    private Context context;

    public ParticipantListAdapter(ArrayList<Participant> items, Meeting meeting, Context context) {
        this.context = context;
        participants.add(meeting.getLocalParticipant());
        participants.addAll(items);
        meeting.addEventListener(new MeetingEventListener() {
            @Override
            public void onParticipantJoined(Participant participant) {
                super.onParticipantJoined(participant);
                participants.add(participant);
                notifyItemInserted(participants.size() - 1);
            }

            @Override
            public void onParticipantLeft(Participant participant) {
                super.onParticipantLeft(participant);
                int pos = -1;
                for (int i = 0; i < participants.size(); i++) {
                    if (participants.get(i).getId().equals(participant.getId())) {
                        pos = i;
                        break;
                    }
                }

                participants.remove(participant);

                if (pos >= 0) {
                    notifyItemRemoved(pos);
                }
            }
        });

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant_list_layout, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Participant participant = participants.get(position);
        if(participants.get(position).isLocal()) {
            holder.particpantName.setText("You");
        }else{
            holder.particpantName.setText(participants.get(position).getDisplayName());
        }

        for (Map.Entry<String, Stream> entry : participant.getStreams().entrySet()) {
            Stream stream = entry.getValue();
            if (stream.getKind().equalsIgnoreCase("video")) {
                Log.d("TAG", "onBindViewHolder: " + stream.getTrack().state().toString());
                holder.camStatus.setImageResource(R.drawable.ic_video_camera);
                holder.camStatus.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.divider_color)));
                break;
            } else if (stream.getKind().equalsIgnoreCase("audio")) {
                Log.d("TAG", "onBindViewHolder: " + stream.getTrack().state().toString());
                holder.micStatus.setImageResource(R.drawable.ic_mic);
                holder.micStatus.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.divider_color)));
            }
        }

        participant.addEventListener(new ParticipantEventListener() {
            @Override
            public void onStreamEnabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    holder.camStatus.setImageResource(R.drawable.ic_video_camera);
                    holder.camStatus.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.divider_color)));
                }
                if (stream.getKind().equalsIgnoreCase("audio")) {
                    holder.micStatus.setImageResource(R.drawable.ic_mic);
                    holder.micStatus.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.divider_color)));
                }
            }

            @Override
            public void onStreamDisabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    holder.camStatus.setImageResource(R.drawable.ic_video_camera_off);
                    holder.camStatus.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.md_red_500)));
                }
                if (stream.getKind().equalsIgnoreCase("audio")) {
                    holder.micStatus.setImageResource(R.drawable.ic_mic_off);
                    holder.micStatus.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.md_red_500)));
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView particpantName;
        FloatingActionButton micStatus, camStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            particpantName = itemView.findViewById(R.id.participant_Name);
            particpantName.setTypeface(Roboto_font.getTypeFace(particpantName.getContext()));
            micStatus = itemView.findViewById(R.id.mic_status);
            camStatus = itemView.findViewById(R.id.cam_status);
        }
    }

}

