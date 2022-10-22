package live.videosdk.rtc.android.java.Common.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import live.videosdk.rtc.android.java.Common.Modal.ListItem;
import live.videosdk.rtc.android.java.R;

public class AudioDeviceListAdapter extends ArrayAdapter<ListItem> {
    private final Context context;
    private final List<ListItem> audioDeviceList;

    public AudioDeviceListAdapter(@NonNull Context context, int resource, @NonNull List<ListItem> audioDeviceList) {
        super(context, resource, audioDeviceList);
        this.audioDeviceList = audioDeviceList;
        this.context = context;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.audio_device_list_layout, parent, false);
        TextView deviceName = rowView.findViewById(R.id.tv_device_name);
        ListItem audioDevice = audioDeviceList.get(position);
        if (audioDevice.isSelected())
            rowView.setBackgroundColor(context.getResources().getColor(R.color.md_grey_200));
        deviceName.setText(audioDevice.getItemName());

        return rowView;
    }

    @Override
    public int getCount() {
        return audioDeviceList.size();
    }
}
