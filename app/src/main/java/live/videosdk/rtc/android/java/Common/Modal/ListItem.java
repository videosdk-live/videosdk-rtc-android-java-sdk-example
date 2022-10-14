package live.videosdk.rtc.android.java.Common.Modal;

import android.graphics.drawable.Drawable;

public class ListItem {

    private String itemName;
    private Drawable itemIcon;
    private Boolean selected;
    private String itemDescription;

    public ListItem(String itemName, Drawable itemIcon) {
        this.itemName = itemName;

        this.itemIcon = itemIcon;
        this.selected = false;
        this.itemDescription = null;

    }

    public ListItem(String itemName, Drawable itemIcon, Boolean selected) {
        this.itemName = itemName;
        this.itemIcon = itemIcon;
        this.selected = selected;
        this.itemDescription = null;

    }

    public ListItem(String itemName,String itemDescription, Drawable itemIcon) {
        this.itemName = itemName;
        this.itemIcon = itemIcon;
        this.selected = false;
        this.itemDescription = itemDescription;

    }

    public String getItemName() {
        return itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }


    public Boolean isSelected() {
        return this.selected;
    }

    public Drawable getItemIcon() {
        return itemIcon;
    }
}
