package live.videosdk.rtc.android.java.Common.Adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.java.R;
import live.videosdk.rtc.android.lib.PubSubMessage;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    Context context;
    int resource;
    List<PubSubMessage> messageList = new ArrayList<>();
    Meeting meeting;

    public MessageAdapter(Context context, int resource, List<PubSubMessage> messageList, Meeting meeting) {
        this.context = context;
        this.resource = resource;
        this.messageList = messageList;
        this.meeting = meeting;
    }

    public void addItem(PubSubMessage pubSubMessage) {
        messageList.add(pubSubMessage);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list,
                parent, false);
        return new ViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        PubSubMessage item = messageList.get(position);

        holder.message.setText(item.getMessage());
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        Date date = new Date(item.getTimestamp());
        holder.messageTime.setText(dateFormat.format(date));

        if (item.getSenderId().equals(meeting.getLocalParticipant().getId())) {
            holder.messageLayout.setGravity(Gravity.RIGHT);
            holder.senderName.setText("You");
        } else {
            holder.messageLayout.setGravity(Gravity.LEFT);
            holder.senderName.setText(item.getSenderName());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout messageLayout;
        TextView senderName;
        TextView message;
        TextView messageTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageLayout = itemView.findViewById(R.id.messageLayout);
            senderName = itemView.findViewById(R.id.senderName);
            message = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.messageTime);
        }
    }
}
