package live.videosdk.rtc.android.java;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
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

        participants.add(meeting.getLocalParticipant());

        meeting.addEventListener(new MeetingEventListener() {
            @Override
            public void onParticipantJoined(Participant participant) {
                participants.add(participant);
                notifyItemInserted(participants.size() - 1);
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
                    notifyItemRemoved(pos);
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
        Participant participant = participants.get(position);

        if (position == 0) {
            holder.btnMenu.setVisibility(View.INVISIBLE);
        }

        //
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = containerHeight / 3;
        holder.itemView.setLayoutParams(layoutParams);

        //
        holder.tvName.setText(participant.getDisplayName());

        for (Map.Entry<String, Stream> entry : participant.getStreams().entrySet()) {
            Stream stream = entry.getValue();
            if (stream.getKind().equalsIgnoreCase("video")) {
                holder.svrParticipant.setVisibility(View.VISIBLE);

                VideoTrack videoTrack = (VideoTrack) stream.getTrack();
                videoTrack.addSink(holder.svrParticipant);

                break;
            } else if (stream.getKind().equalsIgnoreCase("audio")) {
                holder.ivMicStatus.setImageResource(R.drawable.ic_baseline_mic_24);
            }
        }

        participant.addEventListener(new ParticipantEventListener() {
            @Override
            public void onStreamEnabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    holder.svrParticipant.setVisibility(View.VISIBLE);

                    VideoTrack videoTrack = (VideoTrack) stream.getTrack();
                    videoTrack.addSink(holder.svrParticipant);
                } else if (stream.getKind().equalsIgnoreCase("audio")) {
                    holder.ivMicStatus.setImageResource(R.drawable.ic_baseline_mic_24);
                }
            }

            @Override
            public void onStreamDisabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    if (track != null) track.removeSink(holder.svrParticipant);

                    holder.svrParticipant.clearImage();
                    holder.svrParticipant.setVisibility(View.GONE);
                } else if (stream.getKind().equalsIgnoreCase("audio")) {
                    holder.ivMicStatus.setImageResource(R.drawable.ic_baseline_mic_off_24);
                }
            }
        });

        //
        final Participant finalParticipant = participant;
        holder.btnMenu.setOnClickListener(v -> showPopup(holder, finalParticipant));
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull PeerViewHolder holder) {
        holder.svrParticipant.release();
        holder.svrParticipant.clearImage();
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull PeerViewHolder holder) {
        Participant participant = participants.get(holder.getPosition());
        holder.svrParticipant.init(PeerConnectionUtils.getEglContext(), null);
        for (Map.Entry<String, Stream> entry : participant.getStreams().entrySet()) {
            Stream stream = entry.getValue();
            if (stream.getKind().equalsIgnoreCase("video")) {
                holder.svrParticipant.setVisibility(View.VISIBLE);

                VideoTrack videoTrack = (VideoTrack) stream.getTrack();
                videoTrack.addSink(holder.svrParticipant);

                break;
            } else if (stream.getKind().equalsIgnoreCase("audio")) {
                holder.ivMicStatus.setImageResource(R.drawable.ic_baseline_mic_24);
            }
        }
        super.onViewAttachedToWindow(holder);
    }

    private void showPopup(PeerViewHolder holder, Participant participant) {
        PopupMenu popup = new PopupMenu(holder.itemView.getContext(), holder.btnMenu);

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_participant, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.remove) {
                participant.remove();
                return true;
            } else if (item.getItemId() == R.id.toggleMic) {
                toggleMic(participant);
                return true;
            } else if (item.getItemId() == R.id.toggleWebcam) {
                toggleWebcam(participant);
                return true;
            }

            return false;
        });

        popup.show();
    }

    private void toggleMic(Participant participant) {
        boolean micEnabled = false;

        for (Map.Entry<String, Stream> entry : participant.getStreams().entrySet()) {
            if (entry.getValue().getKind().equalsIgnoreCase("audio")) {
                micEnabled = true;
                break;
            }
        }

        if (micEnabled) {
            participant.disableMic();
        } else {
            participant.enableMic();
        }
    }

    private void toggleWebcam(Participant participant) {
        boolean webcamEnabled = false;

        for (Map.Entry<String, Stream> entry : participant.getStreams().entrySet()) {
            if (entry.getValue().getKind().equalsIgnoreCase("video")) {
                webcamEnabled = true;
                break;
            }
        }

        if (webcamEnabled) {
            participant.disableWebcam();
        } else {
            participant.enableWebcam();
        }
    }


    @Override
    public int getItemCount() {
        return participants.size();
    }


    static class PeerViewHolder extends RecyclerView.ViewHolder {
        public SurfaceViewRenderer svrParticipant;
        public TextView tvName;
        public View itemView;
        public ImageButton btnMenu;
        public ImageView ivMicStatus;

        PeerViewHolder(@NonNull View view) {
            super(view);

            itemView = view;

            tvName = view.findViewById(R.id.tvName);
            btnMenu = view.findViewById(R.id.btnMenu);

            svrParticipant = view.findViewById(R.id.svrParticipant);

            ivMicStatus = view.findViewById(R.id.ivMicStatus);
        }
    }
}