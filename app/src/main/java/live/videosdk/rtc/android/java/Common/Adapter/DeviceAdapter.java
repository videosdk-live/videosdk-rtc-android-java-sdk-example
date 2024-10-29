package live.videosdk.rtc.android.java.Common.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import live.videosdk.rtc.android.VideoSDK;
import live.videosdk.rtc.android.java.R;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private final List<String> devices;
    private final ClickListener clickListener;

    public interface ClickListener {
        void onClick(String device);
    }

    public DeviceAdapter(List<String> mList, ClickListener clickListener) {
        this.devices = mList;
        this.clickListener = clickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView label;
        public ImageView icon;
        public ImageView tickIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
            icon = itemView.findViewById(R.id.icon);
            tickIcon = itemView.findViewById(R.id.checkMark);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_items_bottom_sheet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = devices.get(position);
        holder.label.setText(item);

        switch (item) {
            case "BLUETOOTH":
                holder.icon.setImageResource(R.drawable.baseline_bluetooth_connected_24);
                break;
            case "WIRED_HEADSET":
                holder.icon.setImageResource(R.drawable.baseline_headphones_24);
                break;
            case "SPEAKER_PHONE":
                holder.icon.setImageResource(R.drawable.baseline_volume_up_24);
                break;
            case "EARPIECE":
                holder.icon.setImageResource(R.drawable.phone_call);
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(item);
            }
        });

        String selectedAudioDevice = VideoSDK.getSelectedAudioDevice().getLabel();
        holder.tickIcon.setVisibility(holder.label.getText().equals(selectedAudioDevice) ? View.VISIBLE : View.GONE);
    }
}
