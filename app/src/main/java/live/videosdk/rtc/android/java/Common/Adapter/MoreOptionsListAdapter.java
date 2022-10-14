package live.videosdk.rtc.android.java.Common.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import live.videosdk.rtc.android.java.Common.Modal.ListItem;
import live.videosdk.rtc.android.java.R;

public class MoreOptionsListAdapter extends ArrayAdapter<ListItem> {
    private final Context context;
    private final List<ListItem> moreOptionsList;

    public MoreOptionsListAdapter(@NonNull Context context, int resource, @NonNull List<ListItem> objects) {
        super(context, resource, objects);
        moreOptionsList = objects;
        this.context = context;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.more_options_list_layout, parent, false);
        ImageView itemIcon = rowView.findViewById(R.id.iv_item_icon);
        TextView itemName = rowView.findViewById(R.id.tv_item_name);
        ListItem moreOptions = moreOptionsList.get(position);
        itemName.setText(moreOptions.getItemName());
        itemIcon.setImageDrawable(moreOptions.getItemIcon());

        return rowView;
    }

}