package live.videosdk.rtc.android.java;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
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

    public void additem(PubSubMessage pubSubMessage) {
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
        if (item.getSenderId().equals(meeting.getLocalParticipant().getId())) {
            holder.sentMessage.setVisibility(View.VISIBLE);
            holder.sentMessage.setText(item.getMessage());
            holder.receivedMessage.setVisibility(View.INVISIBLE);
        } else {
            holder.receivedMessage.setVisibility(View.VISIBLE);
            holder.receivedMessage.setText(item.getMessage());
            holder.sentMessage.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView sentMessage;
        TextView receivedMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sentMessage = itemView.findViewById(R.id.sentMessage);
            receivedMessage = itemView.findViewById(R.id.receivedMessage);

        }


    }


}
