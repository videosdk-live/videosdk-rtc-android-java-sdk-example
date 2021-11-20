package live.videosdk.rtc.android.java;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.Stream;
import live.videosdk.rtc.android.lib.PeerConnectionUtils;
import live.videosdk.rtc.android.listeners.MeetingEventListener;
import live.videosdk.rtc.android.listeners.ParticipantEventListener;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.PeerViewHolder> {

    private final List<Participant> participants = new ArrayList<>();
    private int containerHeight;

    public ParticipantAdapter(Meeting meeting) {
        Handler mHandler = new Handler(Looper.getMainLooper());

        meeting.addEventListener(new MeetingEventListener() {
            @Override
            public void onParticipantJoined(Participant participant) {
                participants.add(participant);
                mHandler.post(() -> notifyItemInserted(participants.size() - 1));
            }

            @Override
            public void onParticipantLeft(Participant participant) {
                int pos = -1;
                for (int i = 0; i < participants.size(); i++) {
                    if (participants.get(i).getId().equals(participant.getId())) {
                        pos = i;
                        break;
                    }
                }

                participants.remove(participant);

                if (pos >= 0) {
                    int finalPos = pos;
                    mHandler.post(() -> notifyItemRemoved(finalPos));
                }
            }
        });
    }

    @NonNull
    @Override
    public PeerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        containerHeight = parent.getHeight();

        return new PeerViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_remote_peer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PeerViewHolder holder, int position) {
        final Participant participant = participants.get(position);

        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = containerHeight / 3;
        holder.itemView.setLayoutParams(layoutParams);

        holder.tvName.setText(participant.getDisplayName());

        for (Map.Entry<String, Stream> entry : participant.getStreams().entrySet()) {
            Stream stream = entry.getValue();
            if (stream.getKind().equalsIgnoreCase("video")) {
                holder.svrParticipant.setVisibility(View.VISIBLE);

                VideoTrack videoTrack = (VideoTrack) stream.getTrack();
                videoTrack.addSink(holder.svrParticipant);

                break;
            }
        }

        participant.addEventListener(new ParticipantEventListener() {
            @Override
            public void onStreamEnabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    holder.svrParticipant.setVisibility(View.VISIBLE);

                    VideoTrack videoTrack = (VideoTrack) stream.getTrack();
                    videoTrack.addSink(holder.svrParticipant);
                }
            }

            @Override
            public void onStreamDisabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    if (track != null) track.removeSink(holder.svrParticipant);

                    holder.svrParticipant.clearImage();
                    holder.svrParticipant.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return participants.size();
    }


    static class PeerViewHolder extends RecyclerView.ViewHolder {
        public SurfaceViewRenderer svrParticipant;
        public TextView tvName;
        public View itemView;

        PeerViewHolder(@NonNull View view) {
            super(view);

            itemView = view;

            tvName = view.findViewById(R.id.tvName);

            svrParticipant = view.findViewById(R.id.svrParticipant);
            svrParticipant.init(PeerConnectionUtils.getEglContext(), null);
        }
    }
}
