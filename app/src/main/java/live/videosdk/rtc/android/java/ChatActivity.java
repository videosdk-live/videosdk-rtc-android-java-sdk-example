package live.videosdk.rtc.android.java;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.lib.PubSubMessage;
import live.videosdk.rtc.android.listeners.PubSubMessageListener;
import live.videosdk.rtc.android.model.PubSubPublishOptions;

public class ChatActivity extends AppCompatActivity {
    private EditText etmessage;
    private MessageAdapter messageAdapter;
    Meeting meeting;
    private PubSubMessageListener pubSubMessageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //
        etmessage = findViewById(R.id.etMessage);

        //
        meeting = ((MainApplication) this.getApplication()).getMeeting();

        //
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Chat");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        //
        RecyclerView messageRecyclerView = (RecyclerView) findViewById(R.id.messageRcv);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        //
        pubSubMessageListener = new PubSubMessageListener() {
            @Override
            public void onMessageReceived(PubSubMessage message) {
                messageAdapter.addItem(message);
                messageRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
            }
        };

        // Subscribe for 'CHAT' topic
        List<PubSubMessage> pubSubMessageList = meeting.pubSub.subscribe("CHAT", pubSubMessageListener);

        //
        messageAdapter = new MessageAdapter(this, R.layout.item_message_list, pubSubMessageList, meeting);
        messageRecyclerView.setAdapter(messageAdapter);
        messageRecyclerView.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) ->
                messageRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1));

        //
        findViewById(R.id.btnSend).setOnClickListener(view -> {
            String message = etmessage.getText().toString();
            if (!message.equals("")) {
                PubSubPublishOptions publishOptions = new PubSubPublishOptions();
                publishOptions.setPersist(true);

                meeting.pubSub.publish("CHAT", message, publishOptions);
                etmessage.setText("");
            } else {
                Toast.makeText(ChatActivity.this, "Please Enter Message",
                        Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    protected void onDestroy() {
        // Unsubscribe for 'CHAT' topic
        meeting.pubSub.unsubscribe("CHAT", pubSubMessageListener);
        super.onDestroy();
    }
}