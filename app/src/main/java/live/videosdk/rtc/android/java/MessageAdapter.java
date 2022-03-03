package live.videosdk.rtc.android.java;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import live.videosdk.rtc.android.Meeting;
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
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.messageLayout.getLayoutParams();

        holder.message.setText(item.getMessage());
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        Date date = new Date(item.getTimestamp());
        holder.messageTime.setText(dateFormat.format(date));

        if (item.getSenderId().equals(meeting.getLocalParticipant().getId())) {
            holder.messageLayout.setGravity(Gravity.RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            holder.senderName.setText("You");
        } else {
            holder.messageLayout.setGravity(Gravity.LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            holder.senderName.setText(item.getSenderName());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout messageLayout;
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
